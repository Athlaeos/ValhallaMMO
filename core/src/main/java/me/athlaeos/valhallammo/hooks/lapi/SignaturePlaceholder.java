package me.athlaeos.valhallammo.hooks.lapi;

import me.athlaeos.lapi.placeholder.StringPlaceholder;
import me.athlaeos.lapi.utils.ItemBuilder;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.implementations.item_misc.PlayerSignatureAdd;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SignaturePlaceholder extends StringPlaceholder {
    @Override
    public String getIdentifier() {
        return "valhallammo";
    }

    @Override
    public String getPlaceholder() {
        return "signature";
    }

    @Override
    public String parse(Player player, ItemBuilder itemBuilder) {
        UUID signature = PlayerSignatureAdd.getSignature(itemBuilder.getMeta());
        OfflinePlayer p = signature == null ? null : ValhallaMMO.getInstance().getServer().getOfflinePlayer(signature);
        return p == null ? "" : p.getName();
    }
}
