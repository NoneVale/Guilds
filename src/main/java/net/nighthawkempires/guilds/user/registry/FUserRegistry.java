package net.nighthawkempires.guilds.user.registry;

import net.nighthawkempires.core.datasection.AbstractFileRegistry;
import net.nighthawkempires.guilds.user.UserModel;

import java.util.Map;

public class FUserRegistry extends AbstractFileRegistry<UserModel> implements UserRegistry {
    private static final boolean SAVE_PRETTY = true;

    public FUserRegistry(String path) {
        super(path, NAME, SAVE_PRETTY, 5);
    }

    @Override
    public Map<String, UserModel> getRegisteredData() {
        return REGISTERED_DATA.asMap();
    }
}
