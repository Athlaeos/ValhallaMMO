import java.lang.reflect.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.common.collect.*;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;
import me.athlaeos.valhallammo.persistence.implementations.SQLite;
import me.athlaeos.valhallammo.playerstats.profiles.*;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.*;

public class SaveRaceProbe extends JavaPlugin {
    static final UUID ID=UUID.fromString("deaf0000-0000-0000-0000-000000000102");
    static final UUID STOP=UUID.fromString("deaf0000-0000-0000-0000-000000000119");
    int checks;
    boolean baseline;
    public static class FaultDB extends SQLite {
        boolean fail;
        @Override public <T extends Profile> void insertOrUpdateProfile(UUID id,T profile) {
            if (fail) { fail=false; throw new IllegalStateException("injected transient write failure"); }
            super.insertOrUpdateProfile(id,profile);
        }
    }
    void check(boolean condition,String text) {
        if(!condition)throw new AssertionError(text);
        checks++;getLogger().info("PASS "+text);
    }
    @SuppressWarnings("unchecked")
    void cache(ProfilePersistence db, UUID id, Profile... values) throws Exception {
        var m=MutableClassToInstanceMap.<Profile>create();
        var c=new HashMap<String,ConfigurableProfile>();
        for(Profile p:values) if(p instanceof ConfigurableProfile cp)c.put(cp.getSkillTypeName(),cp);else m.put(p.getClass(),p);
        Field f=ProfilePersistence.class.getDeclaredField("persistentProfiles");f.setAccessible(true);
        ((AsyncLoadingCache<UUID,ClassToInstanceMap<Profile>>)f.get(db)).put(id,CompletableFuture.completedFuture(m));
        f=ProfilePersistence.class.getDeclaredField("persistentConfigurableProfiles");f.setAccessible(true);
        ((AsyncLoadingCache<UUID,Map<String,ConfigurableProfile>>)f.get(db)).put(id,CompletableFuture.completedFuture(c));
    }
    PowerProfile power(UUID id,int level) {
        PowerProfile p=ProfileRegistry.getBlankProfile(id,PowerProfile.class);
        p.setLevel(level);p.setEXP(123.5);p.setTotalEXP(level*1000);
        p.setUnlockedPerks(new HashSet<>(List.of("mining_perk_1","power_perk_1a")));
        p.setSpendableSkillPoints(7);
        ProfilePersistence.scheduleProfilePersisting(id,PowerProfile.class);return p;
    }
    int readLevel(ProfilePersistence db,UUID id) {
        PowerProfile p=power(id,0);return db.fillProfile(id,p)?p.getLevel():-1;
    }
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this,()->{
            try {
                baseline=Files.exists(Path.of("baseline"));
                run();Files.writeString(Path.of("race-passed.txt"),"checks="+checks);
            } catch(Throwable e) {e.printStackTrace();try{Files.writeString(Path.of("race-failed.txt"),e.toString());}catch(Exception ignored){}}
            finally {Bukkit.shutdown();}
        },60);
    }
    void run() throws Exception {
        FaultDB db=new FaultDB();
        try {
            if(Files.exists(Path.of("restart-verify"))) {
                PowerProfile restored=power(STOP,0);
                check(db.fillProfile(STOP,restored),"shutdown profile row exists after full restart");
                check(restored.getLevel()==19,"Power 19 survives plugin shutdown and startup");
                check(restored.getUnlockedPerks().contains("mining_perk_1")&&restored.getSpendableSkillPoints()==7,"perks and points survive shutdown");
                return;
            }
            db.insertOrUpdateProfile(ID,power(ID,11));
            PowerProfile p=power(ID,19);cache(db,ID,p);
            // Occupy all real executor workers, so async saving is marked but not started.
            int count=((ScheduledThreadPoolExecutor)db.profileThreads).getCorePoolSize();
            CountDownLatch started=new CountDownLatch(count), release=new CountDownLatch(1);
            for(int i=0;i<count;i++)db.profileThreads.execute(()->{started.countDown();try{release.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}});
            check(started.await(5,TimeUnit.SECONDS),"async worker queue is deliberately blocked");
            try {
                db.saveProfileAsync(ID);db.saveProfile(ID,true);
                check(readLevel(db,ID)==(baseline?11:19),baseline?"REPRODUCED: pending async save makes synchronous save skip Power 19":"pending async save cannot skip synchronous Power 19 write");
            } finally {release.countDown();}
            db.profileThreads.shutdown();check(db.profileThreads.awaitTermination(10,TimeUnit.SECONDS),"queued async save drains");

            FaultDB retry=new FaultDB();
            try {
                retry.insertOrUpdateProfile(ID,power(ID,11));
                p=power(ID,19);cache(retry,ID,p);retry.fail=true;
                boolean threw=false;try{retry.saveProfile(ID,true);}catch(IllegalStateException e){threw=true;}
                check(threw,"injected write exception reaches caller");
                p.setLevel(20);cache(retry,ID,p);retry.saveProfile(ID,true);
                check(readLevel(retry,ID)==(baseline?11:20),baseline?"REPRODUCED: exception permanently blocks later saves":"saving recovers after transient exception");

                p=power(ID,19);
                ConfigurableProfile custom=new ConfigurableProfile(ID,"rr_save_race");custom.setLevel(7);custom.setEXP(35);custom.setTotalEXP(1234);
                retry.createProfileTable(custom);
                try(Statement st=retry.getConnection().createStatement()){st.execute("DELETE FROM profiles_rr_save_race");}
                ProfilePersistence.scheduleConfigurableProfilePersisting(ID,"rr_save_race");
                // Fresh instance isolates this scenario from the baseline's stuck lock.
                FaultDB both=new FaultDB();
                try {
                    cache(both,ID,p,custom);both.saveProfile(ID,true);both.saveConfigurableProfile(ID,true);
                    ConfigurableProfile loaded=new ConfigurableProfile(ID,"rr_save_race");
                    boolean exists=both.fillProfile(ID,loaded);
                    check(exists!=baseline,baseline?"REPRODUCED: normal offline save evicts unsaved configurable profile":"offline save preserves both profile families");
                    if(!baseline)check(loaded.getLevel()==7&&loaded.getEXP()==35,"configurable profile values round trip");
                } finally {both.profileThreads.shutdownNow();both.configurableProfileThreads.shutdownNow();}
            } finally {retry.profileThreads.shutdownNow();retry.configurableProfileThreads.shutdownNow();}
            if(!baseline) {
                FaultDB shutdown=new FaultDB();cache(shutdown,STOP,power(STOP,19));
                ProfileRegistry.setPersistence(shutdown);
                check(shutdown.isLoaded(STOP),"Power 19 remains cached for actual onDisable save");
            }
        } finally {db.profileThreads.shutdownNow();db.configurableProfileThreads.shutdownNow();}
    }
}
