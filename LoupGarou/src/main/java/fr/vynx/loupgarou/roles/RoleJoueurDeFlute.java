package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleJoueurDeFlute extends Role {
    public RoleJoueurDeFlute(Player p) { super(p); }
    public String getName() { return "§aJoueur de Flûte"; }
    public String getDescription() { return "§7Charme 2 joueurs par nuit. Tu gagnes si tous les vivants sont charmés."; }
    public void onNightAction() {}
}