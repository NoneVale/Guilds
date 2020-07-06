package net.nighthawkempires.guilds.guild;

public enum GuildRelation {

    ALLY, ENEMY, NEUTRAL, TRUCE;

    public String getName() {
        return this.name().substring(0, 1).toUpperCase() + this.name().substring(1, this.name().length()).toLowerCase();
    }
}