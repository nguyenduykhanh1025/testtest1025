const PREFIX = ctx + "support-request/ice";
const HIST_PREFIX = ctx + "om/controlling";
const SEARCH_HEIGHT = $(".main-body__search-wrapper").height();
var dogrid = document.getElementById("container-grid"),
  hot;
var shipmentSelected,
  checkList,
  allChecked,
  sourceData,
  rowAmount = 0,
  shipmentDetailIds;
var shipment = new Object();
shipment.params = new Object();

const SPECIAL_STATUS = {
  yet: "1",
  pending: "2",
  approve: "3",
  reject: "4",
};

const DANGEROUS_STATUS = {
  yet: "T", // là cont dangerous
  pending: "2", // là cont danger đang chờ xét duyết
  approve: "3", // là cont danger đã đc xét duyết
  reject: "4", // là cont danger đã bị từ chối
  NOT: "F", // không phải là cont danger
};

var detailInformationForContainerSpecial = {
  data: [],
  indexSelected: -1,
};

$(document).ready(function () {
  $(".main-body").layout();

  $(".collapse").click(function () {
    $(".main-body__search-wrapper").height(15);
    $(".main-body__search-wrapper--container").hide();
    $(this).hide();
    $(".uncollapse").show();
  });

  $(".uncollapse").click(function () {
    $(".main-body__search-wrapper").height(SEARCH_HEIGHT);
    $(".main-body__search-wrapper--container").show();
    $(this).hide();
    $(".collapse").show();
  });

  $(".left-side__collapse").click(function () {
    $("#main-layout").layout("collapse", "west");
  });

  $(".right-side__collapse").click(function () {
    $("#right-layout").layout("collapse", "south");
    setTimeout(() => {
      hot.updateSettings({
        height: $("#right-side__main-table").height() - 35,
      });
      hot.render();
    }, 200);
  });

  $("#right-layout").layout({
    onExpand: function (region) {
      if (region == "south") {
        hot.updateSettings({
          height: $("#right-side__main-table").height() - 35,
        });
        hot.render();
      }
    },
  });

  $("#right-layout").layout("collapse", "south");
  setTimeout(() => {
    hot.updateSettings({ height: $("#right-side__main-table").height() - 35 });
    hot.render();
  }, 200);

  $("#contSpecialStatus").combobox({
    panelHeight: "auto",
    valueField: "alias",
    editable: false,
    textField: "text",
    data: [
      {
        alias: "",
        text: "Trạng thái",
      },

      {
        alias: SPECIAL_STATUS.pending,
        text: "Chưa kiểm tra",
        selected: true,
      },

      {
        alias: SPECIAL_STATUS.approve,
        text: "Đã kiểm tra",
      },

      {
        alias: SPECIAL_STATUS.reject,
        text: "Đã từ chối kiểm tra",
      },
    ],
    onSelect: function (contSpecialStatus) {
      if (contSpecialStatus.alias != "") {
        shipment.params.contSpecialStatus = contSpecialStatus.alias;
      } else {
        shipment.params.contSpecialStatus = null;
      }
      loadTable();
    },
  });

  $("#logisticGroups").combobox({
    valueField: "id",
    textField: "groupName",
    data: logisticGroups,
    onSelect: function (logisticGroup) {
      if (logisticGroup.id != 0) {
        shipment.logisticGroupId = logisticGroup.id;
      } else {
        shipment.logisticGroupId = null;
      }
      $("#opr").combobox("select", "Chọn OPR");
      $("#containerNo").textbox("setText", "");
      loadTable();
    },
  });

  $("#blNo")
    .textbox("textbox")
    .bind("keydown", function (e) {
      // enter key
      if (e.keyCode == 13) {
        shipment.blNo = $("#blNo").textbox("getText").toUpperCase();
        loadTable();
      }
    });

  $("#containerNo")
    .textbox("textbox")
    .bind("keydown", function (e) {
      // enter key
      if (e.keyCode == 13) {
        shipment.params.containerNo = $("#containerNo")
          .textbox("getText")
          .toUpperCase();
        loadTable();
      }
    });

  $("#opr").combobox({
    panelMaxHeight: 200,
    valueField: "dictValue",
    textField: "dictLabel",
    data: oprList,
    onSelect: function (opr) {
      if (opr.dictValue != "Chọn OPR") {
        shipment.opeCode = opr.dictValue;
      } else {
        shipment.opeCode = null;
      }
      loadTable();
    },
  });
  $("#opr").combobox("select", "Chọn OPR");

  // loadTable();
});

