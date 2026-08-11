package fr.vynx.loupgarou.roles;

import org.bukkit.entity.Player;

public class RoleSorciere extends Role {

    private boolean hasLifePotion = true;
    private boolean hasDeathPotion = true;

    public RoleSorciere(Player player) { super(player); }

    @Override
    public String getName() { return "§dSorcière"; }

    @Override
    public String getDescription() {
        return "§7Tu possèdes une potion de vie et une de mort utilisables une seule fois dans la partie.";
    }

    @Override
    public void onNightAction() {
        // La sorcière ne joue pas tout de suite, elle doit attendre que les loups finissent !
        getPlayer().sendMessage("§8Attends que les loups fassent leur choix...");
    }

    public boolean hasLifePotion() { return hasLifePotion; }
    public void useLifePotion() { this.hasLifePotion = false; }

    public boolean hasDeathPotion() { return hasDeathPotion; }
    public void useDeathPotion() { this.hasDeathPotion = false; }
}