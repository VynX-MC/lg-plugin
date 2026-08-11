package fr.vynx.loupgarou.roles;

import org.bukkit.entity.Player;

public abstract class Role {

    // Le joueur qui possède ce rôle
    private final Player player;

    // Le constructeur (quand on donne le rôle au joueur)
    public Role(Player player) {
        this.player = player;
    }

    // Permet de récupérer le joueur pour lui envoyer des messages ou le téléporter
    public Player getPlayer() {
        return player;
    }

    // --------------------------------------------------------
    // Méthodes "abstraites" que chaque rôle devra personnaliser
    // --------------------------------------------------------

    // Le nom du rôle (ex: "§cLoup-Garou", "§aVillageois")
    public abstract String getName();

    // La description envoyée au joueur au début de la partie
    public abstract String getDescription();

    // Ce que fait ce rôle pendant la nuit (ex: se réveiller pour voter)
    // Si c'est un simple villageois, cette méthode ne fera rien.
    public abstract void onNightAction();
}