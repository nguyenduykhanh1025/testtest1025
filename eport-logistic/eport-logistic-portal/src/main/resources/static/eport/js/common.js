$(function() {
	if (window.layer !== undefined) {
		layer.config({
		    extend: 'moon/style.css',
		    skin: 'layer-ext-moon'
		});
	}
	// Back to top binding
	if ($.fn.toTop !== undefined) {
		$('#scroll-up').toTop();
	}
	// select2 checkbox event binding
	if ($.fn.select2 !== undefined) {
        $.fn.select2.defaults.set( "theme", "bootstrap" );
		$("select.form-control:not(.noselect2)").each(function () {
			$(this).select2().on("change", function () {
				$(this).valid();
			})
		})
	}
	
	// iCheck radio button and checkbox event binding
	if ($.fn.iCheck !== undefined) {
		$(".check-box:not(.noicheck),.radio-box:not(.noicheck)").each(function() {
            $(this).iCheck({
                checkboxClass: 'icheckbox-blue',
                radioClass: 'iradio-blue',
            })
        })
	}
	 
	// laydate time control binding
	if ($(".select-time").length > 0) {
		layui.use('laydate', function() {
		    var laydate = layui.laydate;
		    var startDate = laydate.render({
		        elem: '#startTime',
		        max: $('#endTime').val(),
		        theme: 'molv',
		        trigger: 'click',
		        done: function(value, date) {
		            if (value !== '') {
		                endDate.config.min.year = date.year;
		                endDate.config.min.month = date.month - 1;
		                endDate.config.min.date = date.date;
		            } else {
		                endDate.config.min.year = '';
		                endDate.config.min.month = '';
		                endDate.config.min.date = '';
		            }
		        }
		    });
		    var endDate = laydate.render({
		        elem: '#endTime',
		        min: $('#startTime').val(),
		        theme: 'molv',
		        trigger: 'click',
		        done: function(value, date) {
		            // Start time is less than end time
		            if (value !== '') {
		                startDate.config.max.year = date.year;
		                startDate.config.max.month = date.month - 1;
		                startDate.config.max.date = date.date;
		            } else {
		                startDate.config.max.year = '2099';
		                startDate.config.max.month = '12';
		                startDate.config.max.date = '31';
		            }
		        }
		    });
		});
	}
	// laydate time-input time control binding
	if ($(".time-input").length > 0) {
		layui.use('laydate', function () {
			var com = layui.laydate;
			$(".time-input").each(function (index, item) {
				var time = $(item);
				// Control appearance
				var type = time.attr("data-type") || 'date';
				// Control echo format
				var format = time.attr("data-format") || 'dd/MM/yyyy';
				// Control date control buttons
				var buttons = time.attr("data-btn") || 'clear|now|confirm', newBtnArr = [];
				// Callback processing after the date control is selected
				var callback = time.attr("data-callback") || {};
				if (buttons) {
					if (buttons.indexOf("|") > 0) {
						var btnArr = buttons.split("|"), btnLen = btnArr.length;
						for (var j = 0; j < btnLen; j++) {
							if ("clear" === btnArr[j] || "now" === btnArr[j] || "confirm" === btnArr[j]) {
								newBtnArr.push(btnArr[j]);
							}
						}
					} else {
						if ("clear" === buttons || "now" === buttons || "confirm" === buttons) {
							newBtnArr.push(buttons);
						}
					}
				} else {
					newBtnArr = ['clear', 'now', 'confirm'];
				}
				com.render({
					elem: item,
					theme: 'molv',
					trigger: 'click',
					type: type,
					format: format,
					btns: newBtnArr,
					done: function (value, data) {
						if (typeof window[callback] != 'undefined'
							&& window[callback] instanceof Function) {
							window[callback](value, data);
						}
					}
				});
			});
		});
	}
	// tree Keyword search binding
	if ($("#keyword").length > 0) {
		$("#keyword").bind("focus", function focusKey(e) {
		    if ($("#keyword").hasClass("empty")) {
		        $("#keyword").removeClass("empty");
		    }
		}).bind("blur", function blurKey(e) {
		    if ($("#keyword").val() === "") {
		        $("#keyword").addClass("empty");
		    }
		    $.tree.searchNode(e);
		}).bind("input propertychange", $.tree.searchNode);
	}
	// treeTable tree Expand / collapse
	var expandFlag;
	$("#expandAllBtn").click(function() {
		var dataExpand = $.common.isEmpty(table.options.expandAll) ? true : table.options.expandAll;
		expandFlag = $.common.isEmpty(expandFlag) ? dataExpand : expandFlag;
	    if (!expandFlag) {
	    	$.bttTable.bootstrapTreeTable('expandAll');
	    } else {
	    	$.bttTable.bootstrapTreeTable('collapseAll');
	    }
	    expandFlag = expandFlag ? false: true;
	})
	// Press the ESC button to close the popup
	$('body', document).on('keyup', function(e) {
	    if (e.which === 27) {
	        $.modal.closeAll();
	    }
	});
});

