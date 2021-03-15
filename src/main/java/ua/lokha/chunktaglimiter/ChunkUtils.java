package ua.lokha.chunktaglimiter;

import net.minecraft.server.v1_12_R1.*;

import java.util.List;

public class ChunkUtils {

    public static long calcSize(Chunk chunk) {
        long len = 0;
        for (TileEntity tileEntity : chunk.tileEntities.values()) {
            if (tileEntity == null) {
                continue;
            }
            if (tileEntity instanceof IInventory) {
                len += calcDeepItemsSize(((IInventory) tileEntity).getContents());
            }
        }

        for (List<Entity> entitySlice : chunk.entitySlices) {
            if (entitySlice == null) {
                continue;
            }
            for (Entity entity : entitySlice) {
                if (entity == null) {
                    continue;
                }
                if (entity instanceof IInventory) {
                    len += calcDeepItemsSize(((IInventory) entity).getContents());
                } else if (entity instanceof EntityHorseAbstract) {
                    EntityHorseAbstract horse = (EntityHorseAbstract) entity;
                    len += calcDeepItemsSize(horse.inventoryChest.items);
                } else if (entity instanceof EntityItem) {
                    ItemStack itemStack = ((EntityItem) entity).getItemStack();
                    if (itemStack != null) {
                        len += calcDeepLenStringsInTag(itemStack.getTag());
                    }
                } else if (entity instanceof EntityItemFrame) {
                    ItemStack itemStack = ((EntityItemFrame) entity).getItem();
                    if (itemStack != null) {
                        len += calcDeepLenStringsInTag(itemStack.getTag());
                    }
                }
            }
        }

        return len;
    }

    public static long calcDeepItemsSize(List<ItemStack> items) {
        if (items == null) {
            return 0;
        }
        long size = 0;
        for (ItemStack item : items) {
            if (item != null) {
                size += calcDeepLenStringsInTag(item.getTag());
            }
        }
        return size;
    }

    /**
     * Посчитать суммарную длину всех строк в этом теге
     */
    public static long calcDeepLenStringsInTag(NBTBase tag){
        if(tag == null){
            return 0;
        }
        if(tag instanceof NBTTagCompound){
            long counter = 0;
            for(NBTBase value : ((NBTTagCompound) tag).map.values()){
                counter += calcDeepLenStringsInTag(value);
            }
            return counter;
        } else if(tag instanceof NBTTagList){
            long counter = 0;
            for(NBTBase value : ((NBTTagList) tag).list){
                counter += calcDeepLenStringsInTag(value);
            }
            return counter;
        } else if(tag instanceof NBTTagString){
            return ((NBTTagString) tag).c_().length();
        }

        return 0;
    }
}
