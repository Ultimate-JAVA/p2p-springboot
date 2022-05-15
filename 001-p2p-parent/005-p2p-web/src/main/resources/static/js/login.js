var referrer = "";//登录后返回页面
referrer = document.referrer;
if (!referrer) {
	try {
		if (window.opener) {                
			// IE下如果跨域则抛出权限异常，Safari和Chrome下window.opener.location没有任何属性              
			referrer = window.opener.location.href;
		}  
	} catch (e) {
	}
}

//按键盘Enter键即可登录
$(document).keyup(function(event){
	if(event.keyCode == 13){
		$('#loginBtn').click()
	}
});

//错误提示
function showError(id,msg) {
	$("#"+id+"Ok").hide();
	$("#"+id+"Err").html("<i></i><p>"+msg+"</p>");
	$("#"+id+"Err").show();
	$("#"+id).addClass("input-red");
}
//错误隐藏
function hideError(id) {
	$("#"+id+"Err").hide();
	$("#"+id+"Err").html("");
	$("#"+id).removeClass("input-red");
}
//显示成功
function showSuccess(id) {
	$("#"+id+"Err").hide();
	$("#"+id+"Err").html("");
	$("#"+id+"Ok").show();
	$("#"+id).removeClass("input-red");
}

$(function () {
	function phoneText() {
		var phone = $.trim($('#phone').val())
		if (phone == "") {
			showError('phone', '请输入手机号');
		} else if (phone.length < 11) {
			showError('phone', '正确的手机号为11位');
		} else if (!/^1[1-9]\d{9}$/.test(phone)) {
			showError('phone', '请输入正确的手机号')
		}
	}

	$("#phone").on('focus', function () {
		hideError('phone')
	});

	function passwordText() {
		var loginPassword = $.trim($("#loginPassword").val())
		if (loginPassword == "") {
			showError('loginPassword', "请输入密码")
		}
	}

	$("#loginPassword").on('focus', function () {
		hideError('loginPassword')
	})

	$("#captcha").on('blur', function () {
		var captcha = $.trim($("#captcha").val())
		if (captcha == "") {
			showError('captcha',"请输入验证码")
		}
	})
	$("#captcha").on('focus', function () {
		hideError('captcha')
	})
//登录按钮
	$('#loginBtn').on('click', function () {
		passwordText()
		phoneText()
		var textErr = $("div[id$='Err']").text()
		if (textErr == "") {
			var phone = $.trim($('#phone').val())
			var loginPassword = $.trim($("#loginPassword").val())
			var messageCode = $.trim($("#captcha").val())
			$("#loginPassword").val($.md5(loginPassword))
			$.ajax({
				url: '/p2p/loan/login',
				data: {
					phone: phone,
					loginPassword: $.md5(loginPassword),
					messageCode:messageCode
				},
				dataType: 'json',
				type: 'get',
				success: function (data) {
					if (data.code == 1) {
						window.location.href = '/p2p/index'
					} else if (data.code == -1) {
						//登录失败
						showError('loginPassword', data.message)
						$("#loginPassword").val("")
						$("#captcha").val("")
						$("#imgCode").click()
					} else if (data.code == -2) {
						//验证码错误
						showError('captcha', data.message)
						$("#loginPassword").val("")
						$("#captcha").val("")
						$("#imgCode").click()
					}
				},
				error: function () {
					showError('loginPassword', '系统繁忙，请稍后...')
				}
			})
		}
	});

	//点击验证码刷新
	$("#imgCode").on("click", function () {
		$.ajax({
			url: "/p2p/jcaptcha/captcha",
			type: "get",
			success: function () {

				$("#imgCode").attr("src", "http://localhost:8081/p2p/jcaptcha/captcha")
			}
		})
	})
});

