package com.longjunwang.finbotplus.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    // 配置常量
    private static final String STORAGE_FILE = "id_sequence.dat";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 实例变量
    private static final String prefix = "RC";
    private static final Path storagePath = Paths.get(STORAGE_FILE);


    public static synchronized String generateId() throws IOException {
        // 初始化或加载持久化数据
        SequenceData data = loadSequenceData();
        String today = getCurrentDateString();

        String currentDateStr;
        AtomicInteger sequence;
        if (data != null && data.date.equals(today)) {
            // 如果是同一天，继续使用保存的序列号
            currentDateStr = data.date;
            sequence = new AtomicInteger(data.sequence + 1);
        } else {
            // 新的一天或首次使用，重置序列号
            currentDateStr = today;
            sequence = new AtomicInteger(1);
        }

        // 生成4位序号，前面补零
        int seq = sequence.getAndIncrement();
        if (seq > 9999) {
            throw new IllegalStateException("当日序号已超过9999");
        }

        // 持久化当前状态
        saveSequenceData(new SequenceData(currentDateStr, seq));

        return prefix + currentDateStr + String.format("%04d", seq);
    }

    // 内部数据结构
    private static class SequenceData implements Serializable {
        private static final long serialVersionUID = 1L;
        final String date;
        final int sequence;

        SequenceData(String date, int sequence) {
            this.date = date;
            this.sequence = sequence;
        }
    }

    // 持久化方法
    private static void saveSequenceData(SequenceData data) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(storagePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            oos.writeObject(data);
        }
    }

    // 加载方法
    private static SequenceData loadSequenceData() {
        if (!Files.exists(storagePath)) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(storagePath))) {
            return (SequenceData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载序列号数据失败，将重新开始计数: " + e.getMessage());
            return null;
        }
    }

    private static String getCurrentDateString() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
}
