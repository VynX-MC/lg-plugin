package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleLoupGarouBlanc extends Role {
    public RoleLoupGarouBlanc(Player p) { super(p); }
    public String getName() { return "§fLoup-Garou Blanc"; }
    public String getDescription() { return "§7Tu gagnes seul. Tu peux dévorer un loup chaque nuit !"; }
    public void onNightAction() {}
}