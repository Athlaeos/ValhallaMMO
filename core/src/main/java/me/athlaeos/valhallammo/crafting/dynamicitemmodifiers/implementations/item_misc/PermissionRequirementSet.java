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
import me.athlaeos.valhallammo.item.ItemSkillRequirements;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PermissionRequirementSet extends DynamicItemModifier {
    private String permission = null;

    public PermissionRequirementSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (!context.shouldExecuteUsageMechanics()) return;
        ItemSkillRequirements.setPermissionRequirement(context.getItem().getMeta(), permission);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public void onButtonPress(InventoryClickEvent e, DynamicModifierMenu menu, int button) {
        if (button == 12) {
            if (e.isShiftClick()) permission = null;
            else {
                e.getWhoClicked().closeInventory();
                Questionnaire questionnaire = new Questionnaire((Player) e.getWhoClicked(), null, null,
                        new Question("&fWhat should the permission be? (type in chat, or 'cancel' to cancel)", s -> true, "")
                ) {
                    @Override
                    public Action<Player> getOnFinish() {
                        if (getQuestions().isEmpty()) return super.getOnFinish();
                        Question question = getQuestions().get(0);
                        if (question.getAnswer() == null) return super.getOnFinish();
                        return (p) -> {
                            String answer = question.getAnswer();
                            if (!answer.contains("cancel")) permission = answer;
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
                        .name("&eWhat should the permission be?")
                        .lore("&fSet to " + (permission == null ? "&cremoval" : permission),
                                "&6Click to enter permission (in chat)",
                                "&6Shift-Click to reset the permission back to nothing.")
                        .get()).map(new HashSet<>());
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.NAME_TAG).get();
    }

    @Override
    public String getDisplayName() {
        return "&eRequire Permission";
    }

    @Override
    public String getDescription() {
        return "&fRequires the player to have a specific permission to be able to utilize the item well.";
    }

    @Override
    public String getActiveDescription() {
        if (permission == null) return "&fRemoves the permission requirement off the item";
        return String.format("&fRequires the player to have the permission &e%s &fto be able to utilize the item well", permission);
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public DynamicItemModifier copy() {
        PermissionRequirementSet m = new PermissionRequirementSet(getName());
        m.setPermission(this.permission);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 1) return "One argument expected: the permission";
        try {
            permission = args[0];
        } catch (NumberFormatException ignored){
            return "One argument expected: the permission";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<permission>");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 1;
    }
}
