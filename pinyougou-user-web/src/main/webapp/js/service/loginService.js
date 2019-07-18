app.service("loginService",function ($http) {
    this.getUsername=function () {
        return $http.get("../login/showName.do");
    }
});