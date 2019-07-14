 //控制层 
app.controller('itemController' ,function($scope){
	$scope.addCart=function (x) {
		$scope.num=$scope.num+x;
		if ($scope.num < 1){
			$scope.num = 1;
		}
	}
	//定义规格
	$scope.specification={};
	//定义选中之后给其中赋值的方法
	$scope.selectSpecification=function (name, value) {
		$scope.specification[name]=value;
		$scope.searchSku();
	};
	//判断选框是否选中以为其定义样式
	$scope.isSelected=function (name, value) {
		if ($scope.specification[name]==value) {
			return true;
		}else{
			return false;
		}
	}

	//定义加载sku的方法
	$scope.loadSku=function () {
		$scope.sku=skuList[0];
		$scope.specification=JSON.parse(JSON.stringify($scope.sku.spec))
	}

	//判断两个对象是否相等
	$scope.matchObject=function (map1, map2) {
		for (var k in map1) {
			if (map1[k]!=map2[k]){
				return false;
			}
		}
		for (var j in map2){
			if (map2[j]!=map2[j]){
				return false;
			}
		}
		return true;
	};

	//当用户选中相应的规格后，应该将选中的规格在skuList中进行遍历
	$scope.searchSku=function () {
		for (var i = 0; i < skuList.length; i++) {
			if ($scope.matchObject(skuList[i].spec,$scope.specification)) {
				$scope.sku=skuList[i];
			}
		}
		$scope.sku={id:0,title:'--------',price:0};
	}
});	
