package fr.vynx.loupgarou.manager;

import fr.vynx.loupgarou.roles.Role;
import fr.vynx.loupgarou.roles.RoleLoupGarou;
import fr.vynx.loupgarou.roles.RoleVillageois;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final List<UUID> players = new ArrayList<>();
    private GameState state = GameState.WAITING;
    private final Map<UUID, Role> playerRoles = new HashMap<>();

    // NOUVEAU : Dictionnaire pour stocker qui a voté pour qui (Loup -> Victime)
    private final Map<UUID, UUID> wolfVotes = new HashMap<>();

    public void addPlayer(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
            player.setGameMode(GameMode.ADVENTURE); // On les met en aventure par défaut
            player.sendMessage("§a[Loup-Garou] Tu as rejoint la partie ! (" + players.size() + " joueurs)");
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        playerRoles.remove(player.getUniqueId());
        wolfVotes.remove(player.getUniqueId());
    }

    public void startGame() {
        if (state != GameState.WAITING) return;
        if (players.size() < 2) {
            Bukkit.broadcastMessage("§c[Loup-Garou] Pas assez de joueurs pour lancer (Minimum 2)");
            return;
        }

        Bukkit.broadcastMessage("§6[Loup-Garou] La partie commence !");

        List<UUID> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        for (int i = 0; i < shuffledPlayers.size(); i++) {
            UUID uuid = shuffledPlayers.get(i);
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            Role role = (i == 0) ? new RoleLoupGarou(player) : new RoleVillageois(player);
            playerRoles.put(uuid, role);

            player.sendMessage("§8=============================");
            player.sendMessage("§fTon rôle : " + role.getName());
            player.sendMessage(role.getDescription());
            player.sendMessage("§8=============================");
        }

        setState(GameState.NIGHT);
    }

    // ----------------------------------------------------
    // NOUVEAU : GESTION DES VOTES ET DES MORTS
    // ----------------------------------------------------

    public void registerWolfVote(UUID wolfId, UUID targetId) {
        wolfVotes.put(wolfId, targetId);

        // On compte combien de loups sont encore en vie
        int aliveWolves = 0;
        for (UUID uuid : players) {
            if (playerRoles.get(uuid) instanceof RoleLoupGarou) aliveWolves++;
        }

        // Si tous les loups ont voté, on termine la nuit
        if (wolfVotes.size() >= aliveWolves) {
            processNightResults();
        }
    }

    private void processNightResults() {
        // Décompte des votes
        Map<UUID, Integer> voteCount = new HashMap<>();
        for (UUID targetId : wolfVotes.values()) {
            voteCount.put(targetId, voteCount.getOrDefault(targetId, 0) + 1);
        }

        // Trouver qui a le plus de votes
        UUID victim = null;
        int maxVotes = 0;
        for (Map.Entry<UUID, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                victim = entry.getKey();
            }
        }

        wolfVotes.clear(); // On vide les votes pour la nuit suivante

        // Annonce du résultat et passage au jour
        if (victim != null) {
            Player pVictim = Bukkit.getPlayer(victim);
            players.remove(victim); // Le joueur n'est plus dans la liste des vivants

            if (pVictim != null) {
                pVictim.setGameMode(GameMode.SPECTATOR); // Il devient un fantôme
                pVictim.sendMessage("§cTu as été dévoré pendant la nuit... Tu es maintenant spectateur.");
                Bukkit.broadcastMessage("§cLe village se réveille, mais il manque quelqu'un... §e" + pVictim.getName() + " §ca été dévoré !");
            }
        } else {
            Bukkit.broadcastMessage("§aLe village se réveille et miracle, personne n'est mort cette nuit !");
        }

        setState(GameState.DAY);
        checkWinConditions(); // On regarde si la partie est terminée
    }

    private void checkWinConditions() {
        int wolves = 0;
        int villagers = 0;

        for (UUID uuid : players) {
            if (playerRoles.get(uuid) instanceof RoleLoupGarou) wolves++;
            else villagers++;
        }

        if (wolves == 0) {
            Bukkit.broadcastMessage("§a§lVictoire des Villageois ! Les loups ont été éradiqués.");
            setState(GameState.ENDED);
        } else if (wolves >= villagers) {
            Bukkit.broadcastMessage("§c§lVictoire des Loups-Garous ! Ils contrôlent désormais le village.");
            setState(GameState.ENDED);
        }
    }

    // ----------------------------------------------------
    // GESTION DU JOUR ET DE LA NUIT (Modifié)
    // ----------------------------------------------------

    public void setState(GameState newState) {
        this.state = newState;
        if (newState == GameState.NIGHT) startNight();
        else if (newState == GameState.DAY) startDay();
    }

    private void startNight() {
        Bukkit.broadcastMessage("§8La nuit tombe sur le village... Tout le monde s'endort.");
        wolfVotes.clear(); // Sécurité : on efface les vieux votes

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getWorld().setTime(18000);
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 1, false, false));

                Role role = playerRoles.get(uuid);
                if (role != null) role.onNightAction();
            }
        }
    }

    private void startDay() {
        if (state == GameState.ENDED) return; // Si la partie est finie, on n'annonce pas le jour

        Bukkit.broadcastMessage("§eLe soleil se lève. Il est temps de débattre !");
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getWorld().setTime(6000);
                player.removePotionEffect(PotionEffectType.BLINDNESS);
            }
        }
    }

    public Role getPlayerRole(Player player) { return playerRoles.get(player.getUniqueId()); }
    public List<UUID> getPlayers() { return players; }
    public GameState getState() { return state; }

    public enum GameState {
        WAITING, STARTING, DAY, NIGHT, ENDED
    }
}