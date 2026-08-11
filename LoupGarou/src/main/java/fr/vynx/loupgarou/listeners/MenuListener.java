package fr.vynx.loupgarou.listeners;

import fr.vynx.loupgarou.MainLoupGarou;
import fr.vynx.loupgarou.manager.GameManager;
import fr.vynx.loupgarou.roles.Role;
import fr.vynx.loupgarou.roles.RoleCupidon;
import fr.vynx.loupgarou.roles.RoleVoyante;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        GameManager gm = MainLoupGarou.getInstance().getGameManager();
        Player player = (Player) event.getWhoClicked();

        if (title.equals("§cQui dévorer ce soir ?") || title.equals("§eVote du Village") ||
                title.equals("§6Élection du Maire") || title.equals("§6Choix du Successeur") ||
                title.equals("§dVision de la Voyante") || title.equals("§dPotions de la Sorcière") ||
                title.equals("§cQui assassiner ?") || title.contains("Flèche de Cupidon") ||
                title.equals("§2Dernier tir du Chasseur !")) {

            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getItemMeta() == null) return;

            // Gestion Potions Sorcière
            if (title.equals("§dPotions de la Sorcière")) {
                if (clicked.getType() == Material.GLISTERING_MELON_SLICE) {
                    player.sendMessage("§aTu as utilisé ta Potion de Vie !");
                    gm.sorciereUseLifePotion();
                    player.closeInventory();
                } else if (clicked.getType() == Material.SPIDER_EYE) {
                    fr.vynx.loupgarou.menu.VoteMenu.openSorciereKillMenu(player);
                } else if (clicked.getType() == Material.PAPER) {
                    player.sendMessage("§fTu décides de ne rien faire.");
                    player.closeInventory();
                    gm.nextNightPhase();
                }
                return;
            }

            if (clicked.getType() == Material.PLAYER_HEAD) {
                String targetName = clicked.getItemMeta().getDisplayName().substring(2);
                Player target = Bukkit.getPlayerExact(targetName);
                if (target == null) return;

                if (title.equals("§cQui dévorer ce soir ?")) {
                    player.sendMessage("§aTu as voté (Loup) contre §e" + targetName);
                    gm.registerWolfVote(player.getUniqueId(), target.getUniqueId());

                } else if (title.equals("§eVote du Village")) {
                    player.sendMessage("§aTu as voté (Village) contre §e" + targetName);
                    gm.registerDayVote(player.getUniqueId(), target.getUniqueId());

                } else if (title.equals("§6Élection du Maire")) {
                    player.sendMessage("§aTu as voté pour élire §6" + targetName);
                    gm.registerElectionVote(player.getUniqueId(), target.getUniqueId());

                } else if (title.equals("§6Choix du Successeur")) {
                    player.sendMessage("§aTu as désigné §6" + targetName);
                    gm.setMayor(target.getUniqueId());
                    player.closeInventory();

                } else if (title.equals("§dVision de la Voyante")) {
                    Role myRole = gm.getPlayerRole(player);
                    if (myRole instanceof RoleVoyante) {
                        Role targetRole = gm.getPlayerRole(target);
                        player.sendMessage("§dVision : §e" + target.getName() + " §dest " + targetRole.getName());
                        player.closeInventory();
                        gm.nextNightPhase();
                    }

                } else if (title.equals("§cQui assassiner ?")) {
                    player.sendMessage("§cTu as jeté ta Potion de Mort sur §e" + targetName);
                    gm.sorciereUseDeathPotion(target.getUniqueId());

                } else if (title.contains("Flèche de Cupidon")) {
                    Role myRole = gm.getPlayerRole(player);
                    if (myRole instanceof RoleCupidon) {
                        RoleCupidon cup = (RoleCupidon) myRole;
                        if (cup.getFirstLover() == null) {
                            cup.setFirstLover(target.getUniqueId());
                            player.sendMessage("§dPremier amoureux choisi. Choisis le deuxième !");
                            fr.vynx.loupgarou.menu.VoteMenu.openCupidonMenu(player, "§dFlèche de Cupidon (2/2)");
                        } else {
                            if (cup.getFirstLover().equals(target.getUniqueId())) {
                                player.sendMessage("§cTu dois choisir un joueur différent !");
                                return;
                            }
                            player.sendMessage("§dLes amoureux sont liés pour la vie !");
                            player.closeInventory();
                            gm.setLovers(cup.getFirstLover(), target.getUniqueId());
                        }
                    }
                } else if (title.equals("§2Dernier tir du Chasseur !")) {
                    player.sendMessage("§2PAN ! Tu as abattu §e" + targetName);
                    player.closeInventory();
                    gm.chasseurShoot(target.getUniqueId());
                }

                if (!title.contains("Flèche de Cupidon")) player.closeInventory();
            }
        }
    }
}