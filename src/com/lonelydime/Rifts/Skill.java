package com.lonelydime.Rifts;

public class Skill {
	public static Rifts plugin;
	String name;
	String type;
	boolean active;
	int skillId;
	int manaUsage;
	int hpUsage;
	int requiredLevel;
	int skillPointsRequired;
	int skillLevel;
	String description;

	public Skill(int id, String skillname, String skilltype, int mananeeded, int hpneeded, int skillreq, int levelreq, int level, String desc) {
		skillId = id;
		name = skillname;
		type = skilltype;
		active = false;
		manaUsage = mananeeded;
		hpUsage = hpneeded;
		requiredLevel = levelreq;
		skillPointsRequired = skillreq;
		skillLevel = level;
		description = desc;
	}
	
	//Standard return methods
	public String getName() {
		return this.name;
	}
	public String getType() {
		return this.type;
	}
	public boolean isActive() {
		return this.active;
	}
	public int getManaReq() {
		return this.manaUsage;
	}
	public int getHpReq() {
		return this.hpUsage;
	}
	public int getRequiredLevel() {
		return this.requiredLevel;
	}
	public int getLevel() {
		return this.skillLevel;
	}
	public int getReqPoints() {
		return this.skillPointsRequired;
	}
	public String getDescription() {
		return this.description;
	}
	
}
