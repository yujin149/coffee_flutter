class Member {
  String? userid;
  String? name;
  String? email;
  String? emailCk;
  String? password;
  String? passwordCk;
  String? postcode;
  String? address;
  String? tel;
  String? birthdate;
  int? membership;
  String? loginType;
  bool emailVerified;

  Member({
    this.userid,
    this.name,
    this.email,
    this.emailCk,
    this.password,
    this.passwordCk,
    this.postcode,
    this.address,
    this.tel,
    this.birthdate,
    this.membership,
    this.loginType,
    this.emailVerified = false,
  });


  factory Member.fromJson(Map<String, dynamic> json) {
    return Member(
      userid: json['userid'],
      name: json['name'],
      email: json['email'],
      emailCk: json['emailCk'],
      password: json['password'],
      passwordCk: json['passwordCk'],
      postcode: json['postcode'],
      address: json['address'],
      tel: json['tel'],
      birthdate: json['birthdate'],
      membership: json['membership'],
      loginType: json['loginType'],
      emailVerified: json['emailVerified'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'userid': userid,
      'name': name,
      'email': email,
      'emailCk': emailCk,
      'password': password,
      'passwordCk': passwordCk,
      'postcode': postcode,
      'address': address,
      'tel': tel,
      'birthdate': birthdate,
      'membership': membership,
      'loginType': loginType,
      'emailVerified': emailVerified,
    };
  }
  factory Member.empty() {
    return Member(
      userid: '',
      name: 'Unknown',
      email: '',
      emailCk: '',
      password: '',
      passwordCk: '',
      postcode: '',
      address: '',
      tel: '',
      birthdate: '',
      membership: 0,
      loginType: 'NORMAL',
      emailVerified: false,
    );
  }
}