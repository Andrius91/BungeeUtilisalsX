package com.dbsoftwares.bungeeutilisals.bungee;

/*
 * Created by DBSoftwares on 19 augustus 2017
 * Developer: Dieter Blancke
 * Project: BungeeUtilisals
 */

import com.dbsoftwares.bungeeutilisals.api.BUCore;
import com.dbsoftwares.bungeeutilisals.api.bossbar.BarColor;
import com.dbsoftwares.bungeeutilisals.api.bossbar.BarStyle;
import com.dbsoftwares.bungeeutilisals.api.bossbar.IBossBar;
import com.dbsoftwares.bungeeutilisals.api.configuration.IConfiguration;
import com.dbsoftwares.bungeeutilisals.api.event.event.Event;
import com.dbsoftwares.bungeeutilisals.api.event.event.EventExecutor;
import com.dbsoftwares.bungeeutilisals.api.event.event.IEventLoader;
import com.dbsoftwares.bungeeutilisals.api.event.events.punishment.UserPunishEvent;
import com.dbsoftwares.bungeeutilisals.api.event.events.user.UserChatEvent;
import com.dbsoftwares.bungeeutilisals.api.event.events.user.UserChatPreExecuteEvent;
import com.dbsoftwares.bungeeutilisals.api.event.events.user.UserCommandEvent;
import com.dbsoftwares.bungeeutilisals.api.event.events.user.UserLoadEvent;
import com.dbsoftwares.bungeeutilisals.api.experimental.event.PacketReceiveEvent;
import com.dbsoftwares.bungeeutilisals.api.experimental.event.PacketUpdateEvent;
import com.dbsoftwares.bungeeutilisals.api.experimental.packets.client.PacketPlayOutBossBar;
import com.dbsoftwares.bungeeutilisals.api.placeholder.PlaceHolderAPI;
import com.dbsoftwares.bungeeutilisals.api.storage.AbstractStorageManager;
import com.dbsoftwares.bungeeutilisals.api.storage.AbstractStorageManager.StorageType;
import com.dbsoftwares.bungeeutilisals.api.utils.Utils;
import com.dbsoftwares.bungeeutilisals.api.utils.file.FileLocation;
import com.dbsoftwares.bungeeutilisals.api.utils.file.FileUtils;
import com.dbsoftwares.bungeeutilisals.api.utils.math.MathUtils;
import com.dbsoftwares.bungeeutilisals.bungee.api.BUtilisalsAPI;
import com.dbsoftwares.bungeeutilisals.bungee.api.configuration.yaml.YamlConfiguration;
import com.dbsoftwares.bungeeutilisals.bungee.api.placeholder.DefaultPlaceHolders;
import com.dbsoftwares.bungeeutilisals.bungee.commands.PluginCommand;
import com.dbsoftwares.bungeeutilisals.bungee.commands.punishments.*;
import com.dbsoftwares.bungeeutilisals.bungee.executors.MuteCheckExecutor;
import com.dbsoftwares.bungeeutilisals.bungee.executors.UserChatExecutor;
import com.dbsoftwares.bungeeutilisals.bungee.executors.UserExecutor;
import com.dbsoftwares.bungeeutilisals.bungee.executors.UserPunishExecutor;
import com.dbsoftwares.bungeeutilisals.bungee.experimental.executors.PacketUpdateExecutor;
import com.dbsoftwares.bungeeutilisals.bungee.experimental.listeners.SimplePacketListener;
import com.dbsoftwares.bungeeutilisals.bungee.library.Library;
import com.dbsoftwares.bungeeutilisals.bungee.library.classloader.LibraryClassLoader;
import com.dbsoftwares.bungeeutilisals.bungee.listeners.PunishmentListener;
import com.dbsoftwares.bungeeutilisals.bungee.listeners.UserChatListener;
import com.dbsoftwares.bungeeutilisals.bungee.listeners.UserConnectionListener;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BungeeUtilisals extends Plugin {

    @Getter
    private static BungeeUtilisals instance;
    @Getter
    private static BUtilisalsAPI api;
    @Getter
    private static Map<FileLocation, IConfiguration> configurations = Maps.newHashMap();
    @Getter
    private LibraryClassLoader libraryClassLoader;
    @Getter
    private AbstractStorageManager databaseManagement;

    public static IConfiguration getConfiguration(FileLocation location) {
        return configurations.get(location);
    }

    public static void log(String message) {
        System.out.println("[BungeeUtilisals] " + message);
    }

    @Override
    public void onEnable() {
        // Setting instance
        instance = this;

        // Creating datafolder if not exists.
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Loading setting files ...
        createAndLoadFiles();

        // Loading default PlaceHolders. Must be done BEFORE API / database loads.
        PlaceHolderAPI.loadPlaceHolderPack(new DefaultPlaceHolders());

        // Loading libraries
        loadLibraries();

        // Loading database
        loadDatabase();

        // Initializing API
        api = new BUtilisalsAPI(this);

        // Initialize metric system
        new Metrics(this);

        // Creating datafolder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        // Registering experimental features
        if (configurations.get(FileLocation.CONFIG).getBoolean("experimental")) {
            registerExperimentalFeatures();
        }

        // Register executors & listeners
        ProxyServer.getInstance().getPluginManager().registerListener(this, new UserConnectionListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new UserChatListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new PunishmentListener());

        IEventLoader loader = api.getEventLoader();

        loader.register(UserLoadEvent.class, new UserExecutor());

        UserChatExecutor chatExecutor = new UserChatExecutor(api.getChatManager());
        loader.register(UserChatEvent.class, chatExecutor);
        loader.register(UserChatPreExecuteEvent.class, chatExecutor);

        MuteCheckExecutor muteCheckExecutor = new MuteCheckExecutor();
        loader.register(UserChatEvent.class, muteCheckExecutor);
        loader.register(UserCommandEvent.class, muteCheckExecutor);
        loader.register(UserPunishEvent.class, new UserPunishExecutor());

        new PluginCommand();
        if (getConfiguration(FileLocation.PUNISHMENTS_CONFIG).getBoolean("enabled")) {
            new BanCommand();
            new IPBanCommand();
            new TempBanCommand();
            new IPTempBanCommand();
            new MuteCommand();
            new IPMuteCommand();
            new TempMuteCommand();
            new IPTempMuteCommand();
            new KickCommand();
            new WarnCommand();
        }
    }

    @Override
    public void onDisable() {
        try {
            databaseManagement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDatabase() {
        StorageType type;
        try {
            type = StorageType.valueOf(getConfig().getString("storage.type").toUpperCase());
        } catch (IllegalArgumentException e) {
            type = StorageType.MYSQL;
        }
        try {
            databaseManagement = type.getManager().getConstructor(Plugin.class).newInstance(this);
            databaseManagement.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLibraries() {
        log("Loading libraries ...");
        libraryClassLoader = new LibraryClassLoader(this);

        for (Library library : Library.values()) {
            if (library.shouldBeLoaded() && !library.isPresent()) {
                library.load();
            }
        }
        log("Libraries have been loaded.");
    }

    public IConfiguration getConfig() {
        return configurations.get(FileLocation.CONFIG);
    }

    private void createAndLoadFiles() {
        for (FileLocation location : FileLocation.values()) {
            File file = new File(getDataFolder(), location.getPath());

            if (!file.exists()) {
                FileUtils.createDefaultFile(this, location.getPath(), file, true);
            }

            YamlConfiguration configuration = (YamlConfiguration) IConfiguration.loadYamlConfiguration(file);
            configurations.put(location, configuration);
        }
    }

    private void registerExperimentalFeatures() {
        Utils.registerPacket(Protocol.GAME.TO_CLIENT, PacketPlayOutBossBar.class,
                Utils.createProtocolMapping(ProtocolConstants.MINECRAFT_1_9, 12),
                Utils.createProtocolMapping(ProtocolConstants.MINECRAFT_1_12_2, 12));

        ProxyServer.getInstance().getPluginManager().registerListener(this, new SimplePacketListener());

        PacketUpdateExecutor packetUpdateExecutor = new PacketUpdateExecutor();
        api.getEventLoader().register(PacketUpdateEvent.class, packetUpdateExecutor);
        api.getEventLoader().register(PacketReceiveEvent.class, packetUpdateExecutor);

        api.getEventLoader().register(UserLoadEvent.class, new EventExecutor() {

            @Event
            public void onLoad(UserLoadEvent event) {
                IBossBar bar = BUCore.getApi().createBossBar(UUID.randomUUID(), BarColor.PURPLE, BarStyle.TWELVE_SEGMENTS,
                        0.1F, Utils.format("&e&lBungeeUtilisals FTW :D"));

                bar.addUser(event.getUser());

                BungeeCord.getInstance().getScheduler().schedule(BungeeUtilisals.this, new Runnable() {

                    float progress = 0.1F;

                    @Override
                    public void run() {
                        progress += 0.1F;
                        if (progress > 1.0F) {
                            progress = 0.0F;
                        }

                        bar.setProgress(progress);
                        bar.setMessage(Utils.format(MathUtils.getRandomFromArray(ChatColor.values()).toString() + "BungeeUtilisals FTW :D"));
                    }

                }, 1, 1, TimeUnit.SECONDS);
            }

        });
    }
}