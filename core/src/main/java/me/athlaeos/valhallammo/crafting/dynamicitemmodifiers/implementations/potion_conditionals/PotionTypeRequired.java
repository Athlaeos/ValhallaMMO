package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_conditionals;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.*;

public class PotionTypeRequired extends DynamicItemModifier {
    public static final List<PotionType> legalTypes = new ArrayList<>(List.of(PotionType.AWKWARD, PotionType.MUNDANE, PotionType.THICK));
    static {
        if (!MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)) legalTypes.add(PotionType.valueOf("UNCRAFTABLE"));
    }
    private final PotionType type;

    public PotionTypeRequired(String name, PotionType type) {
        super(name);
        this.type = type;
    }

    @Override
    public void processItem(ModifierContext context) {
        if (context.shouldValidate() && context.getItem().getMeta() instanceof PotionMeta meta && ValhallaMMO.getNms().getPotionType(meta) != type){
            failedRecipe(context.getItem(), TranslationManager.getTranslation("modifier_warning_required_potion_type"));
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
        return new ItemBuilder(Material.POTION).flag(ConventionUtils.getHidePotionEffectsFlag()).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Cancel if not " + StringUtils.toPascalCase(type.toString().replace("_", " "));
    }

    @Override
    public String getDescription() {
        return "&fCancels recipe if the potion type isn't " + StringUtils.toPascalCase(type.toString().replace("_", " "));
    }

    @Override
    public String getActiveDescription() {
        return "&fCancels recipe if the potion type isn't " + StringUtils.toPascalCase(type.toString().replace("_", " "));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_CONDITIONALS.id());
    }

    @Override
    public DynamicItemModifier copy() {
        PotionTypeRequired m = new PotionTypeRequired(getName(), type);
        m.setPriority(this.getPriority());
        return m;
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
