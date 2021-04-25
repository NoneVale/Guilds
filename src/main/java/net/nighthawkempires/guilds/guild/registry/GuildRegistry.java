package net.nighthawkempires.guilds.guild.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.nighthawkempires.core.datasection.DataSection;
import net.nighthawkempires.core.datasection.Registry;
import net.nighthawkempires.guilds.guild.GuildModel;
import org.bukkit.Chunk;

import java.util.List;
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
        for (GuildModel guildModel : getData().values()) {
            if (guildModel.isClaimed(chunk))
                return guildModel;
        }
        return null;
    }

    default ImmutableList<GuildModel> getGuilds() {
        return ImmutableList.copyOf(getData().values());
    }

    @Deprecated
    Map<String, GuildModel> getRegisteredData();

    default Map<String, GuildModel> getData() {
        return loadAllFromDb();
    }

    default ImmutableList<String> getGuildNames() {
        List<String> names = Lists.newArrayList();
        for (GuildModel guild : getGuilds()) {
            names.add(guild.getName());
        }
        return ImmutableList.copyOf(names);
    }

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