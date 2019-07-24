app.service("secKillGoodsService",function ($http) {
    this.findList=function () {
        return $http.post("/seckillGoods/findList.do")
    };

    this.findOneFromRedis=function (id) {
        return $http.get("/seckillGoods/findOneFormRedis.do?id="+id)
    }
    this.submitOrder=function (id) {
        return $http.get("/seckillOrder/submitOrder.do?seckillGoodId="+id);
    }
});