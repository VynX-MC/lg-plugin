package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleLoupGarou extends Role {
    public RoleLoupGarou(Player p) { super(p); }
    public String getName() { return "§cLoup-Garou"; }
    public String getDescription() { return "§7Mangez un villageois chaque nuit avec les autres loups."; }
    public void onNightAction() {}
}