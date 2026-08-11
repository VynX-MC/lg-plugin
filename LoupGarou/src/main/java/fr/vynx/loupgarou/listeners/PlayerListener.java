package fr.vynx.loupgarou.listeners;

import fr.vynx.loupgarou.MainLoupGarou;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    // @EventHandler indique à notre serveur d'écouter attentivement cet événement spécifique
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // On vérifie si ce joueur était inscrit dans notre partie
        if (MainLoupGarou.getInstance().getGameManager().getPlayers().contains(player.getUniqueId())) {

            // Si oui, on le retire proprement pour ne pas faire bugger le jeu
            MainLoupGarou.getInstance().getGameManager().removePlayer(player);

            // Petit bonus : on peut afficher un message aux autres joueurs si on veut
            System.out.println("[Loup-Garou] " + player.getName() + " s'est deconnecté et a ete retire de la partie.");
        }
    }
}