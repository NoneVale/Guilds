package net.nighthawkempires.guilds.user.registry;

import net.nighthawkempires.core.datasection.DataSection;
import net.nighthawkempires.core.datasection.Registry;
import net.nighthawkempires.guilds.user.UserModel;

import java.util.Map;
import java.util.UUID;

public interface UserRegistry extends Registry<UserModel> {

    String NAME = "users";

    default UserModel fromDataSection(String stringKey, DataSection data) {
        return new UserModel(stringKey, data);
    }

    default UserModel getUser(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return fromKey(uuid.toString()).orElseGet(() -> register(new UserModel(uuid)));
    }

    @Deprecated
    Map<String, UserModel> getRegisteredData();

    default boolean userExists(UUID uuid) {
        return fromKey(uuid.toString()).isPresent();
    }
}
