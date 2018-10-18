import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:imsdk/imsdk.dart';
import 'package:imsdk/MessageElement.dart';
import 'package:imsdk/Conversation.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final GlobalKey<ScaffoldState> _key = new GlobalKey();
  final TextEditingController _controllerAppId = new TextEditingController();
  final TextEditingController _controllerId = new TextEditingController();
  final TextEditingController _controllerSignature = new TextEditingController();
  String _platformVersion = 'Unknown';

  List<Conversation> _conversations = new List();
  @override
  void initState() {
    super.initState();
    initPlatformState();
    Imsdk.init();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await Imsdk.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    List<Widget> children = new List();
    children.addAll(_conversations.map((conversation){
      return new InkWell(
        onTap: (){
          Navigator.of(context).push(new MaterialPageRoute(builder: (context){
            return new ChatPage(conversation);
          }));
        },
        child: new Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: <Widget>[
            new Text(conversation.type),
            new Text(conversation.id),
          ],
        ),
      );
    }).toList());
    children.add(new Column(
      children: <Widget>[
        new Text('Running on: $_platformVersion\n'),
        new Row(
          children: <Widget>[
            new Expanded(
              child: new TextFormField(
                decoration: InputDecoration(
                    hintText: 'Enter your appId'
                ),
                controller: _controllerAppId,
              )
            ),
            new MaterialButton(onPressed: (){
              print(_controllerAppId.text);
              Imsdk.initSdk(int.parse(_controllerAppId.text));
            },child: new Text('Init sdk'),)
          ],
        ),
        new TextFormField(
          decoration: InputDecoration(
              hintText: 'Enter your id'
          ),
          controller: _controllerId,
        ),
        new TextFormField(
          decoration: InputDecoration(
              hintText: 'Enter your signature'
          ),
          controller: _controllerSignature,
        ),
        new MaterialButton(onPressed: (){
          Imsdk.login(_controllerId.text, _controllerSignature.text).then((result){
            print('Login returned $result');
            _key.currentState.showSnackBar(new SnackBar(content: Text('Login returned $result')));
            if(result){
              Imsdk.getConversationList().then((result){
                setState(() {
                  _conversations = result;
                });
              });
            }
          });
        },child: new Text('Login'),)
      ],
    ));
    return new MaterialApp(
      home: new Scaffold(
        key: _key,
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: new ListView(
          children: children,
        ),
      ),
    );
  }
}

class ChatPage extends StatefulWidget{
  final Conversation conversation;
  ChatPage(this.conversation);
  @override
  State<StatefulWidget> createState() {
    return new ChatPageState();
  }
}

class ChatPageState extends State<ChatPage>{
  final GlobalKey<ScaffoldState> _key = new GlobalKey();
  List<String> _textMessages = new List();
  String _message;
  @override
  void initState() {
    super.initState();
    Imsdk.onNewMessage = (message){
      print('---------------onNewMessage');
      print('elementCount:${message.count}');
      for(int i = 0;i < message.count;i ++){
        MessageElement e = message.elements[i];
        print('element $i type:${e.type}');
        print('text:${e.text}');
        _textMessages.add(e.text);
      }
      if(mounted){
        setState(() {

        });
      }
    };
  }
  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      key: _key,
      appBar: new AppBar(
        title: new Text('Chat with ${widget.conversation.id}'),
      ),
      body: new Stack(
        children: <Widget>[
          new Positioned.fill(
            child: new ListView(
              children: _textMessages.map((text){
                return new Text(text);
              }).toList(),
            ),
            bottom: 100.0,
          ),
          new Positioned(
            child: new Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: <Widget>[
                new TextFormField(
                  decoration: InputDecoration(
                      hintText: 'Enter your message'
                  ),
                  onSaved: (text){
                    _message = text;
                  },
                ),
                new MaterialButton(onPressed: (){

                  Imsdk.sendTextMessage(widget.conversation.type, widget.conversation.id, _message).then((result){
                    _key.currentState.showSnackBar(new SnackBar(content: Text('Send ${result ? 'succeed' : 'failed'}')));
                    if(result){
                      setState(() {
                        _textMessages.add(_message);
                      });
                    }
                  });
                },child: new Text('Send'),)
              ],
            ),
            bottom: 0.0,
            height: 100.0,
            left: 0.0,
            right: 0.0,
          )
        ],
      ),
    );
  }
}
