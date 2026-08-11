package fr.vynx.loupgarou.listeners;

import fr.vynx.loupgarou.MainLoupGarou;
import fr.vynx.loupgarou.manager.GameManager;
import fr.vynx.loupgarou.roles.Role;
import fr.vynx.loupgarou.roles.RoleLoupGarou;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GameManager gm = MainLoupGarou.getInstance().getGameManager();

        // On ne s'occupe que des joueurs qui sont dans la partie
        if (!gm.getPlayers().contains(player.getUniqueId())) return;

        // Si c'est la nuit, on modifie les règles de la parole
        if (gm.getState() == GameManager.GameState.NIGHT) {

            // On annule le message public pour que personne ne l'entende
            event.setCancelled(true);

            Role role = gm.getPlayerRole(player);

            // Si c'est un Loup-Garou qui parle
            if (role instanceof RoleLoupGarou) {
                // On envoie son message uniquement aux autres loups
                for (UUID uuid : gm.getPlayers()) {
                    Player target = Bukkit.getPlayer(uuid);
                    if (target != null) {
                        Role targetRole = gm.getPlayerRole(target);
                        if (targetRole instanceof RoleLoupGarou) {
                            target.sendMessage("§c[Chat Loup] " + player.getName() + " : §7" + event.getMessage());
                        }
                    }
                }
            } else {
                // Si c'est un villageois, on le fait taire
                player.sendMessage("§cChut... Tu dors, tu ne peux pas parler la nuit !");
            }
        }
    }
}