package com.lonelydime.Rifts;

//sql
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Database {
	public static Rifts plugin;
    Connection connection;
	Statement statement;
	
	public Database(Rifts instance) throws ClassNotFoundException, SQLException {
		plugin = instance;

		Class.forName("org.sqlite.JDBC");
		
		this.connection = DriverManager.getConnection("jdbc:sqlite:"+plugin.getDataFolder().toString()+File.separator+"database.db");		
		this.statement = this.connection.createStatement();		

	}
	
	public void close() {
		try {
			this.statement.close();
			this.connection.close();
		}
		catch(Exception e) {
			System.out.println("Error closing database: "+e);
		}
	}
	
	/*********************************************************************
	 * 
	 * Player Specific methods
	 * 
	 * ******************************************************************/
	
	public boolean playerExists(Player player) {
		boolean exists = false;
		int charId = -1;
		
		try {	
			ResultSet rs = this.statement.executeQuery("SELECT `charid` FROM `characters` WHERE `name`='"+player.getName()+"';");
				
			while (rs.next()) {
				charId = rs.getInt("charid");
	        }
			
			if (charId >= 0)
				exists = true;
			
			rs.close();
    	}
    	catch(Exception e) {
    		System.out.println("Error finding player: "+e);
    	}
		return exists;
	}
	
	public void createPlayer(Player player, String factionName, String className) {
		try {
			int exp = 0;
			int lvl = 1;
			int str = 0;
			int def = 0;
			int spr = 0;
			int agl = 0;
			int dex = 0;
			int beenpvp = 0;
			int showmiss = 1;
			int inte = 0;
			int showexp = 1;
			int mana = 100;
			int totalMana = 100;
			int freeStats = 0;
			int freeSkills = 0;
			
			this.statement.executeUpdate("INSERT INTO `characters` (`charid`,`name`,`faction`,`class`,`experience`,`level`,`STR`,`DEF`,`SPR`,`INT`,`DEX`,`AGL`,`SHOWMISS`,`SHOWEXP`,`MANA`,`TOTAL_MANA`,`FREE_STATS`,`BEEN_PVP`,`FREE_SKILLS`,`SKILL_LIST`) "
					+"VALUES (NULL, '"+player.getName()+"', '"+factionName+"', '"+className+"', "+exp+", "+lvl+", "+str
					+", "+def+", "+spr+", "+inte+", "+dex+", "+agl+", "+showmiss+", "+showexp+", "+mana+", "+totalMana+", "+freeStats+", "+beenpvp+", "+freeSkills+", '');");
			
    	}
    	catch(Exception e) {
    		System.out.println("Error creating player: "+e);
    	}
	}
	
	public String[] onLogin(Player player) {
		String[] info = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
		
		try {
			ResultSet rs = this.statement.executeQuery("SELECT `charid`,`faction`,`class`,`experience`,`level`,`STR`,`DEF`,`SPR`,`INT`,`DEX`,`AGL`,`SHOWEXP`,`MANA`,`TOTAL_MANA`,`FREE_STATS`,`BEEN_PVP`,`SHOWMISS`,`FREE_SKILLS`, `SKILL_LIST` "
					+"FROM `characters` WHERE `name`='"+player.getName()+"';");
			
			while (rs.next()) {
				info[0] = rs.getString("charid");
				info[1] = rs.getString("faction");
				info[2] = rs.getString("class");
				info[3] = rs.getString("experience");
				info[4] = rs.getString("level");
				info[5] = rs.getString("STR");
				info[6] = rs.getString("DEF");
				info[7] = rs.getString("SPR");
				info[8] = rs.getString("INT");
				info[9] = rs.getString("DEX");
				info[10] = rs.getString("AGL");
				info[11] = rs.getString("SHOWEXP");
				info[12] = rs.getString("MANA");
				info[13] = rs.getString("TOTAL_MANA");
				info[14] = rs.getString("FREE_STATS");
				info[15] = rs.getString("BEEN_PVP");
				info[16] = rs.getString("SHOWMISS");
				info[17] = rs.getString("FREE_SKILLS");
				info[18] = rs.getString("SKILL_LIST");
	        }
			
			rs.close();
    	}
    	catch(Exception e) {
    		System.out.println("Error getting login info: "+e);
    	}
    	
    	return info;
	}
	
	public void characterSave(Character character) {
		try {
			this.statement.executeUpdate("UPDATE characters SET faction='"+character.getFaction()+"', class='"+character.getClassName()+"', "+
				"experience="+character.getExp()+", level="+character.getLevel()+", STR="+character.getStr()+", DEF="+character.getDef()+", "+
				"SPR="+character.getSpr()+", INT="+character.getInt()+", AGL="+character.getAgl()+", DEX="+character.getDex()+", SHOWEXP="+character.getShowExpInt()+", SHOWMISS="+character.getShowMiss()+", "+
				"MANA="+character.getMana()+", TOTAL_MANA="+character.getTotalMana()+", FREE_STATS="+character.getFreeStats()+", BEEN_PVP="+character.getBeenPvp()+", FREE_SKILLS="+character.getFreeSkills()+", "+
				"SKILL_LIST='"+character.getSkillList()+"' WHERE charid="+character.getCharId()+";");
    	}
    	catch(Exception e) {
    		System.out.println("Error saving data for "+character.getPlayer().getName()+": "+e);
    	}
	}
    
    /***************************************************************
     * 
     * GM commands
     * 
     **************************************************************/
    
    public void changeClass(Character character, String newClass) {
    	newClass = newClass.toLowerCase();
    	
    	if (newClass.matches("archer") || newClass.matches("mage") || newClass.matches("priest") || newClass.matches("defender") || 
    			newClass.matches("fighter")) {
	    	try {
	    		this.statement.executeUpdate("UPDATE characters SET class='"+character.getClassName()+"', skill_list='' WHERE charid="+character.getCharId()+";");
	    	}
	    	catch(Exception e) {
	    		System.out.println("Error changing character's class: "+e);
	    	}
    	}
    }
    
    public void changeLevel(Character character, int newLevel) {
    	try {
			int newExp = plugin.getExpForLevel(newLevel);
			statement.executeUpdate("UPDATE characters SET level="+newLevel+", experience="+newExp+" WHERE charid="+character.getCharId()+";");
    	}
    	catch(Exception e) {
    		System.out.println("Error changing character's level: "+e);
    	}
    }
    
    public int getTotalMembers(String tableName, String columnName, String equals) {
    	int total = 0;
    	
    	try {
			ResultSet rs = this.statement.executeQuery("SELECT count(*) FROM "+tableName+" WHERE "+columnName+"='"+equals+"';");
		
			while (rs.next()) {
				total = rs.getInt("count(*)");
	        }
			
			rs.close();
    	}
    	catch (Exception e) {
    		System.out.println("[Rifts] getTotalMembers failed: "+e);
    	}
    	
    	return total;
    }
    
    public void createPortal(Block portal1, Block portal2, String faction) {
    	int x1 = portal1.getX();
    	int x2 = portal2.getX();
    	int y1 = portal1.getY();
    	int y2 = portal2.getY();
    	int z1 = portal1.getZ();
    	int z2 = portal2.getZ();
    	
    	try {			
    		this.statement.executeUpdate("INSERT INTO `portals` (`portalid`,`loc1x`,`loc1y`,`loc1z`,`loc2x`,`loc2y`,`loc2z`,`world`,`uses`,`faction`) "
					+"VALUES (NULL, "+x1+", "+y1+", "+z1+", "+x2+", "+y2+", "+z2+", '"+portal1.getWorld().getName()+"', "+0+", '"+faction+"');");
    	}
    	catch(Exception e) {
    		System.out.println("Error creating portal: "+e);
    	}
    }
    
    public void loadPortals() {
    	int x1=0,y1=0,z1=0,x2=0,y2=0,z2=0;//uses=0;
    	String world, faction;
    	Block portal1, portal2;
    	Portal portal;
    	
    	try {
			ResultSet rs = this.statement.executeQuery("select * from portals;");
			while (rs.next()) {
				x1 = rs.getInt("loc1x");
				y1 = rs.getInt("loc1y");
				z1 = rs.getInt("loc1z");
				x2 = rs.getInt("loc2x");
				y2 = rs.getInt("loc2y");
				z2 = rs.getInt("loc2z");
				//uses = rs.getInt("uses");
				world = rs.getString("world");
				faction = rs.getString("faction");
				portal1 = plugin.getServer().getWorld(world).getBlockAt(x1, y1, z1);
				portal2 = plugin.getServer().getWorld(world).getBlockAt(x2, y2, z2);
				portal = new Portal(plugin, portal1, portal2, faction);
				plugin.portals.add(portal);
	        }
			rs.close();
    	}
    	
    	catch (Exception e) {
    		System.out.println("Error loading portals: "+e);
    	}
    }
    
    public void removePortal(Portal portal) {
    	int x=portal.getBlock1().getX();
    	int y=portal.getBlock1().getY();
    	int z=portal.getBlock1().getZ();
    	String world = portal.getBlock1().getWorld().getName();
    	
    	try {
    		this.statement.executeUpdate("delete from portals where loc1x="+x+" and loc1y="+y+" and loc1z="+z+" and world='"+world+"';");
    	}
    	
    	catch (Exception e) {
    		System.out.println("Error removing portal: "+e);
    	}
    }
    
    public void saveCage(Cage cage) {
    	Location location = cage.getCenter();
    	int x = location.getBlockX();
    	int y = location.getBlockY();
    	int z = location.getBlockZ();
    	String world = location.getWorld().getName();
    	
    	try {				
    		this.statement.executeUpdate("INSERT INTO `cages` (`cageid`,`locx`,`locy`,`locz`,`world`,`faction`) "
					+"VALUES (NULL, "+x+", "+y+", "+z+", '"+world+"', '"+cage.getFaction()+"');");
    	}
    	catch(Exception e) {
    		System.out.println("Error creating cage: "+e);
    	}
    }
    
    public int loadCages() {
    	int x=0,y=0,z=0,num=0;//uses=0;
    	String world, faction;
    	
    	try {
			ResultSet rs = this.statement.executeQuery("select * from cages;");
			
			while (rs.next()) {
				x = rs.getInt("locx");
				y = rs.getInt("locy");
				z = rs.getInt("locz");
				world = rs.getString("world");
				faction = rs.getString("faction");
				
				Location location = plugin.getServer().getWorld(world).getBlockAt(x, y, z).getLocation();
				Cage cage = new Cage(plugin, location, faction, false);
				plugin.cages.add(cage);
				//System.out.println("Cage: "+cage.getCenter().getBlockX()+":"+cage.getCenter().getBlockY()+":"+cage.getCenter().getBlockZ());
				num++;
	        }
			
			rs.close();
    	}
    	
    	catch (Exception e) {
    		System.out.println("Error loading cages: "+e);
    	}
    	
    	return num;
    }
    
    public void removeCharacter(Character character) {	
    	try {
    		Class.forName("org.sqlite.JDBC");
    		this.statement.executeUpdate("delete from characters where charid="+character.getCharId()+";");
    	}
    	
    	catch (Exception e) {
    		System.out.println("Error removing character: "+e);
    	}
    }

}
