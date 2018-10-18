import 'package:json_annotation/json_annotation.dart';

part 'Conversation.g.dart';

@JsonSerializable()
class Conversation extends Object with _$ConversationSerializerMixin {
	Conversation(this.type,this.id);

	String type;
	String id;

	factory Conversation.fromJson(Map<String, dynamic> json) => _$ConversationFromJson(json);

	static Conversation createFromJson(dynamic json) => _$ConversationFromJson(json);
}