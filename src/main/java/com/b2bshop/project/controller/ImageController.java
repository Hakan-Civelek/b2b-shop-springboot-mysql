package com.b2bshop.project.controller;

import com.b2bshop.project.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public List<String> upload(HttpServletRequest request, @RequestParam("file") List<MultipartFile> multipartFiles) {
        return imageService.upload(request, multipartFiles);
    }
}