function loadTable() {
  $("#dg").datagrid({
    url: PREFIX + "/shipments",
    height:
      $(document).height() - $(".main-body__search-wrapper").height() - 70,
    method: "POST",
    singleSelect: true,
    collapsible: true,
    clientPaging: false,
    pagination: true,
    rownumbers: true,
    onBeforeSelect: function (index, row) {
      getSelected(index, row);
    },
    pageSize: 50,
    nowrap: true,
    striped: true,
    loadMsg: " Đang xử lý...",
    loader: function (param, success, error) {
      var opts = $(this).datagrid("options");
      if (!opts.url) return false;
      $.ajax({
        type: opts.method,
        url: opts.url,
        contentType: "application/json",
        data: JSON.stringify({
          pageNum: param.page,
          pageSize: param.rows,
          orderByColumn: param.sort,
          isAsc: param.order,
          data: shipment,
        }),
        success: function (res) {
          if (res.code == 0) {
            success(res.shipments);
            $("#dg").datagrid("selectRow", 0);
          } else {
            success([]);
            loadShipmentDetails(null);
          }
        },
        error: function () {
          error.apply(this, arguments);
        },
      });
    },
  });
}

// FORMATTER
// Format logistic name for clickable show link
function formatLogistic(value, row, index) {
  return (
    '<a onclick="logisticInfo(' +
    row.logisticGroupId +
    "," +
    "'" +
    value +
    "')\"> " +
    value +
    "</a>"
  );
}
// FORMAT DATE FOR date time format dd/mm/yyyy
function formatDate(value) {
  let date = new Date(value);
  let day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
  let month = date.getMonth() + 1;
  let monthText = month < 10 ? "0" + month : month;
  return day + "/" + monthText + "/" + date.getFullYear();
}

function formatBlBooking(value, row) {
  if (row) {
    if (row.blNo) {
      return row.blNo;
    } else if (row.bookingNo) {
      return row.bookingNo;
    }
  }
  return "";
}

// Trigger when click a row in easy ui data grid on the left screen
function getSelected(index, row) {
  if (row) {
    shipmentSelected = row;
    rowAmount = shipmentSelected.containerAmount;
    checkList = Array(rowAmount).fill(0);
    allChecked = false;
    let title = "";
    title += "Mã Lô: " + row.id + " - ";
    title += "SL: " + row.containerAmount + " - ";
    title += "B/L No: ";
    if (row.blNo != null) {
      title += row.blNo;
    } else {
      title += "Trống";
    }
    $("#shipmentInfo").html(title);
  }
  loadShipmentDetails(shipmentSelected.id);
  loadListComment();
}

// FORMAT HANDSONTABLE COLUMN
function checkBoxRenderer(instance, td, row, col, prop, value, cellProperties) {
  let content = "";
  if (checkList[row] == 1) {
    content +=
      '<div><input type="checkbox" id="check' +
      row +
      '" onclick="check(' +
      row +
      ')" checked></div>';
  } else {
    content +=
      '<div><input type="checkbox" id="check' +
      row +
      '" onclick="check(' +
      row +
      ')"></div>';
  }
  $(td)
    .attr("id", "checkbox" + row)
    .addClass("htCenter")
    .addClass("htMiddle")
    .html(content);
  return td;
}
function historyRenderer(instance, td, row, col, prop, value, cellProperties) {
  let historyIcon =
    '<a id="custom" onclick="openHistoryFormCatos(' +
    row +
    ')" class="fa fa-window-restore easyui-tooltip" title="Lịch Sử Catos" aria-hidden="true" style="color: #3498db;"></a>';
  $(td).addClass("htCenter").addClass("htMiddle").html(historyIcon);
}

