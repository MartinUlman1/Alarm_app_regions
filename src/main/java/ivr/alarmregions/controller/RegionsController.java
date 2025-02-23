package ivr.alarmregions.controller;

import ivr.alarmregions.authentication.SecurityUtil;
import ivr.alarmregions.entity.KarlovarReg;
import ivr.alarmregions.entity.KralHradReg;
import ivr.alarmregions.entity.MiddleReg;
import ivr.alarmregions.entity.Regions;
import ivr.alarmregions.service.EmailService;
import ivr.alarmregions.service.LogService;
import ivr.alarmregions.service.RegionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;


@Controller
@Slf4j
public class RegionsController extends AbstractController {

    @Value("${spring.mail.sender}")
    private String sender;

    @Value("${spring.mail.recipient.region}")
    private String recipient;

    @Value("${app.department}")
    private String department;

    @Value("${app.uploadVoice}")
    private String uploadVoice;

    @Value("${audio.path}")
    private String audiopath;

    @Autowired
    private LogService logService;

    @Autowired
    private RegionsService regionsService;

    @Autowired
    private EmailService emailService;

    public static final String PATH = "/region/stredocesky";
    public static final String AUTHORITY = "STREDOCESKY";
    public static final String PATH1 = "/region/karlovarsky";
    public static final String AUTHORITY1 = "KARLOVARSKY";
    public static final String PATH2 = "/region/kralovehradecky";
    public static final String AUTHORITY2 = "KRALOVEHRADECKY";

    @GetMapping(PATH)
    public String getStred(Model model) {
        model.addAttribute("valueList", MiddleReg.valueList);
        model.addAttribute("valueText", MiddleReg.fullTextDir);
        model.addAttribute("uploadValue", MiddleReg.uploadable);
        List<Regions> ivrParams = regionsService.ivrRegForStredocesky();
        model.addAttribute("ivrParms", ivrParams);
        model.addAttribute("department", department);
        model.addAttribute("uploadVoice", uploadVoice);

        model.addAttribute("activatedValueForStred1", ivrParams.stream().filter(b -> b.getIvrRegValue()
                .equalsIgnoreCase("STREDOCESKY_1")).findFirst().get().getIvrRegPlace());
        model.addAttribute("activatedValueForStred2", ivrParams.stream().filter(b -> b.getIvrRegValue()
                .equalsIgnoreCase("STREDOCESKY_2")).findFirst().get().getIvrRegPlace());
        model.addAttribute("activatedValueForStred3", ivrParams.stream().filter(b -> b.getIvrRegValue()
                .equalsIgnoreCase("STREDOCESKY_3")).findFirst().get().getIvrRegPlace());

        return "stredocesky";
    }

    @PostMapping("/region/stredocesky/storestreddata")
    public String storeStredData(@RequestParam String ivrReg, @RequestParam String ivrValue, Model model,
                                    RedirectAttributes redirectAttributes) {

        String user = SecurityUtil.getLoggedInUserName();

        Regions regions = new Regions();
        regions.setIvrRegValue(ivrReg);
        regions.setIvrRegPlace(ivrValue);

        regionsService.updateOrInsertIvrParmKhc(ivrReg, ivrValue);
        emailService.sendEmailReg(sender, recipient, "Změna ve středočeském kraji", user, "STŘEDOČESKÝ" + department);

        List<Regions> ivrParams = regionsService.fetchSpecificIvrRegions(ivrReg, ivrValue);
        model.addAttribute("ivrParms", ivrParams);

        String textValue = MiddleReg.valueList.get(ivrValue);
        String message = "Byla požadována změna " + ivrReg + " = " + textValue;
        redirectAttributes.addAttribute("message", message);

        regionsService.reloadConfiguration();
        logService.logAction("stredocesky", user + " mění hlášku " + ivrReg + " na hodnotu: " + ivrValue);
        return "redirect:/region/stredocesky";
    }

