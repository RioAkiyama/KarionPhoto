
function initAjax(){
    if($("meta[name='_csrf']").length == 0) return;
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function(e, xhr, options) {
		xhr.setRequestHeader(header, token);
	});
}

function initDataTable(){
    $.extend($.fn.dataTable.defaults, { language: {
            "sProcessing":   "処理中...",
            "sLengthMenu":   "_MENU_ 件表示",
            "sEmptyTable":  "データなし",
            "sInfo":         " _TOTAL_ 件中 _START_ ～ _END_ 件表示中",
            "sInfoEmpty":    " 0 件中 0 ～ 0 件表示中",
            "sInfoFiltered": "（全 _MAX_ 件より抽出）",
            "sInfoPostFix":  "",
            "sSearch":       "検索:",
            "sUrl":          "",
            "oPaginate": {
                "sFirst":    "先頭",
                "sPrevious": "前",
                "sNext":     "次",
                "sLast":     "最終"
            }
        }
    });
}

function isDevice(){
	return (window.matchMedia && window.matchMedia('(max-device-width:640px)').matches);
}

function showProgCircle() {
	if ($('#loading').length == 0) {
		$('body').append('<div id="loading"><div class="animation"></div></div>');
	}
}

function removeProgCircle(){
	$('#loading').remove();
}

function getUrlParam(){
	var query = new Array();
	var param = location.search;
	if(param){
	    var queries = param.substr(1, param.length);
	    var keyVals = queries.split('&');
	    for(var i=0; i<keyVals.length; i++){
	        var data = keyVals[i].split('=');
	        if(data.length != 2) continue;
	        query[sanitize(data[0])] = sanitize(data[1]);
	    }
	}
	return query;
}

function isFunction(functionToCheck) {
    return functionToCheck && {}.toString.call(functionToCheck) === '[object Function]';
}

function showOkDialog(title, message, callback){
	var okDialog = $( ".ok-dialog" );
	okDialog.prop('title', title);
	$('#ok-dialog-message').text(message);

	okDialog.dialog({
		modal:true, width:$(window).width()*0.3, height:200, closeText:''
	});

	$('#ok-dialog-button').click(function () {
		okDialog.dialog("close");
		if(isFunction(callback)) callback();
	});
}

function showYesNoDialog(title, message, yes_callback, no_callback){
    var yesNoDialog = $( ".yes-no-dialog" );
    yesNoDialog.prop('title', title);
    $('#yes-no-dialog-message').text(message);

    yesNoDialog.dialog({
        modal:true, width:$(window).width()*0.3, height:250, closeText:''
    });

    $('#dialog-button-yes').click(function () {
        yesNoDialog.dialog("close");
        if(isFunction(yes_callback)) yes_callback();
    });
    $('#dialog-button-no').click(function () {
        yesNoDialog.dialog("close");
        if(isFunction(no_callback)) no_callback();
    });
}


function getPostalCodeStr(number){
    if(!number || number.length != 7) return number;
    return number.substr(0, 3) + "-" + number.substr(3, 4);
}

function getPhoneStr(number){
    if(!number){
        return number;
    }else if(number.length == 10){
        return number.substr(0, 2) + "-" + number.substr(2, 4) + "-" + number.substr(6, 4);
    }else if(number.length == 11){
        return number.substr(0, 3) + "-" + number.substr(3, 4) + "-" + number.substr(7, 4);
    }
    return number;
}

function post(url, data, success_callback, fail_callback){
	showProgCircle();

	$.ajax({
        url: url,
        type: 'post',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify(data),
		timeout: 30000
    })
    .done(function (response) {
    	if(isFunction(success_callback)) success_callback(response);
    	removeProgCircle();
    })
    .fail(function (XMLHttpRequest, textStatus, errorThrown) {
    	if(isFunction(fail_callback)) fail_callback();
        removeProgCircle();
    });
}

