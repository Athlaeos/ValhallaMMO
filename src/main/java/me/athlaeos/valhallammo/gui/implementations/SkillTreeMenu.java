package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.localization.TranslationManager;
import me.athlaeos.valhallammo.placeholder.PlaceholderRegistry;
import me.athlaeos.valhallammo.skills.skills.Perk;
import me.athlaeos.valhallammo.skills.skills.PerkConnectionIcon;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpense;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import me.athlaeos.valhallammo.skills.skills.implementations.power.PowerProfile;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpenseRegistry;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockCondition;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockConditionRegistry;
import me.athlaeos.valhallammo.skills.skills.PerkRegistry;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.power.PowerSkill;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class SkillTreeMenu extends Menu {
    private final NamespacedKey buttonKey = new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_button_id");
    private final Player target;
    private final List<ItemStack> skillIcons = new ArrayList<>();
    private Skill selectedSkill;
    private final Map<String, ItemStack[][]> skillTrees = new HashMap<>();
    private int x;
    private int y;

    private static final ItemStack directionN = new ItemBuilder(getButtonData("skilltree_direction_n", Material.ARROW)).name(TranslationManager.getTranslation("skilltree_arrow_name_n")).get();
    private static final ItemStack directionNE = new ItemBuilder(getButtonData("skilltree_direction_ne", Material.ARROW)).name(TranslationManager.getTranslation("skilltree_arrow_name_ne")).get();
    private static final ItemStack directionE = new ItemBuilder(getButtonData("skilltree_direction_e", Material.ARROW)).name(TranslationManager.getTranslation("skilltree_arrow_name_e")).get();
    private static final ItemStack directionSE = new ItemBuilder(getButtonData("skilltree_direction_se", Material.ARROW)).name(TranslationManager.getTranslation("skilltree_arrow_name_se")).get();
    private static final ItemStack directionS = new ItemBuilder(getButtonData("skilltree_direction_s", Material.ARROW)).name(TranslationManager.getTranslation("skilltree_arrow_name_s")).get();
    private static final ItemStack directionSW = new ItemBuilder(getButtonData("skilltree_direction_sw", Material.ARROW)).name(TranslationManager.getTranslation("skilltree_arrow_name_sw")).get();
    private static final ItemStack directionW = new ItemBuilder(getButtonData("skilltree_direction_w", Material.ARROW)).name(TranslationManager.getTranslation("skilltree_arrow_name_w")).get();
    private static final ItemStack directionNW = new ItemBuilder(getButtonData("skilltree_direction_nw", Material.ARROW)).name(TranslationManager.getTranslation("skilltree_arrow_name_nw")).get();
    private final String perk_requirement_warning_levels = TranslationManager.getTranslation("perk_requirement_warning_levels");
    private final String perk_requirement_status_unlockable = TranslationManager.getTranslation("perk_requirement_status_unlockable");
    private final String perk_requirement_status_unlocked = TranslationManager.getTranslation("perk_requirement_status_unlocked");
    private final String perk_requirement_status_permanently_locked = TranslationManager.getTranslation("perk_requirement_status_permanently_locked");
    private final String perk_requirement_status_fake_unlocked = TranslationManager.getTranslation("perk_requirement_status_fake_unlocked");
    private static int configInt(String key){
        return ValhallaMMO.getPluginConfig().getInt(key);
    }


    public SkillTreeMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);

        selectedSkill = SkillRegistry.getSkill(PowerSkill.class);
        x = selectedSkill.getCenterX();
        y = selectedSkill.getCenterY();
        target = playerMenuUtility.getOwner();

        buildSkillTrees();
    }

    public SkillTreeMenu(PlayerMenuUtility playerMenuUtility, Player target) {
        super(playerMenuUtility);

        selectedSkill = SkillRegistry.getSkill(PowerSkill.class);
        x = selectedSkill.getCenterX();
        y = selectedSkill.getCenterY();
        this.target = target;

        buildSkillTrees();
    }

    @Override
    public String getMenuName() {
        return Utils.chat(ValhallaMMO.isResourcePackConfigForced() ? "&f\uF808\uF001" : TranslationManager.getTranslation("skilltree"));
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack i = e.getCurrentItem();
        if (!ItemUtils.isEmpty(i)){
            ItemMeta meta = ItemUtils.getItemMeta(i);
            if (meta == null) {
                setMenuItems();
                return;
            }
            int x2 = x; // only purpose is to compare before and after, to see if the menu should be updated
            int y2 = y;

            int s = e.getSlot();
            if ((s == 0 || s == 4 || s == 8) && y - 1 >= 0) y -= 1; // the three northern buttons should subtract y by 1
            if ((s == 36 || s == 40 || s == 44) && y + 5 < currentSkillTreeHeight()) y += 1; // the three southern buttons should increase y by 1
            if ((s == 0 || s == 18 || s == 36) && x - 1 >= 0) x -= 1; // the three western buttons should subtract x by 1
            if ((s == 8 || s == 26 || s == 44) && x + 9 < currentSkillTreeWidth()) x += 1; // the three eastern buttons should increase x by 1
            if (x != x2 || y != y2) {
                // navigational buttons were clicked, so nothing else needs to run
                setMenuItems();
                return;
            }

            if (meta.getPersistentDataContainer().has(buttonKey, PersistentDataType.STRING)){
                String id = meta.getPersistentDataContainer().get(buttonKey, PersistentDataType.STRING);

                if (s >= 45) {
                    Skill selectedSkill = SkillRegistry.getSkill(id);
                    if (selectedSkill != null) {
                        this.selectedSkill = selectedSkill;
                        this.x = selectedSkill.getCenterX();
                        this.y = selectedSkill.getCenterY();
                    }
                } else {
                    if (selectedSkill == null){
                        setMenuItems();
                        return;
                    }
                    Perk p = PerkRegistry.getPerk(id);
                    if (p != null){
                        if (p.canUnlock(target)){
                            // persist perk as unlocked
                            PowerProfile account = ProfileManager.getPersistentProfile(target, PowerProfile.class);

                            Collection<String> perks = account.getUnlockedPerks();
                            perks.add(p.getName());
                            account.setUnlockedPerks(perks);

                            ProfileManager.setPersistentProfile(target, account, PowerProfile.class);

                            // execute perk's rewards
                            p.execute(target);

                            // remove resources
                            for (ResourceExpense expense : p.getExpenses()) expense.purchase(target, true);
                        } else {
                            for (ResourceExpense expense : p.getExpenses()){
                                if (!expense.canPurchase(target)) playerMenuUtility.getOwner().sendMessage(Utils.chat(expense.getInsufficientFundsMessage()));
                            }
                        }
                        skillTrees.put(selectedSkill.getType(), getSkillTree(selectedSkill));
                    }
                }
            }
        }
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {

    }

    private int currentSkillTreeHeight(){
        return skillTrees.get(selectedSkill.getType()).length;
    }

    private int currentSkillTreeWidth(){
        return skillTrees.get(selectedSkill.getType())[0].length;
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        setScrollBar();
        if (selectedSkill != null && skillTrees.containsKey(selectedSkill.getType())){
            ItemStack[][] treeView = getSkillTreeView(skillTrees.get(selectedSkill.getType()), x, y);
            if (!ArrayUtils.isEmpty(treeView)){
                int index = 0;
                if (treeView.length >= 5){
                    for (int r = 0; r < 5; r++){
                        ItemStack[] row = treeView[r];
                        if (row.length >= 9){
                            for (int i = 0; i < 9; i++){
                                if (row[i] != null){
                                    inventory.setItem(index, row[i]);
                                }
                                index++;
                            }
                        }
                    }
                }
            }
        }
        inventory.setItem(0, directionNW);
        inventory.setItem(4, directionN);
        inventory.setItem(8, directionNE);
        inventory.setItem(18, directionW);
        inventory.setItem(26, directionE);
        inventory.setItem(36, directionSW);
        inventory.setItem(40, directionS);
        inventory.setItem(44, directionSE);
    }

    private void setScrollBar(){
        List<ItemStack> icons = new ArrayList<>(skillIcons);
        int iconsSize = icons.size();
        for (ItemStack i : skillIcons){
            if (ItemUtils.isEmpty(i)) continue;
            ItemMeta meta = ItemUtils.getItemMeta(i);
            if (meta == null) continue;
            String storedType = getItemStoredSkillType(meta);
            if (storedType != null){
                Skill s = SkillRegistry.getSkill(storedType);
                if (s != null) {
                    PowerProfile acc = ProfileManager.getMergedProfile(target, PowerProfile.class);

                    Profile p = ProfileManager.getPersistentProfile(target, s.getProfileType());
                    double expRequired = s.expForLevel(p.getLevel() + 1);
                    List<String> lore = new ArrayList<>();
                    for (String line : TranslationManager.getListTranslation("skilltree_icon_format")){
                        lore.add(Utils.chat(line
                                .replace("%level_current%", "" + p.getLevel())
                                .replace("%exp_current%", String.format("%.2f", p.getEXP()))
                                .replace("%exp_next%", (expRequired < 0) ? TranslationManager.getTranslation("max_level") : String.format("%.2f", expRequired))
                                .replace("%exp_total%", String.format("%.2f", p.getTotalEXP()))
                                .replace("%skillpoints%", "" + (acc.getSpendableSkillPoints() - acc.getSpentSkillPoints()))));
                    }
                    meta.setLore(lore);
                    ItemUtils.setItemMeta(i, meta);
                }
            }
        }
        if (iconsSize > 0){
            for (int i = 0; i < SkillRegistry.getAllSkills().size(); i++){
                for (int o = 0; o < 9; o++){
                    if (o >= skillIcons.size()) break;
                    ItemStack iconToPut = skillIcons.get(o);
                    inventory.setItem(45 + o, iconToPut);
                }
                ItemStack centerItem = inventory.getItem(49);
                if (centerItem != null){
                    ItemMeta meta = ItemUtils.getItemMeta(centerItem);
                    if (meta == null) continue;
                    String stored = getItemStoredSkillType(meta);
                    if (stored != null){
                        if (stored.equals(this.selectedSkill.getType())){
                            break;
                        }
                    }
                }
                Collections.rotate(skillIcons, 1);
            }
        }
    }

    private String getItemStoredSkillType(ItemMeta meta){
        return meta.getPersistentDataContainer().get(buttonKey, PersistentDataType.STRING);
    }

    // If everything goes right, this should return a 2D array of itemstacks with a size of at least 9x5
    private ItemStack[][] getSkillTree(Skill skill){
        if (skill == null) return null;
        // skilltree size at least 1x1 at the center
        int minX = skill.getCenterX();
        int maxX = skill.getCenterX();
        int minY = skill.getCenterY();
        int maxY = skill.getCenterY();

        int xOff = 0;
        int yOff = 0;
        // move min and max x and y as far apart as necessary to fit skill tree
        for (Perk p : skill.getPerks()){
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        // offset min and max x and y coords, so they are all 0 and above
        // if center x or y are negative, offset the center to compensate for it
        if (minX < 0) {
            skill.setCenterX(skill.getCenterX() - minX);
            xOff = -minX;
            maxX -= minX;
            minX = 0;
        }
        if (minY < 0){
            skill.setCenterY(skill.getCenterY() - minY);
            yOff = -minY;
            maxY -= minY;
            minY = 0;
        }
        int width = (maxX - minX) + 9; // give skill tree an empty border for moving
        int height = (maxY - minY) + 5;

        ItemStack[][] skillTree = new ItemStack[height][width];
        for (ItemStack[] row : skillTree){
            Arrays.fill(row, null);
        }

        List<Perk> perks = new ArrayList<>(skill.getPerks());
        perks.sort(Comparator.comparingInt(Perk::getLevelRequirement));
        for (Perk p : perks){
            if ((!p.isHiddenUntilRequirementsMet()) || p.shouldBeVisible(target) || p.hasUnlocked(target)){
                ItemStack perkIcon = new ItemBuilder(p.getIcon())
                        .name(p.getDisplayName())
                        .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS)
                        .get();

                ItemMeta perkMeta = ItemUtils.getItemMeta(perkIcon);
                if (perkMeta == null) continue;
                List<String> iconLore = new ArrayList<>();

                int unlockedStatus = p.hasPermanentlyLocked(target) ? 2 : // permanently locked
                        p.hasFakeUnlocked(target) ? 3 : // fake unlocked
                                p.hasUnlocked(target) ? 1 : // unlocked normally
                                        0; // not unlocked
                for (String l : TranslationManager.getListTranslation("skilltree_perk_format")){
                    if (l.contains("%description%")){
                        String description = p.getDescription();
                        for (PerkReward reward : p.getRewards()){
                            description = description.replace("{" + reward.getName() + "}", reward.rewardPlacholder());
                        }
                        iconLore.addAll(Utils.chat(StringUtils.separateStringIntoLines(description, 40)));
                    } else if (UnlockConditionRegistry.getValuePlaceholders().stream().anyMatch(c -> l.contains(String.format("%%%s%%", c)))) { // if lore contains a value placeholder
                        // retrieve the condition matching the placeholder, and insert value into perk lore if there is one
                        UnlockCondition condition = p.getConditions().stream().filter(c -> l.contains(String.format("%%%s%%", c.getValuePlaceholder()))).findAny().orElse(null);
                        if (condition != null && condition.getConditionMessages() != null && !condition.getConditionMessages().isEmpty()){
                            // inserts lore if placeholder is present
                            iconLore.addAll(Utils.chat(condition.getConditionMessages()));
                        }
                    } else if (UnlockConditionRegistry.getFailurePlaceholders().stream().anyMatch(c -> l.contains(String.format("%%%s%%", c)))) { // if lore contains a failure placeholder
                        // retrieve the condition matching the placeholder, and insert value into perk lore if there is one
                        UnlockCondition condition = p.getConditions().stream().filter(c -> l.contains(String.format("%%%s%%", c.getFailurePlaceholder()))).findAny().orElse(null);
                        if (unlockedStatus == 0 && !p.metConditionRequirements(target, false) && condition != null && !StringUtils.isEmpty(condition.getFailedConditionMessage())){
                            // inserts lore if placeholder is present but also if target did not meet conditions
                            iconLore.add(Utils.chat(condition.getFailedConditionMessage()));
                        }
                    } else if (ResourceExpenseRegistry.getValuePlaceholders().stream().anyMatch(c -> l.contains(String.format("%%%s%%", c)))) { // if lore contains a cost placeholder
                        // retrieve the expense matching the placeholder, and insert value into perk lore if there is one
                        ResourceExpense condition = p.getExpenses().stream().filter(c -> l.contains(String.format("%%%s%%", c.getCostMessage()))).findAny().orElse(null);
                        if (condition != null && !StringUtils.isEmpty(condition.getCostMessage())){
                            // inserts lore if placeholder is present
                            iconLore.add(Utils.chat(condition.getCostMessage()));
                        }
                    } else if (ResourceExpenseRegistry.getFailurePlaceholders().stream().anyMatch(c -> l.contains(String.format("%%%s%%", c)))) { // if lore contains a cost placeholder
                        // retrieve the expense matching the placeholder, and insert value into perk lore if there is one
                        ResourceExpense condition = p.getExpenses().stream().filter(c -> l.contains(String.format("%%%s%%", c.getCostMessage()))).findAny().orElse(null);
                        if (unlockedStatus == 0 && !p.metResourceRequirements(target) && condition != null && !StringUtils.isEmpty(condition.getInsufficientFundsMessage())){
                            // inserts lore if placeholder is present but also if target did not meet conditions
                            iconLore.add(Utils.chat(condition.getInsufficientFundsMessage()));
                        }
                    } else if (l.contains("%warning_levels%")){
                        if (unlockedStatus == 0 &&
                                !p.metLevelRequirement(target) &&
                                !StringUtils.isEmpty(perk_requirement_warning_levels)) iconLore.add(Utils.chat(perk_requirement_warning_levels));
                    } else if (l.contains("%status_unlocked%")){
                        String status = switch (unlockedStatus) {
                            case 1 -> perk_requirement_status_unlocked;
                            case 2 -> perk_requirement_status_permanently_locked;
                            case 3 -> perk_requirement_status_fake_unlocked;
                            default -> null;
                        };
                        if (!StringUtils.isEmpty(status)) {
                            iconLore.add(Utils.chat(status));
                        }
                    } else if (l.contains("%warning_cost%")){
                        if (unlockedStatus == 0 && !p.metResourceRequirements(target)) {
                            for (ResourceExpense expense : p.getExpenses()){
                                if (!expense.canPurchase(target)) iconLore.add(Utils.chat(expense.getInsufficientFundsMessage()));
                            }
                        }
                    } else if (l.contains("%status_unlockable%")){
                        if (unlockedStatus == 0 && p.canUnlock(target)){
                            if (!StringUtils.isEmpty(perk_requirement_status_unlockable)){
                                iconLore.add(Utils.chat(perk_requirement_status_unlockable));
                            }
                        }
                    } else if (l.contains("%cost%")){
                        if (unlockedStatus == 0){
                            for (ResourceExpense expense : p.getExpenses()) iconLore.add(Utils.chat(expense.getCostMessage()));
                        }
                    } else {
                        String line = Utils.chat(PlaceholderRegistry.parse(
                                l.replace("%level_required%", String.valueOf(p.getLevelRequirement()))
                                        .replace("%skill%", p.getSkill().getDisplayName()), target)
                        );
                        iconLore.add(line);
                    }
                }
                perkMeta.setLore(iconLore);

                boolean unlocked = p.hasUnlocked(target);
                boolean visible = p.shouldBeVisible(target);
                int data = unlocked ? p.getCustomModelDataUnlocked() : visible ? p.getCustomModelDataUnlockable() : p.getCustomModelDataVisible();
                if (data > 0) perkMeta.setCustomModelData(0);

                perkMeta.getPersistentDataContainer().set(buttonKey, PersistentDataType.STRING, p.getName());
                perkMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE);
                ItemUtils.setItemMeta(perkIcon, perkMeta);
                skillTree[p.getY() + 2 + yOff][p.getX() + 4 + xOff] = perkIcon;

                for (PerkConnectionIcon i : p.getConnectionLine()) {
                    ItemStack icon = unlocked ?
                            new ItemBuilder(i.getUnlockedMaterial()).data(i.getUnlockedData()).name("").get()
                            : visible ?
                            new ItemBuilder(i.getUnlockableMaterial()).data(i.getUnlockableData()).name("").get() :
                            new ItemBuilder(i.getLockedMaterial()).data(i.getLockedData()).name("").get();
                    skillTree[p.getY() + 2 + yOff][p.getX() + 4 + xOff] = icon;
                }
            }
        }
        return skillTree;
    }

    // If everything goes right, this should return a 9x5 section 2D array of itemstacks given a center point x and y of
    // the whole skill tree map. If the given map isn't at least 9x5 in size, it returns an empty 9x5 array.
    private ItemStack[][] getSkillTreeView(ItemStack[][] fullSkillTree, int centerX, int centerY){
        ItemStack[][] view = new ItemStack[5][9];
        centerX += 4;
        centerY += 2;
        if (centerY - 2 < 0) {
            centerY = 2;
        }
        if (centerY + 2 >= fullSkillTree.length) {
            centerY = fullSkillTree.length - 1;
        }
        if (fullSkillTree.length < 5){
            return view;
        }
        ItemStack[][] skillTreeYSection = Arrays.copyOfRange(fullSkillTree, centerY - 2, centerY + 3);
        for (int i = 0; i < skillTreeYSection.length; i++){
            ItemStack[] row = skillTreeYSection[i];
            if (row != null){
                if (row.length < 9) return view;
                if (centerX - 4 < 0) centerX = 4;
                if (centerX + 4 >= row.length) centerX = row.length - 1;
                ItemStack[] nineWideRow = Arrays.copyOfRange(row, centerX - 4, centerX + 5);
                view[i] = nineWideRow;
            }
        }
        return view;
    }

    private void buildSkillTrees(){
        ValhallaMMO.getInstance().getServer().getScheduler().runTaskAsynchronously(ValhallaMMO.getInstance(), () -> {
            List<Skill> skills = new ArrayList<>(SkillRegistry.getAllSkills().values());
            skills.sort(Comparator.comparingInt(Skill::getSkillTreeMenuOrderPriority));
            for (Skill s : skills){
                if (!s.isLevelableSkill()) continue;
                ItemStack skillIcon = new ItemBuilder(s.getIcon())
                        .name(s.getDisplayName())
                        .lore(StringUtils.separateStringIntoLines(Utils.chat(s.getDescription()), 40))
                        .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS)
                        .stringTag(buttonKey, s.getType())
                        .get();

                skillIcons.add(skillIcon);
                skillTrees.put(s.getType(), getSkillTree(s));
            }
            // makes sure there are enough items in the skillIcons to fill a 9-item row of icons
            for (int i = 0; i < 9; i++){
                if (skillIcons.size() >= 9) break;
                skillIcons.addAll(new ArrayList<>(skillIcons));
            }
            setMenuItems();
        });
    }
}
