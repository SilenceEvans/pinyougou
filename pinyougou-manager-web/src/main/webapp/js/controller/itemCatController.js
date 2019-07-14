 //控制层 
app.controller('itemCatController' ,function($scope,$controller,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};
	
	//查询实体 
	$scope.findOne=function(id){
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;
				typeTemplateService.selectOptionTemplateName($scope.entity.typeId).success(function (response) {
					$scope.template = response;
				});
			}
		);				
	};

	//定义一个parentId
	$scope.parentId=0;
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			$scope.entity.parentId=$scope.parentId;
			$scope.entity.typeId=$scope.template.id;
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			//增加时为entity添加entity.parentId
			$scope.entity.parentId=$scope.parentId;
			$scope.entity.typeId=$scope.template.id;
			serviceObject=itemCatService.add( $scope.entity );//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.list=$scope.findByParentId($scope.parentId);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	};
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.findByParentId($scope.parentId);//刷新列表
					alert(response.message);
					$scope.selectIds=[];
				}else {
					$scope.haveChild=response.haveChild;
					if ($scope.haveChild != null) {
						$scope.findByParentId($scope.parentId);
						$scope.selectIds=[];
						alert("id为"+response.haveChild+"的类"+response.message);
					}else {
						$scope.findByParentId($scope.parentId);
						$scope.selectIds=[];
						alert(response.message)
					}
				}
			}		
		);				
	};
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

	//查询下一级
	$scope.findByParentId=function (parentId) {

		itemCatService.findByParentId(parentId).success(function (response) {

			$scope.list = response;
		})

	};


	//类型模板下拉列表
	$scope.specNameList={data:[]};
	$scope.brandTemplateList=function(){
		typeTemplateService.selectOptionTemplateList().success(function (response) {
			$scope.specNameList={data:response}
		})
	};

	//定义一个变量template
	$scope.template={};

	//定义与面包屑级数相关的变量grade
	$scope.grade = 1;

	//定义设置面包屑的方法setGrade
	$scope.setGrade = function (value) {

		$scope.grade = value;

	};

	//定义点击下一级触发的方法
	$scope.findNextGradeList=function (entity) {

		//调用findByParentId方法
		$scope.findByParentId(entity.id);

		if ($scope.grade == 1) {
			//设置一级面包屑下的两个面包屑均为空值
			$scope.entity_1=null;
			$scope.entity_2=null;

		}else if ($scope.grade == 2) {

			$scope.entity_1=entity;
			$scope.entity_2=null;

		}else if ($scope.grade == 3) {

			$scope.entity_2 = entity;

		}

		//若此方法被调用，则parentId值被改变
		$scope.parentId = entity.id;

	}
    
});	
