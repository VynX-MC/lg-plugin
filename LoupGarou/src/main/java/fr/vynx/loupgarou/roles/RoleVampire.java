package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleVampire extends Role {
    public RoleVampire(Player p) { super(p); }
    public String getName() { return "§5Vampire"; }
    public String getDescription() { return "§7Tu transformes un joueur en vampire chaque nuit."; }
    public void onNightAction() {}
}