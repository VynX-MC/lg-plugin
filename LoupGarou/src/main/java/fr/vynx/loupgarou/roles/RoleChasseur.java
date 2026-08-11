package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleChasseur extends Role {
    public RoleChasseur(Player p) { super(p); }
    public String getName() { return "§2Chasseur"; }
    public String getDescription() { return "§7À ta mort, tu peux abattre un dernier joueur."; }
    public void onNightAction() {}
}