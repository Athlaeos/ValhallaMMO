package me.athlaeos.valhallammo.dom;

import me.athlaeos.valhallammo.ValhallaMMO;

public class Catch {
    public static <T> T catchOrElse(Fetcher<T> c, T r){
        return catchOrElse(c, r, null);
    }

    public static <T> T catchOrElse(Fetcher<T> c, T r, String log){
        try {
            return c.get();
        } catch (Exception e){
            if (log != null) ValhallaMMO.logWarning(log);
            return r;
        }
    }
}
