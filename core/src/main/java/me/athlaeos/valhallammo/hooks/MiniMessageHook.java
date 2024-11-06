package me.athlaeos.valhallammo.hooks;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MiniMessageHook {
    public static String convertMiniMessage(String message){
        MiniMessage miniMessage = MiniMessage.miniMessage();
        String converted = miniMessage.serialize(LegacyComponentSerializer.legacySection().deserialize(message)).replace("\\", "");
        return LegacyComponentSerializer.legacyAmpersand().serialize(miniMessage.deserialize(converted));
    }
}
