package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FileProcessingController {

    private final FileProcessingService fileProcessingService;

    @Autowired
    public FileProcessingController(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
    }

    @GetMapping("/download/{fileName}")
    public String processFile(@PathVariable String fileName) {
        System.out.println("Got request for " + fileName);
        fileProcessingService.processFile(fileName).blockLast();
        return "Done";
    }

    @GetMapping("/test-api/{fileUrl}")
    public String testApi(@PathVariable String fileUrl) {
        return fileUrl;
    }
}