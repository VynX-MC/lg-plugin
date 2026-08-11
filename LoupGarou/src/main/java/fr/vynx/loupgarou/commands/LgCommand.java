package fr.vynx.loupgarou.commands;

import fr.vynx.loupgarou.MainLoupGarou;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LgCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // On s'assure que c'est bien un joueur (et non la console du serveur) qui tape la commande
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seul un joueur dans le jeu peut utiliser cette commande !");
            return true;
        }

        Player player = (Player) sender;

        // Si le joueur tape juste "/lg" sans rien après
        if (args.length == 0) {
            player.sendMessage("§e--- Commandes Loup-Garou ---");
            player.sendMessage("§7/lg join §f- Rejoindre la partie d'attente");
            player.sendMessage("§7/lg leave §f- Quitter la partie");
            // NOUVEAU : On ajoute l'aide pour le start
            if (player.isOp()) {
                player.sendMessage("§7/lg start §f- Lancer la partie (Admin)");
            }
            return true;
        }

        // Si le joueur tape "/lg join"
        if (args[0].equalsIgnoreCase("join")) {
            MainLoupGarou.getInstance().getGameManager().addPlayer(player);
            return true;
        }

        // Si le joueur tape "/lg leave"
        if (args[0].equalsIgnoreCase("leave")) {
            MainLoupGarou.getInstance().getGameManager().removePlayer(player);
            return true;
        }

        // NOUVEAU : Si l'admin tape "/lg start"
        if (args[0].equalsIgnoreCase("start")) {
            if (player.isOp()) { // On vérifie qu'il a les permissions
                MainLoupGarou.getInstance().getGameManager().startGame();
            } else {
                player.sendMessage("§cTu n'as pas la permission de lancer la partie !");
            }
            return true;
        }

        player.sendMessage("§cCommande inconnue. Tape /lg pour voir l'aide.");
        return true;
    }
}