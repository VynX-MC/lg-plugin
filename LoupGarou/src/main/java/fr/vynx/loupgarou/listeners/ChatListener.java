package fr.vynx.loupgarou.listeners;

import fr.vynx.loupgarou.MainLoupGarou;
import fr.vynx.loupgarou.manager.GameManager;
import fr.vynx.loupgarou.roles.Role;
import fr.vynx.loupgarou.roles.RoleLoupGarou;
import fr.vynx.loupgarou.roles.RolePetiteFille;
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

        if (!gm.getPlayers().contains(player.getUniqueId())) return;

        if (gm.getState() == GameManager.GameState.NIGHT) {
            event.setCancelled(true);
            Role role = gm.getPlayerRole(player);

            if (role instanceof RoleLoupGarou) {
                for (UUID uuid : gm.getPlayers()) {
                    Player target = Bukkit.getPlayer(uuid);
                    if (target != null) {
                        Role targetRole = gm.getPlayerRole(target);
                        if (targetRole instanceof RoleLoupGarou) {
                            target.sendMessage("§c[Chat Loup] " + player.getName() + " : §7" + event.getMessage());
                        } else if (targetRole instanceof RolePetiteFille) {
                            // La Petite Fille entend le message mais le nom est caché !
                            target.sendMessage("§8[Murmure de Loup] §7" + event.getMessage());
                        }
                    }
                }
            } else {
                player.sendMessage("§cChut... Tu dors, tu ne peux pas parler la nuit !");
            }
        }
    }
}