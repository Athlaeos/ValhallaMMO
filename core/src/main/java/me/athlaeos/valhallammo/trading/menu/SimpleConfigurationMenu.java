package me.athlaeos.valhallammo.trading.menu;

import me.athlaeos.valhallammo.dom.Fetcher;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class SimpleConfigurationMenu <T> extends Menu {
    private static final int INDEX_BACK = 49;
    private final Map<Integer, Button> buttons = new HashMap<>();
    private final Menu previousMenu;
    protected final T thingyToConfigure;

    protected SimpleConfigurationMenu(PlayerMenuUtility playerMenuUtility, Menu previousMenu, T thingyToConfigure, List<Button> buttons) {
        super(playerMenuUtility);
        this.previousMenu = previousMenu;
        this.thingyToConfigure = thingyToConfigure;
        for (Button b : buttons) this.buttons.put(b.position, b);
    }

    @Override
    public int getSlots() {
        return 54;
    }

    protected final void addButton(Button b){
        this.buttons.put(b.position, b);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(!(e.getClickedInventory() instanceof PlayerInventory));
        if (e.getRawSlot() == INDEX_BACK){
            previousMenu.open();
            return;
        }
        Button clicked = buttons.get(e.getRawSlot());
        if (clicked == null) return;
        clicked.onClick.accept(thingyToConfigure, e);
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void setMenuItems() {
        inventory.clear();
        inventory.setItem(INDEX_BACK, backToMenuButton);
        for (Integer slot : buttons.keySet()){
            Button b = buttons.get(slot);
            inventory.setItem(slot, new ItemBuilder(b.appearance).name(b.name.get()).lore(b.description.get()).get());
        }
    }

    private static final ItemStack backToMenuButton =
            new ItemBuilder(getButtonData("editor_backtomenu", Material.LIME_DYE))
                    .name("&fBack")
                    .get();

    protected class Button{
        protected final int position;
        protected final BiConsumer<T, InventoryClickEvent> onClick;
        protected final Material appearance;
        protected final Fetcher<String> name;
        protected final Fetcher<List<String>> description;

        protected Button(Material appearance, int pos, Fetcher<String> name, Fetcher<List<String>> description, BiConsumer<T, InventoryClickEvent> onClick){
            this.position = pos;
            this.appearance = appearance;
            this.name = name;
            this.description = description;
            this.onClick = onClick;
        }
    }
}
