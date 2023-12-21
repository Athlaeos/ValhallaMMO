package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerSignatureAdd extends DynamicItemModifier {
    private static final NamespacedKey SIGNATURE = new NamespacedKey(ValhallaMMO.getInstance(), "player_signature");

    private boolean end = false;

    public PlayerSignatureAdd(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        List<String> lore = outputItem.getMeta().getLore() != null ? outputItem.getMeta().getLore() : new ArrayList<>();
        String format = TranslationManager.getTranslation("format_signature");
        if (end || lore.isEmpty()) lore.add(Utils.chat(format.replace("%player%", crafter.getName())));
        else lore.set(0, Utils.chat(format.replace("%player%", crafter.getName())));
        outputItem.lore(lore);
        outputItem.stringTag(SIGNATURE, crafter.getUniqueId().toString());
    }

    public static UUID getSignature(ItemStack i){
        String value = ItemUtils.getPDCString(SIGNATURE, i, null);
        if (StringUtils.isEmpty(value)) return null;
        return UUID.fromString(value);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) end = !end;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.PAPER)
                        .name("&fOrder: " + (end ? "&eappend to end" : "&eplace at start"))
                        .lore("&eClick to toggle whether you want",
                                "&ethe signature to appear at the",
                                "&estart of the item's lore or at",
                                "&eend")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.WRITTEN_BOOK).get();
    }

    @Override
    public String getDisplayName() {
        return "&bPlayer Signature";
    }

    @Override
    public String getDescription() {
        return "&fAdds a player signature to the item's lore.";
    }

    @Override
    public String getActiveDescription() {
        return "&fAdds a player signature to the " + (end ? "&eend of the&f" : "&estart of the&f") + " item's lore.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    @Override
    public DynamicItemModifier copy() {
        PlayerSignatureAdd m = new PlayerSignatureAdd(getName());
        m.setEnd(this.end);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate if you want the signature added at the &estart &cor &eend";
        end = args[0].equalsIgnoreCase("end");
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("start", "end");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
