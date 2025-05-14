package com.longjunwang.finbotplus.util;

import cn.hutool.extra.spring.SpringUtil;
import io.netty.channel.ChannelOption;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class FileUtil {

    public static Resource downloadFile(File file){
        Environment environment = SpringUtil.getBean(Environment.class);
        String local = environment.getProperty("telegram.local");
        createFolder(local);
        String filePath = file.getFilePath();
        String name = System.currentTimeMillis() + filePath.substring(filePath.indexOf("/") + 1);
        java.io.File newFile = new java.io.File("src/main/resources/images/" + name);
        downloadWithWebClient(file.getFileUrl(environment.getProperty("telegram.token")), newFile);
        return new FileSystemResource(newFile);
    }

    private static void downloadWithWebClient(String imageUrl, java.io.File destinationPath) {
        Environment environment = SpringUtil.getBean(Environment.class);
        HttpClient httpClient = HttpClient.create()
                .proxy(proxy -> proxy
                        .type(ProxyProvider.Proxy.HTTP)
                        .host("127.0.0.1")
                        .port(Integer.parseInt(environment.getProperty("telegram.proxyPort"))))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(30));

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient)).build();

        // 使用block()同步等待
        Resource resource = webClient.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(Resource.class)
                .block();  // 这里会阻塞直到下载完成

        try (InputStream in = resource.getInputStream();
             FileOutputStream out = new FileOutputStream(destinationPath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createFolder(String folderPath) {
        Path path = Paths.get(folderPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path); // 自动创建所有不存在的父目录
                System.out.println("文件夹创建成功: " + folderPath);
            } catch (IOException e) {
                System.err.println("文件夹创建失败: " + e.getMessage());
            }
        } else {
            System.out.println("文件夹已存在: " + folderPath);
        }

    }
}
