package me.athlaeos.valhallammo.version;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.configuration.ConfigManager;
import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.dom.Structures;
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.listeners.LootListener;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.DiggingProfile;
import me.athlaeos.valhallammo.skills.skills.implementations.DiggingSkill;
import me.athlaeos.valhallammo.utility.BlockStore;
import me.athlaeos.valhallammo.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.StringUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.bukkit.block.data.Brushable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

import java.util.*;

public class DiggingArchaeologyExtension implements Listener {
    private final DiggingSkill skill;
    private final Map<Structures, Integer> structureRadiusMap = new HashMap<>();
    private final Map<Structures, LootTables> structureLootTableMap = new HashMap<>();
    private final Map<Material, Double> gravelConversionBlocks = new HashMap<>();
    private final Map<Material, Double> sandConversionBlocks = new HashMap<>();
    private final Map<Material, Double> archaeologyExpValues = new HashMap<>();
    private final List<LootTables> rareLootTables = new ArrayList<>();
    private final List<LootTables> commonLootTables = new ArrayList<>();

    public DiggingArchaeologyExtension(DiggingSkill skill){
        this.skill = skill;
        ValhallaMMO.getInstance().save("skills/digging_progression.yml");
        ValhallaMMO.getInstance().save("skills/digging.yml");

        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/digging.yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/digging_progression.yml").get();

        ConfigurationSection expSection = progressionConfig.getConfigurationSection("experience.archaeology_brush");
        if (expSection != null){
            Collection<String> invalidMaterials = new HashSet<>();
            for (String m : expSection.getKeys(false)){
                Material material = Catch.catchOrElse(() -> Material.valueOf(m), null);
                if (material == null) invalidMaterials.add(m);
                else archaeologyExpValues.put(material, progressionConfig.getDouble("experience.archaeology_brush." + m));
            }
            if (!invalidMaterials.isEmpty()) {
                ValhallaMMO.logWarning("The following materials in skills/digging_progression.yml do not exist, no exp values set (ignore warning if your version does not have these materials)");
                ValhallaMMO.logWarning(String.join(", ", invalidMaterials));
            }
        }
        Collection<String> invalidMaterials = new HashSet<>();
        for (String m : skillConfig.getStringList("archaeology_convertable_gravel")){
            String[] args = m.split(":");
            double multiplier = args.length > 1 ? Catch.catchOrElse(() -> StringUtils.parseDouble(args[1]), 1D) : 1;
            Material material = Catch.catchOrElse(() -> Material.valueOf(args[0]), null);
            if (material == null) invalidMaterials.add(args[0]);
            else gravelConversionBlocks.put(material, multiplier);
        }
        for (String m : skillConfig.getStringList("archaeology_convertable_sand")){
            String[] args = m.split(":");
            double multiplier = args.length > 1 ? Catch.catchOrElse(() -> StringUtils.parseDouble(args[1]), 1D) : 1;
            Material material = Catch.catchOrElse(() -> Material.valueOf(args[0]), null);
            if (material == null) invalidMaterials.add(args[0]);
            else sandConversionBlocks.put(material, multiplier);
        }
        if (!invalidMaterials.isEmpty()) {
            ValhallaMMO.logWarning("The following materials in skills/digging.yml do not exist, blocks cannot be converted to suspicious blocks (ignore warning if your version does not have these materials)");
            ValhallaMMO.logWarning(String.join(", ", invalidMaterials));
        }

        for (String table : skillConfig.getStringList("archaeology_rare_loot_tables")){
            LootTables lootTable = Catch.catchOrElse(() -> LootTables.valueOf(table), null);
            rareLootTables.add(lootTable);
        }
        for (String table : skillConfig.getStringList("archaeology_common_loot_tables")){
            LootTables lootTable = Catch.catchOrElse(() -> LootTables.valueOf(table), null);
            commonLootTables.add(lootTable);
        }

        Collection<String> invalidStructures = new HashSet<>();
        for (String m : skillConfig.getStringList("archaeology_valid_vacinity_structures")){
            String[] args = m.split(":");
            int radius = args.length > 1 ? Catch.catchOrElse(() -> Integer.parseInt(args[1]), 2) : 2;
            LootTables lootTable = args.length > 2 ? Catch.catchOrElse(() -> LootTables.valueOf(args[2]), null) : LootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE;
            Structures structure = Catch.catchOrElse(() -> Structures.valueOf(args[0]), null);
            if (structure == null) invalidStructures.add(args[0]);
            else {
                structureRadiusMap.put(structure, radius);
                structureLootTableMap.put(structure, lootTable);
            }
        }
        if (!invalidStructures.isEmpty()) {
            ValhallaMMO.logWarning("The following structures in skills/digging.yml do not exist, you might have made a typo or the structure hasn't been added yet");
            ValhallaMMO.logWarning(String.join(", ", invalidMaterials));
        }
    }

    private final int[][] conversionOffsets = new int[][]{
            {-1, 0, 0},
            {1, 0, 0},
            {0, 0, -1},
            {0, 0, 1},
            {0, -1, 0}
    };