    @PostMapping("/region/stredocesky//upload")
    public String stredSubmit(@RequestParam("file") MultipartFile file, @RequestParam("pozadavek") String pozadavek,
                             @RequestParam("speechfile") String speechfile, Model model, RedirectAttributes redirectAttributes) {
        String user = SecurityUtil.getLoggedInUserName();

        String originalFilename = file.getOriginalFilename();

        try {
            String tempFilePath = String.valueOf(Paths.get(System.getProperty("java.io.tmpdir"), speechfile + ".wav"));

            file.transferTo(new java.io.File(tempFilePath));

            java.io.File dir = new java.io.File(audiopath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String outputFilePath = audiopath + "/" + speechfile + ".wav";
            regionsService.convertAudioFile(tempFilePath, outputFilePath);

            Regions regions = new Regions();
            regions.setIvrRegValue(pozadavek);
            regions.setIvrRegPlace(speechfile);
            regionsService.updateOrInsertIvrParmKhc(regions.getIvrRegValue(), regions.getIvrRegPlace());

            redirectAttributes.addAttribute("message", regionsService.confirmUploadFile(originalFilename, user, pozadavek));

            logService.logAction("stredocesky", "Požadavek: " + pozadavek + " vydán uživatelem: " + user +
                    " soubor: " + speechfile + " nahrán a konvertován");
            emailService.sendEmailReg(sender, recipient, "Změna ve středočeském kraji", user, "STŘEDOČESKÝ" + department);
            regionsService.reloadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("message", "Chyba při nahrávání souboru.");
        }

        return "redirect:/region/stredocesky";
    }

    @GetMapping(PATH1)
    public String getKarlovarsky(Model model) {
        model.addAttribute("valueList", KarlovarReg.valueList);
        model.addAttribute("valueText", KarlovarReg.fullTextDir);
        model.addAttribute("uploadValue", KarlovarReg.uploadable);
        List<Regions> ivrParams = regionsService.ivrRegForKarlovarsky();
        model.addAttribute("ivrParms", ivrParams);
        model.addAttribute("department", department);
        model.addAttribute("uploadVoice", uploadVoice);

        model.addAttribute("activatedValueForKar1", ivrParams.stream().filter(b -> b.getIvrRegValue()
                .equalsIgnoreCase("KARLOVARSKY_1")).findFirst().get().getIvrRegPlace());
        model.addAttribute("activatedValueForKar2", ivrParams.stream().filter(b -> b.getIvrRegValue()
                .equalsIgnoreCase("KARLOVARSKY_2")).findFirst().get().getIvrRegPlace());

        return "karlovarsky";
    }

    @PostMapping("/region/karlovarsky/storekarlovdata")
    public String storeKarlovarskyData(@RequestParam String ivrReg, @RequestParam String ivrValue, Model model,
                                   RedirectAttributes redirectAttributes) {

        String user = SecurityUtil.getLoggedInUserName();

        Regions regions = new Regions();
        regions.setIvrRegValue(ivrReg);
        regions.setIvrRegPlace(ivrValue);

        regionsService.updateOrInsertIvrParmKhc(ivrReg, ivrValue);
        emailService.sendEmailReg(sender, recipient, "Změna v Karlovarském kraji", user, "KARLOVARSKY" + department);

        List<Regions> ivrParams = regionsService.fetchSpecificIvrRegions(ivrReg, ivrValue);
        model.addAttribute("ivrParms", ivrParams);

        String textValue = KarlovarReg.valueList.get(ivrValue);
        String message = "Byla požadována změna " + ivrReg + " = " + textValue;
        redirectAttributes.addAttribute("message", message);

        logService.logAction("karlovarsky", user + " mění hlášku " + ivrReg + " na hodnotu: " + ivrValue);
        regionsService.reloadConfiguration();
        return "redirect:/region/karlovarsky";
    }

    @PostMapping("/region/karlovarsky/upload")
    public String karlovarskySubmit(@RequestParam("file") MultipartFile file, @RequestParam("pozadavek") String pozadavek,
                                @RequestParam("speechfile") String speechfile, Model model, RedirectAttributes redirectAttributes) {
        String user = SecurityUtil.getLoggedInUserName();

        String originalFilename = file.getOriginalFilename();

        try {
            String tempFilePath = String.valueOf(Paths.get(System.getProperty("java.io.tmpdir"), speechfile + ".wav"));

            file.transferTo(new java.io.File(tempFilePath));

            java.io.File dir = new java.io.File(audiopath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String outputFilePath = audiopath + "/" + speechfile + ".wav";
            regionsService.convertAudioFile(tempFilePath, outputFilePath);

            Regions regions = new Regions();
            regions.setIvrRegValue(pozadavek);
            regions.setIvrRegPlace(speechfile);
            regionsService.updateOrInsertIvrParmKhc(regions.getIvrRegValue(), regions.getIvrRegPlace());

            redirectAttributes.addAttribute("message", regionsService.confirmUploadFile(originalFilename, user, pozadavek));

            logService.logAction("karlovarsky", "Požadavek: " + pozadavek + " vydán uživatelem: " + user +
                    " soubor: " + speechfile + " nahrán a konvertován");
            emailService.sendEmailReg(sender, recipient, "Změna v Karlovarském kraji", user, "KARLOVARSKY" + department);
            regionsService.reloadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("message", "Chyba při nahrávání souboru.");
        }

        return "redirect:/region/karlovarsky";
    }

    @GetMapping(PATH2)
    public String getKraloveHradecky(Model model) {
        model.addAttribute("valueList", KralHradReg.valueList);
        model.addAttribute("valueText", KralHradReg.fullTextDir);
        model.addAttribute("uploadValue", KralHradReg.uploadable);
        List<Regions> ivrParams = regionsService.ivrRegForKralovHrad();
        model.addAttribute("ivrParms", ivrParams);
        model.addAttribute("department", department);
        model.addAttribute("uploadVoice", uploadVoice);

        model.addAttribute("activatedValueForKralHrad1", ivrParams.stream().filter(b -> b.getIvrRegValue()
                .equalsIgnoreCase("KRALOVEHRADECKY_1")).findFirst().get().getIvrRegPlace());
        model.addAttribute("activatedValueForKralHrad2", ivrParams.stream().filter(b -> b.getIvrRegValue()
                .equalsIgnoreCase("KRALOVEHRADECKY_2")).findFirst().get().getIvrRegPlace());

        return "kralovehradecky";
    }

    @PostMapping("/region/kralovehradecky/storekralovehradeckydata")
    public String storeKraloveHradeckyData(@RequestParam String ivrReg, @RequestParam String ivrValue, Model model,
                               RedirectAttributes redirectAttributes) {

        String user = SecurityUtil.getLoggedInUserName();

        Regions regions = new Regions();
        regions.setIvrRegValue(ivrReg);
        regions.setIvrRegPlace(ivrValue);

        regionsService.updateOrInsertIvrParmKhc(ivrReg, ivrValue);
        emailService.sendEmailReg(sender, recipient, "Změna v Královéhradeckém kraji", user, "KRALOVEHRADECKY" + department);

        List<Regions> ivrParams = regionsService.fetchSpecificIvrRegions(ivrReg, ivrValue);
        model.addAttribute("ivrParms", ivrParams);

        String textValue = KralHradReg.valueList.get(ivrValue);
        String message = "Byla požadována změna " + ivrReg + " = " + textValue;
        redirectAttributes.addAttribute("message", message);
        logService.logAction("kralovehradecky", user + " mění hlášku " + ivrReg + " na hodnotu: " + ivrValue);
        regionsService.reloadConfiguration();

        return "redirect:/region/kralovehradecky";
    }


    @PostMapping("/region/kralovehradecky/upload")
    public String kraloveHradeckySubmit(@RequestParam("file") MultipartFile file, @RequestParam("pozadavek") String pozadavek,
                            @RequestParam("speechfile") String speechfile, Model model, RedirectAttributes redirectAttributes) {
        String user = SecurityUtil.getLoggedInUserName();

        String originalFilename = file.getOriginalFilename();

        try {
            String tempFilePath = String.valueOf(Paths.get(System.getProperty("java.io.tmpdir"), speechfile + ".wav"));

            file.transferTo(new java.io.File(tempFilePath));

            java.io.File dir = new java.io.File(audiopath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String outputFilePath = audiopath + "/" + speechfile + ".wav";
            regionsService.convertAudioFile(tempFilePath, outputFilePath);

            Regions regions = new Regions();
            regions.setIvrRegValue(pozadavek);
            regions.setIvrRegPlace(speechfile);
            regionsService.updateOrInsertIvrParmKhc(regions.getIvrRegValue(), regions.getIvrRegPlace());

            redirectAttributes.addAttribute("message", regionsService.confirmUploadFile(originalFilename, user, pozadavek));

            logService.logAction("kralovehradecky", "Požadavek: " + pozadavek + " vydán uživatelem: " + user +
                    " soubor: " + speechfile + " nahrán a konvertován");
            emailService.sendEmailReg(sender, recipient, "Změna v Královéhradeckém kraji", user, "KRALOVEHRADECKY" + department);
            regionsService.reloadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("message", "Chyba při nahrávání souboru.");
        }

        return "redirect:/region/kralovehradecky";
    }

}


