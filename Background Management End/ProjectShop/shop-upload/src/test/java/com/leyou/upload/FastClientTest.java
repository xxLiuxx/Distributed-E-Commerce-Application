package com.leyou.upload;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FastClientTest {
    @Autowired
    private FastFileStorageClient fileStorageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;

    @Test
    public void uploadTest() throws FileNotFoundException {
        File file = new File("/Users/liuyuchen/IdeaProjects/Leyou/image/im1.jpg");
        // 上传并保存图片，参数：1-上传的文件流 2-文件的大小 3-文件的后缀 4-可以不管他
        StorePath storePath = this.fileStorageClient.uploadFile(new FileInputStream(file), file.length(), "jpg", null);
        // 带分组路径
        System.out.println(storePath.getFullPath());
        // 不带分组路径
        System.out.println(storePath.getPath());
    }

    @Test
    public void uploadThumbTest() throws FileNotFoundException {
        File file = new File("/Users/liuyuchen/IdeaProjects/Leyou/image/im1.jpg");
        StorePath storePath = this.fileStorageClient.uploadImageAndCrtThumbImage(new FileInputStream(file), file.length(), "png", null);
        System.out.println(storePath.getFullPath());
        System.out.println(storePath.getPath());
        String thumbImagePath = thumbImageConfig.getThumbImagePath(storePath.getFullPath());
        System.out.println(thumbImagePath);
    }

}
