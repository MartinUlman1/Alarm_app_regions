package ivr.alarmregions;

import lombok.Data;

@Data
public class MenuEntry {
    private final String path;
    private final String text;
    private final String authority;



    public MenuEntry(String path, String text, String authority) {
        this.path = path;
        this.text = text;
        this.authority = authority;

    }

}
