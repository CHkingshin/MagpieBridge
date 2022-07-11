package cn.chihsien.cosapi.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author KingShin
 */
public interface FileService {
    //文件上传
    String upload(MultipartFile file);
}
