package me.athlaeos.valhallammo;

import me.athlaeos.valhallammo.crafting.CustomRecipeRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierPriority;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierRegistry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DefaultAttributeScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_stats.DurabilityScale;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.rewards.SkillExperience;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.SlotEntry;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.ConfigurableMaterialsChoice;
import me.athlaeos.valhallammo.crafting.ingredientconfiguration.implementations.MaterialChoice;
import me.athlaeos.valhallammo.crafting.recipetypes.DynamicGridRecipe;
import me.athlaeos.valhallammo.item.CustomItem;
import me.athlaeos.valhallammo.item.CustomItemRegistry;
import me.athlaeos.valhallammo.loot.LootTableRegistry;
import me.athlaeos.valhallammo.loot.ReplacementEntry;
import me.athlaeos.valhallammo.loot.ReplacementPool;
import me.athlaeos.valhallammo.loot.ReplacementTable;
import me.athlaeos.valhallammo.trading.CustomMerchantManager;
import me.athlaeos.valhallammo.trading.dom.MerchantLevel;
import me.athlaeos.valhallammo.trading.dom.MerchantTrade;
import me.athlaeos.valhallammo.trading.dom.MerchantType;
import me.athlaeos.valhallammo.trading.dom.ProfessionWrapper;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Scripts implements Listener {
    private static final QuickTrade[] args = new QuickTrade[]{
            new QuickTrade("librarian_buy_random_custom_enchanted_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "E6x W25 4-25%-8xEMERALD_BLOCK for 1xBOOK NOGIFT EXP=3200 ENCH=32 VIL=32 ORD=16 ITEMREF=random_custom_enchanted_book"),
            new QuickTrade("librarian_buy_random_custom_enchanted_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, ""),
            new QuickTrade("librarian_buy_random_custom_enchanted_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, ""),
            new QuickTrade("librarian_buy_random_custom_enchanted_book", ProfessionWrapper.LIBRARIAN, MerchantLevel.MASTER, "")
            // TODO create custom enchantments and add to enchanter exclusive trades
            // custom enchantments
            // Dreamgate (levelled) replaces parry. sneak right click (hold) sword into ground surface to create waypoint.
            // requires block placement permissions. sneak right click (hold) sword into sky (looking up) to teleport to waypoint.
            // cost scales with distance. enchantment level increases max distance. location is saved on sword

            // Resonance (levelled) replaces vein miner. right clicking pickaxe onto rock sends a resonating wave, briefly lighting up
            // all ores around. radius scales with level. consumes durability

            // Mjolnir (levelled) replaces overhead slam. right clicking right after jumping lunges player forward.
            // while in the air after lunging forward, hitting an entity deals a lot more damage in a larger aoe
            // lunge range increases with levels, radius and damage increase with height (up to limit)

            // Knocking (levelled) replaces tree capitator. shift mining a log block shakes the tree heavily, scanning the
            // tree for leaf blocks and generating drops for each leaf block that would be broken (as well as custom loot)
            // the player's loot luck increases with level. the drops are dropped down from the leaves but leave the tree and
            // its leaves intact. this cannot be repeated on the same tree for some time

            // Homing (levelled) replaces charged shot. firing charged shots do not benefit from charged shot stats.
            // charged shots fly much slower, are not affected by gravity, but home in on the nearest target to
            // the crosshair (within fov). right before hitting, the arrow's velocity is restored to its previous velocity.
            // homing strength increases with level

            // Philosopher's Stone (levelled) replaces divine harvest, requires transmutation. Divine harvest still harvests
            // all crops in a large field, but all the drops are converted to other items.
            // beetroots are converted to diamonds. wheat to emeralds. potatoes to iron. carrots to gold. nether warts to coal. cocoa to copper
            // conversion isn't 1:1 and depends on material

            // Aspect of Kali (levelled) replaces rage. Taking damage that would have killed you starts a timer, during
            // you are invulnerable and deal increased damage. your health bar now represents this timer, starting at full
            // but ticking down. you cannot heal during this period. you also cannot lose exhaustion during this time.
            // if you kill the entity that dealt the finishing blow to you during this time, the timer stops and you
            // remain at the health the timer set you at. if you fail to kill the killer, you simply run out of health
            // and die anyway. this utilizes a resurrection event. Duration and damage increase with levels.
            // a message should be sent telling you to kill your attacker

            // Aspect of Hermes (levelled) replaces adrenaline. Taking damage that would have killed you starts a timer.
            // you are teleported a distance away from the attacker, cleansed from all debuffs, bleeds, fire, and are granted
            // a speed boost. you stay at minimal health during this time and cannot heal. taking a single hit of damage
            // from the attacker kills you, but nothing else can harm you. if you take no damage from the attacker during
            // by the end of the timer, you heal to some percentage of your hp. teleport distance and speed increase with levels, duration decreases

            // Divine Judgement (levelled) replaces disarming. fully disarming the opponent instead equalizes the hp of the grappler
            // and the opponent. the grappler is then granted additional attack damage equal to some fraction of the difference in HP for some time
            // levels increase the damage dealt
    };

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if (!e.getPlayer().getName().equalsIgnoreCase("athlaeos") && !e.getPlayer().getName().equalsIgnoreCase("altlaeos")) return;
        if (e.getMessage().startsWith("runthing"))
            run();
    }

    private static final Map<ProfessionWrapper, Map<MerchantLevel, Integer>> typeCounter = new HashMap<>();

    public static void run(){
//        for (ProfessionWrapper profession : ProfessionWrapper.values()){
//            String id = String.format("%s_simple", profession.toString().toLowerCase());
//            if (CustomMerchantManager.getMerchantType(id) == null) {
//                CustomMerchantManager.registerMerchantType(new MerchantType(id));
//                CustomMerchantManager.getMerchantConfiguration(profession.getProfession()).getMerchantTypes().add(id);
//            }
//        }
//
//        for (QuickTrade trade : args){
//            Map<MerchantLevel, Integer> counter = typeCounter.getOrDefault(trade.profession, new HashMap<>());
//            int prio = counter.getOrDefault(trade.level, 0);
//            counter.put(trade.level, prio + 1);
//            typeCounter.put(trade.profession, counter);
//            String id = String.format("%s_simple", trade.profession.toString().toLowerCase());
//            MerchantTrade t = parse(trade.id, trade.arg);
//            t.setPriority(prio);
//            CustomMerchantManager.registerTrade(t);
//
//            MerchantType type = CustomMerchantManager.getMerchantType(id);
//            type.addTrade(trade.level, t);
//        }
//
//        for (DynamicGridRecipe recipe : CustomRecipeRegistry.getGridRecipes().values()){
//            if (!isGoodRecipe(recipe.getName())) continue;
//            if (!recipe.getName().contains("arrow")) {
//                DynamicItemModifier qualitySetModifier = recipe.getModifiers().stream().filter(m -> m instanceof SmithingQualityScale).map(DynamicItemModifier::copy).findFirst().orElse(null);
//                if (qualitySetModifier == null) continue;
//                qualitySetModifier.setPriority(ModifierPriority.SOONEST);
//
//                String scalingItemID = String.format("%s_scaling", recipe.getName().replace("craft_", ""));
//                CustomItem scalingItem = CustomItemRegistry.getItem(scalingItemID);
//                if (scalingItem == null) {
//                    scalingItem = new CustomItem(scalingItemID, new ItemStack(Material.BARRIER));
//                    CustomItemRegistry.register(scalingItemID, scalingItem);
//                }
//                scalingItem.setItem(recipe.getResult());
//
//                List<DynamicItemModifier> filteredRecipeModifiers = new ArrayList<>(recipe.getModifiers().stream().map(DynamicItemModifier::copy).toList());
//                filteredRecipeModifiers.removeIf(m -> m instanceof SkillExperience || m instanceof SmithingQualityScale);
//                scalingItem.setModifiers(filteredRecipeModifiers);
//
//                List<DynamicItemModifier> attributeScalingAndExperienceModifiers = new ArrayList<>(recipe.getModifiers().stream().filter(m -> m instanceof SkillExperience).map(DynamicItemModifier::copy).toList());
//                attributeScalingAndExperienceModifiers.add(qualitySetModifier);
//                ItemReplaceByIndexed modifier = (ItemReplaceByIndexed) ModifierRegistry.createModifier("replace_by_custom");
//                modifier.setOnlyExecuteModifiers(true);
//                modifier.setItem(scalingItemID);
//                modifier.setPriority(ModifierPriority.SOON);
//                attributeScalingAndExperienceModifiers.add(modifier);
//                recipe.setModifiers(attributeScalingAndExperienceModifiers);
//            }
//
//            String nonScalingItemID = String.format("%s", recipe.getName().replace("craft_", ""));
//            CustomItem nonScalingItem = CustomItemRegistry.getItem(nonScalingItemID);
//            if (nonScalingItem == null) nonScalingItem = new CustomItem(nonScalingItemID, new ItemStack(Material.BARRIER));
//            nonScalingItem.setItem(recipe.getResult());
//            nonScalingItem.getItem().setAmount(1);
//
//            List<DynamicItemModifier> filteredRecipeModifiers = new ArrayList<>(recipe.getModifiers().stream().map(DynamicItemModifier::copy).toList());
//            filteredRecipeModifiers.removeIf(m -> m instanceof DefaultAttributeScale || m instanceof DurabilityScale || m instanceof SkillExperience || m instanceof SmithingQualityScale);
//            nonScalingItem.setModifiers(filteredRecipeModifiers);
//        }
//
//        ReplacementTable table = new ReplacementTable("loot_valhallafication");
//        for (String p : pools){
//            ReplacementPool pool = table.addPool(p);
//            String material = p.replace("_swords", "")
//                    .replace("_tools", "")
//                    .replace("_armor", "");
//            List<DynamicItemModifier> modifiers = new ArrayList<>();
//            SmithingQualitySet m1 = (SmithingQualitySet) ModifierRegistry.createModifier("smithing_quality_set");
//            m1.setQuality(baseQualityValues.get(material));
//            m1.setPriority(ModifierPriority.SOONEST);
//            SmithingQualityRandomized m2 = (SmithingQualityRandomized) ModifierRegistry.createModifier("smithing_quality_randomize");
//            m2.setLowerBound(-0.2);
//            m2.setUpperBound(0.2);
//            m2.setPriority(ModifierPriority.SOON);
//            modifiers.add(m1);
//            modifiers.add(m2);
//            if (p.contains("swords")) {
//                Material weapon = ofConjugation(material, "sword");
//                pool.setToReplace(new SlotEntry(new ItemStack(weapon), new MaterialChoice()));
//
//                weaponEntry(pool, weapon, "sword", material, modifiers);
//                weaponEntry(pool, weapon, "rapier", material, modifiers);
//                weaponEntry(pool, weapon, "dagger", material, modifiers);
//                weaponEntry(pool, weapon, "mace", material, modifiers);
//                weaponEntry(pool, weapon, "spear", material, modifiers);
//                weaponEntry(pool, weapon, "warhammer", material, modifiers);
//                weaponEntry(pool, weapon, "greataxe", material, modifiers);
//            } else if (p.contains("tools")) {
//                Material pickaxe = ofConjugation(material, "pickaxe");
//                ConfigurableMaterialsChoice choice = new ConfigurableMaterialsChoice();
//                Material axe = ofConjugation(material, "axe");
//                Material hoe = ofConjugation(material, "hoe");
//                Material shovel = ofConjugation(material, "shovel");
//                choice.setValidChoices(new HashSet<>(Set.of(axe, hoe, shovel, pickaxe)));
//                pool.setToReplace(new SlotEntry(new ItemStack(pickaxe), choice));
//
//                ItemReplaceByIndexed m3 = (ItemReplaceByIndexed) ModifierRegistry.createModifier("replace_by_custom");
//                m3.setOnlyExecuteModifiers(true);
//                m3.setItem(createItemIfAbsent(material + "_pickaxe_scaling"));
//                modifiers.add(m3);
//                ReplacementEntry entry = pool.addEntry(new ItemStack(pickaxe));
//                entry.setTinker(true);
//                entry.setModifiers(modifiers);
//            } else if (p.contains("armor")) {
//                ConfigurableMaterialsChoice choice = new ConfigurableMaterialsChoice();
//                Material helmet = ofConjugation(material, "helmet");
//                Material chestplate = ofConjugation(material, "chestplate");
//                Material leggings = ofConjugation(material, "leggings");
//                Material boots = ofConjugation(material, "boots");
//                choice.setValidChoices(new HashSet<>(Set.of(helmet, chestplate, leggings, boots)));
//                pool.setToReplace(new SlotEntry(new ItemStack(chestplate), choice));
//
//                ItemReplaceByIndexed m3 = (ItemReplaceByIndexed) ModifierRegistry.createModifier("replace_by_custom");
//                m3.setOnlyExecuteModifiers(true);
//                m3.setItem(createItemIfAbsent(material + "_chestplate_scaling"));
//                modifiers.add(m3);
//                ReplacementEntry entry = pool.addEntry(new ItemStack(chestplate));
//                entry.setTinker(true);
//                entry.setModifiers(modifiers);
//            } else if (p.contains("bows")) {
//                ConfigurableMaterialsChoice choice = new ConfigurableMaterialsChoice();
//                Material bow = Material.BOW;
//                Material crossbow = Material.CROSSBOW;
//                choice.setValidChoices(new HashSet<>(Set.of(bow, crossbow)));
//                pool.setToReplace(new SlotEntry(new ItemStack(bow), choice));
//
//                ItemReplaceByIndexed m3 = (ItemReplaceByIndexed) ModifierRegistry.createModifier("replace_by_custom");
//                m3.setOnlyExecuteModifiers(true);
//                m3.setItem(createItemIfAbsent("bow_scaling"));
//                modifiers.add(m3);
//                ReplacementEntry entry = pool.addEntry(new ItemStack(bow));
//                entry.setTinker(true);
//                entry.setModifiers(modifiers);
//            } else if (p.contains("tridents")) {
//                Material trident = Material.TRIDENT;
//                pool.setToReplace(new SlotEntry(new ItemStack(trident), new MaterialChoice()));
//
//                ItemReplaceByIndexed m3 = (ItemReplaceByIndexed) ModifierRegistry.createModifier("replace_by_custom");
//                m3.setOnlyExecuteModifiers(true);
//                m3.setItem(createItemIfAbsent("trident_scaling"));
//                modifiers.add(m3);
//                ReplacementEntry entry = pool.addEntry(new ItemStack(trident));
//                entry.setTinker(true);
//                entry.setModifiers(modifiers);
//            }
//        }
//        LootTableRegistry.registerReplacementTable(table, true);
    }

    private static void weaponEntry(ReplacementPool pool, Material weapon, String weaponType, String material, List<DynamicItemModifier> modifiers){
        modifiers = new ArrayList<>(modifiers.stream().map(DynamicItemModifier::copy).toList());
        ReplacementEntry sword = pool.addEntry(new ItemStack(weapon));
        sword.setTinker(true);

        ItemReplaceByIndexed m3 = (ItemReplaceByIndexed) ModifierRegistry.createModifier("replace_by_custom");
        m3.setOnlyExecuteModifiers(true);
        m3.setItem(createItemIfAbsent(material + "_" + weaponType + "_scaling"));
        m3.setPriority(ModifierPriority.NEUTRAL);
        modifiers.add(m3);

        sword.setModifiers(modifiers);
    }

    private static final Map<String, Integer> baseQualityValues = Map.of(
            "wooden", 80,
            "leather", 80,
            "stone", 110,
            "chainmail", 110,
            "iron", 140,
            "golden", 140,
            "diamond", 170,
            "tridents", 155,
            "bows", 110,
            "crossbows", 110
    );

    private static String createItemIfAbsent(String item){
        CustomItem scalingItem = CustomItemRegistry.getItem(item);
        if (scalingItem == null) {
            scalingItem = new CustomItem(item, new ItemStack(Material.BARRIER));
            CustomItemRegistry.register(item, scalingItem);
        }
        return item;
    }

    private static Material ofConjugation(String material, String type){
        return Material.valueOf(material.toUpperCase() + "_" + type.toUpperCase());
    }

    private static final String[] pools = new String[]{
            "wooden_swords",
            "stone_swords",
            "iron_swords",
            "golden_swords",
            "diamond_swords",
            "wooden_tools",
            "stone_tools",
            "iron_tools",
            "golden_tools",
            "diamond_tools",
            "leather_armor",
            "chainmail_armor",
            "iron_armor",
            "golden_armor",
            "diamond_armor",
            "bows",
            "crossbows",
            "tridents"
    };

    private static boolean isGoodRecipe(String name){
        return name.startsWith("craft_wooden_") ||
                name.startsWith("craft_leather_") ||
                name.startsWith("craft_stone_") ||
                name.startsWith("craft_copper_") ||
                name.startsWith("craft_chainmail_") ||
                name.startsWith("craft_iron_") ||
                name.startsWith("craft_golden_") ||
                name.startsWith("craft_diamond_") ||
                name.startsWith("craft_elytra") ||
                name.startsWith("craft_unholy_") ||
                name.startsWith("craft_ender");
    }

    private static MerchantTrade parse(String name, String arg){
        MerchantTrade existing = CustomMerchantManager.getTrade(name);
        if (existing != null) return existing;

        String[] args = arg.split(" ");
        boolean exclusive = args[0].startsWith("E");
        int quant = Integer.parseInt(args[0].replace("E", "").replace("x", ""));
        float weight = args[1].equalsIgnoreCase("100%") ? -1 : Float.parseFloat(args[1].replace("W", ""));

        Material priceItem = Material.valueOf(args[2].split("x")[1]);
        String[] priceArgs = args[2].split("x")[0].split("-");
        int priceLowest = Integer.parseInt(priceArgs[0]);
        float priceDemandStep = Float.parseFloat(priceArgs[1].replace("%", "")) / 100;
        int priceHighest = Integer.parseInt(priceArgs[2]);

        int resultQuantity = Integer.parseInt(args[4].split("x")[0]);
        Material resultItem = Material.valueOf(args[4].split("x")[1]);

        boolean isBuyTrade = args[2].contains("EMERALD");
        int multiplier = (args[2].contains("EMERALD_BLOCK") ? 10 : 1) * (args[2].contains("EMERALD") ? priceLowest + 1 : -1);
        float giftWeight = args[5].equalsIgnoreCase("NOGIFT") ? 0 : Float.parseFloat(args[5].replace("G", ""));
        float skillExp = multiplier > 1 ? multiplier * 100 : Float.parseFloat(args[6].replace("EXP=", ""));
        float enchantingExp = multiplier > 1 ? multiplier : Float.parseFloat(args[7].replace("ENCH=", ""));
        int merchantExp = multiplier > 1 ? multiplier : Integer.parseInt(args[8].replace("VIL=", ""));
        String itemRef = args.length > 10 && args[10].startsWith("ITEMREF=") ? args[10].replaceFirst("ITEMREF=", "") : null;
        int orderCount = 0;
        if (isBuyTrade) orderCount = 2 * quant;

        Map<Integer, String> dynRefs = new HashMap<>();
        if (args.length > 10 && args[10].startsWith("DYNREF=")) {
            String[] split = args[10].replace("DYNREF=", "").split("/");
            for (String s : split){
                String[] a = s.split(":");
                dynRefs.put(Integer.parseInt(a[0]), a[1]);
            }
        }

        MerchantTrade trade = new MerchantTrade(name);
        trade.setMaxUses(quant);
        trade.setSkillExp(skillExp);
        trade.setMaxOrderCount(orderCount);

        int negativeOffset = Math.round(priceLowest * -0.2F);
        int positiveOffset = Math.round(priceLowest * 0.2F);
        trade.setPriceRandomNegativeOffset(negativeOffset);
        trade.setPriceRandomPositiveOffset(positiveOffset);
        trade.setEnchantingExperience(enchantingExp);
        trade.setDemandPriceMax(priceHighest - priceLowest);
        trade.setDemandPriceMultiplier(priceDemandStep);
        trade.setVillagerExperience(merchantExp);
        trade.setRefreshes(true);
        trade.setGiftWeight(giftWeight);
        trade.setScalingCostItem(new ItemStack(priceItem, priceLowest));
        trade.setWeight(weight);
        trade.setResult(new ItemStack(resultItem, resultQuantity));
        trade.setExclusive(exclusive);

        if (itemRef != null){
            if (CustomItemRegistry.getItem(itemRef) == null) CustomItemRegistry.register(itemRef, new ItemStack(Material.BARRIER));
            ItemReplaceByIndexed modifier = (ItemReplaceByIndexed) ModifierRegistry.createModifier("replace_by_custom");
            modifier.setItem(itemRef);
            modifier.setPriority(ModifierPriority.NEUTRAL);
            trade.getModifiers().add(modifier);
        } else if (!dynRefs.isEmpty()){
            ItemReplaceByIndexedBasedOnQuality modifier = (ItemReplaceByIndexedBasedOnQuality) ModifierRegistry.createModifier("replace_by_custom_quality_based");
            for (Integer quality : dynRefs.keySet()){
                String item = dynRefs.get(quality);
                if (CustomItemRegistry.getItem(item) == null) CustomItemRegistry.register(item, new ItemStack(Material.BARRIER));
            }
            modifier.setItems(dynRefs);
            modifier.setPriority(ModifierPriority.NEUTRAL);
            trade.getModifiers().add(modifier);
        }

        return trade;
    }

    private static class QuickTrade{
        private final String id;
        private final ProfessionWrapper profession;
        private final MerchantLevel level;
        private final String arg;

        private QuickTrade(String id, ProfessionWrapper profession, MerchantLevel level, String arg){
            this.id = id;
            this.profession = profession;
            this.level = level;
            this.arg = arg;
        }
    }
}
