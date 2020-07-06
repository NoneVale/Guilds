package net.nighthawkempires.guilds.guild.registry;

import com.google.common.collect.ImmutableList;
import net.nighthawkempires.core.datasection.DataSection;
import net.nighthawkempires.core.datasection.Registry;
import net.nighthawkempires.guilds.guild.GuildModel;
import org.bukkit.Chunk;

import java.util.Map;
import java.util.UUID;

public interface GuildRegistry extends Registry<GuildModel> {

    String NAME = "guilds";

    default GuildModel fromDataSection(String stringKey, DataSection data) {
        return new GuildModel(stringKey, data);
    }

    default GuildModel getGuild(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return fromKey(uuid.toString()).orElseGet(() -> register(new GuildModel(uuid)));
    }

    default GuildModel getGuild(String name) {
        for (GuildModel guildModel : getGuilds()) {
            if (guildModel.getName().toLowerCase().equals(name.toLowerCase())) {
                return guildModel;
            }
        }

        return null;
    }

    default boolean isClaimed(Chunk chunk) {
        for (GuildModel guildModel : getGuilds()) {
            if (guildModel.isClaimed(chunk))
                return true;
        }
        return false;
    }

    default GuildModel claimedBy(Chunk chunk) {
        for (GuildModel guildModel : getRegisteredData().values()) {
            if (guildModel.isClaimed(chunk))
                return guildModel;
        }
        return null;
    }

    default ImmutableList<GuildModel> getGuilds() {
        return ImmutableList.copyOf(getRegisteredData().values());
    }

    @Deprecated
    Map<String, GuildModel> getRegisteredData();

    default boolean guildExists(UUID uuid) {
        return fromKey(uuid.toString()).isPresent();
    }

    default boolean guildExists(String name) {
        for (GuildModel guildModel : getGuilds()) {
            if (guildModel.getName().toLowerCase().equals(name.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}