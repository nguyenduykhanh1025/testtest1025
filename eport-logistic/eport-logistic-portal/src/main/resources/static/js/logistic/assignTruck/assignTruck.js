var prefix = ctx + "logistic/assignTruck";
var shipmentType = 1;
var shipmentSelected;
var dataAssignedDriver = [];
var dataDriver = [];
var dataContainerList = [];
var shipmentSearch = new Object;
// HANDLE COLLAPSE SHIPMENT LIST
$(document).ready(function () {
    //DEFAULT SEARCH FOLLOW DATE
    let fromMonth = (new Date().getMonth() < 10) ? "0" + (new Date().getMonth()) : new Date().getMonth();
    let toMonth = (new Date().getMonth() +2 < 10) ? "0" + (new Date().getMonth() +2 ): new Date().getMonth() +2;
    $('#fromDate').val("01/"+ fromMonth + "/" + new Date().getFullYear());
    $('#toDate').val("01/"+ (toMonth > 12 ? "01" +"/"+ (new Date().getFullYear()+1)  : toMonth + "/" + new Date().getFullYear()));
    let fromDate = stringToDate($('#fromDate').val());
    let toDate =  stringToDate($('#toDate').val());
    fromDate.setHours(0,0,0);
    toDate.setHours(23, 59, 59);
    shipmentSearch.fromDate = fromDate.getTime();
    shipmentSearch.toDate = toDate.getTime();

    loadTable();
    $(".left-side").css("height", $(document).height());
    $("#btn-collapse").click(function () {
        handleCollapse(true);
    });
    $("#btn-uncollapse").click(function () {
        handleCollapse(false);
    });

    //find date
    $('.from-date').datetimepicker({
        language: 'en',
        format: 'dd/mm/yyyy',
        autoclose: true,
        todayBtn: true,
        todayHighlight: true,
        pickTime: false,
        minView: 2
    });
    $('.to-date').datetimepicker({
        language: 'en',
        format: 'dd/mm/yyyy',
        autoclose: true,
        todayBtn: true,
        todayHighlight: true,
        pickTime: false,
        minView: 2
    });
});

//search date
function changeFromDate() {
    let fromDate = stringToDate($('#fromDate').val());
    if ($('#toDate').val() != '' && stringToDate($('#toDate').val()).getTime() < fromDate.getTime()) {
        $.modal.alertError('Quý khách không thể chọn từ ngày cao hơn đến ngày.')
        $('#fromDate').val('');
    } else {
        shipmentSearch.fromDate = fromDate.getTime();
        loadTable();
    }
}

function changeToDate() {
    let toDate = stringToDate($('.to-date').val());
    if ($('.from-date').val() != '' && stringToDate($('.from-date').val()).getTime() > toDate.getTime()) {
        $.modal.alertError('Quý khách không thể chọn đến ngày thấp hơn từ ngày.')
        $('.to-date').val('');
    } else {
        toDate.setHours(23, 59, 59);
        shipmentSearch.toDate = toDate.getTime();
        loadTable();
    }
}

function stringToDate(dateStr) {
    let dateParts = dateStr.split('/');
    return new Date(dateParts[2], dateParts[1] - 1, dateParts[0]);
}
function handleCollapse(status) {
    if (status) {
        $(".left-side").css("width", "0.5%");
        $(".left-side").children().hide();
        $("#btn-collapse").hide();
        $("#btn-uncollapse").show();
        $(".right-side").css("width", "99%");
        setTimeout(() => {
            checkForChanges();
            hot.render();
        }, 500);
        return;
    }
    $(".left-side").css("width", "33%");
    $(".left-side").children().show();
    $("#btn-collapse").show();
    $("#btn-uncollapse").hide();
    $(".right-side").css("width", "67%");
    setTimeout(() => {
        checkForChanges();
        hot.render();
    }, 500);
}
function assignFollowBatchTab() {
    $(".assignFollowBatch").show();
    $("#batchBtn").css({"background-color": "#6c9dc7"});
    $(".assignFollowContainer").hide();
    $("#containerBtn").css({"background-color": "#c7c1c1"});
    // let row = $("#dg").datagrid("getSelected");
    // if(row){
    //     loadDriver(row.id);
    // }
    if(dataAssignedDriver.length >0){
        $("#pickedDriverTable").datagrid('loadData', dataAssignedDriver);
        checkForChanges();

    }
    if(dataDriver.length > 0){
        $("#driverTable").datagrid('loadData', dataDriver)
        checkForChanges();
    }
}

