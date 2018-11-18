import 'package:json_annotation/json_annotation.dart';

part 'message_element.g.dart';

@JsonSerializable()
class MessageElement extends Object with _$MessageElementSerializerMixin {
	MessageElement(this.type,this.text);

	String type;
	String text;

	factory MessageElement.fromJson(Map<String, dynamic> json) => _$MessageElementFromJson(json);

	static MessageElement createFromJson(dynamic json){
		return new MessageElement(json['type'] as String, json['text'] as String);
	}
}