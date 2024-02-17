package me.athlaeos.valhallammo.dom;

import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;

import java.util.LinkedHashMap;
import java.util.Map;

public enum Structures {
    BURIED_TREASURE("BURIED_TREASURE"),
    ANCIENT_CITY("ANCIENT_CITY"),
    BASTION_REMNANT("BASTION_REMNANT"),
    DESERT_PYRAMID("DESERT_PYRAMID"),
    END_CITY("END_CITY"),
    FORTRESS("FORTRESS"),
    IGLOO("IGLOO"),
    JUNGLE_PYRAMID("JUNGLE_PYRAMID"),
    MANSION("MANSION"),
    MINESHAFT("MINESHAFT"),
    MINESHAFT_MESA("MINESHAFT_MESA"),
    MONUMENT("MONUMENT"),
    NETHER_FOSSIL("NETHER_FOSSIL"),
    OCEAN_RUIN_COLD("OCEAN_RUIN_COLD"),
    OCEAN_RUIN_WARM("OCEAN_RUIN_WARM"),
    PILLAGER_OUTPOST("PILLAGER_OUTPOST"),
    RUINED_PORTAL("RUINED_PORTAL"),
    RUINED_PORTAL_DESERT("RUINED_PORTAL_DESERT"),
    RUINED_PORTAL_JUNGLE("RUINED_PORTAL_JUNGLE"),
    RUINED_PORTAL_MOUNTAIN("RUINED_PORTAL_MOUNTAIN"),
    RUINED_PORTAL_NETHER("RUINED_PORTAL_NETHER"),
    RUINED_PORTAL_OCEAN("RUINED_PORTAL_OCEAN"),
    RUINED_PORTAL_SWAMP("RUINED_PORTAL_SWAMP"),
    SHIPWRECK("SHIPWRECK"),
    SHIPWRECK_BEACHED("SHIPWRECK_BEACHED"),
    STRONGHOLD("STRONGHOLD"),
    SWAMP_HUT("SWAMP_HUT"),
    TRAIL_RUINS("TRAIL_RUINS"),
    VILLAGE_DESERT("VILLAGE_DESERT"),
    VILLAGE_PLAINS("VILLAGE_PLAINS"),
    VILLAGE_SAVANNA("VILLAGE_SAVANNA"),
    VILLAGE_SNOWY("VILLAGE_SNOWY"),
    VILLAGE_TAIGA("VILLAGE_TAIGA");

    private static final Map<String, Structure> types = new LinkedHashMap<>();
    static {
        types.put("BURIED_TREASURE", Structure.BURIED_TREASURE);
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_19)) types.put("ANCIENT_CITY", Structure.ANCIENT_CITY);
        types.put("BASTION_REMNANT", Structure.BASTION_REMNANT);
        types.put("DESERT_PYRAMID", Structure.DESERT_PYRAMID);
        types.put("END_CITY", Structure.END_CITY);
        types.put("FORTRESS", Structure.FORTRESS);
        types.put("IGLOO", Structure.IGLOO);
        types.put("JUNGLE_PYRAMID", Structure.JUNGLE_PYRAMID);
        types.put("MANSION", Structure.MANSION);
        types.put("MINESHAFT", Structure.MINESHAFT);
        types.put("MINESHAFT_MESA", Structure.MINESHAFT_MESA);
        types.put("MONUMENT", Structure.MONUMENT);
        types.put("NETHER_FOSSIL", Structure.NETHER_FOSSIL);
        types.put("OCEAN_RUIN_COLD", Structure.OCEAN_RUIN_COLD);
        types.put("OCEAN_RUIN_WARM", Structure.OCEAN_RUIN_WARM);
        types.put("PILLAGER_OUTPOST", Structure.PILLAGER_OUTPOST);
        types.put("RUINED_PORTAL", Structure.RUINED_PORTAL);
        types.put("RUINED_PORTAL_DESERT", Structure.RUINED_PORTAL_DESERT);
        types.put("RUINED_PORTAL_JUNGLE", Structure.RUINED_PORTAL_JUNGLE);
        types.put("RUINED_PORTAL_MOUNTAIN", Structure.RUINED_PORTAL_MOUNTAIN);
        types.put("RUINED_PORTAL_NETHER", Structure.RUINED_PORTAL_NETHER);
        types.put("RUINED_PORTAL_OCEAN", Structure.RUINED_PORTAL_OCEAN);
        types.put("RUINED_PORTAL_SWAMP", Structure.RUINED_PORTAL_SWAMP);
        types.put("SHIPWRECK", Structure.SHIPWRECK);
        types.put("SHIPWRECK_BEACHED", Structure.SHIPWRECK_BEACHED);
        types.put("STRONGHOLD", Structure.STRONGHOLD);
        types.put("SWAMP_HUT", Structure.SWAMP_HUT);
        if (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20)) types.put("TRAIL_RUINS", Structure.TRAIL_RUINS);
        types.put("VILLAGE_DESERT", Structure.VILLAGE_DESERT);
        types.put("VILLAGE_PLAINS", Structure.VILLAGE_PLAINS);
        types.put("VILLAGE_SAVANNA", Structure.VILLAGE_SAVANNA);
        types.put("VILLAGE_SNOWY", Structure.VILLAGE_SNOWY);
        types.put("VILLAGE_TAIGA", Structure.VILLAGE_TAIGA);
    }

    private final String structureName;
    Structures(String structureName){
        this.structureName = structureName;
    }

    public Structure getStructure(){
        return types.get(structureName);
    }

    public static Structures fromStructure(Structure structure){
        for (Structures s : values()){
            if (s.getStructure().equals(structure)) return s;
        }
        return null;
    }

    public static Structures fromStructure(StructureType structure){
        for (Structures s : values()){
            if (s.getStructure().getStructureType().equals(structure)) return s;
        }
        return null;
    }

    public static Map<String, Structure> getTypes() {
        return types;
    }
}
