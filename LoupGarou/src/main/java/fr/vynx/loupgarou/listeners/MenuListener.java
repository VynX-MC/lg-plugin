package fr.vynx.loupgarou.listeners;

import fr.vynx.loupgarou.MainLoupGarou;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§cQui dévorer ce soir ?")) {

            event.setCancelled(true); // Empêche de voler la tête

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getItemMeta() == null) return;

            Player player = (Player) event.getWhoClicked();

            // On récupère le nom (en retirant le code couleur §e)
            String targetName = clickedItem.getItemMeta().getDisplayName().substring(2);
            Player target = Bukkit.getPlayerExact(targetName);

            if (target != null) {
                player.sendMessage("§aTu as voté pour dévorer §e" + targetName + " §a!");
                player.closeInventory();

                // ON TRANSMET LE VOTE AU CERVEAU DU JEU
                MainLoupGarou.getInstance().getGameManager().registerWolfVote(player.getUniqueId(), target.getUniqueId());
            } else {
                player.sendMessage("§cCe joueur est introuvable !");
            }
        }
    }
}