(function ($) {
    var escapes = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        },
        escapeRegexp = /[&<>"']/g,
        hasEscapeRegexp = new RegExp(escapeRegexp.source),
        unescapes = {
            '&amp;': '&',
            '&lt;': '<',
            '&gt;': '>',
            '&quot;': '"',
            '&#39;': "'"
        },
        unescapeRegexp = /&(?:amp|lt|gt|quot|#39);/g,
        hasUnescapeRegexp = new RegExp(unescapeRegexp.source),
        stripRegExp = /<(?:.|\n)*?>/mg,
        hasStripRegexp = new RegExp(stripRegExp.source),
        nl2brRegexp = /([^>\r\n]?)(\r\n|\n\r|\r|\n)/g,
        hasNl2brRegexp = new RegExp(nl2brRegexp.source),
        br2nlRegexp = /<br\s*\/?>/mg,
        hasBr2nlRegexp = new RegExp(br2nlRegexp.source);

    $.fn.textWithLF = function (text) {
        var type = typeof text;

        return (type == 'undefined')
            ? htmlToText(this.html())
            : this.html((type == 'function')
                ? function (index, oldHtml) {
                    var result = text.call(this, index, htmlToText(oldHtml));
                    return (typeof result == 'undefined')
                        ? result
                        : textToHtml(result);
                } : textToHtml(text));
    };

    function textToHtml(text) {
        return nl2br(escape(toString(text)));
    }

    function htmlToText(html) {
        return unescape(strip(br2nl(html)));
    }

    function escape(string) {
        return replace(string, escapeRegexp, hasEscapeRegexp, function (match) {
            return escapes[match];
        });
    }

    function unescape(string) {
        return replace(string, unescapeRegexp, hasUnescapeRegexp, function (match) {
            return unescapes[match];
        });
    }

    function strip(html) {
        return replace(html, stripRegExp, hasStripRegexp, '');
    }

    function nl2br(string) {
        return replace(string, nl2brRegexp, hasNl2brRegexp, '$1<br>');
    }

    function br2nl(string) {
        return replace(string, br2nlRegexp, hasBr2nlRegexp, '\n');
    }

    function replace(string, regexp, hasRegexp, replacement) {
        return (string && hasRegexp.test(string))
            ? string.replace(regexp, replacement)
            : string;
    }

    function toString(value) {
        if (value == null) return '';
        if (typeof value == 'string') return value;
        if (Array.isArray(value)) return value.map(toString) + '';
        var result = value + '';
        return '0' == result && 1 / value == -(1 / 0) ? '-0' : result;
    }
})(jQuery);

function sanitize(html) {
	if(!html) return '';

	var tagBody = '(?:[^"\'>]|"[^"]*"|\'[^\']*\')*';
	var tagOrComment = new RegExp(
	    '<(?:'
	    // Comment body.
	    + '!--(?:(?:-*[^->])*--+|-?)'
	    // Special "raw text" elements whose content should be elided.
	    + '|script\\b' + tagBody + '>[\\s\\S]*?</script\\s*'
	    + '|style\\b' + tagBody + '>[\\s\\S]*?</style\\s*'
	    // Regular name
	    + '|/?[a-z]'
	    + tagBody
	    + ')>',
	    'gi');

	var oldHtml;
	do {
		oldHtml = html;
		html = html.replace(tagOrComment, '');
	} while (html !== oldHtml);

	//return html.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
	return html.replace(/&/g, '').replace(/</g, '').replace(/>/g, '').replace(/"/g, '').replace(/'/g, '');
}

var menuListOpenMSec;
$(function () {
    $('.header-menu li').click(function () {
    	$("ul.dropdwn-menu", this).slideUp();
        $("ul:not(:animated)", this).slideDown();
        menuListOpenMSec = new Date().getTime();
    });
	$('.dropdwn-menu').hover(function(){ }, function(){
        if(menuListOpenMSec && new Date().getTime() - menuListOpenMSec < 1000) return;
        $("ul.dropdwn-menu", this.parentNode).slideUp();
    });
});