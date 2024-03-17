// START CLOCK SCRIPT

Number.prototype.pad = function(n) {
  for (var r = this.toString(); r.length < n; r = 0 + r);
  return r;
};

function updateClock() {
  var now = new Date();
  var sec = now.getSeconds(),
    min = now.getMinutes(),
    hou = now.getHours();
//    mo = now.getMonth(),
//    dy = now.getDate(),
//    yr = now.getFullYear();
//  var months = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];
//  var tags = ["mon", "d", "y", "h", "m", "s"],
//  corr = [months[mo], dy, yr, hou.pad(2), min.pad(2), sec.pad(2)];
  var tags = ["h", "m", "s"],
  corr = [hou.pad(2), min.pad(2), sec.pad(2)];
  for (var i = 0; i < tags.length; i++)
    document.getElementById(tags[i]).firstChild.nodeValue = corr[i];
}

function initClock() {
  updateClock();
  window.setInterval("updateClock()", 1000);
}

// END CLOCK SCRIPT
