package com.shop.service;

import com.shop.dto.MemberDeleteDto;
import com.shop.dto.MemberUpdateDto;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor // 롬복 어노테이션
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;
//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return new BCryptPasswordEncoder();
//    }

    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }
    public Member saveSnsMember(Member member) {
        return  memberRepository.save(member);
    }
    public boolean findMember(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member != null){
            return false;
        }
        return true;
    }
    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail());

        if (findMember != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email){
        Member member = memberRepository.findByEmail(email);

        if (member == null){
            throw new UsernameNotFoundException(email);
        }

        return User.builder().username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    public String checkEmail(Principal principal) {
        String email="";

        if (principal instanceof UsernamePasswordAuthenticationToken) {
            email = ((UsernamePasswordAuthenticationToken) principal).getName();
        } else if (principal instanceof RememberMeAuthenticationToken) {
            email = ((RememberMeAuthenticationToken) principal).getName();
        } else{
            if(((OAuth2AuthenticationToken) principal).getAuthorizedClientRegistrationId().equals("google")) {
                OAuth2User oAuth2User = ((OAuth2AuthenticationToken) principal).getPrincipal();
                email = oAuth2User.getAttribute("email");
            }
            OAuth2User oAuth2User = ((OAuth2AuthenticationToken) principal).getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            if(((OAuth2AuthenticationToken) principal).getAuthorizedClientRegistrationId().equals("naver")) {
                Map<String, Object> response2 = (Map<String, Object>) attributes.get("response");
                String realName = (String) response2.get("name");
                email = (String) response2.get("email");
            }
            if(((OAuth2AuthenticationToken) principal).getAuthorizedClientRegistrationId().equals("kakao")){
                Map<String, Object> profile = (Map<String, Object>) attributes.get("kakao_account");
                email = (String) profile.get("email");
            }
        }
        return email;
    }

    public void updateMember(MemberUpdateDto memberUpdateDto, String email) {
        Member member = memberRepository.findByEmail(email);
        if (!passwordEncoder.matches(memberUpdateDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        member.setName(memberUpdateDto.getName());
        member.setAddress(memberUpdateDto.getAddress());
        member.setTelNumber(memberUpdateDto.getTelNumber());

        // 새 비밀번호가 있으면 실행
        if (StringUtils.hasText(memberUpdateDto.getNewPassword())) {
            member.setPassword(passwordEncoder.encode(memberUpdateDto.getNewPassword()));
        }

        memberRepository.save(member);
    }


    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public void deleteMember(MemberDeleteDto memberDeleteDto, String email) {
        Member member = memberRepository.findByEmail(email);
        if (!passwordEncoder.matches(memberDeleteDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        memberRepository.delete(member);
    }



}
