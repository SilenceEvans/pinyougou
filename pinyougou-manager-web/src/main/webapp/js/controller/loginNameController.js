
//定义关于获取用户名的controller
app.controller("loginNameController",function ($scope,loginNameService) {

    $scope.showName=function () {
        loginNameService.loginName().success(
            function (response) {
            $scope.loginName=response.loginName;

        })
    }
});