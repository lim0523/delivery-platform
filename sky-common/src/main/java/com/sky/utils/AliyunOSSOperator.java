package com.sky.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.sky.properties.AliOssProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class AliyunOSSOperator {

    @Autowired
    private AliOssProperties aliyunOssProperties;

    public String upload(byte[] content, String originalFilename) throws Exception {
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        OSS ossClient = new OSSClientBuilder().build(
                aliyunOssProperties.getEndpoint(),
                aliyunOssProperties.getAccessKeyId(),
                aliyunOssProperties.getAccessKeySecret()
        );

        try {
            ossClient.putObject(
                    aliyunOssProperties.getBucketName(),
                    objectName,
                    new ByteArrayInputStream(content)
            );
        } finally {
            ossClient.shutdown();
        }

        return aliyunOssProperties.getEndpoint().split("//")[0]
                + "//"
                + aliyunOssProperties.getBucketName()
                + "."
                + aliyunOssProperties.getEndpoint().split("//")[1]
                + "/"
                + objectName;
    }
}