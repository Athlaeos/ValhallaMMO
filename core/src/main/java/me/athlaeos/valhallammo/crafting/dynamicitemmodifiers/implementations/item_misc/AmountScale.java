package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc;

import me.athlaeos.valhallammo.commands.Command;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierCategoryRegistry;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Scaling;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.item.MaterialClass;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AmountScale extends DynamicItemModifier {
    private double skillEfficiency = 1;
    private double minimumFraction = 0;
    private boolean damagePenalty = true;
    private static final Scaling scaling = Scaling.fromConfig("skills/smithing.yml", "quantity_scaling");

    public AmountScale(String name) {
        super(name);
    }

    @Override
    public void processItem(Player crafter, ItemBuilder outputItem, boolean use, boolean validate, int timesExecuted) {
        if (scaling == null) {
            failedRecipe(outputItem, "");
            return;
        }
        double skill = AccumulativeStatManager.getCachedStats("SMITHING_QUALITY_GENERAL", crafter, 10000, use);
        double skillMultiplier = 1 + AccumulativeStatManager.getCachedStats("SMITHING_FRACTION_QUALITY_GENERAL", crafter, 10000, use);
        MaterialClass materialClass = MaterialClass.getMatchingClass(outputItem.getMeta());
        if (materialClass != null){
            skill += AccumulativeStatManager.getCachedStats("SMITHING_QUALITY_" + materialClass, crafter, 10000, use);
            skillMultiplier += AccumulativeStatManager.getCachedStats("SMITHING_FRACTION_QUALITY_" + materialClass, crafter, 10000, use);
        }
        int originalAmount = outputItem.getItem().getAmount();

        if (damagePenalty){
            if (outputItem.getMeta() instanceof Damageable && outputItem.getItem().getType().getMaxDurability() > 0){
                double fractionDurability = (outputItem.getItem().getType().getMaxDurability() - ((Damageable) outputItem.getMeta()).getDamage()) / (double) outputItem.getItem().getType().getMaxDurability();
                originalAmount = (int) Math.floor(originalAmount * fractionDurability);
            }
        }

        int minimumAmount = Math.max(0, (int) Math.ceil(originalAmount * minimumFraction));
        int newAmount = (int) Math.max(minimumAmount, Math.floor(scaling.evaluate(scaling.getExpression().replace("%rating%", String.valueOf((skill * skillMultiplier * skillEfficiency))), originalAmount)));
        if (newAmount <= 0) failedRecipe(outputItem, "");
        else outputItem.amount(newAmount);
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 6) skillEfficiency = Math.max(0, skillEfficiency + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01)));
        else if (button == 8) minimumFraction = Math.min(1, Math.max(0, minimumFraction + ((e.isLeftClick() ? 1 : -1) * (e.isShiftClick() ? 0.1 : 0.01))));
        else if (button == 17) damagePenalty = !damagePenalty;
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(6,
                new ItemBuilder(Material.NETHER_STAR)
                        .name("&fSkill Efficiency")
                        .lore(String.format("&fSkill efficiency: %.0f%%", skillEfficiency * 100),
                                String.format("&fMinimum fraction of items: &e%.2fx", minimumFraction),
                                String.format("&fReduces with item damage: &e%s", damagePenalty ? "Yes" : "No"),
                                "&fSkill efficiency determines how much",
                                "&fof the player's skill is used in the",
                                "&fformula.",
                                "&6Click to add/subtract 1%",
                                "&6Shift-Click to add/subtract 10%")
                        .get()).map(
                                Set.of(
                                        new Pair<>(8,
                                                new ItemBuilder(Material.PAPER)
                                                        .name("&fMinimum fraction")
                                                        .lore(String.format("&fSkill efficiency: %.0f%%", skillEfficiency * 100),
                                                                String.format("&fMinimum fraction of items: &e%.2fx", minimumFraction),
                                                                String.format("&fReduces with item damage: &e%s", damagePenalty ? "Yes" : "No"),
                                                                "&fThe resulting item will always be at",
                                                                "&fleast the given fraction of its amount,",
                                                                "&frounded up",
                                                                "&6Click to add/subtract 0.01",
                                                                "&6Shift-Click to add/subtract 0.1")
                                                        .get()
                                        ),
                                        new Pair<>(17,
                                                new ItemBuilder(Material.GOLDEN_PICKAXE)
                                                        .name("&6Scale with durability?")
                                                        .lore(String.format("&fSkill efficiency: %.0f%%", skillEfficiency * 100),
                                                                String.format("&fMinimum fraction of items: &e%.2fx", minimumFraction),
                                                                String.format("&fReduces with item damage: &e%s", damagePenalty ? "Yes" : "No"),
                                                                "&fDetermines if the resulting amount will",
                                                                "&fbe lower if the item is damaged.",
                                                                "&f50% durability will halve the amount",
                                                                "&6Click to toggle on/off")
                                                        .get()
                                        )
                                )
        );
    }

    @Override
    public ItemStack getModifierIcon() {
        return new ItemBuilder(Material.IRON_INGOT).get();
    }

    @Override
    public String getDisplayName() {
        return "&aQuantity (DYNAMIC)";
    }

    @Override
    public String getDescription() {
        return "&fChanges the item's amount based on the player's smithing skill";
    }

    @Override
    public String getActiveDescription() {
        return String.format("&fChanges the item's amount based on &e%.0f%%&f the player's smithing skill, to at least &e%.2fx&f the item's amount &e%s", skillEfficiency * 100, minimumFraction, damagePenalty ? "and reducing further with item durability damage" : "No");
    }

    @Override
    public Collection<String> getCategories() {
        return Set.of(ModifierCategoryRegistry.ITEM_MISC.id());
    }

    public void setDamagePenalty(boolean damagePenalty) {
        this.damagePenalty = damagePenalty;
    }

    public void setMinimumFraction(double minimumFraction) {
        this.minimumFraction = minimumFraction;
    }

    public void setSkillEfficiency(double skillEfficiency) {
        this.skillEfficiency = skillEfficiency;
    }

    @Override
    public DynamicItemModifier copy() {
        AmountScale m = new AmountScale(getName());
        m.setDamagePenalty(this.damagePenalty);
        m.setMinimumFraction(this.minimumFraction);
        m.setSkillEfficiency(this.skillEfficiency);
        m.setPriority(this.getPriority());
        return m;
    }

    @Override
    public String parseCommand(CommandSender executor, String[] args) {
        if (args.length != 3) return "Three arguments are expected: the first two doubles, the third a yes/no answer";
        try {
            skillEfficiency = StringUtils.parseDouble(args[0]);
            minimumFraction = StringUtils.parseDouble(args[1]);
            damagePenalty = args[2].equalsIgnoreCase("yes");
        } catch (NumberFormatException ignored){
            return "Three arguments are expected: the first two doubles, the third a yes/no answer. At least one was not a number";
        }
        return null;
    }

    @Override
    public List<String> commandSuggestions(CommandSender executor, int currentArg) {
        if (currentArg == 0) return List.of("<skill_efficiency>");
        if (currentArg == 1) return List.of("<minimum_fraction>");
        if (currentArg == 2) return List.of("<lower_with_damage>", "yes", "no");
        return Command.noSubcommandArgs();
    }

    @Override
    public int commandArgsRequired() {
        return 3;
    }
}