    private final int[][] airScanOffsets = new int[][]{
            {-1, 0, 0},
            {1, 0, 0},
            {0, 0, -1},
            {0, 0, 1},
            {0, -1, 0},
            {0, 1, 0}
    };

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() || !(sandConversionBlocks.containsKey(e.getBlock().getType()) || gravelConversionBlocks.containsKey(e.getBlock().getType()))) return;
        DiggingProfile profile = ProfileCache.getOrCache(e.getPlayer(), DiggingProfile.class);

        Structures nearbyStructure = null;
        if (ValhallaMMO.getNms() != null){
            Pair<Location, Structures> nearestStructure = ValhallaMMO.getNms().getNearestStructure(e.getBlock().getWorld(), e.getBlock().getLocation(), structureRadiusMap);

            nearbyStructure = nearestStructure == null ? null : nearestStructure.getTwo();
        } else {
            for (Structures structure : structureRadiusMap.keySet()){
                if (e.getBlock().getWorld().locateNearestStructure(e.getBlock().getLocation(), structure.getStructure(), structureRadiusMap.get(structure), false) == null) continue;
                nearbyStructure = structure;
                break;
            }
        }
        conversionOffsets:
        for (int[] offset : conversionOffsets){
            Block b = e.getBlock().getLocation().add(offset[0], offset[1], offset[2]).getBlock();
            if (BlockStore.isPlaced(b)) continue;
            Material toBlock = gravelConversionBlocks.containsKey(b.getType()) ? Material.SUSPICIOUS_GRAVEL : sandConversionBlocks.containsKey(b.getType()) ? Material.SUSPICIOUS_SAND : null;
            if (toBlock == null) continue;
            boolean sand = sandConversionBlocks.containsKey(e.getBlock().getType());
            double chance = nearbyStructure != null ?
                    (sand ? profile.getArchaeologySandNearStructureGenerationChance() : profile.getArchaeologyGravelNearStructureGenerationChance()) :
                    (sand ? profile.getArchaeologySandGenerationChance() : profile.getArchaeologyGravelGenerationChance());
            chance *= sand ? sandConversionBlocks.getOrDefault(e.getBlock().getType(), 1D) : gravelConversionBlocks.getOrDefault(e.getBlock().getType(), 1D);
            if (chance <= 0) continue;
            if (!Utils.proc(e.getPlayer(), chance, false)) continue;
            for (int[] airOff : airScanOffsets){
                if (b.getLocation().add(airOff[0], airOff[1], airOff[2]).getBlock().getType().isAir()) continue conversionOffsets;
            }

            LootTable table = null;
            if (nearbyStructure != null && structureLootTableMap.containsKey(nearbyStructure)) table = structureLootTableMap.get(nearbyStructure).getLootTable();
            else {
                if (Utils.proc(e.getPlayer(), profile.getArchaeologyDefaultRareLootChance(), false)) {
                    if (!rareLootTables.isEmpty()) table = rareLootTables.get(Utils.getRandom().nextInt(rareLootTables.size())).getLootTable();
                } else if (!commonLootTables.isEmpty()) table = commonLootTables.get(Utils.getRandom().nextInt(commonLootTables.size())).getLootTable();
            }
            if (table == null) continue;
            b.setType(toBlock);
            BrushableBlock brushableBlock = (BrushableBlock) b.getState();
            brushableBlock.setLootTable(table);
            brushableBlock.update();
            ArchaeologyListener.setCustomArchaeologyDrops(b, e.getPlayer(), brushableBlock);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBrush(BlockDropItemEvent e){
        if (ValhallaMMO.isWorldBlacklisted(e.getBlock().getWorld().getName()) || e.isCancelled() || !(e.getBlock().getState() instanceof BrushableBlock b) ||
                !(e.getBlock().getBlockData() instanceof Brushable brushable)) return;
        DiggingProfile profile = ProfileCache.getOrCache(e.getPlayer(), DiggingProfile.class);

        double expQuantity = 0;
        for (Item i : e.getItems()){
            if (ItemUtils.isEmpty(i.getItemStack())) continue;
            expQuantity += archaeologyExpValues.getOrDefault(i.getItemStack().getType(), 0D) * i.getItemStack().getAmount();
        }
        for (ItemStack i : LootListener.getPreparedExtraDrops(e.getBlock())){
            if (ItemUtils.isEmpty(i)) continue;
            expQuantity += archaeologyExpValues.getOrDefault(i.getType(), 0D) * i.getAmount();
        }
        skill.addEXP(e.getPlayer(), expQuantity, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.SKILL_ACTION);

        if (!Utils.proc(e.getPlayer(), profile.getArchaeologyRepeatChance(), false)) return;
        Material previousMaterial = e.getBlock().getType();
        LootTable previousTable = ArchaeologyListener.getSuspiciousLootTables().get(e.getBlock());
        if (previousTable == null) return;

        ValhallaMMO.getInstance().getServer().getScheduler().runTaskLater(ValhallaMMO.getInstance(), () -> {
            e.getBlock().setType(previousMaterial);
            b.setLootTable(previousTable);
            b.update();
            ArchaeologyListener.setCustomArchaeologyDrops(e.getBlock(), e.getPlayer(), b);

            brushable.setDusted(0);
            e.getBlock().setBlockData(brushable);
        }, 2L);
    }
}