function historyEportRenderer(
  instance,
  td,
  row,
  col,
  prop,
  value,
  cellProperties
) {
  let historyIcon =
    '<a id="custom" onclick="openHistoryFormEport(' +
    row +
    ')" class="fa fa-history easyui-tooltip" title="Lịch Sử Eport" aria-hidden="true" style="color: #3498db;"></a>';
  $(td).addClass("htCenter").addClass("htMiddle").html(historyIcon);
}
function statusIconsRenderer(
  instance,
  td,
  row,
  col,
  prop,
  value,
  cellProperties
) {
  $(td)
    .attr("id", "statusIcon" + row)
    .addClass("htCenter")
    .addClass("htMiddle");
  if (
    sourceData[row] &&
    sourceData[row].dischargePort &&
    sourceData[row].processStatus &&
    sourceData[row].paymentStatus &&
    sourceData[row].customStatus &&
    sourceData[row].finishStatus
  ) {
    // Command process status
    let process =
      '<i id="verify" class="fa fa-windows easyui-tooltip" title="Chưa xác nhận" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #666"></i>';

    if (
      (!sourceData[row].contSpecialStatus ||
        sourceData[row].contSpecialStatus == SPECIAL_STATUS.approve) &&
      (!sourceData[row].dangerous ||
        sourceData[row].dangerous == DANGEROUS_STATUS.NOT ||
        sourceData[row].dangerous == DANGEROUS_STATUS.approve)
    ) {
      switch (sourceData[row].processStatus) {
        case "E":
          process =
            '<i id="verify" class="fa fa-windows easyui-tooltip" title="Đang chờ kết quả" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color : #f8ac59;"></i>';
          break;
        case "Y":
          process =
            '<i id="verify" class="fa fa-windows easyui-tooltip" title="Đã làm lệnh" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #1ab394;"></i>';
          break;
        case "N":
          process =
            '<i id="verify" class="fa fa-windows easyui-tooltip" title="Có thể làm lệnh" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #3498db;"></i>';
          break;
        case "D":
          process =
            '<i id="verify" class="fa fa-windows easyui-tooltip" title="Đang chờ hủy lệnh" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #f93838;"></i>';
          break;
      }
    }

    // Payment status
    let payment =
      '<i id="payment" class="fa fa-credit-card-alt easyui-tooltip" title="Chưa Thanh Toán" aria-hidden="true" style="margin-left: 8px; color: #666"></i>';
    switch (sourceData[row].paymentStatus) {
      case "E":
        payment =
          '<i id="payment" class="fa fa-credit-card-alt easyui-tooltip" title="Lỗi Thanh Toán" aria-hidden="true" style="margin-left: 8px; color : #ed5565;"></i>';
        break;
      case "Y":
        payment =
          '<i id="payment" class="fa fa-credit-card-alt easyui-tooltip" title="Đã Thanh Toán" aria-hidden="true" style="margin-left: 8px; color: #1ab394;"></i>';
        break;
      case "N":
        if (value > 1) {
          payment =
            '<i id="payment" class="fa fa-credit-card-alt easyui-tooltip" title="Chờ Thanh Toán" aria-hidden="true" style="margin-left: 8px; color: #3498db;"></i>';
        }
        break;
    }
    // Customs Status
    let customs =
      '<i id="custom" class="fa fa-shield easyui-tooltip" title="Chờ Thông Quan" aria-hidden="true" style="margin-left: 8px; color: #666;"></i>';
    switch (sourceData[row].customStatus) {
      case "R":
        customs =
          '<i id="custom" class="fa fa-shield easyui-tooltip" title="Đã Thông Quan" aria-hidden="true" style="margin-left: 8px; color: #1ab394;"></i>';
        break;
      case "Y":
        customs =
          '<i id="custom" class="fa fa-shield easyui-tooltip" title="Chưa Thông Quan" aria-hidden="true" style="margin-left: 8px; color: #ed5565;"></i>';
        break;
      case "N":
        customs =
          '<i id="custom" class="fa fa-shield easyui-tooltip" title="Chờ Thông Quan" aria-hidden="true" style="margin-left: 8px; color: #3498db;"></i>';
        break;
    }
    // released status
    let released =
      '<i id="finish" class="fa fa-ship easyui-tooltip" title="Chưa Thể Giao Container" aria-hidden="true" style="margin-left: 8px; color: #666;"></i>';
    switch (sourceData[row].finishStatus) {
      case "Y":
        released =
          '<i id="finish" class="fa fa-ship easyui-tooltip" title="Đã Giao Container" aria-hidden="true" style="margin-left: 8px; color: #1ab394;"></i>';
        break;
      case "N":
        if (sourceData[row].paymentStatus == "Y") {
          released =
            '<i id="finish" class="fa fa-ship easyui-tooltip" title="Có Thể Giao Container" aria-hidden="true" style="margin-left: 8px; color: #3498db;"></i>';
        }
        break;
    }
    // Return the content
    let content = "<div>";

    content += getRequestConfigIcon(
      sourceData[row].contSpecialStatus,
      sourceData[row].dangerous
    );

    content += process + payment;
    // Domestic cont: VN --> not show
    if (sourceData[row].dischargePort.substring(0, 2) != "VN") {
      content += customs;
    }
    content += released + "</div>";
    $(td).html(content);
  }
  return td;
}

