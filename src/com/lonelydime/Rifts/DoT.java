package com.lonelydime.Rifts;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DoT {
	public static Rifts plugin;
	LivingEntity entity;
	String damageName;
	int DoTID;
	int damageAmount;
	int damageTime;
	int totalDamage;
	int cancelDotId;
	
	public DoT(LivingEntity entityHit, String dotName, int dmgAmount, int time, Rifts instance) {
		plugin = instance;
		this.entity = entityHit;
		this.damageName = dotName;
		//percent based damage
		if (damageName.matches("diseased")) {
			this.damageAmount = Math.round(entityHit.getHealth() * (dmgAmount/100));
			if (this.damageAmount == 0)
				this.damageAmount = 1;
		}
		else {
			this.damageAmount = dmgAmount;
		}
		this.damageTime = time;
		this.totalDamage = damageAmount * (damageTime / 20);
		
		if (entity instanceof Player) {
			Player player = (Player)this.entity;
			player.sendMessage(ChatColor.GREEN+"You are "+dotName+"!");
			if (plugin.characters.containsKey(player)) {
				Character character = plugin.characters.get(player);
				character.addDot(this);
			}
		}
		
		final DoT tempDoT = this;
		this.DoTID = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
		    public void run() {
		    	if (tempDoT.entity != null) {
			    	if (tempDoT.entity.getHealth() > 0) {
			    		int newHealth = tempDoT.entity.getHealth()-tempDoT.damageAmount;
			    		if (newHealth < 0)
			    			newHealth = 0;
			    		if (newHealth == 0) {
			    			plugin.death(tempDoT.entity);	
			    			plugin.fixDrops(entity);
			    		}
			    		tempDoT.entity.setHealth(newHealth);
			    	}
		    	}
		    }
		}, 20, 20);
		
		final int cancelId = this.DoTID;
		
		cancelDotId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			public void run() {
				plugin.getServer().getScheduler().cancelTask(cancelId);
				
				if (tempDoT.entity instanceof Player) {
					Player player = (Player)entity;
					if (plugin.characters.containsKey(player)) {
						Character character = plugin.characters.get(player);
						character.removeDot(tempDoT);
					}
				}
			}
		}, damageTime);
	}
	
	public String getName() {
		return this.damageName;
	}
	
	public int getDotId() {
		return this.DoTID;
	}
	
	public void removeDot() {
		plugin.getServer().getScheduler().cancelTask(this.DoTID);
		plugin.getServer().getScheduler().cancelTask(this.cancelDotId);
		if (this.entity instanceof Player) {
			Player player = (Player)entity;
			if (plugin.characters.containsKey(player)) {
				Character character = plugin.characters.get(player);
				character.removeDot(this);
			}
		}
	}
}