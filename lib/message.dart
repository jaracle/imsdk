import 'package:json_annotation/json_annotation.dart';
import 'message_element.dart';

part 'message.g.dart';

@JsonSerializable()
class Message extends Object with _$MessageSerializerMixin {
	Message(this.time,this.count,this.elements);

	int time;
	int count;
	List<MessageElement> elements;

	factory Message.fromJson(Map<String, dynamic> json) => _$MessageFromJson(json);

	static Message createFromJson(dynamic json) => _$MessageFromJson(json);
}