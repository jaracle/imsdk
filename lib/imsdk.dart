import 'dart:async';

import 'package:flutter/services.dart';
import 'conversation.dart';
import 'message.dart';

typedef void OnNewMessage(Message message);

class Imsdk {
  static const MethodChannel _channel = const MethodChannel('imsdk');
  static OnNewMessage onNewMessage;
  static void init(){
    _channel.setMethodCallHandler((call){
      if(call.method == 'onNewMessage'){
        Message message = Message.createFromJson(call.arguments['message']);
        if(onNewMessage != null) onNewMessage(message);
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

  static Future<bool> sendTextMessage(String conversationType,String id,String text) async{
    bool ret = await _channel.invokeMethod('sendTextMessage',{
      'type':conversationType,
      'id':id,
      'content':text,
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

}
