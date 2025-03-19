import 'package:flutter/material.dart';
import 'package:excoffee/models/member.dart'; // Member 모델을 임포트
import 'package:http/http.dart' as http;
import 'dart:convert';

class MemberForm extends StatefulWidget {
  const MemberForm({super.key});

  @override
  State<MemberForm> createState() => _MemberFormState();
}

class _MemberFormState extends State<MemberForm> {
  final _formKey = GlobalKey<FormState>();
  final Member _member = Member();

  // 텍스트 필드 컨트롤러들
  final TextEditingController _useridController = TextEditingController();
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final TextEditingController _passwordCkController = TextEditingController();
  final TextEditingController _addressController = TextEditingController();
  final TextEditingController _telController = TextEditingController();

  // 서버로 회원가입 요청하는 함수
  Future<void> _submitForm() async {
    if (_formKey.currentState?.validate() ?? false) {
      _formKey.currentState?.save();

      // Member 객체에 폼 데이터 설정
      _member.userid = _useridController.text;
      _member.name = _nameController.text;
      _member.email = _emailController.text;
      _member.password = _passwordController.text;
      _member.passwordCk = _passwordCkController.text;
      _member.address = _addressController.text;
      _member.tel = _telController.text;

      // 서버로 회원가입 요청 보내기 (예시 URL 사용)
      final response = await http.post(
        //Uri.parse('http://10.0.2.2:8080/members/api/new'), // 서버 URL을 여기에 추가
        Uri.parse('http://192.168.0.37:8080/members/api/new'), // 서버 URL을 여기에 추가
        headers: {'Content-Type': 'application/json'},
        body: json.encode(_member.toJson()),
      );

      if (response.statusCode == 200) {
        // 성공적으로 회원가입 처리 후 로그인 페이지로 이동 (예시)
        const SnackBar(content: Text("회원가입 성공!!"),);
        Navigator.pop(context);
      } else {
        // 에러 처리
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('회원가입 실패! 다시 시도해주세요.')),
        );
      }
    }
  }
  Widget _buildTextField(String label, TextEditingController controller, {bool obscureText = false}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 6),
        TextFormField(
          controller: controller,
          obscureText: obscureText,
          decoration: InputDecoration(
            border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
            filled: true,
            fillColor: Colors.grey[100],
            contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          ),
          validator: (value) => value == null || value.isEmpty ? "$label을 입력해주세요." : null,
        ),
        const SizedBox(height: 16),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('회원가입'),
      ),
      body: Center(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Container(
              width: 420,
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(10),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black12,
                    blurRadius: 10,
                    spreadRadius: 2,
                  ),
                ],
              ),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // const Text(
                    //   '회원 정보 입력',
                    //   style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                    // ),
                    const SizedBox(height: 16),
                    _buildTextField('아이디', _useridController),
                    _buildTextField('이름', _nameController),
                    _buildTextField('이메일', _emailController),
                    _buildTextField('비밀번호', _passwordController, obscureText: true),
                    _buildTextField('비밀번호 확인', _passwordCkController, obscureText: true),
                    _buildTextField('주소', _addressController),
                    _buildTextField('전화번호', _telController),
                    const SizedBox(height: 20),
                    SizedBox(
                      width: double.infinity,
                      height: 50,
                      child: ElevatedButton(
                        onPressed: _submitForm,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Color(0xFFEE3424),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        ),
                        child: const Text(
                          '회원가입',
                          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white, ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  // @override
  // Widget build(BuildContext context) {
  //   return Scaffold(
  //     appBar: AppBar(
  //       title: const Text('회원가입'),
  //     ),
  //     body: Padding(
  //       padding: const EdgeInsets.all(16.0),
  //       child: Form(
  //         key: _formKey,
  //         child: ListView(
  //           children: [
  //             // 사용자 아이디
  //             TextFormField(
  //               controller: _useridController,
  //               decoration: const InputDecoration(labelText: '아이디'),
  //               validator: (value) {
  //                 if (value == null || value.isEmpty) {
  //                   return '아이디를 입력해주세요.';
  //                 }
  //                 return null;
  //               },
  //             ),
  //             // 사용자 이름
  //             TextFormField(
  //               controller: _nameController,
  //               decoration: const InputDecoration(labelText: '이름'),
  //               validator: (value) {
  //                 if (value == null || value.isEmpty) {
  //                   return '이름을 입력해주세요.';
  //                 }
  //                 return null;
  //               },
  //             ),
  //             // 사용자 이메일
  //             TextFormField(
  //               controller: _emailController,
  //               decoration: const InputDecoration(labelText: '이메일'),
  //               validator: (value) {
  //                 if (value == null || value.isEmpty) {
  //                   return '이메일을 입력해주세요.';
  //                 } else if (!RegExp(r'^[^@]+@[^@]+\.[^@]+').hasMatch(value)) {
  //                   return '유효한 이메일을 입력해주세요.';
  //                 }
  //                 return null;
  //               },
  //             ),
  //             // 비밀번호
  //             TextFormField(
  //               controller: _passwordController,
  //               decoration: const InputDecoration(labelText: '비밀번호'),
  //               obscureText: true,
  //               validator: (value) {
  //                 if (value == null || value.isEmpty) {
  //                   return '비밀번호를 입력해주세요.';
  //                 }
  //                 return null;
  //               },
  //             ),
  //             // 비밀번호 확인
  //             TextFormField(
  //               controller: _passwordCkController,
  //               decoration: const InputDecoration(labelText: '비밀번호 확인'),
  //               obscureText: true,
  //               validator: (value) {
  //                 if (value == null || value.isEmpty) {
  //                   return '비밀번호 확인을 입력해주세요.';
  //                 } else if (value != _passwordController.text) {
  //                   return '비밀번호가 일치하지 않습니다.';
  //                 }
  //                 return null;
  //               },
  //             ),
  //             // 주소
  //             TextFormField(
  //               controller: _addressController,
  //               decoration: const InputDecoration(labelText: '주소'),
  //               validator: (value) {
  //                 if (value == null || value.isEmpty) {
  //                   return '주소를 입력해주세요.';
  //                 }
  //                 return null;
  //               },
  //             ),
  //             // 전화번호
  //             TextFormField(
  //               controller: _telController,
  //               decoration: const InputDecoration(labelText: '전화번호'),
  //               validator: (value) {
  //                 if (value == null || value.isEmpty) {
  //                   return '전화번호를 입력해주세요.';
  //                 }
  //                 return null;
  //               },
  //             ),
  //             const SizedBox(height: 20),
  //             ElevatedButton(
  //               onPressed: _submitForm,
  //               child: const Text('회원가입'),
  //             ),
  //           ],
  //         ),
  //       ),
  //     ),
  //   );
  // }

}