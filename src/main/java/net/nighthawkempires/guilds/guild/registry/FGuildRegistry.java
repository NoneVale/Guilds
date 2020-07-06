package net.nighthawkempires.guilds.guild.registry;

import net.nighthawkempires.core.datasection.AbstractFileRegistry;
import net.nighthawkempires.guilds.guild.GuildModel;

import java.util.Map;

public class FGuildRegistry extends AbstractFileRegistry<GuildModel> implements GuildRegistry {
    private static final boolean SAVE_PRETTY = true;

    public FGuildRegistry(String path) {
        super(path, NAME, SAVE_PRETTY, -1);
    }

    @Override
    public Map<String, GuildModel> getRegisteredData() {
        return REGISTERED_DATA.asMap();
    }
}
