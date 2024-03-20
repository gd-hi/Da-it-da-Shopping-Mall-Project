package com.shop.service;


import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {

    @Value("${itemImgLocation}")
    private String itemImgLocation;
    private final ItemImgRepository itemImgRepository;
    private final FileService fileService;

    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception {
        try {
            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = "";
            String imgUrl = "";
            System.out.println(oriImgName);

            // 파일 업로드
            if (!StringUtils.isEmpty(oriImgName)) {
                System.out.println("******");
                imgName = fileService.uploadFile(itemImgLocation, oriImgName,
                        itemImgFile.getBytes());

                System.out.println(imgName);
                System.out.println("******");

                imgUrl = "/images/item/" + imgName;
                System.out.println("******");
            }

            System.out.println("1111");
            itemImg.updateItemImg(oriImgName, imgName, imgUrl);
            System.out.println("(((((");

            itemImgRepository.save(itemImg);
        } catch (Exception e) {
            // 예외를 콘솔에 출력
            e.printStackTrace();
            // 필요한 경우, 로그 파일에 예외를 기록
            // log.error("Item image saving failed", e);
            // 예외를 다시 throw 하여 상위로 전파할 수도 있음
            // throw e;
        }
    }


    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception {
        if (!itemImgFile.isEmpty()){
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId).
                    orElseThrow(EntityExistsException::new);

            if(!StringUtils.isEmpty(savedItemImg.getImgName())){
                fileService.deleteFile(itemImgLocation + "/" + savedItemImg.getImgName());
            }

            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(itemImgLocation, oriImgName,
                    itemImgFile.getBytes()); // 파일 업로드
            String imgUrl = "/images/item/" + imgName;

            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);

        }
    }

}
