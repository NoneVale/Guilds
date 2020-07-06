package net.nighthawkempires.guilds.data;

import com.google.common.collect.Lists;
import net.nighthawkempires.core.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static org.bukkit.ChatColor.*;
import static org.bukkit.Material.*;

public class InventoryData {

    public List<Inventory> shopInventories;

    public InventoryData() {
        this.shopInventories = Lists.newArrayList();
    }

    public Inventory getColorShopInventory() {
        String lore = DARK_GRAY + "Price" + GRAY + ": " + GOLD + "10 " + GRAY + "Tokens";
        ItemStack[] items = new ItemStack[]{
                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),
                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),
                new ItemBuilder(RED_CONCRETE).displayName(DARK_RED + "Dark Red").lore(lore).build(),
                new ItemBuilder(RED_CONCRETE_POWDER).displayName(RED + "Red").lore(lore).build(),
                new ItemBuilder(YELLOW_CONCRETE).displayName(GOLD + "Gold").lore(lore).build(),
                new ItemBuilder(YELLOW_CONCRETE_POWDER).displayName(YELLOW + "Yellow").lore(lore).build(),
                new ItemBuilder(LIME_CONCRETE).displayName(DARK_GREEN + "Dark Green").lore(lore).build(),
                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),
                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),

                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),
                new ItemBuilder(LIME_CONCRETE_POWDER).displayName(GREEN + "Green").lore(lore).build(),
                new ItemBuilder(CYAN_CONCRETE).displayName(DARK_AQUA + "Dark Aqua").lore(lore).build(),
                new ItemBuilder(LIGHT_BLUE_CONCRETE_POWDER).displayName(AQUA + "Aqua").lore(lore).build(),
                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),
                new ItemBuilder(BLUE_CONCRETE).displayName(DARK_BLUE + "Dark Blue").lore(lore).build(),
                new ItemBuilder(BLUE_CONCRETE_POWDER).displayName(BLUE + "Blue").lore(lore).build(),
                new ItemBuilder(PURPLE_CONCRETE).displayName(DARK_PURPLE + "Dark Purple").lore(lore).build(),
                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),

                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),
                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),
                new ItemBuilder(PINK_CONCRETE).displayName(LIGHT_PURPLE + "Light Purple").lore(lore).build(),
                new ItemBuilder(WHITE_CONCRETE).displayName(WHITE + "White").lore(lore).build(),
                new ItemBuilder(LIGHT_GRAY_CONCRETE).displayName(GRAY + "Gray").lore(lore).build(),
                new ItemBuilder(GRAY_CONCRETE).displayName(DARK_GRAY + "Dark Gray").lore(lore).build(),
                new ItemBuilder(BLACK_CONCRETE).displayName(BLACK + "Black").lore(lore).build(),
                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),
                new ItemBuilder(BLACK_STAINED_GLASS_PANE).displayName(" ").build(),
        };

        Inventory inventory = Bukkit.createInventory(null, 27, "Guild Color Shop");
        inventory.setContents(items);

        this.shopInventories.add(inventory);
        return inventory;
    }
}
