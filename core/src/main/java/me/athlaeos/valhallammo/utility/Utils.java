package me.athlaeos.valhallammo.utility;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Weighted;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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

    public static int getManhattanDistance(Location l1, Location l2){
        return Math.abs(l1.getBlockX() - l2.getBlockX()) +
                Math.abs(l1.getBlockY() - l2.getBlockY()) +
                Math.abs(l1.getBlockZ() - l2.getBlockZ());
    }

    public static int getManhattanDistance(int x1, int z1, int x2, int z2){
        return Math.abs(x1 - x2) + Math.abs(z1 - z2);
    }

    public static Map<String, OfflinePlayer> getPlayersFromUUIDs(Collection<UUID> uuids){
        Map<String, OfflinePlayer> players = new HashMap<>();
        for (UUID uuid : uuids){
            OfflinePlayer player = ValhallaMMO.getInstance().getServer().getOfflinePlayer(uuid);
            players.put(player.getName(), player);
        }
        return players;
    }

    public static Map<String, Player> getOnlinePlayersFromUUIDs(Collection<UUID> uuids){
        Map<String, Player> players = new HashMap<>();
        for (UUID uuid : uuids){
            Player player = ValhallaMMO.getInstance().getServer().getPlayer(uuid);
            if (player != null) players.put(player.getName(), player);
        }
        return players;
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
     * This method assumes a <i>true</i> outcome has a positive effect.<br>
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
            return random.nextDouble() <= 1 - MathUtils.pow(1 - chance, luck + 1);
        else
            return random.nextDouble() <= MathUtils.pow(chance, -luck + 1);
    }


    /**
     * Randomly spits out true or false based on chance. <br>
     * This method assumes a <i>true</i> outcome has a negative effect.<br>
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
    public static boolean badProc(double chance, double luck, boolean ignoreConfig){
        return proc(chance, -luck, ignoreConfig);
    }

    /**
     * Does what {@link Utils#proc(double, double, boolean)} does, except it fetches the luck stat from said entity.
     * @param e the entity to gather their luck stat from
     * @param chance the chance for a "proc"
     * @param ignoreConfig true if you want to calculate WITH luck, false if you want the config.yml value to decide that
     * @return true if procced, false if not
     */
    public static boolean proc(LivingEntity e, double chance, boolean ignoreConfig){
        AttributeInstance luckInstance = e.getAttribute(Attribute.GENERIC_LUCK);
        return proc(chance, luckInstance == null ? 0 : luckInstance.getValue(), ignoreConfig);
    }

    /**
     * Does what {@link Utils#badProc(double, double, boolean)} does, except it fetches the luck stat from said entity.
     * @param e the entity to gather their luck stat from
     * @param chance the chance for a "proc"
     * @param ignoreConfig true if you want to calculate WITH luck, false if you want the config.yml value to decide that
     * @return true if procced, false if not
     */
    public static boolean badProc(LivingEntity e, double chance, boolean ignoreConfig){
        AttributeInstance luckInstance = e.getAttribute(Attribute.GENERIC_LUCK);
        return badProc(chance, luckInstance == null ? 0 : luckInstance.getValue(), ignoreConfig);
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
    private static final MathEval math = new MathEval();
    public static double eval(String expression) {
        expression = expression.replaceAll(",", ".");
        if (evalCache.containsKey(expression)) {
            return evalCache.get(expression);
        }
//        String str = expression
//                .replaceAll(",", ".")
//                .replace("$pi", String.format("%.15f", Math.PI))
//                .replace("$e", String.format("%.15f", Math.E))
//                .replaceAll("[^A-Za-z0-9.^*/+() -]+", "");
//        if (str.length() <= 0) return 0;
//        double result = new Object() {
//            int pos = -1, ch;
//
//            void nextChar() {
//                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
//            }
//
//            boolean eat(int charToEat) {
//                while (ch == ' ') nextChar();
//                if (ch == charToEat) {
//                    nextChar();
//                    return true;
//                }
//                return false;
//            }
//
//            double parse() {
//                nextChar();
//                double x = parseExpression();
//                if (pos < str.length()) {
//                    throw new RuntimeException("Unexpected: " + (char)ch + " while trying to parse formula " + expression);
//                }
//                return x;
//            }
//
//            // Grammar:
//            // expression = term | expression `+` term | expression `-` term
//            // term = factor | term `*` factor | term `/` factor
//            // factor = `+` factor | `-` factor | `(` expression `)`
//            //        | number | functionName factor | factor `^` factor
//
//            double parseExpression() {
//                double x = parseTerm();
//                for (;;) {
//                    if      (eat('+')) x += parseTerm(); // addition
//                    else if (eat('-')) x -= parseTerm(); // subtraction
//                    else return x;
//                }
//            }
//
//            double parseTerm() {
//                double x = parseFactor();
//                for (;;) {
//                    if      (eat('*')) x *= parseFactor(); // multiplication
//                    else if (eat('/')) x /= parseFactor(); // division
//                    else return x;
//                }
//            }
//
//            double parseFactor() {
//                if (eat('+')) return parseFactor(); // unary plus
//                if (eat('-')) return -parseFactor(); // unary minus
//
//                double x;
//                int startPos = this.pos;
//                if (eat('(')) { // parentheses
//                    x = parseExpression();
//                    eat(')');
//                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
//                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
//                    x = Double.parseDouble(str.substring(startPos, this.pos));
//                } else if (ch >= 'a' && ch <= 'z') { // functions
//                    while (ch >= 'a' && ch <= 'z') nextChar();
//                    String func = str.substring(startPos, this.pos);
//                    x = parseFactor();
//                    x = switch (func) {
//                        case "sqrt" -> MathUtils.sqrt(x);
//                        case "sin" -> MathUtils.sin(MathUtils.toRadians(x));
//                        case "cos" -> MathUtils.cos(MathUtils.toRadians(x));
//                        case "tan" -> MathUtils.tan(MathUtils.toRadians(x));
//                        default ->
//                                throw new RuntimeException("Unknown function: " + func + " while trying to parse formula " + expression);
//                    };
//                } else {
//                    throw new RuntimeException("Unexpected: " + (char)ch + " while trying to parse formula " + expression);
//                }
//
//                if (eat('^')) x = MathUtils.pow(x, parseFactor()); // exponentiation
//
//                return x;
//            }
//        }.parse();
        double result = math.evaluate(expression);
        evalCache.put(expression, result);
        return result;
    }

    static final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static List<String> chat(List<String> messages){
        if (messages == null) return new ArrayList<>();
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
        if (!StringUtils.isEmpty(message)) {
            if (message.startsWith("ACTIONBAR") && whomst instanceof Player p) {
                sendActionBar(p, message.replaceFirst("ACTIONBAR", ""));
            } else if (message.startsWith("TITLE") && whomst instanceof Player p){
                String title = message.replaceFirst("TITLE", "");
                String subtitle = "";
                int titleDuration = 40;
                int fadeDuration = 5;
                String subString = StringUtils.substringBetween(message, "TITLE(", ")");
                if (subString != null){
                    String[] args = subString.split(";");
                    if (args.length > 0) title = args[0];
                    if (args.length > 1) subtitle = args[1];
                    if (args.length > 2) titleDuration = Catch.catchOrElse(() -> Integer.parseInt(args[2]), 100);
                    if (args.length > 3) fadeDuration = Catch.catchOrElse(() -> Integer.parseInt(args[2]), 10);
                }
                sendTitle(p, title, subtitle, titleDuration, fadeDuration);
            } else {
                whomst.sendMessage(chat(message));
            }
        }
    }

    public static void sendActionBar(Player whomst, String message){
        if (!StringUtils.isEmpty(message)) whomst.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(chat(message)));
    }

    public static void sendTitle(Player whomst, String title, String subtitle, int duration, int fade){
        if (!StringUtils.isEmpty(title)) whomst.sendTitle(chat(title), chat(subtitle), fade, duration, fade);
    }

    public static <T extends Weighted> List<T> weightedSelection(Collection<T> entries, int rolls, double luck){
        // weighted selection
        double totalWeight = 0;
        List<T> selectedEntries = new ArrayList<>();
        if (entries.isEmpty()) return selectedEntries;
        List<Pair<T, Double>> totalEntries = new ArrayList<>();
        for (T entry : entries){
            totalWeight += entry.getWeight(luck);
            totalEntries.add(new Pair<>(entry, totalWeight));
        }

        for (int i = 0; i < rolls; i++){
            double random = Utils.getRandom().nextDouble() * totalWeight;
            for (Pair<T, Double> pair : totalEntries){
                if (pair.getTwo() >= random) {
                    selectedEntries.add(pair.getOne());
                    break;
                }
            }
        }
        return selectedEntries;
    }
}
