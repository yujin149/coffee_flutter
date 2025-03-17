package com.shop.dto;


import com.shop.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class MemberFormDto {

    @NotEmpty(message = "ID는 필수 입력 값입니다.",groups = {ValidationGroups.SignUp.class, ValidationGroups.Pwd.class})
    @Length(min = 8, max = 16, message = "아이디는 8자이상, 16자 이하로 입력해주세요.")
    private String userid;
    @NotBlank(message = "이름은 필수 입력 값입니다.", groups = {ValidationGroups.SignUp.class, ValidationGroups.Update.class}) //비어있으면 안된다.
    private String name;
    @NotEmpty(message = "이메일은 필수 입력 값입니다.")
    @Email
    private String email;
    @Length(min = 8,max = 8, message = "인증 코드는 8자리 입니다", groups = {ValidationGroups.SignUp.class,ValidationGroups.Pwd.class})
    private String emailCk;
    @NotEmpty(message = "비밀번호는 필수 입력 값입니다.", groups = {ValidationGroups.SignUp.class,ValidationGroups.Pwd.class})
    @Length(min = 8, max = 16, message = "비밀번호는 8자이상, 16자 이하로 입력해주세요.")
    private String password;
    @NotEmpty(message = "비밀번호 확인은 필수입니다." , groups = {ValidationGroups.SignUp.class,ValidationGroups.Pwd.class})
    private String passwordCk;

    @NotBlank(message = "주소는 필수 입력 값입니다.", groups = {ValidationGroups.SignUp.class, ValidationGroups.Update.class})
    private String postcode; // 우편번호
    @NotBlank(message = "주소는 필수 입력 값입니다.", groups = {ValidationGroups.SignUp.class, ValidationGroups.Update.class})
    private String address;

    @Pattern(regexp = "^(01[0-9])[-]?[0-9]{3,4}[-]?[0-9]{4}$", message = "전화번호는 010-XXXX-XXXX 또는 010XXXXXXXX 형식으로 입력해주세요.")
    @NotBlank(message = "전화번호는 필수 입력 입니다.", groups = {ValidationGroups.SignUp.class, ValidationGroups.Update.class})
    private String tel;

    private String birthdate;

    private Integer membership;

    private String loginType;

    //이메일인증 확인용
    private boolean emailVerified = false;


    private static ModelMapper modelMapper = new ModelMapper();


    public static MemberFormDto of(Member member) {
        MemberFormDto dto = modelMapper.map(member, MemberFormDto.class);
        dto.setLoginType(member.getLoginType().name()); // Enum 값을 String으로 변환하여 DTO에 추가

        return dto;
    }
}


