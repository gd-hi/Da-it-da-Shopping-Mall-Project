// 메인컨트롤러에서 설정한 userEmail 속성을 사용
var loggedInUsername = "[[${nickname}]]";

var page = 0;
var loading = false;

// 뒤로가기 눌렀을 때 무한 스크롤 된 부분으로 로딩
window.addEventListener('pageshow', function (event) {
    if (event.persisted || window.performance && window.performance.navigation.type == 2) {
         if (sessionStorage.getItem("page")) {
            var pageNum = Number.parseInt(sessionStorage.getItem("page"));
            _page = pageNum ;
            list.search();
        }
    }
});

function resetSearch() {
    document.querySelector('.removeCategory').value = '';
}

$(document).ready(function () {
    var searchQuery = $('[name="searchQuery"]').val();
    console.log(searchQuery);

    $(window).on('scroll', onScroll);

    // 최근 본 상품
    // 스크롤 이벤트에 반응하여 사이드바를 뷰포트 중앙에 위치시킵니다.
    var scrollTimeout; // 스크롤 이벤트 타이머를 저장할 변수

    $(window).scroll(function() {
        var scrollTop = $(window).scrollTop(); // 현재 스크롤 위치를 가져옵니다.
        var desiredTop = scrollTop + 200; // 스크롤 위치에서 200px 아래에 요소를 배치합니다.

        $('.recent-items-container').css('top', desiredTop + 'px');
    });

});




function scrollToTop() {
    document.body.scrollTop = 0;
    document.documentElement.scrollTop = 0;
}

document.addEventListener('DOMContentLoaded', function () {
    // 페이지 로딩 시 최근 본 상품 및 카테고리 사이드바 숨김
    var recentItemsSidebar = document.querySelector('.recent-items-container');
    var categorySidebar = document.querySelector('.sidebar-category');
    recentItemsSidebar.style.display = 'none';
    categorySidebar.style.display = 'none';

    document.addEventListener("scroll", function() {
        var recentTriggerHeight = 400; // 최근 본 상품 사이드바가 나타날 스크롤 위치
        var categoryTriggerHeight = 900; // 카테고리 사이드바가 나타날 스크롤 위치

        // 최근 본 상품 사이드바
        if (window.scrollY > recentTriggerHeight) {
            recentItemsSidebar.style.display = 'block';
        } else {
            recentItemsSidebar.style.display = 'none';
        }

        // 카테고리 사이드바
        if (window.scrollY > categoryTriggerHeight) {
            categorySidebar.style.display = 'block';
        } else {
            categorySidebar.style.display = 'none';
        }
    });

});

// 캐러셀
$('.multi-item-carousel .carousel-item').each(function(){
  var next = $(this).next();
  if (!next.length) {
    next = $(this).siblings(':first');
  }
  next.children(':first-child').clone().appendTo($(this));
});

$('.multi-item-carousel .carousel-item').each(function(){
  var prev = $(this).prev();
  if (!prev.length) {
    prev = $(this).siblings(':last');
  }
  prev.children(':nth-last-child(2)').clone().prependTo($(this));
});


function loadMoreItems() {
    if (!loading) {
        loading = true;

        // 로딩 아이콘 표시
        // $('#item-container').append('<div class="loader"></div>');

        var currentSearchQuery = $('[name="searchQuery2"]').val();
        console.log(currentSearchQuery);

        $.ajax({
            type: "GET",
            url: "/load-more?searchQuery=" + encodeURIComponent(currentSearchQuery) + "&page=" + (++page),
            success: function (data) {
                // console.log(data);
                if (data.trim().length == 0) {
                    $(window).off('scroll', onScroll);
                } else {
                    // $('#item-container .loader').remove(); // 로딩 아이콘 제거
                    $('#item-container').append(data);
                }
            },
            complete: function () {
                loading = false; // 로딩 완료 후 loading 변수를 false로 설정
            }
        });
    }
}

function onScroll() {
    if ($(window).scrollTop() + $(window).height() >= $(document).height() - 100) {
        loadMoreItems();
    }
}

// 정렬
function sortItems(sortBy) {
    $.ajax({
        type: "GET",
        url: "/sortItems?sortBy=" + sortBy,
        success: function(data) {
            console.log(data);
            var itemsHtml = "";

            $.each(data, function(index, item) {
                // 기본 이미지 URL 설정
                var defaultImageUrl = "/img/none.jpg";
                // 대표 이미지 찾기
                var repImgUrl = item.itemImgs.find(img => img.repImgYn === 'Y')?.imgUrl || defaultImageUrl;

                var priceFormatted = item.price.toLocaleString() + '원';
                var itemNmTrimmed = item.itemNm.split('#')[0].substring(0, 30);
                var itemDetailTrimmed = item.itemDetail.substring(0, 30);

                itemsHtml += '<div class="col mb-5">';
                itemsHtml += '    <div class="a">';
                itemsHtml += '        <div class="card h-100 d-flex" style="border: 0; flex-direction: column;">';
                itemsHtml += '            <a href="/item/' + item.id + '" class="text-dark">';
                if(item.itemSellStatus == "SOLD_OUT") {
                    itemsHtml += '<div class="sold-out-overlay">품절</div>';
                }
                // 대표 이미지 URL 사용
                itemsHtml += '                <img src="' + repImgUrl + '" class="card-img-top flex-grow-1 border border-light-subtle" alt="' + item.itemNm + '" style="object-fit: cover; height: 250px; width: 100%;">';
                itemsHtml += '                <div class="card-body" style="padding-left: 0;">';
                itemsHtml += '                    <div class="text-left">';
                itemsHtml += '                        <h4 class="card-title fw-bolder">' + priceFormatted + '</h4>';
                itemsHtml += '                        <h6 class="fw-normal">' + itemNmTrimmed + '</h6>';
                itemsHtml += '                        <p class="fw-bolder" style="font-size:0.8rem">' + itemDetailTrimmed + '</p>';
                itemsHtml += '                    </div>';
                itemsHtml += '                </div>';
                itemsHtml += '            </a>';
                itemsHtml += '        </div>';
                itemsHtml += '    </div>';
                itemsHtml += '</div>';
            });

            // 교체
            $("#item-container").html(itemsHtml);
        }
    });
}