function getRequestConfigIcon(contSpecialStatus, dangerous) {
  let contSpecialStatusResult = "";

  if (
    contSpecialStatus == SPECIAL_STATUS.reject ||
    dangerous == DANGEROUS_STATUS.reject
  ) {
    contSpecialStatusResult =
      '<i id="verify" class="fa fa-user-circle-o" title="Yêu cầu xác nhận bị từ chối" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #ff0000"></i>';
  } else if (
    contSpecialStatus == SPECIAL_STATUS.pending ||
    dangerous == DANGEROUS_STATUS.pending
  ) {
    if (
      contSpecialStatus == SPECIAL_STATUS.pending &&
      dangerous == DANGEROUS_STATUS.approve
    ) {
      contSpecialStatusResult =
        '<i id="verify" class="fa fa-user-circle-o" title="Đang chờ yêu cầu xác nhận từ tổ đặc biệt" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #e6e600"></i>';
    } else if (
      contSpecialStatus == SPECIAL_STATUS.approve &&
      dangerous == DANGEROUS_STATUS.pending
    ) {
      contSpecialStatusResult =
        '<i id="verify" class="fa fa-user-circle-o" title="Đang chờ yêu cầu xác nhận từ tổ nguy hiểm" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #e6e600"></i>';
    } else {
      contSpecialStatusResult =
        '<i id="verify" class="fa fa-user-circle-o" title="Đang chờ yêu cầu xác nhận" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #f8ac59"></i>';
    }
  } else if (
    contSpecialStatus == SPECIAL_STATUS.approve &&
    (!dangerous ||
      dangerous == DANGEROUS_STATUS.NOT ||
      dangerous == DANGEROUS_STATUS.approve)
  ) {
    contSpecialStatusResult =
      '<i id="verify" class="fa fa-user-circle-o" title="Yêu cầu xác nhật đã được duyệt" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #1ab394"></i>';
  } else if (
    contSpecialStatus == SPECIAL_STATUS.yet ||
    dangerous == DANGEROUS_STATUS.yet
  ) {
    contSpecialStatusResult =
      '<i id="verify" class="fa fa-user-circle-o" title="Có thể yêu cầu xác nhận" aria-hidden="true" style="margin-left: 8px; font-size: 15px; color: #3498db"></i>';
  }
  return contSpecialStatusResult;
}
/**
 * Render button
 */
function btnDetailRenderer(
  instance,
  td,
  row,
  col,
  prop,
  value,
  cellProperties
) {
  $(td)
    .attr("id", "wgt" + row)
    .addClass("htMiddle")
    .addClass("htCenter");
  let containerNo, sztp;

  if (sourceData && sourceData.length > 0) {
    if (sourceData.length > row && sourceData[row].id) {
      value = `<button class="btn btn-default btn-xs" onclick="openDetail('${sourceData[row].id}', '${containerNo}', '${sztp}', '${row}')"><i class="fa fa-check-circle"></i>Chi tiết</button>`;
    } else if (containerNo && sztp) {
      value =
        '<button class="btn btn-success btn-xs" id="detailBtn ' +
        row +
        '" onclick="openDetail(' +
        null +
        ",'" +
        containerNo +
        "'," +
        "'" +
        sztp +
        '\')"><i class="fa fa-check-circle"></i>Chi tiết</button>';
    } else {
      value =
        '<button class="btn btn-success btn-xs" id="detailBtn ' +
        row +
        '" onclick="openDetail(' +
        null +
        ",'" +
        containerNo +
        "'," +
        "'" +
        sztp +
        '\')" disabled><i class="fa fa-check-circle"></i>Chi tiết</button>';
    }
  }
  $(td).html(value);
  cellProperties.readOnly = "true";
  return td;
}

function openDetail(id, containerNo, sztp, row) {
  if (!id) {
    id = 0;
  }
  detailInformationForContainerSpecial.indexSelected = row;
  $.modal.openCustomForm(
    "Khai báo chi tiết",
    `${PREFIX}/shipment-detail/${id}/cont/${containerNo}/sztp/${sztp}/detail`,
    800,
    460
  );
}

