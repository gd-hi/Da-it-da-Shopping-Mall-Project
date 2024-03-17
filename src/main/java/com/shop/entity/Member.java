package com.shop.entity;


import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table (name = "member")
@Getter
@Setter
@ToString
public class Member extends BaseEntity{
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.AUTO) // autoincrement
    private Long id;
    private  String name;

    @Column (unique = true) // 중복 X
    private String email;

    private String password;
    private String address;
    private String telNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Member를 DB 로 넘겨주는 방식
    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        member.setTelNumber(memberFormDto.getTelNumber());

        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);

        // 여기서 역할을 설정
        member.setRole(memberFormDto.getRole());

        return member;
    }

    public void update(String name, String password, String address) {
        this.name = name;
        this.password = password;
        this.address = address;
    }


}
