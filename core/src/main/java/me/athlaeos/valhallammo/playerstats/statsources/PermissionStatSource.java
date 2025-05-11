package me.athlaeos.valhallammo.playerstats.statsources;

import me.athlaeos.valhallammo.dom.Catch;
import me.athlaeos.valhallammo.playerstats.AccumulativeStatSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PermissionStatSource implements AccumulativeStatSource {
    private final String basePermission;

    public PermissionStatSource(String stat){
        this.basePermission = stat;
    }

    @Override
    public double fetch(Entity statPossessor, boolean use) {
        if (!(statPossessor instanceof Player p) || !p.hasPermission(basePermission)) return 0;
        double value = 0;
        for (PermissionAttachmentInfo permission : p.getEffectivePermissions()){
            if (!permission.getValue() || !permission.getPermission().startsWith(basePermission)) continue;
            String v = permission.getPermission().replace(basePermission + ".", "");
            Double d = Catch.catchOrElse(() -> Double.valueOf(v), null);
            if (d == null) continue;
            value += d;
        }
        return value;
    }
}
