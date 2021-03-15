package ua.lokha.chunktaglimiter;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Главный класс плагина
 */
public class Main extends JavaPlugin {

    @Getter
    private static Main instance;

    @Getter
    private long limitBytes;
    @Getter
    private int periodTicks;
    @Getter
    private int checkWarnTimeMillis;

    private BukkitTask timer;

    public Main() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfigParams();

        this.getCommand("chunktaglimiter").setExecutor(new ChunkTagLimiterCommandExecutor());
    }

    public void reloadConfigParams() {
        if (timer != null) {
            try {
                timer.cancel();
            } catch (Exception ignored) {}
            timer = null;
        }

        limitBytes = ((Number)this.getConfig().get("limit-bytes", 200_000)).longValue();
        periodTicks = this.getConfig().getInt("period-ticks", 200);
        checkWarnTimeMillis = this.getConfig().getInt("check-warn-time-millis", 5);

        timer = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new ChunkChecker(), periodTicks, periodTicks);
    }
}
