package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleBouffon extends Role {
    public RoleBouffon(Player p) { super(p); }
    public String getName() { return "§eBouffon"; }
    public String getDescription() { return "§7Ton but est d'être éliminé lors du vote de jour par le village."; }
    public void onNightAction() {}
}