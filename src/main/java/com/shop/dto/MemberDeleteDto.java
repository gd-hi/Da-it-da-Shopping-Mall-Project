package com.shop.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
public class MemberDeleteDto {
    @NotEmpty(message = "비밀번호는 필수 입력 값입니다.")
    private String password; // 탈퇴 시 비밀번호 확인용
}
