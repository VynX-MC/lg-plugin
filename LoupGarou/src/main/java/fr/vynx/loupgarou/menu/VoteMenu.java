package fr.vynx.loupgarou.menu;

import fr.vynx.loupgarou.MainLoupGarou;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class VoteMenu {

    public static void openWolfMenu(Player wolf) {
        // Création d'un inventaire de 3 lignes (27 cases) avec un titre spécifique
        Inventory inv = Bukkit.createInventory(null, 27, "§cQui dévorer ce soir ?");

        int slot = 0;
        // On boucle sur tous les joueurs en vie dans la partie
        for (UUID targetId : MainLoupGarou.getInstance().getGameManager().getPlayers()) {
            Player target = Bukkit.getPlayer(targetId);

            // On affiche tout le monde SAUF le loup lui-même (on ne se mange pas)
            if (target != null && !target.equals(wolf)) {

                // On crée un item "Tête de joueur"
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();

                if (meta != null) {
                    meta.setOwningPlayer(target); // Applique le skin du joueur
                    meta.setDisplayName("§e" + target.getName()); // Met son nom en jaune
                    head.setItemMeta(meta);
                }

                inv.setItem(slot, head);
                slot++;
            }
        }

        // On ouvre enfin le menu au loup
        wolf.openInventory(inv);
    }
}