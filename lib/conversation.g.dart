// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'conversation.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Conversation _$ConversationFromJson(Map<String, dynamic> json) {
  return new Conversation(json['type'] as String, json['id'] as String);
}

abstract class _$ConversationSerializerMixin {
  String get type;
  String get id;
  Map<String, dynamic> toJson() => <String, dynamic>{'type': type, 'id': id};
}
