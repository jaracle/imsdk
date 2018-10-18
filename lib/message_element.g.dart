// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'message_element.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MessageElement _$MessageElementFromJson(Map<String, dynamic> json) {
  return new MessageElement(json['type'] as String, json['text'] as String);
}

abstract class _$MessageElementSerializerMixin {
  String get type;
  String get text;
  Map<String, dynamic> toJson() =>
      <String, dynamic>{'type': type, 'text': text};
}
