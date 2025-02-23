package ivr.alarmregions.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LogService {

    public String getLogContent(String page) {
        String logFilePath = "logs/" + page + ".log";
        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            try {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error creating log file.";
            }
        }
        try {
            return new String(Files.readAllBytes(Paths.get(logFilePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading log file.";
        }
    }

    public void logAction(String page, String action) {
        String logFilePath = "logs/" + page + ".log";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = timestamp + " - " + action;
        try (FileWriter writer = new FileWriter(logFilePath, StandardCharsets.UTF_8, true)) {
            writer.write(logMessage + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}