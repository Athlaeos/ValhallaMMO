package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetModifiersMenu;
import me.athlaeos.valhallammo.gui.SetRecipeOptionMenu;
import me.athlaeos.valhallammo.gui.implementations.DynamicModifierMenu;
import me.athlaeos.valhallammo.gui.implementations.RecipeOptionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.services.service_implementations.UpgradeService;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpgradingServiceConfigurationMenu extends SimpleConfigurationMenu<UpgradeService> implements SetRecipeOptionMenu, SetModifiersMenu {
    private boolean ingredientChoiceOnInput = false;
    private MerchantLevel currentSelectedLevel = MerchantLevel.NOVICE;
    public UpgradingServiceConfigurationMenu(PlayerMenuUtility playerMenuUtility, Menu previousMenu, UpgradeService thingyToConfigure) {
        super(playerMenuUtility, previousMenu, thingyToConfigure, new ArrayList<>());

        ItemStack ingredientOptionsButton = new ItemBuilder(getButtonData("editor_recipe_cooking_recipeoptions", Material.WRITABLE_BOOK))
                .name("&bIngredient Options")
                .lore("&7Ingredient options are ingredient",
                        "&7flags you can put on an ingredient",
                        "&7to change its behavior during crafting.",
                        "&eClick to open the menu").get();
        addButton(new Button(3, () -> ingredientOptionsButton,
                (service, event) -> {
                    ingredientChoiceOnInput = true;
                    playerMenuUtility.setPreviousMenu(this);
                    new RecipeOptionMenu(playerMenuUtility, this).open();
                }));

        addButton(new Button(5, () -> ingredientOptionsButton,
                (service, event) -> {
                    ingredientChoiceOnInput = false;
                    playerMenuUtility.setPreviousMenu(this);
                    new RecipeOptionMenu(playerMenuUtility, this).open();
                }));

        addButton(new Button(12, () -> new ItemBuilder(thingyToConfigure.getInput().getItem())
                        .appendLore(SlotEntry.getOptionLore(thingyToConfigure.getInput())).get(), (service, event) -> {
                    if (!ItemUtils.isEmpty(event.getCursor())) service.setInput(new SlotEntry(event.getCursor().clone(), service.getInput().getOption()));
                })
        );

        addButton(new Button(14, () -> new ItemBuilder(thingyToConfigure.getCost().getItem())
                        .appendLore(SlotEntry.getOptionLore(thingyToConfigure.getCost())).get(), (service, event) -> {
                    if (!ItemUtils.isEmpty(event.getCursor())) service.setInput(new SlotEntry(event.getCursor().clone(), service.getCost().getOption()));
                })
        );

        List<String> modifierLore = new ArrayList<>();
        thingyToConfigure.getModifiers().forEach(m -> modifierLore.addAll(StringUtils.separateStringIntoLines("&d> " + m.getActiveDescription(), 40)));

        addButton(new Button(22, () -> new ItemBuilder(getButtonData("editor_loottable_entry_modifiers", Material.WRITABLE_BOOK))
                .name("&dDynamic Item Modifiers")
                .lore("&7Modifiers are functions to edit",
                        "&7the output item based on player",
                        "&7stats.",
                        "&eClick to open the menu",
                        "&8&m                <>                ",
                        "%modifiers%")
                .placeholderLore("%modifiers%", modifierLore).get(),
                (service, event) -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new DynamicModifierMenu(playerMenuUtility, this).open();
                }));

        addButton(new Button(40, () -> new ItemBuilder(thingyToConfigure.getUpgradeIcon()).get(), (service, event) -> {
                    if (!ItemUtils.isEmpty(event.getCursor())) service.setUpgradeIcon(event.getCursor().clone());
                })
        );

        addButton(new Button(29, () -> new ItemBuilder(Material.KNOWLEDGE_BOOK).name("&fSkill Experience Rewarded").lore(
                String.format("&7Currently %.1f", thingyToConfigure.getSkillExp()),
                "&8Determines how much skill experience",
                "&8is rewarded with the purchase of",
                "&8this upgrade",
                "&6Click to increase/decrease by 1",
                "&6Shift-Click to do so by 25"
        ).get(), (service, event) -> service.setSkillExp(Math.max(0, service.getSkillExp() + ((event.isShiftClick() ? 25F : 1F) * (event.isLeftClick() ? 1 : -1))))));


        addButton(new Button(33, () -> new ItemBuilder(Material.DIAMOND).name("&fValid Merchant Levels").lore(
                "&8The levels the merchant has to be for",
                "&8this upgrade to be purchasable.",
                "&8Basically a level filter",
                "",
                "&6Click to cycle",
                "&6Shift-Left-Click to add to list",
                "&cShift-Right-Click to clear list",
                "",
                "&7Currently selected: " + currentSelectedLevel,
                "",
                "&fCurrent list: "
                ).appendLore(
                        thingyToConfigure.getAppearanceLevels().stream().map(l -> "&f> &e" + l).toList()
                ).get(), (service, event) -> {


            List<MerchantLevel> levels = new ArrayList<>(Arrays.asList(MerchantLevel.values()));
            int currentSkill = levels.indexOf(currentSelectedLevel);
            if (!event.isShiftClick()) {
                if (event.isLeftClick()) {
                    if (currentSkill + 1 >= levels.size()) currentSkill = 0;
                    else currentSkill++;
                } else {
                    if (currentSkill - 1 < 0) currentSkill = levels.size() - 1;
                    else currentSkill--;
                }
                currentSelectedLevel = levels.get(currentSkill);
            } else {
                if (event.isLeftClick()) {
                    service.getAppearanceLevels().add(currentSelectedLevel);
                } else {
                    service.getAppearanceLevels().clear();
                }
            }
        }));
    }

    @Override
    public String getMenuName() {
        return Utils.chat("&8Configure Upgrading Service " + thingyToConfigure.getID());
    }

    @Override
    public void setRecipeOption(RecipeOption option) {
        if (option == null) return;
        if (ingredientChoiceOnInput) {
            if (!option.isCompatible(thingyToConfigure.getInput().getItem()) || !option.isCompatibleWithInputItem(true)) {
                Utils.sendMessage(playerMenuUtility.getOwner(), "&cNot compatible with this input item");
            } else {
                thingyToConfigure.getInput().setOption(option);
            }
        } else {
            if (!option.isCompatible(thingyToConfigure.getCost().getItem()) || !option.isCompatibleWithInputItem(true)) {
                Utils.sendMessage(playerMenuUtility.getOwner(), "&cNot compatible with this cost item");
            } else {
                thingyToConfigure.getCost().setOption(option);
            }
        }
    }

    @Override
    public void setResultModifiers(List<DynamicItemModifier> resultModifiers) {
        thingyToConfigure.setModifiers(resultModifiers);
    }

    @Override
    public List<DynamicItemModifier> getResultModifiers() {
        return thingyToConfigure.getModifiers();
    }
}
