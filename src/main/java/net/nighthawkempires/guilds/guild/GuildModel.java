package net.nighthawkempires.guilds.guild;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.nighthawkempires.core.datasection.DataSection;
import net.nighthawkempires.core.datasection.Model;
import net.nighthawkempires.core.location.SavedChunk;
import net.nighthawkempires.core.location.SavedLocation;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import static net.nighthawkempires.guilds.GuildsPlugin.*;
import static org.bukkit.ChatColor.*;

public class GuildModel implements Model {

    private UUID uuid;

    private String key;
    private String name;
    private String description;

    private ChatColor color;

    private SavedLocation home;

    private UUID leader;

    private List<UUID> members;
    private List<UUID> invites;

    private List<Chunk> claimedTerritory;
    private List<SavedChunk> savedTerritory;

    private ConcurrentMap<UUID, GuildRelation> relations;

    public GuildModel(UUID uuid) {
        this.uuid = uuid;

        this.key = uuid.toString();

        this.name = "";
        this.description = "";

        this.color = GRAY;

        this.home = null;

        this.leader = null;

        this.members = Lists.newArrayList();
        this.invites = Lists.newArrayList();

        this.claimedTerritory = Lists.newArrayList();
        this.savedTerritory = Lists.newArrayList();

        this.relations = Maps.newConcurrentMap();
    }

