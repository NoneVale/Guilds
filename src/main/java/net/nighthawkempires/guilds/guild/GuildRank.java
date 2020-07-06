package net.nighthawkempires.guilds.guild;

public enum GuildRank {

    LEADER, OFFICER, MEMBER, RECRUIT;

    public String getName() {
        return this.name().substring(0, 1).toUpperCase() + this.name().substring(1, this.name().length()).toLowerCase();
    }
}
