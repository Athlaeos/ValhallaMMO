package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
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

import java.util.*;
import java.util.function.Consumer;

public class LoreSet extends DynamicItemModifier {
    private List<String> lore = new ArrayList<>();
    private int mode = 0;

    public LoreSet(String name) {
        super(name);
    }

    @Override
    public void processItem(ModifierContext context) {
        if (lore.isEmpty()) context.getItem().lore(lore);
        else {
            switch (mode) {
                case 0 -> context.getItem().prependLore(lore);
                case 1 -> context.getItem().appendLore(lore);
                case 2 -> context.getItem().lore(lore);
            }
        }
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {

    }

    @Override
    public void onButtonPress(InventoryClickEvent e, DynamicModifierMenu menu, int button) {
        if (button == 11) {
            mode = Math.max(0, Math.min(2, mode + (e.isLeftClick() ? 1 : -1)));
        } else if (button == 13) {
            if (e.isShiftClick()) this.lore.clear();
            else {
                ask((Player) e.getWhoClicked(), menu, "What lore should be used? Use /n for a new line, or /_ for a forceful space", (answer) -> {
                    this.lore = new ArrayList<>(List.of(answer.replace("/_", " ").split("/n")));
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
        return new Pair<>(13,
                new ItemBuilder(Material.INK_SAC)
                        .name("&eWhat should the lore be?")
                        .lore(mode == 0 ? "&fThe following is added to the start" : mode == 1 ? "&fThe following is added to the end" : "&fThe lore will be set to",
                                "&8&m                                 ")
                        .appendLore(lore.isEmpty() ? List.of("&cLore is cleared") : lore)
                        .appendLore(
                                "&8&m                                 ",
                                "&6Click to set the lore (in chat)",
                                "&6Or shift-click to reset"
                        ).get()).map(Set.of(
                new Pair<>(11,
                        new ItemBuilder(Material.REDSTONE_TORCH)
                                .name("&eHow should the lore be added?")
                                .lore(mode == 0 ? "&fThe following is added to the start" : mode == 1 ? "&fThe following is added to the end" : "&fThe lore will be set to",
                                        "&8&m                                 ")
                                .appendLore(lore.isEmpty() ? List.of("&cLore is cleared") : lore)
                                .appendLore(
                                        "&8&m                                 ",
                                        "&6Click to cycle"
                                ).get())
        ));
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.WRITABLE_BOOK).get();
    }

    @Override
    public String getDisplayName() {
        return "&dLore";
    }

    @Override
    public String getDescription() {
        return "&fChanges the lore of the item";
    }

    @Override
    public String getActiveDescription() {
        return "&fChanges the lore of the item to " + (lore.isEmpty() ? "be cleared" : "/n" + String.join("/n", lore));
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    @Override
    public DynamicItemModifier copy() {
        LoreSet m = new LoreSet(getName());
        m.setLore(new ArrayList<>(this.lore));
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 2) return "You must indicate the new lore of the item, or 'null' to clear it, along with the mode. 0 = prepend, 1 = append, 2 = replace";
        if (args[0].equalsIgnoreCase("null")) lore.clear();
        else lore.addAll(Arrays.stream(args[0].split("/n")).map(l -> l.replace("/_", " ")).toList());
        Integer mode = Catch.catchOrElse(() -> Integer.parseInt(args[1]), null);
        if (mode == null || mode < 0 || mode > 2) return "Invalid/absent mode, 0 = prepend before lore, 1 = append after lore, 2 = replace lore";
        this.mode = mode;
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<lore>", "use-/n-for-new-lines", "use-/_-for-spaces");
        if (currentArg == 1) return List.of("<mode>", "0-for-prepend", "1-for-append", "2-for-replace");
        return null;
    }

    @Override
    public int commandArgsRequired() {
        return 2;
    }
}
