package fr.vynx.loupgarou.menu;

import fr.vynx.loupgarou.MainLoupGarou;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class VoteMenu {

    public static void openWolfMenu(Player wolf) {
        Inventory inv = Bukkit.createInventory(null, 27, "§cQui dévorer ce soir ?");
        populateMenu(inv, wolf, false);
        wolf.openInventory(inv);
    }

    public static void openDayVoteMenu(Player voter) {
        Inventory inv = Bukkit.createInventory(null, 27, "§eVote du Village");
        populateMenu(inv, voter, true);
        voter.openInventory(inv);
    }

    public static void openElectionMenu(Player voter) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Élection du Maire");
        populateMenu(inv, voter, true);
        voter.openInventory(inv);
    }

    public static void openSuccessionMenu(Player deadMayor) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Choix du Successeur");
        populateMenu(inv, deadMayor, false);
        deadMayor.openInventory(inv);
    }

    // NOUVEAU : Menu de la Voyante
    public static void openVoyanteMenu(Player voyante) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dVision de la Voyante");
        populateMenu(inv, voyante, false);
        voyante.openInventory(inv);
    }

    // NOUVEAU : Menu principal de la Sorcière
    public static void openSorciereMenu(Player sorciere, String victimName, boolean hasLife, boolean hasDeath) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dPotions de la Sorcière");

        // Bouton Potion de Vie
        ItemStack life = new ItemStack(hasLife && victimName != null ? Material.GLISTERING_MELON_SLICE : Material.BARRIER);
        ItemMeta lifeMeta = life.getItemMeta();
        if (hasLife && victimName != null) {
            lifeMeta.setDisplayName("§aSauver " + victimName + " (Potion de Vie)");
        } else {
            lifeMeta.setDisplayName("§cPlus de Potion de Vie (ou pas de victime)");
        }
        life.setItemMeta(lifeMeta);
        inv.setItem(11, life);

        // Bouton Ne rien faire
        ItemStack skip = new ItemStack(Material.PAPER);
        ItemMeta skipMeta = skip.getItemMeta();
        skipMeta.setDisplayName("§fNe rien faire");
        skip.setItemMeta(skipMeta);
        inv.setItem(13, skip);

        // Bouton Potion de Mort
        ItemStack death = new ItemStack(hasDeath ? Material.SPIDER_EYE : Material.BARRIER);
        ItemMeta deathMeta = death.getItemMeta();
        if (hasDeath) {
            deathMeta.setDisplayName("§cAssassiner quelqu'un (Potion de Mort)");
        } else {
            deathMeta.setDisplayName("§cPlus de Potion de Mort");
        }
        death.setItemMeta(deathMeta);
        inv.setItem(15, death);

        sorciere.openInventory(inv);
    }

    // NOUVEAU : Menu pour choisir qui tuer avec la potion
    public static void openSorciereKillMenu(Player sorciere) {
        Inventory inv = Bukkit.createInventory(null, 27, "§cQui assassiner ?");
        populateMenu(inv, sorciere, false);
        sorciere.openInventory(inv);
    }

    public static void openCupidonMenu(Player cupidon, String title) {
        Inventory inv = Bukkit.createInventory(null, 27, title);
        populateMenu(inv, cupidon, true); // True : Il peut se choisir lui-même !
        cupidon.openInventory(inv);
    }

    public static void openChasseurMenu(Player chasseur) {
        Inventory inv = Bukkit.createInventory(null, 27, "§2Dernier tir du Chasseur !");
        populateMenu(inv, chasseur, false);
        chasseur.openInventory(inv);
    }

    private static void populateMenu(Inventory inv, Player viewer, boolean showSelf) {
        int slot = 0;
        for (UUID targetId : MainLoupGarou.getInstance().getGameManager().getPlayers()) {
            Player target = Bukkit.getPlayer(targetId);
            if (target != null && (showSelf || !target.equals(viewer))) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(target);
                    meta.setDisplayName("§e" + target.getName());
                    head.setItemMeta(meta);
                }
                inv.setItem(slot, head);
                slot++;
            }
        }
    }
}