package net.nighthawkempires.guilds.guild.registry;

import com.mongodb.client.MongoDatabase;
import net.nighthawkempires.core.datasection.AbstractMongoRegistry;
import net.nighthawkempires.guilds.guild.GuildModel;

import java.util.Map;

public class MGuildRegistry extends AbstractMongoRegistry<GuildModel> implements GuildRegistry {

    public MGuildRegistry(MongoDatabase database) {
        super(database.getCollection(NAME), -1);
    }

    @Override
    public Map<String, GuildModel> getRegisteredData()
    {
        return m_RegisteredData.asMap();
    }
}
