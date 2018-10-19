import 'package:json_annotation/json_annotation.dart';
import 'message_element.dart';

part 'message.g.dart';

@JsonSerializable()
class Message extends Object with _$MessageSerializerMixin {
	Message(this.time,this.count,this.elements,this.sender);

	int time;
	int count;
	String sender;
	List<MessageElement> elements;

	factory Message.fromJson(Map<String, dynamic> json) => _$MessageFromJson(json);

	static Message createFromJson(dynamic json) => _$MessageFromJson(json);
}