package com.yuanjiajia.imsdk;


import android.content.Context;
import android.util.Log;


import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConnListener;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMElemType;
import com.tencent.imsdk.TIMFaceElem;
import com.tencent.imsdk.TIMFriendshipManager;
import com.tencent.imsdk.TIMFriendshipSettings;
import com.tencent.imsdk.TIMGroupEventListener;
import com.tencent.imsdk.TIMGroupMemberInfo;
import com.tencent.imsdk.TIMGroupSettings;
import com.tencent.imsdk.TIMGroupTipsElem;
import com.tencent.imsdk.TIMImage;
import com.tencent.imsdk.TIMImageElem;
import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMLogListener;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMRefreshListener;
import com.tencent.imsdk.TIMSNSChangeInfo;
import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.imsdk.TIMSoundElem;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMUser;
import com.tencent.imsdk.TIMUserConfig;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.imsdk.TIMUserStatusListener;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.group.TIMGroupAssistantListener;
import com.tencent.imsdk.ext.group.TIMGroupCacheInfo;
import com.tencent.imsdk.ext.group.TIMUserConfigGroupExt;
import com.tencent.imsdk.ext.message.TIMConversationExt;
import com.tencent.imsdk.ext.message.TIMManagerExt;
import com.tencent.imsdk.ext.message.TIMUserConfigMsgExt;
import com.tencent.imsdk.ext.sns.TIMFriendGroup;
import com.tencent.imsdk.ext.sns.TIMFriendshipProxyListener;
import com.tencent.imsdk.ext.sns.TIMUserConfigSnsExt;
import com.tencent.imsdk.protocol.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * ImsdkPlugin
 */
public class ImsdkPlugin implements MethodCallHandler {
    private Context context;
    private int appId;
    private MethodChannel channel;

