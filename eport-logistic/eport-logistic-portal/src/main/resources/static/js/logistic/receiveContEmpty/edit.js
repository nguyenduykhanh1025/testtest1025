var prefix = ctx + "logistic/receive-cont-empty";
var currentBooking = shipment.bookingNo;

function loadGroupName() {
    if ($("#taxCode").val() != null && $("#taxCode").val() != '') {
        $.ajax({
            url: "/logistic/company/" + $("#taxCode").val(),
            method: "GET",
        }).done(function (result) {
            if (result.code == 0) {
                $("#groupName").val(result.groupName);
                $("#taxCode").removeClass("error-input");
            } else {
                $.modal.msgError("Không tìm ra mã số thuế!");
                $("#taxCode").addClass("error-input");
                $("#groupName").val('');
            }
        });
    } else {
        $("#groupName").val('');
    }
}

// Map data to form
if (shipment != null) {
    $("#id").val(shipment.id);
    $("#shipmentCode").val(shipment.id);
    $("#bookingNo").val(shipment.bookingNo);
    $("#specificContFlg").val(shipment.specificContFlg);
    $("#taxCode").val(shipment.taxCode);
    $("input[name='specificContFlg'][value='"+shipment.specificContFlg+"']").prop('checked', true);
    $("#containerAmount").val(shipment.containerAmount);
    $("#groupName").val(shipment.groupName);
    $("#remark").val(shipment.remark);
}

$("#form-edit-shipment").validate({
    focusCleanup: true
});

function submitHandler() {
    if ($.validate.form()) {
        if ($("#groupName").val() != null && $("#groupName").val() != '') {
            if ($("#bookingNo").val() != currentBooking) {
                $.ajax({
                    url: prefix + "/unique/booking-no/" + $("#bookingNo").val(),
                    method: "GET",
                }).done(function (result) {
                    if (result.code == 0) {
                        $("#bookingNo").removeClass("error-input");
                        $.operate.save(prefix + "/shipment/" + shipment.id, $('#form-edit-shipment').serialize());
                        parent.loadTable();
                    } else {
                        $.modal.msgError("Số book đã tồn tại!");
                        $("#bookingNo").addClass("error-input");
                    }
                });
            } else {
                $.operate.save(prefix + "/shipment/" + shipment.id, $('#form-edit-shipment').serialize());
                parent.loadTable();
            }
        } else {
            $.modal.msgError("Không tìm thấy mã số thuế!");
        }
    }
}

function checkBookingNoUnique() {
    if ($("#bookingNo").val() != null && $("#bookingNo").val() != '' && $("#bookingNo").val() != currentBooking) {
        $.ajax({
            url: prefix + "/unique/booking-no/" + $("#bookingNo").val(),
            method: "GET",
        }).done(function (result) {
            if (result.code == 0) {
                $("#bookingNo").removeClass("error-input");
            } else {
                $.modal.msgError("Số book đã tồn tại!");
                $("#bookingNo").addClass("error-input");
            }
        });
    }
}