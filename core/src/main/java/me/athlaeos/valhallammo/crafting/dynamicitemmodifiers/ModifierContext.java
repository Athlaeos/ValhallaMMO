package me.athlaeos.valhallammo.crafting.dynamicitemmodifiers;

import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ModifierContext {
    private Player crafter;
    private Entity relativeEntity;
    private ItemBuilder item;
    private final List<ItemBuilder> otherInvolvedItems = new ArrayList<>();
    private int timesExecuted = 1;
    private boolean shouldSort = false;
    private boolean shouldValidate = false;
    private boolean executeUsageMechanics = false;
    private final Map<Class<?>, Object> otherArgs = new HashMap<>();

    private ModifierContext(ItemBuilder item){
        this.item = item;
    }

    public static ModifierContextBuilder builder(ItemBuilder item){
        return new ModifierContextBuilder(item);
    }

    public Player getCrafter() { return crafter; }
    public Entity getRelativeEntity() { return relativeEntity; }
    public ItemBuilder getItem() { return item; }
    public List<ItemBuilder> getOtherInvolvedItems() { return otherInvolvedItems; }
    public int getTimesExecuted() { return timesExecuted; }
    public boolean shouldSort() { return shouldSort; }
    public boolean shouldValidate() { return shouldValidate; }
    public boolean shouldExecuteUsageMechanics() { return executeUsageMechanics; }
    @SuppressWarnings("unchecked")
    public <T> T getOtherType(Class<T> clazz){
        Object o = otherArgs.get(clazz);
        if (o == null) return null;
        return (T) o;
    }

    public static class ModifierContextBuilder{
        private final ModifierContext context;

        private ModifierContextBuilder(ItemBuilder item){ this.context = new ModifierContext(item); }
        public ModifierContextBuilder crafter(Player crafter){ context.crafter = crafter; return this; }
        public ModifierContextBuilder entity(Entity entity){ context.relativeEntity = entity; return this; }
        public ModifierContextBuilder items(ItemStack... items){
            for (ItemStack i : items){
                if (!ItemUtils.isEmpty(i)) context.otherInvolvedItems.add(new ItemBuilder(i));
            }
            return this;
        }
        public ModifierContextBuilder items(List<ItemStack> items){
            for (ItemStack i : items){
                if (!ItemUtils.isEmpty(i)) context.otherInvolvedItems.add(new ItemBuilder(i));
            }
            return this;
        }
        public ModifierContextBuilder items(ItemBuilder... items){ context.otherInvolvedItems.addAll(Arrays.asList(items)); return this; }
        public ModifierContextBuilder count(int count){ context.timesExecuted = count; return this; }
        public ModifierContextBuilder sort(){ context.shouldSort = true; return this; }
        public ModifierContextBuilder validate(){ context.shouldValidate = true; return this; }
        public ModifierContextBuilder executeUsageMechanics(){ context.executeUsageMechanics = true; return this; }
        public ModifierContextBuilder item(ItemBuilder item){ context.item = item; return this; }
        public ModifierContextBuilder setOtherType(Object o){
            context.otherArgs.put(o.getClass(), o);
            return this;
        }
        public ModifierContext get(){ return this.context; }
    }
}
