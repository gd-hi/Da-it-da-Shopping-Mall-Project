document.addEventListener("DOMContentLoaded", function() {
    var productNames = document.getElementsByClassName("removeCategory");

    for (var i = 0; i < productNames.length; i++) {
        // # 이후의 내용을 지우기
        productNames[i].textContent = productNames[i].textContent.split('#')[0];
    }
});

function bindDomEvent(){
    $(".imageFile.form-control").on("change", function(){
        var fileName = $(this).val().split("\\").pop();
        var fileExt = fileName.substring(fileName.lastIndexOf(".")+1);
        fileExt = fileExt.toLowerCase();

        if(fileExt != "jpg" && fileExt != "jpeg" && fileExt != "gif"
        && fileExt != "png" && fileExt != "bmp"){
            Swal.fire({
                 title: "이미지 파일만 등록이 가능합니다.",
                 icon: 'warning',
            });
            $(this).val("");
            return;
        }
    });
}