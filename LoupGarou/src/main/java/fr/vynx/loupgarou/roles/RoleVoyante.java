package fr.vynx.loupgarou.roles;

import fr.vynx.loupgarou.menu.VoteMenu;
import org.bukkit.entity.Player;

public class RoleVoyante extends Role {

    private boolean hasLookedThisNight = false;

    public RoleVoyante(Player player) { super(player); }

    @Override
    public String getName() { return "§dVoyante"; }

    @Override
    public String getDescription() {
        return "§7Chaque nuit, tu peux découvrir le rôle d'un joueur.";
    }

    @Override
    public void onNightAction() {
        hasLookedThisNight = false; // On réinitialise son pouvoir chaque nuit
        getPlayer().sendMessage("§dC'est à ton tour... Choisis un joueur !");
        VoteMenu.openVoyanteMenu(getPlayer());
    }

    public boolean hasLooked() { return hasLookedThisNight; }
    public void setLooked(boolean b) { this.hasLookedThisNight = b; }
}