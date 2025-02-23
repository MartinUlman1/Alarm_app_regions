package ivr.alarmregions.service;

import ivr.alarmregions.entity.Regions;
import ivr.alarmregions.repository.RegionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RegionsService {

    @Autowired
    private RegionsRepository regionsRepository;

    @Value("${reload.first}")
    private String reload1;

    @Value("${reload.second}")
    private String reload2;

    @Value("${sox.path}")
    private String soxPath;

    public void updateIvrParm(String key, String value) {
        Optional<Regions> ivrParamOptional = regionsRepository.findById(key);
        if (ivrParamOptional.isPresent()) {
            Regions ivrParam = ivrParamOptional.get();
            ivrParam.setIvrRegPlace(value);

            regionsRepository.save(ivrParam);

        }
    }

    public List<Regions> ivrRegForStredocesky() {
        List<String> ivrParmParms = Arrays.asList("STREDOCESKY_1", "STREDOCESKY_2", "STREDOCESKY_3");
        return regionsRepository.findAllById(ivrParmParms);
    }

    public List<Regions> ivrRegForKralovHrad() {
        List<String> ivrParmParms = Arrays.asList("KRALOVEHRADECKY_1", "KRALOVEHRADECKY_2", "KRALOVEHRADECKY_3");
        return regionsRepository.findAllById(ivrParmParms);
    }

    public List<Regions> ivrRegForKarlovarsky() {
        List<String> ivrParmParms = Arrays.asList("KARLOVARSKY_1", "KARLOVARSKY_2");
        return regionsRepository.findAllById(ivrParmParms);
    }

    public List<Regions> fetchSpecificIvrRegions(String ivrParmParm, String ivrParmValue) {
        return regionsRepository.findByIvrRegAndIvrId(ivrParmParm, ivrParmValue);
    }

    public void insertIvrParm(String key, String value) {
        if (!regionsRepository.existsById(key)) {
            Regions ivrParam = new Regions();
            ivrParam.setIvrRegValue(key);
            ivrParam.setIvrRegPlace(value);

            regionsRepository.save(ivrParam);

        }
    }

    public void updateOrInsertIvrParmKhc(String key, String value) {
        try {
            if (regionsRepository.findByIvrRegs(key).isPresent()) {
                updateIvrParm(key, value);

            } else {
                insertIvrParm(key, value);
            }
        } catch (Exception e) {
            log.error("Error while updating or inserting IVR parameter", e);
        }
    }

    public void reloadConfiguration() {
        RestTemplate restTemplate = new RestTemplate();
        String[] urls = {reload1, reload2};
        for (String url : urls) {
            try {
                restTemplate.getForObject(url, String.class);
                log.info("Reload konfigurace byl úspěšně proveden pro URL: {}", url);
            } catch (Exception e) {
                log.error("Chyba při reloadu konfigurace pro URL: {}", url, e);
            }
        }
    }

    public void convertAudioFile(String inputFilePath, String outputFilePath) {
        String command = soxPath + " " + inputFilePath + " -D -e a-law -b 8 -r 8000 -c 1 " + outputFilePath + " gain -n -10";

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            log.info("File succesfully upload");
        } catch (IOException | InterruptedException e) {
            log.error("File dont upload {} ", e.getMessage());
            log.error("Notification was sent to email");
        }
    }

    public String confirmUploadFile(String fileName, String userName, String department) {
        return "Soubor " + fileName + " byl úspěšně nahrán uživatelem : " + userName + " pro : " + department;
    }

}