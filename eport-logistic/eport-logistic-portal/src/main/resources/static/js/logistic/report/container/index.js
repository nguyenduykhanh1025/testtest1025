const PREFIX = ctx + "logistic/report/container";
var pickupHistory = new Object();

$(document).ready(function() {
    //DEFAULT SEARCH FOLLOW DATE
    let fromMonth = (new Date().getMonth()+1 < 10) ? "0" + (new Date().getMonth()+1) : new Date().getMonth()+1;
    let toMonth = (new Date().getMonth() +2 < 10) ? "0" + (new Date().getMonth() +2 ): new Date().getMonth() +2;
    $('#fromDate').val("01/"+ fromMonth + "/" + new Date().getFullYear());
    $('#toDate').val("01/"+ (toMonth > 12 ? "01" +"/"+ (new Date().getFullYear()+1)  : toMonth + "/" + new Date().getFullYear()));
    let fromDate = stringToDate($('#fromDate').val());
    let toDate =  stringToDate($('#toDate').val());
    fromDate.setHours(0,0,0);
    toDate.setHours(23, 59, 59);
    pickupHistory.fromDate = fromDate.getTime();
    pickupHistory.toDate = toDate.getTime();

    loadTable();

    $('#searchAllInput').keyup(function(event) {
        if (event.keyCode == 13) {
            pickupHistory.blNo = $('#searchAllInput').val().toUpperCase();
            pickupHistory.bookingNo = $('#searchAllInput').val().toUpperCase();
            pickupHistory.containerNo = $('#searchAllInput').val().toUpperCase();
            pickupHistory.truckNo = $('#searchAllInput').val().toUpperCase();
            pickupHistory.chassisNo = $('#searchAllInput').val().toUpperCase();
            pickupHistory.sztp = $('#searchAllInput').val().toUpperCase();
            pickupHistory.vslNm = $('#searchAllInput').val().toUpperCase();
            pickupHistory.voyNo = $('#searchAllInput').val().toUpperCase();
            loadTable();
        }
    });
});

function loadTable() {
    $("#dg").datagrid({
        url: PREFIX + "/list",
        method: "POST",
        singleSelect: true,
        height: currentHeight,
        clientPaging: false,
        pagination: true,
        rownumbers: true,
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
                accept: 'text/plain',
                dataType: 'text',
                data: JSON.stringify({
                    pageNum: param.page,
                    pageSize: param.rows,
                    orderByColumn: param.sort,
                    isAsc: param.order,
                    data: pickupHistory
                }),
                success: function (data) {
                    success(JSON.parse(data));
                },
                error: function () {
                    error.apply(this, arguments);
                },
            });
        },
    });
}

function refresh() {
    $('#searchAllInput').val('');
    $('#seviceTypeSelect').val('');
    $('#fromDate').val('');
    $('#toDate').val('');
    pickupStory = new Object();
    loadTable();
}

function formatBlBooking(value, row) {
    if (row.shipmentDetail) {
        if (row.shipmentDetail.blNo) {
            return row.shipmentDetail.blNo;
        }
        if (row.shipmentDetail.bookingNo) {
            return row.shipmentDetail.bookingNo;
        }
    }
    return '';
}

function formatSztp(value, row) {
    if (row.shipmentDetail) {
        return row.shipmentDetail.sztp;
    }
    return '';
}

function formatServiceType(value, row) {
    switch (row.shipment.serviceType) {
        case 1:
            return 'Bốc Hàng';
        case 2:
            return 'Hạ Rỗng';
        case 3:
            return 'Bốc Rỗng';
        case 4:
            return 'Hạ Hàng';
    }
}

function formatVslNm(value, row) {
    return row.shipmentDetail.vslNm;
}

function formatVoyNo(value, row) {
    return row.shipmentDetail.voyNo;
}

function formatDate(value) {
    if (value != null && value != '') {
        return value.substring(8, 10)+'/'+value.substring(5, 7)+'/'+value.substring(0, 4)+value.substring(10, 19);
    }
    return value;
}
function formatRegisterTime(value, row){
    return row.shipmentDetail.createTime;
}

function changeServiceType() {
    pickupHistory.serviceType = $('#seviceTypeSelect').val();
    loadTable();
}

function changeFromDate() {
    let fromDate = stringToDate($('.from-date').val());
    if ($('.to-date').val() != '' && stringToDate($('.to-date').val()).getTime() < fromDate.getTime()) {
        $.modal.alertError('Quý khách không thể chọn từ ngày cao hơn đến ngày.')
        $('.from-date').val('');
    } else {
        pickupHistory.fromDate = fromDate.getTime();
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
        pickupHistory.toDate = toDate.getTime();
        loadTable();
    }
}

function stringToDate(dateStr) {
    let dateParts = dateStr.split('/');
    return new Date(dateParts[2], dateParts[1] - 1, dateParts[0]);
}