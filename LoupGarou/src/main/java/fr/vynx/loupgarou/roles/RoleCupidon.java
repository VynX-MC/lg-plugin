package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
import java.util.UUID;
public class RoleCupidon extends Role {
    private UUID firstLover = null;
    public RoleCupidon(Player p) { super(p); }
    public String getName() { return "§dCupidon"; }
    public String getDescription() { return "§7Lies deux joueurs pour la vie au premier tour."; }
    public void onNightAction() {}
    public UUID getFirstLover() { return firstLover; }
    public void setFirstLover(UUID id) { firstLover = id; }
}