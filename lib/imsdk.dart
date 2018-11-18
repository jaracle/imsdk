import 'dart:async';

import 'package:flutter/services.dart';
import 'conversation.dart';
import 'message.dart';
import 'profile.dart';

typedef void OnNewMessage(Message message);

class Imsdk {
  static const MethodChannel _channel = const MethodChannel('imsdk');
  static OnNewMessage onNewMessage;
  static void init(){
    _channel.setMethodCallHandler((call){
      if(call.method == 'onNewMessage'){
        Message message = Message.createFromJson(call.arguments['message']);
        if(onNewMessage != null) onNewMessage(message);
      }else if(call.method == "log"){
        String tag = call.arguments['tag'];
        String content = call.arguments['content'];
        print(tag+'----------'+content);
      }
    });
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> initSdk(int appId) async{
    await _channel.invokeMethod('initSdk',{
      'appId':appId
    });
  }

  static Future<bool> login(String id,String signature) async{
    bool ret = await _channel.invokeMethod('login',{
      'id':id,
      'signature':signature,
    });
    return ret;
  }

  static Future<List<Conversation>> getConversationList() async{
    List<dynamic> ret = await _channel.invokeMethod('getConversationList');
    return ret.map(Conversation.createFromJson).toList();
  }

  static Future<Conversation> getConversation(String type,String id) async{
    dynamic ret = await _channel.invokeMethod('getConversation',{
      'type':type,
      'id':id
    });
    return Conversation.createFromJson(ret);
  }

  ///Send text message to receiver<br><br>
  static Future<bool> sendTextMessage(String conversationType,String id,String text) async{
    bool ret = await _channel.invokeMethod('sendTextMessage',{
      'type':conversationType,
      'id':id,
      'content':text,
    });
    return ret;
  }

  ///Send text message to receiver<br><br>
  static Future<bool> sendImageMessage(String conversationType,String id,List<String> images) async{
    bool ret = await _channel.invokeMethod('sendImageMessage',{
      'type':conversationType,
      'id':id,
      'images':images,
    });
    return ret;
  }

  ///Send text message to receiver<br><br>
  static Future<bool> sendVoiceMessage(String conversationType,String id,String voiceFile,int duration) async{
    bool ret = await _channel.invokeMethod('sendVoiceMessage',{
      'type':conversationType,
      'id':id,
      'voiceFile':voiceFile,
      'duration':duration,
    });
    return ret;
  }

  ///Send text message to receiver<br><br>
  static Future<bool> sendEmojiMessage(String conversationType,String id,int emoji) async{
    bool ret = await _channel.invokeMethod('sendEmojiMessage',{
      'type':conversationType,
      'id':id,
      'emoji':emoji,
    });
    return ret;
  }

  static Future<List<Message>> getMessages(String conversationType,String id,int count) async{
    List<dynamic> ret = await _channel.invokeMethod('getMessage',{
      'type':conversationType,
      'id':id,
      'count':count,
    });
    return ret.map(Message.createFromJson).toList();
  }

  static Future<void> deleteConversation(String conversationType,String id) async{
    await _channel.invokeMethod('deleteConversation',{
      'type':conversationType,
      'id':id,
    });
  }

  static Future<Profile> getSelfProfile() async{
    dynamic ret = await _channel.invokeMethod('getSelfProfile');
    return Profile.createFromJson(ret);
  }

  static Future<List<Profile>> getUsersProfile(List<String> users) async{
    List<dynamic> ret = await _channel.invokeMethod('getUsersProfile',{
      'users':users,
    });
    return ret.map(Profile.createFromJson).toList();
  }

  static Future<bool> modifyProfile(String nickName,String avatar) async{
    bool ret = await _channel.invokeMethod('modifyProfile',{
      'nickName':nickName,
      'avatar':avatar
    });
    return ret;
  }

}
