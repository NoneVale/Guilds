package net.nighthawkempires.guilds.listeners;

import net.nighthawkempires.core.CorePlugin;
import net.nighthawkempires.guilds.GuildsPlugin;
import net.nighthawkempires.guilds.guild.GuildModel;
import net.nighthawkempires.guilds.user.UserModel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static net.nighthawkempires.guilds.GuildsPlugin.*;
import static org.bukkit.ChatColor.*;

public class PlayerListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UserModel user = getUserRegistry().getUser(player.getUniqueId());

        if (player.getKiller() != null) {
            Player killer = player.getKiller();
            UserModel kuser = getUserRegistry().getUser(killer.getUniqueId());

            if (user.getPower() == 0 || user.getPower() == 1) {
                user.setPower(0);
            } else {
                user.setPower(user.getPower() - 2);
            }

            if (kuser.getPower() < 10)
                kuser.setPower(kuser.getPower() + 1);

            player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "Your power is now " + GOLD + user.getPower() + GRAY + "."));
        } else {
            if (user.getPower() == 0) {
                user.setPower(0);
            } else {
                user.setPower(user.getPower() - 1);
            }
            player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "Your power is now " + GOLD + user.getPower() + GRAY + "."));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UserModel userModel = getUserRegistry().getUser(player.getUniqueId());

        GuildModel claimedBy = getGuildRegistry().claimedBy(player.getLocation().getChunk());
        boolean inTerritory = false;
        if (claimedBy != null) {
            if (!getPlayerData().locationMap.containsKey(player.getUniqueId()) || getPlayerData().locationMap.get(player.getUniqueId()) != claimedBy) {
                getPlayerData().locationMap.put(player.getUniqueId(), claimedBy);
                player.sendTitle(claimedBy.getColor() + claimedBy.getName(), GRAY + claimedBy.getDescription(), 10, 30, 10);
            }
            inTerritory = true;
        }

        if (!inTerritory) {
            getPlayerData().locationMap.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        getPlayerData().locationMap.remove(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        getPlayerData().locationMap.remove(player.getUniqueId());
    }
}