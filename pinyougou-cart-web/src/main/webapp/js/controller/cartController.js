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

    /*
        定义一个用户登录后，获取用户所拥有地址信息的方法
     */
    $scope.findUserAddress=function () {
        cartService.findUserAddress().success(function (response) {
            $scope.userAddress = response;
            //对userAddres进行遍历，将默认的地址对象赋值给$scope.address
            for (var i = 0; i < $scope.userAddress.length; i++) {
                if ("1" == $scope.userAddress[i].isDefault) {
                    $scope.address = $scope.userAddress[i];
                    return;
                }
            }
        })
    };
    /*
        定义一个方法，为$scope.address赋值为当前的address
     */
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }
    /*
        再定义一个方法，如果当前循环的address是$scope.address,则返回true
     */
    $scope.isSelected=function (address) {
        if (address == $scope.address){
            return true;
        } else {
            return false;
        }
    }
    /*
        定义一个结算的购物车$scope.settleCartList
        定义一个方法，当点击单选框，进行结算时，将所对应的购物车添加至$scope.settleCartList
     */
    /*$scope.settleCartList=[];
    $scope.settleCart={"sellerId":"","sellerName":"","orderList":$scope.settleCartList.orderItemList};
    $scope.settleOrderItemList=[];
    //结算单个购物车
    $scope.addSettleCart=function (cart) {
        $scope.settleCartList.push(cart);
    };
    //结算单个购物车里的单个订单
    $scope.addOrderItemList=function (sellerId,sellerName,orderList) {
        $scope.settleOrderItemList.push(orderList);
        $scope.settleCart={"sellerId":sellerId,"sellerName":sellerName,"orderList":$scope.settleOrderItemList};
        $scope.settleCartList.push($scope.settleCart);
    };
    //结算全部购物车
    $scope.addSettleCartList=function () {
        $scope.settleCartList =$scope.cartList;
    }*/

    //选择支付方式
    $scope.order={payType:"1"};
    $scope.selectPayType=function (type) {
        $scope.order.payType=type;
    };

    //定义提交订单的方法
    $scope.submitOrder=function() {
        $scope.order.receiverAreaName = $scope.address.address;//地址
        $scope.order.receiverMobile = $scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success) {
                alert(response.message);
            }else {
                alert(response.message);
            }
        })
    }
});