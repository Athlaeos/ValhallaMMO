package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.potioneffects.PotionEffectRegistry;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ItemType extends DynamicItemModifier implements ResultChangingModifier {
    private Material material = Material.DIAMOND;

    public ItemType(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        context.getItem().type(material);
        PotionEffectRegistry.updateItemName(context.getItem().getMeta(), true, false);
    }

    @Override
    public ItemStack getNewResult(ModifierContext context) {
        return context.getItem().copy().type(material).get();
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            ItemStack cursor = e.getCursor();
            if (!ItemUtils.isEmpty(cursor)) material = cursor.getType();
        }
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(material)
                        .name("&eWhat should the new item type be?")
                        .lore("&6Click with another item to",
                                "&6copy its type over.",
                                "&fSet to " + StringUtils.toPascalCase(material.toString()))
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.SLIME_BALL).get();
    }

    @Override
    public String getDisplayName() {
        return "&aChange Item Type";
    }

    @Override
    public String getDescription() {
        return "&fReplaces the item type with another one";
    }

    @Override
    public String getActiveDescription() {
        return "&fItem type will change to " + StringUtils.toPascalCase((material == null ? Material.BARRIER : material).toString());
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    @Override
    public DynamicItemModifier copy() {
        ItemType m = new ItemType(getName());
        m.setMaterial(this.material);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the item type for replacement, or 'hand' for your current held item type";
        if (args[0].equalsIgnoreCase("hand")) {
            if (!(executor instanceof Player p)) return "This argument requires you to be a player for usage";
            ItemStack held = p.getInventory().getItemInMainHand();
            if (ItemUtils.isEmpty(held)) return "The replace-by type cannot be nothing";
            material = held.getType();
        } else {
            try {
                Material m = Material.valueOf(args[0]);
                if (m.isAir()) return "The replace-by type cannot be nothing";
                material = m;
            } catch (IllegalArgumentException ignored){
                return "A material is required. Invalid material";
            }
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return Arrays.stream(Material.values()).map(Object::toString).collect(Collectors.toList());
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
