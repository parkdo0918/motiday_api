package com.example.motiday_api.domain.file.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    //생성자, 업로드 경로 읽어옴 -> 파일 저장할 폴더 만들기
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    /**
     * 파일 저장
     * @param file 업로드할 파일
     * @param subDirectory 하위 디렉토리 (feed, profile, product 등)
     * @return 저장된 파일명 (UUID + 확장자만, 경로 제외)
     */
    public String storeFile(MultipartFile file, String subDirectory) {
        String originalFileName = file.getOriginalFilename();

        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            if (fileName.contains("..")) {
                throw new RuntimeException("파일명에 부적절한 경로가 포함되어 있습니다: " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(subDirectory);
            Files.createDirectories(targetLocation);

            Path filePath = targetLocation.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 순수 파일명만 반환 (경로 제외)
            return fileName;  // ← 변경: subDirectory + "/" + fileName 대신
        } catch (IOException ex) {
            throw new RuntimeException("파일을 저장할 수 없습니다: " + fileName, ex);
        }
    }

    /**
     * 파일 로드
     * @param fileName 파일명 (하위 디렉토리 포함)
     * @return 파일 리소스
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName, ex);
        }
    }

    /**
     * 파일 삭제
     * @param fileName 파일명 (하위 디렉토리 포함)
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("파일을 삭제할 수 없습니다: " + fileName, ex);
        }
    }
}