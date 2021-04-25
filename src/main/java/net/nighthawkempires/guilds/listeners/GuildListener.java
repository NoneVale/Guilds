package net.nighthawkempires.guilds.listeners;

import net.nighthawkempires.core.util.EntityUtil;
import net.nighthawkempires.core.util.ItemUtil;
import net.nighthawkempires.guilds.GuildsPlugin;
import net.nighthawkempires.guilds.guild.GuildModel;
import net.nighthawkempires.guilds.user.UserModel;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

import static net.nighthawkempires.guilds.GuildsPlugin.*;
import static net.nighthawkempires.guilds.GuildsPlugin.getUserRegistry;
import static net.nighthawkempires.core.CorePlugin.*;
import static org.bukkit.ChatColor.*;

public class GuildListener implements Listener {

    @EventHandler
    public void onInterat(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UserModel userModel = getUserRegistry().getUser(player.getUniqueId());

        GuildModel claimedGuild = getGuildRegistry().claimedBy(player.getLocation().getChunk());

        if (claimedGuild != null) {
            if (event.getAction() == Action.PHYSICAL) {
                if (event.getClickedBlock().getType() == Material.FARMLAND) {
                    event.setCancelled(true);
                }
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!getPlayerData().bypassList.contains(player.getUniqueId())) {
                    if (userModel.getGuild() != null) {
                        if (userModel.getGuild() != claimedGuild) {
                            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                                if (player.getInventory().getItemInMainHand().getType() == Material.ARMOR_STAND
                                        || player.getInventory().getItemInOffHand().getType() == Material.ARMOR_STAND
                                        || player.getInventory().getItemInMainHand().getType() == Material.ITEM_FRAME
                                        || player.getInventory().getItemInOffHand().getType() == Material.ITEM_FRAME) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to place this inside of "
                                            + claimedGuild.getColor() + claimedGuild.getName() + "'s " + GRAY + "territory."));
                                    event.setCancelled(true);
                                }
                            }

                            if (ItemUtil.isInteractable(event.getClickedBlock().getType(), Material.ENDER_CHEST, Material.TRAPPED_CHEST)) {
                                if (!claimedGuild.isAllied(userModel.getGuild().getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to interact inside of "
                                            + claimedGuild.getColor() + claimedGuild.getName() + "'s " + GRAY + "territory."));
                                    event.setCancelled(true);
                                }
                            }
                        }
                    } else {
                        if (ItemUtil.isInteractable(event.getClickedBlock().getType(), Material.ENDER_CHEST, Material.TRAPPED_CHEST)) {
                            if (!claimedGuild.isAllied(userModel.getGuild().getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to interact inside of "
                                        + claimedGuild.getColor() + claimedGuild.getName() + "'s " + GRAY + "territory."));
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        UserModel userModel = getUserRegistry().getUser(player.getUniqueId());

        GuildModel claimedGuild = getGuildRegistry().claimedBy(player.getLocation().getChunk());

        if (claimedGuild != null) {
            if (!getPlayerData().bypassList.contains(player.getUniqueId())) {
                if (userModel.getGuild() != null) {
                    if (userModel.getGuild() != claimedGuild) {
                        if (EntityUtil.isInteractable(event.getRightClicked())) {
                            if (!claimedGuild.isAllied(userModel.getGuild().getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to interact inside of "
                                        + claimedGuild.getColor() + claimedGuild.getName() + "'s " + GRAY + "territory."));
                                event.setCancelled(true);
                            }
                        }
                    }
                } else {
                    if (EntityUtil.isInteractable(event.getRightClicked())) {
                        if (!claimedGuild.isAllied(userModel.getGuild().getUniqueId())) {
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to interact inside of "
                                    + claimedGuild.getColor() + claimedGuild.getName() + "'s " + GRAY + "territory."));
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            UserModel userModel = getUserRegistry().getUser(player.getUniqueId());
            GuildModel userGuild = userModel.getGuild();

            if (EntityUtil.isInteractable(event.getEntity())) {
                GuildModel claimedBy = getGuildRegistry().claimedBy(player.getLocation().getChunk());
                if (claimedBy != null) {
                    if (userModel.getGuild() != null) {
                        if (userModel.getGuild() != claimedBy) {
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to break that in "
                                    + claimedBy.getColor() + claimedBy.getName() + "'s " + GRAY + "territory."));
                            event.setCancelled(true);
                        }
                    } else {
                        player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to break that in "
                                + claimedBy.getColor() + claimedBy.getName() + "'s " + GRAY + "territory."));
                        event.setCancelled(true);
                    }
                }
            } else {
                if (event.getEntity() instanceof Player) {
                    Player attacked = (Player) event.getEntity();
                    UserModel attackedModel = getUserRegistry().getUser(attacked.getUniqueId());
                    GuildModel attackedGuild = attackedModel.getGuild();

                    if (attackedGuild != null) {
                        if (userGuild != null) {
                            if (attackedGuild.getKey().equals(userGuild.getKey())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt other members of your guild."));
                                event.setCancelled(true);
                            } else if (attackedGuild.isAllied(userGuild.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt players that you're allied with."));
                                event.setCancelled(true);
                            } else if (attackedGuild.isTruced(userGuild.getUniqueId())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt players that you're truced with."));
                                event.setCancelled(true);
                            } else if (attackedGuild.isNeutral(userGuild.getUniqueId())) {
                                if (attackedGuild.getTerritory().contains(attacked.getLocation().getChunk())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt players in their own territory while" +
                                            " neutral with them."));
                                    event.setCancelled(true);
                                }
                            }
                        } else {
                            if (attackedGuild.getTerritory().contains(attacked.getLocation().getChunk())) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt " + GREEN + attacked.getName() +
                                        GRAY + " in their territory."));
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        } else if (event.getDamager() instanceof Projectile) {
            if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                Player player = (Player) ((Projectile) event.getDamager()).getShooter();
                UserModel userModel = getUserRegistry().getUser(player.getUniqueId());
                GuildModel userGuild = userModel.getGuild();

                if (EntityUtil.isInteractable(event.getEntity())) {
                    GuildModel claimedBy = getGuildRegistry().claimedBy(player.getLocation().getChunk());
                    if (claimedBy != null) {
                        if (userModel.getGuild() != null) {
                            if (userModel.getGuild() != claimedBy) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to break that in "
                                        + claimedBy.getColor() + claimedBy.getName() + "'s " + GRAY + "territory."));
                                event.setCancelled(true);
                            }
                        } else {
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to break that in "
                                    + claimedBy.getColor() + claimedBy.getName() + "'s " + GRAY + "territory."));
                            event.setCancelled(true);
                        }
                    }
                } else {
                    if (event.getEntity() instanceof Player) {
                        Player attacked = (Player) event.getEntity();
                        UserModel attackedModel = getUserRegistry().getUser(attacked.getUniqueId());
                        GuildModel attackedGuild = attackedModel.getGuild();

                        if (attackedGuild != null) {
                            if (userGuild != null) {
                                if (attackedGuild.getKey().equals(userGuild.getKey())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt other members of your guild."));
                                    event.setCancelled(true);
                                } else if (attackedGuild.isAllied(userGuild.getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt players that you're allied with."));
                                    event.setCancelled(true);
                                } else if (attackedGuild.isTruced(userGuild.getUniqueId())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt players that you're truced with."));
                                    event.setCancelled(true);
                                } else if (attackedGuild.isNeutral(userGuild.getUniqueId())) {
                                    if (attackedGuild.getTerritory().contains(attacked.getLocation().getChunk())) {
                                        player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt players in their own territory while" +
                                                " neutral with them."));
                                        event.setCancelled(true);
                                    }
                                }
                            } else {
                                if (attackedGuild.getTerritory().contains(attacked.getLocation().getChunk())) {
                                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt " + GREEN + attacked.getName() +
                                            GRAY + " in their territory."));
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBuild(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        GuildModel guildModel = getGuildRegistry().claimedBy(player.getLocation().getChunk());
        if (guildModel != null) {
            if (!getPlayerData().bypassList.contains(player.getUniqueId())) {
                UserModel userModel = getUserRegistry().getUser(player.getUniqueId());
                if (userModel.getGuild() != null) {
                    if (userModel.getGuild() != guildModel) {
                        player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to build inside of " + guildModel.getColor()
                                + "'s " + GRAY + " territory."));
                        event.setCancelled(true);
                    }
                } else {
                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to break blocks inside of " + guildModel.getColor()
                            + "'s " + GRAY + " territory."));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        GuildModel guildModel = getGuildRegistry().claimedBy(player.getLocation().getChunk());
        if (guildModel != null) {
            if (!getPlayerData().bypassList.contains(player.getUniqueId())) {
                UserModel userModel = getUserRegistry().getUser(player.getUniqueId());
                if (userModel.getGuild() != null) {
                    if (userModel.getGuild() != guildModel) {
                        player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to break blocks inside of " + guildModel.getColor()
                                + "'s " + GRAY + " territory."));
                        event.setCancelled(true);
                    }
                } else {
                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to break blocks inside of " + guildModel.getColor()
                            + "'s " + GRAY + " territory."));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        UserModel userModel = getUserRegistry().getUser(player.getUniqueId());

        GuildModel guildModel = getGuildRegistry().claimedBy(event.getRightClicked().getLocation().getChunk());
        if (guildModel != null) {
            if (!getPlayerData().bypassList.contains(player.getUniqueId())) {
                if (userModel.getGuild() != null) {
                    if (userModel.getGuild() != guildModel) {
                        // Maybe allow allies to take armor off? idk
                        player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to interact inside of " + guildModel.getColor()
                                + "'s " + GRAY + " territory."));
                        event.setCancelled(true);
                    }
                } else {
                    player.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to interact inside of " + guildModel.getColor()
                            + "'s " + GRAY + " territory."));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UserModel user = getUserRegistry().getUser(player.getUniqueId());

        GuildModel claimedBy = getGuildRegistry().claimedBy(player.getLocation().getChunk());
        if (claimedBy != null) {
            if (user.getGuild() != null) {
                if (claimedBy.isEnemy(user.getGuild().getUniqueId())) {
                    String[] commandParts = event.getMessage().split(" ");

                    String command = commandParts[0].replaceFirst("/", "").toLowerCase();

                    if (!getPlayerData().bypassList.contains(player.getUniqueId())) {
                        if (getConfigg().getWarmupCommands().contains(command)) {
                            if (getConfigg().getRequiredArgLength(command) <= (commandParts.length - 1)) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not teleport while in enemy territory."));
                                event.setCancelled(true);
                                return;
                            }
                        } else if (command.toLowerCase().equals("g") && commandParts.length > 1) {
                            if (commandParts[1].toLowerCase().equals("home") || commandParts[1].toLowerCase().equals("sethome")) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You can not teleport while in enemy territory."));
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }

        String commandMessage = event.getMessage().replaceFirst("/", "");
        String[] commandParts = event.getMessage().split(" ");

        String command = commandParts[0].replaceFirst("/", "").toLowerCase();

        if (!player.hasPermission("ne.cooldown.bypass")) {
            if (command.toLowerCase().equals("g") && commandParts.length > 1) {
                if (commandParts[1].toLowerCase().equals("home") || commandParts[1].toLowerCase().equals("sethome")) {
                    HashMap<UUID, String> warmingUp = getPlayerData().warmingUp;

                    if (warmingUp.containsKey(player.getUniqueId())) {
                        player.sendMessage(getMessages().getChatMessage(GRAY + "You are already warming a command up!"));
                        event.setCancelled(true);
                    } else {
                        warmingUp.put(player.getUniqueId(), commandMessage);
                        player.sendMessage(getMessages().getChatMessage(GRAY + "You must wait " + GOLD + 5 + " seconds" + GRAY + " for command "
                                + DARK_AQUA + "/" + command + " " + commandParts[1] + GRAY + " to warm up."));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(GuildsPlugin.getPlugin(), () -> {
                            if (warmingUp.containsKey(player.getUniqueId())) {
                                if (warmingUp.get(player.getUniqueId()).equalsIgnoreCase(commandMessage)) {
                                    warmingUp.remove(player.getUniqueId());
                                    Bukkit.dispatchCommand(player, commandMessage);
                                    System.out.println(commandMessage);
                                }
                            }
                        }, 120);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    public boolean canBuild(Chunk chunk, Player builder) {
        GuildModel guildModel = getGuildRegistry().claimedBy(chunk);
        if (guildModel != null) {
            if (!getPlayerData().bypassList.contains(builder.getUniqueId())) {
                UserModel userModel = getUserRegistry().getUser(builder.getUniqueId());
                if (userModel.getGuild() != null) {
                    if (userModel.getGuild() != guildModel) {
                        builder.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to build inside of " + guildModel.getColor()
                                + "'s " + GRAY + " territory."));
                        return false;
                    }
                } else return false;
            }
        }
        return true;
    }

    public boolean canDamage(Player attacked, Player attacker) {
        UserModel attackedModel = getUserRegistry().getUser(attacked.getUniqueId());
        UserModel attackerModel = getUserRegistry().getUser(attacker.getUniqueId());

        GuildModel attackedGuild = attackedModel.getGuild();
        GuildModel attackerGuild = attackerModel.getGuild();

        if (attackedGuild != null) {
            if (attackerGuild != null) {
                if (attackedGuild.getKey().equals(attackerGuild.getKey())) {
                    attacker.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt other members of your guild."));
                    return false;
                } else if (attackedGuild.isAllied(attackerGuild.getUniqueId())) {
                    attacker.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt players that you're allied with."));
                    return false;
                } else if (attackedGuild.isTruced(attackerGuild.getUniqueId())) {
                    attacker.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt players that you're truced with."));
                    return false;
                } else if (attackedGuild.isNeutral(attackerGuild.getUniqueId())) {
                    if (attackedGuild.getTerritory().contains(attacked.getLocation().getChunk())) {
                        attacker.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt players in their own territory while" +
                                " neutral with them."));
                        return false;
                    }
                }
            } else {
                if (attackedGuild.getTerritory().contains(attacked.getLocation().getChunk())) {
                    attacker.sendMessage(getMessages().getChatMessage(GRAY + "You're not allowed to hurt " + GREEN + attacked.getName() +
                            GRAY + " in their territory."));
                    return false;
                }
            }
        }

        return true;
    }
}