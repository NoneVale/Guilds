package net.nighthawkempires.guilds.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.nighthawkempires.guilds.GuildsPlugin;
import net.nighthawkempires.guilds.guild.GuildModel;
import net.nighthawkempires.guilds.guild.GuildRank;
import net.nighthawkempires.guilds.user.UserModel;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.nighthawkempires.core.CorePlugin.*;
import static net.nighthawkempires.guilds.GuildsPlugin.*;
import static net.nighthawkempires.guilds.GuildsPlugin.getUserRegistry;

import static net.nighthawkempires.core.lang.Messages.*;
import static org.bukkit.ChatColor.*;

public class GuildAdminCommand implements CommandExecutor {

    public GuildAdminCommand() {
        getCommandManager().registerCommands("guildadmin", new String[] {
                "ne.guilds.admin"
        });

        commandList = Lists.newArrayList(
                getMessages().getCommand("gadmin", "bypass", "Toggle admin bypass"),
                getMessages().getCommand("gadmin", "help [page]", "Show this help menu"),
                getMessages().getCommand("gadmin", "join <guild>", "Join a guild"),
                getMessages().getCommand("gadmin", "name <guild> <name>", "Set a guild's name"),
                getMessages().getCommand("gadmin", "disband <guild>", "Disband a guild"),
                getMessages().getCommand("gadmin", "home <guild>", "Teleport to a guild's home"),
                getMessages().getCommand("gadmin", "unclaim [guild] [all]", "Unclaim a guild's territory"),
                getMessages().getCommand("gadmin", "leader <guild> <player>", "Set the leader of a guild")
        );

        disband = Lists.newArrayList();
        leader = Maps.newHashMap();
    }

    private List<String> commandList;
    private List<UUID> disband;
    private Map<UUID, UUID> leader;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UserModel user = getUserRegistry().getUser(player.getUniqueId());
            GuildModel guild = user.getGuild();

            if  (!player.hasPermission("ne.guilds.admin")) {
                player.sendMessage(getMessages().getChatTag(NO_PERMS));
                return true;
            }

