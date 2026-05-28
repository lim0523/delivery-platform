package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliyunOSSOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/admin/common")
@Api("通用控制器")
public class CommonController {
@Autowired
AliyunOSSOperator aliyunOSSOperator;
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload( MultipartFile file){
        log.info("上传文件{}",file.getOriginalFilename());
        try {
            String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
            log.info("url地址:{}",url);
            return Result.success(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
