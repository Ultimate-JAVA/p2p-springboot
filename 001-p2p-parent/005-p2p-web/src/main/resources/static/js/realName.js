
//同意实名认证协议
$(function() {
	$("#agree").click(function(){
		var ischeck = document.getElementById("agree").checked;
		if (ischeck) {
			$("#btnRegist").attr("disabled", false);
			$("#btnRegist").removeClass("fail");
		} else {
			$("#btnRegist").attr("disabled","disabled");
			$("#btnRegist").addClass("fail");
		}
	});
});
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
	//实名认证按钮
	$("#btnRegist").on('click',function () {
		$("#phone").blur()
		$("#realName").blur()
		$("#idCard").blur()
		$("#messageCode").blur()
		var textErr = $("div[id$='Err']").text()
		if (textErr==""){
			var phone = $.trim($("#phone").val());
			var realName = $.trim($("#realName").val());
			var idCard = $.trim($("#idCard").val());
			var messageCode = $.trim($("#messageCode").val());
			$.ajax({
				url:"/p2p/loan/realName",
				type:"get",
				data:{
					phone:phone,
					realName:realName,
					idCard:idCard,
					messageCode:messageCode
				},
				dataType:'json',
				success:function (data) {
					if (data.code==1){
					//	认证成功
						window.location.href = "/p2p/"
					}else if (data.code==-1){
						showError('idCard',data.message)
					}
				},
				error:function () {
					showError('idCard',data.message)
				}
			})
		}
	})

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

	//手机号
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

			/*$.ajax({
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
			})*/
		}
	});
	$("#phone").on('focus', function () {
		hideError('phone')
	});

	//姓名
	$("#realName").on('blur',function() {
		var realName = $.trim($("#realName").val())
		if (realName == "") {
			showError('realName',"请输入您的真实姓名")
		}else if (!/^[\u4e00-\u9fa5]{0,}$/.test(realName)){
			showError('realName','请输入中文姓名')
		}else {
			showSuccess('realName')
		}
	})
	$("#realName").on('focus',function () {
		hideError("realName")
	})

	//身份证号码
	$("#idCard").on('blur',function() {
		var idCard = $.trim($("#idCard").val())
		if (idCard == "") {
			showError('idCard',"请输入身份证号码")
		}else if (!/(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard)){
			showError('idCard','请输入正确的身份证号码')
		}else {
			showSuccess('idCard')
		}
	})
	$("#idCard").on('focus',function () {
		hideError("idCard")
	})

})