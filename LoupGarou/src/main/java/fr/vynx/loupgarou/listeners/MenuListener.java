package fr.vynx.loupgarou.listeners;

import fr.vynx.loupgarou.MainLoupGarou;
import fr.vynx.loupgarou.manager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        GameManager gm = MainLoupGarou.getInstance().getGameManager();
        Player player = (Player) event.getWhoClicked();

        if (title.contains("Qui ") || title.contains("Vote") || title.contains("Élection") ||
                title.contains("Successeur") || title.contains("Vision") || title.contains("Potions") ||
                title.contains("Flèche") || title.contains("tir") || title.contains("Action") ||
                title.contains("camp") || title.contains("rôle") || title.contains("Charmer") ||
                title.contains("Trahir")) {

            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getItemMeta() == null) return;

            // --- MENUS SPÉCIAUX ---
            if (title.equals("§dPotions de la Sorcière")) {
                if (clicked.getType() == Material.GLISTERING_MELON_SLICE) {
                    player.sendMessage("§aTu as utilisé ta Potion de Vie !");
                    player.closeInventory(); gm.sorciereUseLifePotion();
                } else if (clicked.getType() == Material.SPIDER_EYE) {
                    gm.openSorciereKill(player); // Transition fluide !
                } else if (clicked.getType() == Material.PAPER) {
                    player.closeInventory(); gm.nextNightPhase();
                }
                return;
            }

            if (title.equals("§6Action du Pyromane")) {
                if (clicked.getType() == Material.WATER_BUCKET) {
                    gm.openPyromaneAsperger(player); // Transition fluide !
                } else if (clicked.getType() == Material.FLINT_AND_STEEL) {
                    player.sendMessage("§cTu as tout enflammé !");
                    player.closeInventory(); gm.pyromaneIgnite();
                } else if (clicked.getType() == Material.PAPER) {
                    player.closeInventory(); gm.nextNightPhase();
                }
                return;
            }

            if (title.equals("§bChoisis ton camp")) {
                player.closeInventory();
                gm.chienLoupChoose(clicked.getType() == Material.BONE);
                return;
            }

            if (title.equals("§8Choisis ton nouveau rôle")) {
                player.closeInventory();
                gm.voleurChoose(clicked.getItemMeta().getDisplayName());
                return;
            }

            if (title.equals("§fTrahir un Loup ?") && clicked.getType() == Material.PAPER) {
                player.closeInventory(); gm.nextNightPhase(); return;
            }

            // --- MENUS AVEC TÊTES DE JOUEURS ---
            if (clicked.getType() == Material.PLAYER_HEAD) {
                String targetName = clicked.getItemMeta().getDisplayName().substring(2);
                Player target = Bukkit.getPlayerExact(targetName);
                if (target == null) return;
                UUID tId = target.getUniqueId();

                if (title.equals("§cQui dévorer ce soir ?")) {
                    gm.registerWolfVote(player.getUniqueId(), tId);
                } else if (title.equals("§eVote du Village")) {
                    gm.registerDayVote(player.getUniqueId(), tId);
                } else if (title.equals("§6Élection du Maire")) {
                    gm.registerElectionVote(player.getUniqueId(), tId);
                } else if (title.equals("§6Choix du Successeur")) {
                    gm.setMayor(tId); player.closeInventory();
                } else if (title.equals("§dVision de la Voyante")) {
                    player.sendMessage("§dVision : §e" + target.getName() + " §dest " + gm.getPlayerRole(target).getName());
                    player.closeInventory(); gm.nextNightPhase(); return;
                } else if (title.equals("§cQui assassiner ? (Sorcière)")) {
                    player.closeInventory(); gm.sorciereUseDeathPotion(tId); return;
                } else if (title.equals("§3Qui protéger ?")) {
                    player.closeInventory(); gm.registerGarde(tId); return;
                } else if (title.equals("§4Qui assassiner ?")) {
                    player.closeInventory(); gm.registerAssassin(tId); return;
                } else if (title.equals("§5Qui mordre ce soir ?")) {
                    player.closeInventory(); gm.vampireBite(tId); return;
                } else if (title.equals("§6Qui asperger d'essence ?")) {
                    player.closeInventory(); gm.pyromaneAsperger(tId); return;
                } else if (title.equals("§fTrahir un Loup ?")) {
                    player.closeInventory(); gm.loupBlancKill(tId); return;
                } else if (title.equals("§2Dernier tir du Chasseur !")) {
                    player.closeInventory(); gm.chasseurShoot(tId); return;
                } else if (title.contains("Charmer")) {
                    gm.fluteCharm(player, tId, title.contains("1/2")); return;
                } else if (title.contains("Flèche")) {
                    gm.cupidonShoot(player, tId, title.contains("1/2")); return;
                }

                if (!title.contains("Flèche") && !title.contains("Charmer")) player.closeInventory();
            }
        }
    }
}
