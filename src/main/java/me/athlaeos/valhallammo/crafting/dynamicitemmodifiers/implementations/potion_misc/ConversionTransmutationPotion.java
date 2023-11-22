package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.AlchemySkill;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ConversionTransmutationPotion extends DynamicItemModifier {

    public ConversionTransmutationPotion(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (!SkillRegistry.isRegistered(AlchemySkill.class)) return;
        if (outputItem.getItem().getType() == Material.POTION) outputItem.type(Material.SPLASH_POTION);
        AlchemySkill.setTransmutationPotion(outputItem.getMeta(), true);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) { }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.GOLD_BLOCK).get();
    }

    @Override
    public String getDisplayName() {
        return "&fTransmutation Potion";
    }

    @Override
    public String getDescription() {
        return "&fConverts the item into a splash transmutation potion.";
    }

    @Override
    public String getActiveDescription() {
        return "&fConverts the item into a splash transmutation potion.";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_MISC.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new ConversionTransmutationPotion(getName());
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 0;
    }
}