    public ImsdkPlugin(Context context,MethodChannel channel) {
        this.context = context;
        this.channel = channel;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "imsdk");
        channel.setMethodCallHandler(new ImsdkPlugin(registrar.activeContext(),channel));
    }

    private void initSdk(int appId) {
        log("initSdk,appId="+appId);
        this.appId = appId;
        TIMSdkConfig config = new TIMSdkConfig(appId)
                .enableLogPrint(true)
                .enableCrashReport(true)
                .setLogLevel(TIMLogLevel.INFO)
                .setLogListener(new TIMLogListener() {
                    @Override
                    public void log(int i, String s, String s1) {

                    }
                });
        TIMManager.getInstance().init(context, config);
        setupUserSettings();
        //设置消息监听器，收到新消息时，通过此监听器回调
        TIMManager.getInstance().addMessageListener(new TIMMessageListener() {//消息监听器
            @Override
            public boolean onNewMessages(List<TIMMessage> msgs) {//收到新消息
                //消息的内容解析请参考消息收发文档中的消息解析说明
                for (TIMMessage message : msgs) {
                    handleNewMessage(message);
                }
                return true; //返回true将终止回调链，不再调用下一个新消息监听器
            }
        });
    }

    private void handleNewMessage(TIMMessage msg) {
        log("onNewMessage");
        for(int i = 0;i < msg.getElementCount();i ++){
            log("element "+i+" "+msg.getElement(i).getType());
        }
        Map<String,Object> args = new HashMap<>();
        args.put("message",parseMessage(msg));
        channel.invokeMethod("onNewMessage",args);
    }

    private int getUnreadMessageCount(TIMConversation conversation) {
        TIMConversationExt ext = new TIMConversationExt(conversation);
        return (int) ext.getUnreadMessageNum();
    }

    private void setAllMessagesRead(TIMConversation conversation, final TIMCallBack callback) {
        final TIMConversationExt ext = new TIMConversationExt(conversation);
        //获取本地消息
        ext.getMessage(1, null, new TIMValueCallBack<List<TIMMessage>>() {
            @Override
            public void onError(int code, String desc) {
                callback.onError(code, desc);
            }

            @Override
            public void onSuccess(List<TIMMessage> timMessages) {
                if (timMessages != null && timMessages.size() > 0) {
                    ext.setReadMessage(timMessages.get(0), callback);
                }
            }
        });
    }

    private void modifyProfile(String nickName,String avatar,final TIMCallBack callBack){
        TIMFriendshipManager.ModifyUserProfileParam param = new TIMFriendshipManager.ModifyUserProfileParam();
        param.setNickname(nickName);
        param.setFaceUrl(avatar);
        TIMFriendshipManager.getInstance().modifyProfile(param,callBack);
    }

    private TIMGroupSettings initGroupSettings() {
        TIMGroupSettings settings = new TIMGroupSettings();
        return settings;
    }

    private TIMFriendshipSettings initFriendshipSettings() {
        TIMFriendshipSettings settings = new TIMFriendshipSettings();
        return settings;
    }

    private void setupUserSettings() {
        //基本用户配置
        TIMUserConfig userConfig = new TIMUserConfig()
//                //设置群组资料拉取字段
//                .setGroupSettings(initGroupSettings())
//                //设置资料关系链拉取字段
//                .setFriendshipSettings(initFriendshipSettings())
                //设置用户状态变更事件监听器
                .setUserStatusListener(new TIMUserStatusListener() {
                    @Override
                    public void onForceOffline() {
                        //被其他终端踢下线
                    }

                    @Override
                    public void onUserSigExpired() {
                        //用户签名过期了，需要刷新 userSig 重新登录 SDK
                    }
                })
                //设置连接状态事件监听器
                .setConnectionListener(new TIMConnListener() {
                    @Override
                    public void onConnected() {
                    }

                    @Override
                    public void onDisconnected(int code, String desc) {
                    }

                    @Override
                    public void onWifiNeedAuth(String name) {
                    }
                })
                //设置群组事件监听器
                .setGroupEventListener(new TIMGroupEventListener() {
                    @Override
                    public void onGroupTipsEvent(TIMGroupTipsElem elem) {
                    }
                })
                //设置会话刷新监听器
                .setRefreshListener(new TIMRefreshListener() {
                    @Override
                    public void onRefresh() {
                    }

                    @Override
                    public void onRefreshConversation(List<TIMConversation> conversations) {
                    }
                });
        //消息扩展用户配置
        userConfig = new TIMUserConfigMsgExt(userConfig)
                //禁用消息存储
                .enableStorage(true)
                //开启消息已读回执
                .enableReadReceipt(true);
        //资料关系链扩展用户配置
        userConfig = new TIMUserConfigSnsExt(userConfig)
                //开启资料关系链本地存储
                .enableFriendshipStorage(true)
                //设置关系链变更事件监听器
                .setFriendshipProxyListener(new TIMFriendshipProxyListener() {
                    @Override
                    public void OnAddFriends(List<TIMUserProfile> users) {
                    }

                    @Override
                    public void OnDelFriends(List<String> identifiers) {
                    }

                    @Override
                    public void OnFriendProfileUpdate(List<TIMUserProfile> profiles) {
                    }

                    @Override
                    public void OnAddFriendReqs(List<TIMSNSChangeInfo> reqs) {
                    }

                });
        //群组管理扩展用户配置
        userConfig = new TIMUserConfigGroupExt(userConfig)
                //开启群组资料本地存储
                .enableGroupStorage(true)
                //设置群组资料变更事件监听器
                .setGroupAssistantListener(new TIMGroupAssistantListener() {
                    @Override
                    public void onMemberJoin(String groupId, List<TIMGroupMemberInfo> memberInfos) {
                    }

                    @Override
                    public void onMemberQuit(String groupId, List<String> members) {
                    }

                    @Override
                    public void onMemberUpdate(String groupId, List<TIMGroupMemberInfo> memberInfos) {
                    }

                    @Override
                    public void onGroupAdd(TIMGroupCacheInfo groupCacheInfo) {
                    }

                    @Override
                    public void onGroupDelete(String groupId) {
                    }

                    @Override
                    public void onGroupUpdate(TIMGroupCacheInfo groupCacheInfo) {
                    }
                });

//将用户配置与通讯管理器进行绑定
        TIMManager.getInstance().setUserConfig(userConfig);
    }

    private void login(String identifier, String signature,TIMCallBack callback) {
        // identifier 为用户名，userSig 为用户登录凭证
        //发起登录请求
        TIMManager.getInstance().login(identifier, signature, callback);
    }


    private void offlineLogin(String identifier,TIMCallBack callback) {
        TIMManagerExt.getInstance().initStorage(identifier,callback);
    }

    private void deleteConversation(TIMConversationType type, String id) {
        TIMManagerExt.getInstance().deleteConversationAndLocalMsgs(type, id);
    }

    private TIMConversation getConversation(TIMConversationType type, String id) {
        //获取会话实例
        TIMConversation conversation = TIMManager.getInstance().getConversation(type, id);
        return conversation;
    }

    private void getMessage(TIMConversation conversation, int count,TIMValueCallBack<List<TIMMessage>> callBack) {
        //获取会话扩展实例
        TIMConversationExt ext = new TIMConversationExt(conversation);
        //获取本地消息
        ext.getMessage(count, null, callBack);
    }

    private void getLocalMessage(TIMConversation conversation, int count) {
        //获取会话扩展实例
        TIMConversationExt ext = new TIMConversationExt(conversation);
        //获取本地消息
        ext.getLocalMessage(count, null, new TIMValueCallBack<List<TIMMessage>>() {
            @Override
            public void onError(int code, String desc) {
            }

            @Override
            public void onSuccess(List<TIMMessage> timMessages) {
            }
        });
    }

    private List<TIMConversation> getConversationList() {
        return TIMManagerExt.getInstance().getConversationList();
    }

    private void sendTextMessage(TIMMessage msg,String text, TIMConversation conversation,TIMValueCallBack<TIMMessage> callBack) {
        //添加文本内容
        TIMTextElem elem = new TIMTextElem();
        elem.setText(text);
        //将elem添加到消息
        if (msg.addElement(elem) != 0) {
            return;
        }
        conversation.sendMessage(msg, callBack);
    }

    private void sendEmojiMessage(TIMMessage msg,int emoji, TIMConversation conversation,TIMValueCallBack<TIMMessage> callBack) {
        TIMFaceElem elem = new TIMFaceElem();
        elem.setIndex(emoji);
        //将elem添加到消息
        if (msg.addElement(elem) != 0) {
            return;
        }
        conversation.sendMessage(msg, callBack);
    }

    private void sendVoiceMessage(TIMMessage msg,String voiceFile,int duration, TIMConversation conversation,TIMValueCallBack<TIMMessage> callBack) {
        TIMSoundElem elem = new TIMSoundElem();
        elem.setPath(voiceFile);
        elem.setDuration(duration);
        //将elem添加到消息
        if (msg.addElement(elem) != 0) {
            return;
        }
        conversation.sendMessage(msg, callBack);
    }

    private void sendImageMessage(TIMMessage msg,TIMConversation conversation,TIMValueCallBack<TIMMessage> callBack,List<String> images) {
        if(images != null){
            for(String image : images){
                TIMImageElem elem = new TIMImageElem();
                elem.setPath(image);
                elem.setLevel(1);
                //将elem添加到消息
                if (msg.addElement(elem) != 0) {
                    return;
                }
            }
            conversation.sendMessage(msg, callBack);
        }
    }

    private TIMMessage createBaseMessage(String ... params){
        //构造一条消息
        TIMMessage msg = new TIMMessage();
        if(params != null){
            for(String param : params){
                TIMTextElem elem = new TIMTextElem();
                elem.setText(param);
                if (msg.addElement(elem) != 0) {
                    return null;
                }
            }
        }
        return msg;
    }

    private void logout(TIMCallBack callBack) {
        //登出
        TIMManager.getInstance().logout(callBack);
    }

    private void log(String text) {
        Log.e("IMSDK",text);
        Map<String,String> args = new HashMap<>();
        args.put("tag","IMSDK");
        args.put("content",text);
        channel.invokeMethod("log",args);
    }

    @Override
    public void onMethodCall(MethodCall call, final Result result) {
        if (isMethod(call,"getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        }else if(isMethod(call,"initSdk")){
            int appId = call.argument("appId");
            initSdk(appId);
            result.success(null);
        }else if(isMethod(call,"login")){
            String id = call.argument("id");
            String signature = call.argument("signature");
            login(id, signature, new TIMCallBack() {
                @Override
                public void onError(int i, String s) {
                    result.error("IMSDK","Login error code:"+i+" reason:"+s,null);
                }

                @Override
                public void onSuccess() {
                    result.success(true);
                }
            });
        }else if(isMethod(call,"getConversationList")){
            List<TIMConversation> conversations = getConversationList();
            List<Map<String,Object>> ret = new ArrayList<>();
            for(TIMConversation conversation : conversations){
                ret.add(encodeConversation(conversation));
            }
            result.success(ret);
        }else if(isMethod(call,"sendTextMessage")){
            String content = call.argument("content");
            TIMConversation conversation = parseConversation(call);
            if(conversation != null){
                log("Ready to send text message "+content+" in conversation "+conversation.getPeer());
                sendTextMessage(
                        createBaseMessage(),
                        content, conversation,
                        new TIMValueCallBack<TIMMessage>() {
                            @Override
                            public void onError(int i, String s) {
                                result.error("IMSDK","Error sending text message.code:"+i+" reason:"+s,null);
                            }

                            @Override
                            public void onSuccess(TIMMessage timMessage) {
                                result.success(true);
                            }
                        }
                );
            }else{
                result.error("IMSDK","sendTextMessage,Invalid conversation.",null);
            }
        }else if(isMethod(call,"sendImageMessage")){
            List images = call.argument("images");
            TIMConversation conversation = parseConversation(call);
            if(conversation != null){
                log("Ready to send image in conversation "+conversation.getPeer());
                sendImageMessage(
                    createBaseMessage(),
                    conversation,
                    new TIMValueCallBack<TIMMessage>() {
                        @Override
                        public void onError(int i, String s) {
                            result.error("IMSDK","Error sending image message.code:"+i+" reason:"+s,null);
                        }

                        @Override
                        public void onSuccess(TIMMessage timMessage) {
                            result.success(true);
                        }
                    },
                    images
                );
            }else{
                result.error("IMSDK","sendImageMessage,Invalid conversation.",null);
            }
        }else if(isMethod(call,"sendVoiceMessage")){
            String voiceFile = call.argument("voiceFile");
            int duration = call.argument("duration");
            TIMConversation conversation = parseConversation(call);
            if(conversation != null){
                log("Ready to send voice message "+voiceFile+" in conversation "+conversation.getPeer());
                sendVoiceMessage(
                        createBaseMessage(),
                        voiceFile,duration, conversation,
                        new TIMValueCallBack<TIMMessage>() {
                            @Override
                            public void onError(int i, String s) {
                                result.error("IMSDK","Error sending voice message.code:"+i+" reason:"+s,null);
                            }

                            @Override
                            public void onSuccess(TIMMessage timMessage) {
                                result.success(true);
                            }
                        }
                );
            }else{
                result.error("IMSDK","sendVoiceMessage,Invalid conversation.",null);
            }
        }else if(isMethod(call,"sendEmojiMessage")){
            int emoji = call.argument("emoji");
            TIMConversation conversation = parseConversation(call);
            if(conversation != null){
                log("Ready to send emoji message "+emoji+" in conversation "+conversation.getPeer());
                sendEmojiMessage(
                        createBaseMessage(),
                        emoji, conversation,
                        new TIMValueCallBack<TIMMessage>() {
                            @Override
                            public void onError(int i, String s) {
                                result.error("IMSDK","Error sending emoji message.code:"+i+" reason:"+s,null);
                            }

                            @Override
                            public void onSuccess(TIMMessage timMessage) {
                                result.success(true);
                            }
                        }
                );
            }else{
                result.error("IMSDK","sendEmojiMessage,Invalid conversation.",null);
            }
        }else if(isMethod(call,"getMessage")){
            int count = call.argument("count");
            TIMConversation conversation = parseConversation(call);
            if(conversation != null){
                getMessage(conversation, count, new TIMValueCallBack<List<TIMMessage>>() {
                    @Override
                    public void onError(int i, String s) {
                        result.error("IMSDK","Error getting message.code:"+i+" reason:"+s,null);
                    }

                    @Override
                    public void onSuccess(List<TIMMessage> timMessages) {
                        result.success(parseMessageList(timMessages));
                    }
                });
            }else{
                result.error("IMSDK","getMessage,Invalid conversation.",null);
            }
        }else if(isMethod(call,"getConversation")){
            TIMConversation conversation = parseConversation(call);
            if(conversation != null){
                result.success(encodeConversation(conversation));
            }else{
                result.error("IMSDK","Error getting conversation.",null);
            }
        }else if(isMethod(call,"deleteConversation")){
            String type = call.argument("type");
            String id = call.argument("id");
            if(type.equals("C2C")){
                deleteConversation(TIMConversationType.C2C,id);
                result.success(null);
            }else if(type.equals("Group")){
                deleteConversation(TIMConversationType.Group,id);
                result.success(null);
            }else{
                result.error("IMSDK","Error getting conversation.",null);
            }
        }else if(isMethod(call,"getSelfProfile")){
            TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
                @Override
                public void onError(int i, String s) {
                    result.error("IMSDK","Error getting profile.code:"+i+" reason:"+s,null);
                }

                @Override
                public void onSuccess(TIMUserProfile timUserProfile) {
                    result.success(encodeProfile(timUserProfile));
                }
            });
        }else if(isMethod(call,"getUsersProfile")){
            List users = call.argument("users");
            TIMFriendshipManager.getInstance().getUsersProfile(users, new TIMValueCallBack<List<TIMUserProfile>>() {
                @Override
                public void onError(int i, String s) {
                    result.error("IMSDK","Error getting profiles.code:"+i+" reason:"+s,null);
                }

                @Override
                public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                    List profiles = new ArrayList();
                    for(TIMUserProfile profile : timUserProfiles){
                        profiles.add(encodeProfile(profile));
                    }
                    result.success(profiles);
                }
            });
        }else if(isMethod(call,"modifyProfile")){
            String nickName = call.argument("nickName");
            String avatar = call.argument("avatar");
            modifyProfile(nickName, avatar, new TIMCallBack() {
                @Override
                public void onError(int i, String s) {
                    result.error("IMSDK","Error modifying profile.code:"+i+" reason:"+s,null);
                }

                @Override
                public void onSuccess() {
                    result.success(true);
                }
            });
        }
    }

    private Map<String,Object> encodeProfile(TIMUserProfile profile){
        Map<String,Object> result = new HashMap<>();
        result.put("nickName",profile.getNickName());
        result.put("avatar",profile.getFaceUrl());
        return result;
    }

    private Map<String,Object> encodeConversation(TIMConversation conversation){
        Map<String,Object> ret = new HashMap<>();
        ret.put("type",conversation.getType().toString());
        ret.put("id",conversation.getPeer());
        return ret;
    }

    private List<Map<String,Object>> parseMessageList(List<TIMMessage> messageList){
        List<Map<String,Object>> ret = new ArrayList<>();
        for(TIMMessage message : messageList){
            ret.add(parseMessage(message));
        }
        return ret;
    }



    private Map<String,Object> parseMessage(TIMMessage message){
        Map<String,Object> map = new HashMap<>();
        map.put("time",message.timestamp());
        map.put("count",message.getElementCount());
        map.put("sender",message.getSender());
        map.put("isSelf",message.isSelf());
        map.put("conversationType",message.getConversation().getType().toString());
        map.put("conversationId",message.getConversation().getPeer());
        List<Map<String,Object>> elements = new ArrayList<>();
        for(int i = 0; i < message.getElementCount(); ++i) {
            TIMElem elem = message.getElement(i);
            //获取当前元素的类型
            TIMElemType elemType = elem.getType();
            Map<String,Object> data = new HashMap<>();
            if (elemType == TIMElemType.Text) {
                TIMTextElem e = (TIMTextElem)elem;
                //处理文本消息
                data.put("text",e.getText());
            } else if (elemType == TIMElemType.Image) {
                //处理图片消息
                TIMImageElem e = (TIMImageElem)elem;
                List<TIMImage> imageList = e.getImageList();
                List<Map<String,Object>> items = new ArrayList<>();
                for(TIMImage image : imageList){
                    log("image------------"+image.getUrl());
                    Map<String,Object> item = new HashMap<>();
                    item.put("uuid",image.getUuid());
                    item.put("url",image.getUrl());
                    item.put("width",image.getWidth());
                    item.put("height",image.getHeight());
                    items.add(item);
                }
                data.put("images",items);
            }else if(elemType == TIMElemType.Sound){
                TIMSoundElem e = (TIMSoundElem)elem;
                data.put("file",e.getPath());
                data.put("uuid",e.getUuid());
                data.put("duration",e.getDuration());
            }else if(elemType == TIMElemType.Face){
                TIMFaceElem e = (TIMFaceElem)elem;
                data.put("index",e.getIndex());
            }
            elements.add(data);
        }
        map.put("elements",elements);
        return map;
    }

    private TIMConversation parseConversation(MethodCall call){
        String type = call.argument("type");
        String id = call.argument("id");
        if(type.equals("C2C")){
            return getConversation(TIMConversationType.C2C,id);
        }else if(type.equals("Group")){
            return getConversation(TIMConversationType.Group,id);
        }
        return null;
    }

    private boolean isMethod(MethodCall call,String method){
        return call.method.equals(method);
    }
}
