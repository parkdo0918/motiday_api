package com.example.motiday_api.controller;

import com.example.motiday_api.domain.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "feed") String type
    ) {
        if (!type.equals("feed") && !type.equals("profile") && !type.equals("product")) {
            throw new IllegalArgumentException("type은 feed, profile, product만 가능합니다.");
        }

        // 파일 저장 (순수 파일명만 반환됨)
        String fileName = fileStorageService.storeFile(file, type);

        // 파일 다운로드 URI 생성
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .path(type)
                .path("/")
                .path(fileName)
                .toUriString();

        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);  // uuid.png
        response.put("fileUrl", fileDownloadUri);  // /api/files/feed/uuid.png
        response.put("fileType", file.getContentType());
        response.put("size", String.valueOf(file.getSize()));

        return ResponseEntity.ok(response);
    }

    /**
     * 파일 다운로드/조회
     * @param fileName 파일명 (하위 디렉토리 포함, 예: feed/abc-123.jpg)
     * @return 파일 리소스
     */
    @GetMapping("/{type}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String type,
            @PathVariable String fileName
    ) {
        // 파일 로드
        Resource resource = fileStorageService.loadFileAsResource(type + "/" + fileName);

        // 파일의 Content-Type 결정
        String contentType = "application/octet-stream";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.endsWith(".gif")) {
            contentType = "image/gif";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * 파일 삭제
     * @param type 파일 타입
     * @param fileName 파일명
     * @return 성공 메시지
     */
    @DeleteMapping("/{type}/{fileName:.+}")
    public ResponseEntity<Map<String, String>> deleteFile(
            @PathVariable String type,
            @PathVariable String fileName
    ) {
        fileStorageService.deleteFile(type + "/" + fileName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "파일이 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }
}