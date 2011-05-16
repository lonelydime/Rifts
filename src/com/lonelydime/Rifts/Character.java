package com.lonelydime.Rifts;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Character {
	private long Experience;
	private int charId;
	private byte level;
	private short Strength;
	private short Defense;
	private short Spirit;
	private short Intelligence;
	private short Agility;
	private short Dexterity;
	private byte showExp;
	private short Mana;
	private short totalMana;
	private short freeStats;
	private short freeSkills;
	private int manaSchedulerId;
	private byte beenPvp;
	private byte showMiss;
	private byte axePowerup = 0;
	private byte swordPowerup = 0;
	private byte tutorialLevel = 5;
	private String factionName;
	private String className;
	private String charName;
	private String armorMax;
	private String weaponMax;
	private Skill activeSkill;
	private List<Skill> skills = new ArrayList<Skill>();
	private boolean canUseBow;
	private boolean isStunned = false;
	private boolean isSleeping = false;
	private boolean inventoryOpen = false;
	private boolean isInvincible = false;
	private boolean isSneaking = false;
	private List<Buff> buffs = new ArrayList<Buff>();
	private List<Debuff> debuffs = new ArrayList<Debuff>();
	private List<DoT> dots = new ArrayList<DoT>();
	private static Rifts plugin;
	
	private Player basePlayer;

	public Character(Player player, Rifts instance) {
		basePlayer = player;
		plugin = instance;
	}
	
	public void setupSkills() {
		//skill this, id, name, type, mp req, hp req, level req, skill points req, skill level
		
		// ARCHER //
		if (this.className.matches("archer")) {
			this.armorMax = "cloth";
			this.weaponMax = "stone";
			this.canUseBow = true;
		}
		
		// DEFENDER //
		else if (this.className.matches("defender")) {
			this.armorMax = "diamond";
			this.weaponMax = "iron";
			this.canUseBow = false;
		}
		
		// MAGE //
		else if (this.className.matches("mage")) {
			this.armorMax = "cloth";
			this.weaponMax = "stone";
			this.canUseBow = false;
		}
		
		// PRIEST //
		else if (this.className.matches("priest")) {
			this.armorMax = "iron";
			this.weaponMax = "stone";
			this.canUseBow = false;
		}
		
		// FIGHTER //
		else if (this.className.matches("fighter")) {
			this.armorMax = "iron";
			this.weaponMax = "diamond";
			this.canUseBow = false;
		}
		
		else {
			System.out.println(ChatColor.RED+"[Rifts] Severe: "+this.basePlayer.getName()+" has an invalid class.  They need to be reset.");
		}

		this.regainMana();
	}
	
	public Player getPlayer() {
		return basePlayer;
	}
	
	public String getFaction() {
		return factionName;
	}
	
	public String getClassName() {
		return className;
	}
	
	public short getFreeSkills() {
		return this.freeSkills;
	}
	
	public void addExp(long exp) {
		if (this.getShowExp() && exp > 0) {
			this.Experience = Experience + exp;
			this.basePlayer.sendMessage(ChatColor.YELLOW+"You gained "+exp+" experience.");
		}
		else if (exp > 0) {
			this.Experience = Experience + exp;
		}
		
		if (this.Experience > plugin.getExpForLevel(this.level+1) && this.level < 60) {
			this.levelup();
		}
	}
	
	public boolean getShowExp() {
		if (this.showExp == 1)
			return true;
		else
			return false;
	}
	
	public boolean showMiss() {
		if (this.showMiss == 1)
			return true;
		else
			return false;
	}
	
	public byte getShowMiss() {
		return this.showMiss;
	}
	
	public byte getShowExpInt() {
		return this.showExp;
	}
	
	public int getCharId() {
		return this.charId;
	}
	
	public boolean isSneaking() {
		return this.isSneaking;
	}
	
	public long getExp() {
		return this.Experience;
	}
	
	public byte getLevel() {
		return this.level;
	}
	
	public short getStr() {
		return this.Strength;
	}
	
	public short getDef() {
		return this.Defense;
	}
	
	public short getSpr() {
		return this.Spirit;
	}
	
	public short getInt() {
		return this.Intelligence;
	}
	
	public short getDex() {
		return this.Dexterity;
	}
	
	public short getAgl() {
		return this.Agility;
	}
	
	public short getMana() {
		return this.Mana;
	}
	
	public short getTotalMana() {
		return this.totalMana;
	}
	
	public int getDotDamage() {
		int totalDamage = 0;
		
		for (DoT dot:dots) {
			totalDamage = dot.damageAmount * dot.damageTime;
		}
		
		return totalDamage;
	}
	
	public short getFreeStats() {
		return this.freeStats;
	}
	
	public boolean getInvincible() {
		return this.isInvincible;
	}
	
	public byte getTutorialLevel() {
		return this.tutorialLevel;
	}
	
	public String getCharName() {
		return this.charName;
	}
	
	public boolean getStunned() {
		return this.isStunned;
	}
	
	public boolean isSleeping() {
		return this.isSleeping;
	}
	
	public byte getAxePowerup() {
		return this.axePowerup;
	}
	
	public byte getSwordPowerup() {
		return this.swordPowerup;
	}
	
	public String getSkillList() {
		String list = "";
		
		if (this.skills.size() > 0) {
			for(int i=0;i<this.skills.size();i++) {
				list = list + "," + (this.skills.get(i).skillId);
			}
			list = list.substring(1);
		}
		
		return list;
	}
	
	public String getArmorMax() {
		return this.armorMax;
	}
	
	public String getWeaponMax() {
		return this.weaponMax;
	}
	
	public int getManaSchedulerId() {
		return this.manaSchedulerId;
	}
	
	public List<Buff> getBuffs() {
		return this.buffs;
	}
	
	public List<Debuff> getDebuffs() {
		return this.debuffs;
	}
	
	public List<DoT> getDots() {
		return this.dots;
	}
	
	public boolean canHoldBow() {
		return this.canUseBow;
	}
	
	public boolean beenPvp() {
		if (this.beenPvp == 1)
			return true;
		else
			return false;
	}
	
	public byte getBeenPvp() {
		return this.beenPvp;
	}
	
	public Skill getActiveSkill() {
		return this.activeSkill;
	}
	
	public List<Skill> getAvailableSkills() {
		return skills;
	}
	
	public boolean hasSkill(String skillName, int skLevel) {
		for (Skill skill:this.skills) {
			if (skill.getName().matches(skillName) && skill.getLevel() >= skLevel)
				return true;
		}
		return false;
	}
	
	public int hasBuff(String buffName) {
		for (Buff buff:buffs) {
			if (buff.getName().matches(buffName))
				return buff.getBuffId();
		}
		return -1;
	}
	
	public int hasDebuff(String debuffName) {
		for (Debuff debuff:debuffs) {
			if (debuff.getName().matches(debuffName))
				return debuff.getBuffId();
		}
		return -1;
	}
	
	public int hasDot(String dotName) {
		for (DoT dot:dots) {
			if (dot.getName().matches(dotName))
				return dot.getDotId();
		}
		return -1;
	}
	
	public int getHighestSkillLevel(String skillName) {
		int skillLevel = 0;
		for (Skill skill:this.skills) {
			if (skill.getName().matches(skillName) && skill.getLevel() > skillLevel)
				skillLevel = skill.getLevel();
		}
		
		return skillLevel;
	}
	
	public void addSkill(Skill skill) {
		this.skills.add(skill);
	}
	
	public void setCharId(int arg) {
		this.charId = arg;
	}
	
	public void setStunned(boolean isStunned) {
		this.isStunned = isStunned;
	}
	
	public void setShowExp(byte show) {
		this.showExp = show;
	}
	
	public void setFaction(String arg) {
		this.factionName = arg;
	}
	
	public void setClassName(String arg) {
		this.className = arg;
	}
	
	public void setExp(int arg) {
		this.Experience = arg;
	}
	
	public void setLevel(byte arg) {
		this.level = arg;
	}
	
	public void setStr(short arg) {
		this.Strength = arg;
	}
	
	public void setDef(short arg) {
		this.Defense = arg;
	}
	
	public void setSpr(short arg) {
		this.Spirit = arg;
	}
	
	public void setInt(short arg) {
		this.Intelligence = arg;
	}
	
	public void setAgl(short arg) {
		this.Agility = arg;
	}
	
	public void setDex(short arg) {
		this.Dexterity = arg;
	}
	
	public void setMana(short arg) {
		this.Mana = arg;
	}
	
	public void setTotalMana(short arg) {
		this.totalMana = arg;
	}
	
	public void setFreeStats(short arg) {
		this.freeStats = arg;
	}
	
	public void setFreeSkills(short arg) {
		this.freeSkills = arg;
	}
	
	public void setBeenPvp(byte arg) {
		this.beenPvp = arg;
	}
	
	public void setShowMiss(byte arg) {
		this.showMiss = arg;
	}
	
	public void setSleeping(boolean sleep) {
		this.isSleeping = sleep;
	}
	
	public void setAxePowerup(byte arg) {
		this.axePowerup = arg;
	}
	
	public void setSwordPowerup(byte arg) {
		this.swordPowerup = arg;
	}
	
	public void setSneaking(boolean arg) {
		this.isSneaking = arg;
	}
	
	public void setInvincible(boolean arg) {
		this.isInvincible = arg;
	}
	
	public void setSkillList(String list) {
		if (list.length() > 0) {
			String[] skillId = list.split(",");
			for (int i=0;i<skillId.length;i++) {
				skills.add(plugin.skillListById.get(Integer.parseInt(skillId[i])));
			}
		}
		else {
			skills.clear();
		}
	}
	
	public void addBuff(Buff buff) {
		this.buffs.add(buff);
	}
	
	public void addDebuff(Debuff debuff) {
		this.debuffs.add(debuff);
	}
	
	public void removeBuff(Buff buff) {
		this.buffs.remove(buff);
	}
	
	public void removeDebuff(Debuff debuff) {
		this.debuffs.remove(debuff);
	}
	
	public void addDot(DoT dot) {
		this.dots.add(dot);
	}
	
	public void removeDot(DoT dot) {
		this.dots.remove(dot);
	}
	
	public void setActiveSkill(String skillname) {
		Iterator<Skill> itr = this.skills.iterator();
		Skill skill = null;
		Skill temp;
		while(itr.hasNext()) {
			temp = itr.next();
			if (temp.getName().matches(skillname)) {
				skill = temp;
			}
		}
		
		this.activeSkill = skill;
	}
	
	public void setTutorialLevel(byte arg) {
		this.tutorialLevel = arg;
	}
	
	public void cycleSkills() {
		int indexOfSkill = 0;
		boolean doloop = true;
		boolean hasNonBuff = false;
		if (this.skills.size() > 0) {
			for (Skill skill:this.skills) {
				if (!skill.getType().contains("buff") && !skill.getType().matches("ability"))
					hasNonBuff = true;
			}
		}
		if (hasNonBuff) {
			while (doloop) {
				if (this.activeSkill != null) {
					indexOfSkill = this.skills.indexOf(this.activeSkill);
					indexOfSkill++;
					if (indexOfSkill > (this.skills.size()-1))
						indexOfSkill = -1;
				}
				else 
					indexOfSkill = 0;
				
				if (indexOfSkill >= 0) {
					this.activeSkill = this.skills.get(indexOfSkill);
					//Don't show skills that are buffs or abilities, they don't work this way.
					if (this.activeSkill.getType().contains("buff") || this.activeSkill.getType().matches("ability"))
						doloop = true;
					else if (this.hasSkill(this.activeSkill.getName(), this.activeSkill.getLevel()+1))
						doloop = true;
					else
						doloop = false;
				}
				else {
					this.activeSkill = null;
					doloop = false;
				}
				
			}
			if (this.activeSkill != null) {
				String displayName = this.activeSkill.getName().substring(0, 1).toUpperCase()+this.activeSkill.getName().substring(1);
				this.basePlayer.sendMessage(ChatColor.GOLD+"Switched to the "+ChatColor.GREEN+displayName+ChatColor.GOLD+" skill!");
			}
			else {
				this.basePlayer.sendMessage(ChatColor.GOLD+"Skills off!");
			}
		}

	}
	
	public void removeActiveSkill() {
		this.activeSkill = null;
	}
	
	public void levelup() {		
		this.level++;
		this.basePlayer.sendMessage(ChatColor.GOLD+"You are now level "+this.level+"!");
		if (level == 2) {
			this.basePlayer.sendMessage(ChatColor.AQUA+"You now have 7 stat points to enhance your character!");
			this.basePlayer.sendMessage(ChatColor.AQUA+"Type "+ChatColor.GREEN+"/stats add <amount> <type>"+ChatColor.AQUA+" to increase your stats.");
			this.basePlayer.sendMessage(ChatColor.AQUA+"Example: "+ChatColor.GREEN+"/stats add 4 str"+ChatColor.AQUA+" to add 4 strength points.");
			this.basePlayer.sendMessage(ChatColor.AQUA+"To know more about what stats do, type "+ChatColor.GREEN+"/statshelp.");
		}
		else if (level == 5) {
			this.basePlayer.sendMessage(ChatColor.AQUA+"You gained access to skills!");
			this.basePlayer.sendMessage(ChatColor.AQUA+"Type "+ChatColor.GREEN+"/skills"+ChatColor.AQUA+" to check your available skills.");
			this.basePlayer.sendMessage(ChatColor.AQUA+"Add them by typing "+ChatColor.GREEN+"/skill add <name>"+ChatColor.AQUA+".  Doing this uses skill points.");
			this.basePlayer.sendMessage(ChatColor.AQUA+"You can use your new skill by typing "+ChatColor.GREEN+"/skill <name>"+ChatColor.AQUA+" or");
			if (this.className.matches("fighter")) {
				this.basePlayer.sendMessage(ChatColor.AQUA+"right clicking air with sword in hand.");
				this.basePlayer.sendMessage(ChatColor.AQUA+"Use your sword against a foe to utilize it!");
			}
			else if (this.className.matches("defender")) {
				this.basePlayer.sendMessage(ChatColor.AQUA+"right clicking air with axe in hand.");
				this.basePlayer.sendMessage(ChatColor.AQUA+"Use your axe against a foe to utilize it!");
			}
			else if (this.className.matches("priest") || this.className.matches("mage")) {
				this.basePlayer.sendMessage(ChatColor.AQUA+"left clicking air with stick in hand.");
				this.basePlayer.sendMessage(ChatColor.AQUA+"Right click with stick in hand to fire off your spell!");
			}
			else if (this.className.matches("arhcer")) {
				this.basePlayer.sendMessage(ChatColor.AQUA+"left clicking air with bow in hand.");
				this.basePlayer.sendMessage(ChatColor.AQUA+"Fire an arrow to utilize it!");
			}
			this.basePlayer.sendMessage(ChatColor.AQUA+"Type "+ChatColor.GREEN+"/skilllist"+ChatColor.AQUA+" to see skills you own.");

		}
		if (this.className.matches("fighter")) {
			this.Strength++;
		}
		else if (this.className.matches("defender")) {
			this.Defense++;
		}
		else if (this.className.matches("mage")) {
			this.Intelligence++;
		}
		else if (this.className.matches("priest")) {
			this.Spirit++;
		}
		else if (this.className.matches("archer")) {
			this.Agility++;
		}
		this.totalMana = (short)(this.totalMana + this.level*5);
		
		//totally heal player
		this.basePlayer.setHealth(20);
		this.Mana = this.totalMana;
		
		this.setFreeStats((short)(this.getFreeStats() + 5));
		this.setFreeSkills((short)(this.getFreeSkills() + 2));
	}
	
	public void regainMana() {
		int regenTime = 1000;
		final Character character = this;
		character.manaSchedulerId = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
		    public void run() {
		    	int manaMultiplier = character.getSpr()/10;
		    	if (manaMultiplier < 1)
		    		manaMultiplier = 1;
		    	
		    	int manaToAdd = Math.round((character.getLevel()*manaMultiplier));
		    	manaToAdd = character.getMana()+manaToAdd;
		    	if (manaToAdd > character.getTotalMana()) {
		    		manaToAdd = character.getTotalMana();
		    	}
		    	
		    	character.setMana((short)manaToAdd);
		    	character.regainMana();
		    }
		}, regenTime);
	}
	
	public boolean isInventoryOpen() {
		return this.inventoryOpen;
	}
	
	public void setInventoryOpen(boolean isopen) {
		this.inventoryOpen = isopen;
	}
	
}

