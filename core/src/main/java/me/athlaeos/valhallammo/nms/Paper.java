package me.athlaeos.valhallammo.nms;

import me.athlaeos.valhallammo.item.ItemBuilder;

public interface Paper {
    void setConsumable(ItemBuilder builder, boolean edible, boolean canAlwaysEat, float eatTimeSeconds);

    void setTool(ItemBuilder builder, float miningSpeed, boolean canDestroyInCreative);
}
