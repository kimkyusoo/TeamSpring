package com.example.imageupload.controller;

import com.example.imageupload.dto.response.ResponseDto;
import com.example.imageupload.service.ImageService;
import com.example.imageupload.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/")
@Slf4j
@RequiredArgsConstructor
@RestController
public class ImageController {
    private final ImageService imageService;

    private final PostService postService;

    @RequestMapping(value = "/api/post/{id}", method = RequestMethod.GET)
    public ResponseDto<?> getPost(@PathVariable Long id) {

        return postService.getPost(id);
    }



    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> execWrite(@RequestPart MultipartFile file) throws IOException {
        String imgPath = imageService.upload(file);
        log.info("imagePath = {}", imgPath);
        return ResponseEntity.ok(imgPath);
    }


    @PostMapping(value = "/api/image/{ignoredId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<List<String>> execWriteBulk(@RequestPart List<MultipartFile> file) {
        final List<String> urls = file.stream()
                .map(multipartFile -> {
                    try {
                        return imageService.upload(multipartFile);
                    } catch (IOException e) {
                        log.error("image bulk upload error... ", e);
                        return null;
                    }
                }).peek(s -> log.info("imagePath = {}", s))
                .collect(Collectors.toList());

        return ResponseEntity.ok(urls);
    }
}