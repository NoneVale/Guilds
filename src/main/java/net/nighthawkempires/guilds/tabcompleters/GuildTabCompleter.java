package net.nighthawkempires.guilds.tabcompleters;

import com.google.common.collect.Lists;
import net.nighthawkempires.guilds.GuildsPlugin;
import net.nighthawkempires.guilds.guild.GuildRank;
import net.nighthawkempires.guilds.user.UserModel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Collections;
import java.util.List;

import static net.nighthawkempires.core.CorePlugin.getMessages;

public class GuildTabCompleter implements TabCompleter {

    List<String> commandList = Lists.newArrayList(
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

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = Lists.newArrayList();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UserModel userModel = GuildsPlugin.getUserRegistry().getUser(player.getUniqueId());

            if (!player.hasPermission("ne.guilds")) {
                return completions;
            }

            switch (args.length) {
                case 1:
                    List<String> arggs = Lists.newArrayList("help", "info", "show", "list", "map", "showchunks");
                    if (userModel.getGuild() == null) {
                        arggs.addAll(Lists.newArrayList("create", "join"));
                    } else if (userModel.getGuild() != null) {
                        arggs.addAll(Lists.newArrayList("leave", "home"));

                        GuildRank rank = userModel.getRank();
                        if (rank == GuildRank.LEADER || rank == GuildRank.OFFICER) {
                            arggs.addAll(Lists.newArrayList("claim", "invite", "kick", "sethome", "ally", "truce", "neutral",
                                    "enemy", "unclaim", "desc", "promote", "demote", "color", "rank"));

                            if (rank == GuildRank.LEADER) {
                                arggs.addAll(Lists.newArrayList("name", "disband", "leader"));
                            }
                        }
                    }
                    StringUtil.copyPartialMatches(args[0], arggs, completions);
                    Collections.sort(completions);
                    return completions;
                case 2:
                    switch (args[0]) {
                        case "help":
                            int pages = (int) Math.ceil((double) commandList.size() / 10);
                            arggs = Lists.newArrayList();
                            for (int i = 0; i < pages; i++) {
                                arggs.add((i + 1) + "");
                            }
                            StringUtil.copyPartialMatches(args[1], arggs, completions);
                            Collections.sort(completions);
                            return completions;
                        case "list":
                            pages = (int) Math.ceil((double) GuildsPlugin.getGuildRegistry().getGuilds().size() / 10);
                            arggs = Lists.newArrayList();
                            for (int i = 0; i < pages; i++) {
                                arggs.add((i + 1) + "");
                            }
                            StringUtil.copyPartialMatches(args[1], arggs, completions);
                            Collections.sort(completions);
                            return completions;
                        case "info":
                            StringUtil.copyPartialMatches(args[1], GuildsPlugin.getGuildRegistry().getGuildNames(), completions);
                            Collections.sort(completions);
                            return completions;
                        case "show":
                            arggs = Lists.newArrayList();
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                arggs.add(players.getName());
                            }
                            StringUtil.copyPartialMatches(args[1], arggs, completions);
                            Collections.sort(completions);
                            return completions;
                        case "join":
                            if (userModel.getGuild() == null) {
                                StringUtil.copyPartialMatches(args[1], GuildsPlugin.getGuildRegistry().getGuildNames(), completions);
                                Collections.sort(completions);
                                return completions;
                            } else {
                                return completions;
                            }
                        case "ally":
                        case "truce":
                        case "neutral":
                        case "enemy":
                            arggs = Lists.newArrayList();
                            if (userModel.getGuild() != null) {
                                GuildRank rank = userModel.getRank();
                                if (rank == GuildRank.LEADER || rank == GuildRank.OFFICER) {
                                    StringUtil.copyPartialMatches(args[1], GuildsPlugin.getGuildRegistry().getGuildNames(), completions);
                                    Collections.sort(completions);
                                    return completions;
                                }
                            }
                            return arggs;
                        case "invite":
                            arggs = Lists.newArrayList();
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                if (!userModel.getGuild().isMember(players.getUniqueId())) {
                                    arggs.add(players.getName());
                                }
                            }
                            StringUtil.copyPartialMatches(args[1], arggs, completions);
                            Collections.sort(completions);
                            return completions;
                        case "kick":
                        case "promote":
                        case "demote":
                        case "rank":
                            arggs = Lists.newArrayList();
                            if (userModel.getGuild() != null) {
                                GuildRank rank = userModel.getRank();
                                if (rank == GuildRank.LEADER || rank == GuildRank.OFFICER) {
                                    for (Player players : Bukkit.getOnlinePlayers()) {
                                        if (userModel.getGuild().isMember(players.getUniqueId())) {
                                            arggs.add(players.getName());
                                        }
                                    }
                                    StringUtil.copyPartialMatches(args[1], arggs, completions);
                                    Collections.sort(completions);
                                    return completions;
                                }
                            }
                            return completions;
                        case "leader":
                            arggs = Lists.newArrayList();
                            if (userModel.getGuild() != null) {
                                GuildRank rank = userModel.getRank();
                                if (rank == GuildRank.LEADER) {
                                    for (Player players : Bukkit.getOnlinePlayers()) {
                                        if (userModel.getGuild().isMember(players.getUniqueId())) {
                                            arggs.add(players.getName());
                                        }
                                    }
                                    StringUtil.copyPartialMatches(args[1], arggs, completions);
                                    Collections.sort(completions);
                                    return completions;
                                }
                            }
                            return completions;
                        case "unclaim":
                            arggs = Lists.newArrayList();
                            if (userModel.getGuild() != null) {
                                GuildRank rank = userModel.getRank();
                                if (rank == GuildRank.LEADER || rank == GuildRank.OFFICER) {
                                    arggs.add("all");
                                    StringUtil.copyPartialMatches(args[1], GuildsPlugin.getGuildRegistry().getGuildNames(), completions);
                                    Collections.sort(completions);
                                    return completions;
                                }
                            }
                            return completions;
                    }
                case 3:
                    switch (args[0]) {
                        case "rank":
                            arggs = Lists.newArrayList();
                            if (userModel.getGuild() != null) {
                                GuildRank rank = userModel.getRank();
                                if (rank == GuildRank.LEADER || rank == GuildRank.OFFICER) {
                                    arggs.addAll(Lists.newArrayList(GuildRank.RECRUIT.name(), GuildRank.MEMBER.name()));
                                    if (rank == GuildRank.LEADER) {
                                        arggs.add(GuildRank.OFFICER.name());
                                    }
                                    StringUtil.copyPartialMatches(args[2], GuildsPlugin.getGuildRegistry().getGuildNames(), completions);
                                    Collections.sort(completions);
                                    return completions;
                                }
                            }
                            return completions;
                        default:
                            return completions;
                    }
                default:
                    return completions;
            }
        }
        return completions;
    }
}
