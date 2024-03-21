package me.athlaeos.valhallammo.gui.implementations;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.dom.Pair;
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
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.skills.perkresourcecost.ResourceExpenseRegistry;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockCondition;
import me.athlaeos.valhallammo.skills.perkunlockconditions.UnlockConditionRegistry;
import me.athlaeos.valhallammo.skills.skills.PerkRegistry;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;
import me.athlaeos.valhallammo.skills.skills.implementations.PowerSkill;
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
    private static final NamespacedKey buttonKey = new NamespacedKey(ValhallaMMO.getInstance(), "valhalla_button_id");
    private static final Map<Skill, List<List<List<Perk>>>> skillTrees = new HashMap<>(); // first list represents rows, second columns, third perks on the location
    private static final Map<Skill, Pair<Integer, Integer>> coordinateOffsets = new HashMap<>();

    static {
        updateSkillTrees();
    }


    private final Player target;
    private final List<ItemStack> skillIcons = new ArrayList<>();
    private Skill selectedSkill;
    private final Map<Skill, ItemStack[][]> skillTreeItems = new HashMap<>();
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

    public SkillTreeMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);

        selectedSkill = SkillRegistry.getSkill(PowerSkill.class);
        x = selectedSkill.getCenterX() + 4;
        y = selectedSkill.getCenterY() + 2;
        target = playerMenuUtility.getOwner();

        buildSkillTrees();
    }

    public SkillTreeMenu(PlayerMenuUtility playerMenuUtility, Player target) {
        super(playerMenuUtility);

        selectedSkill = SkillRegistry.getSkill(PowerSkill.class);
        this.x = selectedSkill.getCenterX() + 4;
        this.y = selectedSkill.getCenterY() + 2;
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

    private String perkConfirmation = null;

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
            if (selectedSkill.isNavigable()){
                if ((s == 0 || s == 4 || s == 8) && y - 1 >= 2) y -= 1; // the three northern buttons should subtract y by 1
                if ((s == 36 || s == 40 || s == 44) && y + 3 < currentSkillTreeHeight()) y += 1; // the three southern buttons should increase y by 1
                if ((s == 0 || s == 18 || s == 36) && x - 1 >= 4) x -= 1; // the three western buttons should subtract x by 1
                if ((s == 8 || s == 26 || s == 44) && x + 5 < currentSkillTreeWidth()) x += 1; // the three eastern buttons should increase x by 1
                if (x != x2 || y != y2) {
                    // navigational buttons were clicked, so nothing else needs to run
                    setMenuItems();
                    perkConfirmation = null;
                    return;
                }
            }

            if (meta.getPersistentDataContainer().has(buttonKey, PersistentDataType.STRING)){
                String id = meta.getPersistentDataContainer().get(buttonKey, PersistentDataType.STRING);

                if (s >= 45) {
                    Skill selectedSkill = SkillRegistry.getSkill(id);
                    if (selectedSkill != null) {
                        this.selectedSkill = selectedSkill;
                        this.x = selectedSkill.getCenterX() + 4;
                        this.y = selectedSkill.getCenterY() + 2;
                    }
                } else {
                    if (selectedSkill == null){
                        setMenuItems();
                        return;
                    }
                    Perk p = PerkRegistry.getPerk(id);
                    if (p != null){
                        if (p.canUnlock(target)){
                            if (perkConfirmation != null && perkConfirmation.equals(p.getName())){
                                perkConfirmation = null;
                                // persist perk as unlocked
                                PowerProfile account = ProfileRegistry.getPersistentProfile(target, PowerProfile.class);

                                Collection<String> perks = account.getUnlockedPerks();
                                perks.add(p.getName());
                                account.setUnlockedPerks(perks);

                                ProfileRegistry.setPersistentProfile(target, account, PowerProfile.class);

                                // execute perk's rewards
                                p.execute(target);

                                // remove resources
                                for (ResourceExpense expense : p.getExpenses()) expense.purchase(target, true);

                                ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), this::setMenuItems, 2L);
                            } else perkConfirmation = p.getName();
                        } else {
                            for (ResourceExpense expense : p.getExpenses()){
                                if (!expense.canPurchase(target)) Utils.sendMessage(playerMenuUtility.getOwner(), expense.getInsufficientFundsMessage());
                            }
                        }
                        skillTreeItems.put(selectedSkill, getSkillTree(selectedSkill));
                    }
                }
            } else {
                perkConfirmation = null;
            }
        }
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {

    }

    private int currentSkillTreeHeight(){
        return skillTreeItems.get(selectedSkill).length;
    }

    private int currentSkillTreeWidth(){
        return skillTreeItems.get(selectedSkill)[0].length;
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        setScrollBar();
        if (selectedSkill != null && skillTreeItems.containsKey(selectedSkill)){
            ItemStack[][] treeView = getSkillTreeView(selectedSkill);
            if (!ArrayUtils.isEmpty(treeView)){
                int index = 0;
                if (treeView.length >= 5){
                    for (int r = 0; r < 5; r++){
                        ItemStack[] row = treeView[r];
                        if (row.length < 9) continue;
                        for (int i = 0; i < 9; i++){
                            if (row[i] != null) inventory.setItem(index, row[i]);
                            index++;
                        }
                    }
                }
            }
            if (selectedSkill.isNavigable()){
                inventory.setItem(0, directionNW);
                inventory.setItem(4, directionN);
                inventory.setItem(8, directionNE);
                inventory.setItem(18, directionW);
                inventory.setItem(26, directionE);
                inventory.setItem(36, directionSW);
                inventory.setItem(40, directionS);
                inventory.setItem(44, directionSE);
            }
        }
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
                    PowerProfile acc = ProfileRegistry.getMergedProfile(target, PowerProfile.class);
                    meta.setDisplayName(Utils.chat(s.getDisplayName() + (acc.getNewGamePlus() > 0 ?
                            TranslationManager.getTranslation("prestige_level_format")
                                    .replace("%prestige_roman%", StringUtils.toRoman(acc.getNewGamePlus())
                                            .replace("%prestige_numeric%", String.valueOf(acc.getNewGamePlus()))) :
                            "")));

                    Profile p = ProfileRegistry.getPersistentProfile(target, s.getProfileType());
                    double expRequired = s.expForLevel(p.getLevel() + 1);
                    List<String> lore = new ArrayList<>();
                    for (String line : TranslationManager.getListTranslation("skilltree_icon_format")){
                        lore.add(Utils.chat(line
                                .replace("%level_current%", "" + p.getLevel())
                                .replace("%exp_current%", String.format("%.2f", p.getEXP()))
                                .replace("%exp_next%", (expRequired < 0) ? TranslationManager.getTranslation("max_level") : String.format("%.2f", expRequired))
                                .replace("%exp_total%", String.format("%.2f", p.getTotalEXP()))
                                .replace("%prestigepoints%", String.valueOf((acc.getSpendablePrestigePoints() - acc.getSpentPrestigePoints())))
                                .replace("%skillpoints%", String.valueOf((acc.getSpendableSkillPoints() - acc.getSpentSkillPoints())))));
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
        if (skill == null || !skillTrees.containsKey(skill)) return null;
        List<List<List<Perk>>> perks = skillTrees.get(skill);
        if (perks.isEmpty() || perks.get(0).isEmpty()) return null;

        ItemStack[][] skillTree = new ItemStack[perks.size()][perks.get(0).size()];
        for (ItemStack[] row : skillTree) Arrays.fill(row, null);

        for (int r = 0; r < perks.size(); r++){
            List<List<Perk>> row = perks.get(r);
            if (row == null) continue;
            for (int c = 0; c < row.size(); c++){
                List<Perk> column = row.get(c);
                if (column == null) continue;
                for (Perk p : column){
                    Pair<Integer, Integer> offsets = coordinateOffsets.get(skill);
                    if (p == null || !p.shouldBeVisible(target) || offsets == null) continue;
                    ItemBuilder icon = new ItemBuilder(p.getIcon())
                            .name(perkConfirmation != null && perkConfirmation.equals(p.getName()) ? TranslationManager.getTranslation("skilltree_perk_confirmation").replace("%perk%", p.getDisplayName()) : p.getDisplayName())
                            .flag(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS)
                            .stringTag(buttonKey, p.getName());
                    int unlockedStatus = p.hasPermanentlyLocked(target) ? 2 : // permanently locked
                            p.hasFakeUnlocked(target) ? 3 : // fake unlocked
                                    p.hasUnlocked(target) ? 1 : // unlocked normally
                                            0; // not unlocked

                    List<String> lore = new ArrayList<>();
                    TranslationManager.getListTranslation("skilltree_perk_format").forEach(l -> {
                        if (l.contains("%description%")) {
                            String description = p.getDescription();
                            for (PerkReward reward : p.getRewards()) description = description.replace("{" + reward.getName() + "}", reward.rewardPlaceholder());
                            lore.addAll(Utils.chat(StringUtils.separateStringIntoLines(description, 40)));
                        } else if (UnlockConditionRegistry.getValuePlaceholders().stream().anyMatch(con -> l.contains(String.format("%%%s%%", con)))) { // if lore contains a value placeholder
                            UnlockCondition condition = p.getConditions().stream().filter(con -> l.contains(String.format("%%%s%%", con.getValuePlaceholder()))).findAny().orElse(null);
                            if (condition != null && condition.getConditionMessages() != null && !condition.getConditionMessages().isEmpty()) lore.addAll(Utils.chat(condition.getConditionMessages()));
                        } else if (UnlockConditionRegistry.getFailurePlaceholders().stream().anyMatch(con -> l.contains(String.format("%%%s%%", con)))) { // if lore contains a failure placeholder
                            UnlockCondition condition = p.getConditions().stream().filter(con -> l.contains(String.format("%%%s%%", con.getFailurePlaceholder()))).findAny().orElse(null);
                            if (unlockedStatus == 0 && !p.metConditionRequirements(target, false) && condition != null && !StringUtils.isEmpty(condition.getFailedConditionMessage())) lore.add(Utils.chat(condition.getFailedConditionMessage()));
                        } else if (ResourceExpenseRegistry.getValuePlaceholders().stream().anyMatch(con -> l.contains(String.format("%%%s%%", con)))) { // if lore contains a cost placeholder
                            ResourceExpense condition = p.getExpenses().stream().filter(con -> l.contains(String.format("%%%s%%", con.getCostMessage()))).findAny().orElse(null);
                            if (condition != null && !StringUtils.isEmpty(condition.getCostMessage())) lore.add(Utils.chat(condition.getCostMessage()));
                        } else if (ResourceExpenseRegistry.getFailurePlaceholders().stream().anyMatch(con -> l.contains(String.format("%%%s%%", con)))) { // if lore contains a cost placeholder
                            ResourceExpense condition = p.getExpenses().stream().filter(con -> l.contains(String.format("%%%s%%", con.getCostMessage()))).findAny().orElse(null);
                            if (unlockedStatus == 0 && !p.metResourceRequirements(target) && condition != null && !StringUtils.isEmpty(condition.getInsufficientFundsMessage())) lore.add(Utils.chat(condition.getInsufficientFundsMessage()));
                        } else if (l.contains("%warning_levels%")){
                            if (unlockedStatus == 0 && !p.metLevelRequirement(target) && !StringUtils.isEmpty(perk_requirement_warning_levels)) lore.add(Utils.chat(perk_requirement_warning_levels));
                        } else if (l.contains("%status_unlocked%")){
                            String status = switch (unlockedStatus) {
                                case 1 -> perk_requirement_status_unlocked;
                                case 2 -> perk_requirement_status_permanently_locked;
                                case 3 -> perk_requirement_status_fake_unlocked;
                                default -> null;
                            };
                            if (!StringUtils.isEmpty(status)) lore.add(Utils.chat(status));
                        } else if (l.contains("%warning_cost%")){
                            if (unlockedStatus == 0 && !p.metResourceRequirements(target)) {
                                for (ResourceExpense expense : p.getExpenses()) if (!expense.canPurchase(target)) lore.add(Utils.chat(expense.getInsufficientFundsMessage()));
                            }
                        } else if (l.contains("%status_unlockable%")){
                            if (unlockedStatus == 0 && p.canUnlock(target) && !StringUtils.isEmpty(perk_requirement_status_unlockable)) lore.add(Utils.chat(perk_requirement_status_unlockable));
                        } else if (l.contains("%cost%")){
                            if (unlockedStatus == 0) for (ResourceExpense expense : p.getExpenses()) lore.add(Utils.chat(expense.getCostMessage()));
                        } else {
                            lore.add(Utils.chat(PlaceholderRegistry.parsePapi(PlaceholderRegistry.parse(l.replace("%level_required%", String.valueOf(p.getLevelRequirement())).replace("%skill%", p.getSkill().getDisplayName()), target), target)));
                        }
                    });
                    icon.lore(lore);
                    boolean unlocked = p.hasUnlocked(target);
                    boolean unlockable = p.canUnlock(target);
                    int data = unlocked ? p.getCustomModelDataUnlocked() : unlockable ? p.getCustomModelDataUnlockable() : p.getCustomModelDataVisible();
                    if (data > 0) icon.data(data);

                    int xOff = offsets.getOne(), yOff = offsets.getTwo();

                    skillTree[r][c] = icon.get();

                    for (PerkConnectionIcon i : p.getConnectionLine()) {
                        int d = unlocked ? i.getUnlockedData() : unlockable ? i.getUnlockableData() : i.getLockedData();
                        Material m = unlocked ? i.getUnlockedMaterial() : unlockable ? i.getUnlockableMaterial() : i.getLockedMaterial();
                        ItemStack line = new ItemBuilder(m).data(d).name("&r").get();
                        skillTree[i.getY() + 2 + yOff][i.getX() + 4 + xOff] = line;
                    }
                }
            }
        }
        return skillTree;
    }

    // If everything goes right, this should return a 9x5 section 2D array of itemstacks given a center point x and y of
    // the whole skill tree map. If the given map isn't at least 9x5 in size, it returns an empty 9x5 array. This should never occur though
    private ItemStack[][] getSkillTreeView(Skill skill){
        ItemStack[][] view = new ItemStack[5][9];
        ItemStack[][] fullSkillTree = skillTreeItems.get(skill);
        int x = Math.max(4, Math.min(fullSkillTree[0].length - 1, this.x + coordinateOffsets.get(skill).getOne()));
        int y = Math.max(2, Math.min(fullSkillTree.length - 1, this.y + coordinateOffsets.get(skill).getTwo()));

        ItemStack[][] skillTreeYSection = Arrays.copyOfRange(fullSkillTree, y - 2, y + 3);// pick 5 rows from the skill tree's height
        for (int i = 0; i < skillTreeYSection.length; i++){
            ItemStack[] row = skillTreeYSection[i];
            if (row != null){
                ItemStack[] nineWideRow = Arrays.copyOfRange(row, x - 4, x + 5); // pick 9 columns from the skill tree's width
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
                skillTreeItems.put(s, getSkillTree(s));
            }
            // makes sure there are enough items in the skillIcons to fill a 9-item row of icons
            for (int i = 0; i < 9; i++){
                if (skillIcons.size() >= 9) break;
                skillIcons.addAll(new ArrayList<>(skillIcons));
            }
            setMenuItems();
        });
    }

    public static void updateSkillTree(Skill skill){
        if (!skill.isLevelableSkill()) return;
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE, offsetX = 0, offsetY = 0;
        boolean initialized = false;
        for (Perk perk : skill.getPerks()){
            minX = Math.min(perk.getX(), minX);
            maxX = Math.max(perk.getX(), maxX);
            minY = Math.min(perk.getY(), minY);
            maxY = Math.max(perk.getY(), maxY);
            initialized = true;
            for (PerkConnectionIcon line : perk.getConnectionLine()){
                minX = Math.min(line.getX(), minX);
                maxX = Math.max(line.getX(), maxX);
                minY = Math.min(line.getY(), minY);
                maxY = Math.max(line.getY(), maxY);
            }
        }
        if (!initialized) {
            coordinateOffsets.put(skill, new Pair<>(0, 0));
            List<List<List<Perk>>> perks = new ArrayList<>();
            for (int i = 0; i < 5; i++){
                List<List<Perk>> rows = new ArrayList<>();
                for (int o = 0; o < 9; o++) rows.add(null);
                perks.add(rows);
            }
            skillTrees.put(skill, perks);
            return;
        }
        if (minX != 0) offsetX = -minX; // if the minimum value of x isn't 0, all perks should be offset by the amount that would make it 0.
        if (minY != 0) offsetY = -minY; // same with the y values. after all that is done we're left with a normalized skill tree with all positive locations starting at 0,0
        coordinateOffsets.put(skill, new Pair<>(offsetX, offsetY));

        int width = Math.max(9, (maxX + offsetX) - (minX + offsetX)) + 8, height = Math.max(5, (maxY + offsetY) - (minY + offsetY)) + 4; // define width and height with an additional 8 and 4 spaces so the array comes out at least 9x5 in size

        List<List<List<Perk>>> perks = new ArrayList<>();
        for (int i = 0; i <= height; i++){
            List<List<Perk>> rows = new ArrayList<>();
            for (int o = 0; o < width; o++) rows.add(null);
            perks.add(rows);
        }

        for (Perk perk : skill.getPerks()){
            List<Perk> perksAtSpot = perks.get(perk.getY() + 2 + offsetY).get(perk.getX() + 4 + offsetX); // multiple perks can share the same location, so it must be a list
            if (perksAtSpot == null) perksAtSpot = new ArrayList<>();
            perksAtSpot.add(perk);
            if (perksAtSpot.size() > 1) perksAtSpot.sort(Comparator.comparingInt(Perk::getLevelRequirement));
            perks.get(perk.getY() + 2 + offsetY).set(perk.getX() + 4 + offsetX, perksAtSpot);
        }

        skillTrees.put(skill, perks);
    }
    public static void updateSkillTrees(){
        skillTrees.clear();
        coordinateOffsets.clear();
        for (Skill skill : SkillRegistry.getAllSkills().values()) updateSkillTree(skill);
    }
}
