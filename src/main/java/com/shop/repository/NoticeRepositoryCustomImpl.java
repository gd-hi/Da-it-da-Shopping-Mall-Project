package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.entity.Notice;
import com.shop.entity.QNotice;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class NoticeRepositoryCustomImpl implements NoticeRepositoryCustom{

    private JPAQueryFactory queryFactory; // 동적쿼리 사용하기 위해 JPAQueryFactory 변수 선언
    // 생성자
    public NoticeRepositoryCustomImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em); // JPAQueryFactory 실질적인 객체가 만들어 집니다.
    }
    @Override
    public Page<Notice> getNotice(Pageable pageable){
        QueryResults<Notice> results = queryFactory.selectFrom(QNotice.notice)
                .orderBy(QNotice.notice.id.desc())
                .offset(pageable.getOffset()).limit(pageable.getPageSize()).fetchResults();
        List<Notice> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content,pageable,total);

    }
}
