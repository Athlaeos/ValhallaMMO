package me.athlaeos.valhallammo.paper;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.Tool;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.nms.Paper;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.inventory.ItemStack;

public final class Paper_v1_21_R3 implements Paper {
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setConsumable(ItemBuilder builder, boolean edible, boolean canAlwaysEat, float eatTimeSeconds) {
        ItemStack item = builder.get();
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                .consumeSeconds(eatTimeSeconds)
                .build()
        );
        item.setData(DataComponentTypes.FOOD, FoodProperties.food()
                .canAlwaysEat(canAlwaysEat)
                .build()
        );
        builder.setItem(item);
        builder.setMeta(ItemUtils.getItemMeta(item));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setTool(ItemBuilder builder, float miningSpeed, boolean canDestroyInCreative) {
        ItemStack item = builder.get();
        item.setData(DataComponentTypes.TOOL, Tool.tool()
                .defaultMiningSpeed(miningSpeed)
                .build()
        );
        builder.setItem(item);
        builder.setMeta(ItemUtils.getItemMeta(item));
    }
}
