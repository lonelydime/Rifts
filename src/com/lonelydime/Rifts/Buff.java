package com.lonelydime.Rifts;
//make invis show a different message than the 5%
import org.bukkit.ChatColor;

public class Buff {
	public static Rifts plugin;
	Character character;
	String type;
	String name;
	int buffId;
	short origStr;
	short origDef;
	short origSpr;
	short origInt;
	short origDex;
	short origAgl;
	short origMP;
	boolean origSneak;
	
	public Buff(Character chara, String buffName, String buffType, float amount, int time, Rifts instance) {
		plugin = instance;
		this.character = chara;
		this.type = buffType;
		this.name = buffName;
		this.origStr = this.character.getStr();
		this.origDef = this.character.getDef();
		this.origSpr = this.character.getSpr();
		this.origInt = this.character.getInt();
		this.origDex = this.character.getDex();
		this.origAgl = this.character.getAgl();
		this.origMP = this.character.getTotalMana();
		this.origSneak = this.character.isSneaking();
		
		if (this.type.matches("strength")) {
			this.character.setStr((short)Math.round(origStr+(origStr*amount)));
		}
		else if (this.type.matches("defense")) {
			this.character.setDef((short)Math.round(origDef+(origDef*amount)));
		}
		else if (this.type.matches("spirit")) {
			this.character.setSpr((short)Math.round(origSpr+(origSpr*amount)));	
		}
		else if (this.type.matches("intelligence")) {
			this.character.setInt((short)Math.round(origInt+(origInt*amount)));
		}
		else if (this.type.matches("mana")) {
			this.character.setTotalMana((short)Math.round(origMP+(origMP*amount)));
		}
		else if (this.type.matches("dexterity")) {
			this.character.setDex((short)Math.round(origDex+(origDex*amount)));
		}
		else if (this.type.matches("agility")) {
			this.character.setAgl((short)Math.round(origAgl+(origAgl*amount)));
		}
		else if (this.type.matches("sneak")) {
			this.character.getPlayer().setSneaking(true);
			this.character.setSneaking(true);
		}
		else if (this.type.matches("all")) {
			this.character.setAgl((short)Math.round(origAgl+(origAgl*amount)));
			this.character.setDex((short)Math.round(origDex+(origDex*amount)));
			this.character.setTotalMana((short)Math.round(origMP+(origMP*amount)));
			this.character.setInt((short)Math.round(origInt+(origInt*amount)));
			this.character.setSpr((short)Math.round(origSpr+(origSpr*amount)));
			this.character.setDef((short)Math.round(origDef+(origDef*amount)));
			this.character.setStr((short)Math.round(origStr+(origStr*amount)));
		}
		else if (this.type.matches("invinc")) {
			this.character.setInvincible(true);
		}
		
		if (!this.type.matches("sneak") && !this.type.matches("all"))
			this.character.getPlayer().sendMessage(ChatColor.GREEN+"Your "+this.type+" has increased by "+(amount*100)+"%!");
		else if (this.type.matches("all")) {
			this.character.getPlayer().sendMessage(ChatColor.GREEN+"All of your stats has been increased by "+(amount*100)+"%!");
		}
		
		final Buff tempBuff = this;
		this.buffId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
		    public void run() {
		    	tempBuff.removeBuff();
		    	plugin.deleteBuff(tempBuff);
		    }
		}, time);
	}
	
	public void removeBuff() {
		this.character.setStr(this.origStr);
		this.character.setDef(this.origDef);
		this.character.setSpr(this.origSpr);
		this.character.setInt(this.origInt);
		this.character.setDex(this.origDex);
		this.character.setAgl(this.origAgl);
		this.character.setTotalMana(this.origMP);
		this.character.getPlayer().setSneaking(false);
		this.character.setSneaking(this.origSneak);
		this.character.setInvincible(false);
		character.removeBuff(this);
		character.getPlayer().sendMessage(ChatColor.YELLOW+"Your "+this.type+" buff has expired!");
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getBuffId() {
		return this.buffId;
	}
}
