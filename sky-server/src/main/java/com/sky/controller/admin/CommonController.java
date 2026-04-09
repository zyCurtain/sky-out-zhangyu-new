package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/common")
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result upload(MultipartFile file){
        String originalFilename = file.getOriginalFilename(); // 获取原始文件名
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 获取文件名
        String new_name = UUID.randomUUID().toString() + extension; // 构造新文件名

        // 调用OSS工具类上传文件
        try {
            String uploadPath = aliOssUtil.upload(file.getBytes(), new_name); // 返回文件路径
            return Result.success(uploadPath);
        } catch (IOException e) {
            log.error("文件上传失败：{}",e);
        }
        return Result.success(MessageConstant.UPLOAD_FAILED);
    }
}
