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
            for (int i = 0; i <= 16; i++) {
                if (i == 16) {
                    Block block1 = chunk.getBlock(i - 1, y, 0);
                    Block block1f = player.getWorld().getBlockAt(block1.getX() + 1, block1.getY(), block1.getZ());
                    Block block2 = chunk.getBlock(15, y, i - 1);
                    Block block2f = player.getWorld().getBlockAt(block2.getX() + 1, block2.getY(), block2.getZ() + 1);
                    Block block3 = chunk.getBlock(15 - (i - 1), y, 15);
                    Block block3f = player.getWorld().getBlockAt(block3.getX() + 1, block3.getY(), block3.getZ() + 1);
                    Block block4 = chunk.getBlock(0, y, 15 - (i - 1));
                    Block block4f = player.getWorld().getBlockAt(block4.getX(), block4.getY(), block4.getZ() + 1);

                    if (block1.getType().isAir())
                        player.spawnParticle(Particle.DRIP_WATER, block1.getLocation(), 2);
                    if (block2f.getType().isAir())
                        player.spawnParticle(Particle.DRIP_WATER, block2f.getLocation(), 2);
                    if (block3f.getType().isAir())
                        player.spawnParticle(Particle.DRIP_WATER, block3f.getLocation(), 2);
                    if (block4.getType().isAir())
                        player.spawnParticle(Particle.DRIP_WATER, block4.getLocation(), 2);
                } else {
                    Block block1 = chunk.getBlock(i, y, 0);
                    Block block2 = chunk.getBlock(15, y, i);
                    Block block2f = player.getWorld().getBlockAt(block2.getX() + 1, block2.getY(), block2.getZ());
                    Block block3 = chunk.getBlock(15 - i, y, 15);
                    Block block3f = player.getWorld().getBlockAt(block3.getX(), block3.getY(), block3.getZ() + 1);
                    Block block4 = chunk.getBlock(0, y, 15 - i);

                    if (block1.getType().isAir())
                        player.spawnParticle(Particle.DRIP_WATER, block1.getLocation(), 2);
                    if (block2f.getType().isAir())
                        player.spawnParticle(Particle.DRIP_WATER, block2f.getLocation(), 2);
                    if (block3f.getType().isAir())
                        player.spawnParticle(Particle.DRIP_WATER, block3f.getLocation(), 2);
                    if (block4.getType().isAir())
                        player.spawnParticle(Particle.DRIP_WATER, block4.getLocation(), 2);
                }
            }
        }
    }
}