package ivr.alarmregions.service;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
@Service
@Slf4j
@Data
public class FileService {

    private Map<String, List<ParamItem>> hlaskyDict;
    private Map<String, String> reverseHlasky;


    public FileService(@Value("${filename}") String filename) throws IOException {
        hlaskyDict = new HashMap<>();
        reverseHlasky = new HashMap<>();
        loadFile(filename);
    }

    private void loadFile(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource(filename);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String section = "-";
            List<ParamItem> paramList = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                if (line.matches("^\\[[a-zA-Z][a-zA-Z0-9]*\\].*")) {
                    hlaskyDict.put(section, paramList);
                    section = line.substring(1, line.indexOf(']'));
                    paramList = new ArrayList<>();
                } else {
                    String[] parts = line.split(";");
                    if (parts.length > 1) {
                        String key = parts[0].trim();
                        List<String> values = new ArrayList<>();
                        Arrays.stream(parts)
                                .skip(1)
                                .map(String::trim)
                                .forEach(value -> {
                                    assert (value.matches("\\d{1,4}") || "NONE".equals(value));
                                    values.add(value);
                                    reverseHlasky.put(value, key);
                                });
                        paramList.add(new ParamItem(key, values));
                    }
                }
            }
            reverseHlasky.put("1", "SPECIALNI uploadovana hlaska");
            hlaskyDict.put(section, paramList);
        }
    }

    public static class ParamItem {
        private String key;
        private List<String> values;

        public ParamItem(String key, List<String> values) {
            this.key = key;
            this.values = values;
        }

        public String getKey() {
            return key;
        }

        public List<String> getValues() {
            return values;
        }
    }

}

