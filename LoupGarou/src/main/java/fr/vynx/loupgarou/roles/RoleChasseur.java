package fr.vynx.loupgarou.roles;

import org.bukkit.entity.Player;

public class RoleChasseur extends Role {
    public RoleChasseur(Player player) { super(player); }

    @Override
    public String getName() { return "§2Chasseur"; }

    @Override
    public String getDescription() {
        return "§7Si tu meurs (par les loups ou le village), tu as le pouvoir d'éliminer un joueur de ton choix avant de rendre l'âme.";
    }

    @Override
    public void onNightAction() {
        getPlayer().sendMessage("§8Tu dors paisiblement avec ton fusil sous l'oreiller...");
    }
}