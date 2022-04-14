package net.nighthawkempires.guilds.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.nighthawkempires.guilds.GuildsPlugin;
import net.nighthawkempires.guilds.guild.GuildModel;
import net.nighthawkempires.guilds.guild.GuildRank;
import net.nighthawkempires.guilds.guild.GuildRelation;
import net.nighthawkempires.guilds.user.UserModel;
import net.nighthawkempires.regions.RegionsPlugin;
import net.nighthawkempires.regions.region.RegionModel;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.nighthawkempires.core.CorePlugin.*;
import static net.nighthawkempires.core.lang.Messages.*;
import static net.nighthawkempires.guilds.GuildsPlugin.*;
import static org.bukkit.ChatColor.*;

public class GuildCommand implements CommandExecutor {

    public GuildCommand() {
        getCommandManager().registerCommands("guild", new String[] {
                "ne.guilds"
        });

        commandList = Lists.newArrayList(
                getMessages().getCommand("g", "claim", "Claim the chunk you're in"),
                getMessages().getCommand("g", "create <name>", "Create a guild"),
                getMessages().getCommand("g", "help [page]", "Show this help menu"),
                getMessages().getCommand("g", "join <guild>", "Join a guild"),
                getMessages().getCommand("g", "invite <player>", "Invite a player to your guild"),
                getMessages().getCommand("g", "info [guild]", "Show a guild's info"),
                getMessages().getCommand("g", "show [player]", "Show a player's guild info"),
                getMessages().getCommand("g", "kick <player>", "Kick a player from the guild"),
                getMessages().getCommand("g", "list [page]", "Show a lit of guilds"),
                getMessages().getCommand("g", "name <name>", "Set the guild's name"),
                getMessages().getCommand("g", "sethome", "Set the guild's home"),
                getMessages().getCommand("g", "disband", "Disband the guild"),
                getMessages().getCommand("g", "color", "Open the Guild Color Shop"),
                getMessages().getCommand("g", "home", "Teleport to the guild home"),
                getMessages().getCommand("g", "ally <guild>", "Ally with another guild"),
                getMessages().getCommand("g", "truce <guild>", "Truce with another guild"),
                getMessages().getCommand("g", "neutral <guild>", "Go neutral with a guild"),
                getMessages().getCommand("g", "enemy <guild>", "Enemy another guild"),
                getMessages().getCommand("g", "unclaim [all]", "Unclaim territory"),
                getMessages().getCommand("g", "desc <desc>", "Set the guild's description"),
                getMessages().getCommand("g", "leave", "Leave the guild"),
                getMessages().getCommand("g", "leader <player>", "Set the leader of the guild"),
                getMessages().getCommand("g", "promote <player>", "Promote a member of the guild"),
                getMessages().getCommand("g", "demote <player>", "Demote a member of the guild"),
                getMessages().getCommand("g", "rank <player> <rank>", "Set a member's rank"),
                getMessages().getCommand("g", "map", "Show a map of guilds around you"),
                getMessages().getCommand("g", "showchunks", "Show chunk boundaries")
        );

        this.disband = Lists.newArrayList();

        this.leader = Maps.newHashMap();
    }

    private List<String> commandList;
    private List<UUID> disband;

    private Map<UUID, UUID> leader;

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UserModel user = GuildsPlugin.getUserRegistry().getUser(player.getUniqueId());
            GuildModel guild = user.getGuild();

            if (!player.hasPermission("ne.guilds")) {
                player.sendMessage(getMessages().getChatTag(NO_PERMS));
                return true;
            }

