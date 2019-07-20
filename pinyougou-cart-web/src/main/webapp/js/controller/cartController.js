app.controller("cartController",function ($scope,cartService) {

    $scope.findAllCart=function(){
        cartService.findAllCart().success(function (response) {
            $scope.cartList=response;
            $scope.totalValue={totalNum:0,totalMoney:0.00};
            sum();
        })
    };

    $scope.addGoodsToCart=function (itemId,num) {
        cartService.addGoodsToCart(itemId,num).success(function (response) {
            if (response.success){
                $scope.findAllCart();
            }else {
                alert(response.message)
            }
        })
    };
    /**
     * 定义获取订单总数与总金额的方法
     * @type {{totalNum: number, totalMoney: number}}
     */
    $scope.totalValue={totalNum:0,totalMoney:0.00};
    sum=function () {
        for (var i = 0; i < $scope.cartList.length; i++) {
            orderItemList=$scope.cartList[i].orderItemList;
            for (var j = 0; j < orderItemList.length; j++) {
                $scope.totalValue.totalNum+=orderItemList[j].num;
                $scope.totalValue.totalMoney+=orderItemList[j].totalFee;
            }
        }
    }
});