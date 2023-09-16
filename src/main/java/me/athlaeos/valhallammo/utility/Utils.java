package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Random random = new Random();

    public static Random getRandom(){
        return random;
    }

    public static boolean withinManhattanRange(Location l1, Location l2, double range){
        return Math.abs(l1.getX() - l2.getX()) <= range &&
                Math.abs(l1.getY() - l2.getY()) <= range &&
                Math.abs(l1.getZ() - l2.getZ()) <= range;
    }

    public static Color hexToRgb(String colorStr) {
        return Color.fromRGB(Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ));
    }

    public static String rgbToHex(int r, int g, int b){
        return String.format("#%02x%02x%02x", r, g, b);
    }

    /**
     * Returns an integer based on the chance given, but always between this chance rounded down and the chance rounded
     * up. Example:
     * a chance of 3.4 will always return at least 3, with a 40% chance to return 4 instead.
     * a chance of 0.9 will have a 90% chance to return 1
     * a chance of 7.5 will always return at least 7, with a 50% chance to return 8 instead.
     * @param chance the average to calculate from
     * @return an integer returning at least the average rounded down, with the remaining chance to return 1 extra
     */
    public static int randomAverage(double chance){
        boolean negative = chance < 0;
        int atLeast = (negative) ? (int) Math.ceil(chance) : (int) Math.floor(chance);
        double remainingChance = chance - atLeast;
        if (getRandom().nextDouble() <= Math.abs(remainingChance)) atLeast += negative ? -1 : 1;
        return atLeast;
    }

    /**
     * Randomly spits out true or false based on chance. <br>
     * If custom_luck is enabled in config.yml, the following rules apply:<br>
     * Positive luck rolls the chance again for a favourable outcome (a 50% chance is converted to 75%, 10% to 19%, etc.)<br>
     * Negative luck rolls the chance again for an unfavourable outcome (a 50% chance is converted to 25%, 10% to 1%, etc.)<br>
     * <br>
     * If ignoreConfig is enabled, the luck mechanic will always come into play.
     * @param chance the chance for a "proc"
     * @param luck the amount of luck to affect the proc chance
     * @param ignoreConfig true if you want to calculate WITH luck, false if you want the config.yml value to decide that
     * @return true if procced, false if not
     */
    public static boolean proc(double chance, double luck, boolean ignoreConfig){
        if (luck == 0 || (!ignoreConfig && !ValhallaMMO.getPluginConfig().getBoolean("custom_luck", true))) return random.nextDouble() <= chance;
        if (chance >= 1) return true;
        if (chance <= 0) return false;
        if (luck > 0)
            return random.nextDouble() <= 1 - Math.pow(1 - chance, luck + 1);
        else
            return random.nextDouble() <= Math.pow(chance, -luck + 1);
    }

    /**
     * Does what {@link Utils#proc(double, double, boolean)} does, except it fetches the luck stat from said entity.
     * @param e the entity to gather their luck stat from
     * @param chance the chance for a "proc"
     * @param ignoreConfig true if you want to calculate WITH luck, false if you want the config.yml value to decide that
     * @return true if procced, false if not
     */
    public static boolean proc(LivingEntity e, double chance, boolean ignoreConfig){
        double luck = AccumulativeStatManager.getCachedStats("LUCK", e, 10000, true);
        return proc(chance, luck, ignoreConfig);
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

    public static void sendActionBar(Player whomst, String message){
        if (!StringUtils.isEmpty(message)) whomst.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(chat(message)));
    }
}
