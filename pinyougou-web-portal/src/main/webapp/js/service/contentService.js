app.service("contentService",function ($http) {
    this.findContentByCategoryId=function(categoryId){
        return $http.get("../portal/content/findAllContents.do?categoryId="+categoryId);
    }
});