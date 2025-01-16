package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimeStampAdd extends DynamicItemModifier {
    private static final NamespacedKey TIME_STAMP = new NamespacedKey(ValhallaMMO.getInstance(), "time_created");

    private boolean end = false;
    private boolean addLore = false;

    public TimeStampAdd(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        LocalDateTime date = LocalDateTime.now();
        if (addLore){
            ZoneOffset timeZone = Catch.catchOrElse(() -> ZoneOffset.of(TranslationManager.getTranslation("formatter_time_timezone")), ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TranslationManager.getTranslation("formatter_time"));
            List<String> lore = outputItem.getMeta().getLore() != null ? outputItem.getMeta().getLore() : new ArrayList<>();
            String loreFormat = TranslationManager.getTranslation("format_time");
            if (end || lore.isEmpty()) lore.add(Utils.chat(loreFormat.replace("%time%", date.atOffset(timeZone).format(formatter))));
            else lore.set(0, Utils.chat(loreFormat.replace("%time%", date.atOffset(timeZone).format(formatter))));
            outputItem.lore(lore);
        }
        outputItem.longTag(TIME_STAMP, date.toEpochSecond(ZoneOffset.UTC));
    }

    public static LocalDateTime getTime(ItemMeta i){
        long value = ItemUtils.getPDCLong(TIME_STAMP, i, -1L);
        return value < 0 ? null : LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.UTC);
    }

    public static LocalDateTime getTime(ItemStack i){
        return getTime(ItemUtils.getItemMeta(i));
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11) end = !end;
        else if (button == 13) addLore = !addLore;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(11,
                new ItemBuilder(Material.PAPER)
                        .name("&fOrder: " + (end ? "&eappend to end" : "&eplace at start"))
                        .lore("&eClick to toggle whether you want",
                                "&ethe time stamp to appear at the",
                                "&estart of the item's lore or at",
                                "&eend&f, if lore is enabled")
                        .get()).map(new HashSet<>(Set.of(
                new Pair<>(13,
                        new ItemBuilder(Material.PAPER)
                                .name("&fLore enabled: " + (addLore ? "&eYes" : "&eNo"))
                                .lore("&eClick to toggle whether you want",
                                        "&ethe time stamp to appear in the",
                                        "&elore at all. If not, it is only",
                                        "&eadded as NBT tag for external use")
                                .get())
        )));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.CLOCK).get();
    }

    @Override
    public String getDisplayName() {
        return "&bCreation Timestamp";
    }

    @Override
    public String getDescription() {
        return "&fAdds a timestamp of the current time to the item.";
    }

    @Override
    public String getActiveDescription() {
        return "&fAdds a timestamp of the current time " + (addLore ? "to the " + (end ? "&eend of the&f" : "&estart of the&f") + " item's lore." : "invisibly to the item's meta");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public void setAddLore(boolean addLore) {
        this.addLore = addLore;
    }

    @Override
    public DynamicItemModifier copy() {
        TimeStampAdd m = new TimeStampAdd(getName());
        m.setEnd(this.end);
        m.setPriority(this.getPriority());
        m.setAddLore(this.addLore);
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate if you want the timestamp added at the &estart &cor &eend&c, or &enone";
        addLore = !args[0].equalsIgnoreCase("none");
        end = args[0].equalsIgnoreCase("end");
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("start", "end", "none");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
