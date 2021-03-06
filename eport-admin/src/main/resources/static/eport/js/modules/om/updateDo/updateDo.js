$(function () {
  loadTable(blNo = null);
});
var prefix = "/updateDO";
$(".c-search-box").select2({
  placeholder: "Nhập BL No",
  allowClear: true,
  minimumInputLength: 2,
  ajax: {
    url: prefix + "/getOptionSearchDo",
    dataType: "json",
    method: "GET",
    data: function (params) {
      return {
        keyString: params.term,
      };
    },
    processResults: function (data) {
      let results = [];
      data.data.forEach(function (element, i) {
        let obj = {};
        obj.id = i;
        obj.text = element;
        results.push(obj);
      })
      return {
        results: results,
      };
    },
  },
});

//For submit search
$(".c-search-box").change(function () {
  loadTable($(this).text().trim());
  $(this).text(null);
  $('#btnReceiverDo').removeAttr("disabled");
});

function loadTable(blNo) {
  $("#dg").datagrid({
    url: prefix + "/getShipmentDetail",
    method: "GET",
    singleSelect: true,
    height: document.documentElement.clientHeight - 70,
    clientPaging: false,
    pagination: true,
    rownumbers: true,
    pageSize: 50,
    nowrap: false,
    striped: true,
    loadMsg: " Đang xử lý...",
    loader: function (param, success, error) {
      var opts = $(this).datagrid("options");
      if (!opts.url) return false;
      $.ajax({
        type: opts.method,
        url: opts.url,
        data: {
          pageNum: param.page,
          pageSize: param.rows,
          orderByColumn: param.sort,
          isAsc: param.order,
          blNo: blNo
        },
        dataType: "json",
        success: function (data) {
          success(data);
        },
        error: function () {
          error.apply(this, arguments);
        },
      });
    },
  });
}

function receiverDo() {
  let blNo = $(".c-search-box").text().trim();
  $.modal.confirm("Xác nhận đã nhận DO của Billing " + blNo, function () {
    $.ajax({
      url: prefix + "/updateStatusDo",
      method: "GET",
      data: {
        blNo: blNo,
      },
      success: function (data) {
        $.modal.msgReload("Lưu thành công! Đang tải lại dữ liệu...", 5000, modal_status.SUCCESS);
        $.modal.closeLoading();
      },
      error: function (data) {
        $.modal.alertError(data);
      },
    })
  })
}