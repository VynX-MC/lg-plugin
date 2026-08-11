package fr.vynx.loupgarou.roles;

import org.bukkit.entity.Player;
import java.util.UUID;

public class RoleCupidon extends Role {

    private UUID firstLover = null;

    public RoleCupidon(Player player) { super(player); }

    @Override
    public String getName() { return "§dCupidon"; }

    @Override
    public String getDescription() {
        return "§7Désigne deux joueurs qui tomberont fous amoureux. Si l'un meurt, l'autre se suicide.";
    }

    @Override
    public void onNightAction() {}

    public UUID getFirstLover() { return firstLover; }
    public void setFirstLover(UUID id) { this.firstLover = id; }
}