function containerNoRenderer(
  instance,
  td,
  row,
  col,
  prop,
  value,
  cellProperties
) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function sztpRenderer(instance, td, row, col, prop, value, cellProperties) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function consigneeRenderer(
  instance,
  td,
  row,
  col,
  prop,
  value,
  cellProperties
) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function vslNmRenderer(instance, td, row, col, prop, value, cellProperties) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  if (sourceData[row] && sourceData[row].vslNm && sourceData[row].voyNo) {
    $(td).html(
      '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
        sourceData[row].vslNm +
        " - " +
        sourceData[row].voyNo +
        "</div>"
    );
  }
  return td;
}
function wgtRenderer(instance, td, row, col, prop, value, cellProperties) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function cargoTypeRenderer(
  instance,
  td,
  row,
  col,
  prop,
  value,
  cellProperties
) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function sealNoRenderer(instance, td, row, col, prop, value, cellProperties) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function dischargePortRenderer(
  instance,
  td,
  row,
  col,
  prop,
  value,
  cellProperties
) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function payTypeRenderer(instance, td, row, col, prop, value, cellProperties) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function payerRenderer(instance, td, row, col, prop, value, cellProperties) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function orderNoRenderer(instance, td, row, col, prop, value, cellProperties) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}
function remarkRenderer(instance, td, row, col, prop, value, cellProperties) {
  $(td).addClass("htMiddle").addClass("htCenter");
  if (!value) {
    value = "";
  }
  $(td).html(
    '<div style="width: 100%; white-space: nowrap; text-overflow: ellipsis;">' +
      value +
      "</div>"
  );
  return td;
}

// CONFIGURATE HANDSONTABLE
function configHandson() {
  config = {
    stretchH: "all",
    height: $("#right-side__main-table").height() - 35,
    minRows: rowAmount,
    maxRows: rowAmount,
    width: "100%",
    minSpareRows: 0,
    rowHeights: 30,
    fixedColumnsLeft: 4,
    manualColumnResize: true,
    manualRowResize: true,
    renderAllRows: true,
    rowHeaders: true,
    className: "htMiddle",
    colHeaders: function (col) {
      switch (col) {
        case 0:
          var txt = "<input type='checkbox' class='checker' ";
          txt += "onclick='checkAll()' ";
          txt += ">";
          return txt;
        case 1:
          return '<a class="fa fa-window-restore easyui-tooltip" title="Lịch Sử Catos" aria-hidden="true" style="color: #3498db;"></a>';
        case 2:
          return '<a class="fa fa-history easyui-tooltip" title="Lịch Sử Catos" aria-hidden="true" style="color: #3498db;"></a>';
        case 3:
          return "Trạng Thái";
        case 4:
          return "Số Tham Chiếu";
        case 5:
          return "Số Container";
        case 6:
          return '<span class="required">Chi Tiết</span>';
        case 7:
          return "Sztp";
        case 8:
          return "Chủ Hàng";
        case 9:
          return "Tàu - Chuyến";
        case 10:
          return "Trọng Lượng";
        case 11:
          return "Loại Hàng";
        case 12:
          return "Số Seal";
        case 13:
          return "Cảng Dỡ Hàng";
        case 14:
          return "P.T.T.T";
        case 15:
          return "Payer";
        case 16:
          return "Ghi Chú";
      }
    },
    colWidths: [
      23,
      21,
      21,
      120,
      130,
      100,
      150,
      60,
      200,
      100,
      100,
      80,
      80,
      100,
      100,
      100,
      100,
    ],
    filter: "true",
    columns: [
      {
        data: "active",
        type: "checkbox",
        renderer: checkBoxRenderer,
      },
      {
        data: "historyCatos",
        readOnly: true,
        renderer: historyRenderer,
      },
      {
        data: "historyEport",
        readOnly: true,
        renderer: historyEportRenderer,
      },
      {
        data: "status",
        readOnly: true,
        renderer: statusIconsRenderer,
      },

      {
        data: "orderNo",
        renderer: orderNoRenderer,
      },
      {
        data: "containerNo",
        renderer: containerNoRenderer,
      },
      {
        data: "btnInformationContainer",
        renderer: btnDetailRenderer,
      },
      {
        data: "sztp",
        renderer: sztpRenderer,
      },

      {
        data: "consignee",
        renderer: consigneeRenderer,
      },
      {
        data: "vslNm",
        renderer: vslNmRenderer,
      },
      {
        data: "wgt",
        renderer: wgtRenderer,
      },
      {
        data: "cargoType",
        renderer: cargoTypeRenderer,
      },
      {
        data: "sealNo",
        renderer: sealNoRenderer,
      },
      {
        data: "dischargePort",
        renderer: dischargePortRenderer,
      },
      {
        data: "payType",
        renderer: payTypeRenderer,
      },
      {
        data: "payer",
        renderer: payerRenderer,
      },
      {
        data: "remark",
        renderer: remarkRenderer,
      },
    ],
    beforeKeyDown: function (e) {
      let selected = hot.getSelected()[0];
      switch (e.keyCode) {
        // Arrow Left
        case 37:
          if (selected[3] == 0) {
            e.stopImmediatePropagation();
          }
          break;
        // Arrow Up
        case 38:
          if (selected[2] == 0) {
            e.stopImmediatePropagation();
          }
          break;
        // Arrow Right
        case 39:
          if (selected[3] == 15) {
            e.stopImmediatePropagation();
          }
          break;
        // Arrow Down
        case 40:
          if (selected[2] == shipmentSelected.containerAmount - 1) {
            e.stopImmediatePropagation();
          }
          break;
        default:
          break;
      }
    },
  };
}
configHandson();
hot = new Handsontable(dogrid, config);

