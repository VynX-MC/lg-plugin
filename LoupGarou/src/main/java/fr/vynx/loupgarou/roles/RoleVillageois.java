package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleVillageois extends Role {
    public RoleVillageois(Player p) { super(p); }
    public String getName() { return "§aVillageois"; }
    public String getDescription() { return "§7Trouve les Loups-Garous et élimine-les au tribunal."; }
    public void onNightAction() {}
}