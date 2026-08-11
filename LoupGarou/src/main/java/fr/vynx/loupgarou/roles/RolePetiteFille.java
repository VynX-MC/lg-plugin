package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RolePetiteFille extends Role {
    public RolePetiteFille(Player p) { super(p); }
    public String getName() { return "§9Petite Fille"; }
    public String getDescription() { return "§7Tu n'as pas de cécité la nuit et entends les loups chuchoter."; }
    public void onNightAction() {}
}