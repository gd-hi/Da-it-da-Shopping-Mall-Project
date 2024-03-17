package com.shop.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table
@Getter
@Setter
@ToString
@DynamicInsert // 디폴트 값 설정시 필요
public class Item extends BaseEntity{
    @Id // Primary Key
    @Column(name = "item_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 50)
    private String itemNm;

    @Column(name = "price", nullable = false)
    private int price; // 가격

    @Column(nullable = false)
    private int stockNumber; // 재고 수량

    @Column(name = "view")
    @ColumnDefault("0") // 디폴트 설정
    private Long view; // 상품 조회수

    @Lob
    @Column(nullable = false)
    private String itemDetail; // 상품 상세 설명

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; // 상품 판매 상태  문자열 -> 숫자

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "member_item",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<Member> member;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ItemImg> itemImgs;


    public void updateItem(ItemFormDto itemFormDto){
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
    }

    public void removeStock(int stockNumber){

        int restStock = this.stockNumber - stockNumber;

        if (restStock == 0){
            itemSellStatus = ItemSellStatus.SOLD_OUT;
        }
        else if(restStock > 0){
            itemSellStatus = ItemSellStatus.SELL;
        }
        else{
            throw new OutOfStockException("" +
                    "상품의 재고가 부족합니다. (현재 재고 수량: " + this.stockNumber + ")");
        }

        this.stockNumber = restStock;
    }

    public void addStock(int stockNumber){
        this.stockNumber += stockNumber;

        itemSellStatus = ItemSellStatus.SELL;

    }
}
