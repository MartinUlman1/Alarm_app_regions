package ivr.alarmregions.controller;

import ivr.alarmregions.authentication.SecurityUtil;
import ivr.alarmregions.entity.PragueReg;
import ivr.alarmregions.service.EmailService;
import ivr.alarmregions.service.FileService;
import ivr.alarmregions.service.PragueService;
import ivr.alarmregions.service.RegionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@SessionAttributes("message")
public class PragueController extends AbstractController {

    @Value("${audio.path}")
    private String audioPath;

    @Value("${spring.mail.sender}")
    private String sender;

    @Value("${spring.mail.recipient.file}")
    private String recipient;

    @Value("${app.department}")
    private String department;

    @Value("${app.uploadVoice}")
    private String uploadVoice;

    @Autowired
    private PragueService pragueService;

    @Autowired
    private RegionsService regionsService;

    @Autowired
    private EmailService emailService;

    private final FileService fileService;

    public static final String PATH = "/region/praha";
    public static final String AUTHORITY = "PRAHA";

    public PragueController(FileService fileService) throws IOException {
        this.fileService = new FileService("praguefile.dat");
    }

    @GetMapping(PATH)
    public String getPrague(Model model) {
        Map<String, List<FileService.ParamItem>> hlaskyDict = fileService.getHlaskyDict();
        List<FileService.ParamItem> pevneHlasky = hlaskyDict.get("PEVNE");
        List<FileService.ParamItem> uvody = hlaskyDict.get("UVODY");
        List<FileService.ParamItem> lokality = hlaskyDict.get("LOKALITY");
        List<FileService.ParamItem> casy = hlaskyDict.get("CASY");
        List<FileService.ParamItem> omluvy = hlaskyDict.get("OMLUVY");
        model.addAttribute("hlaskyDict", hlaskyDict);
        model.addAttribute("pevneHlasky", pevneHlasky);
        model.addAttribute("uvody", uvody);
        model.addAttribute("lokality", lokality);
        model.addAttribute("casy", casy);
        model.addAttribute("dates", pragueService.generateDayOptions());
        model.addAttribute("hours", pragueService.generateHourOptions());
        model.addAttribute("omluvy", omluvy);
        model.addAttribute("reverseHlasky", fileService.getReverseHlasky());
        String message = pragueService.retrieveMessage("PRAGUE");
        model.addAttribute("message", message);
        String login = SecurityUtil.getLoggedInUserName();
        model.addAttribute("login", login);
        model.addAttribute("department", department);
        model.addAttribute("uploadVoice", uploadVoice);
        return "praha";
    }


    @PostMapping("/region/praha/processform")
    public String PragueForm(@RequestParam HashMap<String, String> formData, RedirectAttributes redirectAttributes) {
        String minulyStav = pragueService.retrieveData("PRAGUE");
        String user = SecurityUtil.getLoggedInUserName();
        formData.forEach((key, value) -> {
            log.info("form data {} {}", key, value);
        });
        String stringProIvr = pragueService.constructStringProIvr(formData);
        log.info("Storing data: " + stringProIvr + ", " + user);
        pragueService.storeData(stringProIvr, user, "PRAGUE");

        Map<String, List<FileService.ParamItem>> hlaskyDict = fileService.getHlaskyDict();
        String message = pragueService.constructMessageForPrague(formData, hlaskyDict, user);
        pragueService.storeMessage("PRAGUE", message);

        redirectAttributes.addAttribute("message", message);
        String soucasnyStav = pragueService.retrieveData("PRAGUE");
        emailService.sendEmailPrague(sender,recipient, soucasnyStav, minulyStav, "FIX", department);
        regionsService.reloadConfiguration();
        return "redirect:/regions/praha";
    }

    @PostMapping("/region/praha/deletedata")
    public String pragueDeleteData(@RequestParam String ivrParmValue, RedirectAttributes redirectAttributes) {
        String minulyStav = pragueService.retrieveData("PRAGUE");
        String user = SecurityUtil.getLoggedInUserName();
        PragueReg pragueReg = new PragueReg();
        pragueReg.setIvrPragueReg("PRAGUE");
        pragueReg.setIvrPragueValue(ivrParmValue);
        pragueService.deleteData(pragueReg.getIvrPragueValue(), user, pragueReg.getIvrPragueReg());
        StringBuilder message = new StringBuilder("Krizová hláška vypnuta.\n");
        message.append("\nTuto změnu provedl: ").append(user);
        pragueService.storeMessage("PRAGUE", message.toString());

        redirectAttributes.addAttribute("message", message);
        String soucasnyStav = pragueService.retrieveData("PRAGUE");
        emailService.sendEmailPrague(sender,recipient, soucasnyStav, minulyStav, "FIX", department);
        regionsService.reloadConfiguration();
        return "redirect:/region/praha";
    }

    @PostMapping("/region/praha/upload")
    public String pragueUploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes,
                                 @RequestParam("pozadavek") String pozadavek) {
        String minulyStav = pragueService.retrieveData("PRAGUE");
        String user = SecurityUtil.getLoggedInUserName();
        String originalFilename = file.getOriginalFilename();


        try {
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + originalFilename;
            file.transferTo(new java.io.File(tempFilePath));

            String speechfileDir = audioPath + "/" + "PRAGUE" + "/" + pozadavek + ".wav";
            java.io.File dir = new java.io.File(speechfileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String outputFilePath = speechfileDir + "/" + originalFilename;
            regionsService.convertAudioFile(tempFilePath, outputFilePath);

            PragueReg pragueReg = new PragueReg();
            pragueReg.setIvrPragueReg("PRAGUE");
            pragueReg.setIvrPragueValue(pozadavek);
            pragueService.storeDataUploadFile(user, pragueReg.getIvrPragueReg());

            StringBuilder message = new StringBuilder("Krizová hláška zapnuta.\n");
            message.append("\nSPECIÁLNÍ uploadovaná hláška\n");
            message.append("\nTuto změnu provedl: ").append(user);
            pragueService.storeMessage("PRAGUE", message.toString());

            redirectAttributes.addAttribute("message", message);
            String soucasnyStav = pragueService.retrieveData("PRAGUE");
            emailService.sendEmailPrague(sender,recipient, soucasnyStav, minulyStav, "PRAGUE", department);
            regionsService.reloadConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("message", "Chyba při nahrávání souboru.");
        }

        return "redirect:/region/praha";

    }

    @ModelAttribute("message")
    public String getMessage() {
        return pragueService.retrieveMessage("PRAGUE");
    }
}