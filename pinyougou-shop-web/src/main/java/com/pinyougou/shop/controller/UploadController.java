package com.pinyougou.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/upload.do")
    public Result upload(MultipartFile file) throws Exception {

        //获取文件名称
        String originalFilename = file.getOriginalFilename();
        //获取文件的扩展名
        String exname = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        try {
            //创建FastDFS客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:properties/fdfs_client.conf");

            //执行上传处理
            String path = fastDFSClient.uploadFile(file.getBytes(), exname);

            //拼接url+path
            String url = FILE_SERVER_URL + path;

            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败！");
        }


    }


}
