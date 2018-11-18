class Profile{
  String nickName;
  String avatar;

  Profile(this.nickName, this.avatar);

  static Profile createFromJson(dynamic json){
    return new Profile(json['nickName'] as String,
        json['avatar'] as String);
  }

}