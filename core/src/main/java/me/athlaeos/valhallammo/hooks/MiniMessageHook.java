package me.athlaeos.valhallammo.hooks;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MiniMessageHook {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static String convertMiniMessage(String message){
        String converted = miniMessage.serialize(LegacyComponentSerializer.legacySection().deserialize(message)).replace("\\", "");
        return LegacyComponentSerializer.legacyAmpersand().serialize(miniMessage.deserialize(converted));
    }
}
