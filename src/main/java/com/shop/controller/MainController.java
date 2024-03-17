package com.shop.controller;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Main-Controller", description = "메인 페이지 및 상품 조회 관련 API")
@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    private final ItemRepository itemRepository;

    private final MemberRepository memberRepository;

    @GetMapping(value = "/")
    @Operation(summary = "메인 페이지 조회", description = "메인 페이지와 상품 목록을 조회합니다. 최근 본 상품과 조회수가 높은 상품도 함께 표시됩니다.")
    public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model,
                       @CookieValue(value = "recentItems", defaultValue = "") String recentItems) throws UnsupportedEncodingException {

        if (itemSearchDto.getSearchQuery() == "") {
            itemSearchDto.setSearchQuery("");
        }

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 8);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);
        // 조회수 상위 4개 아이템을 가져오기
        Pageable topFive = PageRequest.of(0, 4);
        Page<Item> topViewedItems = itemRepository.findTop5ByOrderByViewDesc(topFive);

        System.out.println("현재 페이지 : " + items.getNumber());
        System.out.println("총 페이지 : " + items.getTotalPages());

        List<ItemFormDto> item = new ArrayList<>();

        for(ItemFormDto currentItem : item){
            ItemSellStatus sellStatus = currentItem.getItemSellStatus();
            System.out.println("ItemSellStatus: " + sellStatus);
        }

        // URL 디코딩을 수행
        String decodedRecentItems = URLDecoder.decode(recentItems, StandardCharsets.UTF_8.toString());
        // 쿠키에서 최근 본 상품 ID 목록을 가져옴
        String[] recentItemIds = decodedRecentItems.split("\\|");
        List<Item> recentViewedItems = new ArrayList<>();

        for (String itemIdStr : recentItemIds) {
            if (!itemIdStr.isEmpty() && itemIdStr.matches("\\d+")) {
                Long itemId = Long.parseLong(itemIdStr);
                itemRepository.findById(itemId).ifPresent(recentViewedItems::add);
            }
        }

        // 채팅 유저정보 가져오기
        String userNickName = getCurrentUserNickName();

        Member member = memberRepository.findByEmail(userNickName);

        // 회원 정보를 찾을 수 없는 경우
        if (member == null) {
            model.addAttribute("nickname", userNickName);
            model.addAttribute("items", items);
            model.addAttribute("itemSearchDto", itemSearchDto);
            model.addAttribute("topViewedItems", topViewedItems.getContent());
            model.addAttribute("maxPage", 5);
            model.addAttribute("recentItems", recentViewedItems);

            return "main";
        }

        String nickname = member.getName();


        model.addAttribute("nickname", nickname);
        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("topViewedItems", topViewedItems.getContent());
        model.addAttribute("maxPage", 5);
        // 최근 본 상품 목록을 모델에 추가
        model.addAttribute("recentItems", recentViewedItems);

        return "main";

    }

    private String getCurrentUserNickName() {
        // SecurityContext에서 인증 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            // 인증된 사용자의 이메일(아이디) 반환
            return authentication.getName();
        }

        // 인증 정보가 없거나 인증되지 않은 사용자는 "GUEST" 반환
        return "GUEST";
    }

    @GetMapping("/sortItems")
    @Operation(summary = "상품 정렬 조회", description = "지정된 기준에 따라 상품을 정렬하여 조회합니다.")
    @ResponseBody
    public List<Item> getSortedItems(@RequestParam String sortBy) {
        Sort sort;

        if (sortBy.equals("view_desc")) {
            sort = Sort.by(Sort.Direction.DESC, "view");
        } else if (sortBy.equals("reg_time_desc")) {
            sort = Sort.by(Sort.Direction.DESC, "regTime");
        } else if (sortBy.equals("reg_time_asc")) {
            sort = Sort.by(Sort.Direction.ASC, "regTime");
        } else if (sortBy.equals("price_asc")) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if (sortBy.equals("price_desc")) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "id");
        }

        return itemService.getItemsSorted(sort);
    }

    @GetMapping(value = "/load-more")
    @Operation(summary = "더 많은 상품 조회", description = "더 많은 상품을 동적으로 불러옵니다. 페이지 번호에 따라 추가 상품 정보를 로드합니다.")
    @ResponseBody
    public String loadMoreItems(ItemSearchDto itemSearchDto, @RequestParam(defaultValue = "0") int page
                                            , Model model) {

        System.out.println("SearchQuery: " + itemSearchDto.getSearchQuery());

        Pageable pageable = PageRequest.of(page, 8); // 상품 갯수

        if (itemSearchDto.getSearchQuery() == "") {
            System.out.println("검색어가 없습니다.");
            itemSearchDto.setSearchQuery("");
        }
        else{
            System.out.println("검색 진입");
            itemSearchDto.setSearchQuery((itemSearchDto.getSearchQuery()));
            System.out.println("검색어 : " + itemSearchDto.getSearchQuery());
        }

        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        StringBuilder response = new StringBuilder();

        for (MainItemDto item : items.getContent()) {
            response.append("<div class='col mb-5'>");
            response.append("<div class='a'>");
            response.append("<div class='card h-100 d-flex' style='border: 0; flex-direction: column;'>");
            response.append("<a href='/item/" + item.getId() + "' class='text-dark'>");
            if(Objects.equals(item.getItemSellStatus(), "SOLD_OUT")){
                String s = "sold-out-overlay";
                response.append("<div class=" + s + ">품절</div>");
            }
            response.append("<img src='" + item.getImgUrl() + "' class='card-img-top flex-grow-1 border border-light-subtle' alt='" + item.getItemNm() + "' style='object-fit: cover; height: 250px; width: 100%;'>");
            response.append("<div class='card-body' style='padding-left: 0;'>");
            response.append("<div class='text-left'>");
            response.append("<h4 class='card-title fw-bolder'>" + String.format("%,d", item.getPrice()) + "<abbr>원</abbr></h4>");
            response.append("<h6 class='fw-normal'>" + item.getItemNm().split("#")[0].substring(0, Math.min(30, item.getItemNm().split("#")[0].length())) + "</h6>");
            response.append("<p class='fw-bolder' style='font-size:0.8rem'>" + item.getItemDetail().substring(0, Math.min(30, item.getItemDetail().length())) + "</p>");
            response.append("</div>");
            response.append("</div>");
            response.append("</a>");
            response.append("</div>");
            response.append("</div>");
            response.append("</div>");
        }
        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);

        return response.toString();
    }

    @GetMapping(value = "/map")
    @Operation(summary = "지도 페이지 조회", description = "지도 페이지를 조회합니다.")
    public String map (){
        return "map";
    }

    @GetMapping(value = "/notice")
    @Operation(summary = "공지사항 페이지 조회", description = "공지사항 페이지를 조회합니다.")
    public String notice () {
        return  "notice";
    }

}
