package com.leyou.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {
    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpg", "image/jpeg", "image/gif");

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FastFileStorageClient fileStorageClient;

    public String uploadImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        //检查文件类型
        if(!CONTENT_TYPES.contains(contentType)) {
            LOGGER.info("文件类型不合法： {}", originalFilename);
            return null;
        }

        try {
            //检查文件内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if(bufferedImage == null) {
                LOGGER.info("文件内容不合法： {}", originalFilename);
                return null;
            }

            //上传文件到服务器
            //file.transferTo(new File("/Users/liuyuchen/IdeaProjects/Leyou/image/" + originalFilename));
            // 获取上传文件的类型
            String suffix = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath storePath = this.fileStorageClient.uploadFile(file.getInputStream(), file.getSize(), suffix, null);
            String fullPath = storePath.getFullPath();
            //返回url，通过nginx转发到服务器地址获取图片
            return "http://image.leyou.com/" + fullPath;
        } catch (IOException e) {
            LOGGER.info("服务器内部错误: {}", originalFilename);
            e.printStackTrace();
        }
        return null;
    }
}
