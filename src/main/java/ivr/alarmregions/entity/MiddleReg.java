package ivr.alarmregions.entity;

import lombok.Data;

import java.util.TreeMap;

@Data
public class MiddleReg {

    public static TreeMap<String, String> fullTextDir;
    public static TreeMap<String, String> valueList;
    public static TreeMap<String, String> uploadable;

    static {
        fullTextDir = new TreeMap<>();
        fullTextDir.put("5", "Libovolný text");
        fullTextDir.put("31", "Libovolný text");
        fullTextDir.put("32", "Libovolný text");
        fullTextDir.put("33", "Libovolný text");
        fullTextDir.put("34", "Libovolný text");
        fullTextDir.put("35", "Libovolný text");
        fullTextDir.put("36", "Libovolný text");
        fullTextDir.put("37", "Libovolný text");
        fullTextDir.put("38", "Libovolný text");
        fullTextDir.put("39", "Libovolný text");
        fullTextDir.put("40", "Libovolný text");
        fullTextDir.put("46", "Libovolný text");
        fullTextDir.put("47", "Libovolný text");
        fullTextDir.put("48", "Libovolný text");
    }

    static {
        valueList = new TreeMap<>();
        valueList.put("0", "Libovolný text");
        valueList.put("5", "Libovolný text");
        valueList.put("31", "Libovolný text");
        valueList.put("32", "Libovolný text");
        valueList.put("33", "Libovolný text");
        valueList.put("34", "Libovolný text");
        valueList.put("35", "Libovolný text");
        valueList.put("36", "Libovolný text");
        valueList.put("37", "Libovolný text");
        valueList.put("38", "Libovolný text");
        valueList.put("39", "Libovolný text");
        valueList.put("40", "Libovolný text");
        valueList.put("46", "Libovolný text");
        valueList.put("47", "Libovolný text");

    }

    static {
        uploadable = new TreeMap<>();
        uploadable.put("MID_2", "39");
    }


}
