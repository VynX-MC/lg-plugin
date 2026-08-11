package fr.vynx.loupgarou.menu;

import fr.vynx.loupgarou.MainLoupGarou;
import fr.vynx.loupgarou.roles.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class VoteMenu {

    // --- MENUS CLASSIQUES ---
    public static void openWolfMenu(Player wolf) { openTargetMenu(wolf, "§cQui dévorer ce soir ?", false); }
    public static void openDayVoteMenu(Player voter) { openTargetMenu(voter, "§eVote du Village", true); }
    public static void openElectionMenu(Player voter) { openTargetMenu(voter, "§6Élection du Maire", true); }
    public static void openSuccessionMenu(Player deadMayor) { openTargetMenu(deadMayor, "§6Choix du Successeur", false); }
    public static void openVoyanteMenu(Player voyante) { openTargetMenu(voyante, "§dVision de la Voyante", false); }
    public static void openGardeMenu(Player garde) { openTargetMenu(garde, "§3Qui protéger ?", true); }
    public static void openAssassinMenu(Player assassin) { openTargetMenu(assassin, "§4Qui assassiner ?", false); }
    public static void openCupidonMenu(Player cupidon, String title) { openTargetMenu(cupidon, title, true); }
    public static void openChasseurMenu(Player chasseur) { openTargetMenu(chasseur, "§2Dernier tir du Chasseur !", false); }

    // --- NOUVEAUX MENUS CIBLÉS ---
    public static void openVampireMenu(Player vampire) { openTargetMenu(vampire, "§5Qui mordre ce soir ?", false); }
    public static void openFluteMenu(Player flute, String title) { openTargetMenu(flute, title, false); }
    public static void openPyromaneAspergerMenu(Player pyro) { openTargetMenu(pyro, "§6Qui asperger d'essence ?", false); }

    public static void openLoupBlancMenu(Player loupBlanc) {
        Inventory inv = Bukkit.createInventory(null, 27, "§fTrahir un Loup ?");
        int slot = 0;
        for (UUID targetId : MainLoupGarou.getInstance().getGameManager().getPlayers()) {
            Player target = Bukkit.getPlayer(targetId);
            Role role = MainLoupGarou.getInstance().getGameManager().getPlayerRole(target);
            if (target != null && !target.equals(loupBlanc) && role.getName().contains("Loup")) {
                inv.setItem(slot++, getPlayerHead(target));
            }
        }
        inv.setItem(26, createItem(Material.PAPER, "§fNe rien faire"));
        loupBlanc.openInventory(inv);
    }

    // --- MENUS À CHOIX MULTIPLES ---
    public static void openSorciereMenu(Player sorciere, String victimName, boolean hasLife, boolean hasDeath) {
        Inventory inv = Bukkit.createInventory(null, 27, "§dPotions de la Sorcière");
        inv.setItem(11, createItem(hasLife && victimName != null ? Material.GLISTERING_MELON_SLICE : Material.BARRIER, hasLife && victimName != null ? "§aSauver " + victimName : "§cPlus de Potion"));
        inv.setItem(13, createItem(Material.PAPER, "§fNe rien faire"));
        inv.setItem(15, createItem(hasDeath ? Material.SPIDER_EYE : Material.BARRIER, hasDeath ? "§cAssassiner quelqu'un" : "§cPlus de Potion de Mort"));
        sorciere.openInventory(inv);
    }
    public static void openSorciereKillMenu(Player sorciere) { openTargetMenu(sorciere, "§cQui assassiner ? (Sorcière)", false); }

    public static void openPyromaneMenu(Player pyro) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Action du Pyromane");
        inv.setItem(11, createItem(Material.WATER_BUCKET, "§eAsperger un joueur"));
        inv.setItem(13, createItem(Material.PAPER, "§fNe rien faire"));
        inv.setItem(15, createItem(Material.FLINT_AND_STEEL, "§cTout enflammer !"));
        pyro.openInventory(inv);
    }

    public static void openChienLoupMenu(Player chien) {
        Inventory inv = Bukkit.createInventory(null, 27, "§bChoisis ton camp");
        inv.setItem(11, createItem(Material.EMERALD, "§aRester Villageois"));
        inv.setItem(15, createItem(Material.BONE, "§cDevenir Loup-Garou"));
        chien.openInventory(inv);
    }

    public static void openVoleurMenu(Player voleur, Role carte1, Role carte2) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Choisis ton nouveau rôle");
        inv.setItem(11, createItem(Material.PAPER, carte1.getName()));
        inv.setItem(15, createItem(Material.PAPER, carte2.getName()));
        voleur.openInventory(inv);
    }

    // --- UTILITAIRES ---
    private static void openTargetMenu(Player viewer, String title, boolean showSelf) {
        Inventory inv = Bukkit.createInventory(null, 27, title);
        int slot = 0;
        for (UUID targetId : MainLoupGarou.getInstance().getGameManager().getPlayers()) {
            Player target = Bukkit.getPlayer(targetId);
            if (target != null && (showSelf || !target.equals(viewer))) {
                inv.setItem(slot++, getPlayerHead(target));
            }
        }
        viewer.openInventory(inv);
    }

    private static ItemStack getPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName("§e" + player.getName());
            head.setItemMeta(meta);
        }
        return head;
    }

    private static ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }
}