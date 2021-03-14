package ua.lokha.chunktaglimiter;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Главный класс плагина
 */
public class Main extends JavaPlugin {

    @Getter
    private static Main instance;

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

    }
}
