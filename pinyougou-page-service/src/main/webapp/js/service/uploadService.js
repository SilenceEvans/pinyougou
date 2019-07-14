app.service("uploadService",function ($http) {

    this.uploadFile=function () {

        var formData = new FormData();
        //添加第一个添加框中的文件
        formData.append("file",file.files[0]);
        return $http({
            method:"post",
            url:"../upload.do",
            data:formData,
            headers:{"content-type":undefined},
            transformRequest:angular.identity
        })
    }
});