package net.nighthawkempires.guilds.user;

import com.google.common.collect.Maps;
import net.nighthawkempires.core.datasection.DataSection;
import net.nighthawkempires.core.datasection.Model;
import net.nighthawkempires.guilds.guild.GuildModel;
import net.nighthawkempires.guilds.guild.GuildRank;

import java.util.Map;
import java.util.UUID;

import static net.nighthawkempires.guilds.GuildsPlugin.*;

public class UserModel implements Model {

    private String key;

    private GuildModel guild;
    private GuildRank rank;

    private int power;

    public UserModel(UUID uuid) {
        this.key = uuid.toString();

        this.guild = null;
        this.rank = null;

        this.power = 10;
    }

    public UserModel(String key, DataSection data) {
        this.key = key;

        if (data.isSet("guild")) {
            UUID uuid = UUID.fromString(data.getString("guild"));
            if (getGuildRegistry().guildExists(uuid))
                this.guild = getGuildRegistry().getGuild(uuid);

            this.rank = GuildRank.valueOf(data.getString("rank"));
        }

        this.power = data.getInt("power");
    }

    public GuildModel getGuild() {
        return this.guild;
    }

    public void setGuild(GuildModel guild) {
        this.guild = guild;
        getUserRegistry().register(this);
    }

    public GuildRank getRank() {
        return this.rank;
    }

    public void setRank(GuildRank rank) {
        this.rank = rank;
        getUserRegistry().register(this);
    }

    public int getPower() {
        return this.power;
    }

    public void setPower(int power) {
        this.power = power;
        getUserRegistry().register(this);
    }

    public void addPower(int power) {
        this.setPower(getPower() + power);
    }

    public void removePower(int power) {
        this.setPower(getPower() - power);
    }

    public String getKey() {
        return this.key;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newHashMap();

        if (guild != null) {
            map.put("guild", guild.getKey());

            if (rank != null) {
                map.put("rank", rank.name());
            }
        }

        map.put("power", power);
        return map;
    }
}
