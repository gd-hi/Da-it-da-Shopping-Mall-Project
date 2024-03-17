package com.shop.service;

import com.shop.dto.NoticeDto;
import com.shop.entity.Notice;
import com.shop.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // 나 서비스다.
@Transactional // 트랜젝션설정 : 성공을하면 그대로 적용 실패하면 롤백
@RequiredArgsConstructor // final 또는 @NonNull 명령어가 붙으면 객체를 자동 붙혀줍니다.
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public void saveNotice(NoticeDto noticeDto) {
        Notice notice = noticeDto.createNotice();
        noticeRepository.save(notice);
    }

    @Transactional(readOnly = true)
    public Page<Notice> getNotice(Pageable pageable){
        return noticeRepository.getNotice(pageable);
    }
}
