package com.shop.controller;

import com.shop.dto.MemberDeleteDto;
import com.shop.dto.MemberFormDto;
import com.shop.dto.MemberUpdateDto;
import com.shop.entity.Member;
import com.shop.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;


@Tag(name = "Member-Controller", description = "회원 관리와 관련된 컨트롤러")
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    private final PasswordEncoder passwordEncoder;

    @GetMapping(value = "/new")
    @Operation(summary = "회원 가입 폼", description = "회원 가입을 위한 폼을 반환합니다.")
    public String memberForm(Model model){

        model.addAttribute("memberFormDto", new MemberFormDto());

        return "member/memberForm";
    }


    @PostMapping(value = "/new")
    @Operation(summary = "회원 가입 처리", description = "회원 가입 폼을 처리합니다.")
    public String memberForm(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, RedirectAttributes redirectAttributes){

        if(bindingResult.hasErrors()){
            return "member/memberForm";
        }

        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입에 성공하였습니다!"); // 성공 메시지를 세션에 저장
        } catch (IllegalStateException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/members/new"; // 에러 발생 시 다시 회원가입 폼으로 리다이렉트
        }

        return "redirect:/"; // 성공 시 홈으로 리다이렉트
    }


    @GetMapping(value = "/login")
    @Operation(summary = "로그인 폼", description = "로그인을 위한 폼을 반환합니다.")
    public String loginMember(){

        return "member/memberLoginForm";

    }

    @GetMapping(value = "login/error")
    @Operation(summary = "로그인 에러", description = "로그인 에러 시 메시지를 표시합니다.")
    public String loginError(Model model){

        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");

        return "member/memberLoginForm";
    }

    @GetMapping("/update")
    @Operation(summary = "회원 정보 수정 폼", description = "로그인한 회원의 정보를 수정하기 위한 폼을 반환합니다.")
    public String updateForm(Principal principal, Model model) {
        String email = principal.getName(); // 현재 로그인한 사용자의 이메일을 가져옵니다.
        Member member = memberService.findMemberByEmail(email); // 이메일을 통해 회원 정보를 가져옵니다.

        if (member == null) {
            return "redirect:/"; // 회원 정보가 없으면 홈으로 리다이렉트
        }

        MemberUpdateDto memberUpdateDto = new MemberUpdateDto();
        memberUpdateDto.setName(member.getName());
        memberUpdateDto.setAddress(member.getAddress());
        memberUpdateDto.setTelNumber(member.getTelNumber());
        // 필요한 정보를 DTO에 설정

        model.addAttribute("memberUpdateDto", memberUpdateDto);
        return "member/memberEditForm"; // 회원 정보 수정 페이지로 이동
    }


    @PostMapping("/update")
    @Operation(summary = "회원 정보 수정 처리", description = "회원 정보 수정 폼을 통해 제출된 데이터로 회원 정보를 업데이트합니다.")
    public String updateMember(@Valid MemberUpdateDto memberUpdateDto, BindingResult bindingResult, Principal principal, Model model) {
        if (bindingResult.hasErrors()) {
            return "member/memberEditForm";
        }
        try {
            memberService.updateMember(memberUpdateDto, principal.getName());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberEditForm";
        }
        return "redirect:/members/logout";
    }

    @PostMapping("/delete")
    @Operation(summary = "회원 탈퇴 처리", description = "회원 탈퇴 요청을 처리합니다.")
    public String deleteMember(@Valid MemberDeleteDto memberDeleteDto, BindingResult bindingResult, Principal principal, Model model) {
        if (bindingResult.hasErrors()) {
            return "member/memberDeleteForm";
        }
        try {
            memberService.deleteMember(memberDeleteDto, principal.getName());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberDeleteForm";
        }
        return "redirect:/members/logout";
    }

    @GetMapping("/delete")
    @Operation(summary = "회원 탈퇴 폼", description = "회원 탈퇴를 위한 폼을 반환합니다.")
    public String deleteForm(Model model) {
        model.addAttribute("memberDeleteDto", new MemberDeleteDto());
        return "member/memberDeleteForm"; // 회원 탈퇴 페이지로 이동
    }





}
