package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.dom.Action;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Question;
import me.athlaeos.valhallammo.dom.Questionnaire;
import me.athlaeos.valhallammo.gui.implementations.DynamicModifierMenu;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DisplayNameSet extends DynamicItemModifier {
    private String displayName;

    public DisplayNameSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        context.getItem().name(displayName);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public void onButtonPress(InventoryClickEvent e, DynamicModifierMenu menu, int button) {
        if (button == 12) {
            if (e.isShiftClick()) displayName = null;
            else {
                e.getWhoClicked().closeInventory();
                Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                        new Question("&fWhat should the item's name be? (type in chat, or 'cancel' to cancel)", s -> true, "")
                ) {
                    @Override
                    public Action<Player> getOnFinish() {
                        if (getQuestions().isEmpty()) return super.getOnFinish();
                        Question question = getQuestions().get(0);
                        if (question.getAnswer() == null) return super.getOnFinish();
                        return (p) -> {
                            String answer = question.getAnswer();
                            if (!answer.contains("cancel")) displayName = answer;
                            menu.open();
                        };
                    }
                };
                Questionnaire.startQuestionnaire((Player) e.getWhoClicked(), questionnaire);
            }
        }
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(12,
                new ItemBuilder(Material.INK_SAC)
                        .name("&eWhat should the name be?")
                        .lore("&fSet to " + displayName,
                                "&6Click to enter name (in chat)",
                                "&6Shift-Click to reset the name back to nothing.")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.NAME_TAG).get();
    }

    @Override
    public String getDisplayName() {
        return "&dDisplay Name";
    }

    @Override
    public String getDescription() {
        return "&fChanges the display name of the item";
    }

    @Override
    public String getActiveDescription() {
        return "&fChanges the display name of the item to " + (displayName == null ? "nothing" : displayName);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public DynamicItemModifier copy() {
        DisplayNameSet m = new DisplayNameSet(getName());
        m.setDisplayName(this.displayName);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "You must indicate the new name of the item, or 'null' for nothing";
        if (args[0].equalsIgnoreCase("null")) displayName = null;
        else displayName = Utils.chat(args[0].replace("/_", " "));
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<name_or_null>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
