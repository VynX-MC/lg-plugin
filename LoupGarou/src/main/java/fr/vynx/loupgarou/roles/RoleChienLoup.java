package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleChienLoup extends Role {
    public RoleChienLoup(Player p) { super(p); }
    public String getName() { return "§bChien Loup"; }
    public String getDescription() { return "§7Au premier tour, choisis de rejoindre les Villageois ou les Loups."; }
    public void onNightAction() {}
}