            switch (args.length) {
                case 0:
                    sendPage(player, 1);
                    return true;
                case 1:
                    switch (args[0].toLowerCase()) {
                        case "claim":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild in order to do this."));
                                return true;
                            }

                            Chunk chunk = player.getLocation().getChunk();

                            if (isRegionsEnabled()) {
                                RegionModel region = RegionsPlugin.getRegionRegistry().getRegion(chunk);
                                if (region != null) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You can not claim this land, as part of it is in a protected region."));
                                    return true;
                                }
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            if (getGuildRegistry().isClaimed(chunk)) {
                                if (getGuildRegistry().claimedBy(chunk) == user.getGuild()) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "This land is already claimed by your guild."));
                                    return true;
                                }
                            }

                            if (guild.getTerritory().size() >= guild.getMembers().size() * 10) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "Your guild does not have enough land power to claim this."));
                                return true;
                            }

                            GuildModel rival = getGuildRegistry().claimedBy(chunk);
                            if (rival != null) {
                                if (guild.getRelationStatus(rival) == GuildRelation.ALLY
                                        || guild.getRelationStatus(rival) == GuildRelation.TRUCE) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "This land is already claimed by your "
                                            + guild.getRelationStatus(rival).getName().toLowerCase() + " "
                                            + guild.getColor() + guild.getName() + GRAY + "."));
                                    return true;
                                }

                                int power = 0;
                                for (UUID uuid : guild.getMembers()) {
                                    UserModel temp = GuildsPlugin.getUserRegistry().getUser(uuid);
                                    power += temp.getPower();
                                }

                                power /= guild.getMembers().size();

                                int powerRival = 0;
                                for (UUID uuid : rival.getMembers()) {
                                    UserModel temp = GuildsPlugin.getUserRegistry().getUser(uuid);
                                    powerRival += temp.getPower();
                                }

                                powerRival /= rival.getMembers().size();

                                if (power > ((int) Math.ceil((double) powerRival * 2.5))) {
                                    rival.unclaim(chunk);
                                    rival.message(guild.getColor() + guild.getName() + GRAY + " claimed over your land at " + DARK_GRAY
                                            + "[" + GOLD + chunk.getX() + DARK_GRAY + ", " + GOLD + chunk.getZ() + DARK_GRAY + "]" + GRAY + ".");

                                    if (rival.getHome() != null) {
                                        if (rival.getHome().getChunk() == chunk) {
                                            rival.setHome(null);
                                            rival.message(getMessages().getChatMessage(GRAY + "Your guild's home has been unset due to " + guild.getColor()
                                                    + guild.getName() + " claiming over the land it was in."));
                                        }
                                    }
                                } else {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "This land is already claimed by "
                                            + guild.getColor() + guild.getName() + GRAY + "."));
                                    return true;
                                }
                            }

                            guild.claim(chunk);
                            if (rival != null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You claimed over " + rival.getColor()
                                        + rival.getName() + "'s" + GRAY + " land at " + DARK_GRAY + "[" + GOLD + chunk.getX()
                                        + DARK_GRAY + ", " + GOLD + chunk.getZ() + DARK_GRAY + "]" + GRAY
                                        + " for your guild."));
                                guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " claimed over " + rival.getColor()
                                        + rival.getName() + "'s" + GRAY + " land at " + DARK_GRAY + "[" + GOLD + chunk.getX()
                                        + DARK_GRAY + ", " + GOLD + chunk.getZ() + DARK_GRAY + "]" + GRAY
                                        + " for the guild."), player);
                                return true;
                            } else {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You claimed land at " + DARK_GRAY + "[" + GOLD
                                        + chunk.getX() + DARK_GRAY + ", " + GOLD + chunk.getZ() + DARK_GRAY + "]" + GRAY
                                        + " for your guild."));
                                guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " claimed land at " + DARK_GRAY
                                        + "[" + GOLD + chunk.getX() + DARK_GRAY + ", " + GOLD + chunk.getZ() + DARK_GRAY + "]" + GRAY
                                        + " for the guild."), player);
                                return true;
                            }
                        case "color":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild in order to do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            player.openInventory(getInventoryData().getColorShopInventory());
                            return true;
                        case "disband":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but you must be the leader of the guild in order to do this."));
                                return true;
                            }

                            if (this.disband.contains(player.getUniqueId())) {
                                for (UUID uuid : guild.getMembers()) {
                                    UserModel temp = GuildsPlugin.getUserRegistry().getUser(uuid);
                                    temp.setGuild(null);
                                }

                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have disbanded your guild."));
                                guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has disbanded the guild."), player);
                                getGuildRegistry().remove(guild);
                                return true;
                            } else {
                                this.disband.add(player.getUniqueId());
                                Bukkit.getScheduler().scheduleSyncDelayedTask(GuildsPlugin.getPlugin(),
                                        () -> this.disband.remove(player.getUniqueId()),
                                        600);
                                player.sendMessage(getMessages().getChatMessage(GRAY + "Run the command " + AQUA + "/g " + DARK_AQUA + "disband" + GRAY
                                        + " again to confirm this action."));
                                return true;
                            }
                        case "help":
                            sendPage(player, 1);
                            return true;
                        case "home":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (guild.getHome() == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "Your guild does not have a home set."));
                                return true;
                            }

                            player.teleport(guild.getHome());
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have teleported to your guild's home."));
                            return true;
                        case "info":
                        case "show":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            sendGuildInfo(player, guild);
                            return true;
                        case "leave":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() == GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can either disband the guild, or pass leadership on to leave the guild."));
                                return true;
                            }

                            user.setGuild(null);
                            guild.removeMember(player.getUniqueId());
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have left the guild."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has left the guild."), player);
                            return true;
                        case "list":
                            sendGuildPage(player, 1);
                            return true;
                        case "map":
                            sendGuildMap(player);
                            return true;
                        case "sethome":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            chunk = player.getLocation().getChunk();
                            if (!getGuildRegistry().claimedBy(chunk).getKey().equals(guild.getKey())) {
                                player.sendMessage(getMessages().getChatMessage("You can only set the guild's home in land claimed by the guild."));
                                return true;
                            }

                            guild.setHome(player.getLocation());
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have set the guild's home."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has set the guild's home."), player);
                            return true;
                        case "sc":
                        case "showchunks":
                            if (getPlayerData().boundaryList.contains(player.getUniqueId())) {
                                getPlayerData().boundaryList.remove(player.getUniqueId());
                                player.sendMessage(getMessages().getChatMessage(GRAY + "No longer displaying chunk boundaries."));
                                return true;
                            } else {
                                getPlayerData().boundaryList.add(player.getUniqueId());
                                player.sendMessage(getMessages().getChatMessage(GRAY + "Now displaying chunk boundaries."));
                                return true;
                            }
                        case "unclaim":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            chunk = player.getLocation().getChunk();
                            if (!getGuildRegistry().isClaimed(chunk)) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "This land is not claimed by anyone."));
                                return true;
                            }

                            GuildModel claimedBy = getGuildRegistry().claimedBy(chunk);
                            if (guild.getKey().equals(claimedBy.getKey())) {
                                guild.unclaim(chunk);
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You unclaimed land at " + DARK_GRAY + "[" + GOLD
                                        + chunk.getX() + DARK_GRAY + ", " + GOLD + chunk.getZ() + DARK_GRAY + "]" + GRAY + "."));
                                guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " unclaimed land at " + DARK_GRAY
                                        + "[" + GOLD + chunk.getX() + DARK_GRAY + ", " + GOLD + chunk.getZ() + DARK_GRAY + "]" + GRAY + "."), player);

                                if (guild.getHome() != null) {
                                    if (guild.getHome().getChunk() == chunk) {
                                        guild.setHome(null);
                                        player.sendMessage(getMessages().getChatMessage(GRAY + "Your guild's home has been unset due to you"
                                                + " unclaiming the land it was in." ));
                                        guild.message(getMessages().getChatMessage(GRAY + "Your guild's home has been unset due to " + GREEN
                                                + player.getName() + " unclaiming the land it was in."), player);
                                    }
                                }
                                return true;
                            } else {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not unclaim " + claimedBy.getColor()
                                        + claimedBy.getName() + "'s " + GRAY + "land."));
                                return true;
                            }
                        default:
                            player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                            return true;
                    }
                case 2:
                    switch (args[0].toLowerCase()) {
                        case "ally":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            String name = args[1];

                            GuildModel other = null;
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

                            if (guild.getKey().equals(other.getKey())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not set relation status' with your own guild."));
                                return true;
                            }

                            if (guild.getRelationStatus(guild) == GuildRelation.ALLY) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You are already allied with " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                return true;
                            }

                            if (guild.getRelation(guild) == GuildRelation.ALLY) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have already sent an ally request to " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                return true;
                            }

                            guild.setRelation(other, GuildRelation.ALLY);
                            if (other.getRelation(guild) == GuildRelation.ALLY) {
                                other.message(getMessages().getChatMessage(guild.getColor() + guild.getName() + GRAY + " has accepted your request to be allies."));
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have accepted " + other.getColor()
                                        + other.getName() + "'s " + GRAY + "ally request."));
                                guild.message(getMessages().getChatMessage(GREEN + " has accepted " + other.getColor()
                                        + other.getName() + "'s " + GRAY + "ally request."), player);
                            } else {
                                other.message(getMessages().getChatMessage(guild.getColor() + guild.getName() + GRAY + " has requested to be allies."));
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have requested to be allies with " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                guild.message(getMessages().getChatMessage(GREEN + " has requested to be allies with " + other.getColor()
                                        + other.getName() + GRAY + "."), player);
                            }
                            return true;
                        case "create":
                            if (user.getGuild() != null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're already in a guild."));
                                return true;
                            }

                            name = args[1];

                            if (name.length() > 15) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but that name is too long."));
                                return true;
                            }

                            if (getGuildRegistry().guildExists(name)) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "A guild with that name already exists."));
                                return true;
                            }

                            UUID uuid = UUID.randomUUID();
                            while (getGuildRegistry().guildExists(uuid)) {
                                uuid = UUID.randomUUID();
                            }

                            guild = getGuildRegistry().getGuild(uuid);

                            guild.setName(name);
                            guild.setLeader(player.getUniqueId());
                            guild.addMember(player.getUniqueId());

                            user.setGuild(guild);
                            user.setRank(GuildRank.LEADER);

                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have created a guild by the name of " + guild.getColor()
                                    + guild.getName() + GRAY + "."));
                            getMessages().broadcatServerMessage(GREEN + player.getName() + GRAY + " has created a guild by the name of "
                                    + guild.getColor() + guild.getName() + GRAY + ".");
                            return true;
                        case "demote":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            name = args[1];
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

                            if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatTag(PLAYER_NOT_FOUND));
                                return true;
                            }

                            UserModel target = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());

                            if (!guild.getMembers().contains(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That player is not in your guild."));
                                return true;
                            }

                            if (offlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not demote yourself."));
                                return true;
                            }

                            if (target.getRank() == user.getRank()) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not demote that player since they're the same rank as you."));
                                return true;
                            }

                            switch (target.getRank()) {
                                case OFFICER:
                                    target.setRank(GuildRank.MEMBER);
                                    break;
                                case MEMBER:
                                    target.setRank(GuildRank.RECRUIT);
                                    break;
                                case RECRUIT:
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "This player can not be demoted any lower."));
                                    return true;
                            }

                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have demoted " + GREEN + offlinePlayer.getName() + GRAY
                                    + " to " + WHITE + target.getRank().getName() + GRAY + "."));
                            if (offlinePlayer.isOnline())
                                offlinePlayer.getPlayer().sendMessage(getMessages().getChatMessage(GRAY + "You have been demoted" +
                                        " to " + WHITE + target.getRank().getName() + GRAY + "."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has demoted " + GREEN
                                    + offlinePlayer.getName() + GRAY + " to " + WHITE + target.getRank().getName() + GRAY + "."), player);
                            return true;
                        case "desc":
                        case "description":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            guild.setDescription(args[1]);
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have set the guild's description to " + YELLOW + args[1] + GRAY + "."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has set the guild's description to "
                                    + YELLOW + args[1] + GRAY + "."), player);
                            return true;
                        case "enemy":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            name = args[1];

                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                offlinePlayer = Bukkit.getOfflinePlayer(name);
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

                            if (guild.getKey().equals(other.getKey())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not set relation status' with your own guild."));
                                return true;
                            }

                            if (guild.getRelation(guild) == GuildRelation.ENEMY) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have already enemies with " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                return true;
                            }

                            guild.setRelation(other, GuildRelation.ENEMY);
                            other.message(getMessages().getChatMessage(guild.getColor() + guild.getName() + GRAY + " is now an enemy guild."));
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You are now enemies with " + other.getColor()
                                    + other.getName() + GRAY + "."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + " has enemied guild " + other.getColor()
                                    + other.getName() + GRAY + "."), player);
                            return true;
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
                        case "info":
                        case "show":
                            name = args[1];

                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                offlinePlayer = Bukkit.getOfflinePlayer(name);
                                if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                    return true;
                                }

                                UserModel userModel = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());
                                other = userModel.getGuild();
                            }

                            if (other == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but I could not find the guild you're looking for."));
                                return true;
                            }

                            sendGuildInfo(player, other);
                            return true;
                        case "inv":
                        case "invite":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            name = args[1];
                            offlinePlayer = Bukkit.getOfflinePlayer(name);

                            if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatTag(PLAYER_NOT_FOUND));
                                return true;
                            }

                            if (guild.getMembers().contains(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GREEN + offlinePlayer.getName() + GRAY + " is already in the guild."));
                                return true;
                            }

                            if (guild.isInvited(offlinePlayer.getUniqueId())) {
                                guild.removeInvite(offlinePlayer.getUniqueId());

                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have revoked " + GREEN + offlinePlayer.getName() + "'s "
                                        + GRAY + "invitation to the guild."));
                                guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has revoked " + GREEN
                                        + offlinePlayer.getName() + "'s " + GRAY + "invitation to the guild." ), player);
                                if (offlinePlayer.isOnline())
                                    offlinePlayer.getPlayer().sendMessage(getMessages().getChatMessage(GREEN + player.getName() + GRAY +
                                            " has revoked your invitation to " + guild.getColor() + guild.getName() + GRAY + "."));
                                return true;
                            } else {
                                guild.addInvite(offlinePlayer.getUniqueId());
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have invited " + GREEN + offlinePlayer.getName()
                                        + GRAY + " to the guild."));
                                guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has invited " + GREEN
                                        + offlinePlayer.getName() + GRAY + " to the guild." ), player);
                                if (offlinePlayer.isOnline())
                                    offlinePlayer.getPlayer().sendMessage(getMessages().getChatMessage(GREEN + player.getName() + GRAY +
                                            " has invited you to " + guild.getColor() + guild.getName() + GRAY + "."));
                                return true;
                            }
                        case "join":
                            if (guild != null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're already in a guild."));
                                return true;
                            }

                            name = args[1];

                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                offlinePlayer = Bukkit.getOfflinePlayer(name);
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

                            if (!other.isInvited(player.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not invited to " + other.getColor() + other.getName()
                                        + GRAY + "."));
                                other.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " tried to join the guild."));
                                return true;
                            }

                            other.removeInvite(player.getUniqueId());
                            other.addMember(player.getUniqueId());

                            user.setGuild(other);
                            user.setRank(GuildRank.RECRUIT);

                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have joined " + other.getColor() + other.getName() + GRAY + "."));
                            other.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has joined the guild."), player);
                            return true;
                        case "kick":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            name = args[1];
                            offlinePlayer = Bukkit.getOfflinePlayer(name);

                            if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatTag(PLAYER_NOT_FOUND));
                                return true;
                            }

                            target = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());

                            if (!guild.getMembers().contains(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That player is not in your guild."));
                                return true;
                            }

                            if (offlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not kick yourself from the guild."));
                                return true;
                            }

                            if (target.getRank() == user.getRank()) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not kick that player since they're the same rank as you."));
                                return true;
                            }

                            guild.removeMember(offlinePlayer.getUniqueId());

                            target.setGuild(null);

                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have kicked " + GREEN + offlinePlayer.getName() + GRAY
                                    + " from the guild."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has kicked " + GREEN + offlinePlayer.getName()
                                    + " from the guild."), player);

                            if (offlinePlayer.isOnline())
                                offlinePlayer.getPlayer().sendMessage(getMessages().getChatMessage(GRAY + "You have been kicked from "
                                        + guild.getColor() + guild.getName() + GRAY + "."));
                            return true;
                        case "leader":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but you must be the leader of the guild in order to do this."));
                                return true;
                            }

                            name = args[1];
                            offlinePlayer = Bukkit.getOfflinePlayer(name);

                            if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatTag(PLAYER_NOT_FOUND));
                                return true;
                            }

                            target = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());

                            if (!guild.getMembers().contains(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That player is not in your guild."));
                                return true;
                            }

                            if (offlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're already the leader of the guild."));
                                return true;
                            }

                            if (leader.containsKey(player.getUniqueId()) && leader.get(player.getUniqueId()) == offlinePlayer.getUniqueId()) {
                                leader.remove(player.getUniqueId());

                                guild.setLeader(offlinePlayer.getUniqueId());

                                target.setRank(GuildRank.LEADER);
                                user.setRank(GuildRank.OFFICER);

                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have transferred guild leadership to "
                                        + GREEN + offlinePlayer.getName() + GRAY + "."));
                                if (offlinePlayer.isOnline()) {
                                    offlinePlayer.getPlayer().sendMessage(getMessages().getChatMessage(GREEN + player.getName() + GRAY +
                                            "has transferred guild leadership to you."));
                                    guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has transferred guild leadership to "
                                            + GREEN + offlinePlayer.getName() + GRAY + "."), player, offlinePlayer.getPlayer());
                                    return true;
                                }
                                guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has transferred guild leadership to "
                                        + GREEN + offlinePlayer.getName() + GRAY + "."), player);
                                return true;
                            } else {
                                leader.put(player.getUniqueId(), offlinePlayer.getUniqueId());
                                Bukkit.getScheduler().scheduleSyncDelayedTask(GuildsPlugin.getPlugin(),
                                        () -> this.leader.remove(player.getUniqueId()),
                                        600);
                                player.sendMessage(getMessages().getChatMessage(GRAY + "Run the command " + AQUA + "/g " + DARK_AQUA + "leader "
                                        + offlinePlayer.getName() + GRAY + " again to confirm that you want to make " + GREEN + offlinePlayer.getName()
                                        + GRAY + " the leader of the guild."));
                                return true;
                            }
                        case "list":
                            if (!NumberUtils.isDigits(args[1])) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "The page must be a valid number."));
                                return true;
                            }

                            page = Integer.parseInt(args[1]);

                            if (page < 1 || page > getTotalGuildPages()) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That is not a valid pages.  The range is 1 - " + getTotalGuildPages() + "."));
                                return true;
                            }

                            sendGuildPage(player, page);
                            return true;
                        case "name":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but you must be the leader of the guild in order to do this."));
                                return true;
                            }

                            name = args[1];

                            if (name.length() > 15) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "I'm sorry, but that name is too long."));
                                return true;
                            }

                            if (getGuildRegistry().guildExists(name)) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "A guild with that name already exists."));
                                return true;
                            }

                            guild.setName(name);
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have changed the name of the guild to "
                                    + guild.getColor() + guild.getName() + GRAY + "."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has changed the name of the guild to " +
                                    guild.getColor() + guild.getName() + GRAY + "."), player);
                            return true;
                        case "neutral":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            name = args[1];

                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                offlinePlayer = Bukkit.getOfflinePlayer(name);
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

                            if (guild.getKey().equals(other.getKey())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not set relation status' with your own guild."));
                                return true;
                            }

                            if (guild.getRelation(guild) == GuildRelation.NEUTRAL) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're already neutral with " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                return true;
                            }

                            guild.setRelation(other, GuildRelation.NEUTRAL);
                            if (other.getRelation(guild) == GuildRelation.NEUTRAL) {
                                other.message(getMessages().getChatMessage(guild.getColor() + guild.getName() + GRAY + " has accepted your request to be neutral."));
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have accepted " + other.getColor()
                                        + other.getName() + "'s " + GRAY + "neutral request."));
                                guild.message(getMessages().getChatMessage(GREEN + " has accepted " + other.getColor()
                                        + other.getName() + "'s " + GRAY + "neutral request."), player);
                            } else {
                                other.message(getMessages().getChatMessage(guild.getColor() + guild.getName() + GRAY + " has requested to be neutral."));
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have requested to be neutral with " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                guild.message(getMessages().getChatMessage(GREEN + " has requested to be neutral with " + other.getColor()
                                        + other.getName() + GRAY + "."), player);
                            }
                            return true;
                        case "promote":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            name = args[1];
                            offlinePlayer = Bukkit.getOfflinePlayer(name);

                            if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatTag(PLAYER_NOT_FOUND));
                                return true;
                            }

                            target = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());

                            if (!guild.getMembers().contains(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That player is not in your guild."));
                                return true;
                            }

                            if (offlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not promote yourself."));
                                return true;
                            }

                            if (target.getRank() == user.getRank()) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not promote that player since they're the same rank as you."));
                                return true;
                            }

                            if (target.getRank() == GuildRank.LEADER && user.getRank() == GuildRank.MEMBER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to promote members to Officer."));
                                return true;
                            }

                            switch (target.getRank()) {
                                case OFFICER:
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "This player can not be promoted any higher." +
                                            "  If you're wanting to make them the leader of the guild run the command " + AQUA + "/g "
                                            + DARK_AQUA + " leader <player>" + GRAY + "."));
                                    return true;
                                case MEMBER:
                                    target.setRank(GuildRank.OFFICER);
                                    break;
                                case RECRUIT:
                                    target.setRank(GuildRank.MEMBER);
                                    break;
                            }

                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have promoted " + GREEN + offlinePlayer.getName() + GRAY
                                    + " to " + WHITE + target.getRank().getName() + GRAY + "."));
                            if (offlinePlayer.isOnline())
                                offlinePlayer.getPlayer().sendMessage(getMessages().getChatMessage(GRAY + "You have been promoted" +
                                        " to " + WHITE + target.getRank().getName() + GRAY + "."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has promoted " + GREEN
                                    + offlinePlayer.getName() + GRAY + " to " + WHITE + target.getRank().getName() + GRAY + "."), player);
                            return true;
                        case "truce":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            name = args[1];

                            if (getGuildRegistry().guildExists(name)) {
                                other = getGuildRegistry().getGuild(name);
                            } else {
                                offlinePlayer = Bukkit.getOfflinePlayer(name);
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

                            if (guild.getKey().equals(other.getKey())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not set relation status' with your own guild."));
                                return true;
                            }

                            if (guild.getRelationStatus(guild) == GuildRelation.TRUCE) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You are already truced with " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                return true;
                            }

                            if (guild.getRelation(guild) == GuildRelation.TRUCE) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have already sent a truce request to " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                return true;
                            }

                            guild.setRelation(other, GuildRelation.TRUCE);
                            if (other.getRelation(guild) == GuildRelation.TRUCE) {
                                other.message(getMessages().getChatMessage(guild.getColor() + guild.getName() + GRAY + " has accepted your truce request."));
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have accepted " + other.getColor()
                                        + other.getName() + "'s " + GRAY + "truce request."));
                                guild.message(getMessages().getChatMessage(GREEN + " has accepted " + other.getColor()
                                        + other.getName() + "'s " + GRAY + "truce request."), player);
                            } else {
                                other.message(getMessages().getChatMessage(guild.getColor() + guild.getName() + GRAY + " has requested to be truced."));
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You have requested to be truced with " + other.getColor()
                                        + other.getName() + GRAY + "."));
                                guild.message(getMessages().getChatMessage(GREEN + " has requested to be truced with " + other.getColor()
                                        + other.getName() + GRAY + "."), player);
                            }
                            return true;
                        case "unclaim":
                            if (args[1].toLowerCase().equals("all")) {
                                if (guild == null) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                    return true;
                                }

                                if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                    return true;
                                }

                                if (guild.getTerritory().size() == 0) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "Your guild doesn't have any claimed land."));
                                    return true;
                                }

                                guild.unclaimAll();

                                if (guild.getHome() != null) {
                                    guild.setHome(null);
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "Your guild's home has been unset due to you"
                                            + " unclaiming the land it was in." ));
                                    guild.message(getMessages().getChatMessage(GRAY + "Your guild's home has been unset due to " + GREEN
                                            + player.getName() + " unclaiming the land it was in."));
                                }

                                player.sendMessage(getMessages().getChatMessage(GRAY + "You unclaimed all of your guild's land."));
                                guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has unclaimed all of the guild's land."), player);
                                return true;
                            } else {
                                player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                                return true;
                            }
                        default:
                            player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                            return true;
                    }
                case 3:
                    switch (args[0].toLowerCase()) {
                        case "desc":
                        case "description":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            String desc = args[1] + " " + args[2];

                            guild.setDescription(desc);
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have set the guild's description to " + YELLOW + desc + GRAY + "."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has set the guild's description to "
                                    + YELLOW + desc + GRAY + "."), player);
                            return true;
                        case "rank":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            String name = args[1];
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

                            if (!GuildsPlugin.getUserRegistry().userExists(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatTag(PLAYER_NOT_FOUND));
                                return true;
                            }

                            UserModel target = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());

                            if (!guild.getMembers().contains(offlinePlayer.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That player is not in your guild."));
                                return true;
                            }

                            if (offlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not promote yourself."));
                                return true;
                            }

                            if (target.getRank() == user.getRank()) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not promote that player since they're the same rank as you."));
                                return true;
                            }

                            name = args[2];
                            GuildRank rank = null;
                            for (GuildRank forRank : GuildRank.values()) {
                                if (forRank.getName().toLowerCase().equals(name.toLowerCase())) {
                                    rank = forRank;
                                    break;
                                }
                            }

                            if (rank == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That rank does not exist"));
                            }

                            if (rank == GuildRank.LEADER && user.getRank() == GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "If you would like to make " + GREEN + offlinePlayer.getName()
                                        + GRAY + " the leader of the guild, run command " + AQUA + "/g " + DARK_AQUA + "leader " + offlinePlayer.getName()
                                        + GRAY + "."));
                                return true;
                            }

                            if ((rank == GuildRank.OFFICER && user.getRank() == GuildRank.OFFICER)
                                    || (rank == GuildRank.LEADER && user.getRank() == GuildRank.OFFICER)) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not able to give someone a rank above " + WHITE +
                                        GuildRank.MEMBER.getName() + GRAY + "."));
                                return true;
                            }

                            UserModel targetModel = GuildsPlugin.getUserRegistry().getUser(offlinePlayer.getUniqueId());

                            targetModel.setRank(rank);
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have set " + GREEN + offlinePlayer.getName() + "'s " + GRAY
                                    + "rank in the guild to " + WHITE + rank.getName() + GRAY + "."));
                            if (offlinePlayer.isOnline()) {
                                offlinePlayer.getPlayer().sendMessage(getMessages().getChatMessage(GRAY + "Your rank in the guild has been set to "
                                        + WHITE + rank.getName() + GRAY + "."));
                                guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has set " + GREEN + offlinePlayer.getName()
                                        + "'s " + GRAY + "rank in the guild to " + WHITE + rank.getName() + GRAY + "."), player, offlinePlayer.getPlayer());
                                return true;
                            }
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has set " + GREEN + offlinePlayer.getName()
                                    + "'s " + GRAY + "rank in the guild to " + WHITE + rank.getName() + GRAY + "."), player);
                            return true;
                        default:
                            player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                            return true;
                    }
                default:
                    switch (args[0].toLowerCase()) {
                        case "desc":
                        case "description":
                            if (guild == null) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must be in a guild before you can do this."));
                                return true;
                            }

                            if (user.getRank() != GuildRank.OFFICER && user.getRank() != GuildRank.LEADER) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You must at least be an officer of the guild to do this."));
                                return true;
                            }

                            StringBuilder descBuilder = new StringBuilder();
                            for (int i = 1; i < args.length; i++) {
                                descBuilder.append(args[i]).append(" ");
                            }

                            String desc = descBuilder.toString().trim();

                            guild.setDescription(desc);
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have set the guild's description to " + YELLOW + desc + GRAY + "."));
                            guild.message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has set the guild's description to "
                                    + YELLOW + desc + GRAY + "."), player);
                            return true;
                        default:
                            player.sendMessage(getMessages().getChatTag(INVALID_SYNTAX));
                            return true;
                    }
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
        return DARK_GRAY + "Guild Help Topic Page" + GRAY + ": " + DARK_GRAY + "[" + GOLD + ""
                + UNDERLINE + page + DARK_GRAY + "/" + GOLD + "" + UNDERLINE + total + DARK_GRAY + "]";
    }

    private int getTotalGuildPages() {
        return (int) Math.ceil((double) getGuildRegistry().getGuilds().size() / 10);
    }

    private void sendGuildPage(CommandSender sender, int page) {
        int displayPage = page;
        page = page - 1;

        List<GuildModel> guilds = Lists.newArrayList(getGuildRegistry().getGuilds());
        guilds.sort(Comparator.comparing(GuildModel::getSize).reversed());

        int start = 10 * page;
        int finish = Math.min(start + 10, guilds.size());

        String[] help = new String[]{
                getMessages().getMessage(CHAT_HEADER),
                getGuildList(displayPage, getTotalGuildPages()),
                getMessages().getMessage(CHAT_FOOTER)
        };
        sender.sendMessage(help);

        for (int i = start; i < finish; i++) {
            GuildModel guild = guilds.get(i);

            StringBuilder spacer = new StringBuilder();
            int spaces = 15 - guild.getName().length();
            for (int j = 0; j < spaces; j++)
                spacer.append(" ");

            int pos = i + 1;
            sender.sendMessage(GOLD + "" + pos + DARK_GRAY + ". " + guild.getColor() + guild.getName()
                    + spacer.toString() + DARK_GRAY + " - Members" + GRAY + ": " + GOLD + guild.getSize()
                    + DARK_GRAY + "/" + GOLD + 15);
        }

        sender.sendMessage(getMessages().getMessage(CHAT_FOOTER));
    }

    public String getGuildList(int page, int total) {
        return DARK_GRAY + "List" + GRAY + ": Guilds    " + DARK_GRAY + "-    Page" + GRAY + ": " + DARK_GRAY + "[" + GOLD + ""
                + UNDERLINE + page + DARK_GRAY + "/" + GOLD + "" + UNDERLINE + total + DARK_GRAY + "]";
    }

    private void sendGuildInfo(Player player, GuildModel guild) {
        int size = guild.getMembers().size();
        int power = 0;
        for (UUID uuid : guild.getMembers()) {
            UserModel temp = GuildsPlugin.getUserRegistry().getUser(uuid);
            power += temp.getPower();
        }
        int maxpower = size * 10;

        List<GuildModel> allies = Lists.newArrayList();
        List<GuildModel> enemies = Lists.newArrayList();
        for (UUID uuid : guild.getRelations().keySet()) {
            if (guild.getRelation(uuid) == GuildRelation.ALLY)
                allies.add(getGuildRegistry().getGuild(uuid));
            else if (guild.getRelation(uuid) == GuildRelation.ENEMY)
                enemies.add(getGuildRegistry().getGuild(uuid));
        }

        StringBuilder allyBuilder = new StringBuilder();
        for (int i = 0; i < allies.size(); i++) {
            GuildModel g = allies.get(i);
            allyBuilder.append(BLUE).append(g.getName());

            if (i < allies.size() - 1) {
                allyBuilder.append(DARK_GRAY).append(", ");
            }
        }

        if (allies.isEmpty()) {
            allyBuilder.append(RED).append("None");
        }

        StringBuilder enemyBuilder = new StringBuilder();
        for (int i = 0; i < enemies.size(); i++) {
            GuildModel g = enemies.get(i);
            enemyBuilder.append(RED).append(g.getName());

            if (i < allies.size() - 1) {
                enemyBuilder.append(DARK_GRAY).append(", ");
            }
        }

        if (enemies.isEmpty()) {
            enemyBuilder.append(RED).append("None");
        }

        StringBuilder memberBuilder = new StringBuilder();

        for (int i = 0; i < guild.getMembers().size(); i++) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(guild.getMembers().get(i));
            UserModel temp = GuildsPlugin.getUserRegistry().getUser(guild.getMembers().get(i));

            if (offlinePlayer.isOnline())
                memberBuilder.append(GREEN);
            else
                memberBuilder.append(RED);

            switch (temp.getRank()) {
                case LEADER:
                    memberBuilder.append("** ");
                    break;
                case OFFICER:
                    memberBuilder.append("* ");
                    break;
            }

            memberBuilder.append(offlinePlayer.getName());

            if (i < guild.getMembers().size() - 1) {
                memberBuilder.append(DARK_GRAY).append(", ");
            }
        }

        String[] info = new String[]{
                getMessages().getMessage(CHAT_HEADER),
                DARK_GRAY + "Info" + GRAY + ": " + guild.getName(),
                getMessages().getMessage(CHAT_FOOTER),
                DARK_GRAY + "Name" + GRAY + ": " + guild.getName(),
                DARK_GRAY + "Description" + GRAY + ": " + GREEN + guild.getDescription(),
                DARK_GRAY + "Color" + GRAY + ": " + guild.getColor() + guild.getColor().name(),
                DARK_GRAY + "Size" + GRAY + ": " + GOLD + guild.getMembers().size() +
                        DARK_GRAY + "/" + GOLD + 15,
                DARK_GRAY + "Power" + GRAY + ": " + GOLD + power + DARK_GRAY +
                        "/" + GOLD + maxpower,
                DARK_GRAY + "Land Power" + GRAY + ": " + GOLD +
                        guild.getTerritory().size() + DARK_GRAY + "/" + GOLD +
                        guild.getMembers().size() * 10,
                DARK_GRAY + "Allies" + GRAY + ": " + BLUE + "" + allyBuilder.toString().trim(),
                DARK_GRAY + "Enemies" + GRAY + ": " + RED + "" + enemyBuilder.toString().trim(),
                DARK_GRAY + "Members" + GRAY + ": " + RED + "" + memberBuilder.toString().trim(),
                getMessages().getMessage(CHAT_FOOTER),
        };
        player.sendMessage(info);
    }

    public static void sendGuildMap(Player player) {
        StringBuilder mapBuilder = new StringBuilder();
        List<GuildModel> guilds = Lists.newArrayList();

        Chunk chunk = player.getLocation().getChunk();
        int line = 0;
        for (int z = chunk.getZ() - 3; z < chunk.getZ() + 4; z++) {
            switch (line) {
                case 2:
                    mapBuilder.append(translateAlternateColorCodes('&', "&8___&7\\&6N&7/&8___"));
                    break;
                case 3:
                    mapBuilder.append(translateAlternateColorCodes('&', "&8___&6W&7+&6E&8___"));
                    break;
                case 4:
                    mapBuilder.append(translateAlternateColorCodes('&', "&8___&7/&6S&7\\&8___"));
                    break;
                default:
                    mapBuilder.append(DARK_GRAY).append("_________");
                    break;
            }


            for (int x = chunk.getX() - 15; x < chunk.getX() + 16; x++) {
                String symbol = GRAY + "-";
                Chunk chunkAt = player.getWorld().getChunkAt(x, z);

                GuildModel guild = getGuildRegistry().claimedBy(chunkAt);

                if (guild != null) {
                    if (!guilds.contains(guild))
                        guilds.add(guild);

                    symbol = guild.getColor() + guild.getName().substring(0, 1).toUpperCase();
                }

                if (chunk == chunkAt)
                    symbol = WHITE + "*";

                mapBuilder.append(symbol);
            }

            mapBuilder.append(DARK_GRAY).append("__________\n");
            line++;
        }

        player.sendMessage(getMessages().getMessage(CHAT_HEADER));
        player.sendMessage(DARK_GRAY + "Map" + GRAY + ": Guilds");
        player.sendMessage(getMessages().getMessage(CHAT_FOOTER));
        player.sendMessage(mapBuilder.toString().trim());
        player.sendMessage(getMessages().getMessage(CHAT_FOOTER));

        List<String> shown = Lists.newArrayList();
        StringBuilder guildBuilder = new StringBuilder();
        guildBuilder.append(WHITE + "").append(BOLD).append("*").append(DARK_GRAY).append(" - ").append(WHITE).append("You").append(DARK_GRAY).append(", ");
        if (!guilds.isEmpty()) {
            for (GuildModel guildModel : guilds) {
                if (!shown.contains(guildModel.getKey())) {
                    guildBuilder.append(guildModel.getColor())
                            .append(guildModel.getName().substring(0, 1).toUpperCase()).append(DARK_GRAY)
                            .append(" - ").append(guildModel.getColor()).append(guildModel.getName())
                            .append(DARK_GRAY).append(", ");
                    shown.add(guildModel.getKey());
                }
            }
        }
        player.sendMessage(DARK_GRAY + "Keys" + GRAY + ": " +
                guildBuilder.toString().substring(0, guildBuilder.length() - 2));
        player.sendMessage(getMessages().getMessage(CHAT_FOOTER));
    }
}