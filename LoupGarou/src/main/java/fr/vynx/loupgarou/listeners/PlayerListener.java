package fr.vynx.loupgarou.listeners;

import fr.vynx.loupgarou.MainLoupGarou;
import fr.vynx.loupgarou.manager.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage("§7[+] §e" + event.getPlayer().getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage("§7[-] §e" + event.getPlayer().getName());
        MainLoupGarou.getInstance().getGameManager().removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        GameManager gm = MainLoupGarou.getInstance().getGameManager();

        if (gm.getPlayers().contains(event.getPlayer().getUniqueId())) {
            if (gm.getState() != GameManager.GameState.WAITING && gm.getState() != GameManager.GameState.ENDED) {
                if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                        event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}