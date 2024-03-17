package com.shop.controller;

import com.shop.dto.ItemFormDto;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MemberFormDto;
import com.shop.entity.Item;
import com.shop.repository.ItemRepository;
import com.shop.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Item-Controller", description = "상품 관련 API")
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private final ItemRepository itemRepository;

    @GetMapping(value = "/admin/item/new")
    @Operation(summary = "상품 등록 폼", description = "새 상품을 등록하기 위한 폼 페이지를 반환합니다.")
    public String itemForm(Model model){
        ItemFormDto itemFormDto = new ItemFormDto();

        model.addAttribute("itemFormDto", itemFormDto);

        return "item/itemForm";
    }

    @PostMapping(value = "/admin/item/new")
    @Operation(summary = "상품 등록", description = "새 상품을 등록합니다. 상품 이미지도 함께 업로드할 수 있습니다.")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("itemImgFile")List<MultipartFile> itemImgFileList) {

        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage",
                    "첫 번째 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }

        try {
            String combinedName = itemFormDto.getItemNm() + "#" + itemFormDto.getCategory();
            itemFormDto.setItemNm(combinedName);

            itemFormDto.setCategory(itemFormDto.getCategory());

            itemService.saveItem(itemFormDto, itemImgFileList);

        }catch (Exception e){
            model.addAttribute("errorMessage",
                    "상품 등록 중 에러가 발생하였습니다.");

            return "item/itemForm";
        }

        return "redirect:/";

    }

    @GetMapping(value = "/admin/item/{itemId}")
    @Operation(summary = "상품 상세 정보 조회", description = "상품 ID를 기반으로 상품의 상세 정보를 조회합니다.")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model){
        try {

            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);

        }catch (EntityNotFoundException e){

            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());

            return "item/itemForm";

        }

        return "item/itemForm";

    }

    @PostMapping(value = "/admin/item/{itemId}")
    @Operation(summary = "상품 정보 수정", description = "상품의 정보를 수정합니다. 상품 이미지도 함께 업데이트할 수 있습니다.")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                             @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
                             Model model){

        System.out.println("수정 컨트롤러 진입");

        if (bindingResult.hasErrors()){
            return "item/itemForm";
        }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수 입력 값 입니다.");
            return "item/itemForm";
        }

        try {
//            List<ItemFormDto> itemFormDtoList = new ArrayList<>();
//            for (ItemFormDto item : itemFormDtoList) {
//                System.out.println("item : " + item);
//            }

//            String combinedName = itemFormDto.getItemNm() + "#" + itemFormDto.getCategory();
//            itemFormDto.setItemNm(combinedName);
//
//            itemFormDto.setCategory(itemFormDto.getCategory());

            String temp[] = itemFormDto.getItemNm().split("#");
            String combinedName = temp[0] + "#" + itemFormDto.getCategory();
            itemFormDto.setItemNm(combinedName);
            System.out.println("수정 될 상품명 : " + combinedName);
            itemFormDto.setCategory(itemFormDto.getCategory());
            System.out.println("수정 될 카테고리 : " + itemFormDto.getCategory());

            itemService.updateItem(itemFormDto, itemImgFileList);
            model.addAttribute("itemFormDto", itemFormDto);

        }catch (Exception e){
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/";

    }

    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    @Operation(summary = "상품 관리 목록", description = "상품 검색 조건에 맞는 상품 목록을 페이지네이션으로 관리합니다.")
    public String itemManage(ItemSearchDto itemSearchDto,
                             @PathVariable("page") Optional<Integer> page,
                             Model model){
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 10);

        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 10);

        return "item/itemMng";

    }

    @GetMapping(value = "/item/{itemId}")
    @Operation(summary = "상품 상세 페이지 조회", description = "고객이 상품 상세 페이지를 조회할 때, 최근 본 상품 목록에 추가합니다.")
    public String itemDtl(Model model, MemberFormDto member, @PathVariable("itemId") Long itemId,
                          @CookieValue(value = "recentItems", defaultValue = "") String recentItems,
                          HttpServletResponse response) throws UnsupportedEncodingException {
        // 조회수 증가
        itemService.updateView(itemId);

        // 상품 조회
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        ItemFormDto itemFormDto = itemService.getItemDtl(itemId);

        // 쿠키에 최근 본 상품 추가
        String decodedRecentItems = recentItems;
        String newRecentItems = addRecentItemToCookie(decodedRecentItems, itemId.toString(), 5); // 5개만 저장

        // 새로운 쿠키를 생성하거나 기존 쿠키를 갱신
        // URLEncoder.encode를 사용하여 인코딩된 값을 쿠키에 설정합니다.
        Cookie cookie = new Cookie("recentItems", URLEncoder.encode(newRecentItems, StandardCharsets.UTF_8.toString()));
        cookie.setMaxAge(7 * 24 * 60 * 60); // 쿠키의 유효 시간을 7일로 설정
        cookie.setPath("/"); // 전체 경로에서 유효
        response.addCookie(cookie); // 응답에 쿠키 추가

        // 상품이 존재하는지 확인
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            // 조회수 확인
            System.out.println(item.getView());
        }

        model.addAttribute("item", itemFormDto);
        model.addAttribute("member", member);

        return "item/itemDtl";
    }


    private String addRecentItemToCookie(String recentItems, String newItemId, int maxItems) {
        // "|" 대신 "\\|"로 변경하여 올바르게 이스케이프 처리합니다.
        List<String> itemsList = new ArrayList<>(Arrays.asList(recentItems.split("\\|")));
        // 중복 상품 ID 제거
        itemsList.remove(newItemId);
        // 새 상품 ID 추가
        itemsList.add(0, newItemId);
        // 리스트를 maxItems 크기로 유지
        String encodedItems = itemsList.stream()
                .limit(maxItems)
                .collect(Collectors.joining("|"));
        // 여기에서는 URL 인코딩을 하지 않습니다.
        return encodedItems;
    }


//    @GetMapping(value = "/item/view/{itemId}")
//    public Item getView(@PathVariable("itemId") Long id){
//
//        System.out.println("**********************************");
//
//        Optional<Item> item = itemRepository.findById(id);
//        itemService.updateView(id);
//        return item.get();
//    }



}
