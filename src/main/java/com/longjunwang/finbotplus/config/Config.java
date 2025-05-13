package com.longjunwang.finbotplus.config;

import com.longjunwang.finbotplus.telegram.TeleGramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
@Slf4j
public class Config {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    // 梯子的IP，我的是本地的
    public static final String proxyHost = "127.0.0.1";
    // 本地监听的端口
    public static final int proxyPort = 7890;

    @Bean
    public DefaultBotOptions defaultBotOptions() {
        DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setProxyHost(proxyHost);
        botOptions.setProxyPort(proxyPort);
        //ProxyType是个枚举
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        return botOptions;
    }

    @Bean
    public DefaultBotSession DefaultBotSession() {
        DefaultBotSession defaultBotSession = new DefaultBotSession();
        defaultBotSession.setOptions(defaultBotOptions());
        return defaultBotSession;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TeleGramBot teleGramBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession().getClass());
        telegramBotsApi.registerBot(teleGramBot);
        return telegramBotsApi;
    }

    @Bean
    public DataSource dataSource() {
        String dbPath = dbUrl.replace("jdbc:sqlite:", "");
        File dbFile = new File(dbPath);

        // 如果数据库文件不存在，创建它
        if (!dbFile.exists()) {
            try {
                log.info("dbPath不存在,将自动创建, dbPath: {}", dbPath);
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
                initializeDatabase(dbPath);
            } catch (IOException e) {
                throw new RuntimeException("无法创建数据库文件", e);
            }
        }

        return DataSourceBuilder.create()
                .url(dbUrl)
                .driverClassName("org.sqlite.JDBC")
                .build();
    }

    private void initializeDatabase(String dbPath) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement()) {
            String tableSQl = """
                    CREATE TABLE IF NOT EXISTS record
                    (
                        record_no  TEXT NOT NULL PRIMARY KEY,
                        date       TEXT NOT NULL,
                        amount     TEXT NOT NULL,
                        type       TEXT NOT NULL,
                        remark     TEXT,
                        sub_remark TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                    """;
            // 执行建表SQL
            stmt.executeUpdate(tableSQl);
            // 可以添加更多表或初始化数据
        } catch (SQLException e) {
            throw new RuntimeException("初始化数据库失败", e);
        }
    }
}