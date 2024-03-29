package net.nighthawkempires.guilds.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.nighthawkempires.guilds.guild.GuildModel;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class PlayerData {

    public List<UUID> boundaryList;
    public List<UUID> bypassList;

    public HashMap<UUID, String> locationMap;
    public HashMap<UUID, String> chunkMap;
    public HashMap<UUID, String> warmingUp;

    public PlayerData() {
        this.boundaryList = Lists.newArrayList();
        this.bypassList = Lists.newArrayList();

        this.locationMap = Maps.newHashMap();
        this.chunkMap = Maps.newHashMap();
        this.warmingUp = Maps.newHashMap();
    }
}
