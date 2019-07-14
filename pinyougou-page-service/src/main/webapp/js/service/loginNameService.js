
//定义关于获取用户名的service
app.service("loginNameService",function ($http) {

    this.loginName=function () {

        return $http.post("../index/loginName.do");

    };

});