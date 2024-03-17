function cancelOrder(orderId) {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/order/" + orderId + "/cancel";
    var paramData = {
        orderId : orderId,
    };

    var param = JSON.stringify(paramData);

    $.ajax({
        url      : url,
        type     : "POST",
        contentType : "application/json",
        data     : param,
        beforeSend : function(xhr){
            /* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
            xhr.setRequestHeader(header, token);
        },
        dataType : "json",
        cache   : false,
        success: function(result, status){
            Swal.fire({
                title: "주문이 취소 되었습니다.",
                icon: 'success',
            }).then((result) => {
                if (result.isConfirmed) {
                    location.reload();
                }
            });
        },
        error : function(jqXHR, status, error){
            if (jqXHR.status == '401') {
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
                        location.href = '/members/login';
                    }
                })

            }
            else {
                Swal.fire({
                    title: jqXHR.responseText,
                    icon: 'warning',
                });
            }
        }
    });
}

function reOrder(orderId) {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/order/" + orderId + "/reOrder";
    var paramData = {
        orderId : orderId,
    };

    var param = JSON.stringify(paramData);

    $.ajax({
        url      : url,
        type     : "POST",
        contentType : "application/json",
        data     : param,
        beforeSend : function(xhr){
            /* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
            xhr.setRequestHeader(header, token);
        },
        dataType : "json",
        cache   : false,
        success : function(result, status){
             Swal.fire({
                title: '상품을 다시 담았습니다',
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
                 else{
                    location.href = '/'
                 }
             })
        },
        error : function(jqXHR, status, error){
            if (jqXHR.status == '401') {
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
                        location.href = '/members/login';
                    }
                })

            }
            else {
                Swal.fire({
                    title: jqXHR.responseText,
                    icon: 'warning',
                });
            }
        }
    });
}