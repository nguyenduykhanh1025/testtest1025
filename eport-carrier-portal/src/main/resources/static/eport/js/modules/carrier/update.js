const PREFIX = ctx + "carrier/do";

$(function () {
  $("#containerNumber").val(containerNumber);
  $("#expiredDem").val(formatDate(expiredDem));
  $("#detFreeTime").val(detFreeTime);
  $("#emptyContainerDepot").val(emptyContainerDepot);
})

function formatDate(value) {
  if(value == null || value == undefined)
  {
      return;
  }
  var date = new Date(value)
  var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
  var month = date.getMonth() + 1;
  var monthText = month < 10 ? "0" + month : month;
  return day + "/" + monthText + "/" + date.getFullYear();
}

function closeForm() {
  $.modal.close();
}

function confirm() {
  if (formatDate(expiredDem) == $("#expiredDem").val() && $("#detFreeTime").val() == detFreeTime && $("#emptyContainerDepot").val() == emptyContainerDepot) {
    $.modal.alertError("Không có thông tin nào được thay đổi !!!")
    return;
  }
  if (validateDateUpdate($("#expiredDem").val()) == 1 && formatDate(expiredDem) != $("#expiredDem").val()) {
    $.modal.alertError("Hạn lệnh chỉ có thể thay đổi về quá khứ nhiều nhất là 1 ngày !!!")
    return;
  }
  $.modal.confirm(
    "Bạn có chắc chắn muốn cập nhật DO không? Hành động này không thể hoàn tác",
    function () {
      $.ajax({
        url: PREFIX + "/update",
        method: "post",
        dataType: "json",
        data: {
          id: id,
          expiredDem: formatDateForSubmit($("#expiredDem").val()),
          detFreeTime: $("#detFreeTime").val() == detFreeTime ? "" : $("#detFreeTime").val(),
          emptyContainerDepot: $("#emptyContainerDepot").val() == emptyContainerDepot ? "" : $("#emptyContainerDepot").val()
        },
        success: function (data) {
          if (data.code == 0) {
            layer.msg('Cập nhật thành công! ', {icon: 1});
          } else {
            $.modal.alertError(data.msg);
            $.modal.closeLoading();
            return;
          }
          setTimeout(function () {
            parent.getSelectedRow();
            $.modal.close();
          }, 1000)
        },
        error: function (data) {
          $.modal.alertError("Có lỗi trong quá trình thêm dữ liệu, vui lòng thử lại sau.")
        },
      })
    }, {
      title: "Xác nhận cập nhật DO",
      btn: ["Đồng Ý", "Hủy Bỏ"]
    }
  )
}

function formatDateForSubmit(value) {
  let checkDate = validateUpdateDate(formatDate(expiredDem), $("#expiredDem").val());
  if (checkDate == 1) {
    return;
  }
  if (value == null || value == undefined) {
    return;
  }
  var newdate = value.split("/").reverse();
  var date = new Date(newdate)
  var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
  var month = date.getMonth() + 1;
  var monthText = month < 10 ? "0" + month : month;
  var s = date.getFullYear() + "-" + monthText + "-" + day;
  return date.getFullYear() + "-" + monthText + "-" + day;
}

function validateUpdateDate(fromDate, toDate) {
  if (fromDate == toDate) {
    return 1;
  }
  return 0;
}

$.ajax({
  type: "GET",
  url: PREFIX + "/getEmptyContainerDeport",
  success(data) {
    data.data.forEach(element => {
      if(element['dictLabel'] != emptyContainerDepot)
      {
        $('.select-emptyContainerDeport').append(`<option value="${element['dictLabel']}"> 
                                                  ${element['dictLabel']} 
                                                </option>`);
      }
    });
  }
})

function validateDateUpdate(toDate) {
  if(toDate == null || toDate == undefined)
  {
      return;
  }
  toDate = toDate.split("/").reverse().join("-");
  if (toDate == "") {
    return 1;
  }
  var currentDay = new Date();
  var toDate1 = new Date(toDate);
  var offset = toDate1.getTime() - currentDay.getTime();
  var totalDays = Math.round(offset / 1000 / 60 / 60 / 24);
  if (totalDays < -1) {
    return 1;
  }
  return 0;
}
