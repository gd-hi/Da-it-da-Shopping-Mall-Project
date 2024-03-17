$(document).ready(function(){
    // 서버로부터 전달받은 로그인된 사용자의 이름 loggedInUsername
    // 나머지 스크립트 로직은 동일하며, 닉네임 입력 요구 관련 로직을 제거합니다.

    // 방 목록 그리기
    const listHtml = function(roomList) {
        let listHtml = "";
        for(let i=roomList.length-1; i>=0; i--) {
            listHtml += `
                <li data-room_number="${roomList[i].roomNumber}">
                    <span class="chat_title">${roomList[i].roomName}</span>
                    <span class="chat_count">${roomList[i].users.length}명</span>
                </li>`;
        }
        $("main ul").html(listHtml);
    };

    // 채팅방 목록 불러오기
    const chattingRoomList = function() {
        $.ajax({
            url: "/chattingRoomList",
            type: "GET",
        })
        .then(function(result) {
            listHtml(result);
        })
        .fail(function() {
            alert("채팅방 목록을 불러오는 중 오류가 발생했습니다.");
        });
    };



    const socket = new SockJS('/websocket');
    const stomp = Stomp.over(socket);
    stomp.debug = null; // stomp 콘솔출력 X


    // 구독을 취소하기위해 구독 시 아이디 저장
    const subscribe = [];

    // 모든 구독 취소하기
    const subscribeCancle = function() {
        const length = subscribe.length;
        for(let i=0;i<length;i++) {
            const sid = subscribe.pop();
            stomp.unsubscribe(sid.id);
        }
    }


    // 메인 화면
    const main = function() {
        $("main").show();

        // 기존 구독 취소
        subscribeCancle();

        // 채팅 중이었던 방이 있을때
        const room = chattingRoom();

        if(room) {
            return;
        }

        const subscribeId = stomp.subscribe("/topic/roomList", function(){
            // "/topic/roomList"에서 메세지가 왔을때 실행할 함수
            chattingRoomList();
        });

        subscribe.push(subscribeId);
        chattingRoomList();
    };



    stomp.connect({}, function(){
        main();
    });


    // ----------------- 메인화면 ---------------------------



    // ----------------- 채팅방 ---------------------------


    const info = (function(){
        let nickname = "";
        let roomNumber = "";

        const getNickname = function() {
            return nickname;
        }

        const setNickname = function(set){
            nickname = set;
        }

        const getRoomNumber = function() {
            return roomNumber;
        }

        const setRoomNumber = function(set) {
            roomNumber = set;
        }
        return {
            getNickname : getNickname,
            setNickname : setNickname,
            getRoomNumber : getRoomNumber,
            setRoomNumber : setRoomNumber,
        }
    })();


    const errorMSG = function(result){
        if(result.status == 404) {
            alert("종료되었거나 없는 방입니다");
        } else {
            alert("const errorMSG = function(result) 에러가 발생했습니다");
        }
        location.href = "/";
    }


    // 참가 인원 그리기
    const userList = function(users){
        $(".chat .chat_users .user").text(users.length + "명");

        let userHtml = "";
        for(let i=0;i<users.length;i++) {
            userHtml += `
                <li>${users[i] }</li>`;
        }

        $(".chat .chat_nickname ul").html(userHtml);
    }


    // 메세지 그리기
    const chatting = function(messageInfo){
        let nickname = messageInfo.nickname;
        let message = messageInfo.message;

        message = message.replaceAll("\n", "<br>").replaceAll(" ", "&nbsp");

        const date = messageInfo.date;
        const d = new Date(date);

        const time = String(d.getHours()).padStart(2, "0")
                    + ":"
                    + String(d.getMinutes()).padStart(2, "0");

        let sender = "";

        if(info.getNickname() == nickname) {
            sender = "chat_me";
            nickname = "";
        } else {
            sender=  "chat_other";
        }


        const chatHtml = `
            <li>
                <div class=${sender }>
                    <div>
                        <div class="nickname">${nickname }</div>
                        <div class="message">
                            <span class=chat_in_time>${time }</span>
                            <span class="chat_content">${message }</span>
                        <span>
                    </div>
                </div>
            </li>`;

        $(".chat ul.chat_list").append(chatHtml);

        $(".chat ul").scrollTop($(".chat ul")[0].scrollHeight);
    }


    // 채팅방 구독
    const chattingConnect = function(roomNumber){
        // 기존 구독 취소
        subscribeCancle();

        // 메세지를 받을 경로
        const id1 = stomp.subscribe("/topic/message/" + roomNumber, function(result){
            const message = JSON.parse(result.body);

            // 메세지가 왔을때 실행할 함수
            chatting(message);
        })

        // 입장,퇴장 알림을 받을 경로
        const id2 = stomp.subscribe("/topic/notification/" + roomNumber, function(result){
            const room = JSON.parse(result.body);
            const message = room.message;

            // 메세지가 왔을때 실행할 함수
            userList(room.users);

            const chatHtml = `
                <li>
                    <div class="notification">
                        <span>${message}</span>
                    </div>
                </li>`;

            $(".chat ul.chat_list").append(chatHtml);

            const chatList = $(".chat ul");
            if (chatList.length > 0) {
                chatList.scrollTop(chatList[0].scrollHeight);
            }

        })

        subscribe.push(id1);
        subscribe.push(id2);
    }



    // 채팅방 세팅
    const initRoom = function(room, nickname) {
        // 방 목록 업데이트
        stomp.send("/socket/roomList");

        $("main").hide();

        info.setNickname(nickname);
        info.setRoomNumber(room.roomNumber);

        $(".chat").show();
        $(".chat .chat_title").text(room.roomName);

        userList(room.users);
        chattingConnect(room.roomNumber);

        $(".chat_input_area textarea").focus();
    }


    // 메세지 보내기
    const sendMessage = function(){
        const message = $(".chat_input_area textarea");

        if (message.val() == "") {
            return;
        }

        const roomNumber = info.getRoomNumber();
        const nickname = info.getNickname();

        const data = {
            message : message.val(),
            nickname : nickname,
        }

        stomp.send("/socket/sendMessage/" + roomNumber, {}, JSON.stringify(data));
        message.val("");
    }




    $(".chat_button_area button").click(function() {
        sendMessage();
        $(".chat_input_area textarea").focus();
    })


    $(".chat_input_area textarea").keypress(function(event) {
        if (event.keyCode == 13) {
            if (!event.shiftKey) {
                event.preventDefault();

                sendMessage();
            }
        }
    })


    // 닉네임 만들고 채팅방 들어가기
    const enterChattingRoom = function(roomNumber) {

        const nickname = loggedInUsername; // 로그인된 사용자의 이름을 닉네임으로 사용

        // 닉네임과 방 번호를 사용하여 서버에 요청
        const data = { roomNumber: roomNumber, nickname: nickname };
        $.ajax({
            url: "/chattingRoom-enter",
            type: "GET",
            data: data,
        })
        .then(function(room) {
            initRoom(room, nickname); // 방 초기화 및 입장 처리
            // 채팅방 참가 메세지
            room.message = nickname + "님이 참가하셨습니다";
            stomp.send(
                "/socket/notification/" + roomNumber, {},
                JSON.stringify(room));
        })
        .fail(function(result) {
            errorMSG(result);
        });

    };

    // 새 채팅방 만들기
    const createRoom = function(roomName) {
        const data = {
            roomName: roomName,
            nickname: loggedInUsername // 로그인된 사용자의 이름 사용
        };

        $.ajax({
            url: "/chattingRoom",
            type: "POST",
            data: data,
        })
        .then(function(room){
            initRoom(room, loggedInUsername); // 로그인된 사용자의 이름으로 초기화
        })
        .fail(function(){
            alert("const createRoom = function(roomName) 에러가 발생했습니다");
        });
    };

    $(".new_chat").click(function(){
        swal({
            text: "생성할 방 이름을 입력해주세요",
            content: "input",
            buttons: {
                cancel: "취소",
                confirm: {
                    text: "만들기",
                    value: true,
                    className: "custom-color",
                    closeModal: true
                }
            },
            closeOnClickOutside: false
        })
        .then(function(roomName){
            if(roomName) {
                createRoom(roomName);
            }
        })
    })

    $(document).on("dblclick", "main li", function(){
        const roomNumber = $(this).data("room_number");
        enterChattingRoom(roomNumber);
    })


    // 채팅방 나가기
    $(".chat_back").click(function() {
        swal({
            text: "대화방에서 나갑니다",
            buttons: {
                cancel: "아니요 계속 있을래요",
                confirm: {
                    text: "네 나갈래요",
                    value: true,
                    className: "custom-color",
                    closeModal: true
                }
            }
        })
        .then(function(result){
            if(result) {

                $.ajax({
                    url: "/chattingRoom-exit",
                    type: "PATCH",
                    contentType: "application/json",
                    data: JSON.stringify({ roomNumber: info.getRoomNumber(), nickname: info.getNickname() }),
                    success: function(room) {
                        const roomNumber = info.getRoomNumber();

                        if(room.users.length != 0) {
                            // 채팅방 나가기 메세지
                            room.message = info.getNickname() + "님이 퇴장하셨습니다";
                            stomp.send(
                                "/socket/notification/" + roomNumber, {},
                                JSON.stringify(room));
                        }

                        // 채팅방 목록 업데이트
                        stomp.send("/socket/roomList");

                        main();
                        $(".chat").hide();
                        $(".chat ul.chat_list").html("");

                        info.setRoomNumber("");
                        info.setNickname("");

                    },
                    error: function(xhr, status, error) {
                        errorMSG();
                    }
                });
            }
        })
    })



    // 대화 중이던 방
    const chattingRoom = function (){
        let returnRoom = null;

        $.ajax({
            url: "/chattingRoom",
            type: "GET",
            async: false,
        })
        .then(function(result){
            if(result != "") {
                const room = result.chattingRoom;
                const nickname = result.myNickname;
                initRoom(room, nickname);
                returnRoom = result;
            }
        })
        .fail(function(result){
            errorMSG(result);
        })

        return returnRoom;
    };



}) // document.ready

// 버튼 색상을 업데이트하는 함수
function updateButtonColor() {
    var textArea = document.querySelector('.chat_input_area textarea');
    var sendButton = document.querySelector('.chat_button_area button');

    // 입력 내용에 따라 버튼의 배경색을 변경
    sendButton.style.background = textArea.value.trim() ? '#FEE500' : '#eee';
}

document.addEventListener('DOMContentLoaded', function () {

    document.getElementById('chat-icon').addEventListener('click', function() {
        event.stopPropagation(); // 이벤트 버블링 방지

        var chatBox = document.getElementById('chat-box');
        if (chatBox.style.display === 'none' || chatBox.style.display === '') {
            chatBox.style.display = 'block';
            console.log("111");
        } else {
            chatBox.style.display = 'none';
            console.log("222");
        }
    });

    // 전송버튼 색 바꾸기
    // 초기 상태 설정
    updateButtonColor();

    // textarea에 이벤트 리스너를 추가하여 내용 변경 시 색상 업데이트
    document.querySelector('.chat_input_area textarea').addEventListener('keyup', updateButtonColor);


});