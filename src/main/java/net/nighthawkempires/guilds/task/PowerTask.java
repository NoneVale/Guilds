package net.nighthawkempires.guilds.task;

import net.nighthawkempires.guilds.GuildsPlugin;
import net.nighthawkempires.guilds.user.UserModel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PowerTask implements Runnable {

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UserModel userModel = GuildsPlugin.getUserRegistry().getUser(player.getUniqueId());

            if (userModel.getPower() < 10)
                userModel.setPower(userModel.getPower() + 1);
        }
    }
}