function loadShipmentDetails(id) {
  if (id) {
    $.modal.loading("Đang xử lý ...");
    $.ajax({
      url:
        PREFIX +
        "/shipment/" +
        id +
        "/shipmentDetails/constSpecialStatus/" +
        shipment.params.contSpecialStatus,
      method: "GET",
      success: function (res) {
        $.modal.closeLoading();
        if (res.code == 0) {
          checkList = Array(rowAmount).fill(0);
          allChecked = false;
          $(".checker").prop("checked", false);
          for (let i = 0; i < checkList.length; i++) {
            $("#check" + i).prop("checked", false);
          }
          sourceData = res.shipmentDetails;
          hot.destroy();
          configHandson();
          hot = new Handsontable(dogrid, config);
          hot.loadData(sourceData);
          hot.render();
        }
      },
      error: function (data) {
        $.modal.closeLoading();
      },
    });
  } else {
    hot.destroy();
    configHandson();
    hot = new Handsontable(dogrid, config);
    hot.loadData([]);
    hot.render();
  }
}

// TRIGGER CHECK ALL SHIPMENT DETAIL
function checkAll() {
  if (!allChecked) {
    allChecked = true;
    for (let i = 0; i < checkList.length; i++) {
      if (hot.getDataAtCell(i, 3) == null) {
        break;
      }
      checkList[i] = 1;
      $("#check" + i).prop("checked", true);
    }
  } else {
    allChecked = false;
    checkList = Array(rowAmount).fill(0);
    for (let i = 0; i < checkList.length; i++) {
      $("#check" + i).prop("checked", false);
    }
  }
  let tempCheck = allChecked;
  updateLayout();
  hot.render();
  allChecked = tempCheck;
  $(".checker").prop("checked", allChecked);
}
function check(id) {
  if (sourceData[id].id != null) {
    if (checkList[id] == 0) {
      $("#check" + id).prop("checked", true);
      checkList[id] = 1;
    } else {
      $("#check" + id).prop("checked", false);
      checkList[id] = 0;
    }
    hot.render();
    updateLayout();
  }
}
function updateLayout() {
  allChecked = true;
  for (let i = 0; i < checkList.length; i++) {
    if (hot.getDataAtCell(i, 3) != null) {
      if (checkList[i] != 1) {
        allChecked = false;
      }
    }
  }
  $(".checker").prop("checked", allChecked);
}

// GET CHECKED SHIPMENT DETAIL LIST, VALIDATE FIELD WHEN isValidate = true
function getDataSelectedFromTable() {
  let myTableData = hot.getSourceData();
  let errorFlg = false;
  if (myTableData && checkList) {
    let cleanedGridData = [];
    for (let i = 0; i < checkList.length; i++) {
      if (Object.keys(myTableData[i]).length > 0) {
        if (checkList[i] == 1) {
          cleanedGridData.push(myTableData[i]);
        }
      }
    }
    shipmentDetailIds = "";
    $.each(cleanedGridData, function (index, object) {
      // if ('N' == object["paymentStatus"]) {
      //   errorFlg = true;
      //   $.modal.alertWarning("Không thể xác nhận chứng từ gốc cho container chưa thanh toán. Vui lòng kiểm tra lại.");
      //   return false;
      // }
      shipmentDetailIds += object["id"] + ",";
    });

    if (!errorFlg) {
      if (shipmentDetailIds.length == 0) {
        $.modal.alertWarning("Bạn chưa chọn container nào.");
        errorFlg = true;
      } else {
        shipmentDetailIds = shipmentDetailIds.substring(
          0,
          shipmentDetailIds.length - 1
        );
      }
    }
  } else {
    $.modal.alertWarning("Bạn chưa chọn lô.");
    errorFlg = true;
  }

  if (errorFlg) {
    return false;
  } else {
    return true;
  }
}

