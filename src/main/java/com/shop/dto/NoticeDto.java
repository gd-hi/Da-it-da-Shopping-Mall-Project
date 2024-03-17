package com.shop.dto;

import com.shop.entity.Notice;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class NoticeDto {
    private Long id;
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private static ModelMapper modelMapper = new ModelMapper();


    public Notice createNotice(){
        return modelMapper.map(this, Notice.class);
    }
}
