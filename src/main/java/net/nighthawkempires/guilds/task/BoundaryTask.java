package net.nighthawkempires.guilds.task;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.nighthawkempires.guilds.GuildsPlugin.*;

public class BoundaryTask implements Runnable {

    public void run() {
        for (UUID uuid : getPlayerData().boundaryList) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (!offlinePlayer.isOnline()) {
                getPlayerData().boundaryList.remove(uuid);
                return;
            }

            Player player = offlinePlayer.getPlayer();
            showBoundaries(player);
        }
    }

    private void showBoundaries(Player player) {
        Chunk chunk = player.getLocation().getChunk();

        for (int y = player.getLocation().getBlockY() - 3; y < player.getLocation().getBlockY() + 8; y++) {
            for (int i = 0; i < 16; i++) {
                Block block1 = chunk.getBlock(i, y, 0);
                Block block2 = chunk.getBlock(15, y, i);
                Block block3 = chunk.getBlock(15 - i, y, 15);
                Block block4 = chunk.getBlock(0, y, 15 - i);

                if (block1.getType().isAir())
                    player.spawnParticle(Particle.DRIP_WATER, block1.getLocation(), 2);
                if (block2.getType().isAir())
                    player.spawnParticle(Particle.DRIP_WATER, block2.getLocation(), 2);
                if (block3.getType().isAir())
                    player.spawnParticle(Particle.DRIP_WATER, block3.getLocation(), 2);
                if (block4.getType().isAir())
                    player.spawnParticle(Particle.DRIP_WATER, block4.getLocation(), 2);
            }
        }
    }
}