function search() {
  loadTable();
}

function clearInput() {
  $("#opr").combobox("select", "Chọn OPR");
  $("#logisticGroups").combobox("select", "0");
  $("#doStatus").combobox("select", "");
  $("#containerNo").textbox("setText", "");
  $("#blNo").textbox("setText", "");
  shipment = new Object();
  shipment.params = new Object();
  loadTable();
}

function confirmRequestDocument() {
  if (getDataSelectedFromTable()) {
    layer.confirm(
      "Xác nhận kiểm tra thông tin đúng.",
      {
        icon: 3,
        title: "Xác Nhận",
        btn: ["Đồng Ý", "Hủy Bỏ"],
      },
      function () {
        $.modal.loading("Đang xử lý ...");
        layer.close(layer.index);
        $.ajax({
          url: PREFIX + "/confirmation",
          method: "POST",
          data: {
            shipmentDetailIds: shipmentDetailIds,
            logisticGroupId: shipmentSelected.logisticGroupId,
          },
          success: function (res) {
            $.modal.closeLoading();
            if (res.code == 0) {
              $.modal.alertSuccess(res.msg);

              loadTable();
            } else {
              $.modal.alertError(res.msg);
            }
          },
          error: function (data) {
            $.modal.closeLoading();
          },
        });
      },
      function () {
        // close form
      }
    );
  }
}

function rejectRequestDocument() {
  if (getDataSelectedFromTable()) {
    layer.confirm(
      "Từ chối kiểm tra thông tin đúng.",
      {
        icon: 3,
        title: "Xác Nhận",
        btn: ["Đồng Ý", "Hủy Bỏ"],
      },
      function () {
        layer.close(layer.index);
        openReject();
      },
      function () {}
    );
  }
}

function logisticInfo(id, logistics) {
  $.modal.openLogisticInfo(
    "Thông tin liên lạc " + logistics,
    ctx + "om/support/logistics/" + id + "/info",
    null,
    470,
    function () {
      $.modal.close();
    }
  );
}

function loadListComment(shipmentCommentId) {
  let req = {
    serviceType: 1,
    shipmentId: shipmentSelected.id,
  };
  $.ajax({
    url: ctx + "shipment-comment/shipment/list",
    method: "POST",
    contentType: "application/json",
    data: JSON.stringify(req),
    success: function (data) {
      if (data.code == 0) {
        let html = "";
        // set title for panel comment
        let commentTitle = '<span style="color: black">Hỗ Trợ<span>';
        let commentNumber = 0;
        if (data.shipmentComments != null) {
          data.shipmentComments.forEach(function (element, index) {
            let createTime = element.createTime;
            let date = "";
            let time = "";
            if (createTime) {
              date =
                createTime.substring(8, 10) +
                "/" +
                createTime.substring(5, 7) +
                "/" +
                createTime.substring(0, 4);
              time = createTime.substring(10, 19);
            }

            let resolvedBackground = "";
            if (
              (shipmentCommentId && shipmentCommentId == element.id) ||
              !element.resolvedFlg
            ) {
              resolvedBackground = 'style="background-color: #ececec;"';
              commentNumber++;
            }

            html += "<div " + resolvedBackground + ">";
            // User name comment and date time comment
            html +=
              '<div><i style="font-size: 15px; color: #015198;" class="fa fa-user-circle" aria-hidden="true"></i><span> <a>' +
              element.userName +
              " (" +
              element.userAlias +
              ")</a>: <i>" +
              date +
              " at " +
              time +
              "</i></span></div>";
            // Topic comment
            html +=
              "<div><span><strong>Yêu cầu:</strong> " +
              element.topic +
              "</span></div>";
            // Content comment
            html +=
              "<div><span>" +
              element.content.replaceAll("#{domain}", domain) +
              "</span></div>";
            html += "</div>";
            html += "<hr>";
          });
        }
        commentTitle +=
          ' <span class="round-notify-count">' + commentNumber + "</span>";
        $("#right-layout")
          .layout("panel", "expandSouth")
          .panel("setTitle", commentTitle);
        $("#commentList").html(html);
        // $("#comment-div").animate({ scrollTop: $("#comment-div")[0].scrollHeight}, 1000);
      }
    },
  });
}

