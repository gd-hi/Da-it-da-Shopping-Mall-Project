package com.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 등록자 및 수정자 제외한 시간관련 Auditing 기능 수행 Entity
@EntityListeners(value = {AuditingEntityListener.class})
@MappedSuperclass
@Getter
@Setter
public abstract class BaseTimeEntity {

    @CreatedDate // 생성 시 자동 저장
    @Column(updatable = false)
    private LocalDateTime regTime;

    @LastModifiedDate // 변경 시 자동 저장
    private LocalDateTime updateTime;
}
