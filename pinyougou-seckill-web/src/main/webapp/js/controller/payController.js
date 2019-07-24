app.controller("payController",function (payService, $scope,$location) {
    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            $scope.money=(response.total_fee)/100;
            $scope.out_trade_no=response.out_trade_no;
            //生成二维码
            var qr = new QRious({
                element:document.getElementById('qrious'),
                size:250,
                level:'H',
                value:response.code_url
            });
            //调用发送请求查询订单状态的方法
            queryPayStatus($scope.out_trade_no);
        });
    };

    /*
        定义查询订单支付状态的方法
     */
    queryPayStatus=function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (response) {
            if (response.success) {
                location.href="paysuccess.html#?money="+$scope.money;
            }else {
                    alert(response.message);
                    location.href="payfail.html";
            }
        })
    }

    /*
        定义页面跳转到支付成功页之后显示余额的方法
     */
    $scope.getMoney=function () {
        return $location.search()['money'];
    }
});