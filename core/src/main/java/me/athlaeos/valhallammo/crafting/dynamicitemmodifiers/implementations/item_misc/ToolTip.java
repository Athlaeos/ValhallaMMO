package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.implementations.DynamicModifierMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ToolTip extends DynamicItemModifier {
    private String toolTip = null;

    public ToolTip(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        ValhallaMMO.getNms().setToolTipStyle(context.getItem().getMeta(), toolTip);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public void onButtonPress(InventoryClickEvent e, DynamicModifierMenu menu, int button) {
        if (button == 12){
            if (e.isShiftClick()) toolTip = null;
            else {
                ask((Player) e.getWhoClicked(), menu, "What tooltip style should be used?", (answer) -> {
                    this.toolTip = answer;
                });
            }
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
        return new Pair<>(12,
                new ItemBuilder(Material.BARRIER)
                        .name("&fWhich tooltip should the item have?")
                        .lore("&fCurrently set to &e" + (toolTip == null ? "&cnothing" : toolTip),
                                "&fDetermines the visual appearance",
                                "&fof the item's tooltip",
                                "&6Click with another item to",
                                "&6copy the tooltip of the item over")
                        .get()).map(Set.of());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.BRUSH).get();
    }

    @Override
    public String getDisplayName() {
        return "&aSet Item Tooltip";
    }

    @Override
    public String getDescription() {
        return "&fChanges the texture of the item's tooltip";
    }

    @Override
    public String getActiveDescription() {
        return toolTip == null ? "&cRemoves item tooltip" : "&fSets item tooltip to " + toolTip;
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    @Override
    public DynamicItemModifier copy() {
        ToolTip m = new ToolTip(getName());
        m.setToolTip(this.toolTip);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1)
            return "You must enter the tooltip you want the item to have, or 'reset' if you want to remove it";
        toolTip = args[0].equalsIgnoreCase("reset") ? null : args[0];
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<model>", "minecraft:");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
