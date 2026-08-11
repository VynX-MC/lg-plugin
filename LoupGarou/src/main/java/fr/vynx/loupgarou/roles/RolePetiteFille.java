package fr.vynx.loupgarou.roles;

import org.bukkit.entity.Player;

public class RolePetiteFille extends Role {
    public RolePetiteFille(Player player) { super(player); }

    @Override
    public String getName() { return "§9Petite Fille"; }

    @Override
    public String getDescription() {
        return "§7Tu peux espionner les Loups-Garous pendant la nuit, mais attention à ne pas te faire repérer !";
    }

    @Override
    public void onNightAction() {
        getPlayer().sendMessage("§9Tu entrouvres les yeux... Tu entends les loups roder.");
    }
}