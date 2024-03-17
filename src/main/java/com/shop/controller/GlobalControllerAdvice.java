package com.shop.controller;

import com.shop.service.CartService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

// 전역 컨트롤 설정
@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;

    public GlobalControllerAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    // 장바구니 갯수 전역적으로 확인
    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            // 수정된 부분: Authentication 객체에서 이름을 직접 가져옵니다.
            String email = authentication.getName();
            int cartItemCount = cartService.countCartItemsByEmail(email); // 가정: 이메일을 사용해 장바구니 항목 수를 계산하는 메소드
            model.addAttribute("cartItemCount", cartItemCount);
        } else {
            model.addAttribute("cartItemCount", 0);
        }
    }
}
