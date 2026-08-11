package fr.vynx.loupgarou.roles;

import fr.vynx.loupgarou.menu.VoteMenu;
import org.bukkit.entity.Player;

public class RoleLoupGarou extends Role {

    public RoleLoupGarou(Player player) {
        super(player);
    }

    @Override
    public String getName() {
        return "§cLoup-Garou";
    }

    @Override
    public String getDescription() {
        return "§7Ton but est d'éliminer tous les villageois. Chaque nuit, tu te réveilles avec les autres loups pour choisir une victime !";
    }

    @Override
    public void onNightAction() {
        getPlayer().sendMessage("§cLa nuit tombe... C'est l'heure de chasser !");

        // On ouvre le menu de vote spécifiquement pour ce loup
        VoteMenu.openWolfMenu(getPlayer());
    }
}