(function ($) {
    'use strict';
    $.fn.toTop = function(opt) {
        var elem = this;
        var win = $(window);
        var doc = $('html, body');
        var options = $.extend({
            autohide: true,
            offset: 50,
            speed: 500,
            position: true,
            right: 15,
            bottom: 5
        }, opt);
        elem.css({
            'cursor': 'pointer'
        });
        if (options.autohide) {
            elem.css('display', 'none');
        }
        if (options.position) {
            elem.css({
                'position': 'fixed',
                'right': options.right,
                'bottom': options.bottom,
            });
        }
        elem.click(function() {
            doc.animate({
                scrollTop: 0
            }, options.speed);
        });
        win.scroll(function() {
            var scrolling = win.scrollTop();
            if (options.autohide) {
                if (scrolling > options.offset) {
                    elem.fadeIn(options.speed);
                } else elem.fadeOut(options.speed);
            }
        });
    };
})(jQuery);

/** Refresh tab */
var refreshItem = function(){
    var topWindow = $(window.parent.document);
	var currentId = $('.page-tabs-content', topWindow).find('.active').attr('data-id');
	var target = $('.eport_iframe[data-id="' + currentId + '"]', topWindow);
    var url = target.attr('src');
    target.attr('src', url).ready();
}

/** Close tab */
var closeItem = function(dataId){
	var topWindow = $(window.parent.document);
	if($.common.isNotEmpty(dataId)){
		window.parent.$.modal.closeLoading();
		$('.menuTab[data-id="' + dataId + '"]', topWindow).remove();
		$('.mainContent .eport_iframe[data-id="' + dataId + '"]', topWindow).remove();
		return;
	}
	var panelUrl = window.frameElement.getAttribute('data-panel');
	$('.page-tabs-content .active i', topWindow).click();
	if($.common.isNotEmpty(panelUrl)){
		$('.menuTab[data-id="' + panelUrl + '"]', topWindow).addClass('active').siblings('.menuTab').removeClass('active');
		$('.mainContent .eport_iframe', topWindow).each(function() {
            if ($(this).data('id') == panelUrl) {
                $(this).show().siblings('.eport_iframe').hide();
                return false;
            }
		});
	}
}

function createMenuItem(dataUrl, menuName) {
	var panelUrl = window.frameElement.getAttribute('data-id');
    dataIndex = $.common.random(1,100),
    flag = true;
    if (dataUrl == undefined || $.trim(dataUrl).length == 0) return false;
    var topWindow = $(window.parent.document);
    $('.menuTab', topWindow).each(function() {
        if ($(this).data('id') == dataUrl) {
            if (!$(this).hasClass('active')) {
                $(this).addClass('active').siblings('.menuTab').removeClass('active');
                $('.page-tabs-content').animate({ marginLeft: ""}, "fast");
                $('.mainContent .eport_iframe', topWindow).each(function() {
                    if ($(this).data('id') == dataUrl) {
                        $(this).show().siblings('.eport_iframe').hide();
                        return false;
                    }
                });
            }
            flag = false;
            return false;
        }
    });
    if (flag) {
        var str = '<a href="javascript:;" class="active menuTab" data-id="' + dataUrl + '" data-panel="' + panelUrl + '">' + menuName + ' <i class="fa fa-times-circle"></i></a>';
        $('.menuTab', topWindow).removeClass('active');
        var str1 = '<iframe class="eport_iframe" name="iframe' + dataIndex + '" width="100%" height="100%" src="' + dataUrl + '" frameborder="0" data-id="' + dataUrl + '" data-panel="' + panelUrl + '" seamless></iframe>';
        $('.mainContent', topWindow).find('iframe.eport_iframe').hide().parents('.mainContent').append(str1);
        
        window.parent.$.modal.loading(" Đang xử lý...");
        $('.mainContent iframe:visible', topWindow).load(function () {
        	window.parent.$.modal.closeLoading();
        });
        $('.menuTabs .page-tabs-content', topWindow).append(str);
    }
    return false;
}

