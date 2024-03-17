package com.shop.controller;

import com.shop.dto.NoticeDto;
import com.shop.entity.Notice;
import com.shop.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Tag(name = "Notice-Controller", description = "공지사항 관련 컨트롤러")
@Controller
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;
    @GetMapping(value = "/make")
    @Operation(summary = "공지사항 작성 폼", description = "공지사항을 작성할 수 있는 폼을 반환합니다.")
    public String memberForm(Model model){
        model.addAttribute("noticeDto",new NoticeDto());
        return "notice/noticeForm";
    }

    @PostMapping(value = "/make")
    @Operation(summary = "공지사항 작성 처리", description = "공지사항 작성 폼의 데이터를 처리하고 공지사항을 저장합니다.")
    public String memberForm(@Valid NoticeDto noticeDto,
                             BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return "notice/noticeForm";
        }
        try {
            noticeService.saveNotice(noticeDto);
        }catch(IllegalStateException e){
            model.addAttribute("errorMessage",e.getMessage());
            return "notice/noticeForm";
        }
        return "redirect:/";
    }


    @GetMapping(value = {"/notice", "/notice/items/{page}"})
    @Operation(summary = "공지사항 목록 조회", description = "페이징 처리된 공지사항 목록을 조회합니다.")
    public String noticeView(@PathVariable("page") Optional<Integer> page, Model model){
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 8);
        Page<Notice> notices = noticeService.getNotice(pageable);

        if (!notices.isEmpty()) { // 공지사항이 비어있지 않은 경우에만 처리
            System.out.println("확인 아이디" + notices.getContent().get(0).getId());
            System.out.println("확인 제목:" + notices.getContent().get(0).getTitle());
            System.out.println("확인 내용:" + notices.getContent().get(0).getContent());
        }

        model.addAttribute("notices", notices);
        model.addAttribute("maxPage", 8);

        return "notice/notice";
    }

}
