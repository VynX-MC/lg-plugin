package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleVoyante extends Role {
    public RoleVoyante(Player p) { super(p); }
    public String getName() { return "§dVoyante"; }
    public String getDescription() { return "§7Découvre le rôle d'un joueur chaque nuit."; }
    public void onNightAction() {}
}