            switch (args.length) {
                case 0:
                    sendPage(player, 1);
                    return true;
                case 1:
                    switch (args[0].toLowerCase()) {
                        case "bypass":
                            if (getPlayerData().bypassList.contains(player.getUniqueId())) {
                                getPlayerData().bypassList.remove(player.getUniqueId());
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have " + RED + "" + UNDERLINE + "" + ITALIC + "DISABLED"
                                        + GRAY + " admin bypass mode"));
                                return true;
                            } else {
                                getPlayerData().bypassList.add(player.getUniqueId());
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have " + GREEN + "" + UNDERLINE + "" + ITALIC + "ENABLED"
                                        + GRAY + " admin bypass mode"));
                                return true;
                            }
                        case "help":
                            sendPage(player, 1);
                            return true;
                        case "unclaim":
                            Chunk chunk = player.getLocation().getChunk();
                            if (!getGuildRegistry().isClaimed(chunk)) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "This land is not claimed by anyone."));
                                return true;
                            }

                            GuildModel claimedBy = getGuildRegistry().claimedBy(chunk);
                            if (claimedBy != null) {
                                claimedBy.unclaim(chunk);
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You unclaimed land at " + DARK_GRAY + "[" + GOLD
                                        + chunk.getX() + DARK_GRAY + ", " + GOLD + chunk.getZ() + DARK_GRAY + "]" + GRAY + " from guild "
                                        + claimedBy.getColor() + claimedBy.getName() + GRAY + "."));
                                claimedBy.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " unclaimed land at " + DARK_GRAY
                                        + "[" + GOLD + chunk.getX() + DARK_GRAY + ", " + GOLD + chunk.getZ() + DARK_GRAY + "]" + GRAY
                                        + " from your guild."), player);

                                if (claimedBy.getHome() != null) {
                                    if (claimedBy.getHome().getChunk() == chunk) {
                                        claimedBy.setHome(null);
                                        player.sendMessage(getMessages().getChatMessage(claimedBy.getColor() + claimedBy.getName() + "'s "
                                                + GRAY + " guilds home has been unset due to you"
                                                + " unclaiming the land it was in." ));
                                        claimedBy.message(getMessages().getChatMessage(GRAY + "Your guild's home has been unset due to " + GREEN
                                                + player.getName() + " unclaiming the land it was in."), player);
                                    }
                                }
                                return true;
                            }
                        default:
                            player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                            return true;
                    }
                case 2:
                    switch (args[0].toLowerCase()) {
                        case "help":
                            if (!NumberUtils.isDigits(args[1])) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "The page must be a valid number."));
                                return true;
                            }

                            int page = Integer.parseInt(args[1]);

                            if (page < 1 || page > getTotalPages()) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That is not a valid pages.  The range is 1 - " + getTotalPages() + "."));
                                return true;
                            }

                            sendPage(player, page);
                            return true;
                        case "join":
                            if (guild != null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're already in a guild."));
                                return true;
                            }

                            String name = args[1];

                            GuildModel other;
                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                                if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                    return true;
                                }

                                UserModel temp = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());
                                other = temp.getGuild();
                            }

                            if (other == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                return true;
                            }

                            other.addMember(player.getUniqueId());

                            user.setGuild(other);
                            user.setRank(GuildRank.RECRUIT);

                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have joined " + other.getColor() + other.getName() + GRAY + "."));
                            other.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has joined the guild."), player);
                            return true;
                        case "disband":
                            name = args[1];

                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                                if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                    return true;
                                }

                                UserModel temp = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());
                                other = temp.getGuild();
                            }

                            if (other == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                return true;
                            }

                            if (this.disband.contains(player.getUniqueId())) {
                                for (UUID uuid : guild.getMembers()) {
                                    UserModel temp = GuildsPlugin.getUserRegistry().getUser(uuid);
                                    temp.setGuild(null);
                                }

                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have disbanded guild " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                other.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has disbanded your guild."), player);
                                getGuildRegistry().remove(other);
                                return true;
                            } else {
                                this.disband.add(player.getUniqueId());
                                Bukkit.getScheduler().scheduleSyncDelayedTask(GuildsPlugin.getPlugin(),
                                        () -> this.disband.remove(player.getUniqueId()),
                                        600);
                                player.sendMessage(getMessages().getChatMessage(GRAY + "Run the command " + AQUA + "/gadmin " + DARK_AQUA + "disband" + GRAY
                                        + " again to confirm this action."));
                                return true;
                            }
                        case "home":
                            name = args[1];

                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                                if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                    return true;
                                }

                                UserModel temp = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());
                                other = temp.getGuild();
                            }

                            if (other == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                return true;
                            }

                            if (other.getHome() == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That guild does not have a home set."));
                                return true;
                            }

                            player.teleport(other.getHome());
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have teleported to " + other.getColor()
                                    + other.getName() + " guild home."));
                            return true;
                        default:
                            player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                            return true;
                    }
                case 3:
                    switch (args[0].toLowerCase()) {
                        case "name":
                            String name = args[1];

                            GuildModel other;
                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                                if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                    return true;
                                }

                                UserModel temp = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());
                                other = temp.getGuild();
                            }

                            if (other == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                return true;
                            }

                            name = args[2];

                            if (name.length() > 15) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but that name is too long."));
                                return true;
                            }

                            if (getGuildRegistry().guildExists(name)) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "A guild with that name already exists."));
                                return true;
                            }

                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have changed the name of the guild " +
                                    other.getColor() + other.getName() + GRAY + " to " + guild.getColor() + name + GRAY + "."));
                            other.setName(name);
                            other.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has changed the name of the guild to " +
                                    guild.getColor() + guild.getName() + GRAY + "."), player);
                            return true;
                        case "unclaim":
                            name = args[1];

                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                                if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                    return true;
                                }

                                UserModel temp = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());
                                other = temp.getGuild();
                            }

                            if (other == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                return true;
                            }

                            if (args[2].toLowerCase().equals("all")) {
                                if (other.getTerritory().size() == 0) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "Your guild doesn't have any claimed land."));
                                    return true;
                                }

                                other.unclaimAll();

                                if (other.getHome() != null) {
                                    other.setHome(null);
                                    player.sendMessage(getMessages().getChatMessage(other.getColor() + other.getName() + GRAY
                                            + " guild home has been unset due to you unclaiming the land it was in." ));
                                    other.message(getMessages().getChatMessage(GRAY + "Your guild's home has been unset due to " + GREEN
                                            + player.getName() + " unclaiming the land it was in."));
                                }

                                player.sendMessage(getMessages().getChatMessage(GRAY + "You unclaimed all of " + other.getColor()
                                        + other.getName() + "'s " + GRAY + " guild land."));
                                other.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has unclaimed all of the guild's land."), player);
                                return true;
                            } else {
                                player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                                return true;
                            }
                        case "leader":
                            name = args[1];

                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                                if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                    return true;
                                }

                                UserModel temp = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());
                                other = temp.getGuild();
                            }

                            if (other == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                return true;
                            }

                            name = args[2];

                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

                            if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatTag(PLAYER_NOT_FOUND));
                                return true;
                            }

                            UserModel target = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());

                            if (!guild.getMembers().contains(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That player is not in that guild."));
                                return true;
                            }

                            if (offlinePlayer.getUniqueId().equals(other.getLeader())) {
                                player.sendMessage(getMessages().getChatMessage(GREEN + offlinePlayer.getName() + GRAY
                                        + " is already the leader of that guild."));
                                return true;
                            }

                            if (leader.containsKey(player.getUniqueId()) && leader.get(player.getUniqueId()) == offlinePlayer.getUniqueId()) {
                                leader.remove(player.getUniqueId());

                                UserModel leader = getUserRegistry().getUser(guild.getLeader());

                                guild.setLeader(offlinePlayer.getUniqueId());

                                target.setRank(GuildRank.LEADER);
                                leader.setRank(GuildRank.OFFICER);

                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have transferred guild leadership of guild " +
                                        other.getColor() + other.getName() + GRAY + " to " + GREEN + offlinePlayer.getName() + GRAY + "."));
                                if (offlinePlayer.isOnline()) {
                                    offlinePlayer.getPlayer().sendMessage(getMessages().getChatMessage(GREEN + player.getName() + GRAY +
                                            "has transferred guild leadership to you."));
                                    guild.message(getMessages().getChatMessage(GREEN + offlinePlayer.getName() + GRAY + " was made leader of the guild by "
                                            + GREEN + player.getName() + GRAY + "."), player, offlinePlayer.getPlayer());
                                    return true;
                                }
                                guild.message(getMessages().getChatMessage(GREEN + offlinePlayer.getName() + GRAY + " was made leader of the guild by "
                                        + GREEN + player.getName() + GRAY + "."), player, offlinePlayer.getPlayer());
                                return true;
                            } else {
                                leader.put(player.getUniqueId(), offlinePlayer.getUniqueId());
                                Bukkit.getScheduler().scheduleSyncDelayedTask(GuildsPlugin.getPlugin(),
                                        () -> this.leader.remove(player.getUniqueId()),
                                        600);
                                player.sendMessage(getMessages().getChatMessage(GRAY + "Run the command " + AQUA + "/gadmin " + DARK_AQUA + "leader "
                                        + other.getName() + " " + offlinePlayer.getName() + GRAY + " again to confirm that you want to make " + GREEN
                                        + offlinePlayer.getName() + GRAY + " the leader of the guild."));
                                return true;
                            }
                        default:
                            player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                            return true;
                    }
                default:
                    player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                    return true;
            }
        }
        return false;
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) commandList.size() / 10);
    }

    private void sendPage(CommandSender sender, int page) {
        int displayPage = page;
        page = page - 1;

        int start = 10 * page;
        int finish = Math.min(start + 10, commandList.size());

        String[] help = new String[]{
                getMessages().getMessage(CHAT_HEADER),
                getHelpLine(displayPage, getTotalPages()),
                getMessages().getMessage(CHAT_FOOTER)
        };
        sender.sendMessage(help);

        for (int i = start; i < finish; i++) {
            sender.sendMessage(commandList.get(i));
        }

        sender.sendMessage(getMessages().getMessage(CHAT_FOOTER));
    }

    public String getHelpLine(int page, int total) {
        return DARK_GRAY + "Guild Admin Help Topic Page" + GRAY + ": " + DARK_GRAY + "[" + GOLD + ""
                + UNDERLINE + page + DARK_GRAY + "/" + GOLD + "" + UNDERLINE + total + DARK_GRAY + "]";
    }
}
