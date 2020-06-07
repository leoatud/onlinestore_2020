package com.gmall.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
public class FileController {

    @Value("${fileServer.url}")
    String fileServerUrl;

    @PostMapping("/fileUpload")
    public String fileUpload(@RequestParam MultipartFile file) throws IOException, MyException {
        String configPath = this.getClass().getResource("/tracker.conf").getFile();

        ClientGlobal.init(configPath);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        StorageClient storageClient = new StorageClient(trackerServer, null);

        String originalFileName = file.getOriginalFilename();
        String extentionName = StringUtils.substringAfterLast(originalFileName, ",");

        String[] path = storageClient.upload_file(file.getBytes(), extentionName, null);

        String fileUrl = fileServerUrl;
        for (String s : path) {
            fileUrl += "/" + s;
        }
        return fileUrl;  //return path
    }
}
