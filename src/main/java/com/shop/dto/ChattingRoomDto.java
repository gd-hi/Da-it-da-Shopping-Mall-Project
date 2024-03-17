package com.shop.dto;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedList;
import java.util.Objects;

@Data
@Builder
public class ChattingRoomDto {
    private String roomNumber; // 방 번호
    private String roomName; // 방 이름
    private LinkedList<String> users; // 유저목록


    // 채팅방 목록에서 방 번호로 방을 찾을 수 있게 equals와 hashCode를 오버라이딩
    // 생성자를 사용하지 않고 Builder패턴을 사용

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChattingRoomDto other = (ChattingRoomDto) obj;
        return Objects.equals(roomNumber, other.roomNumber);
    }
    @Override
    public int hashCode() {
        return Objects.hash(roomNumber);
    }
}