function assignFollowContainerTab() {
    $(".assignFollowContainer").css("display","flex");;
    $("#containerBtn").css({"background-color": "#6c9dc7"});
    $(".assignFollowBatch").hide();
    $("#batchBtn").css({"background-color": "#c7c1c1"});
    // let row = $("#dg").datagrid("getSelected");
    // if(row){
    //     loadShipmentDetail(row.id);
    // }
    if(dataContainerList.length > 0){
        $("#dgShipmentDetail").datagrid('loadData', dataContainerList)
        checkForChanges();
    }
}
// LOAD SHIPMENT LIST
function loadTable() {
    shipmentSearch.serviceType = $('#shipmentType').val();
	//shipment
    $("#dg").datagrid({
        url: prefix + "/listShipment",
        height: window.innerHeight - 100,
        method:"post",
        singleSelect: true,
        collapsible: true,
        clientPaging: false,
        pagination: true,
        onClickRow: function () {
            getSelectedShipment();
        },
        rownumbers:true,
        pageSize: 50,
        nowrap: false,
        striped: true,
        loadMsg: " Đang xử lý...",
        loader: function (param, success, error) {
            let opts = $(this).datagrid("options");
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
                  data: shipmentSearch
                }),
                success: function (data) {
                    success(data);
                    if ($('#shipmentType').val() == 1 || $('#shipmentType').val() == 2) {
                        $("#dg").datagrid("hideColumn", "bookingNo");
                        $("#dg").datagrid("showColumn", "blNo");
                        $("#bookingNoDiv").hide();
                        $("#blNoDiv").show();
                    } else if ($('#shipmentType').val() == 3 || $('#shipmentType').val() == 4) {
                        $("#dg").datagrid("hideColumn", "blNo");
                        $("#dg").datagrid("showColumn", "bookingNo");
                        $("#bookingNoDiv").show();
                        $("#blNoDiv").hide();
                    } else {
                        $("#dg").datagrid("hideColumn", "blNo");
                        $("#dg").datagrid("hideColumn", "bookingNo");
                        $("#bookingNoDiv").hide();
                        $("#blNoDiv").hide();
                    }
                },
                error: function () {
                    error.apply(this, arguments);
                },
            });
        },
    });
}
//FORMAT QUANTITY FOR SHIPMENT LIST
function formatQuantity(){
    
}
// FORMAT DATE FOR SHIPMENT LIST
function formatDate(value) {
	var date = new Date(value);
    var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
    var month = date.getMonth() + 1;
    var monthText = month < 10 ? "0" + month : month;
    let hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
    let minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
    return day + "/" + monthText + "/" + date.getFullYear() + " " + hours + ":" + minutes;
}

// HANDLE WHEN SELECT A SHIPMENT
function getSelectedShipment() {
    let row = $("#dg").datagrid("getSelected");
    if (row) {
        shipmentSelected = row;
        $("#batchCode").text(row.id);
        $("#taxCode").text(row.taxCode);
        $("#blNo").text(row.blNo);
        $("#bookingNo").text(row.bookingNo);
        $("#edoFlg").text(row.edoFlg == 1 ? "eDO" : "DO");
        loadRemarkFollowBatch(row.id);
        loadShipmentDetail(row.id);
        loadDriver(row.id);
        loadOutSource(row.id);
    }
}

