package com.shop.controller;

import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.service.CartService;
import com.shop.service.OrderService;
import com.shop.service.Payments.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Tag(name = "Order Controller", description = "주문 관련 컨트롤러")
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    // 결제
    private final PaymentService paymentService;

    @PostMapping(value = "/order")
    @Operation(summary = "상품 주문", description = "상품을 주문합니다. 결제 검증 후 주문을 진행합니다.")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid OrderDto orderDto,
                                              BindingResult bindingResult, Principal principal) throws IOException {

        // 1. 포트원 API 키와 SECRET키로 토큰을 생성
        // 토큰이 생성되는지 확인
        String token = paymentService.getToken();
        System.out.println("토큰 : " + token);

        // 2. 토큰으로 결제 완료된 주문정보를 가져옴
        // 결제 완료된 금액
        int amount = paymentService.paymentInfo(orderDto.getImpUid(), token);

        // 3. DB에서 실제 계산되어야 할 가격가져오기

        // 4. 결제 완료된 금액과 실제 계산되어야 할 금액이 다를경우 결제 취소

        // 5. 결제에러시 결제 취소

        if (bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();

            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrors){
                sb.append(fieldError.getDefaultMessage());
            }

            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);

        }
        String email = principal.getName();

        Long orderId;

        try {
            System.out.println("결제 : " + orderDto.getMakeMerchantUid());
            orderDto.setMakeMerchantUid(orderDto.getMakeMerchantUid());
            orderId = orderService.order(orderDto, principal);

        }catch (Exception e){
            paymentService.payMentCancle(token, orderDto.getImpUid(), amount, "결제 에러! 결제를 취소합니다.");
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(orderId, HttpStatus.OK);

    }



    @GetMapping(value = {"/orders", "/orders/{page}"})
    @Operation(summary = "주문 내역 조회", description = "사용자의 주문 내역을 페이지네이션으로 조회합니다.")
    public String orderHist(@PathVariable("page")Optional<Integer> page, Principal principal, Model model){

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);
        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(principal, pageable);

        for (OrderHistDto orderHistDto : orderHistDtoList){
            System.out.println("orderHistDto : " + orderHistDto);
        }

        model.addAttribute("orders", orderHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 10);

        return "order/orderHist";

    }

    @PostMapping("/order/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "특정 주문을 취소합니다. 주문 취소 권한 검증을 거칩니다.")
    // AJAX 형태!!!!
    public @ResponseBody ResponseEntity cancelOrder(@PathVariable("orderId")
                                                    Long orderId, Principal principal) throws IOException {

        if (!orderService.validateOrder(orderId, principal)){

            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);

        }

        orderService.cancelOrder(orderId);
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);

    }

    @PostMapping("/order/{orderId}/reOrder")
    @Operation(summary = "재주문", description = "취소된 주문을 재주문합니다. 재주문 권한 검증을 거칩니다.")
    // AJAX 형태!!!!
    public @ResponseBody ResponseEntity reOrder(@PathVariable("orderId")
                                                Long orderId, Principal principal){

        if (!orderService.validateOrder(orderId, principal)){
            return new ResponseEntity<String>("재주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        cartService.reAddCart(orderId,principal);

        return new ResponseEntity<Long>(orderId, HttpStatus.OK);

    }

}
