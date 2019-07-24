app.controller("secKillGoodsController",function (secKillGoodsService, $scope,$location,$interval) {
    $scope.findList = function () {
        secKillGoodsService.findList().success(function (response) {
            $scope.goodsList=response;
        });
    };
    /**
     * 定义提交秒杀商品订单的方法
     */
    $scope.submitOrder=function(){
        secKillGoodsService.submitOrder($location.search()['id']).success(function (response) {
            if (response.success){
                alert(response.message+",请在1分钟内完成支付！");
                //跳转到支付页面
                location.href="pay.html";
            } else {
                if (confirm(response.message)) {
                    location.href="login.html";
                }
            }
        })
    };
    /**
     * 定义获得商品详情的方法
     */
    $scope.findOneFromRedis=function () {
        secKillGoodsService.findOneFromRedis($location.search()['id']).success(function (response) {
            $scope.good=response;
            /*
       定义显示倒计时的方法
    */
            allSecond=Math.floor((new Date($scope.good.endTime).getTime() -
                new Date().getTime())/1000);
            time=$interval(function () {
                if (allSecond > 0){
                    allSecond -= 1;
                    $scope.timeString=convertTimeString(allSecond);//转换时间字符串
                }else {
                    $interval.cancel(time);
                    alert("该商品秒杀活动已结束")
                }
            },1000);
        })
    };

        //转换秒为日时分秒的方法
    convertTimeString=function (allSecond) {
        //求天数
        var day = Math.floor(allSecond/(24*60*60));
        //求小时
        var hour = Math.floor((allSecond - (day*24*3600))/3600);
        //求分钟
        var minute = Math.floor((allSecond - ((day*24*3600)+(hour*3600)))/60);
        //秒数
        var second = allSecond - ((minute*60)+(hour*3600)+(day*24*3600));
        var timeString="";
        if (day > 0){
            timeString = day+"天";
        }
        if (hour < 10){
            timeString = timeString + "0" + hour + ":";
        } else {
            timeString = timeString + hour + ":";
        }
        if (minute < 10){
            timeString = timeString + "0" + minute + ":";
        } else {
            timeString = timeString + minute + ":";
        }
        if (second < 10){
            timeString = timeString + "0" + second;
        }else {
            timeString = timeString + second;
        }
        return timeString;
    }
});