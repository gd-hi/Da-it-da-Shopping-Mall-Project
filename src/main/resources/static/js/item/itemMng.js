$(document).ready(function () {
    $("serachBtn").on("click", function (e) {
        e.preverntDefault();
        page(0);
    });
});
function page(page) {
    var searchDateType = $("#searchDateType").val();
    var searchSellStatus = $("#searchSellStatus").val();
    var searchBy = $("#searchBy").val();
    var searchQuery = $("#searchQuery").val();
    location.href = "/admin/items/" + page + "?searchDateType=" + searchDateType
        + "&searchSellStatus=" + searchSellStatus + "&searchBy=" + searchBy
        + "&searchQuery=" + searchQuery;
}