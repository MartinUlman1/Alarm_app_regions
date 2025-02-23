package ivr.alarmregions.service;

import ivr.alarmregions.entity.PragueReg;
import ivr.alarmregions.repository.PragueRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Slf4j
@Data
public class PragueService {

    @Autowired
    private PragueRepository pragueRepository;

    @Autowired
    private CacheService cacheService;

    public void storeMessage(String key, String message) {
        cacheService.put(key, message);
    }

    public String retrieveMessage(String key) {
        return cacheService.get(key);
    }

    public void storeData(String strIvr, String login, String id) {
        String dataStored = strIvr + "N\\" + login;
        if (dataStored.length() > 250) {
            dataStored = dataStored.substring(0, 250);
        }

        Optional<PragueReg> dta = pragueRepository.findById(id);
        PragueReg reg = dta.orElse(new PragueReg());
        reg.setIvrPragueReg(id);
        reg.setIvrPragueValue(dataStored);
        pragueRepository.save(reg);
    }

    public void storeDataUploadFile(String login, String id) {
        String dataStored = "S\\" + login;
        if (dataStored.length() > 250) {
            dataStored = dataStored.substring(0, 250);
        }

        Optional<PragueReg> dta = pragueRepository.findById(id);
        PragueReg reg = dta.orElse(new PragueReg());
        reg.setIvrPragueReg(id);
        reg.setIvrPragueValue(dataStored);
        pragueRepository.save(reg);
    }

    public void deleteData(String value, String login, String id) {
        String dataStored = value + "\\" + login;

        Optional<PragueReg> dta = pragueRepository.findById(id);
        PragueReg reg = dta.orElse(new PragueReg());
        reg.setIvrPragueReg(id);
        reg.setIvrPragueValue(dataStored);
        pragueRepository.save(reg);

    }

    public String retrieveData(String id) {
        Optional<PragueReg> reg = pragueRepository.findById(id);
        return reg.map(PragueReg::getIvrPragueValue).orElse("");
    }

    /**
     * IVR string construction based on the values entered in formData.
     * - Adds a fixed message, if defined.
     * - Processes a compound message (introduction, locations, times, excuses).
     * - Limits the number of locations to a maximum of three.
     * - Formats the date and time if they are entered correctly.
     * - Returns "0" if there is no relevant data (off message).
     *
     * @param formData Map containing input data
     * @return IVR string in the required format
     */
    public String constructStringProIvr(HashMap<String, String> formData) {
        final int MAXIMUM_LOKALIT = 3;
        String stringProIvr = "";
        String pe = formData.get("Pevna_hlaska");
        if (pe != null && !"NONE".equals(pe)) {
            stringProIvr += "S" + String.format("%04d", Integer.parseInt(pe)); // pole Snnnn pro IVR
        }
        if (formData.containsKey("Skladana_ANO")) {
            String[] uvodParts = formData.getOrDefault("UVOD", "").trim().split(",");
            String uv1 = uvodParts.length > 0 ? uvodParts[0] : "0";
            String uv2 = uvodParts.length > 1 ? uvodParts[1] : "0";

            if (!"0".equals(uv1)) {
                stringProIvr += "S" + String.format("%04d", Integer.parseInt(uv1)); // uvodni fragment
            }
            if (!"0".equals(uv2)) {
                stringProIvr += "S" + String.format("%04d", Integer.parseInt(uv2)); // uvodni fragment
            }

            List<String> listLo = new ArrayList<>();
            formData.forEach((key, value) -> {
                log.info("form data {} {}", key, value);
                if (key.equalsIgnoreCase("LOKALITA")) {
                    listLo.add(value);
                }
            });
            int mm = Math.min(MAXIMUM_LOKALIT, listLo.size());
            if (!listLo.get(0).equals("NONE")) {
                for (String lo : listLo.subList(0, mm)) {
                    stringProIvr += "S" + String.format("%04d", Integer.parseInt(lo));
                    log.info("Lokality info {} {}", listLo.size(), formData.getOrDefault("LOKALITA", ""));
                }
            }
            String cu = formData.get("CASY");
            if ("calendar".equals(cu)) {
                String day = formData.getOrDefault("DAY", "0");
                try {
                    LocalDate date = LocalDate.parse(day);
                    stringProIvr += "Y" + date.getYear() + "D" + String.format("%02d", date.getMonthValue()) + String.format("%02d", date.getDayOfMonth());
                } catch (DateTimeParseException e) {
                    log.error("Invalid date format for DAY: " + day, e);
                    // Handle the error, e.g., set a default date or skip this part
                    stringProIvr += "Y0000D0000"; // Example of setting a default value
                }
                String hour = formData.getOrDefault("HOUR", "0");
                if (hour.length() == 2) {
                    stringProIvr += "H" + hour + "00";
                }
            }
            String omluvy = formData.get("OMLUVY");
            if (omluvy != null && !"NONE".equals(omluvy)) {
                stringProIvr += "S" + String.format("%04d", Integer.parseInt(omluvy));
            }
        }
        if (stringProIvr.isEmpty()) {
            stringProIvr = "0"; // to znamena vypnutou hlasku
        }

        return stringProIvr;
    }


