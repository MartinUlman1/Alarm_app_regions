package ivr.alarmregions.controller;


import ivr.alarmregions.Menu;
import ivr.alarmregions.MenuEntry;
import org.springframework.web.bind.annotation.ModelAttribute;

public abstract class AbstractController {

    @ModelAttribute("globalMenu")
    public Menu globalMenu() {
        Menu result = new Menu();
        result.put("/region/praha", new MenuEntry(PragueController.PATH, "Praha",
                PragueController.AUTHORITY));
        result.put("/region/stredocesky", new MenuEntry(RegionsController.PATH, "Středočeský kraj",
                RegionsController.AUTHORITY));
        result.put("/region/karlovarsky", new MenuEntry(RegionsController.PATH1, "Karlovarský kraj",
                RegionsController.AUTHORITY1));
        result.put("/region/kralovehradecky", new MenuEntry(RegionsController.PATH2, "Králové hradecký kraj",
                RegionsController.AUTHORITY2));
        return result;
    }
}
