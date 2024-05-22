package com.b2bshop.project.service;

import com.b2bshop.project.model.Image;
import com.b2bshop.project.model.Product;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.ImageRepository;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    private final  ProductService productService;
    private final  JwtService jwtService;
    private final  UserService userService;
    private final ImageRepository imageRepository;

    public ImageService(ProductService productService, JwtService jwtService, UserService userService,
                        ImageRepository imageRepository) {
        this.productService = productService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.imageRepository = imageRepository;
    }

    private String uploadFile(File file, String fileName) throws IOException {
        BlobId blobId = BlobId.of("b2bshop-d0961.appspot.com", fileName); // Replace with your bucket name
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
        InputStream inputStream = ImageService.class.getClassLoader().getResourceAsStream("serviceAccountKey.json"); // change the file name with your one
        Credentials credentials = GoogleCredentials.fromStream(inputStream);
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        storage.create(blobInfo, Files.readAllBytes(file.toPath()));

        String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/b2bshop-d0961.appspot.com/o/%s?alt=media";
        return String.format(DOWNLOAD_URL, URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Transactional
    public List<String> upload(HttpServletRequest request, List<MultipartFile> multipartFiles) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userService.findUserByName(userName);

        List<String> urls = new ArrayList<>();
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                String fileName = multipartFile.getOriginalFilename();
                fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));

                File file = this.convertToFile(multipartFile, fileName);
                String URL = this.uploadFile(file, fileName);
                Image image = new Image();
                image.setUrl(URL);
                image.setCreatedBy(user);
                imageRepository.save(image);
                urls.add(URL);
                file.delete();
            }
            return urls;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