    public Map<String, String> generateDayOptions() {
        Map<String, String> dayOptions = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i <= 3; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            String formattedDate = date.format(formatter);
            dayOptions.put(formattedDate, formattedDate);
        }
        return dayOptions;
    }

    public Map<String, String> generateHourOptions() {
        Map<String, String> hourOptions = new TreeMap<>();
        for (int i = 0; i < 24; i++) {
            String hour = String.format("%02d", i);
            hourOptions.put(hour, hour);
        }
        return hourOptions;
    }

    /**
     * Compiles a crisis report for Prague based on input data.
     *
     * - Gets the corresponding message texts (fixed message, introduction, location, times, apologies)
     * from the message dictionary (`hlaskyDict`) according to the values in `formData`.
     * - If the compound message (`Composite_ANO`) is enabled, adds the introduction message, location,
     * the estimated time to fix the fault, and an apology.
     * - Includes the user who made the change in the message.
     * - The output is the formatted text of the crisis message.
     *
     * @param formData A map containing input data about the message.
     * @param hlaskyDict Dictionary of messages for assigning texts to values
     * @param user The user making the change
     * @return The compiled text message of the crisis message
     */

    public String constructMessageForPrague(Map<String, String> formData, Map<String, List<FileService.ParamItem>> hlaskyDict,
                                            String user) {
        String pevnaHlaska = formData.get("Pevna_hlaska");
        String pevnaHlaskaText = hlaskyDict.get("PEVNE").stream()
                .filter(item -> item.getValues().get(0).equals(pevnaHlaska))
                .findFirst()
                .map(FileService.ParamItem::getKey)
                .orElse("");

        String uvod = formData.get("UVOD");
        String uvodText = hlaskyDict.get("UVODY").stream()
                .filter(item -> item.getValues().get(0).equals(uvod))
                .findFirst()
                .map(FileService.ParamItem::getKey)
                .orElse("");

        String lokalita = formData.get("LOKALITA");
        String lokalitaText = hlaskyDict.get("LOKALITY").stream()
                .filter(item -> item.getValues().get(0).equals(lokalita))
                .findFirst()
                .map(FileService.ParamItem::getKey)
                .orElse("");

        String casy = formData.get("CASY");
        String casyText = hlaskyDict.get("CASY").stream()
                .filter(item -> item.getValues().get(0).equals(casy))
                .findFirst()
                .map(FileService.ParamItem::getKey)
                .orElse("");

        String omluvy = formData.get("OMLUVY");
        String omluvyText = hlaskyDict.get("OMLUVY").stream()
                .filter(item -> item.getValues().get(0).equals(omluvy))
                .findFirst()
                .map(FileService.ParamItem::getKey)
                .orElse("");

        String day = formData.get("DAY");
        String hour = formData.get("HOUR");

        StringBuilder message = new StringBuilder("Krizová hláška zapnuta.\n");
        if (!pevnaHlaskaText.isEmpty()) {
            message.append(pevnaHlaskaText).append("\n");
        }
        if (formData.containsKey("Skladana_ANO")) {
            if (!uvodText.isEmpty()) {
                message.append(uvodText).append("\n");
            }
            if (!lokalitaText.isEmpty()) {
                message.append(lokalitaText).append("\n");
            }
            if (!"NONE".equals(casy)) {
                message.append("Předpokládaný čas odstranění závady je: ").append(casyText).append(" ").append(day)
                        .append(" ").append(hour).append(":00h\n");
            }
            if (!omluvyText.isEmpty()) {
                message.append(omluvyText).append("\n");
            }
        }

        message.append("\nTuto změnu provedl: ").append(user);

        return message.toString().replace("--------", "").trim();
    }

}



