package com.gmall;


import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ManagerWebApplicationTest {

    @Test
    public void uploadFile() throws IOException, MyException {
        String file = this.getClass().getResource("/tracker.conf").getFile();

        ClientGlobal.init(file);
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        StorageClient storageClient = new StorageClient(trackerServer, null);

        String[] path = storageClient.upload_file("/Users/chenliu/Downloads/duoduo.jpg", "jpg", null);

        for (String s : path) {
            System.out.println(s);
        }


    }
}