function loadShipmentDetail(id) {
    //reset dataContainerList
    dataContainerList = []
    $("#dgShipmentDetail").datagrid({
        url: prefix + "/getShipmentDetail",
        height: window.innerHeight - 70,
        singleSelect: true,
        collapsible: true,
        rownumbers:true,
        clientPaging: false,
        nowrap: false,
        striped: true,
        loadMsg: " Đang xử lý...",
        loader: function (param, success, error) {
            let opts = $(this).datagrid("options");
            if (!opts.url) return false;
            $.ajax({
                type: opts.method,
                url: opts.url,
                data: {
                    shipmentId: id
                },
                dataType: "json",
                success: function (data) {
                    dataContainerList = data.rows;
                    success(data);
                    $("#dgShipmentDetail").datagrid("hideColumn", "id");
                    $("#quantity").text(data.total);
                    if ($('#shipmentType').val() == 1) {
                        $("#dgShipmentDetail").datagrid("showColumn", "preorderPickup");
                    } else {
                        $("#dgShipmentDetail").datagrid("hideColumn", "preorderPickup");
                    }
                },
                error: function () {
                    error.apply(this, arguments);
                },
            });
        },
    });
}

// HANDLE WHEN SELECT A SHIPMENTDETAIL
// function getSelectedShipmentDetail() {
//     let row = $("#dgShipmentDetail").datagrid("getSelected");
//     if (row) {
//         shipmentSelected = row;
//         $("#batchCode").text(row.id);
//         $("#taxCode").text(row.taxCode);
//         $("#blNo").text(row.blNo);
//         $("#bookingNo").text(row.bookingNo);
//         loadShipmentDetail(row.id);
//     }
// }

function loadRemarkFollowBatch(shipmentId){
    $('#remark').val('');
    $('#deliveryAddress').val('');
    $('#deliveryPhoneNumber').val('');
    // $.modal.loading("Đang xử lý ...");
    $.ajax({
        url: prefix + "/remark/batch/" + shipmentId,
        method: "GET",
        success: function (data) {
        	$.modal.closeLoading();
            if (data.code == 0) {
                if(data.remark){
                    $('#remark').val(data.remark);
                    $('#deliveryAddress').val(data.deliveryAddress);
                    $('#deliveryPhoneNumber').val(data.deliveryPhoneNumber);
                }
            }
        },
        error: function () {
        	$.modal.closeLoading();
            $.modal.alertError("Có lỗi trong quá trình tải dữ liệu, vui lòng thử lại sau.");
        },
    });
}
function formatPickup(value) {
    if (value == "Y") {
        return "<span class='label label-success'>Có</span>"
    } else{
        return "<span class='label label-default'>Không</span>"
    }
}

// function pickTruckForAll() {
//     if ($("#quantity").text() != "0") {
//         $.modal.openFullPickTruck("Điều xe", prefix + "/pickTruckForm/" + shipmentSelected.id + "/" + false + "/" + "0");
//     } else {
//         $.modal.alertError("Lô này hiện đang trống!");
//     }
// }

