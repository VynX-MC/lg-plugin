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

    private UUID mayor = null, lover1 = null, lover2 = null;
    private boolean isFirstDay = true, isPausedForChasseur = false, isKillingLover = false;

    private UUID guardedPlayer = null, currentWolfVictim = null, currentSorciereVictim = null, currentAssassinVictim = null, currentLoupBlancVictim = null;
    private boolean isWolfVictimSaved = false, pyromaneIgnited = false;

    private final List<UUID> charmedPlayers = new ArrayList<>();
    private final List<UUID> dousedPlayers = new ArrayList<>();
    private final List<Role> voleurCards = new ArrayList<>();

    public enum NightPhase { VOLEUR, CHIEN_LOUP, CUPIDON, VOYANTE, GARDE, LOUPS, LOUP_BLANC, SORCIERE, ASSASSIN, VAMPIRE, PYROMANE, JOUEUR_FLUTE }
    private final Queue<NightPhase> nightQueue = new LinkedList<>();

    // --- SYSTÈME DE MENU FORCÉ ---
    private class PersistentMenu {
        String title;
        Runnable opener;
        PersistentMenu(String title, Runnable opener) {
            this.title = title;
            this.opener = opener;
        }
    }
    private final Map<UUID, PersistentMenu> activeMenus = new HashMap<>();
    private org.bukkit.scheduler.BukkitTask menuTask = null;

    private void setMenu(Player p, String title, Runnable opener) {
        activeMenus.put(p.getUniqueId(), new PersistentMenu(title, opener));
        Bukkit.getScheduler().runTaskLater(MainLoupGarou.getInstance(), opener, 10L);
    }

    public void addPlayer(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
            player.sendMessage("§aTu as rejoint la prochaine partie !");
        }
    }

    public void removePlayer(Player player) { players.remove(player.getUniqueId()); playerRoles.remove(player.getUniqueId()); }

    public void startGame() {
        if (state != GameState.WAITING) return;
        List<UUID> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);

        voleurCards.clear();
        voleurCards.add(new RoleVillageois(null));
        voleurCards.add(new RoleLoupGarou(null));

        for (int i = 0; i < shuffled.size(); i++) {
            UUID uuid = shuffled.get(i);
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            Role role;
            if (i == 0) role = new RoleLoupGarou(player);
            else if (i == 1) role = new RoleVoyante(player);
            else if (i == 2) role = new RoleVoleur(player);
            else if (i == 3) role = new RoleChienLoup(player);
            else if (i == 4) role = new RolePyromane(player);
            else if (i == 5) role = new RoleJoueurDeFlute(player);
            else if (i == 6) role = new RoleVampire(player);
            else if (i == 7) role = new RoleLoupGarouBlanc(player);
            else role = new RoleVillageois(player);

            playerRoles.put(uuid, role);
            player.sendMessage("§fTon rôle : " + role.getName());
        }

        isFirstDay = true;

        if (menuTask == null) {
            menuTask = Bukkit.getScheduler().runTaskTimer(MainLoupGarou.getInstance(), () -> {
                for (Map.Entry<UUID, PersistentMenu> entry : activeMenus.entrySet()) {
                    Player p = Bukkit.getPlayer(entry.getKey());
                    if (p != null && p.isOnline()) {
                        if (p.getOpenInventory() == null || !p.getOpenInventory().getTitle().equals(entry.getValue().title)) {
                            p.closeInventory();
                            entry.getValue().opener.run();
                        }
                    }
                }
            }, 20L, 40L);
        }

        setState(GameState.NIGHT);
    }

    private void startNight() {
        Bukkit.broadcastMessage("§8La nuit tombe...");
        wolfVotes.clear();
        currentWolfVictim = null; currentSorciereVictim = null; currentAssassinVictim = null; currentLoupBlancVictim = null;
        isWolfVictimSaved = false; guardedPlayer = null; pyromaneIgnited = false;
        activeMenus.clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getWorld().setTime(18000);
            Role r = getPlayerRole(p);
            if (r instanceof RolePetiteFille) {
                p.sendMessage("§9Tu espionnes dans la nuit...");
            } else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 255, false, false));
            }
        }

        nightQueue.clear();
        if (isFirstDay && isRoleAlive(RoleVoleur.class)) nightQueue.add(NightPhase.VOLEUR);
        if (isFirstDay && isRoleAlive(RoleChienLoup.class)) nightQueue.add(NightPhase.CHIEN_LOUP);
        if (isFirstDay && isRoleAlive(RoleCupidon.class)) nightQueue.add(NightPhase.CUPIDON);
        if (isRoleAlive(RoleGarde.class)) nightQueue.add(NightPhase.GARDE);
        if (isRoleAlive(RoleVoyante.class)) nightQueue.add(NightPhase.VOYANTE);
        nightQueue.add(NightPhase.LOUPS);
        if (isRoleAlive(RoleLoupGarouBlanc.class)) nightQueue.add(NightPhase.LOUP_BLANC);
        if (isRoleAlive(RoleSorciere.class)) nightQueue.add(NightPhase.SORCIERE);
        if (isRoleAlive(RoleAssassin.class)) nightQueue.add(NightPhase.ASSASSIN);
        if (isRoleAlive(RoleVampire.class)) nightQueue.add(NightPhase.VAMPIRE);
        if (isRoleAlive(RolePyromane.class)) nightQueue.add(NightPhase.PYROMANE);
        if (isRoleAlive(RoleJoueurDeFlute.class)) nightQueue.add(NightPhase.JOUEUR_FLUTE);

        Bukkit.getScheduler().runTaskLater(MainLoupGarou.getInstance(), this::nextNightPhase, 60L);
    }

    public void nextNightPhase() {
        activeMenus.clear();
        if (nightQueue.isEmpty()) { finishNight(); return; }
        NightPhase phase = nightQueue.poll();

        Player p;
        switch (phase) {
            case VOLEUR:
                Bukkit.broadcastMessage("§8Le Voleur se réveille...");
                p = getPlayerByRole(RoleVoleur.class);
                if (p != null) setMenu(p, "§8Choisis ton nouveau rôle", () -> VoteMenu.openVoleurMenu(p, voleurCards.get(0), voleurCards.get(1)));
                else nextNightPhase();
                break;
            case CHIEN_LOUP:
                Bukkit.broadcastMessage("§bLe Chien-Loup se réveille...");
                p = getPlayerByRole(RoleChienLoup.class);
                if (p != null) setMenu(p, "§bChoisis ton camp", () -> VoteMenu.openChienLoupMenu(p));
                else nextNightPhase();
                break;
            case CUPIDON:
                Bukkit.broadcastMessage("§dCupidon se réveille...");
                p = getPlayerByRole(RoleCupidon.class);
                if (p != null) setMenu(p, "§dFlèche (1/2)", () -> VoteMenu.openCupidonMenu(p, "§dFlèche (1/2)"));
                else nextNightPhase();
                break;
            case GARDE:
                Bukkit.broadcastMessage("§3Le Garde se réveille...");
                p = getPlayerByRole(RoleGarde.class);
                if (p != null) setMenu(p, "§3Qui protéger ?", () -> VoteMenu.openGardeMenu(p));
                else nextNightPhase();
                break;
            case VOYANTE:
                Bukkit.broadcastMessage("§dLa Voyante se réveille...");
                p = getPlayerByRole(RoleVoyante.class);
                if (p != null) setMenu(p, "§dVision de la Voyante", () -> VoteMenu.openVoyanteMenu(p));
                else nextNightPhase();
                break;
            case LOUPS:
                Bukkit.broadcastMessage("§cLes Loups se réveillent...");
                boolean w = false;
                for (UUID u : players) {
                    // C'est ici que l'instanceof corrige le bug des menus pour le Chien-Loup !
                    if (playerRoles.get(u) instanceof RoleLoupGarou) {
                        Player l = Bukkit.getPlayer(u);
                        if (l != null) { w = true; setMenu(l, "§cQui dévorer ce soir ?", () -> VoteMenu.openWolfMenu(l)); }
                    }
                }
                if (!w) nextNightPhase();
                break;
            case LOUP_BLANC:
                Bukkit.broadcastMessage("§fLe Loup-Garou Blanc se réveille...");
                p = getPlayerByRole(RoleLoupGarouBlanc.class);
                if (p != null) setMenu(p, "§fTrahir un Loup ?", () -> VoteMenu.openLoupBlancMenu(p));
                else nextNightPhase();
                break;
            case SORCIERE:
                Bukkit.broadcastMessage("§dLa Sorcière se réveille...");
                p = getPlayerByRole(RoleSorciere.class);
                if (p != null) {
                    RoleSorciere r = (RoleSorciere) getPlayerRole(p);
                    if (r.hasLifePotion() || r.hasDeathPotion()) {
                        String vic = (currentWolfVictim != null) ? Bukkit.getPlayer(currentWolfVictim).getName() : null;
                        Player finalP = p;
                        setMenu(p, "§dPotions de la Sorcière", () -> VoteMenu.openSorciereMenu(finalP, vic, r.hasLifePotion(), r.hasDeathPotion()));
                    } else nextNightPhase();
                } else nextNightPhase();
                break;
            case ASSASSIN:
                Bukkit.broadcastMessage("§4L'Assassin se réveille...");
                p = getPlayerByRole(RoleAssassin.class);
                if (p != null) setMenu(p, "§4Qui assassiner ?", () -> VoteMenu.openAssassinMenu(p));
                else nextNightPhase();
                break;
            case VAMPIRE:
                Bukkit.broadcastMessage("§5Le Vampire se réveille...");
                p = getPlayerByRole(RoleVampire.class);
                if (p != null) setMenu(p, "§5Qui mordre ce soir ?", () -> VoteMenu.openVampireMenu(p));
                else nextNightPhase();
                break;
            case PYROMANE:
                Bukkit.broadcastMessage("§6Le Pyromane se réveille...");
                p = getPlayerByRole(RolePyromane.class);
                if (p != null) setMenu(p, "§6Action du Pyromane", () -> VoteMenu.openPyromaneMenu(p));
                else nextNightPhase();
                break;
            case JOUEUR_FLUTE:
                Bukkit.broadcastMessage("§aLe Joueur de Flûte se réveille...");
                p = getPlayerByRole(RoleJoueurDeFlute.class);
                if (p != null) setMenu(p, "§aCharmer (1/2)", () -> VoteMenu.openFluteMenu(p, "§aCharmer (1/2)"));
                else nextNightPhase();
                break;
        }
    }

    public void voleurChoose(String roleName) {
        Player p = getPlayerByRole(RoleVoleur.class);
        if (p != null) {
            Role newRole = roleName.contains("Loup") ? new RoleLoupGarou(p) : new RoleVillageois(p);
            playerRoles.put(p.getUniqueId(), newRole);
            p.sendMessage("§8Tu es maintenant : " + newRole.getName());
        }
        nextNightPhase();
    }

    public void chienLoupChoose(boolean becomeWolf) {
        Player p = getPlayerByRole(RoleChienLoup.class);
        if (p != null && becomeWolf) {
            playerRoles.put(p.getUniqueId(), new RoleLoupGarou(p));
            p.sendMessage("§cTu as rejoint les Loups !");
        }
        nextNightPhase();
    }

    public void cupidonShoot(Player p, UUID tId, boolean isFirst) {
        if (isFirst) {
            lover1 = tId; p.closeInventory();
            setMenu(p, "§dFlèche (2/2)", () -> VoteMenu.openCupidonMenu(p, "§dFlèche (2/2)"));
        } else {
            activeMenus.remove(p.getUniqueId());
            lover2 = tId; p.closeInventory();
            Player p1 = Bukkit.getPlayer(lover1), p2 = Bukkit.getPlayer(lover2);
            if (p1 != null) p1.sendMessage("§dTu es amoureux de " + p2.getName());
            if (p2 != null) p2.sendMessage("§dTu es amoureux de " + p1.getName());
            nextNightPhase();
        }
    }

    public void fluteCharm(Player p, UUID tId, boolean isFirst) {
        if (!charmedPlayers.contains(tId)) charmedPlayers.add(tId);
        if (isFirst) {
            p.closeInventory();
            setMenu(p, "§aCharmer (2/2)", () -> VoteMenu.openFluteMenu(p, "§aCharmer (2/2)"));
        } else {
            activeMenus.remove(p.getUniqueId());
            p.closeInventory();
            for (UUID u : charmedPlayers) {
                Player cp = Bukkit.getPlayer(u);
                if (cp != null) cp.sendMessage("§aTu as été charmé par la mélodie...");
            }
            nextNightPhase();
        }
    }

    public void pyromaneAsperger(UUID tId) {
        if (!dousedPlayers.contains(tId)) dousedPlayers.add(tId);
        Player t = Bukkit.getPlayer(tId);
        if (t != null) t.sendMessage("§6Tu sens une forte odeur d'essence...");
        nextNightPhase();
    }

    public void pyromaneIgnite() { pyromaneIgnited = true; nextNightPhase(); }

    public void vampireBite(UUID tId) {
        Player t = Bukkit.getPlayer(tId);
        if (t != null) {
            playerRoles.put(tId, new RoleVampire(t));
            t.sendMessage("§5Tu as été mordu... Tu es maintenant Vampire !");
        }
        nextNightPhase();
    }

    public void loupBlancKill(UUID tId) { currentLoupBlancVictim = tId; nextNightPhase(); }
    public void registerGarde(UUID tId) { guardedPlayer = tId; nextNightPhase(); }
    public void registerAssassin(UUID tId) { currentAssassinVictim = tId; nextNightPhase(); }

    public void sorciereUseLifePotion() {
        RoleSorciere r = (RoleSorciere) getPlayerRole(getPlayerByRole(RoleSorciere.class));
        if (r != null) r.useLifePotion();
        isWolfVictimSaved = true; nextNightPhase();
    }

    public void sorciereUseDeathPotion(UUID tId) {
        RoleSorciere r = (RoleSorciere) getPlayerRole(getPlayerByRole(RoleSorciere.class));
        if (r != null) r.useDeathPotion();
        currentSorciereVictim = tId; nextNightPhase();
    }

    public void registerWolfVote(UUID wId, UUID tId) {
        activeMenus.remove(wId);
        wolfVotes.put(wId, tId);
        int l = 0;
        for (UUID u : players) {
            // C'est ici que l'instanceof empêche le comptage des faux loups pour le vote !
            if (playerRoles.get(u) instanceof RoleLoupGarou) l++;
        }
        if (wolfVotes.size() >= l) {
            currentWolfVictim = getMajorityVote(wolfVotes, false); wolfVotes.clear();
            Bukkit.getScheduler().runTaskLater(MainLoupGarou.getInstance(), this::nextNightPhase, 40L);
        }
    }

    public void registerDayVote(UUID vId, UUID tId) {
        dayVotes.put(vId, tId);
        if (dayVotes.size() >= players.size()) {
            UUID victim = getMajorityVote(dayVotes, true); dayVotes.clear();
            if (victim != null) {
                if (playerRoles.get(victim) instanceof RoleBouffon) {
                    Bukkit.broadcastMessage("§e§lLe Bouffon a été pendu ! Il gagne la partie !");
                    setState(GameState.ENDED); return;
                }
                killPlayer(victim, "lynché par le village");
            }
            evaluateGameFlow();
        }
    }

    public void registerElectionVote(UUID vId, UUID tId) {
        electionVotes.put(vId, tId);
        if (electionVotes.size() >= players.size()) {
            UUID elected = getMajorityVote(electionVotes, false); electionVotes.clear();
            setMayor(elected != null ? elected : players.get(0));
            isFirstDay = false; setState(GameState.DAY);
        }
    }

    private void finishNight() {
        activeMenus.clear();
        if (currentWolfVictim != null && currentWolfVictim.equals(guardedPlayer)) isWolfVictimSaved = true;
        if (currentWolfVictim != null && !isWolfVictimSaved) killPlayer(currentWolfVictim, "dévoré par les loups");
        if (currentLoupBlancVictim != null) killPlayer(currentLoupBlancVictim, "dévoré par le Loup Blanc");
        if (currentSorciereVictim != null) killPlayer(currentSorciereVictim, "assassiné par la sorcière");
        if (currentAssassinVictim != null) killPlayer(currentAssassinVictim, "tué par l'Assassin");
        if (pyromaneIgnited) {
            for (UUID u : dousedPlayers) if (players.contains(u)) killPlayer(u, "brûlé vif par le Pyromane");
            dousedPlayers.clear();
        }
        evaluateGameFlow();
    }

    public void evaluateGameFlow() {
        if (isPausedForChasseur || checkWinConditions()) return;
        if (state == GameState.NIGHT) setState(isFirstDay ? GameState.ELECTION : GameState.DAY);
        else setState(GameState.NIGHT);
    }

    public void killPlayer(UUID victimId, String raison) {
        Player pVictim = Bukkit.getPlayer(victimId);
        players.remove(victimId);
        charmedPlayers.remove(victimId);
        if (pVictim != null) {
            pVictim.setGameMode(GameMode.SPECTATOR);
            Bukkit.broadcastMessage("§c§l" + pVictim.getName() + " §ca été " + raison + " ! Rôle : " + playerRoles.get(victimId).getName());

            if (playerRoles.get(victimId) instanceof RoleChasseur) {
                isPausedForChasseur = true;
                setMenu(pVictim, "§2Dernier tir du Chasseur !", () -> VoteMenu.openChasseurMenu(pVictim));
            }
            if (victimId.equals(mayor)) {
                mayor = null;
                setMenu(pVictim, "§6Choix du Successeur", () -> VoteMenu.openSuccessionMenu(pVictim));
            }
            if (!isKillingLover) {
                if (victimId.equals(lover1) && players.contains(lover2)) { isKillingLover = true; killPlayer(lover2, "mort de chagrin"); isKillingLover = false; }
                else if (victimId.equals(lover2) && players.contains(lover1)) { isKillingLover = true; killPlayer(lover1, "mort de chagrin"); isKillingLover = false; }
            }
        }
    }

    public void chasseurShoot(UUID tId) { activeMenus.clear(); isPausedForChasseur = false; killPlayer(tId, "abattu par le Chasseur"); evaluateGameFlow(); }
    public void setMayor(UUID mId) { activeMenus.clear(); this.mayor = mId; Player p = Bukkit.getPlayer(mId); if (p != null) Bukkit.broadcastMessage("§6§l" + p.getName() + " §eest le Maire !"); }

    private UUID getMajorityVote(Map<UUID, UUID> votes, boolean applyMayor) {
        Map<UUID, Integer> c = new HashMap<>();
        for (Map.Entry<UUID, UUID> e : votes.entrySet()) c.put(e.getValue(), c.getOrDefault(e.getValue(), 0) + ((applyMayor && e.getKey().equals(mayor)) ? 2 : 1));
        UUID vic = null; int max = 0;
        for (Map.Entry<UUID, Integer> e : c.entrySet()) if (e.getValue() > max) { max = e.getValue(); vic = e.getKey(); }
        return vic;
    }

    private boolean checkWinConditions() {
        if (players.size() == 0) return true;

        boolean allCharmed = true;
        for (UUID u : players) { if (!charmedPlayers.contains(u) && !(playerRoles.get(u) instanceof RoleJoueurDeFlute)) allCharmed = false; }
        if (allCharmed && isRoleAlive(RoleJoueurDeFlute.class)) { Bukkit.broadcastMessage("§a§lLe Joueur de Flûte a charmé tout le monde !"); setState(GameState.ENDED); return true; }

        int w = 0, v = 0, vamp = 0, lb = 0, ass = 0, pyro = 0;
        for (UUID u : players) {
            Role r = playerRoles.get(u);
            if (r instanceof RoleVampire) vamp++;
            else if (r instanceof RoleLoupGarouBlanc) lb++;
            else if (r instanceof RoleAssassin) ass++;
            else if (r instanceof RolePyromane) pyro++;
                // L'instanceof sécurise enfin les conditions de victoire !
            else if (r instanceof RoleLoupGarou) w++;
            else v++;
        }

        if (vamp == players.size()) { Bukkit.broadcastMessage("§5§lVictoire des Vampires !"); setState(GameState.ENDED); return true; }
        if (lb == 1 && players.size() == 1) { Bukkit.broadcastMessage("§f§lVictoire du Loup Blanc !"); setState(GameState.ENDED); return true; }
        if (ass == 1 && players.size() == 1) { Bukkit.broadcastMessage("§4§lVictoire de l'Assassin !"); setState(GameState.ENDED); return true; }
        if (pyro == 1 && players.size() == 1) { Bukkit.broadcastMessage("§6§lVictoire du Pyromane !"); setState(GameState.ENDED); return true; }
        if (w == 0 && lb == 0 && vamp == 0 && ass == 0 && pyro == 0) { Bukkit.broadcastMessage("§a§lVictoire du Village !"); setState(GameState.ENDED); return true; }
        if (w >= v + vamp + lb + ass + pyro) { Bukkit.broadcastMessage("§c§lVictoire des Loups !"); setState(GameState.ENDED); return true; }

        return false;
    }

    public void resetGame() {
        this.state = GameState.WAITING;
        this.players.clear();
        this.playerRoles.clear();
        this.wolfVotes.clear();
        this.dayVotes.clear();
        this.electionVotes.clear();
        this.charmedPlayers.clear();
        this.dousedPlayers.clear();
        this.activeMenus.clear();

        this.mayor = null; this.lover1 = null; this.lover2 = null;
        this.guardedPlayer = null; this.currentWolfVictim = null;
        this.currentSorciereVictim = null; this.currentAssassinVictim = null;
        this.currentLoupBlancVictim = null;
        this.isWolfVictimSaved = false; this.pyromaneIgnited = false;
        this.isFirstDay = true; this.isPausedForChasseur = false; this.isKillingLover = false;

        for (Player p : Bukkit.getOnlinePlayers()) {
            addPlayer(p);
        }
        Bukkit.broadcastMessage("§a[Loup-Garou] Le jeu a été réinitialisé ! Tapez votre commande pour rejouer.");
    }

    public void setState(GameState newState) {
        this.state = newState;
        if (newState == GameState.NIGHT) {
            startNight();
        } else if (newState == GameState.ENDED) {
            activeMenus.clear();
            if (menuTask != null) { menuTask.cancel(); menuTask = null; }
            Bukkit.broadcastMessage("§eLa partie est terminée ! Réinitialisation dans 10 secondes...");

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.ADVENTURE);
                for (PotionEffect effect : p.getActivePotionEffects()) {
                    p.removePotionEffect(effect.getType());
                }
            }

            Bukkit.getScheduler().runTaskLater(MainLoupGarou.getInstance(), this::resetGame, 200L);

        } else {
            activeMenus.clear();
            Bukkit.broadcastMessage("§eLe jour se lève !");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.getWorld().setTime(6000);
                for (PotionEffect effect : p.getActivePotionEffects()) {
                    p.removePotionEffect(effect.getType());
                }
            }
            isFirstDay = false;
        }
    }

    private boolean isRoleAlive(Class<? extends Role> rc) { for (UUID u : players) if (rc.isInstance(playerRoles.get(u))) return true; return false; }
    private Player getPlayerByRole(Class<? extends Role> rc) { for (UUID u : players) if (rc.isInstance(playerRoles.get(u))) return Bukkit.getPlayer(u); return null; }
    public Role getPlayerRole(Player p) { return playerRoles.get(p.getUniqueId()); }
    public List<UUID> getPlayers() { return players; }
    public GameState getState() { return state; }
    public enum GameState { WAITING, STARTING, ELECTION, DAY, NIGHT, ENDED }
}