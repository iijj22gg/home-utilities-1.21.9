package com.homeutilities;

import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import net.minecraft.world.PersistentStateType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.datafixer.DataFixTypes;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {

    public HashMap<UUID,PlayerData> players = new HashMap<>();
    public PublicData publicHomes = new PublicData();
    public SettingsData settings = new SettingsData();

    // @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup){
        NbtCompound playersNbt = new NbtCompound();
        players.forEach(((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putString("homes",playerData.toString());
            playerNbt.putString("language",playerData.getLanguage());
            playersNbt.put(uuid.toString(),playerNbt);
        }));
        nbt.put("players", playersNbt);
        nbt.putString("publichomes",publicHomes.toString());
        nbt.putString("settings",settings.toString());
        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        NbtCompound playersNbt = tag.getCompound("players").orElse(new NbtCompound());
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();
            playerData.setHomes(playersNbt.getCompound(key).map(nbt -> nbt.getString("homes").orElse("")).orElse(""));
            playerData.setLanguage(playersNbt.getCompound(key).map(nbt -> nbt.getString("language").orElse("")).orElse(""));
            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        // Add null check for publicHomes
        String publicHomesString = tag.getString("publichomes").orElse("");
        if (!publicHomesString.isEmpty()) {
            state.publicHomes.setHomes(publicHomesString);
        }

        String settingsString = tag.getString("settings").orElse("");
        if (!settingsString.isEmpty()) {
            state.settings.setSettings(settingsString);
        }

        return state;
    }


    private static final PersistentStateType<StateSaverAndLoader> type = new PersistentStateType<>(
            (String) HomeUtilities.MOD_ID,
            StateSaverAndLoader::new,
            // CODEC,
            null,
            DataFixTypes.PLAYER
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server){
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;

        StateSaverAndLoader state = serverWorld.getPersistentStateManager().getOrCreate(type);
        
        state.markDirty();
 
        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player){
        StateSaverAndLoader serverState = getServerState(Objects.requireNonNull(player.getEntityWorld().getServer()));
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }

    public static void resetPlayerState(MinecraftServer server){
        StateSaverAndLoader serverState = getServerState(server);
        serverState.players.forEach(((uuid, playerData) -> playerData.setLanguage("en")));
        saveState(server);
    }

    public static PublicData getPublicState(LivingEntity player){
        StateSaverAndLoader serverState = getServerState(Objects.requireNonNull(player.getEntityWorld().getServer()));
        return serverState.publicHomes;
    }

    public static SettingsData getSettingsState(LivingEntity player){
        StateSaverAndLoader serverState = getServerState(Objects.requireNonNull(player.getEntityWorld().getServer()));
        return serverState.settings;
    }

    public static void saveState(MinecraftServer server) {
        getServerState(server).markDirty();
    }
}
