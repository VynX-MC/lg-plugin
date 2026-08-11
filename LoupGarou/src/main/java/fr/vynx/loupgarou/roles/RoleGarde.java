package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
import java.util.UUID;
public class RoleGarde extends Role {
    private UUID lastGuarded = null;
    public RoleGarde(Player p) { super(p); }
    public String getName() { return "§3Garde"; }
    public String getDescription() { return "§7Protège un joueur des loups (pas le même 2 fois de suite)."; }
    public void onNightAction() {}
    public UUID getLastGuarded() { return lastGuarded; }
    public void setLastGuarded(UUID id) { lastGuarded = id; }
}