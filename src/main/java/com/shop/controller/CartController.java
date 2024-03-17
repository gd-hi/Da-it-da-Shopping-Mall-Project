package com.shop.controller;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.service.CartService;
import com.shop.service.Payments.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;


@Tag(name = "Cart-Controller", description = "장바구니 관련 API")
@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    // 결제
    private final PaymentService paymentService;

    @PostMapping(value = "/cart")
    @Operation(summary = "장바구니에 상품 추가", description = "장바구니에 새로운 상품을 추가합니다.")
    public @ResponseBody
    ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto,
                         BindingResult bindingResult, Principal principal) throws IOException {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        String email = principal.getName();
        Long cartItemId;
        try {
            cartItemId = cartService.addCart(cartItemDto, principal);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @GetMapping(value = "/cart")
    @Operation(summary = "장바구니 목록 조회", description = "사용자의 장바구니에 담긴 상품 목록을 조회합니다.")
    public String orderHist(Principal principal, Model model){
        List<CartDetailDto> cartDetailDtoList = cartService.getCartList(principal);
        model.addAttribute("cartItems", cartDetailDtoList);

        return "cart/cartList";
    }

    @PatchMapping(value = "/cartItem/{cartItemId}")
    @Operation(summary = "장바구니 상품 수량 변경", description = "장바구니에 담긴 특정 상품의 수량을 변경합니다.")
    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                       int count, Principal principal) {
        System.out.println(cartItemId);

        if (count <= 0) {
            return new ResponseEntity<String>("최소 1개이상 담아주세요.", HttpStatus.BAD_REQUEST);
        } else if (!cartService.validateCartItem(cartItemId, principal)) {
            return new ResponseEntity<String>("수정권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItemCount(cartItemId, count);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }
    @DeleteMapping(value = "/cartItem/{cartItemId}")
    @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 특정 상품을 삭제합니다.")
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                       Principal principal){
        if (!cartService.validateCartItem(cartItemId, principal)) {
            return new ResponseEntity<String>("수정권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        cartService.deleteCartItem(cartItemId);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }
    @PostMapping(value = "/cart/orders")
    @Operation(summary = "장바구니 상품 주문", description = "장바구니에 담긴 상품을 주문합니다.")
    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto,
                                                      Principal principal) throws IOException {

        // 결제
        String token = paymentService.getToken();
        System.out.println("토큰 : " + token);
        int amount = paymentService.paymentInfo(cartOrderDto.getImpUid(), token);
        System.out.println("cartOrderDto.getImpUid() : " + cartOrderDto.getImpUid());
        System.out.println("결제 정보 : " + amount);

        System.out.println(cartOrderDto.getCartItemId());
        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        // cartOrderDtoList 검증
        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0){
            return new ResponseEntity<String>("주문할 상품을 선택해주세요.",HttpStatus.FORBIDDEN);
        }
        for(CartOrderDto cartOrder : cartOrderDtoList){
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal)){
                return new ResponseEntity<String>("주문 권한이 없습니다.",HttpStatus.FORBIDDEN);
            }
        }

        String impUid = cartOrderDto.getImpUid();
        Long orderId;

        try {
            orderId = cartService.orderCartItem(cartOrderDtoList, principal, impUid);
        }
        catch (Exception e){
            paymentService.payMentCancle(token, cartOrderDto.getImpUid(), amount, "결제 에러! 결제를 취소합니다.");
            return new ResponseEntity<String>("재고가 부족합니다.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Long>(orderId,HttpStatus.OK);
    }
}
