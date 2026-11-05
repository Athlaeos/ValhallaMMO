import java.nio.file.*;
import java.sql.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import me.athlaeos.valhallammo.commands.valhallasubcommands.SaveAllCommand;
import me.athlaeos.valhallammo.persistence.ProfilePersistence;
import me.athlaeos.valhallammo.playerstats.profiles.*;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.*;

public class SaveFaultProbe extends SaveRaceProbe {
    static class CountingDB extends FaultDB {
        final AtomicInteger attempts=new AtomicInteger();
        public void saveAllProfiles(boolean async) {
            if(attempts.incrementAndGet()==1)throw new IllegalStateException("injected autosave failure");
            super.saveAllProfiles(async);
        }
    }
    static class DelayedDB extends FaultDB {
        final CountDownLatch release=new CountDownLatch(1);
        final AtomicBoolean committed=new AtomicBoolean();
        public <T extends Profile> void insertOrUpdateProfile(UUID id,T profile) {
            try {if(!release.await(5,TimeUnit.SECONDS))throw new AssertionError("manual write release timed out");}
            catch(InterruptedException e){throw new RuntimeException(e);}
            super.insertOrUpdateProfile(id,profile);committed.set(true);
        }
    }
    void complete(Throwable error) {
        try {
            if(error==null)Files.writeString(Path.of("race-passed.txt"),"checks="+checks);
            else {error.printStackTrace();Files.writeString(Path.of("race-failed.txt"),error.toString());}
        } catch(Exception ex){ex.printStackTrace();}
        Bukkit.shutdown();
    }
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this,()->{
            baseline=Files.exists(Path.of("baseline"));
            try {sqlTests();manualTest();}catch(Throwable ex){complete(ex);}
        },60);
    }
    void sqlTests() throws Exception {
        FaultDB db=new FaultDB();
        try {
            db.insertOrUpdateProfile(ID,power(ID,11));cache(db,ID,power(ID,19));
            try(Statement st=db.getConnection().createStatement()){
                st.execute("CREATE TRIGGER rr_fail_write BEFORE INSERT ON profiles_power WHEN NEW.owner='"+ID+"' BEGIN SELECT RAISE(ABORT, 'injected SQLite write failure'); END;");
            }
            boolean threw=false;
            try{db.saveProfile(ID,true);}catch(IllegalStateException e){threw=true;}
            check(threw!=baseline,baseline?"REPRODUCED: SQLite write error is swallowed":"SQLite write error reaches save caller");
            check(db.isLoaded(ID)!=baseline,baseline?"REPRODUCED: failed SQLite write evicts sole cached profile":"failed SQLite write retains in-memory Power 19");
            try(Statement st=db.getConnection().createStatement()){st.execute("DROP TRIGGER rr_fail_write");}
            db.saveProfile(ID,true);
            check(readLevel(db,ID)==(baseline?11:19),baseline?"REPRODUCED: only old Power 11 remains after SQL error":"Power 19 is written after SQLite recovers");
            // A read failure must not be confused with 'new player, no saved row'.
            try(Statement st=db.getConnection().createStatement()){st.execute("ALTER TABLE profiles_power RENAME TO rr_hidden_power");}
            threw=false;try{db.fillProfile(ID,power(ID,0));}catch(IllegalStateException e){threw=true;}
            finally{try(Statement st=db.getConnection().createStatement()){st.execute("ALTER TABLE rr_hidden_power RENAME TO profiles_power");}}
            check(threw!=baseline,baseline?"REPRODUCED: SQL read error returns same result as absent player":"SQL read error cannot produce a successful blank profile");
            MiningProfile zero=ProfileRegistry.getBlankProfile(ID,MiningProfile.class);zero.setEXP(42);zero.setTotalEXP(42);
            ProfilePersistence.scheduleProfilePersisting(ID,MiningProfile.class);
            check(db.shouldPersist(zero)!=baseline,"level-zero eligibility matches expected build");
            check(!db.shouldPersist(ProfileRegistry.getBlankProfile((UUID)null,PowerProfile.class)),"ownerless profiles remain ineligible");
        } finally {db.profileThreads.shutdownNow();db.configurableProfileThreads.shutdownNow();}
    }
    void manualTest() throws Exception {
        DelayedDB db=new DelayedDB();cache(db,ID,power(ID,19));ProfileRegistry.setPersistence(db);
        AtomicReference<Boolean> committedAtMessage=new AtomicReference<>();
        CommandSender sender=(CommandSender)Proxy.newProxyInstance(CommandSender.class.getClassLoader(),new Class<?>[]{CommandSender.class},(o,m,a)->{
            if(m.getName().equals("sendMessage"))committedAtMessage.compareAndSet(null,db.committed.get());
            if(m.getName().equals("getName"))return "SaveFaultProbe";
            if(m.getReturnType()==boolean.class)return true;
            if(m.getReturnType()==int.class)return 0;
            return null;
        });
        new SaveAllCommand().execute(sender,new String[]{"saveall"});
        Bukkit.getScheduler().runTaskLater(this,db.release::countDown,10);
        Bukkit.getScheduler().runTaskLater(this,()->{
            try {
                check(committedAtMessage.get()!=null,"manual save sends completion message");
                check(committedAtMessage.get()!=baseline,baseline?"REPRODUCED: saveall reports completion before SQL write":"saveall confirms only after SQL write completes");
                check(readLevel(db,ID)==19,"manual save finally writes Power 19");
                db.profileThreads.shutdownNow();db.configurableProfileThreads.shutdownNow();
                timerTest();
            }catch(Throwable ex){complete(ex);}
        },35);
    }
    void timerTest() {
        CountingDB db=new CountingDB();ProfileRegistry.setPersistence(db);
        Bukkit.getScheduler().runTaskLater(this,()->{
            try {
                check(db.attempts.get()>0,"periodic autosave attempted injected failure");
                check(baseline?db.attempts.get()==1:db.attempts.get()>=2,baseline?"REPRODUCED: one exception cancels every future autosave":"autosave continues retrying after exception");
                complete(null);
            }catch(Throwable ex){complete(ex);}
        },90);
    }
}
