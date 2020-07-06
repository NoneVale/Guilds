package net.nighthawkempires.guilds.scoreboard;

import net.nighthawkempires.core.CorePlugin;
import net.nighthawkempires.core.lang.Messages;
import net.nighthawkempires.core.scoreboard.NEScoreboard;
import net.nighthawkempires.core.settings.ConfigModel;
import net.nighthawkempires.guilds.GuildsPlugin;
import net.nighthawkempires.guilds.user.UserModel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import static org.bukkit.ChatColor.*;

public class GuildsScoreboard extends NEScoreboard {

    private int taskId;

    @Override
    public int getPriority() {
        return 3;
    }

    public String getName() {
        return "guilds";
    }

    public int getTaskId() {
        return this.taskId;
    }

    public Scoreboard getFor(Player player) {
        UserModel userModel = GuildsPlugin.getUserRegistry().getUser(player.getUniqueId());
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("test", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(CorePlugin.getMessages().getMessage(Messages.SCOREBOARD_HEADER).replaceAll("%SERVER%",
                CorePlugin.getMessages().getServerTag(getConfig().getServerType())));
        Team top = scoreboard.registerNewTeam("top");
        top.addEntry(GRAY + " ➛  " + BLUE + "" + BOLD);
        top.setPrefix("");
        top.setSuffix("");
        Team middle = scoreboard.registerNewTeam("middle");
        middle.addEntry(GRAY + " ➛  " + GRAY + "" + BOLD);
        middle.setPrefix("");
        middle.setSuffix("");
        Team bottom = scoreboard.registerNewTeam("bottom");
        bottom.addEntry(GRAY + " ➛  " + GOLD + "" + BOLD);
        bottom.setPrefix("");
        bottom.setSuffix("");

        objective.getScore(DARK_GRAY + "" + STRIKETHROUGH + "" + BOLD + "--------------")
                .setScore(10);
        objective.getScore(GRAY + "" + BOLD + " Guild" + GRAY + ": ").setScore(9);
        objective.getScore(GRAY + " ➛  " + BLUE + "" + BOLD).setScore(8);
        //op.setSuffix(player.getName());
        if (userModel.getGuild() != null)
            top.setSuffix(userModel.getGuild().getColor() + userModel.getGuild().getName());
        else
            top.setSuffix(GRAY + "None");
        objective.getScore(DARK_PURPLE + " ").setScore(7);
        objective.getScore(GRAY + "" + BOLD + " Rank" + GRAY + ": ").setScore(6);
        objective.getScore(GRAY + " ➛  " + GRAY + "" + BOLD).setScore(5);
        if (userModel.getGuild() != null)
            middle.setSuffix(userModel.getRank().getName());
        else
            middle.setSuffix(GRAY + "None");
        objective.getScore(YELLOW + "  ").setScore(4);
        objective.getScore(GRAY + "" + BOLD + " Power" + GRAY + ": ").setScore(3);
        objective.getScore(GRAY + " ➛  " + GOLD + "" + BOLD).setScore(2);
        bottom.setSuffix(String.valueOf(userModel.getPower()));
        objective.getScore(DARK_GRAY + "" + STRIKETHROUGH + "" + BOLD + "--------------")
                .setScore(1);

        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(CorePlugin.getPlugin(), () -> {
            if (userModel.getGuild() != null)
                top.setSuffix(userModel.getGuild().getColor() + userModel.getGuild().getName());
            else
                top.setSuffix(GRAY + "None");
            if (userModel.getGuild() != null)
                middle.setSuffix(userModel.getRank().getName());
            else
                middle.setSuffix(GRAY + "None");
            bottom.setSuffix(String.valueOf(userModel.getPower()));
        }, 0 , 5);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.getPlugin(), () -> {
            Bukkit.getScheduler().cancelTask(getTaskId());
        }, 295);
        return scoreboard;
    }

    private ConfigModel getConfig() {
        return CorePlugin.getConfigg();
    }
}
