package me.athlaeos.valhallammo.event;

import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemModificationEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final ModifierContext context;
    private final List<DynamicItemModifier> modifiers;

    public ItemModificationEvent(ModifierContext context, List<DynamicItemModifier> modifiers){
        this.context = context;
        this.modifiers = modifiers;
    }

    @Override
    public @NotNull String getEventName() {
        return super.getEventName();
    }

    public ModifierContext getContext() {
        return context;
    }

    public List<DynamicItemModifier> getModifiers() {
        return modifiers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
