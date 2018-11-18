// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'message.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Message _$MessageFromJson(Map<String, dynamic> json) {
  return new Message(
      json['time'] as int,
      json['count'] as int,
      (json['elements'] as List)
          ?.map((e) => e == null
              ? null
              : new MessageElement.fromJson(e as Map<String, dynamic>))
          ?.toList(),
      json['sender'] as String,
      json['isSelf'] as bool,
      json['conversationType'] as String,
      json['conversationId'] as String);
}

abstract class _$MessageSerializerMixin {
  int get time;
  int get count;
  String get sender;
  bool get isSelf;
  List<MessageElement> get elements;
  String get conversationType;
  String get conversationId;
  Map<String, dynamic> toJson() => <String, dynamic>{
        'time': time,
        'count': count,
        'sender': sender,
        'isSelf': isSelf,
        'elements': elements,
        'conversationType': conversationType,
        'conversationId': conversationId
      };
}
