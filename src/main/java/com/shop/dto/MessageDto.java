package com.shop.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MessageDto {
    private String message;
    private String nickname;
    private Date date;

    MessageDto(){
        date = new Date();
    }
}