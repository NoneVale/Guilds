package net.nighthawkempires.guilds.user.registry;

import com.mongodb.client.MongoDatabase;
import net.nighthawkempires.core.datasection.AbstractMongoRegistry;
import net.nighthawkempires.guilds.user.UserModel;

import java.util.Map;

public class MUserRegistry extends AbstractMongoRegistry<UserModel> implements UserRegistry {

    public MUserRegistry(MongoDatabase database) {
        super(database.getCollection(NAME), 5);
    }

    @Override
    public Map<String, UserModel> getRegisteredData()
    {
        return m_RegisteredData.asMap();
    }
}
