package ua.lokha.chunktaglimiter;

import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.ArrayList;
import java.util.List;

public class ChunkChecker implements Runnable {

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        for (World world : Bukkit.getWorlds()) {
            try {
                WorldServer handle = ((CraftWorld) world).getHandle();
                List<Chunk> doRemove = null;

                for (Chunk chunk : handle.getChunkProviderServer().chunks.values()) {
                    try {
                        long size = ChunkUtils.calcSize(chunk);
                        if (size * 2 /* char is 2 bytes */ > Main.getInstance().getLimitBytes()) {
                            Main.getInstance().getLogger().warning("Чанк world " + world.getName() + ", " +
                                    "x " + chunk.locX + ", z " + chunk.locZ + " (" +
                                    "/tp " + (chunk.locX << 4) + " 100 " + (chunk.locZ << 4) + ") " +
                                    "превысил лимит по длине " + (size * 2) + ">" + Main.getInstance().getLimitBytes() +
                                    ", удаляем его.");
                            if (doRemove == null) {
                                doRemove = new ArrayList<>();
                            }
                            doRemove.add(chunk);
                        }
                    } catch (Exception e) {
                        Main.getInstance().getLogger().severe("Ошибка обработки чанка " +
                                chunk.locX + " " + chunk.locZ + " в мире " + world.getName());
                        e.printStackTrace();
                    }
                }

                if (doRemove != null) {
                    for (Chunk chunk : doRemove) {
                        try {
                            handle.getChunkProviderServer().unloadQueue.remove(chunk.chunkKey);
                            handle.getChunkProviderServer().chunks.remove(chunk.chunkKey);
                        } catch (Exception e) {
                            Main.getInstance().getLogger().severe("Ошибка при удалении чанка " +
                                    chunk.locX + " " + chunk.locZ + " в мире " + world.getName());
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                Main.getInstance().getLogger().severe("Ошибка обработки мира " + world.getName());
                e.printStackTrace();
            }
        }
        long left = System.currentTimeMillis() - start;
        if (left > Main.getInstance().getCheckWarnTimeMillis()) {
            Bukkit.getLogger().warning("Проверка чанков заняла " + left + " ms " +
                    "(в конфиге можно настроить порог времени для лога этого сообщения).");
        }
    }
}
