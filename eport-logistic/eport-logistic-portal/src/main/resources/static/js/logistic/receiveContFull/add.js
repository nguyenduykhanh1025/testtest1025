var prefix = ctx + "logistic/receive-cont-full";

$("#form-add-shipment").validate({
    focusCleanup: true
});

$('#taxCode').val(taxCode).prop('readonly', true);
loadGroupName();

$('input:radio[name="taxCodeDefault"]').change(function() {
    if ($(this).val() == '1') {
        $('#taxCode').val(taxCode).prop('readonly', true);
        loadGroupName();
    } else {
        $('#taxCode').val('').prop('readonly', false);
        $("#taxCode").removeClass("error-input");
    }
});

async function submitHandler() {
    if ($.validate.form()) {
        if ($("#groupName").val() != null && $("#groupName").val() != '') {
            let res = await getBillNoUnique();
            if (res.code == 500) {
                $.modal.msgError(result.msg);
                $("#blNo").addClass("error-input");
                $('#opeCode').val("");
                $('#containerAmount').val("");
                $('#edoFlg').val(null).text("");
            } else {
                $("#blNo").removeClass("error-input");
                $('#opeCode').val(result.shipment.opeCode);
                $('#containerAmount').val(result.shipment.containerAmount);
                if (result.shipment.edoFlg == "1") {
                    $('#edoFlg').val(result.shipment.edoFlg).text("Lệnh giao hàng điện tử (eDO)");
                    $('#edoFlgInput').val(result.shipment.edoFlg);
                } else {
                    $('#edoFlg').val(result.shipment.edoFlg).text("Lệnh giao hàng (DO)");
                    $('#edoFlgInput').val(result.shipment.edoFlg);
                }
                await $.operate.save(prefix + "/shipment", $('#form-add-shipment').serialize());
                parent.loadTable();
            }
        } else {
            $.modal.alertError("Không tìm ra mã số thuế!<br>Quý khách vui lòng liên hệ đến bộ phận chăm sóc khách hàng 0933.157.159.");
        }
    }
}

function getBillNoUnique() {
    return $.ajax({
        url: prefix + "/shipment/bl-no/" + $("#blNo").val(),
        method: "GET",
    });
}

function checkBlNoUnique() {
    if ($("#blNo").val() != null && $("#blNo").val() != '') {
        //check bill unique, opeCode,edoFlag, containerAmount trong db edo, catos
        $.ajax({
            url: prefix + "/shipment/bl-no/" + $("#blNo").val(),
            method: "GET",
        }).done(function (result) {
            if (result.code == 500) {
                $.modal.msgError(result.msg);
                $("#blNo").addClass("error-input");
                $('#opeCode').val("");
                $('#containerAmount').val("");
                $('#edoFlg').val(null).text("");
            } else {
            	$("#blNo").removeClass("error-input");
                $('#opeCode').val(result.shipment.opeCode);
                $('#containerAmount').val(result.shipment.containerAmount);
                if (result.shipment.edoFlg == "1") {
                    $('#edoFlg').val(result.shipment.edoFlg).text("Lệnh giao hàng điện tử (eDO)");
                    $('#edoFlgInput').val(result.shipment.edoFlg);
                } else {
                    $('#edoFlg').val(result.shipment.edoFlg).text("Lệnh giao hàng (DO)");
                    $('#edoFlgInput').val(result.shipment.edoFlg);
                }
            }
        });
    }
}

function loadGroupName() {
    if ($("#taxCode").val() != null && $("#taxCode").val() != '') {
        $.ajax({
            url: "/logistic/company/" + $("#taxCode").val(),
            method: "get"
        }).done(function (result) {
            if (result.code == 0) {
                $("#groupName").val(result.groupName);
                $("#taxCode").removeClass("error-input");
            } else {
                $.modal.alertError("Không tìm ra mã số thuế!<br>Quý khách vui lòng liên hệ đến bộ phận chăm sóc khách hàng 0933.157.159.");
                $("#taxCode").addClass("error-input");
                $("#groupName").val('');
            }
        });
    } else {
        $("#groupName").val('');
    }
}