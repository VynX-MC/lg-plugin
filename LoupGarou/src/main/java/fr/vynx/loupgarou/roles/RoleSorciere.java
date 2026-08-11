package fr.vynx.loupgarou.roles;
import org.bukkit.entity.Player;
public class RoleSorciere extends Role {
    private boolean lifePotion = true, deathPotion = true;
    public RoleSorciere(Player p) { super(p); }
    public String getName() { return "§dSorcière"; }
    public String getDescription() { return "§7Tu as une potion de vie et une de mort (1 utilisation chacune)."; }
    public void onNightAction() {}
    public boolean hasLifePotion() { return lifePotion; }
    public void useLifePotion() { lifePotion = false; }
    public boolean hasDeathPotion() { return deathPotion; }
    public void useDeathPotion() { deathPotion = false; }
}