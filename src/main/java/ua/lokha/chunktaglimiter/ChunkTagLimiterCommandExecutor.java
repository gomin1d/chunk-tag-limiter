package ua.lokha.chunktaglimiter;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.*;
import java.util.stream.Collectors;

public class ChunkTagLimiterCommandExecutor implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(
                    "§e=========[ChunkTagLimiter]=========" +
                            "\n§4/chunktaglimiter reload §7- перезагрузить конфиг" +
                            "\n§4/chunktaglimiter top §7- посмотреть топ чанков по длине"
            );
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Main.getInstance().reloadConfig();
            Main.getInstance().reloadConfigParams();
            sender.sendMessage("§eКонфиг перезагружен.");
            return true;
        }


        if (args[0].equalsIgnoreCase("top")) {
            List<ChunkData> chunks = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                try {
                    WorldServer handle = ((CraftWorld) world).getHandle();
                    for (Chunk chunk : handle.getChunkProviderServer().chunks.values()) {
                        try {
                            long size = ChunkUtils.calcSize(chunk);
                            chunks.add(new ChunkData(world.getName(), chunk.locX, chunk.locZ, size));
                        } catch (Exception e) {
                            sender.sendMessage("§cОшибка обработки чанка " +
                                    chunk.locX + " " + chunk.locZ + " в мире " + world.getName());
                            Main.getInstance().getLogger().severe("Ошибка обработки чанка " +
                                    chunk.locX + " " + chunk.locZ + " в мире " + world.getName());
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    sender.sendMessage("§cОшибка обработки мира " + world.getName());
                    Main.getInstance().getLogger().severe("Ошибка обработки мира " + world.getName());
                    e.printStackTrace();
                }
            }

            sender.sendMessage("§eТоп чанков по длине:");
            chunks.sort(Comparator.comparingLong(value -> -value.size));
            for (int i = 0; i < Math.min(10, chunks.size()); i++) {
                ChunkData data = chunks.get(i);
                TextComponent component = new TextComponent("§7  " + (i + 1) + ") world " + data.world + ", x " + data.x + ", z " + data.z +
                        ", size " + data.size * 2 + " bytes");
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new BaseComponent[]{new TextComponent("§aНажми, чтобы телепортироваться.")}));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/tp " + (data.x << 4) + " 100 " + (data.z << 4)));
                sender.sendMessage(component);
            }
            return true;
        }

        sender.sendMessage("§cАргумент команды не найден.");
        return true;
    }

    private static class ChunkData {
        private String world;
        private int x;
        private int z;
        private long size;

        public ChunkData(String world, int x, int z, long size) {
            this.world = world;
            this.x = x;
            this.z = z;
            this.size = size;
        }
    }

    @SuppressWarnings({"LambdaBodyCanBeCodeBlock"})
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterTabResponse(Arrays.asList("reload", "top"), args);
        }

        return Collections.emptyList();
    }

    private static List<String> filterTabResponse(List<String> list, String[] args) {
        return list.stream()
                .filter(el -> StringUtils.containsIgnoreCase(el, args[args.length - 1]))
                .collect(Collectors.toList());
    }
}
