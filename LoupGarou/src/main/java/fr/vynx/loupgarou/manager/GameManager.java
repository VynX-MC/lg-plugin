package fr.vynx.loupgarou.manager;

import fr.vynx.loupgarou.MainLoupGarou;
import fr.vynx.loupgarou.menu.VoteMenu;
import fr.vynx.loupgarou.roles.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class GameManager {

    private final List<UUID> players = new ArrayList<>();
    private GameState state = GameState.WAITING;
    private final Map<UUID, Role> playerRoles = new HashMap<>();

    private final Map<UUID, UUID> wolfVotes = new HashMap<>();
    private final Map<UUID, UUID> dayVotes = new HashMap<>();
    private final Map<UUID, UUID> electionVotes = new HashMap<>();

    private UUID mayor = null;
    private boolean isFirstDay = true;

    // VARIABLES DES RÔLES SPÉCIAUX
    private UUID lover1 = null;
    private UUID lover2 = null;
    private boolean isKillingLover = false; // Sécurité anti-boucle infinie
    private boolean isPausedForChasseur = false;

    public enum NightPhase { CUPIDON, VOYANTE, LOUPS, SORCIERE }
    private final Queue<NightPhase> nightQueue = new LinkedList<>();

    private UUID currentWolfVictim = null;
    private UUID currentSorciereVictim = null;
    private boolean isWolfVictimSaved = false;

    public void addPlayer(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage("§a[Loup-Garou] Tu as rejoint la partie ! (" + players.size() + " joueurs)");
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        playerRoles.remove(player.getUniqueId());
    }

    public void startGame() {
        if (state != GameState.WAITING) return;
        if (players.size() < 2) { Bukkit.broadcastMessage("§cPas assez de joueurs"); return; }

        Bukkit.broadcastMessage("§6La partie commence !");
        List<UUID> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);

        for (int i = 0; i < shuffled.size(); i++) {
            UUID uuid = shuffled.get(i);
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            Role role;
            if (i == 0) role = new RoleLoupGarou(player);
            else if (i == 1 && players.size() >= 3) role = new RoleVoyante(player);
            else if (i == 2 && players.size() >= 4) role = new RoleSorciere(player);
            else if (i == 3 && players.size() >= 5) role = new RoleChasseur(player);
            else if (i == 4 && players.size() >= 6) role = new RoleCupidon(player);
            else if (i == 5 && players.size() >= 7) role = new RolePetiteFille(player);
            else if (i == 6 && players.size() >= 8) role = new RoleLoupGarou(player);
            else role = new RoleVillageois(player);

            playerRoles.put(uuid, role);
            player.sendMessage("§8=============================");
            player.sendMessage("§fTon rôle : " + role.getName());
            player.sendMessage(role.getDescription());
            player.sendMessage("§8=============================");
        }

        isFirstDay = true;
        mayor = null;
        lover1 = null;
        lover2 = null;
        setState(GameState.NIGHT);
    }

    // ==========================================
    // MÉCANIQUE DE LA NUIT
    // ==========================================

    private void startNight() {
        Bukkit.broadcastMessage("§8La nuit tombe sur le village... Tout le monde ferme les yeux.");
        wolfVotes.clear();
        currentWolfVictim = null; currentSorciereVictim = null; isWolfVictimSaved = false;

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.getWorld().setTime(18000);

                // LA PETITE FILLE NE REÇOIT PAS LA CÉCITÉ !
                if (playerRoles.get(uuid) instanceof RolePetiteFille) {
                    p.sendMessage("§9Tu restes éveillée pour espionner dans la pénombre...");
                } else {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 1, false, false));
                }
            }
        }

        nightQueue.clear();
        if (isFirstDay && isRoleAlive(RoleCupidon.class)) nightQueue.add(NightPhase.CUPIDON);
        if (isRoleAlive(RoleVoyante.class)) nightQueue.add(NightPhase.VOYANTE);
        nightQueue.add(NightPhase.LOUPS);
        if (isRoleAlive(RoleSorciere.class)) nightQueue.add(NightPhase.SORCIERE);

        Bukkit.getScheduler().runTaskLater(MainLoupGarou.getInstance(), this::nextNightPhase, 60L);
    }

    public void nextNightPhase() {
        if (nightQueue.isEmpty()) { finishNight(); return; }

        NightPhase phase = nightQueue.poll();

        if (phase == NightPhase.CUPIDON) {
            Bukkit.broadcastMessage("§dCupidon se réveille...");
            Player cup = getPlayerByRole(RoleCupidon.class);
            if (cup != null) VoteMenu.openCupidonMenu(cup, "§dFlèche de Cupidon (1/2)");
            else nextNightPhase();

        } else if (phase == NightPhase.VOYANTE) {
            Bukkit.broadcastMessage("§dLa Voyante se réveille...");
            Player voyante = getPlayerByRole(RoleVoyante.class);
            if (voyante != null) VoteMenu.openVoyanteMenu(voyante);
            else nextNightPhase();

        } else if (phase == NightPhase.LOUPS) {
            Bukkit.broadcastMessage("§cLes Loups-Garous se réveillent...");
            boolean wolvesWokenUp = false;
            for (UUID uuid : players) {
                if (playerRoles.get(uuid) instanceof RoleLoupGarou) {
                    Player loup = Bukkit.getPlayer(uuid);
                    if (loup != null) { VoteMenu.openWolfMenu(loup); wolvesWokenUp = true; }
                }
            }
            if (!wolvesWokenUp) nextNightPhase();

        } else if (phase == NightPhase.SORCIERE) {
            Player pSorciere = getPlayerByRole(RoleSorciere.class);
            RoleSorciere role = (RoleSorciere) getRoleInstance(RoleSorciere.class);
            if (pSorciere != null && role != null && (role.hasLifePotion() || role.hasDeathPotion())) {
                Bukkit.broadcastMessage("§dLa Sorcière se réveille...");
                String victimName = (currentWolfVictim != null) ? Bukkit.getPlayer(currentWolfVictim).getName() : null;
                VoteMenu.openSorciereMenu(pSorciere, victimName, role.hasLifePotion(), role.hasDeathPotion());
            } else nextNightPhase();
        }
    }

    public void setLovers(UUID l1, UUID l2) {
        this.lover1 = l1; this.lover2 = l2;
        Player p1 = Bukkit.getPlayer(l1); Player p2 = Bukkit.getPlayer(l2);
        if (p1 != null) p1.sendMessage("§dTu es tombé fou amoureux de " + p2.getName() + " !");
        if (p2 != null) p2.sendMessage("§dTu es tombé fou amoureux de " + p1.getName() + " !");
        Bukkit.broadcastMessage("§dCupidon a tiré ses flèches et se rendort...");
        Bukkit.getScheduler().runTaskLater(MainLoupGarou.getInstance(), this::nextNightPhase, 40L);
    }

    public void registerWolfVote(UUID wolfId, UUID targetId) {
        wolfVotes.put(wolfId, targetId);
        int aliveWolves = 0;
        for (UUID uuid : players) if (playerRoles.get(uuid) instanceof RoleLoupGarou) aliveWolves++;

        if (wolfVotes.size() >= aliveWolves) {
            currentWolfVictim = getMajorityVote(wolfVotes, false);
            wolfVotes.clear();
            Bukkit.broadcastMessage("§cLes Loups-Garous se rendorment...");
            Bukkit.getScheduler().runTaskLater(MainLoupGarou.getInstance(), this::nextNightPhase, 40L);
        }
    }

    public void sorciereUseLifePotion() {
        RoleSorciere role = (RoleSorciere) getRoleInstance(RoleSorciere.class);
        if (role != null) role.useLifePotion();
        isWolfVictimSaved = true;
        nextNightPhase();
    }

    public void sorciereUseDeathPotion(UUID target) {
        RoleSorciere role = (RoleSorciere) getRoleInstance(RoleSorciere.class);
        if (role != null) role.useDeathPotion();
        currentSorciereVictim = target;
        nextNightPhase();
    }

    private void finishNight() {
        if (currentWolfVictim != null && !isWolfVictimSaved) killPlayer(currentWolfVictim, "dévoré par les loups");
        else if (currentWolfVictim == null && currentSorciereVictim == null) Bukkit.broadcastMessage("§aPersonne n'est mort cette nuit !");
        else if (isWolfVictimSaved) Bukkit.broadcastMessage("§aLe village se réveille... et la Sorcière a sauvé une victime !");

        if (currentSorciereVictim != null) killPlayer(currentSorciereVictim, "assassiné par la Sorcière");

        evaluateGameFlow();
    }

    // ==========================================
    // RÉSOLUTION GLOBALE DU JEU (L'ARBITRE)
    // ==========================================

    public void evaluateGameFlow() {
        if (isPausedForChasseur) return; // Le jeu est en pause, on attend le tir !

        if (checkWinConditions()) return; // La partie est finie

        if (state == GameState.NIGHT) {
            if (isFirstDay) setState(GameState.ELECTION);
            else setState(GameState.DAY);
        } else if (state == GameState.DAY || state == GameState.ELECTION) {
            setState(GameState.NIGHT);
        }
    }

    public void killPlayer(UUID victimId, String raison) {
        Player pVictim = Bukkit.getPlayer(victimId);
        players.remove(victimId);

        if (pVictim != null) {
            pVictim.setGameMode(GameMode.SPECTATOR);
            pVictim.sendMessage("§cTu as été " + raison + ".");
            Role role = playerRoles.get(victimId);
            String mayorSuffix = victimId.equals(mayor) ? " §6§lET IL ÉTAIT LE MAIRE !" : "";
            Bukkit.broadcastMessage("§c§l" + pVictim.getName() + " §ca été " + raison + " ! Il était " + role.getName() + mayorSuffix);

            // ACTION DU CHASSEUR
            if (role instanceof RoleChasseur) {
                isPausedForChasseur = true;
                Bukkit.broadcastMessage("§2Le Chasseur a été tué ! Il arme son fusil dans un dernier souffle...");
                Bukkit.getScheduler().runTaskLater(MainLoupGarou.getInstance(), () -> VoteMenu.openChasseurMenu(pVictim), 40L);
            }

            // SUCCESSION DU MAIRE
            if (victimId.equals(mayor)) {
                mayor = null;
                Bukkit.getScheduler().runTaskLater(MainLoupGarou.getInstance(), () -> VoteMenu.openSuccessionMenu(pVictim), 60L);
            }

            // SUICIDE DES AMOUREUX
            if (!isKillingLover) {
                if (victimId.equals(lover1) && players.contains(lover2)) {
                    isKillingLover = true;
                    killPlayer(lover2, "mort de chagrin (Amoureux)");
                    isKillingLover = false;
                } else if (victimId.equals(lover2) && players.contains(lover1)) {
                    isKillingLover = true;
                    killPlayer(lover1, "mort de chagrin (Amoureux)");
                    isKillingLover = false;
                }
            }
        }
    }

    public void chasseurShoot(UUID targetId) {
        isPausedForChasseur = false; // On enlève la pause
        killPlayer(targetId, "abattu par le Chasseur");
        evaluateGameFlow(); // Le jeu reprend son cours normal
    }

    // ==========================================
    // RESTE DU CODE (Votes Jour / Utilitaires)
    // ==========================================

    public void registerElectionVote(UUID voterId, UUID targetId) {
        electionVotes.put(voterId, targetId);
        Bukkit.broadcastMessage("§eVote enregistré (" + electionVotes.size() + "/" + players.size() + ")");
        if (electionVotes.size() >= players.size()) {
            UUID elected = getMajorityVote(electionVotes, false);
            electionVotes.clear();
            setMayor(elected != null ? elected : players.get(0));
            isFirstDay = false;
            setState(GameState.DAY);
        }
    }

    public void setMayor(UUID newMayorId) {
        this.mayor = newMayorId;
        Player pMayor = Bukkit.getPlayer(newMayorId);
        if (pMayor != null) Bukkit.broadcastMessage("§6§l" + pMayor.getName() + " §eest le nouveau Maire !");
    }

    public void registerDayVote(UUID voterId, UUID targetId) {
        dayVotes.put(voterId, targetId);
        Bukkit.broadcastMessage("§eVote enregistré (" + dayVotes.size() + "/" + players.size() + ")");
        if (dayVotes.size() >= players.size()) {
            UUID victim = getMajorityVote(dayVotes, true);
            dayVotes.clear();
            if (victim != null) killPlayer(victim, "lynché par le village");
            else Bukkit.broadcastMessage("§eAucun accord trouvé. Personne n'est pendu !");

            evaluateGameFlow();
        }
    }

    private UUID getMajorityVote(Map<UUID, UUID> votes, boolean applyMayorBoost) {
        Map<UUID, Integer> counts = new HashMap<>();
        for (Map.Entry<UUID, UUID> entry : votes.entrySet()) {
            int weight = (applyMayorBoost && entry.getKey().equals(mayor)) ? 2 : 1;
            counts.put(entry.getValue(), counts.getOrDefault(entry.getValue(), 0) + weight);
        }
        UUID victim = null; int max = 0;
        for (Map.Entry<UUID, Integer> e : counts.entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); victim = e.getKey(); }
        }
        return victim;
    }

    private boolean checkWinConditions() {
        int wolves = 0, villagers = 0;
        for (UUID uuid : players) {
            if (playerRoles.get(uuid) instanceof RoleLoupGarou) wolves++;
            else villagers++;
        }
        if (wolves == 0) {
            Bukkit.broadcastMessage("§a§lVictoire du Village !");
            setState(GameState.ENDED);
            return true;
        } else if (wolves >= villagers) {
            Bukkit.broadcastMessage("§c§lVictoire des Loups-Garous !");
            setState(GameState.ENDED);
            return true;
        }
        return false;
    }

    public void setState(GameState newState) {
        this.state = newState;
        if (newState == GameState.NIGHT) startNight();
        else if (newState == GameState.ELECTION) {
            Bukkit.broadcastMessage("§6C'est le premier jour ! Votez pour le Maire avec §b/lg vote");
            for (UUID uuid : players) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) { p.getWorld().setTime(6000); p.removePotionEffect(PotionEffectType.BLINDNESS); }
            }
        }
        else if (newState == GameState.DAY) {
            Bukkit.broadcastMessage("§eLe soleil se lève ! Débattez et votez avec §b/lg vote");
            for (UUID uuid : players) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) { p.getWorld().setTime(6000); p.removePotionEffect(PotionEffectType.BLINDNESS); }
            }
        }
    }

    private boolean isRoleAlive(Class<? extends Role> roleClass) {
        for (UUID uuid : players) if (roleClass.isInstance(playerRoles.get(uuid))) return true;
        return false;
    }
    private Player getPlayerByRole(Class<? extends Role> roleClass) {
        for (UUID uuid : players) if (roleClass.isInstance(playerRoles.get(uuid))) return Bukkit.getPlayer(uuid);
        return null;
    }
    private Role getRoleInstance(Class<? extends Role> roleClass) {
        for (UUID uuid : players) if (roleClass.isInstance(playerRoles.get(uuid))) return playerRoles.get(uuid);
        return null;
    }

    public Role getPlayerRole(Player p) { return playerRoles.get(p.getUniqueId()); }
    public List<UUID> getPlayers() { return players; }
    public GameState getState() { return state; }
    public enum GameState { WAITING, STARTING, ELECTION, DAY, NIGHT, ENDED }
}