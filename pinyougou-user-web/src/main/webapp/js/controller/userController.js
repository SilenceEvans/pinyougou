 //控制层 
app.controller('userController' ,function($scope,$controller ,userService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		userService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		userService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};
	
	//查询实体 
	$scope.findOne=function(id){				
		userService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	};
	
	/*
		进行注册的方法
	 */
	$scope.add=function(){
		//调用userService中的add方法
		userService.add($scope.entity,$scope.code).success(function (response) {
			if (response.success()){
				alert(response.message);
			} else {
				alert(response.message);
			}
		})
	};
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		userService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		userService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

	/*
		1.判断两次输入的密码是否一致,输入一致提交信息，输入不一致打断方法
	 */
    $scope.reg=function () {
		if ($scope.entity.password != $scope.password) {
			alert("两次输入的密码不一致！")
			return;
		}else {
			userService.add($scope.entity,$scope.code).success(function (response) {
				if (response.success){
					alert(response.message);
				} else {
					alert(response.message);
				}
			})
		}
	};
	/*
		2.定义点击发送短信验证码，验证码进行发送的方法
	 */
	$scope.sendSmsCode=function () {
		//先进行前台正则校验，校验失败，直接打断方法弹出错误信息
		var reg_telephone =
			new RegExp("^(13[0|9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$");
		if ($scope.entity.phone == null || $scope.entity.phone == "") {
			alert("请输入手机号！")
			return;
		}else if (!reg_telephone.test($scope.entity.phone)){
			alert("手机号码不合法！");
			return;
		} else{
			//调用userService中发送验证码的方法，将验证码进行发送
			userService.sendSmsCode($scope.entity.phone).success(function (response) {
				alert(response.message);
			})
		}
	}
});	
