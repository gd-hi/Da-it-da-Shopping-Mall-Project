/* package com.shop.service;

import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.entity.*;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
// 상품과 주문한 고객을 조회
// 주문 상품 객체 생성 👉 주문 객체 생성
public class OrderService {

    private static final String API_KEY = "7022773244761756";
    private static final String API_SECRET = "i8KwqqUJwQ0RKGi7G1HIBAzW092l2yqey5CMmCJ8eVqRlfR1SitRdz1ryRVtUsMjsxswSmFgibXMNH8j";

    private final ItemRepository itemRepository; // 상품을 불러 와서 재고를 변경
    private final MemberRepository memberRepository; // 멤버를 불러 와서 연결
    private final OrderRepository orderRepository; // 주문 객체를 저장

    private final ItemImgRepository itemImgRepository;

    public Long order(OrderDto orderDto, String email){

        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);

        Member member = memberRepository.findByEmail(email);

        List<OrderItem> orderItemList = new ArrayList<>();

        // OrderItem.createOrderItem 👉 static 메소드
        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
        orderItemList.add(orderItem);

        // Order.createOrder 👉 static 메소드
        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();

    }

    // 주문 목록 조회 메소드
    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable){

        // 유저 email과 페이징 조건을 이용하여 주문 목록을 조회
        List<Order> orders = orderRepository.findOrders(email, pageable);
        // 유저의 주문 총 개수
        Long totalCount = orderRepository.countOrder(email);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        // 주문 리스트를 순회하면서 구매 이력 페이지에 전달할 DTO를 생성
        for (Order order : orders){

            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();

            for (OrderItem orderItem : orderItems){

                // 주문한 상품의 대표 이미지를 조회
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.
                        getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);

            }

            orderHistDtos.add(orderHistDto);

        }

        // 페이지 구현 객체를 생성하여 반환
        // 👉 order, orderItem Entity 객체를 각각 OrderHistDto, OrderItemDto 객체로 변환
        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);

    }


    @Transactional(readOnly = true)
    // 상품을 주문한 유저와 주문 취소를 요청한 유저가 동일한지 검증
    public boolean validateOrder(Long orderId, String email){

        Member curMember = memberRepository.findByEmail(email);
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        Member savedMember = order.getMember();

        // 현재 로그인된 멤버와 세이브된 멤버와 비교
        if(!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())){

            return false;

        }

        return true;

    }

    // 주문 취소 메소드 (변경 감지)
    public void cancelOrder(Long orderId){

        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        order.cacncelOrder();

    }

    // 장바구니 페이지에서 전달 받은 구매 상품으로 주문을 생성하는 메소드
    public Long orders(List<OrderDto> orderDtoList, String email){

        // 로그인한 유저 조회
        Member member = memberRepository.findByEmail(email);

        List<OrderItem> orderItemList = new ArrayList<>();

        // orderDto 객체를 이용하여 itme 객체와 count 값을 얻어 OrderItem 객체 생성
        for (OrderDto orderDto : orderDtoList){

            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(EntityNotFoundException::new);
            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());

            orderItemList.add(orderItem);

        }

        // Order Entity 클래스에 존재하는 createOrder 메소드로 Order 생성 및 저장
        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();

    }

} */

// 소셜로그인 USER 추가
// 고친 부분: 매개변수 String email 받는 걸 → principal 로 받고 memberService 메소드로 이메일 빼서 리턴
package com.shop.service;

import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.entity.*;
import com.shop.repository.*;
import com.shop.service.Payments.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemImgRepository itemImgRepository;
    private final MemberService memberService;

    // 결제 환불하기
    private final PaymentService paymentService;


    public Long order(OrderDto orderDto, Principal principal) {
        String email = memberService.checkEmail(principal);
        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByEmail(email);

        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
        orderItemList.add(orderItem);

        Order order = Order.createOrder(member, orderItemList);

        // OrderDto에서 받은 impUid를 Order 엔티티에 설정
        order.setImpUid(orderDto.getImpUid());

        orderRepository.save(order);

        return order.getId();
    }

    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(Principal principal, Pageable pageable) {
        String email = memberService.checkEmail(principal);
        List<Order> orders = orderRepository.findOrders(email, pageable);
        Long totalCount = orderRepository.countOrder(email);
        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.getItem().getId(),
                        "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }

            orderHistDtos.add(orderHistDto);
        }
        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, Principal principal) {
        String email = memberService.checkEmail(principal);
        Member curMember = memberRepository.findByEmail(email);
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        Member savedMember = order.getMember();

        if (!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())) {
            return false;
        }
        return true;
    }

    public void cancelOrder(Long orderId) throws IOException {
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        System.out.println("11111111111111111111111111111 : " + order.getImpUid());

        if(!"".equals(order.getImpUid())) {
            String token = paymentService.getToken();
            System.out.println("2222222222222222222222222222222 : " + token);
            int amount = paymentService.paymentInfo(order.getImpUid(), token);
            System.out.println("3333333333333333333333333333333 : " + amount);
            paymentService.payMentCancle(token, order.getImpUid(), amount, "주문 취소");
        }

        order.cancelOrder();
    }

    // 카트 오더
    public Long orders(List<OrderDto> orderDtoList, String email, String impUid){

        Member member = memberRepository.findByEmail(email);

        List<OrderItem> orderItemList = new ArrayList<>();

        for(OrderDto orderDto : orderDtoList){
            Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);
            OrderItem orderItem = OrderItem.createOrderItem(item,orderDto.getCount());
            orderItemList.add(orderItem);
        }
        Order order = Order.createOrder(member, orderItemList);

        if(impUid != null){
            order.setImpUid(impUid);

            System.out.println("카트 22222222222222 : " + order.getImpUid());
        }

        System.out.println("카트 3333333333333333333333");

        orderRepository.save(order);


        return order.getId();
    }
}