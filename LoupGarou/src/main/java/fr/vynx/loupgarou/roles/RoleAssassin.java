package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleAssassin extends Role {
    public RoleAssassin(Player p) { super(p); }
    public String getName() { return "§4Assassin"; }
    public String getDescription() { return "§7Tu dois tuer tout le monde. Tu assassines une personne par nuit."; }
    public void onNightAction() {}
}