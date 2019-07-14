app.controller("searchController",function ($scope,$location,searchService) {

    /*
        1.searchMap中添加当前页pageNo,每页显示条数pageSize
     */
    $scope.searchMap={"keywords":"","category":"","brand":"","spec":{},"price":"","pageNo":1,"pageSize":20,"sort":"","sortField":""};
    $scope.searchItem=function (){
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
       searchService.searchItem($scope.searchMap).success(function (response) {
           $scope.resultMap=response;
           $scope.resultMap.total=response.total;
           $scope.resultMap.totalPage = response.totalPage;
           buildPageLabel();
       });
    };
    
    //定义一个搜索框专用的方法,点击搜索框时清空searchMap原有的属性值
    $scope.search=function(){
        if ($scope.searchMap.keywords == ""){
            return;
        }
        $scope.searchMap.price="";
        $scope.searchMap.pageNo=1;
        $scope.searchMap.brand="";
        $scope.searchMap.category="";
        $scope.searchMap.spec={};
        $scope.searchItem();
    };

    $scope.addSearchItem=function (key,value) {
        if (key == "category" || key == "brand" || key == "price") {
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
        }
        $scope.searchItem();
    };

    $scope.removeSearchItem=function (key) {
        if (key == "category" || key == "brand" || key == "price") {
            $scope.searchMap[key]="";
        }else {
            delete $scope.searchMap.spec[key];
        }
        $scope.searchItem();
    }

    //定义获得查询结果后显示分页的方法
    buildPageLabel=function () {
        $scope.pageLabel=[];
        //定义起始页
        var firstPage = 1;
        var maxPage =  $scope.resultMap.totalPage;
        var lastPage = maxPage;

        //初始化前后都有省略号
        $scope.firstDot = true;
        $scope.lastDot = true;
        //如果总页数大于5页，一次遍历5个数，firstPage和lastPage重新赋值
        if (maxPage > 5) {
            //如果当前页小于等于3，firstpage等于1
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
                $scope.firstDot = false;
                //如果当前页大于最大页数减2
            }else if ($scope.searchMap.pageNo >= maxPage -2 ){
                firstPage = maxPage - 4;
                $scope.lastDot = false;
            } else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
                $scope.firstDot = true;
                $scope.lastDot = true;
            }
        }
        //定义一个数组，遍历总页数，往数组中存储到总页数长度的元素值
        for (var i = firstPage; i <= lastPage; i++){
            $scope.pageLabel.push(i);
        }
    }

    //定义根据页码进行搜索的方法
    $scope.queryByPageNo = function (pageNo) {
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPage) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.searchItem();
    }

    //定义排序查询的方法
    $scope.sortSearch=function (sort, sortField) {
        $scope.searchMap.sort=sort;
        $scope.searchMap.sortField=sortField;
        $scope.searchItem();
    }

    //定义根据搜索关键字隐藏品牌列表的方法
    $scope.keywordsIsBrand=function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }

    //接收从门户页面传递过来的参数
    $scope.loadKeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.searchItem();
    }
});