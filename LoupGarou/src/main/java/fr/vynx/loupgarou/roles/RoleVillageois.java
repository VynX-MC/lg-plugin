package fr.vynx.loupgarou.roles;

import org.bukkit.entity.Player;

public class RoleVillageois extends Role {

    // Le constructeur qui utilise la fondation de notre classe 'Role'
    public RoleVillageois(Player player) {
        super(player);
    }

    @Override
    public String getName() {
        // Le code couleur §a donne un texte vert dans Minecraft
        return "§aSimple Villageois";
    }

    @Override
    public String getDescription() {
        // Le code couleur §7 donne un texte gris
        return "§7Ton but est d'éliminer tous les Loups-Garous. Tu n'as aucun pouvoir particulier, sers-toi de ta déduction !";
    }

    @Override
    public void onNightAction() {
        // Le villageois normal dort toute la nuit, il n'a pas de menu à ouvrir
        getPlayer().sendMessage("§8La nuit tombe... Tu t'endors paisiblement.");
    }
}