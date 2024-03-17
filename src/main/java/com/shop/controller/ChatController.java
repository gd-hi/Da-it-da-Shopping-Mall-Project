package com.shop.controller;

import com.shop.dto.ChattingRoomDto;
import com.shop.dto.ChattingRoomExitDto;
import com.shop.dto.MessageDto;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import com.shop.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Tag(name = "Chat-Controller", description = "채팅 관련 API")
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MemberRepository memberRepository;

    // 채팅방 목록
    public static LinkedList<ChattingRoomDto> chattingRoomList = new LinkedList<>();


    //	----------------------------------------------------
    // 유틸 메서드

    // 방 번호로 방 찾기
    public ChattingRoomDto findRoom(String roomNumber) {
        ChattingRoomDto room = ChattingRoomDto.builder().roomNumber(roomNumber).build();
        int index = chattingRoomList.indexOf(room);

        if(chattingRoomList.contains(room)) {
            return chattingRoomList.get(index);
        }
        return null;
    }


    // 쿠키에 추가
    public void addCookie(String cookieName, String cookieValue) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletResponse response = attr.getResponse();

        Cookie cookie = new Cookie(cookieName, cookieValue);

        int maxage = 60 * 60 * 24 * 7;
        cookie.setMaxAge(maxage);
        response.addCookie(cookie);
    }



    // 방 번호, 닉네임 쿠키 삭제
    public void deleteCookie( ) {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        HttpServletResponse response = attr.getResponse();

        Cookie roomCookie = new Cookie("roomNumber", null);
        Cookie nicknameCookie = new Cookie("nickname",null);

        roomCookie.setMaxAge(0);
        nicknameCookie.setMaxAge(0);

        response.addCookie(nicknameCookie);
        response.addCookie(roomCookie);
    }



    // 쿠키에서 방번호, 닉네임 찾기
    public Map<String, String> findCookie() {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();

        Cookie[] cookies = request.getCookies();
        String roomNumber = "";
        String nickname= "";

        if(cookies == null) {
            return null;
        }

        if(cookies != null) {
            for(int i=0;i<cookies.length;i++) {
                if("roomNumber".equals(cookies[i].getName())) {
                    roomNumber = cookies[i].getValue();
                }
                if("nickname".equals(cookies[i].getName())) {
                    nickname = cookies[i].getValue();
                }
            }

            if(!"".equals(roomNumber) && !"".equals(nickname)) {
                Map<String, String> map = new HashMap<>();
                map.put("nickname", nickname);
                map.put("roomNumber", roomNumber);

                return map;
            }
        }

        return null;
    }

    // 닉네임 생성
    public void createNickname(String nickname) {
        addCookie("nickname", nickname);
    }

    // 방 입장하기
    public boolean enterChattingRoom(ChattingRoomDto chattingRoom) {
        // 현재 인증된 사용자의 이메일(아이디)를 가져옴
        String userEmail = getCurrentUserEmail();

        // 사용자의 이메일을 기반으로 사용자 정보 조회
        Member member = memberRepository.findByEmail(userEmail);

        if (member == null) {
            // 회원 정보를 찾을 수 없는 경우
            return false;
        }

        String nickname = member.getName(); // 사용자의 이름을 닉네임으로 사용

        // 채팅방에 사용자를 추가하는 로직 (여기서는 예시로 닉네임만 출력)
        System.out.println("닉네임으로 사용될 이름: " + nickname);

        // 채팅방을 찾아서 사용자 리스트에 추가
        ChattingRoomDto room = findRoom(chattingRoom.getRoomNumber());
        if (room != null) {
            room.getUsers().add(nickname);
            return true;
        }

        return false;
    }

    private String getCurrentUserEmail() {
        // SecurityContext에서 인증 정보를 가져옴
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            // 인증된 사용자의 이메일(아이디) 반환
            return authentication.getName();
        }

        // 인증 정보가 없거나 인증되지 않은 사용자는 "GUEST" 반환
        return "GUEST";
    }




    //	----------------------------------------------------

    // 컨트롤러

    // 메인화면
    @GetMapping("/chat")
    @Operation(summary = "메인 채팅 페이지", description = "사용자가 채팅방을 선택할 수 있는 메인 채팅 페이지를 표시합니다.")
    public String chat() {
        return "chat";
    }


    // 채팅방 목록
    @GetMapping("/chattingRoomList")
    @Operation(summary = "채팅방 목록", description = "활성 채팅방의 목록을 검색합니다.")
    public ResponseEntity<?> chattingRoomList() {
        ResponseEntity<LinkedList<ChattingRoomDto>> linkedListResponseEntity = new ResponseEntity<>(chattingRoomList, HttpStatus.OK);
        return linkedListResponseEntity;
    }


    // 방 만들기
    @PostMapping("/chattingRoom")
    @Operation(summary = "채팅방 만들기", description = "새 채팅방을 만들고 사용 가능한 방 목록에 추가합니다.")
    public ResponseEntity<?> chattingRoom(String roomName, String nickname) {

        // 방을 만들고 채팅방목록에 추가
        String roomNumber = UUID.randomUUID().toString();
        ChattingRoomDto chattingRoom = ChattingRoomDto.builder()
                .roomNumber(roomNumber)
                .users(new LinkedList<>())
                .roomName(roomName)
                .build();

        chattingRoomList.add(chattingRoom);

        // 방 입장하기
        enterChattingRoom(chattingRoom);

        return new ResponseEntity<>(chattingRoom, HttpStatus.OK);
    }


    // 방 들어가기
    @GetMapping("/chattingRoom-enter")
    @Operation(summary = "채팅방 들어가기", description = "사용자가 방 번호를 사용하여 채팅방에 입장할 수 있습니다.")
    public ResponseEntity<?> EnterChattingRoom(String roomNumber, String nickname){

        // 방 번호로 방 찾기
        ChattingRoomDto chattingRoom = findRoom(roomNumber);

        if(chattingRoom == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            // 방 들어가기
            enterChattingRoom(chattingRoom);

            return new ResponseEntity<>(chattingRoom, HttpStatus.OK);
        }
    }

    // 방 나가기
    @PatchMapping("/chattingRoom-exit")
    @Operation(summary = "채팅방 나가기", description = "사용자가 채팅방을 나가고 방의 사용자 목록을 그에 따라 업데이트합니다.")
    public ResponseEntity<?> ExitChattingRoom(@RequestBody ChattingRoomExitDto exitDto) {
        String roomNumber = exitDto.getRoomNumber();
        String nickname = exitDto.getNickname();

        ChattingRoomDto chattingRoom = findRoom(roomNumber);
        if (chattingRoom == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        chattingRoom.getUsers().remove(nickname);

        // 0명이면 채팅방 펑
        if (chattingRoom.getUsers().isEmpty()) {
            chattingRoomList.remove(chattingRoom);
        }

        return new ResponseEntity<>(chattingRoom, HttpStatus.OK);
    }


    // 참가 중이었던 대화방
    @GetMapping("/chattingRoom")
    @Operation(summary = "현재 채팅방", description = "저장된 쿠키를 기반으로 사용자가 현재 참여 중인 채팅방을 검색합니다.")
    public ResponseEntity<?> chattingRoom() {
        // 쿠키에 닉네임과 방번호가 있다면 대화중이던 방이 있던것
        Map<String, String> map = findCookie();

        if(map == null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        String roomNumber = map.get("roomNumber");
        String nickname = map.get("nickname");

        ChattingRoomDto chattingRoom = findRoom(roomNumber);

        if(chattingRoom == null) {
            deleteCookie();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Map<String, Object> map2 = new HashMap<>();
            map2.put("chattingRoom", chattingRoom);
            map2.put("myNickname", nickname);

            return new ResponseEntity<>(map2, HttpStatus.OK);
        }
    }



    //	----------------------------------------------------
    // 메세지 컨트롤러

    // 여기서 메세지가 오면 방목록 업데이트
    @MessageMapping("/socket/roomList")
    @SendTo("/topic/roomList")
    @Operation(summary = "방 목록 업데이트", description = "사용 가능한 채팅방 목록을 업데이트합니다.")
    public String roomList() {
        return "";
    }

    // 채팅방에서 메세지 보내기
    @MessageMapping("/socket/sendMessage/{roomNumber}")
    @SendTo("/topic/message/{roomNumber}")
    @Operation(summary = "메시지 보내기", description = "특정 채팅방으로 메시지를 보냅니다.")
    public MessageDto sendMessage(@DestinationVariable String roomNumber, MessageDto message) {
        return message;
    }

    // 채팅방에 입장 퇴장 메세지 보내기
    @MessageMapping("/socket/notification/{roomNumber}")
    @SendTo("/topic/notification/{roomNumber}")
    @Operation(summary = "방 알림", description = "사용자 참가 또는 퇴장과 같은 특정 채팅방에 알림을 보냅니다.")
    public Map<String, Object> notification(@DestinationVariable String roomNumber, Map<String, Object> chattingRoom) {
        return chattingRoom;
    }


}