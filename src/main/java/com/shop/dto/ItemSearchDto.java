package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSearchDto {

    private String searchDateType; // 조회 날짜 All / 1w / 1m / 6m

    private ItemSellStatus searchSellStatus; // 상품 판매 상태 (constant) SELL/SOLD OUT

    private String searchBy; // 조회 유형 createBy...

    private String searchQuery = ""; // 검색 단어

}