// function pickTruckForChosenList() {
//     if ($("#quantity").text() != "0") {
//         $.modal.openFullPickTruck("Điều xe", prefix + "/pickTruckForm/" + shipmentSelected.id + "/" + false + "/" + "0");
//     } else {
//         $.modal.alertError("Quý khách chưa chọn container!");
//     }
// }
///////////////////////ASSIGN DRIVER ////////////////////////////////////
var pickedIds = [];
function loadDriver(shipmentId){
    //reset dataDriver, dataAssignedDriver
    dataDriver = [];
    dataAssignedDriver = [];
    pickedIds = [];
    //pickedDriverList
    $("#pickedDriverTable").datagrid({
        url: prefix + "/assignedDriverAccountList",
        height: window.innerHeight/2 + 5,
        collapsible: true,
        clientPaging: false,
        nowrap: false,
        striped: true,
        loadMsg: " Đang xử lý...",
        loader: function (param, success, error) {
            let opts = $(this).datagrid("options");
            if (!opts.url) return false;
            $.ajax({
                type: opts.method,
                url: opts.url,
                data: {
                    shipmentId:shipmentId
                },
                dataType: "json",
                success: function (data) {
                    dataAssignedDriver = data;
                    success(data);
                    //driverList
                    let records =  $('#pickedDriverTable').datagrid('getRows');
                    if(records){
                        for(let i = 0; i < records.length; i++){
                            pickedIds.push(records[i].id);
                        }
                    }
                    $("#driverTable").datagrid({
                        url: prefix + "/listDriverAccount",
                        height: window.innerHeight/2 + 5,
                        collapsible: true,
                        clientPaging: false,
                        nowrap: false,
                        striped: true,
                        loadMsg: " Đang xử lý...",
                        loader: function (param, success, error) {
                            let opts = $(this).datagrid("options");
                            if (!opts.url) return false;
                            $.ajax({
                                type: opts.method,
                                url: opts.url,
                                data: {
                                    shipmentId: shipmentId,
                                    pickedIds: pickedIds
                                },
                                dataType: "json",
                                success: function (data) {
                                    dataDriver = data;
                                    success(data);
                                },
                                error: function () {
                                    error.apply(this, arguments);
                                },
                            });
                        },
                    });
                },
                error: function () {
                    error.apply(this, arguments);
                },
            });
        },
    });

}
function transferInToOut() {
    let rows = $('#driverTable').datagrid('getSelections');
    if(rows){
        for(let i=0; i< rows.length;i++){
            let index = $('#driverTable').datagrid('getRowIndex', rows[i]);
            $('#driverTable').datagrid('deleteRow', index);
            $('#pickedDriverTable').datagrid('appendRow', rows[i]);
        }
    }
}
function transferOutToIn() {
    let rows = $('#pickedDriverTable').datagrid('getSelections');
    if(rows){
        for(let i=0; i< rows.length;i++){
            let index = $('#pickedDriverTable').datagrid('getRowIndex', rows[i]);
            $('#pickedDriverTable').datagrid('deleteRow', index);
            $('#driverTable').datagrid('appendRow', rows[i]);
        }
    }
}

function transferAllToOut(){
    let rows =  $('#driverTable').datagrid('getRows');
    if(rows){
        for(let i=0; i< rows.length;i++){
            $('#pickedDriverTable').datagrid('appendRow', rows[i]);
        }
        $('#driverTable').datagrid('loadData', {"total":0,"rows":[]});
    }
}

function transferAllToIn(){
    let rows =  $('#pickedDriverTable').datagrid('getRows');
    if(rows){
        for(let i=0; i< rows.length;i++){
            $('#driverTable').datagrid('appendRow', rows[i]);
        }
        $('#pickedDriverTable').datagrid('loadData', {"total":0,"rows":[]});
    }
}

function save(){
    let rows = $('#pickedDriverTable').datagrid('getRows');
    let pickupAssigns = [];
    if(rows){
        for(let i=0; i< rows.length;i++){
            let object = new Object();
            object.driverId = rows[i].id;
            object.shipmentId = shipmentSelected.id;
            object.fullName = rows[i].fullName;
            object.phoneNumber = rows[i].mobileNumber;
            object.remark = $('#remark').val();
            object.deliveryAddress = $('#deliveryAddress').val();
            object.deliveryPhoneNumber = $('#deliveryPhoneNumber').val();
            pickupAssigns.push(object);
        }
    }
    if (getDataFromOutSource(true)){
        if(outsources.length > 0){
            for(let i=0;i < outsources.length; i++){
                outsources[i].remark = $('#remark').val();
                pickupAssigns.push(outsources[i])
            }
        }
        // $.modal.loading("Đang xử lý...");
        $.ajax({
            url: prefix + "/savePickupAssignFollowBatch",
            method: "post",
            contentType: "application/json",
            data: JSON.stringify(pickupAssigns),
            success: function(result){
                if(result.code == 0){
                    $.modal.msgSuccess(result.msg);
                    getSelectedShipment()
                }else{
                    $.modal.msgError(result.msg);
                }
                $.modal.closeLoading();
            },
            error: function (result) {
                $.modal.alertError("Có lỗi trong quá trình lưu dữ liệu, vui lòng thử lại sau.");
                $.modal.closeLoading();
            },
        })
    }

}

