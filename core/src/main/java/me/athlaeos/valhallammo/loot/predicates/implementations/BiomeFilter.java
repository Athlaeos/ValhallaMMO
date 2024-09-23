package me.athlaeos.valhallammo.loot.predicates.implementations;

import me.athlaeos.valhallammo.dom.Pair;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.loot.LootTable;
import me.athlaeos.valhallammo.loot.predicates.LootPredicate;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

import java.util.*;

public class BiomeFilter extends LootPredicate {
    private final Material icon;
    private final String name;
    private final Collection<Biome> biomes;

    public BiomeFilter(Material icon, String name, Biome... biomes){
        this.icon = icon;
        this.name = name;
        this.biomes = Set.of(biomes);
    }

    @Override
    public String getKey() {
        return "biome_" + ChatColor.stripColor(Utils.chat(name)).toLowerCase(java.util.Locale.US).replace(" ", "_");
    }

    @Override
    public Material getIcon() {
        return icon;
    }

    @Override
    public String getDisplayName() {
        return "&fBiome Filter: " + name;
    }

    @Override
    public String getDescription() {
        return "&fRequires the environment to be within the " + name + " &fbiome";
    }

    @Override
    public String getActiveDescription() {
        return "&fRequires the environment to " + (isInverted() ? "&cNOT&f " : "") + "be within the " + name + " &fbiome";
    }

    @Override
    public LootPredicate createNew() {
        return new BiomeFilter(icon, name, biomes.toArray(new Biome[0]));
    }

    @Override
    public Map<Integer, ItemStack> getButtons() {
        return new Pair<>(2,
                new ItemBuilder(Material.TNT)
                        .name("&eInvert Condition")
                        .lore(inverted ? "&cCondition is inverted" : "&aCondition not inverted",
                                "&fInverted conditions must &cnot &fpass",
                                "&fthis condition. ",
                                "&6Click to toggle")
                        .get()).map(new HashSet<>());
    }

    @Override
    public void onButtonPress(InventoryClickEvent e, int button) {
        if (button == 2) inverted = !inverted;
    }

    @Override
    public boolean test(LootContext context) {
        Block b = context.getLocation().getBlock();
        return biomes.contains(b.getBiome()) != inverted;
    }

}
