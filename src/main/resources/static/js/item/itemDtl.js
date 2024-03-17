$(document).ready(function () {

    calculateTotalPrice();

    $("#count").change(function () {
            calculateTotalPrice();
    });
});

function calculateTotalPrice() {
    var count = $("#count").val();
    var price = $("#price").val();
    var totalPrice = price * count;
    var formattedPrice = totalPrice.toLocaleString();
    $("#totalPrice").html(formattedPrice + '원');
}

function addCart(){

    // Ajax 통신할 때, csrf 토큰 값을 조회해서 직접 보내야함
    var token = $("meta[name = '_csrf']").attr("content");
    var header = $("meta[name = '_csrf_header']").attr("content");

    var url = "/cart";

    var paramData = {
        itemId : $("#itemId").val(),
        count : $("#count").val()
    };

    var param = JSON.stringify(paramData);

    $.ajax({

        url : url,
        type : "POST",
        contentType : "application/json",
        data : param,
        beforeSend : function(xhr){
            // 데이터를 전송하기 전에 헤더의 csrf 값을 설정
            xhr.setRequestHeader(header, token);
        },
        dataType : "json",
        cache : false,
        success : function(result, status){
             Swal.fire({
                title: '상품을 담았습니다!',
                text: "장바구니로 이동하시겠습니까?",
                icon: 'success',
                showCancelButton: true,
                confirmButtonColor: '#3085d6',
                cancelButtonColor: '#d33',
                confirmButtonText: '바로 이동할래요.',
                cancelButtonText: '계속 쇼핑할래요.',
             })
             .then((result) => {
                 if (result.isConfirmed) {
                    location.href = '/cart'
                 }
             })
        },
        error : function(jqXHR, status, error){
            if(jqXHR.status == '401'){
                Swal.fire({
                    title: '로그인이 필요합니다.',
                    text: "로그인 후 이용해주세요.",
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#3085d6',
                    cancelButtonColor: '#d33',
                        confirmButtonText: '로그인 할게요.',
                        cancelButtonText: '그냥 볼게요.'
                    }).then((result) => {
                        if (result.isConfirmed) {
                                location.href = '/members/login'
                        }
                    })
            }else{
                console.log("jqXHR.responseText : ", jqXHR.responseText)
                Swal.fire({
                title: jqXHR.responseText,
                icon: 'warning',
                });
            }
        }
    });
}

function scrollToSection(sectionId) {
    document.getElementById(sectionId).scrollIntoView({ behavior: 'smooth' });
}
