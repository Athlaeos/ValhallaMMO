package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ResultChangingModifier;
import me.athlaeos.valhallammo.dom.*;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.implementations.DynamicModifierMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class CustomModelDataOrModelSet extends DynamicItemModifier implements ResultChangingModifier {
    private Integer customModelData = 1000000;
    private String model = null;

    public CustomModelDataOrModelSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_21_4))
            context.getItem().model(model);
        else context.getItem().data(customModelData);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public void onButtonPress(InventoryClickEvent e, DynamicModifierMenu menu, int button) {
        if (button == 6 || button == 7 || button == 8) {
            ItemStack cursor = e.getCursor();
            if (!ItemUtils.isEmpty(cursor)) {
                ItemMeta meta = cursor.getItemMeta();
                if (meta != null) {
                    if (meta.hasCustomModelData()) customModelData = meta.getCustomModelData();
                    else customModelData = null;
                }
            } else {
                if (button == 6)
                    customModelData = Math.min(9999999, Math.max(0, customModelData + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 1000000 : 100000))));
                else if (button == 7)
                    customModelData = Math.min(9999999, Math.max(0, customModelData + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 10000 : 1000))));
                else
                    customModelData = Math.min(9999999, Math.max(0, customModelData + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 25 : 1))));
            }
        } else if (button == 17) {
            ask((Player) e.getWhoClicked(), menu, "What model should be used?", (answer) -> {
                this.model = answer;
            });
        }
    }

    private void ask(Player player, Menu menu, String question, Consumer<String> onAnswer){
        player.closeInventory();
        Questionnaire questionnaire = new Questionnaire(player, null, null,
                new Question("&f" + question + " (type in chat, or 'cancel' to cancel)", s -> true, "")
        ) {
            @Override
            public Action<Player> getOnFinish() {
                if (getQuestions().isEmpty()) return super.getOnFinish();
                Question question = getQuestions().get(0);
                if (question.getAnswer() == null) return super.getOnFinish();
                return (p) -> {
                    String answer = question.getAnswer();
                    if (!answer.contains("cancel")) onAnswer.accept(answer);
                    menu.open();
                };
            }
        };
        Questionnaire.startQuestionnaire(player, questionnaire);
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(6,
                new ItemBuilder(Material.RED_DYE)
                        .name("&eWhat should custom model data be?")
                        .lore("&6Click with another item to copy",
                                "&6its custom model data over.",
                                "&fSet to " + (customModelData == null || customModelData == 0 ? "removal" : customModelData),
                                "&6Click to add/subtract 1000000",
                                "&6Shift-Click to add/subtract 100000")
                        .get()).map(Set.of(
                new Pair<>(7,
                        new ItemBuilder(Material.GREEN_DYE)
                                .name("&eWhat should custom model data be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom model data over.",
                                        "&fSet to " + (customModelData == null || customModelData == 0 ? "removal" : customModelData),
                                        "&6Click to add/subtract 10000",
                                        "&6Shift-Click to add/subtract 1000")
                                .get()),
                new Pair<>(8,
                        new ItemBuilder(Material.BLUE_DYE)
                                .name("&eWhat should custom model data be?")
                                .lore("&6Click with another item to copy",
                                        "&6its custom model data over.",
                                        "&fSet to " + (customModelData == null || customModelData == 0 ? "removal" : customModelData),
                                        "&6Click to add/subtract 25",
                                        "&6Shift-Click to add/subtract 1")
                                .get()),
                new Pair<>(17,
                        new ItemBuilder(Material.BARRIER)
                                .name("&fWhich model should the item have?")
                                .lore("&fCurrently set to &e" + (model == null ? "&cnothing" : model),
                                        "&fDetermines the visual appearance",
                                        "&fof the item",
                                        "&6Click to enter model (in chat)",
                                        "&6Shift-Click to clear")
                                .get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.PAINTING).get();
    }

    @Override
    public String getDisplayName() {
        return "&aSet Custom Model Data &dor Model";
    }

    @Override
    public String getDescription() {
        return "&fSets a custom model data to the item, or a model on versions 1.21.4 and higher";
    }

    @Override
    public String getActiveDescription() {
        return "&fSets custom model data to " + (customModelData == null || customModelData == 0 ? "removal" : customModelData) + " or the model to " + (model == null ? "removal" : model) + " on versions 1.21.4 and higher";
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setCustomModelData(Integer customModelData) {
        this.customModelData = customModelData;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public DynamicItemModifier copy() {
        CustomModelDataOrModelSet m = new CustomModelDataOrModelSet(getName());
        m.setCustomModelData(this.customModelData);
        m.setModel(model);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        return "You should use 'item_model' or 'custom_model_data' instead";
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