function addComment() {
  let topic = $("#topic").textbox("getText");
  let content = $(".summernote").summernote("code"); // get editor content
  let errorFlg = false;
  if (!topic) {
    errorFlg = true;
    $.modal.alertWarning("Vui lòng nhập chủ đề.");
  } else if (!content) {
    errorFlg = true;
    $.modal.alertWarning("Vui lòng nhập nội dung.");
  }
  if (!errorFlg) {
    let req = {
      topic: topic,
      content: content,
      shipmentId: shipmentSelected.id,
      logisticGroupId: shipmentSelected.logisticGroupId,
    };
    $.ajax({
      url: PREFIX + "/shipment/comment",
      type: "post",
      contentType: "application/json",
      data: JSON.stringify(req),
      beforeSend: function () {
        $.modal.loading("Đang xử lý, vui lòng chờ...");
      },
      success: function (result) {
        $.modal.closeLoading();
        if (result.code == 0) {
          loadListComment(result.shipmentCommentId);
          $.modal.msgSuccess("Gửi thành công.");
          $("#topic").textbox("setText", "");
          $(".summernote").summernote("code", "");
        } else {
          $.modal.msgError("Gửi thất bại.");
        }
      },
      error: function (error) {
        $.modal.closeLoading();
        $.modal.msgError("Gửi thất bại.");
      },
    });
  }
}

function openHistoryFormCatos(row) {
  let containerInfo = sourceData[row];
  let vslCd = "EMTY";
  let voyNo = "0000";
  let containerNo = containerInfo.containerNo;
  if (containerInfo == null || !containerNo || !vslCd || !voyNo) {
    $.modal.alertWarning("Container chưa được khai báo.");
  } else {
    layer.open({
      type: 2,
      area: [1002 + "px", 500 + "px"],
      fix: true,
      maxmin: true,
      shade: 0.3,
      title: "Lịch Sử Container " + containerNo + " Catos",
      content:
        HIST_PREFIX +
        "/container/history/" +
        voyNo +
        "/" +
        vslCd +
        "/" +
        containerNo,
      btn: ["Đóng"],
      shadeClose: false,
      yes: function (index, layero) {
        layer.close(index);
      },
    });
  }
}

function openHistoryFormEport(row) {
  let containerInfo = sourceData[row];
  if (containerInfo == null || !containerInfo.id) {
    $.modal.alertWarning("Container chưa được khai báo.");
  } else {
    layer.open({
      type: 2,
      area: [967 + "px", 500 + "px"],
      fix: true,
      maxmin: true,
      shade: 0.3,
      title:
        "Lịch Sử Container " +
        (containerInfo.containerNo != null ? containerInfo.containerNo : "") +
        " Eport",
      content: HIST_PREFIX + "/container/history/" + containerInfo.id,
      btn: ["Đóng"],
      shadeClose: false,
      yes: function (index, layero) {
        layer.close(index);
      },
    });
  }
}

/**
 * @param {none}
 * @description open model reject.js
 * @author Khanh
 */
function openReject() {
  $.modal.openCustomForm(
    "Khai báo lí do từ chối",
    `${PREFIX}/reject/shipment/${shipmentSelected.id}/logistic-group/${shipmentSelected.logisticGroupId}/shipment-detail/${shipmentDetailIds}`,
    800,
    260
  );
}

/**
 * @param {none}
 * @description handel event add coment in reject.js to load coments
 * @author Khanh
 */
function handleLoadCommentFromModelReject(shipmentCommentId) {
  loadListComment(shipmentCommentId);
}

/**
 * @param {none}
 * @description handel event load again table in reject.js to load Table
 * @author Khanh
 */
function handleLoadTableFromModel() {
  loadTable();
}

function formatServiceType(value, row) {
  switch (value) {
    case 1:
      return "Bốc Hàng";
    case 2:
      return "Hạ Rỗng";
    case 3:
      return "Bốc Rỗng";
    case 4:
      return "Hạ Hàng";
    default:
      return "";
  }
}