function formatAction(value, row, index) {
	let actions = [];
    actions.push('<a class="btn btn-primary btn-xs" onclick="editDriver(\'' + row.id + '\')"><i class="fa fa-edit"></i>Sửa</a> ');
    return actions.join('');
}

function formatRemark(value) {
    let remark = value;
    if(value){
        return '<div class="easyui-tooltip" title="' + value + '" style="width: 80; text-align: center;"><span>' + (remark.length < 15 ? value : "...") + '</span></div>';
    }
    return 
  }

function formatActionAssign(value, row, index) {
    let button = '';
    let shipment = $("#dg").datagrid("getSelected");
    if(shipment.serviceType == 1){ //receiveContFull
        if(row.preorderPickup == "Y"){
            button += '<button class="btn btn-primary btn-xs" onclick="assignFollowContainer(\'' + row.id + '\')"><i class="fa fa-edit"></i>Điều xe</button> ';
        }else{
            button += '<button class="btn btn-primary btn-xs" onclick="assignFollowContainer(\'' + row.id + '\')" disabled><i class="fa fa-edit"></i>Điều xe</button> ';
        }
    }else {//3 serviecType con lai
        button += '<button class="btn btn-primary btn-xs" onclick="assignFollowContainer(\'' + row.id + '\')"><i class="fa fa-edit"></i>Điều xe</button> ';
    }
    return button;
}

function editDriver(id){
    $.modal.open("Thông tin Tài xế ", prefix +"/edit/driver/"+id);
}

function assignFollowContainer(id){
    $.modal.openTab("Điều xe theo Container", prefix + "/preoderPickupAssign/" + id);
}

function addTruck(){
    $.modal.open("Thêm xe mới", "/logistic/logisticTruck/add");
}
function addDriver(){
    $.modal.open("Thêm xe mới", "/logistic/transport/add");
}

function finishAssignTruck(msg) {
    $.modal.msgSuccess(msg);
}

function finishAssignFollowCont(msg) {
    $.modal.msgSuccess(msg);
}
function checkForChanges(){		
    $('#driverTable').datagrid('resize');

    $('#pickedDriverTable').datagrid('resize');

    $('#dgShipmentDetail').datagrid('resize');
 }

 //---------------------------------THUE NGOAI------------------------------------------------
 var dogrid = document.getElementById("container-grid"), hot;
 var config;
 var outsources = [];
 function loadOutSource(shipmentId) {
	// $.modal.loading("Đang xử lý ...");
    $.ajax({
        url: prefix + "/out-source/batch/" + shipmentId,
        method: "GET",
        success: function (data) {
        	$.modal.closeLoading();
            if (data.code == 0) {
                hot.destroy();
                hot = new Handsontable(dogrid, config);
                hot.loadData(data.outSourceList);
                hot.render();
            }
        },
        error: function () {
        	$.modal.closeLoading();
            $.modal.alertError("Có lỗi trong quá trình tải dữ liệu, vui lòng thử lại sau.");
        },
    });
}
// CONFIGURATE HANDSONTABLE
config = {
    stretchH: "all",
    height: "100%",
    minRows: 5,
    maxRows: 20,
    width: "100%",
    minSpareRows: 1,
    rowHeights: 30,
    fixedColumnsLeft: 0,
    manualColumnResize: true,
    manualRowResize: true,
    renderAllRows: true,
    rowHeaders: true,
    className: "htMiddle",
    colHeaders: function (col) {
        switch (col) {
            case 0:
                return "Đơn vị chủ quản";
            case 1:
                return '<span class="required">Số điện thoại</span>';
            case 2:
                return '<span class="required">Họ và tên</span>';
            case 3:
                return '<span class="required">Xe đầu kéo</span>';
            case 4:
                return '<span class="required">Xe rơ mooc</span>';
        }
    },
    colWidths: [200, 100, 150, 100, 100],
    filter: "true",
    columns: [
        {
            data: "driverOwner",
            type: "autocomplete",
            source: driverOwnerList,
        },
        {
            data: "phoneNumber",
            type: "autocomplete",
        },
        {
            data: "fullName",
        },
        {
            data: "truckNo",
        },
        {
            data: "chassisNo",
        },
    ],
    afterChange: onChange
};

