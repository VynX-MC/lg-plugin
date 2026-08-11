package fr.vynx.loupgarou.roles;

import org.bukkit.entity.Player;

public abstract class Role {
    private final Player player;

    public Role(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public abstract String getName();
    public abstract String getDescription();
    public abstract void onNightAction();
}