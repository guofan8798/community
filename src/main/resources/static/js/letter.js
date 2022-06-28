$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();

	//异步地发一个post请求
	$.post(
		//参数1：访问路径
		CONTEXT_PATH + "/letter/send",
		//参数2：声明我们要传的数据的参数
		{"toName":toName,"content":content},
		//参数3：处理服务端返回的结果
		function (data) {
			data = $.parseJSON(data);
			if (data.code==0){
                $("#hintBody").text("发送成功！");
            }else {
                $("#hintBody").text(data.msg);
			}
            $("#hintModal").modal("show");
            setTimeout(function(){
                $("#hintModal").modal("hide");
                //重载当前页面
                location.reload();
            }, 2000);
        }
	);


}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}