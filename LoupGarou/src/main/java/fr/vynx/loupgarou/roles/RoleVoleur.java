package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleVoleur extends Role {
    public RoleVoleur(Player p) { super(p); }
    public String getName() { return "§8Voleur"; }
    public String getDescription() { return "§7Au premier tour, choisis ton rôle parmi deux cartes."; }
    public void onNightAction() {}
}