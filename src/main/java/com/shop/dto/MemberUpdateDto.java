package com.shop.dto;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
public class MemberUpdateDto {
    @NotEmpty(message = "이름은 필수 입력 값입니다.")
    private String name;

    @NotEmpty(message = "주소는 필수 입력 값입니다.")
    private String address;

    @NotEmpty(message = "전화번호는 필수 입력 값입니다.")
    private String telNumber;

    private String currentPassword; // 현재 비밀번호 확인용

    // @Length(min = 8, max = 16, message = "새 비밀번호는 8자 이상, 16자 이하로 입력해주세요.")
    private String newPassword; // 새 비밀번호 (선택적)
}
