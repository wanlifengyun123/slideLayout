$(function (){
	var dragArea=$("#dragarea");
	var locahost=location.host;
	var uploadAllfile=true;
	var typeAndriod="txt,pdf,epub,chm,umd,rar,zip,ndl,ndb,ndz,cbr,cbz,ttf";
	var typeIos="txt,pdf,epub,chm,umd,rar,zip,ndl,ndb,ndz,cbr,cbz,doc,ppt,xls,ttf";
	var allowType=$("body").attr("filetype")=="ios"?typeIos:typeAndriod;
	function loadDragsize(){
		var windowH=$(window).height();
		dragArea.css("min-height",windowH-130);
		if(dragArea.find("ul").length==1){
			dragArea.append('<ul style="overflow:auto;height:'+(windowH-180)+'px;" class="draglist"></ul>');
		}else{
			dragArea.find("ul").eq(1).height(windowH-180);
		}

	}
	loadDragsize();
	var dialogState=true;
	function dialogMsg(msg){
		$("#dialog").remove();
		msg=msg || "出现未知错误啦！";
		dialogState=false;
		var winH=$(window).height();
		var html='<div id="dialog" style="position:fixed;left:0;top:0;width:100%;height:'+winH+'px;background-color:rgba(0,0,0,0.5);z-index:9;"><div style="position:absolute;left:50%;top:50%;margin-left:-125px;background-color:#fff;width:250px;padding:25px 25px;text-align:center;" class="dialog-con"><div class="dialog-close" style="position:absolute;top:5px;right:5px;font-size:14px;color:#000;cursor:pointer;">╳</div><div style="line-height:25px;color:#000;font-size:14px;">'+msg+'</div></div></div>';
		$("body").append(html);
		$("#dialog").click(function (){
			$(this).remove();
			dialogState=true;
		}).find(".dialog-con").click(function (){
			return false;
		}).find(".dialog-close").click(function (){
			$("#dialog").remove();
			dialogState=true;
		});
	}
	window.onresize=loadDragsize;
	if(!("FormData" in window && "ondrop" in document.body)){//不支持拖拽
		$(".side").find(".arrow").empty().html("上传的文件将在右侧显示");
	}else{
		/*拖拽上传*/
		function checkFile(file) {
			if(!file.name) return "文件名的格式类型不支持导入";
			var arry=file.name.toLowerCase().split(".");
			if(arry.length==1) return file.name+"的格式类型不支持导入";
			var fileType=arry[arry.length-1];
			if (allowType.indexOf(fileType)==-1) {
				return file.name+"的格式类型不支持导入";
			}
			var len = uploader.files.length;
			for(var i=0; i< len; i++){
				if(uploader.files[i].name == file.name)	{
					return file.name+"已经存在";
				}
			}
			return null;
		}
		function addEvent(){
			var dropArea=document.getElementById("dragarea");
			dropArea.addEventListener('dragover', handleDragOver, false);
			dropArea.addEventListener('dragleave', handleDragLeave, false);
			dropArea.addEventListener('drop', handleDrop, false);
		}
		addEvent();
		function handleDrop(evt){
			evt.stopPropagation();
			evt.preventDefault();

			var file={};
			var errorMsgs = [];
			var len = evt.dataTransfer.files.length;
			var fileNumber=0;
			for(var i=0; i < len; i++){
				file = evt.dataTransfer.files[i];
				//检测文件
				var msg = checkFile(file);
				//文件可以通过
				if(!msg){
					uploader.addFile(file);
					uploader.refresh();
					uploader.start();
				}else{
					errorMsgs.push(msg);
				}
			}
			if(errorMsgs.length>0){
				if(errorMsgs.length==1){
					dialogMsg(errorMsgs[0]);
				}else{
					var message=errorMsgs[0].indexOf("已经存在")!=-1?errorMsgs[0].split("已经存在")[0]+"等"+errorMsgs.length+"个文件":errorMsgs[0].split("的格式类型")[0]+"等"+errorMsgs.length+"个文件";
					message+='格式不正确或文件上传重复。</br>请选择'+allowType.split(",").join("、")+'格式的图书文件，文件名不能重复。';
					dialogMsg("<div style='text-align:left;'>"+message+"</div>");

				}
			}
		}
		function handleDragOver(evt){
			evt.stopPropagation();
			evt.preventDefault();
		}
		function handleDragLeave(evt){
			evt.stopPropagation();
			evt.preventDefault();
		}
		/*拖拽结束*/
	}

	var uploader = new plupload.Uploader({
		browse_button:"plupload",
		//drop_element:"dragarea",
		url:"http://"+locahost+"/",/*上传地址*/
		filters:{
			mime_types:[
				{title:"files",extensions:allowType}
			],
			//max_file_size:"10240kb",/*最多上传10M*/
			prevent_duplicates:true//不允许命名重复
			},
		flash_swf_url:"/tmp/js/Moxie.swf",/*swf上传时需要读取，用绝对路径*/
		silverlight_xap_url:"/tmp/js/Moxie.xap"/*silverlight上传，绝对路径*/
	});
	uploader.init();
	uploader.start();
	/*即将上传选中的文件*/
	uploader.bind("BeforeUpload",function (uploader,file){
		var nameStr=file.name;
		var random=Math.random();
		if(nameStr.substring(nameStr.length-4)==".ttf"){/*上传字体*/
			uploader.setOption("url","http://"+locahost+"/%e5%ad%97%e4%bd%93/?"+random)
		}else{
			uploader.setOption("url","http://"+locahost+"/?"+random);
		}
	});
	/*选择文件上传*/
	uploader.bind('FilesAdded',function(uploader,files){
		uploadAllfile=false;
		var len=files.length;
		var again=true;
		for(var i=0;i<len;i++){
			var file=files[i];
			var appendchild='';
			var fileName=file.name;
			var fileId=file.id;
			var fileSize=(file.size/1024).toFixed(2);
			appendchild='<li id="'+fileId+'"><span class="file-name">'+fileName+'</span><span class="file-size">'+fileSize+'K</span><span class="file-state">等待上传</span></li>';
			if($("#"+fileId).length==0) {
				dragArea.find("ul").eq(1).append(appendchild);
				dragArea.delegate(".delete","click",function (){
					var myfileId=$(this).parents("li").attr("id");
					var myfile= uploader.getFile(myfileId);
					uploader.removeFile(myfile);
					$(this).parents("li").remove();
				}).delegate(".reload","click",function (){
					var myfileId=$(this).parents("li").attr("id");
					var myfile= uploader.getFile(myfileId);
					if(myfile.status!=1){
						myfile.status=1;
					}else{
						myfile.status=0;
					}
					uploader.start();
				});
			}
		}
		uploader.start();//每一个文件进行上传;
	});

	/*上传进度*/
	uploader.bind("UploadProgress",function (uploader,file){
		var thisdom=$('#'+file.id+'');//当前上传的文件
		thisdom.removeClass("fail");
		var percent=file.percent;
		percent=percent>1?percent-1:0;
		var progressHtml='<em class="percent">'+percent+'%</em><em class="delete icon" title="取消上传"></em>';
		if(thisdom.find(".delete").length==0){
			thisdom.find(".file-state").empty().html(progressHtml);
		}else{
			thisdom.find(".percent").text(percent+"%");
		}
	});
	/*所有文件上传完成不管成功还是失败*/
	uploader.bind("UploadComplete",function (uploader,files){
		uploadAllfile=true;
	});
	/*获取服务器端信息*/
	uploader.bind("FileUploaded",function (uploader,file,responseObject){
		var thisdom=$('#'+file.id+'');
		if(responseObject.status==200 || responseObject.status==0){/*服务器上传成功*/
			thisdom.addClass("succ").find(".file-state").empty().html("上传成功");
		}

	});
	/*上传出错*/
	uploader.bind("Error",function (uploader,errObject){
		var errorCode=errObject.code;
		var errorDom=$("#"+errObject.file.id);
		var errorHtml='失败<em class="reload icon" title="重新上传"></em>';
		errorDom.addClass("fail").find(".file-state").html(errorHtml);
		//uploader.removeFile(errObject.file);/*因为要重新上传，不再进行失败移除*/
		if(dialogState) dialogMsg(errorTips(errObject.code));
	});


	function errorTips(code){
		var errorMsg="通用错误";
		switch(code){
			case -100:
				errorMsg="通用错误";
				break;
			case -200:
				errorMsg="<div style='text-align:left;'>网络异常，请确认手机和电脑连接同一个WiFi，并且在传书过程中不要离开WiFi传书页或锁屏</div>";
				break;
			case -300:
				errorMsg="磁盘读写错误";
				break;
			case -400:
				errorMsg="因为安全问题而产生的错误";
				break;
			case -500:
				errorMsg="初始化时发生错误";
				break;
			case -600:
				errorMsg="选择的文件太大";
				break;
			case -601:
				errorMsg="选择的文件类型不符合要求";
				break;
			case -602:
				errorMsg="选取了重复的文件";
				break;
			case -700:
				errorMsg="格式错误";
				break;
			case -701:
				errorMsg="内存错误";
				break;
			case -702:
				errorMsg="文件大小超过了处理上限";
				break;
			default:
				errorMsg="发生未知错误啦！";
				break;
		}
		return errorMsg;
	}
	window.onbeforeunload = function (){
		if(!uploadAllfile) return "您还有没有上传完成的文件，确定要离开吗？";
	}
});