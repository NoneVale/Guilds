package net.nighthawkempires.guilds.guild;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.nighthawkempires.core.chat.tag.PlayerTag;
import net.nighthawkempires.guilds.user.UserModel;
import org.bukkit.entity.Player;

import static net.md_5.bungee.api.ChatColor.*;
import static net.nighthawkempires.guilds.GuildsPlugin.*;

public class GuildTag extends PlayerTag {

    public String getName() {
        return "guild";
    }

    public TextComponent getFor(Player player) {
        UserModel userModel = getUserRegistry().getUser(player.getUniqueId());
        if (userModel.getGuild() != null) {
            TextComponent tag = new TextComponent("[");
            tag.setColor(DARK_GRAY);
            TextComponent mid = new TextComponent(userModel.getGuild().getName());
            mid.setColor(userModel.getGuild().getColor().asBungee());
            tag.addExtra(mid);
            tag.addExtra("]");

            tag.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(GRAY +
                    userModel.getRank().getName() + " of " + userModel.getGuild().getColor().asBungee() + userModel.getGuild().getName())));
            return tag;
        }
        return null;
    }

    public int getPriority() {
        return 2;
    }
}
