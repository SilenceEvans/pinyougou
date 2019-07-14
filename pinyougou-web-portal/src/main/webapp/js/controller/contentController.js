app.controller("contentController",function ($scope,contentService) {

    $scope.contentList=[];
    $scope.findContentByCategoryId=function (categoryId) {
        contentService.findContentByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId]=response;
        })
    }

    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
});