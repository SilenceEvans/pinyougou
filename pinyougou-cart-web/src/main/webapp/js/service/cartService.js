app.service("cartService",function ($http) {
    this.findAllCart=function () {
        return $http.get("../cart/findAllCart.do");
    }

    this.addGoodsToCart=function(itemId,num){
        return $http.get('cart/addGoodsToCart.do?itemId='+itemId+'&num='+num);
    }

    this.findUserAddress=function () {
        return $http.get("address/findUserAddress.do");
    }
    this.submitOrder=function (order) {
        return $http.post("order/add.do",order);
    }
});