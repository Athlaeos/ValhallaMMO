package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.item.MiningSpeed;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class MiningHardnessTranslationsAdd extends DynamicItemModifier {
    private final Map<Material, Material> translations = new HashMap<>();
    private Material baseMaterial = Material.DEEPSLATE;
    private Material translationMaterial = Material.STONE;

    public MiningHardnessTranslationsAdd(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        for (Material m : translations.keySet()){
            MiningSpeed.addHardnessTranslation(context.getItem().getMeta(), m, translations.get(m));
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 11){
            if (!ItemUtils.isEmpty(e.getCursor()) && e.getCursor().getType().isBlock()) baseMaterial = e.getCursor().getType();
        } else if (button == 13){
            if (!ItemUtils.isEmpty(e.getCursor()) && e.getCursor().getType().isBlock()) translationMaterial = e.getCursor().getType();
        } else if (button == 17){
            if (e.isShiftClick()) translations.clear();
            else translations.put(baseMaterial, translationMaterial);
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(11,
                new ItemBuilder(baseMaterial)
                        .name("&fSelect Base Material")
                        .lore("&e" + baseMaterial + "&7 is selected",
                                "&fClick with a block in your inventory",
                                "&fto select different material.")
                        .get()).map(Set.of(
                new Pair<>(13,
                        new ItemBuilder(Material.DIAMOND_PICKAXE)
                                .name("&fSelect To Material")
                                .lore("&e" + translationMaterial + "&7 is selected",
                                        "&fClick with a block in your inventory",
                                        "&fto select different material.")
                                .get()),
                new Pair<>(17,
                        new ItemBuilder(Material.STRUCTURE_VOID)
                                .name("&fConfirm Translation")
                                .lore("&f" + baseMaterial + " will be mined at the",
                                        "&fsame speed as " + translationMaterial,
                                        "&6Click to add translation",
                                        "&cShift-Click to clear translations",
                                        "&fCurrent translations:")
                                .appendLore(translations.entrySet().stream().map(e -> "&e" + e.getKey() + " = " + e.getValue()).toList())
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.GOLDEN_PICKAXE).get();
    }

    @Override
    public String getDisplayName() {
        return "&7Mining Hardness Translations";
    }

    @Override
    public String getDescription() {
        return "&fAdd a mining hardness translation to the item, which causes the item to mine a type of block at the same speed as another";
    }

    @Override
    public String getActiveDescription() {
        return "&fAdds the following mining hardness translations to the item: /n&e" +
                (translations.entrySet().stream().map(e -> "&e" + e.getKey() + " = " + e.getValue()))
                .collect(Collectors.joining("/n&e"));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.CUSTOM_ATTRIBUTES.id());
    }

    public void setBaseMaterial(Material baseMaterial) {
        this.baseMaterial = baseMaterial;
    }

    public void setTranslationMaterial(Material translationMaterial) {
        this.translationMaterial = translationMaterial;
    }

    public Map<Material, Material> getTranslations() {
        return translations;
    }

    @Override
    public DynamicItemModifier copy() {
        MiningHardnessTranslationsAdd m = new MiningHardnessTranslationsAdd(getName());
        m.setBaseMaterial(this.baseMaterial);
        m.setTranslationMaterial(this.translationMaterial);
        m.getTranslations().putAll(this.translations);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "Two argument are expected: two materials";
        try {
            Material m = Material.valueOf(args[0]);
            Material t = Material.valueOf(args[1]);
            translations.put(m, t);
        } catch (IllegalArgumentException ignored){
            return "Two argument are expected: two materials. At least one was not a valid argument";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0 || currentArg == 1) return Arrays.stream(Material.values()).map(Object::toString).collect(Collectors.toList());
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 2;
    }
}
