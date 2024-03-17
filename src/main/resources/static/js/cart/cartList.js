$(document).ready(function () {
    $("input[name=cartChkBox]").change(function () {
        getOrderTotalPrice();
    });

    // 페이지 로드 시 각 상품의 금액 업데이트
    updateIndividualTotalPrices();

    $('.summary-delivery-selection').change(function() {
        applyCoupon();
    });

});

// 쿠폰적용하기
function applyCoupon() {
    var totalAmount = parseInt($('#orderTotalPrice').text().replace(/[^0-9]/g, ''), 10); // 기존 총합 금액
    var discountRate = 0;

    switch ($('.summary-delivery-selection').val()) {
        case 'collection':
            discountRate = 0.05; // 5% 할인
            break;
        case 'first-class':
            discountRate = 0.10; // 10% 할인
            break;
        case 'second-class':
            discountRate = 0.30; // 30% 할인
            break;
        case 'signed-for':
            discountRate = 0.50; // 50% 할인
            break;
        default:
            discountRate = 0; // 할인 없음
    }

    var discountedAmount = totalAmount - (totalAmount * discountRate);
    $('#realOrderTotalPrice').text(discountedAmount.toLocaleString() + '원'); // 할인 적용 금액 업데이트
}

// 각 상품의 총 금액을 업데이트하는 함수
function updateIndividualTotalPrices() {
    $("input[name='count']").each(function () {
        changeCount(this); // 이미 있는 changeCount 함수를 재활용
    });
}

function getOrderTotalPrice() {
    var orderTotalPrice = 0;
    $("input[name=cartChkBox]:checked").each(function () {
        var cartItemId = $(this).val();
        var price = $("#price_" + cartItemId).attr("data-price");
        var count = $("#count_" + cartItemId).val();
        orderTotalPrice += price * count;
    });

    var formattedPrice = orderTotalPrice.toLocaleString();
    $("#orderTotalPrice").html(formattedPrice + '원');
}

// 상품 수량 변경
function changeCount(obj) {
    var count = obj.value; // 현재 수량
    var cartItemId = obj.id.split('_')[1]; // count_ + id 이므로 id를 추출
    var price = $("#price_" + cartItemId).data("price");
    var totalPrice = count * price;
    var formattedPrice = totalPrice.toLocaleString();
    $("#totalPrice_" + cartItemId).html(formattedPrice + "원");
    getOrderTotalPrice();

    updateCartItemCount(cartItemId, count);
}

// 전체 선택 또는 전체 해제
function checkAll() {
    if ($("#checkall").prop("checked")) {
        $("input[name=cartChkBox]").prop("checked", true);
    }
    else {
        $("input[name=cartChkBox]").prop("checked", false);
    }
    getOrderTotalPrice();
}

function updateCartItemCount(cartItemId, count) {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    // 상품 Id 값은 PathVariable, 수량(count)은 QueryString
    var url = "/cartItem/" + cartItemId + "?count=" + count;

    $.ajax({
        url: url,
        type: "PATCH",
        beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
        },
        dataType: "json",
        cache: false,
        success: function (result, status) {
            console.log("cartItem count update success");
        },
        error: function (jqXHR, status, error) {
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
                        location.href = '/members/login'
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

// 장바구니 삭제
function deleteCartItem(obj) {
    var cartItemId = obj.dataset.id;
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/cartItem/" + cartItemId;

    $.ajax({
        url: url,
        type: "DELETE",
        beforeSend: function (xhr) {
            /* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
            xhr.setRequestHeader(header, token);
        },
        dataType: "json",
        cache: false,
        success: function (result, status) {
            location.href = '/cart';
        },
        error: function (jqXHR, status, error) {

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
                        location.href = '/members/login'
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

function collectCartItems() {
    var cartItems = [];
    $("input[name='cartChkBox']:checked").each(function () {
        cartItems.push({cartItemId: $(this).val()});
    });
    return cartItems;
}