package com.shop.service;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;
import com.shop.entity.*;
import com.shop.repository.*;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;
    private final MemberService memberService;
    private final OrderItemService orderItemService;


    public Long addCart(CartItemDto cartItemDto, Principal principal) {
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityExistsException::new);
        String email = memberService.checkEmail(principal);
        Member member = memberRepository.findByEmail(email);

        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());
        if (savedCartItem != null) {
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(Principal principal){
        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();
        String email = memberService.checkEmail(principal);
        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
            return cartDetailDtoList;
        }
        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());
        return cartDetailDtoList;
    }
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, Principal principal){
        String email = memberService.checkEmail(principal);
        Member curMember = memberRepository.findByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityExistsException::new);
        Member savedMember = cartItem.getCart().getMember();

        if (!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())) {
            return false;
        }
        return true;

    }
    public void updateCartItemCount(Long cartItemId, int count){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityExistsException::new);
        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId){
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityExistsException::new);
        cartItemRepository.delete(cartItem);
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, Principal principal, String impUid) {
        List<OrderDto> orderDtoList = new ArrayList<>();

        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityExistsException::new);
            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);

            // ipmUid 추출
            if (impUid == null && cartOrderDto.getImpUid() != null) {
                impUid = cartOrderDto.getImpUid();
                System.out.println("카트 444444444444 : " + impUid);
            }

        }
        String email = memberService.checkEmail(principal);
        Long orderId = orderService.orders(orderDtoList, email, impUid);

        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityExistsException::new);
            cartItemRepository.delete(cartItem);
        }
        return orderId;
    }

    public void reAddCart(Long orderId, Principal principal){
        List<OrderItem> orderItems = orderItemService.getReOrderItems(orderId);
        for(OrderItem orderItem : orderItems) {
            CartItemDto cartItemDto = new CartItemDto();
            cartItemDto.setItemId(orderItem.getItem().getId());
            cartItemDto.setCount(orderItem.getCount());
            addCart(cartItemDto, principal);
        }
    }

//    public int countCartItems(Principal principal) {
//        String email = memberService.checkEmail(principal);
//        Member member = memberRepository.findByEmail(email);
//        Cart cart = cartRepository.findByMemberId(member.getId());
//        if(cart == null) {
//            return 0;
//        }
//        return cartItemRepository.countByCartId(cart.getId());
//    }

    public int countCartItemsByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            return 0;
        }
        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) {
            return 0;
        }
        return cartItemRepository.countByCartId(cart.getId());
    }


}