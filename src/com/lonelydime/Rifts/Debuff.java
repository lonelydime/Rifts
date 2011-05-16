package com.lonelydime.Rifts;

import org.bukkit.ChatColor;

public class Debuff {
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
	
	public Debuff(Character chara, String debuffName, String debuffType, float amount, int time, Rifts instance) {
		plugin = instance;
		this.character = chara;
		this.type = debuffType;
		this.name = debuffName;
		this.origStr = this.character.getStr();
		this.origDef = this.character.getDef();
		this.origSpr = this.character.getSpr();
		this.origInt = this.character.getInt();
		this.origDex = this.character.getDex();
		this.origAgl = this.character.getAgl();
		this.origMP = this.character.getTotalMana();
		
		if (this.type.matches("strength")) {
			this.character.setStr((short)Math.round(origStr-(origStr*amount)));
		}
		else if (this.type.matches("defense")) {
			this.character.setDef((short)Math.round(origDef-(origDef*amount)));
		}
		else if (this.type.matches("spirit")) {
			this.character.setSpr((short)Math.round(origSpr-(origSpr*amount)));	
		}
		else if (this.type.matches("intelligence")) {
			this.character.setInt((short)Math.round(origInt-(origInt*amount)));
		}
		else if (this.type.matches("mana")) {
			this.character.setTotalMana((short)Math.round(origMP-(origMP*amount)));
		}
		else if (this.type.matches("dexterity")) {
			this.character.setDex((short)Math.round(origDex-(origDex*amount)));
		}
		else if (this.type.matches("agility")) {
			this.character.setAgl((short)Math.round(origAgl-(origAgl*amount)));
		}
		else if (this.type.matches("all")) {
			this.character.setAgl((short)Math.round(origAgl-(origAgl*amount)));
			this.character.setDex((short)Math.round(origDex-(origDex*amount)));
			this.character.setTotalMana((short)Math.round(origMP-(origMP*amount)));
			this.character.setInt((short)Math.round(origInt-(origInt*amount)));
			this.character.setSpr((short)Math.round(origSpr-(origSpr*amount)));
			this.character.setDef((short)Math.round(origDef-(origDef*amount)));
			this.character.setStr((short)Math.round(origStr-(origStr*amount)));
		}
		else if (this.type.matches("weaken")) {
			this.character.setInt((short)Math.round(origInt-(origInt*amount)));
			this.character.setStr((short)Math.round(origStr-(origStr*amount)));
		}
		
		if (!this.type.matches("weaken") && !this.type.matches("all"))
			this.character.getPlayer().sendMessage(ChatColor.RED+"Your "+this.type+" has decreased by "+(amount*100)+"%!");
		else if (this.type.matches("all")) {
			this.character.getPlayer().sendMessage(ChatColor.RED+"All of your stats has been decreased by "+(amount*100)+"%!");
		}
		else if(this.type.matches("weaken")) {
			this.character.getPlayer().sendMessage(ChatColor.RED+"Your int and str has been decreased by "+(amount*100)+"%!");
		}
		
		final Debuff tempBuff = this;
		this.buffId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
		    public void run() {
		    	tempBuff.removeDebuff();
		    	plugin.deleteDebuff(tempBuff);
		    }
		}, time);
	}
	
	public void removeDebuff() {
		this.character.setStr(this.origStr);
		this.character.setDef(this.origDef);
		this.character.setSpr(this.origSpr);
		this.character.setInt(this.origInt);
		this.character.setDex(this.origDex);
		this.character.setAgl(this.origAgl);
		this.character.setTotalMana(this.origMP);
		this.character.getPlayer().setSneaking(false);
		character.removeDebuff(this);
		character.getPlayer().sendMessage(ChatColor.YELLOW+"Your "+this.type+" debuff has expired!");
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getBuffId() {
		return this.buffId;
	}
}
