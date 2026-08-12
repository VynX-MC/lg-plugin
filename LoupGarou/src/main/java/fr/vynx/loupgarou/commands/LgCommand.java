package fr.vynx.loupgarou.commands;

import fr.vynx.loupgarou.MainLoupGarou;
import fr.vynx.loupgarou.manager.GameManager;
import fr.vynx.loupgarou.menu.VoteMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LgCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seul un joueur peut utiliser cette commande !");
            return true;
        }

        Player player = (Player) sender;
        GameManager gm = MainLoupGarou.getInstance().getGameManager();

        if (args.length == 0) {
            player.sendMessage("§e--- Commandes Loup-Garou ---");
            player.sendMessage("§7/lg join §f- Rejoindre la partie");
            player.sendMessage("§7/lg leave §f- Quitter la partie");
            player.sendMessage("§7/lg vote §f- Voter le jour (ou Maire)");
            if (player.isOp()) player.sendMessage("§7/lg start §f- Lancer la partie");
            return true;
        }

        if (args[0].equalsIgnoreCase("join")) {
            gm.addPlayer(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("leave")) {
            gm.removePlayer(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("start") && player.isOp()) {
            gm.startGame();
            return true;
        }

        if (args[0].equalsIgnoreCase("vote")) {
            if (!gm.getPlayers().contains(player.getUniqueId())) {
                player.sendMessage("§cTu n'es pas dans la partie ou tu es mort !");
                return true;
            }
            if (gm.getState() == GameManager.GameState.ELECTION) {
                VoteMenu.openElectionMenu(player);
            } else if (gm.getState() == GameManager.GameState.DAY) {
                VoteMenu.openDayVoteMenu(player);
            } else {
                player.sendMessage("§cCe n'est pas le moment de voter !");
            }
            return true;
        }

        player.sendMessage("§cCommande inconnue. Tape /lg pour l'aide.");
        return true;
    }
}