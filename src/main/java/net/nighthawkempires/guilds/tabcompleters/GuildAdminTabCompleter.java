package net.nighthawkempires.guilds.tabcompleters;

import com.google.common.collect.Lists;
import net.nighthawkempires.guilds.GuildsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Collections;
import java.util.List;

import static net.nighthawkempires.core.CorePlugin.getMessages;

public class GuildAdminTabCompleter implements TabCompleter {

    private List<String> commandList = Lists.newArrayList(
            getMessages().getCommand("gadmin", "bypass", "Toggle admin bypass"),
            getMessages().getCommand("gadmin", "help [page]", "Show this help menu"),
            getMessages().getCommand("gadmin", "join <guild>", "Join a guild"),
            getMessages().getCommand("gadmin", "name <guild> <name>", "Set a guild's name"),
            getMessages().getCommand("gadmin", "disband <guild>", "Disband a guild"),
            getMessages().getCommand("gadmin", "home <guild>", "Teleport to a guild's home"),
            getMessages().getCommand("gadmin", "unclaim [guild] [all]", "Unclaim a guild's territory"),
            getMessages().getCommand("gadmin", "leader <guild> <player>", "Set the leader of a guild")
    );

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = Lists.newArrayList();
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if  (!player.hasPermission("ne.guilds.admin") && !player.hasPermission("ne.admin")) {
                return completions;
            }

            switch (args.length) {
                case 1:
                    List<String> arggs = Lists.newArrayList("bypass", "help", "join", "name", "disband", "home", "unclaim", "leader");
                    StringUtil.copyPartialMatches(args[0], arggs, completions);
                    Collections.sort(completions);
                    return completions;
                case 2:
                    switch (args[0]) {
                        case "join":
                        case "name":
                        case "disband":
                        case "home":
                        case "unclaim":
                        case "leader":
                            StringUtil.copyPartialMatches(args[1], GuildsPlugin.getGuildRegistry().getGuildNames(), completions);
                            Collections.sort(completions);
                            return completions;
                        case "help":
                            int pages = (int) Math.ceil((double) commandList.size() / 10);
                            arggs = Lists.newArrayList();
                            for (int i = 0; i < pages; i++) {
                                arggs.add((i + 1) + "");
                            }
                            StringUtil.copyPartialMatches(args[1], arggs, completions);
                            Collections.sort(completions);
                            return completions;
                        default:
                            return completions;
                    }
                case 3:
                    switch (args[0]) {
                        case "unclaim":
                            arggs = Lists.newArrayList("all");
                            StringUtil.copyPartialMatches(args[2], arggs, completions);
                            Collections.sort(completions);
                            return completions;
                        case "leader":
                            arggs = Lists.newArrayList();
                            for (Player players : Bukkit.getOnlinePlayers()) {
                                arggs.add(players.getName());
                            }
                            StringUtil.copyPartialMatches(args[2], arggs, completions);
                            Collections.sort(completions);
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