function onChange(changes, source) {
    if (!changes) {
        return;
    }
    changes.forEach(function (change) {
        if (change[1] == "driverOwner" && change[3] != null && change[3] != '') {
        	// $.modal.loading("Đang xử lý ...");
            $.ajax({
                url: prefix + "/owner/"+ change[3] +"/driver-phone-list",
                method: "GET",
                success: function (data) {
                	$.modal.closeLoading();
                    if (data.code == 0) {
                        hot.updateSettings({
                            cells: function (row, col, prop) {
                                if (row == change[0] && col == 1) {
                                    let cellProperties = {};
                                    cellProperties.source = data.driverPhoneList;
                                    return cellProperties;
                                }
                            }
                        });
                    }
                },
                error: function () {
                	$.modal.closeLoading();
                    $.modal.alertError("Có lỗi trong quá trình tải dữ liệu, vui lòng thử lại sau.");
                },
            });
        } else if (change[1] == "phoneNumber" && change[3] != null && change[3] != '') {
        	// $.modal.loading("Đang xử lý ...");
            $.ajax({
                url: prefix + "/driver-phone/" + change[3] + "/infor",
                method: "GET",
                success: function (data) {
                	$.modal.closeLoading();
                    if (data.code == 0) {
                        hot.setDataAtCell(change[0], 2, data.pickupAssign.fullName);
                        hot.setDataAtCell(change[0], 3, data.pickupAssign.truckNo);
                        hot.setDataAtCell(change[0], 4, data.pickupAssign.chassisNo);
                    }
                },
                error: function () {
                	$.modal.closeLoading();
                    $.modal.alertError("Có lỗi trong quá trình tải dữ liệu, vui lòng thử lại sau.");
                },
            });
        }
    });
}
// RENDER HANSONTABLE FIRST TIME
hot = new Handsontable(dogrid, config);

//GET DATA FROM HANDSOME
function getDataFromOutSource(){
    outsources = [];
    let myTableData = hot.getSourceData();
    let cleanedGridData = [];
    let errorFlg = false;
    for (let i = 0; i < myTableData.length; i++) {
        if (Object.keys(myTableData[i]).length > 0) {
            if (myTableData[i].driverOwner || myTableData[i].phoneNumber || myTableData[i].fullName || myTableData[i].truckNo || myTableData[i].chassisNo) {
                cleanedGridData.push(myTableData[i]);
            }
        }
    }
    $.each(cleanedGridData, function(index, object){
        let outsource = new Object();
        if(!object["phoneNumber"]){
            $.modal.alertError("Số điện thoại hàng:" + (index + 1) + " không được trống!");
            errorFlg = true;
            return false;
        }
        if(!object["fullName"]){
            $.modal.alertError("Họ tên hàng:" + (index + 1) + " không được trống!");
            errorFlg = true;
            return false;
        }
        if(!object["truckNo"]){
            $.modal.alertError("Biển số xe đầu kéo hàng:" + (index + 1) +" không được trống!");
            errorFlg = true;
            return false;
        }
        if(!object["chassisNo"]){
            $.modal.alertError("Biển số xe rơ mooc hàng:" + (index + 1) +" không được trống!");
            errorFlg = true;
            return false;
        }
        outsource.phoneNumber = object["phoneNumber"].trim();
        outsource.driverOwner = object["driverOwner"];
        outsource.truckNo = object["truckNo"].trim().toUpperCase();
        outsource.fullName = object["fullName"].trim();
        outsource.chassisNo = object["chassisNo"].trim().toUpperCase();
        outsource.shipmentId = shipmentSelected.id
        outsource.externalFlg = 1;
        outsources.push(outsource);
    })
    if (errorFlg) {
        return false;
    } else {
        return true;
    }
}
function generatePDF() {
	if(!shipmentSelected){
		$.modal.alertError("Bạn chưa chọn Lô!");
		return
	}
    $.modal.openTab("In phiếu", ctx +"logistic/print/shipment/"+shipmentSelected.id);
}