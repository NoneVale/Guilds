package net.nighthawkempires.guilds.util;

import net.nighthawkempires.guilds.GuildsPlugin;
import net.nighthawkempires.guilds.guild.GuildModel;
import org.bukkit.entity.Player;

public class AllyUtil {

    public static boolean isAlly(Player player, Player target) {
        GuildModel guild = GuildsPlugin.getUserRegistry().getUser(player.getUniqueId()).getGuild();
        GuildModel targetGuild = GuildsPlugin.getUserRegistry().getUser(target.getUniqueId()).getGuild();

        if (targetGuild != null) {
            if (guild != null) {
                return targetGuild.getKey().equals(guild.getKey()) || guild.isAllied(targetGuild.getUniqueId());
            }
        }

        return false;
    }
}