    public GuildModel(String key, DataSection data) {
        this.uuid = UUID.fromString(key);

        this.key = key;

        this.name = data.getString("name", "");
        this.description = data.getString("description", "");

        this.color = ChatColor.valueOf(data.getString("color", "GRAY"));

        if (data.isSet("home"))
            this.home = new SavedLocation(data.getMap("home"));

        if (data.isSet("leader"))
            this.leader = UUID.fromString(data.getString("leader"));

        this.members = Lists.newArrayList();
        for (String uuid : data.getStringList("members")) {
            this.members.add(UUID.fromString(uuid));
        }

        this.invites = Lists.newArrayList();
        for (String uuid : data.getStringList("invites")) {
            this.invites.add(UUID.fromString(uuid));
        }

        this.claimedTerritory = Lists.newArrayList();
        this.savedTerritory = Lists.newArrayList();
        for (Map<String, Object> map : data.getMapList("territory")) {
            this.claimedTerritory.add(new SavedChunk(map).toChunk());
            this.savedTerritory.add(new SavedChunk(map));
        }

        this.relations = Maps.newConcurrentMap();
        if (data.isSet("relations")) {
            Map<String, Object> relationMap = data.getMap("relations");
            for (String guild : relationMap.keySet()) {
                UUID uuid = UUID.fromString(guild);
                GuildRelation relation = GuildRelation.valueOf(relationMap.get(guild).toString());
                this.relations.putIfAbsent(uuid, relation);
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        getGuildRegistry().register(this);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
        getGuildRegistry().register(this);
    }

    public ChatColor getColor() {
        return this.color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
        getGuildRegistry().register(this);
    }

    public Location getHome() {
        return this.home != null ? this.home.toLocation() : null;
    }

    public void setHome(Location location) {
        if (location == null) {
            this.home = null;
        } else {
            this.home = SavedLocation.fromLocation(location);
        }
        getGuildRegistry().register(this);
    }

    public UUID getLeader() {
        return this.leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
        getGuildRegistry().register(this);
    }

    public ImmutableList<UUID> getMembers() {
        return ImmutableList.copyOf(this.members);
    }

    public void addMember(UUID uuid) {
        this.members.add(uuid);
        getGuildRegistry().register(this);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
        getGuildRegistry().register(this);
    }

    public boolean isMember(UUID uuid) {
        return this.members.contains(uuid);
    }

    public ImmutableList<UUID> getInvites() {
        return ImmutableList.copyOf(this.invites);
    }

    public void addInvite(UUID uuid) {
        this.invites.add(uuid);
        getGuildRegistry().register(this);
    }

    public void removeInvite(UUID uuid) {
        this.invites.remove(uuid);
        getGuildRegistry().register(this);
    }

    public boolean isInvited(UUID uuid) {
        return this.invites.contains(uuid);
    }

    public ImmutableList<Chunk> getTerritory() {
        return ImmutableList.copyOf(this.claimedTerritory);
    }

    public void claim(Chunk chunk) {
        this.claimedTerritory.add(chunk);
        this.savedTerritory.add(SavedChunk.fromChunk(chunk));
        getGuildRegistry().register(this);
    }

    public void unclaim(Chunk chunk) {
        for (Chunk chunks : this.claimedTerritory) {
            if (chunk.getX() == chunks.getX() && chunk.getZ() == chunks.getZ()) {
                this.claimedTerritory.remove(chunks);
                this.savedTerritory.removeIf(savedChunk -> savedChunk.toChunk() == chunk);
                break;
            }
        }
        getGuildRegistry().register(this);
    }

    public void unclaimAll() {
        this.claimedTerritory.clear();
        this.savedTerritory.clear();
        getGuildRegistry().register(this);
    }

    public boolean isClaimed(Chunk chunk) {
        for (SavedChunk chunks : savedTerritory) {
            if (chunks.toChunk() == chunk) {
                return true;
            }
        }
        return this.claimedTerritory.contains(chunk);
    }

    public ImmutableMap<UUID, GuildRelation> getRelations() {
        return ImmutableMap.copyOf(this.relations);
    }

    public void setRelation(UUID uuid, GuildRelation relation) {
        this.relations.put(uuid, relation);
    }

    public void setRelation(GuildModel guild, GuildRelation relation) {
        setRelation(guild.getUniqueId(), relation);
    }

    public boolean isAllied(UUID uuid) {
        if (!getGuildRegistry().guildExists(uuid)) return false;
        GuildModel other = getGuildRegistry().getGuild(uuid);
        return this.getRelation(uuid) == GuildRelation.ALLY && other.getRelation(this.uuid) == GuildRelation.ALLY;
    }

    public boolean isTruced(UUID uuid) {
        if (!getGuildRegistry().guildExists(uuid)) return false;
        GuildModel other = getGuildRegistry().getGuild(uuid);
        return this.getRelation(uuid) == GuildRelation.TRUCE && other.getRelation(this.uuid) == GuildRelation.TRUCE;
    }

    public boolean isEnemy(UUID uuid) {
        if (!getGuildRegistry().guildExists(uuid)) return false;
        GuildModel other = getGuildRegistry().getGuild(uuid);
        return this.getRelation(uuid) == GuildRelation.ENEMY || other.getRelation(this.uuid) == GuildRelation.ENEMY;
    }

    public boolean isNeutral(UUID uuid) {
        if (!getGuildRegistry().guildExists(uuid)) return false;
        GuildModel other = getGuildRegistry().getGuild(uuid);

        if (this.getRelation(uuid) == GuildRelation.NEUTRAL && other.getRelation(this.uuid) == GuildRelation.NEUTRAL)
            return true;
        else if (!isAllied(uuid) && !isTruced(uuid) && !isEnemy(uuid))
            return true;
        else return this.getRelation(uuid) == GuildRelation.NEUTRAL;
    }

    public GuildRelation getRelationStatus(UUID uuid) {
        if (!getGuildRegistry().guildExists(uuid)) return GuildRelation.NEUTRAL;

        if (isAllied(uuid))
            return GuildRelation.ALLY;
        else if (isTruced(uuid))
            return GuildRelation.TRUCE;
        else if (isEnemy(uuid))
            return GuildRelation.ENEMY;
        else if (isNeutral(uuid))
            return GuildRelation.NEUTRAL;

        return GuildRelation.NEUTRAL;
    }

    public GuildRelation getRelationStatus(GuildModel other) {
        UUID uuid = UUID.fromString(other.getKey());
        return getRelationStatus(uuid);
    }

    public GuildRelation getRelation(UUID uuid) {
        if (!this.relations.containsKey(uuid)) return GuildRelation.NEUTRAL;
        return this.relations.get(uuid);
    }

    public GuildRelation getRelation(GuildModel guild) {
        UUID uuid = UUID.fromString(guild.getKey());
        return getRelation(uuid);
    }

    public String getKey() {
        return this.key;
    }

    public UUID getUniqueId() {
        return UUID.fromString(this.key);
    }

    public void message(String message) {
        for (UUID uuid : getMembers()) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            if (offline.isOnline()) {
                offline.getPlayer().sendMessage(message);
            }
        }
    }

    public void message(String message, Player... exclude) {
        for (UUID uuid : getMembers()) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
            if (offline.isOnline()) {
                Player target = offline.getPlayer();
                for (Player excluded : exclude) {
                    if (target.getUniqueId() != excluded.getUniqueId()) {
                        target.sendMessage(message);
                    }
                }
            }
        }
    }

    public int getSize() {
        return this.members.size();
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newHashMap();

        map.put("name", name);
        map.put("description", description);

        map.put("color", color.name());

        if (home != null)
            map.put("home", home.serialize());

        if (leader != null)
            map.put("leader", leader.toString());

        List<String> memberList = Lists.newArrayList();
        for (UUID uuid : members)
            memberList.add(uuid.toString());
        map.put("members", memberList);

        List<String> inviteList = Lists.newArrayList();
        for (UUID uuid : invites)
            inviteList.add(uuid.toString());
        map.put("invites", inviteList);

        List<Map<String, Object>> territoryList = Lists.newArrayList();
        for (Chunk chunk : claimedTerritory)
            territoryList.add(SavedChunk.fromChunk(chunk).serialize());
        map.put("territory", territoryList);

        Map<String, String> relationMap = Maps.newHashMap();
        for (UUID uuid : this.relations.keySet()) {
            if (getRelation(uuid) != GuildRelation.NEUTRAL) {
                relationMap.put(uuid.toString(), getRelation(uuid).name());
            }
        }
        map.put("relations", relationMap);

        return map;
    }
}
