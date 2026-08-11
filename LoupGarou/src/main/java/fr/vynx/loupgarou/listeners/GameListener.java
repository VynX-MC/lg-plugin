package fr.vynx.loupgarou.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class GameListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        // On empêche les joueurs d'avoir faim, pas besoin de manger dans un Loup-Garou !
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        // On vérifie que celui qui prend le dégât est bien un joueur
        if (event.getEntity() instanceof Player) {
            // On bloque tous les dégâts (chute, coups de poing, lave...)
            // Les vrais meurtres seront gérés par le code des Loups-Garous plus tard !
            event.setCancelled(true);
        }
    }
}