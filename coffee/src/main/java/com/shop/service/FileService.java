package com.shop.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Log
public class FileService {
    private final String uploadPath = "C:/upload"; // 업로드 경로 설정

    public String uploadFile(String uploadPath, String originalFileName,byte[] fileData)
            throws Exception{
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName= uuid.toString() + extension;
        String fileUploadFilUrl = uploadPath + "/" + savedFileName;
        System.out.println(fileUploadFilUrl);
        FileOutputStream fos = new FileOutputStream(fileUploadFilUrl);
        fos.write(fileData);
        fos.close();
        return savedFileName;
    }
    //    public void deleteFile(String filePath) throws Exception{
//        File deleteFile = new File(filePath);
//
//        if(deleteFile.exists()){
//            deleteFile.delete();
//            log.info("파일을 삭제하였습니다.");
//        }else{
//            log.info("파일이 존재하지 않습니다.");
//        }
//    }
    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(fileName);
            Files.deleteIfExists(filePath); // 파일이 존재하면 삭제
        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 중 오류가 발생했습니다: " + fileName, e);
        }
    }
}
