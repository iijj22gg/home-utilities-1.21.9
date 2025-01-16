package com.homeutilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JsonHandler {
    public static void addLocation(ServerPlayerEntity player, String name, double x, double y, double z, ServerWorld world){
        PlayerData playerData = StateSaverAndLoader.getPlayerState(player);
        JsonObject locationObject = new JsonObject();
        locationObject.addProperty("x", x);
        locationObject.addProperty("y", y);
        locationObject.addProperty("z", z);
        RegistryKey<World> registryKey = world.getRegistryKey();
        locationObject.addProperty("world", registryKey.getValue().toString());
        playerData.getHomes().add(name,locationObject);
        StateSaverAndLoader.saveState(Objects.requireNonNull(player.getServer()));
    }

    public static boolean removeLocation(ServerPlayerEntity player, String name){
        PlayerData playerData = StateSaverAndLoader.getPlayerState(player);
        if(playerData.getHomes().isEmpty() || !playerData.getHomes().has(name)){
            return false;
        }
        else{
            playerData.getHomes().remove(name);
            StateSaverAndLoader.saveState(Objects.requireNonNull(player.getServer()));
            return true;
        }
    }

    public static JsonObject getLocation(ServerPlayerEntity player, String name){
        PlayerData playerData = StateSaverAndLoader.getPlayerState(player);
        if (playerData.getHomes().isEmpty() || !playerData.getHomes().has(name)){
            return null;
        }
        else{
            return playerData.getHomes().get(name).getAsJsonObject();
        }
    }

    public static List<String> listLocations(ServerPlayerEntity player){
        PlayerData playerData = StateSaverAndLoader.getPlayerState(player);
        if (playerData.getHomes().isEmpty()){
            return null;
        }
        else{
            return new ArrayList<>(playerData.getHomes().keySet());
        }
    }
}