var log = {
    log: function(msg) {
        console.log(msg);
    },
    info: function(msg) {
        console.info(msg);
    },
    warn: function(msg) {
        console.warn(msg);
    },
    error: function(msg) {
        console.error(msg);
    }
};

//Local cache processing
var storage = {
    set: function(key, value) {
        window.localStorage.setItem(key, value);
    },
    get: function(key) {
        return window.localStorage.getItem(key);
    },
    remove: function(key) {
        window.localStorage.removeItem(key);
    },
    clear: function() {
        window.localStorage.clear();
    }
};

/** Set global ajax processing */
$.ajaxSetup({
    complete: function(XMLHttpRequest, textStatus) {
        if (textStatus == 'timeout') {
        	$.modal.alertWarning("Hết thời gian chờ, hãy thử lại!");
        	$.modal.enable();
            $.modal.closeLoading();
        } else if (textStatus == "parsererror" || textStatus == "error") {
        	$.modal.alertWarning("Có lỗi khi gửi yêu cầu đến server, vui lòng thử lại.");
        	$.modal.enable();
            $.modal.closeLoading();
        }
    }
});
/**Automatic adjustment of page height*/
$(function() {
	domresize();
});
var heightInfo;
var widthInfo;
var initPageSize;
var initPageNum;
var webH;
var webW;
//Change table width and height
function domresize()
{
	webH = document.documentElement.clientHeight;
	webW = document.documentElement.offsetWidth;
	widthInfo = $("body").outerWidth() -27;
	//var mtopH = $(".box-body").outerHeight();
	//var mtopH = $("#wrapper").outerHeight();
	heightInfo = webH - 55;

	//Paging information changed to 15
	if(heightInfo > 550)
	{
		initPageSize = 15;
		initPageNum = [15,30,50,100];
	}
	else
	{
		initPageSize = 10;
		initPageNum = [10,20,30,50];
	}
}
function dgResize() {
	if($('#dg').length) {
        $('#dg').datagrid('resize', {
            height: $(window).height() -55
        });
	}
}
$(window).resize(function () {
    dgResize();
});

/**
 * Resize image when upload
 * 
 * @param files
 * @returns
 */
function summernoteOnImageUpload(files, editor) {
    $.each(files, function(idx, file) {
        var max_width = 1000;
        var max_height = 1000;
        var reader = new FileReader();
        reader.onload = function() {
            var tmpImg = new Image();
            tmpImg.src = reader.result;

            tmpImg.onload = function() {
                var tmpW = tmpImg.width;
                var tmpH = tmpImg.height;

                if (tmpW > tmpH) {
                    if (tmpW > max_width) {
                       tmpH *= max_width / tmpW;
                       tmpW = max_width;
                    }
                } else {
                    if (tmpH > max_height) {
                       tmpW *= max_height / tmpH;
                       tmpH = max_height;
                    }
                }

                var canvas = document.createElement('canvas');
                canvas.width = tmpW;
                canvas.height = tmpH;
                var ctx = canvas.getContext('2d');
                ctx.drawImage(this, 0, 0, tmpW, tmpH);
                sURL = canvas.toDataURL("image/jpeg");
                editor.summernote('insertImage', sURL, file.name);
            }
        }
        reader.readAsDataURL(file);
    });
}
$(document).ready(function () {
  if($('.summernote')[0]) {
	  $('.summernote').summernote({
	    minHeight: 100,
	    maximumImageFileSize: 150*1024, // maxsize in Kilobyte
	    toolbar: false,
	    placeholder: 'Hãy nhập nội dung cần hỗ trợ',
	    callbacks: {
		  onImageUpload: function(files) {
			  summernoteOnImageUpload(files, $('.summernote'));
		  },
		  onImageUploadError: function() {
		        $.modal.alertError('Hình quá lớn, chỉ cho phép hình dung lượng tối đa 150kb.');
		  }
	    }
	  });
  }
});