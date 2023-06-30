package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.localization.TranslationManager;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static Random random = null;

    public static Random getRandom(){
        if (random == null) random = new Random();
        return random;
    }

    public static boolean withinManhattanRange(Location l1, Location l2, double range){
        return Math.abs(l1.getX() - l2.getX()) <= range &&
                Math.abs(l1.getY() - l2.getY()) <= range &&
                Math.abs(l1.getZ() - l2.getZ()) <= range;
    }

    /**
     * Returns a collection of players from the given selector.
     * Returns a collection with a single player if no selector was used.
     * @throws IllegalArgumentException if an invalid selector was used
     * @param source the command sender that attempts the selector
     * @param selector the selector string
     * @return a collection of matching players, or single player if a single online player was given
     */
    public static Collection<Player> selectPlayers(CommandSender source, String selector) throws IllegalArgumentException{
        Collection<Player> targets = new HashSet<>();
        if (selector.startsWith("@")){
            for (Entity part : Bukkit.selectEntities(source, selector)){
                if (part instanceof Player)
                    targets.add((Player) part);
            }
        } else {
            Player target = ValhallaMMO.getInstance().getServer().getPlayer(selector);
            if (target != null) targets.add(target);
        }
        return targets;
    }

    public static double round6Decimals(double d){
        return (double) Math.round(d * 1000000d) / 1000000d;
    }

    private static final Map<String, Double> evalCache = new HashMap<>();
    public static double eval(String expression) {
        if (evalCache.containsKey(expression)) return evalCache.get(expression);
        String str = expression
                .replaceAll(",", ".")
                .replace("$pi", String.format("%.15f", Math.PI))
                .replace("$e", String.format("%.15f", Math.E))
                .replaceAll("[^A-Za-z0-9.^*/+()-]+", "");
        if (str.length() <= 0) return 0;
        double result = new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) {
                    throw new RuntimeException("Unexpected: " + (char)ch + " while trying to parse formula " + expression);
                }
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    x = switch (func) {
                        case "sqrt" -> Math.sqrt(x);
                        case "sin" -> Math.sin(Math.toRadians(x));
                        case "cos" -> Math.cos(Math.toRadians(x));
                        case "tan" -> Math.tan(Math.toRadians(x));
                        default ->
                                throw new RuntimeException("Unknown function: " + func + " while trying to parse formula " + expression);
                    };
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch + " while trying to parse formula " + expression);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
        evalCache.put(expression, result);
        return result;
    }

    static final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static List<String> chat(List<String> messages){
        return messages.stream().map(Utils::chat).toList();
    }

    /**
     * Converts all color codes to ChatColor. Works with hex codes.
     * Hex code format is triggered with &#123456
     * @param message the message to convert
     * @return the converted message
     */
    public static String chat(String message) {
        if (StringUtils.isEmpty(message)) return "";
        char COLOR_CHAR = ChatColor.COLOR_CHAR;
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static <T> Map<Integer, List<T>> paginate(int pageSize, List<T> allEntries) {
        Map<Integer, List<T>> pages = new HashMap<>();

        int maxPages = (int) Math.ceil((double) allEntries.size() / (double) pageSize);
        for (int pageNumber = 0; pageNumber < maxPages; pageNumber++) {
            pages.put(pageNumber, allEntries.subList( // sublist from start of page to start of next page
                    Math.min(pageNumber * pageSize, allEntries.size()),
                    Math.min((pageNumber + 1) * pageSize, allEntries.size())
            ));
        }

        return pages;
    }

    /**
     * Sends a message to the CommandSender, but only if the message isn't null or empty
     * @param whomst the CommandSender whomst to message
     * @param message the message to send
     */
    public static void sendMessage(CommandSender whomst, String message){
        if (!StringUtils.isEmpty(message)) whomst.sendMessage(chat(message));
    }
}
