package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_conditionals;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.*;

public class PotionTypeForbidden extends DynamicItemModifier {
    public static final List<PotionType> legalTypes = List.of(PotionType.UNCRAFTABLE, PotionType.AWKWARD, PotionType.MUNDANE, PotionType.THICK);
    private final PotionType type;

    public PotionTypeForbidden(String name, PotionType type) {
        super(name);
        this.type = type;
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (validate && outputItem.getMeta() instanceof PotionMeta meta && meta.getBasePotionData().getType() == type){
            failedRecipe(outputItem, TranslationManager.getTranslation("modifier_warning_forbidden_potion_type"));
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new HashMap<>();
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.POTION).flag(ItemFlag.HIDE_POTION_EFFECTS).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Cancel if " + StringUtils.toPascalCase(type.toString().replace("_", " "));
    }

    @Override
    public String getDescription() {
        return "&fCancels recipe if the potion type is " + StringUtils.toPascalCase(type.toString().replace("_", " "));
    }

    @Override
    public String getActiveDescription() {
        return "&fCancels recipe if the potion type is " + StringUtils.toPascalCase(type.toString().replace("_", " "));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_CONDITIONALS.id());
    }

    @Override
    public DynamicItemModifier createNew() {
        return new PotionTypeForbidden(getName(), type);
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
