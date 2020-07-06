package net.nighthawkempires.guilds.listeners;

import net.nighthawkempires.core.CorePlugin;
import net.nighthawkempires.guilds.GuildsPlugin;
import net.nighthawkempires.guilds.guild.GuildRank;
import net.nighthawkempires.guilds.user.UserModel;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import static net.nighthawkempires.core.CorePlugin.*;
import static net.nighthawkempires.guilds.GuildsPlugin.*;
import static org.bukkit.ChatColor.*;

public class InventoryListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            UserModel userModel = GuildsPlugin.getUserRegistry().getUser(player.getUniqueId());

            if (getInventoryData().shopInventories.contains(event.getView().getTopInventory())) {
                if (getInventoryData().shopInventories.contains(event.getClickedInventory())) {
                    if (userModel.getGuild() != null && (userModel.getRank()
                            == GuildRank.LEADER || userModel.getRank() == GuildRank.OFFICER)) {
                        if (event.isShiftClick()) {
                            event.setCancelled(true);
                            return;
                        }

                        if (event.getCurrentItem() != null) {
                            ItemStack clicked = event.getCurrentItem();
                            if (clicked.getType().name().contains("STAINED_GLASS_PANE"))
                                return;

                            String itemName = stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                            ChatColor color = null;
                            for (ChatColor colors : values()) {
                                if (colors.name().replaceAll("_", " ").toLowerCase().equals(itemName.toLowerCase())) {
                                    color = colors;
                                    break;
                                }
                            }

                            if (color == null) return;

                            if (userModel.getGuild().getColor() == color) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "That is already the current color of the guild."));
                                return;
                            }

                            player.closeInventory();

                            net.nighthawkempires.core.user.UserModel coreUser = CorePlugin.getUserRegistry().getUser(player.getUniqueId());
                            if (coreUser.getTokens() < 10) {
                                player.sendMessage(getMessages().getChatMessage(GRAY + "You do not have enough tokens to do this."));
                                return;
                            }

                            userModel.getGuild().setColor(color);
                            coreUser.setTokens(coreUser.getTokens() - 10);
                            player.sendMessage(getMessages().getChatMessage(GRAY + "You have set the guild's color to " + color + itemName + GRAY + "."));
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                            userModel.getGuild().message(getMessages().getChatMessage(GREEN + player.getName() + GRAY + " has set the guild's color to "
                                    + color + itemName + GRAY + "."), player);
                        }
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (getInventoryData().shopInventories.contains(event.getInventory())) {
            getInventoryData().shopInventories.remove(event.getInventory());
        }
    }
}