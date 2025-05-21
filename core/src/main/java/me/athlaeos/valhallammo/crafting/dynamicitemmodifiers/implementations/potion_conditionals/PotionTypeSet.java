package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.potion_conditionals;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.MinecraftVersion;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.version.ConventionUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.*;

public class PotionTypeSet extends DynamicItemModifier {
    public static final List<PotionType> legalTypes = new ArrayList<>(List.of(PotionType.AWKWARD, PotionType.MUNDANE, PotionType.THICK));
    static {
        if (!MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5)) legalTypes.add(PotionType.valueOf("UNCRAFTABLE"));
    }
    private PotionType potionType = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? null : PotionType.valueOf("UNCRAFTABLE");

    public PotionTypeSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (context.getItem().getMeta() instanceof PotionMeta meta){
            ValhallaMMO.getNms().setPotionType(meta, potionType);
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 12) {
            int currentType = legalTypes.indexOf(potionType);
            if (e.isLeftClick()) {
                if (currentType + 1 >= legalTypes.size()) currentType = 0;
                else currentType++;
            } else {
                if (currentType - 1 < 0) currentType = legalTypes.size() - 1;
                else currentType--;
            }
            potionType = legalTypes.get(currentType);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.POTION)
                        .name("&eWhich potion type should it be?")
                        .lore("&fPotion class set to &e" + potionType,
                                "&fUsed in further condition checking.",
                                "&6Click to cycle")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.POTION).flag(ConventionUtils.getHidePotionEffectsFlag()).get();
    }

    @Override
    public String getDisplayName() {
        return "&dSet Potion Type";
    }

    @Override
    public String getDescription() {
        return "&fChanges the potion type of the potion";
    }

    @Override
    public String getActiveDescription() {
        return "&fPotion type set to &e" + potionType;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.POTION_CONDITIONALS.id());
    }

    @Override
    public DynamicItemModifier copy() {
        PotionTypeSet m = new PotionTypeSet(getName());
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the potion type to set the item to";
        try {
            PotionType type = PotionType.valueOf(args[0]);
            if (legalTypes.contains(type)) potionType = type;
            else return "Invalid potion type";
        } catch (IllegalArgumentException ignored){
            return "Invalid potion type";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return legalTypes.stream().map(PotionType::toString).toList();
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
