package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.localization.TranslationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class StringUtils {

    public static String toPascalCase(String s){
        if (s == null) return null;
        if (s.length() == 0) return s;
        String allLowercase = s.toLowerCase();
        char c = allLowercase.charAt(0);
        return allLowercase.replaceFirst("" + c, "" + Character.toUpperCase(c));
    }

    private final static TreeMap<Integer, String> romanNumeralsMap = new TreeMap<>();

    static {
        romanNumeralsMap.put(1000, "M");
        romanNumeralsMap.put(900, "CM");
        romanNumeralsMap.put(500, "D");
        romanNumeralsMap.put(400, "CD");
        romanNumeralsMap.put(100, "C");
        romanNumeralsMap.put(90, "XC");
        romanNumeralsMap.put(50, "L");
        romanNumeralsMap.put(40, "XL");
        romanNumeralsMap.put(10, "X");
        romanNumeralsMap.put(9, "IX");
        romanNumeralsMap.put(5, "V");
        romanNumeralsMap.put(4, "IV");
        romanNumeralsMap.put(1, "I");
    }

    public static String toRoman(int number) {
        if (number <= 0) return "0";
        if (number == 1) return "I";
        int l = romanNumeralsMap.floorKey(number);
        if (number == l) return romanNumeralsMap.get(number);
        return romanNumeralsMap.get(l) + toRoman(number - l);
    }

    public static Double parseDouble(String s) throws NumberFormatException {
        return Double.parseDouble(s.replace(",", "."));
    }

    public static Float parseFloat(String s) throws NumberFormatException {
        return Float.parseFloat(s.replace(",", "."));
    }

    /**
     * returns a timestamp based on the base amount of x in a second
     * @param ticks ticks
     * @param base base to represent a second (example, if 20 is 1 second give a base of 20, if 1 is 1 second give 1.)
     * @return a timestamp in a hh:mm:ss format (or mm:ss if not enough ticks for an hour are given), or ∞ if ticks is < 0
     */
    public static String toTimeStamp(long ticks, long base){
        if (ticks == 0) return "0:00";
        if (ticks < 0) return "∞";
        int hours = (int) Math.floor(ticks / (3600D * base));
        String hrs = "" + hours;
        ticks %= (base * 3600);
        int minutes = (int) Math.floor(ticks / (60D * base));
        String mins = (hours > 0 ? "0" : "") + minutes;
        ticks %= (base * 60);
        int seconds = (int) Math.floor(ticks / (double) base);
        String secs = (seconds > 9 ? "" : "0") + seconds;

        return hours > 0 ? String.format("%s:%s:%s", hrs, mins, secs) : String.format("%s:%s", mins, secs);
    }

    public static String toTimeStamp2(long ticks, long base){
        return toTimeStamp2(ticks, base, true);
    }

    public static String toTimeStamp2(long ticks, long base, boolean decimal){
        if (ticks < 0) return "∞";
        int days = (int) Math.floor(ticks / (3600D * 24 * base));
        ticks %= (base * (3600 * 24));
        int hours = (int) Math.floor(ticks / (3600D * base));
        ticks %= (base * 3600);
        int minutes = (int) Math.floor(ticks / (60D * base));
        ticks %= (base * 60);
        double seconds = decimal ? ticks / (double)base : (int) Math.floor(ticks / (double) base);

        String format = days > 0 ? TranslationManager.getTranslation("timeformat_days") : hours > 0 ?
                TranslationManager.getTranslation("timeformat_hours") : minutes > 0 ?
                TranslationManager.getTranslation("timeformat_minutes") :
                TranslationManager.getTranslation("timeformat_seconds");
        return format.replace("%days%", "" + days)
                .replace("%hours%", "" + hours)
                .replace("%minutes%", "" + minutes)
                .replace("%seconds%", String.format("%." + (decimal ? 1 : 0) + "f", seconds));
    }

    public static List<String> separateStringIntoLines(String string, int maxLength){
        List<String> lines = new ArrayList<>();
        String[] words = string.split(" ");
        if (words.length == 0) return lines;
        StringBuilder sentence = new StringBuilder(words[0]);
        for (String w : Arrays.copyOfRange(words, 1, words.length)){
            if (sentence.length() + w.length() > maxLength || w.contains("/n")){
                w = w.replace("/n", "");
                lines.add(sentence.toString());
                String previousSentence = sentence.toString();
                sentence = new StringBuilder();
                sentence.append(Utils.chat(org.bukkit.ChatColor.getLastColors(Utils.chat(previousSentence)))).append(w);
            } else {
                sentence.append(" ").append(w);
            }
        }
        lines.add(sentence.toString());
        return lines;
    }

    public static boolean isEmpty(String str){
        return str == null || str.isEmpty();
    }
}
