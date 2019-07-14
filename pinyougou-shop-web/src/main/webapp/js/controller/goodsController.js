 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService) {

	$controller('baseController', {$scope: $scope});//继承

	//读取列表数据绑定到表单中
	$scope.findAll = function () {
		goodsService.findAll().success(
			function (response) {
				$scope.list = response;
			}
		);
	};

	//分页
	$scope.findPage = function (page, rows) {
		goodsService.findPage(page, rows).success(
			function (response) {
				$scope.list = response.rows;
				$scope.paginationConf.totalItems = response.total;//更新总记录数
			}
		);
	};

	//查询实体 
	$scope.findOne = function () {
		var id = $location.search()['id'];
		if (id == null){
			return;
		}
		goodsService.findOne(id).success(
			function (response) {
				$scope.entity = response;
				editor.html($scope.entity.goodsDesc.introduction);
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages)
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//将从goodsDesc表中获得的specficationItems也进行转换
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//将itemList进行转换
				for (var i = 0; i < $scope.entity.itemList.length; i++) {
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}
			})
	};

	//更新或者添加
	$scope.save=function(){
		//提取文本编辑器的值
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象
		if($scope.entity.goods.id!=null){//如果有 ID
			serviceObject=goodsService.update( $scope.entity ); //修改
		}else{
			serviceObject=goodsService.add( $scope.entity );//增加
		}
		serviceObject.success(
			function(response){
				if(response.success){
					alert('保存成功');
					//保存成功后跳转到列表页面
					location.href="goods.html";
				}else{
					alert(response.message);
				}
			}
		);
	};

	//保存
	$scope.entity = {};


	//批量删除 
	$scope.dele = function () {
		//获取选中的复选框			
		goodsService.dele($scope.selectIds).success(
			function (response) {
				if (response.success) {
					$scope.reloadList();//刷新列表
					$scope.selectIds = [];
				}
			}
		);
	};

	$scope.searchEntity = {};//定义搜索对象

	//搜索
	$scope.search = function (page, rows) {
		goodsService.search(page, rows, $scope.searchEntity).success(
			function (response) {
				$scope.list = response.total;
				$scope.paginationConf.totalItems = response.totalCount;//更新总记录数
			}
		);
	};
	//定义一个状态数组，用来对应的显示商品的状态
	$scope.status=["未审核","已审核","审核未通过","已关闭"];
	//上传图片
	$scope.image_entity = {};
	$scope.uploadFile = function () {

		uploadService.uploadFile().success(function (response) {

			if (response.success) {
				$scope.image_entity.url = response.message;
				alert("上传成功！");
			} else {
				alert(response.message);
			}

		}).error(function () {
			alert("上传发生错误！");
		})

	};

	//上传图片后保存
	$scope.entity = {goods: {}, goodsDesc: {itemImages: [],specificationItems:[]}};

	$scope.add_image_entity = function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	};

	//移除添加的图片
	$scope.remove = function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index, 1)
	};


	//定义商品信息里商品分类一级分类页面初始化显示的方法
	$scope.showItemCat = function () {
		itemCatService.findByParentId(0).success(function (response) {
			$scope.itemCatList1 = response;
		})
	}

	//显示第二级菜单的方法
	$scope.$watch("entity.goods.category1Id", function (newValue,oldValue) {
		itemCatService.findByParentId(newValue).success(function (response) {
			$scope.itemCatList2 = response;
		})
	})


	//显示第三级菜单的方法
	$scope.$watch("entity.goods.category2Id", function (newValue,oldValue) {
		itemCatService.findByParentId(newValue).success(function (response) {
			$scope.itemCatList3 = response;
		})
	});

	//显示模板id的方法
	$scope.$watch("entity.goods.category3Id",function (newValue, oldValue) {
		itemCatService.findOne(newValue).success(function (response) {
			$scope.entity.goods.templateId=response.typeId;
		})
	});

	//根据模板Id显示商品名称的方法
	$scope.$watch("entity.goods.templateId",function (newValue, oldValue) {
		typeTemplateService.findOne(newValue).success(function (response) {
			$scope.typeTemplateBrandIds = JSON.parse(response.brandIds);

			//根据模板id回写该模板需要填写的扩展属性的名字
			$scope.typeTemplate = response;
			$scope.typeTemplate.customAttributeItems = JSON.parse(response.customAttributeItems);
			if ($location.search()['id'] == null) {
				$scope.entity.goodsDesc.customAttributeItems = $scope.typeTemplate.customAttributeItems;
			}
		});

		typeTemplateService.findSpecList(newValue).success(function (response) {
			$scope.specList = response;
		})
	});

	//保存规格信息的方法
	//specificationItems集合需初始化，在第99行已完成初始化
	$scope.saveSpecificationItems =function ($event,name,value) {
		/*根据name遍历specificationItems查找其中对象是否有和name值一样的，有则将value追加到这个对象中，
		  如果没有则在specificationItems集合中新创建一个对象；
		  调用baseController中的findObjectInSpecs
		 */
		  var object = $scope.findObjectInSpecs($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		  if (object == null) {
			  $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		  }else {
		  		//如果选框选中添加完之后，再次添加该选框，此时则应删除该对象中该value集合中对应的值，否则则添加
			  if ($event.target.checked){
				  object.attributeValue.push(value);
			  } else {
			  	object.attributeValue.splice(object.attributeValue.indexOf(value,1));
			  	//当该attributeValue中的值全被清空后，则该对象没有存在的必要，全部删除
				  if (object.attributeValue.length == 0) {
				  	$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object,1));
				  }
			  }
		  }
		};

	//定义展示sku的方法
	$scope.createItemList=function () {
		//定义存储sku的集合
		$scope.entity.itemList=[{spec:{},price:0,num:0,status:'0',isDefault:"0"}];
		var specList = $scope.entity.goodsDesc.specificationItems;
		//循环items，将其中信息添加至sku
		for (var i = 0; i < specList.length; i++) {
			//新创建一个集合
			var newList=[];
			var specListName = specList[i].attributeName;
			var specListValue = specList[i].attributeValue;
			for (var j = 0; j < $scope.entity.itemList.length; j++) {
				var oldRow=$scope.entity.itemList[j];
				for (var k = 0; k < specListValue.length; k++) {
					//克隆oldRow
					var newRow = JSON.parse(JSON.stringify(oldRow));
					newRow.spec[specListName]=specListValue[k];
					newList.push(newRow);
				}
			}
			$scope.entity.itemList = newList;
		}
	};

	$scope.categoryNameList=[];
	$scope.itemCatNameList=function () {
		itemCatService.findAll().success(function (response) {
			for (var i = 0; i < response.length; i++) {
				$scope.categoryNameList[response[i].id]=response[i].name;
			}
		})
	};

	/*
		回写选中规格信息的方法；
		@param specName 规格选项的名字；
		@param specValue 规格选项对应的改名字下的值；
	 */
	$scope.checkAttributeValue=function (specName,specValue) {
		var items = $scope.entity.goodsDesc.specificationItems;
		/*先调用baseController中的findObjectInSpecs方法，判断页面提交的specName作为返回items集合
		  中的某对象的attributeName是否可以查找到集合中所对应的对象，如果不存在，直接返回false;
		 */
		var object = $scope.findObjectInSpecs(items,'attributeName',specName);
		if (object == null) {
			return false;
		}else{
			/*如果存在该对象，再判断该对象中的attributeValue数组中是否存在对应的值,此时判断不应循环遍历object.attributeValue
			  而应判断是否该值（specValue）对应的索引是否大于等于0，如果是，则说明object.attributeValue中存在对应的值，返回true
			 */
			if (object.attributeValue.indexOf(specValue)>=0){
				return true;
			}  else{
				return false;
			}
		}
	}

	/**
	 * 定义一个上下架的方法
	 * @param status 是否上下架的标记，上架则传1，下架则传0
	 */
	$scope.marketable=["已下架","已上架"];
	$scope.isMarketable=function (status) {
		//调用service中的isMarketable方法
		goodsService.isMarketable($scope.selectIds,status).success(function (response) {
			if (response.success){
				alert(response.message);
				$scope.reloadList();
				$scope.selectIds=[];
			}else {
				alert(response.message);
			}

		})
	}
});