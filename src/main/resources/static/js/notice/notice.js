function collapse(element) {
    var before = document.getElementsByClassName("ac")[0];
    if (before && document.getElementsByClassName("ac")[0] != element) {
        before.nextElementSibling.style.maxHeight = null;
        before.classList.remove("ac");
    }
    element.classList.toggle("ac");

    var notice_content = element.nextElementSibling;
    if (notice_content.style.maxHeight != 0) {
        notice_content.style.maxHeight = null;
    } else {
        notice_content.style.maxHeight = notice_content.scrollHeight + "px";
    }
}