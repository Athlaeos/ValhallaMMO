package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.RecipeOption;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetRecipeOptionMenu;
import me.athlaeos.valhallammo.gui.implementations.RecipeOptionMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.services.service_implementations.TrainService;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TrainingServiceConfigurationMenu extends SimpleConfigurationMenu<TrainService> implements LayoutConfigurable, SetRecipeOptionMenu {
    public TrainingServiceConfigurationMenu(PlayerMenuUtility playerMenuUtility, Menu previousMenu, TrainService thingyToConfigure) {
        super(playerMenuUtility, previousMenu, thingyToConfigure, new ArrayList<>());

        addButton(new Button(4, () -> new ItemBuilder(Material.ANVIL).name("&fSkill to level").lore(
                "&7Currently &e" + StringUtils.toPascalCase(thingyToConfigure.getSkillToLevel().toLowerCase()),
                "&8The skill this service should level.",
                "&8The player pays the cost every time for",
                "",
                "&6Click to cycle"
        ).get(), (service, event) -> {
            List<String> skills = new ArrayList<>(SkillRegistry.getAllSkillsByType().keySet());
            skills.sort(Comparator.comparingInt(s -> SkillRegistry.getSkill(s).getSkillTreeMenuOrderPriority()));
            int currentSkill = skills.indexOf(service.getSkillToLevel());
            if (event.isLeftClick()) {
                if (currentSkill + 1 >= skills.size()) currentSkill = 0;
                else currentSkill++;
            } else {
                if (currentSkill - 1 < 0) currentSkill = skills.size() - 1;
                else currentSkill--;
            }
            service.setSkillToLevel(skills.get(currentSkill));
        }));

        addButton(new Button(11, () -> new ItemBuilder(Material.COPPER_INGOT).name("&fLevel limit at Novice").lore(
                "&7Currently &e" + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.NOVICE, 15),
                "&8If the merchant is a novice, the player",
                "&8can only be leveled to " + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.NOVICE, 15),
                "&6Click to increase/decrease by 1",
                "&6Shift-Click to do so by 10"
        ).get(), (service, event) -> {
            int level = service.getLimitPerLevel().getOrDefault(MerchantLevel.NOVICE, 15);
            service.getLimitPerLevel().put(MerchantLevel.NOVICE, Math.max(0, level + ((event.isShiftClick() ? 10 : 1) * (event.isLeftClick() ? 1 : -1))));
        }));

        addButton(new Button(12, () -> new ItemBuilder(Material.IRON_INGOT).name("&fLevel limit at Apprentice").lore(
                "&7Currently &e" + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.APPRENTICE, 30),
                "&8If the merchant is an apprentice, the player",
                "&8can only be leveled to " + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.APPRENTICE, 30),
                "&6Click to increase/decrease by 1",
                "&6Shift-Click to do so by 10"
        ).get(), (service, event) -> {
            int level = service.getLimitPerLevel().getOrDefault(MerchantLevel.APPRENTICE, 30);
            service.getLimitPerLevel().put(MerchantLevel.APPRENTICE, Math.max(0, level + ((event.isShiftClick() ? 10 : 1) * (event.isLeftClick() ? 1 : -1))));
        }));

        addButton(new Button(13, () -> new ItemBuilder(Material.GOLD_INGOT).name("&fLevel limit at Journeyman").lore(
                "&7Currently &e" + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.JOURNEYMAN, 45),
                "&8If the merchant is a journeyman, the player",
                "&8can only be leveled to " + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.JOURNEYMAN, 45),
                "&6Click to increase/decrease by 1",
                "&6Shift-Click to do so by 10"
        ).get(), (service, event) -> {
            int level = service.getLimitPerLevel().getOrDefault(MerchantLevel.JOURNEYMAN, 45);
            service.getLimitPerLevel().put(MerchantLevel.JOURNEYMAN, Math.max(0, level + ((event.isShiftClick() ? 10 : 1) * (event.isLeftClick() ? 1 : -1))));
        }));

        addButton(new Button(14, () -> new ItemBuilder(Material.DIAMOND).name("&fLevel limit at Expert").lore(
                "&7Currently &e" + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.EXPERT, 60),
                "&8If the merchant is an expert, the player",
                "&8can only be leveled to " + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.EXPERT, 60),
                "&6Click to increase/decrease by 1",
                "&6Shift-Click to do so by 10"
        ).get(), (service, event) -> {
            int level = service.getLimitPerLevel().getOrDefault(MerchantLevel.EXPERT, 60);
            service.getLimitPerLevel().put(MerchantLevel.EXPERT, Math.max(0, level + ((event.isShiftClick() ? 10 : 1) * (event.isLeftClick() ? 1 : -1))));
        }));

        addButton(new Button(15, () -> new ItemBuilder(Material.NETHERITE_INGOT).name("&fLevel limit at Master").lore(
                "&7Currently &e" + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.MASTER, 75),
                "&8If the merchant is a master, the player",
                "&8can only be leveled to " + thingyToConfigure.getLimitPerLevel().getOrDefault(MerchantLevel.MASTER, 75),
                "&6Click to increase/decrease by 1",
                "&6Shift-Click to do so by 10"
        ).get(), (service, event) -> {
            int level = service.getLimitPerLevel().getOrDefault(MerchantLevel.MASTER, 75);
            service.getLimitPerLevel().put(MerchantLevel.MASTER, Math.max(0, level + ((event.isShiftClick() ? 10 : 1) * (event.isLeftClick() ? 1 : -1))));
        }));

        addButton(new Button(29, () -> new ItemBuilder(Material.EXPERIENCE_BOTTLE).name("&fExperience Step").lore(
                String.format("&7Currently &e%.0f", thingyToConfigure.getExpStep()),
                "&8The amount of experience to cost ",
                "&81 unit of whatever is put as the item",
                "&8cost.",
                "&6Click to increase/decrease by 100",
                "&6Shift-Click to do so by 2500"
        ).get(), (service, event) -> service.setExpStep(Math.max(0, service.getExpStep() + ((event.isShiftClick() ? 2500 : 100) * (event.isLeftClick() ? 1 : -1))))));

        addButton(new Button(30, () -> new ItemBuilder(thingyToConfigure.getCost().getItem())
                .appendLore(SlotEntry.getOptionLore(thingyToConfigure.getCost())).get(), (service, event) -> {
                        if (!ItemUtils.isEmpty(event.getCursor())) service.setCost(new SlotEntry(event.getCursor().clone(), service.getCost().getOption()));
                })
        );

        addButton(new Button(32, () -> new ItemBuilder(Material.KNOWLEDGE_BOOK).name("&fSkill Experience Per Level").lore(
                String.format("&7Currently %.1f/order", thingyToConfigure.getSkillExpPerCost()),
                "&8Determines how much skill experience",
                "&8is rewarded per level bought",
                "&6Click to increase/decrease by 1",
                "&6Shift-Click to do so by 25"
        ).get(), (service, event) -> service.setSkillExpPerCost(Math.max(0, service.getSkillExpPerCost() + ((event.isShiftClick() ? 25F : 1F) * (event.isLeftClick() ? 1 : -1))))));

        addButton(new Button(33, () -> new ItemBuilder(Material.BARREL).name("&fEdit Button Position").lore(
                "&8Determines where the button for this",
                "&8service is placed, as well as what",
                "&8it looks like",
                "&6Click to edit"
        ).get(), (service, event) -> new LayoutConfigurationMenu(playerMenuUtility, this, service.getRows(), service.getPrimaryButton(), service.getPrimaryButtonPosition(), service.getSecondaryButtonPositions()).open()));

        ItemStack ingredientOptionsButton = new ItemBuilder(getButtonData("editor_recipe_cooking_recipeoptions", Material.WRITABLE_BOOK))
                .name("&bIngredient Options")
                .lore("&7Ingredient options are ingredient",
                        "&7flags you can put on an ingredient",
                        "&7to change its behavior during crafting.",
                        "&eClick to open the menu").get();
        addButton(new Button(39, () -> ingredientOptionsButton,
                (service, event) -> {
                    playerMenuUtility.setPreviousMenu(this);
                    new RecipeOptionMenu(playerMenuUtility, this).open();
                }));
    }

    @Override
    public String getMenuName() {
        return Utils.chat((ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF321\uF80C\uF80A\uF808\uF802" : "") + "&8" + thingyToConfigure.getID());
    }

    @Override
    public void setConfiguration(int rowCount, int primaryButtonIndex, ItemStack primaryButtonIcon, List<Integer> secondaryIndexes) {
        thingyToConfigure.setRows(rowCount);
        thingyToConfigure.setPrimaryButtonPosition(primaryButtonIndex);
        thingyToConfigure.setPrimaryButton(primaryButtonIcon);
        thingyToConfigure.setSecondaryButtonPositions(secondaryIndexes);
    }

    @Override
    public void setRecipeOption(RecipeOption option) {
        if (option == null) return;
        if (!option.isCompatible(thingyToConfigure.getCost().getItem()) || !option.isCompatibleWithInputItem(true)) {
            Utils.sendMessage(playerMenuUtility.getOwner(), "&cNot compatible with this item");
        } else {
            thingyToConfigure.getCost().setOption(option);
        }
    }
}
