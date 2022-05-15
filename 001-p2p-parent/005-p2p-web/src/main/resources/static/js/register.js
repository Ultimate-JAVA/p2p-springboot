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


//打开注册协议弹层
function alertBox(maskid,bosid){
	$("#"+maskid).show();
	$("#"+bosid).show();
}
//关闭注册协议弹层
function closeBox(maskid,bosid){
	$("#"+maskid).hide();
	$("#"+bosid).hide();
}

//注册协议确认
$(function () {
	$("#agree").click(function () {
		var ischeck = document.getElementById("agree").checked;
		if (ischeck) {
			$("#btnRegist").attr("disabled", false);
			$("#btnRegist").removeClass("fail");
		} else {
			$("#btnRegist").attr("disabled", "disabled");
			$("#btnRegist").addClass("fail");
		}
	});
	$("#phone").on("blur", function () {

		var phone = $.trim($('#phone').val())
		if (phone==""){
			showError('phone', '请输入手机号');
		}else if (phone.length<11){
			showError('phone', '正确的手机号为11位');
		}else if (!/^1[1-9]\d{9}$/.test(phone)){
			showError('phone','请输入正确的手机号')
		}else {
			showSuccess('phone')

			$.ajax({
				url:'/p2p/loan/checkPhone',
				data:'phone='+phone,
				dataType: 'json',
				type:'get',
				success: function (data) {
					if (data.code==1){
						showSuccess('phone')
					}else {
						showError('phone',data.message)
					}
				},
				error:function (data) {
					showError('phone',data.message)
				}
			})
		}
	});
	$("#phone").on('focus', function () {
		hideError('phone')
	});

	$("#loginPassword").on('blur',function() {
		var loginPassword = $.trim($("#loginPassword").val())
		if (loginPassword == null) {
			showError('loginPassword',"请输入密码")
		}else if (loginPassword<6&&loginPassword>20){
			showError('loginPassword','请输入6-20位英文和数字混合密码')
		}else if (!/^[0-9a-zA-Z]+$/.test(loginPassword)){
			showError('loginPassword','密码字符只可使用数字和大小写英文字母')
		}else if (!/^(([a-zA-Z]+[0-9]+)|([0-9]+[a-zA-Z]+))[a-zA-Z0-9]*/.test(loginPassword)){
			showError('loginPassword','密码应同时包含英文和数字')
		}else {
			showSuccess('loginPassword')
		}
	})
	$("#loginPassword").on('focus',function () {
		hideError("loginPassword")
	})

	//注册按钮
	$('#btnRegist').on('click', function () {
		$("#phone").blur()
		$("#loginPassword").blur()
		$("#messageCode").blur()
		var textErr = $("div[id$='Err']").text()
		if (textErr==""){
			var phone = $.trim($('#phone').val())
			var loginPassword = $.trim($("#loginPassword").val())
			var messageCode = $.trim($("#messageCode").val())
			$("#loginPassword").val($.md5(loginPassword))
			$.ajax({
				url:'/p2p/loan/register',
				data:{
					phone:phone,
					loginPassword:$.md5(loginPassword),
					messageCode:messageCode
				},
				dataType: 'json',
				type:'get',
				success:function (data) {
					if (data.code==1){
						window.location.href = '/p2p/loan/page/realName'
					}else if (data.code==-1){
						//注册失败
						showError('loginPassword',data.message)
					}else if (data.code==-2) {
						//验证码错误
						showError('messageCode', data.message)

					}
				},
				error: function () {
					showError('loginPassword','系统繁忙，请稍后...')
				}
			})
		}
	});

	//验证码
	$("#messageCode").on("blur", function () {
		var messageCode = $("#messageCode").val();
		if (messageCode=="") {
			showError('messageCode', "请输入验证码")
		}
	});

	$("#messageCode").on("focus", function () {
		hideError("messageCode")
	});

	//获取验证码按钮
	$("#messageCodeBtn").on("click",function () {
		hideError('messageCode')
		$("#phone").blur()
		$("#loginPassword").blur()
		var textErr = $("div[id$='Err']").text()
		if (textErr==""){

			var phone = $.trim($("#phone").val());
			$.ajax({
				url:'/p2p/user/messageCode',
				data:{phone:phone},
				type:"get",
				dataType:"json",
				success:function (data) {
					if (data.code==1){
						if (!$("#messageCodeBtn").hasClass("on")) {
							$.leftTime(60, function (d) {
								//d.status,值true||false,倒计时是否结束;
								if (d.status) {
									//d.s,倒计时秒;
									$("#messageCodeBtn").text(d.s == '00' ? '60秒' : d.s + '秒')
									$("#messageCodeBtn").addClass("on")
								} else {
									$("#messageCodeBtn").removeClass("on");
									$("#messageCodeBtn").text('获取验证码')
								}
							});
						}
						//短信发送成功，返回验证码
						alert("验证码为："+data.messageCode)
					}else {
						showError('messageCodeBtn',data.message)
					}
				},
				error:function () {
					showError('messageCodeBtn','系统忙，请稍后...')
				}
			})

		}
	})

});
