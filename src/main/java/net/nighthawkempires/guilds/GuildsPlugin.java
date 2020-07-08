package net.nighthawkempires.guilds;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import net.nighthawkempires.core.server.ServerType;
import net.nighthawkempires.guilds.commands.GuildAdminCommand;
import net.nighthawkempires.guilds.commands.GuildCommand;
import net.nighthawkempires.guilds.data.InventoryData;
import net.nighthawkempires.guilds.data.PlayerData;
import net.nighthawkempires.guilds.guild.GuildTag;
import net.nighthawkempires.guilds.guild.registry.GuildRegistry;
import net.nighthawkempires.guilds.guild.registry.MGuildRegistry;
import net.nighthawkempires.guilds.listeners.GuildListener;
import net.nighthawkempires.guilds.listeners.InventoryListener;
import net.nighthawkempires.guilds.listeners.PlayerListener;
import net.nighthawkempires.guilds.scoreboard.GuildsScoreboard;
import net.nighthawkempires.guilds.task.BoundaryTask;
import net.nighthawkempires.guilds.task.PowerTask;
import net.nighthawkempires.guilds.user.registry.MUserRegistry;
import net.nighthawkempires.guilds.user.registry.UserRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import static net.nighthawkempires.core.CorePlugin.*;

public class GuildsPlugin extends JavaPlugin {

    private static Plugin plugin;

    private static GuildRegistry guildRegistry;
    private static UserRegistry userRegistry;

    private static MongoDatabase mongoDatabase;

    private static boolean regionsEnabled;

    private static InventoryData inventoryData;
    private static PlayerData playerData;

    public void onEnable() {
        //oof
        plugin = this;
        if (getConfigg().getServerType() != ServerType.SETUP) {
            String pluginName = getPlugin().getName();
            try {
                String hostname = getConfigg().getMongoHostname();
                String database = getConfigg().getMongoDatabase().replaceAll("%PLUGIN%", pluginName);
                String username = getConfigg().getMongoUsername().replaceAll("%PLUGIN%", pluginName);
                String password = getConfigg().getMongoPassword();

                ServerAddress serverAddress = new ServerAddress(hostname);
                MongoCredential mongoCredential = MongoCredential.createCredential(username, database, password.toCharArray());
                mongoDatabase = new MongoClient(serverAddress, mongoCredential, new MongoClientOptions.Builder().build()).getDatabase(database);

                guildRegistry = new MGuildRegistry(mongoDatabase);
                getGuildRegistry().loadAllFromDb();

                userRegistry = new MUserRegistry(mongoDatabase);

                getLogger().info("Successfully connected to MongoDB.");

                registerCommands();
                registerListeners();
                registerTabCompleters();
                registerTasks();

                //CorePlugin.getScoreboardManager().addScoreboard(new PermissionsScoreboard());
                getChatFormat().add(new GuildTag());
                getScoreboardManager().addScoreboard(new GuildsScoreboard());

                regionsEnabled = Bukkit.getPluginManager().isPluginEnabled("Regions");

                inventoryData = new InventoryData();
                playerData = new PlayerData();
            } catch (Exception exception) {
                exception.printStackTrace();
                getLogger().warning("Could not connect to MongoDB, shutting plugin down...");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    public void registerCommands() {
        this.getCommand("guild").setExecutor(new GuildCommand());
        this.getCommand("guildadmin").setExecutor(new GuildAdminCommand());
    }

    public void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new GuildListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new PlayerListener(), this);

    }

    public void registerTabCompleters() {

    }

    public void registerTasks() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BoundaryTask(), 0, 10);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new PowerTask(), 1200, 2400);
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static GuildRegistry getGuildRegistry() {
        return guildRegistry;
    }

    public static UserRegistry getUserRegistry() {
        return userRegistry;
    }

    public static boolean isRegionsEnabled() {
        return regionsEnabled;
    }

    public static InventoryData getInventoryData() {
        return inventoryData;
    }

    public static PlayerData getPlayerData() {
        return playerData;
    }
}
