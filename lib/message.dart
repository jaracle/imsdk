import 'package:json_annotation/json_annotation.dart';
import 'message_element.dart';

part 'message.g.dart';

@JsonSerializable()
class Message extends Object with _$MessageSerializerMixin {
	Message(this.time,this.count,this.elements,this.sender,this.isSelf,this.conversationType,this.conversationId);

	int time;
	int count;
	String sender;
	bool isSelf;
	List<MessageElement> elements;
	String conversationType;
	String conversationId;

	factory Message.fromJson(Map<String, dynamic> json) => _$MessageFromJson(json);

	static Message createFromJson(dynamic json){
		return new Message(
				json['time'] as int,
				json['count'] as int,
				(json['elements'] as List)
						?.map((e) => e == null
						? null
						: MessageElement.createFromJson(e))
						?.toList(),
				json['sender'] as String,
				json['isSelf'] as bool,
				json['conversationType'] as String,
				json['conversationId'] as String,);
	}
}