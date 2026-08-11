package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RolePyromane extends Role {
    public RolePyromane(Player p) { super(p); }
    public String getName() { return "§6Pyromane"; }
    public String getDescription() { return "§7Asperge un joueur d'essence, ou enflamme tous ceux déjà aspergés !"; }
    public void onNightAction() {}
}