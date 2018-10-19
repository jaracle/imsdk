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
      json['sender'] as String);
}

abstract class _$MessageSerializerMixin {
  int get time;
  int get count;
  String get sender;
  List<MessageElement> get elements;
  Map<String, dynamic> toJson() => <String, dynamic>{
        'time': time,
        'count': count,
        'sender': sender,
        'elements': elements
      };
}
