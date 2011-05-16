package com.lonelydime.Rifts;

//java
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.logging.Logger;

//sql
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

//bukkit
import org.bukkit.craftbukkit.CraftWorld;
import net.minecraft.server.WorldServer;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

//permissions
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.anjocaido.groupmanager.GroupManager;

//iconomy
import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;

public class Rifts extends JavaPlugin {
	
	//listeners
	private final RPlayerListener playerListener = new RPlayerListener(this);
	private final RBlockListener blockListener = new RBlockListener(this);
	private final REntityListener entityListener = new REntityListener(this);
	
	//logger
	private final Logger log = Logger.getLogger("Minecraft");
	
	//permissions
	public static PermissionHandler Permissions = null;
	public static GroupManager gm = null;
	
	//iconomy
	public static iConomy iConomy = null;
	private static Server Server = null;
	private boolean useiConomy = false; 
  
	//class variables
	public double spawniconomyCost = 0;
	public int spawnUses = 0;
	public int goldReceived = 0;
	public boolean autoJoin;
	public boolean usePvp;
	public boolean useFactions;
	public boolean mustRift;
	public String lightSeed, darkSeed, pvpSeed, lightWorldName, darkWorldName, pvpWorldName;
	public HashMap<Player, Character> characters = new HashMap<Player, Character>();
	public HashMap<Player, Party> parties = new HashMap<Player, Party>();
	public HashMap<String, Integer> experience = new HashMap<String, Integer>();
	public HashMap<World, Rift> rifts = new HashMap<World, Rift>();
	public HashMap<Skill, String> skillList = new HashMap<Skill, String>();
	public HashMap<Integer, Skill> skillListById = new HashMap<Integer, Skill>();
	public List<Portal> portals = new ArrayList<Portal>();
	public List<Cage> cages = new ArrayList<Cage>();
	public List<Block> webs = new ArrayList<Block>();
	public HashMap<Player, SpawnBlock> spawnblocks = new HashMap<Player, SpawnBlock>();
	public HashMap<Integer, String> deathby = new HashMap<Integer, String>();
	//database instance
	public Database data;
	//warning instance - removed due to spam
	//public Warning warning = new Warning(this);
	//create the command handler
	Commands commands = new Commands(this);

	public void onDisable() {
		Collection<Character> c = characters.values();
		Iterator<Character> itr = c.iterator();

		while(itr.hasNext()) {
			data.characterSave((Character)itr.next());
		}
		
		log.info("[Rifts] Characters saved.");

		if (rifts.size() > 0) {
			for (Rift rift : rifts.values()) {
			    rift.destroy();
			}
			rifts.clear();
		}
		
		log.info("[Rifts] Cleared active rifts.");
		
		Collection<SpawnBlock> c2 = spawnblocks.values();
		Iterator<SpawnBlock> itr2 = c2.iterator();
		 
		while(itr2.hasNext()) {
			SpawnBlock spawn = itr2.next();
			spawn.DestroySpawnBlock("[serverstop]");
		}
		
		log.info("[Rifts] SpawnBlocks cleared.");
		
		for (Entry<Skill, String> entry : skillList.entrySet()) {
			Skill skill = entry.getKey();
			if (skill != null)
				skill = null;
		}
		skillList.clear();
		skillListById.clear();
		log.info("[Rifts] Skills cleared.");
		getServer().getScheduler().cancelAllTasks();
		
		log.info("[Rifts] Schedules cancelled.");
		
		data.close();
		
		log.info("[Rifts] Disabled");	
	}

	public void onEnable() {
		
		if (!new File(getDataFolder().toString()).exists() ) {
			new File(getDataFolder().toString()).mkdir();
		}

		File yml = new File(getDataFolder()+"/config.yml");
      
		if (!yml.exists()) {
			new File(getDataFolder().toString()).mkdir();
			setupConfig();
			storeConfig();
		}	
		
		else {
			storeConfig();
		}

		//Create the pluginmanager pm.
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		//pm.registerEvent(Event.Type.INVENTORY_OPEN, playerListener, Priority.Normal, this);
		//pm.registerEvent(Event.Type.INVENTORY_CHANGE, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
		//Get the information from the plugin.yml file.
		PluginDescriptionFile pdfFile = this.getDescription();
      
		//Setup Permissions/GroupManager
		setupPermissions();
      
		//iConomy
		getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, new server(this), Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, new server(this), Priority.Monitor, this);

		//create database
		try {
			data = new Database(this);
			connect();
		}
		catch(Exception e) {
			System.out.println("[Rifts] Database setup failed: "+e);
		}
		
		checkWorlds();
		
		//load in skills
		setupSkills();
		log.info("[Rifts] Skills loaded");
		//load in cages
		if (usePvp && useFactions) {
			if (data.loadCages() < 2) {
				log.info("[Rifts] Creating PvP cages");
				//create pvp cages
				World world = getServer().getWorld(pvpWorldName);
				world.loadChunk(world.getSpawnLocation().getBlockX(), world.getSpawnLocation().getBlockZ(), true);
				Cage cage1 = new Cage(this, getServer().getWorld(pvpWorldName).getSpawnLocation(), "light", true);
				Random generator = new Random();
				int x=0,y=0,z=0;
				
				while (y == 0) {
					x = generator.nextInt(400)+50+world.getSpawnLocation().getBlockX();
					z = generator.nextInt(400)+50+world.getSpawnLocation().getBlockX();

					world.loadChunk(x, z, true);
					y = world.getHighestBlockYAt(x, z);
				}
				
				Cage cage2 = new Cage(this, world.getBlockAt(x, y, z).getLocation(), "dark", true);
				cages.add(cage1);
				cages.add(cage2);
			}
		
			log.info("[Rifts] Cages loaded");
		}
		
		//load in portals - Must be done after cages are loaded.
		if (usePvp && useFactions) {
			data.loadPortals();
			log.info("[Rifts] Portals loaded");
		}
		
		//create the thread for generating rifts
		if (this.useFactions) {
			riftTimer();
			warningTimer();
		}
		
		//for reloading, load in all players currently connected.
		reloadPlayers();
		
		//Print that the plugin has been enabled!
		log.info("[Rifts] version " + pdfFile.getVersion() + " by lonelydime is enabled!");
	}
	
	//permissions method
	public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		Plugin p = this.getServer().getPluginManager().getPlugin("GroupManager");
		
		if(Permissions == null) {
		    if(test != null) {
		    	Permissions = ((Permissions)test).getHandler();
		    }
		}
		
		if (p != null) {
			if (!p.isEnabled()) {
				this.getServer().getPluginManager().enablePlugin(p);
			}
			gm = (GroupManager) p;
		} 

	}
	
	//iconomy methods
	public static Server getBukkitServer() {
      return Server;
	}

	public static iConomy getiConomy() {
		return iConomy;
	}
  
	public static boolean setiConomy(iConomy plugin) {
		if (iConomy == null) {
			iConomy = plugin;
		} else {
			return false;
		}
		return true;
	}
  
	public boolean checkiConomy() {
		this.useiConomy = (iConomy != null);
		return this.useiConomy;
	}
	
	//Database connect and setup
	public void connect() throws Exception {

		Class.forName("org.sqlite.JDBC");
		
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+getDataFolder().toString()+File.separator+"database.db");
		Statement statement = connection.createStatement();
		
		statement.execute("CREATE TABLE IF NOT EXISTS characters (`charid` INTEGER PRIMARY KEY, "+
				"`name` varchar(32), `faction` varchar(32), `class` varchar(32), `experience` INTEGER, `level` INTEGER, `STR` INTEGER, "+
				"`DEF` INTEGER, `SPR` INTEGER, `DEX` INTEGER, `INT` INTEGER, `AGL` INTEGER, `SHOWEXP` INTEGER, `MANA` INTEGER, `TOTAL_MANA` INTEGER, "+
				"`FREE_STATS` INTEGER, `FREE_SKILLS` INTEGER, `BEEN_PVP` INTEGER DEFAULT 0, `SHOWMISS` INTEGER, `SKILL_LIST` varchar(500));");
		
		statement.execute("CREATE TABLE IF NOT EXISTS portals (`portalid` INTEGER PRIMARY KEY, `loc1x` INTEGER, "
				+"`loc1y` INTEGER, `loc1z` INTEGER, `loc2x` INTEGER, `loc2y` INTEGER, `loc2z` INTEGER, `world` varchar(32), `uses` INTEGER, `faction` varchar(32));");
		
		statement.execute("CREATE TABLE IF NOT EXISTS cages (`cageid` INTEGER PRIMARY KEY, `locx` INTEGER, "
				+"`locy` INTEGER, `locz` INTEGER, `world` varchar(32), `faction` varchar(32));");
		
		statement.close();
		connection.close();
	}
	
	public void reloadCharacter(Player player) {
		Character character = characters.get(player);
		//remove their mana regain thread
		getServer().getScheduler().cancelTask(character.getManaSchedulerId());
		
		if (parties.containsKey(player)) {
			Party party = parties.get(player);
			party.leave(player);
		}
		
		data.characterSave(character);
		System.out.println(character.getPlayer().getName()+" has been saved.");
		characters.remove(player);
		character = null;
		
		if (data.playerExists(player)) {
			
			String[] charInfo = data.onLogin(player);
			characters.put(player, new Character(player, this));
			character = characters.get(player);

			try {
				//Save all of the database values for a player into their Character instance
				character.setCharId(Integer.parseInt(charInfo[0]));
				character.setFaction(charInfo[1]);
				character.setClassName(charInfo[2]);
				character.setExp(Integer.parseInt(charInfo[3]));
				character.setLevel(Byte.parseByte(charInfo[4]));
				character.setStr(Short.parseShort(charInfo[5]));
				character.setDef(Short.parseShort(charInfo[6]));
				character.setSpr(Short.parseShort(charInfo[7]));
				character.setInt(Short.parseShort(charInfo[8]));
				character.setDex(Short.parseShort(charInfo[9]));
				character.setAgl(Short.parseShort(charInfo[10]));
				character.setShowExp(Byte.parseByte(charInfo[11]));
				character.setMana(Short.parseShort(charInfo[12]));
				character.setTotalMana(Short.parseShort(charInfo[13]));
				character.setFreeStats(Short.parseShort(charInfo[14]));
				character.setBeenPvp(Byte.parseByte(charInfo[15]));
				character.setShowMiss(Byte.parseByte(charInfo[16]));
				character.setFreeSkills(Short.parseShort(charInfo[17]));
				character.setSkillList(charInfo[18]);
				character.setupSkills();
			}
			catch(Exception e) {
				System.out.println("Error setting up character: "+e);
			}
			
		}
	}
	
	public void reloadPlayers() {
		Player[] playerList = getServer().getOnlinePlayers();
		Player player = null;
		
		for (int i=0;i<playerList.length;i++) {
			player = playerList[i];
			
			if (data.playerExists(player)) {
				
				String[] charInfo = data.onLogin(player);
				characters.put(player, new Character(player, this));
				Character character = characters.get(player);

				try {
					//Save all of the database values for a player into their Character instance
					character.setCharId(Integer.parseInt(charInfo[0]));
					character.setFaction(charInfo[1]);
					character.setClassName(charInfo[2]);
					character.setExp(Integer.parseInt(charInfo[3]));
					character.setLevel(Byte.parseByte(charInfo[4]));
					character.setStr(Short.parseShort(charInfo[5]));
					character.setDef(Short.parseShort(charInfo[6]));
					character.setSpr(Short.parseShort(charInfo[7]));
					character.setInt(Short.parseShort(charInfo[8]));
					character.setDex(Short.parseShort(charInfo[9]));
					character.setAgl(Short.parseShort(charInfo[10]));
					character.setShowExp(Byte.parseByte(charInfo[11]));
					character.setMana(Short.parseShort(charInfo[12]));
					character.setTotalMana(Short.parseShort(charInfo[13]));
					character.setFreeStats(Short.parseShort(charInfo[14]));
					character.setBeenPvp(Byte.parseByte(charInfo[15]));
					character.setShowMiss(Byte.parseByte(charInfo[16]));
					character.setFreeSkills(Short.parseShort(charInfo[17]));
					character.setSkillList(charInfo[18]);
					character.setupSkills();
				}
				catch(Exception e) {
					System.out.println("Error setting up character: "+e);
				}
				
			}
		}
	}
  
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		return commands.sendCommand(sender, cmd, commandLabel, args);
	}
	
	public void checkWorlds() {
        List<World> worlds = getServer().getWorlds();
        boolean createLightWorld = true;
        boolean createDarkWorld = true;
        boolean createPvpWorld = true;
        
        for (World world : worlds) {
        	if (useFactions) {
	            if (world.getName() == lightWorldName) {
	            	createLightWorld = false;
	            }
	            else if (world.getName() == darkWorldName) {
	            	createDarkWorld = false;
	            }
        	}
            if (usePvp && useFactions) {
	            if (world.getName() == pvpWorldName) {
	            	createPvpWorld = false;
	            }
            }
        }
        
        if (!usePvp) {
        	createPvpWorld = false;
        }
        
        if (!useFactions) {
        	createLightWorld = false;
        	createDarkWorld = false;
        }

        if (createLightWorld) {
        	log.info("[Rifts] Loading "+lightWorldName+".");
        	if (lightSeed != null)
        		getServer().createWorld(lightWorldName, World.Environment.NORMAL, Long.getLong(lightSeed));
        	else
        		getServer().createWorld(lightWorldName, World.Environment.NORMAL);
        }
        if (createDarkWorld) {
        	log.info("[Rifts] Loading "+darkWorldName+".");
        	if (darkSeed != null)
        		getServer().createWorld(darkWorldName, World.Environment.NORMAL, Long.getLong(darkSeed));
        	else
        		getServer().createWorld(darkWorldName, World.Environment.NORMAL);
        }
        if (usePvp && useFactions) {
	        if (createPvpWorld) {
	        	log.info("[Rifts] Loading "+pvpWorldName+".");
	        	if (pvpSeed != null)
	        		getServer().createWorld(pvpWorldName, World.Environment.NETHER, Long.getLong(pvpSeed));
	        	else
	        		getServer().createWorld(pvpWorldName, World.Environment.NETHER);
	        }
        }
	}
	
	public void setupConfig() {
		File yml = new File(getDataFolder()+"/config.yml");
		try {
			yml.createNewFile();
		}
		catch (IOException ex) {
  	    	System.out.println("cannot create file "+yml.getPath());
  	    }
  	    
  	    try {
  	    	BufferedWriter out = new BufferedWriter(new FileWriter(yml, true));
				
  	    	out.write("spawnblock-iconomy-cost: 0");
  	    	out.newLine();
  	    	out.write("spawnblock-uses: 20");
  	    	out.newLine();
  	    	out.write("force-autojoin: false");
  	    	out.newLine();
  	    	out.write("use-pvpmap: true");
  	    	out.newLine();
  	    	out.write("use-factions: true");
  	    	out.newLine();
  	    	out.write("lightworld-seed: ");
  	    	out.newLine();
  	    	out.write("darkworld-seed: ");
  	    	out.newLine();
  	    	out.write("pvpworld-seed: ");
  	    	out.newLine();
  	    	out.write("lightworld-name: ");
  	    	out.newLine();
  	    	out.write("darkworld-name: ");
  	    	out.newLine();
  	    	out.write("pvpworld-name: ");
  	    	out.newLine();
  	    	out.write("must-rift-to-play: false");
  	    	out.newLine();
  	    	out.newLine();
  	    	out.write("#Experience - These are a multipliers: 5 is normal, 10 is double exp, etc.");
  	    	out.newLine();
  	    	out.write("#Players is for pvp kills, Mobs is for monsters and Blocks is for breaking blocks.");
  	    	out.newLine();
  	    	out.write("players: 5");
  	    	out.newLine();
  	    	out.write("mobs: 5");
  	    	out.newLine();
  	    	out.write("blocks: 5");
  	    	out.newLine();
  	    	out.newLine();
  	    	out.write("#Gold received - This are a multipliers: 5 is normal, 10 is double exp, etc.");
  	    	out.newLine();
  	    	out.write("#Gold is only given when mobs or players are killed.");
  	    	out.newLine();
  	    	out.write("gold-received: 5");

  	    	
  	    	//Close the output stream
  	    	out.close();
  	    }
  	    catch (Exception e) {
  	    	System.out.println("cannot write config file: "+e);
  	    }
	}
	
	private void storeConfig() {
		//Read config.yml
		spawniconomyCost = getConfiguration().getDouble("spawnblock-iconomy-cost", 0.0);
		spawnUses = getConfiguration().getInt("spawnblock-uses", 10);
		autoJoin = getConfiguration().getBoolean("force-autojoin", false);
		goldReceived = getConfiguration().getInt("gold-received", 5);
		usePvp = getConfiguration().getBoolean("use-pvpmap", true);
		useFactions = getConfiguration().getBoolean("use-factions", true);
		lightWorldName = getConfiguration().getString("lightworld-name", "lightworld");
		darkWorldName = getConfiguration().getString("darkworld-name", "darkworld");
		pvpWorldName = getConfiguration().getString("pvpworld-name", "pvpworld");
		lightSeed = getConfiguration().getString("lightworld-seed", null);
		darkSeed = getConfiguration().getString("darkworld-seed", null);
		pvpSeed = getConfiguration().getString("pvpworld-seed", null);
		mustRift = getConfiguration().getBoolean("must-rift-to-play", false);
		//experience table - Mobs
		experience.put("players", getConfiguration().getInt("player-exp", 5));
		experience.put("mobs", getConfiguration().getInt("player-exp", 5));
		experience.put("blocks", getConfiguration().getInt("player-exp", 5));
	}
	
	/**********************************************************
	 * 
	 * Add Exp method.
	 * Adds experience to a person, or splits it up if in a
	 * group - based on level differences.
	 * 
	 *********************************************************/
	
	public long addExp(Player player, int exp, boolean giveMoney, String type) {
		long totalexp = 0;
		int money = 0;
		
		Character character = characters.get(player);
		
		//if player is in a party, figure out the exp distribution
		if (parties.containsKey(player)) {
			Party party = parties.get(player);
			List<Player> members = party.members;
			int averageLevel = 0;
			int totalLevels = 0;
			int minLevel = 100;
			int maxLevel = 0;
			long expToAdd = 0;
			
			
			//total exp for splitting math.
			exp = exp * 2;

			for (Player expPlayer : members) {
				Character character2 = characters.get(expPlayer);
				totalLevels = totalLevels + character2.getLevel();
				
				if (character2.getLevel() < minLevel)
					minLevel = character2.getLevel();
				else if (character2.getLevel() > maxLevel)
					maxLevel = character2.getLevel();
			}
			
			averageLevel = Math.round(totalLevels / members.size());
			money = (averageLevel * 2)/party.members.size();
			if ((maxLevel - minLevel) >= 10) {
				party.sendPartyMessage(ChatColor.RED+"No experience given.  Level difference is too great.");
			}
			else {
				for (Player expPlayer : members) {
					expToAdd = Math.round((averageLevel*.2)*(exp/members.size()));
					if (checkiConomy()) {
						Account account = com.iConomy.iConomy.getAccount(expPlayer.getName());
						if (account != null) {
							Holdings balance = com.iConomy.iConomy.getAccount(expPlayer.getName()).getHoldings();
							balance.add(money);
						}
					}
					if (expToAdd <= 0)
						expToAdd = 1;
					character.addExp(expToAdd);
				}
			}
		}
		else {
			int expToAdd;
			if (checkiConomy()) {
				if (giveMoney) {
					money = characters.get(player).getLevel() * goldReceived;
					if (checkiConomy()) {
						Account account = com.iConomy.iConomy.getAccount(player.getName());
						if (account != null) {
							Holdings balance = com.iConomy.iConomy.getAccount(player.getName()).getHoldings();
							balance.add(money);
						}
					}
				}
			}	
			expToAdd = (int)Math.round((character.getLevel()*.2)*exp);
			if (expToAdd <= 0)
				expToAdd = 1;
			character.addExp(expToAdd);
		}
		
		if (character.getTutorialLevel() == 0 && type.matches("block")) {
			player.sendMessage(ChatColor.AQUA+"You gained experience!  You can check your current experience"
				+ChatColor.AQUA+" by typing "+ChatColor.GREEN+"/exp "+ChatColor.AQUA+"or "+ChatColor.GREEN+"/status");
			player.sendMessage(ChatColor.GREEN+"/status "+ChatColor.AQUA+" will also give you other information like mana.");
			player.sendMessage(ChatColor.AQUA+"You can type "+ChatColor.GREEN+"/hide exp "+ChatColor.AQUA+"to hide experience notifications.");
			player.sendMessage(ChatColor.AQUA+"You can turn it back on with "+ChatColor.GREEN+"/show exp.");
			player.sendMessage(ChatColor.GOLD+"Next, attack and kill a mob.");
			character.setTutorialLevel((byte)1);
		}
		
		else if (character.getTutorialLevel() == 1 && type.matches("entity")) {
			player.sendMessage(ChatColor.AQUA+"You killed something!  While attacking, did you miss your prey?");
			player.sendMessage(ChatColor.AQUA+"By typing "+ChatColor.GREEN+"/hide miss "+ChatColor.AQUA+"you can hide missed/dodged messages.");
			player.sendMessage(ChatColor.AQUA+"If you want them back on, type  "+ChatColor.GREEN+"/show miss ");
			player.sendMessage(ChatColor.GOLD+"Now try to get to level 2!");
			character.setTutorialLevel((byte)2);
		}
		
		return totalexp;
	}
	
	public void death(Entity dead) {
		String entityType = null;
		int experienceStart = 0;
		//squid doesn't fire
		if (dead instanceof Player) {
			entityType = "player";
			experienceStart = 500;
			Player deadPlayer = (Player)dead;
			if (characters.containsKey(deadPlayer)) {
				Character deadCharacter = characters.get(deadPlayer);
				experienceStart = deadCharacter.getLevel() * 50;
				experienceStart = Math.round(experienceStart * (experience.get("players")/5));
			}
		}
		else if (dead instanceof Pig) {
			entityType = "pig";
			experienceStart = 50;
		}
		else if (dead instanceof Sheep) {
			entityType = "sheep";
			experienceStart = 50;
		}
		else if (dead instanceof Cow) {
			entityType = "cow";
			experienceStart = 50;
		}
		else if (dead instanceof Squid) {
			entityType = "squid";
			experienceStart = 75;
		}
		else if (dead instanceof Chicken) {
			entityType = "chicken";
			experienceStart = 50;
		}
		else if (dead instanceof Wolf) {
			entityType = "wolf";
			experienceStart = 150;
		}
		else if (dead instanceof Creeper) {
			entityType = "creeper";
			experienceStart = 250;
		}
		else if (dead instanceof Skeleton) {
			entityType = "skeleton";
			experienceStart = 150;
		}
		else if (dead instanceof PigZombie) {
			entityType = "pigzombie";
			experienceStart = 400;
		}
		else if (dead instanceof Zombie) {
			entityType = "zombie";
			experienceStart = 150;
		}
		else if (dead instanceof Ghast) {
			entityType = "ghast";
			experienceStart = 400;
		}
		else if (dead instanceof Spider) {
			entityType = "spider";
			experienceStart = 150;
		}
		else if (dead instanceof Slime) {
			entityType = "slime";
			experienceStart = 300;
		}
		
		if (deathby.containsKey(dead.getEntityId())) {
			int experienceNum = 0;
			Player player = getServer().getPlayer(deathby.get(dead.getEntityId()));
			
			if (characters.containsKey(player)) {
				if (!entityType.matches("player"))
					experienceNum = Math.round(experienceStart * (experience.get("mobs")/5));
				else
					experienceNum = experienceStart;
				
				addExp(player, experienceNum, true, "entity");
			}
			deathby.remove(dead.getEntityId());
		}
	}
	
	public void explosion(Player player, Block block, float explosionStrenth) {
		try {
			WorldServer world = ((CraftWorld)player.getWorld()).getHandle();

			double x = (double)block.getLocation().getX();
			double y = (double)block.getLocation().getY();
			double z = (double)block.getLocation().getZ();

			world.a(null, x, y, z, explosionStrenth);
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void messageAllPlayers(World world, String message) {
		List<Player> playersInWorld = world.getPlayers();
		
		for (int i=0; i< playersInWorld.size(); i++) {
			playersInWorld.get(i).sendMessage(message);
		}

	}
	
	public void setupSkills() {
		int skillid = 0;
		
		//ARCHER SKILLS - 24 skills - total possible skill points: 120
		Skill impale = new Skill(skillid++, "impale", "attack", 30, 0, 4, 5, 1, "Makes your target bleed for 5 seconds.");
		skillList.put(impale, "archer");
		skillListById.put(impale.skillId, impale);
		Skill aim = new Skill(skillid++, "aim", "buff-dexterity", 20, 0, 3, 7, 1, "Temporarily increases your dexterity by 5%.");
		skillList.put(aim, "archer");
		skillListById.put(aim.skillId, aim);
		Skill speed = new Skill(skillid++, "speed", "buff-agility", 20, 0, 3, 10, 1, "Temporarily increases your agility by 5%.");
		skillList.put(speed, "archer");
		skillListById.put(speed.skillId, speed);
		Skill firearrow = new Skill(skillid++, "firearrow", "attack", 30, 0, 6, 13, 1, "Sets the target on fire for 5 seconds.");
		skillList.put(firearrow, "archer");
		skillListById.put(firearrow.skillId, firearrow);
		Skill icearrow = new Skill(skillid++, "icearrow", "attack", 30, 0, 6, 15, 1, "Freezes a player for 5 seconds.");
		skillList.put(icearrow, "archer");
		skillListById.put(icearrow.skillId, icearrow);
		Skill sneak = new Skill(skillid++, "sneak", "buff-sneak", 20, 0, 6, 17, 1, "Allows you to hide for a time.");
		skillList.put(sneak, "archer");
		skillListById.put(sneak.skillId, sneak);
		Skill pierce = new Skill(skillid++, "pierce", "attack", 30, 0, 4, 20, 1, "Attack pierces through 5% of target's defense.");
		skillList.put(pierce, "archer");
		skillListById.put(pierce.skillId, pierce);
		Skill aim2 = new Skill(skillid++, "aim", "buff-dexterity", 25, 0, 3, 23, 2, "Temporarily increases your dexterity by 10%.");
		skillList.put(aim2, "archer");
		skillListById.put(aim2.skillId, aim2);
		Skill impale2 = new Skill(skillid++, "impale", "attack", 40, 0, 5, 25, 2, "Makes your target bleed for 10 seconds.");
		skillList.put(impale2, "archer");
		skillListById.put(impale2.skillId, impale2);
		Skill speed2 = new Skill(skillid++, "speed", "buff-agility", 25, 0, 4, 27, 2, "Temporarily increases your agility by 10%.");
		skillList.put(speed2, "archer");
		skillListById.put(speed2.skillId, speed2);
		Skill tntarrow = new Skill(skillid++, "tntarrow", "attack", 50, 0, 9, 30, 1, "Your arrows explodes on impact.");
		skillList.put(tntarrow, "archer");
		skillListById.put(tntarrow.skillId, tntarrow);
		Skill firearrow2 = new Skill(skillid++, "firearrow", "attack", 40, 0, 7, 33, 2, "Sets the target on fire for 10 seconds.");
		skillList.put(firearrow2, "archer");
		skillListById.put(firearrow2.skillId, firearrow2);
		Skill icearrow2 = new Skill(skillid++, "icearrow", "attack", 40, 0, 7, 35, 2, "Freezes a player for 10 seconds.");
		skillList.put(icearrow2, "archer");
		skillListById.put(icearrow2.skillId, icearrow2);
		Skill sneak2 = new Skill(skillid++, "sneak", "buff-sneak", 25, 0, 4, 37, 2, "Allows you to hide for a time.");
		skillList.put(sneak2, "archer");
		skillListById.put(sneak2.skillId, sneak2);
		Skill pierce2 = new Skill(skillid++, "pierce", "attack", 40, 0, 6, 40, 2, "Attack pierces through 10% of target's defense.");
		skillList.put(pierce2, "archer");
		skillListById.put(pierce2.skillId, pierce2);
		Skill aim3 = new Skill(skillid++, "aim", "buff-dexterity", 30, 0, 5, 43, 3, "Temporarily increases your dexterity by 15%.");
		skillList.put(aim3, "archer");
		skillListById.put(aim3.skillId, aim3);
		Skill impale3 = new Skill(skillid++, "impale", "attack", 50, 0, 6, 45, 3, "Makes your target bleed for 15 seconds.");
		skillList.put(impale3, "archer");
		skillListById.put(impale3.skillId, impale3);
		Skill speed3 = new Skill(skillid++, "speed", "buff-agility", 30, 0, 5, 47, 3, "Temporarily increases your agility by 15%.");
		skillList.put(speed3, "archer");
		skillListById.put(speed3.skillId, speed3);
		Skill tntarrow2 = new Skill(skillid++, "tntarrow", "attack", 75, 0, 11, 50, 2, "Your arrows explodes on impact.");
		skillList.put(tntarrow2, "archer");
		skillListById.put(tntarrow2.skillId, tntarrow2);
		Skill firearrow3 = new Skill(skillid++, "firearrow", "attack", 50, 0, 9, 53, 3, "Sets the target on fire for 15 seconds.");
		skillList.put(firearrow3, "archer");
		skillListById.put(firearrow3.skillId, firearrow3);
		Skill icearrow3 = new Skill(skillid++, "icearrow", "attack", 50, 0, 9, 55, 3, "Freezes a player for 15 seconds.");
		skillList.put(icearrow3, "archer");
		skillListById.put(icearrow3.skillId, icearrow3);
		Skill sneak3 = new Skill(skillid++, "sneak", "buff-sneak", 30, 0, 5, 57, 3, "Allows you to hide for a time.");
		skillList.put(sneak3, "archer");
		skillListById.put(sneak3.skillId, sneak3);
		Skill pierce3 = new Skill(skillid++, "pierce", "attack", 50, 0, 7, 59, 3, "Attack pierces through 15% of target's defense.");
		skillList.put(pierce3, "archer");
		skillListById.put(pierce3.skillId, pierce3);
		Skill arrowrain = new Skill(skillid++, "arrowrain", "attack", 200, 0, 15, 60, 1, "Rains down arrows from above.");
		skillList.put(arrowrain, "archer");
		skillListById.put(arrowrain.skillId, arrowrain);
		
		//MAGE SKILLS
		Skill magicarrow = new Skill(skillid++, "magicarrow", "magic", 25, 0, 4, 5, 1, "Hits your target with a magic arrow.");
		skillList.put(magicarrow, "mage");
		skillListById.put(magicarrow.skillId, magicarrow);
		Skill focus = new Skill(skillid++, "focus", "buff-intelligence", 20, 0, 3, 7, 1, "Temporarily increases your intelligence by 5%.");
		skillList.put(focus, "mage");
		skillListById.put(focus.skillId, focus);
		Skill fireball = new Skill(skillid++, "fireball", "magic", 30, 0, 5, 10, 1, "Shoots a fireball that ignites the ground.");
		skillList.put(fireball, "mage");
		skillListById.put(fireball.skillId, fireball);
		Skill bolt = new Skill(skillid++, "bolt", "magic", 30, 0, 5, 13, 1, "Strikes your target with lightning.");
		skillList.put(bolt, "mage");
		skillListById.put(bolt.skillId, bolt);
		Skill entangle = new Skill(skillid++, "entangle", "magic", 30, 0, 4, 15, 1, "Slows the target with webs.");
		skillList.put(entangle, "mage");
		skillListById.put(entangle.skillId, entangle);
		Skill teleport = new Skill(skillid++, "teleport", "tele", 50, 0, 6, 17, 1, "Teleports a party member to you.");
		skillList.put(teleport, "mage");
		skillListById.put(teleport.skillId, teleport);
		Skill plague = new Skill(skillid++, "plague", "magic", 25, 0, 4, 20, 1, "Lowers your target's health by 5% a second.");
		skillList.put(plague, "mage");
		skillListById.put(plague.skillId, plague);
		Skill magicarrow2 = new Skill(skillid++, "magicarrow", "magic", 40, 0, 6, 23, 2, "Hits your target with a magic arrow.");
		skillList.put(magicarrow2, "mage");
		skillListById.put(magicarrow2.skillId, magicarrow2);
		Skill focus2 = new Skill(skillid++, "focus", "buff-intelligence", 30, 0, 4, 25, 2, "Temporarily increases your intelligence by 10%.");
		skillList.put(focus2, "mage");
		skillListById.put(focus2.skillId, focus2);
		Skill fireball2 = new Skill(skillid++, "fireball", "magic", 50, 0, 6, 27, 2, "Shoots a fireball that ignites the ground.");
		skillList.put(fireball2, "mage");
		skillListById.put(fireball2.skillId, fireball2);
		Skill blast = new Skill(skillid++, "blast", "magic", 80, 0, 9, 30, 1, "Creates an explosion on impact.");
		skillList.put(blast, "mage");
		skillListById.put(blast.skillId, blast);
		Skill bolt2 = new Skill(skillid++, "bolt", "magic", 50, 0, 6, 33, 2, "Strikes your target with lightning.");
		skillList.put(bolt2, "mage");
		skillListById.put(bolt2.skillId, bolt2);
		Skill entangle2 = new Skill(skillid++, "entangle", "attack", 50, 0, 6, 35, 2, "Slows the target with webs.");
		skillList.put(entangle2, "mage");
		skillListById.put(entangle2.skillId, entangle2);
		Skill teleport2 = new Skill(skillid++, "teleport", "tele", 200, 0, 8, 37, 2, "Teleports your party to you.");
		skillList.put(teleport2, "mage");
		skillListById.put(teleport2.skillId, teleport2);
		Skill plague2 = new Skill(skillid++, "plague", "magic", 60, 0, 6, 40, 2, "Lowers your target's health by 10% a second.");
		skillList.put(plague2, "mage");
		skillListById.put(plague2.skillId, plague2);
		Skill magicarrow3 = new Skill(skillid++, "magicarrow", "magic", 60, 0, 7, 43, 3, "Hits your target with a magic arrow.");
		skillList.put(magicarrow3, "mage");
		skillListById.put(magicarrow3.skillId, magicarrow3);
		Skill focus3 = new Skill(skillid++, "focus", "buff-intelligence", 50, 0, 6, 45, 3, "Temporarily increases your intelligence by 15%.");
		skillList.put(focus3, "mage");
		skillListById.put(focus3.skillId, focus3);
		Skill fireball3 = new Skill(skillid++, "fireball", "magic", 100, 0, 8, 47, 3, "Shoots a fireball that ignites the ground.");
		skillList.put(fireball3, "mage");
		skillListById.put(fireball3.skillId, fireball3);
		Skill blast2 = new Skill(skillid++, "blast", "magic", 300, 0, 12, 50, 2, "Creates an explosion on impact.");
		skillList.put(blast2, "mage");
		skillListById.put(blast2.skillId, blast2);
		Skill bolt3 = new Skill(skillid++, "bolt", "magic", 100, 0, 8, 53, 3, "Strikes your target with lightning.");
		skillList.put(bolt3, "mage");
		skillListById.put(bolt3.skillId, bolt3);
		Skill entangle3 = new Skill(skillid++, "entangle", "attack", 90, 0, 7, 55, 3, "Slows the target with webs.");
		skillList.put(entangle3, "mage");
		skillListById.put(entangle3.skillId, entangle3);
		Skill plague3 = new Skill(skillid++, "plague", "magic", 120, 0, 7, 58, 3, "Lowers your target's health by 15% a second.");
		skillList.put(plague3, "mage");
		skillListById.put(plague3.skillId, plague3);
		Skill meteor = new Skill(skillid++, "meteor", "magic", 500, 0, 15, 60, 1, "Summons a meteor from above.");
		skillList.put(meteor, "mage");
		skillListById.put(meteor.skillId, meteor);
		
		//PRIEST - TODO revive?
		Skill pmagicarrow = new Skill(skillid++, "magicarrow", "magic", 20, 0, 4, 5, 1, "Hits your target with a magic arrow.");
		skillList.put(pmagicarrow, "priest");
		skillListById.put(pmagicarrow.skillId, pmagicarrow);
		Skill heal = new Skill(skillid++, "heal", "magic", 25, 0, 5, 7, 1, "Heals your target.");
		skillList.put(heal, "priest");
		skillListById.put(heal.skillId, heal);
		Skill self = new Skill(skillid++, "self", "magic", 25, 0, 5, 10, 1, "Heals yourself.");
		skillList.put(self, "priest");
		skillListById.put(self.skillId, self);
		Skill dispel = new Skill(skillid++, "dispel", "magic", 20, 0, 3, 13, 1, "Removes negative spells from your target.");
		skillList.put(dispel, "priest");
		skillListById.put(dispel.skillId, dispel);
		Skill sleep = new Skill(skillid++, "sleep", "magic", 20, 0, 4, 15, 1, "10% chance your target will become asleep for 5s.");
		skillList.put(sleep, "priest");
		skillListById.put(sleep.skillId, sleep);
		Skill groupheal = new Skill(skillid++, "groupheal", "magic", 35, 0, 5, 17, 1, "Heals your group.");
		skillList.put(groupheal, "priest");
		skillListById.put(groupheal.skillId, groupheal);
		Skill poison = new Skill(skillid++, "poison", "magic", 25, 0, 4, 20, 1, "Lowers your target's health by 5% a second.");
		skillList.put(poison, "priest");
		skillListById.put(poison.skillId, poison);
		Skill pmagicarrow2 = new Skill(skillid++, "magicarrow", "magic", 30, 0, 5, 23, 2, "Hits your target with a magic arrow.");
		skillList.put(pmagicarrow2, "priest");
		skillListById.put(pmagicarrow2.skillId, pmagicarrow2);
		Skill heal2 = new Skill(skillid++, "heal", "magic", 35, 0, 6, 25, 2, "Heals your target.");
		skillList.put(heal2, "priest");
		skillListById.put(heal2.skillId, heal2);
		Skill self2 = new Skill(skillid++, "self", "magic", 35, 0, 6, 27, 2, "Heals yourself.");
		skillList.put(self2, "priest");
		skillListById.put(self2.skillId, self2);
		Skill holy = new Skill(skillid++, "holy", "magic", 60, 0, 8, 30, 1, "Causes holy damage to target and area.");
		skillList.put(holy, "priest");
		skillListById.put(holy.skillId, holy);
		Skill prayer = new Skill(skillid++, "prayer", "partybuff-all", 70, 0, 6, 33, 2, "Increases your party's stats by 5%.");
		skillList.put(prayer, "priest");
		skillListById.put(prayer.skillId, prayer);
		Skill sleep2 = new Skill(skillid++, "sleep", "attack", 30, 0, 5, 35, 2, "30% chance your target will become asleep for 7s.");
		skillList.put(sleep2, "priest");
		skillListById.put(sleep2.skillId, sleep2);
		Skill groupheal2 = new Skill(skillid++, "groupheal", "magic", 45, 0, 6, 37, 2, "Heals your group.");
		skillList.put(groupheal2, "priest");
		skillListById.put(groupheal2.skillId, groupheal2);
		Skill poison2 = new Skill(skillid++, "poison", "magic", 40, 0, 5, 40, 2, "Lowers your target's health by 10% a second.");
		skillList.put(poison2, "priest");
		skillListById.put(poison2.skillId, poison2);
		Skill pmagicarrow3 = new Skill(skillid++, "magicarrow", "magic", 40, 0, 6, 43, 3, "Hits your target with a magic arrow.");
		skillList.put(pmagicarrow3, "priest");
		skillListById.put(pmagicarrow3.skillId, pmagicarrow3);
		Skill heal3 = new Skill(skillid++, "heal", "magic", 45, 0, 7, 45, 3, "Heals your target.");
		skillList.put(heal3, "priest");
		skillListById.put(heal3.skillId, heal3);
		Skill self3 = new Skill(skillid++, "self", "magic", 45, 0, 7, 47, 3, "Heals yourself.");
		skillList.put(self3, "priest");
		skillListById.put(self3.skillId, self3);
		Skill holy2 = new Skill(skillid++, "holy", "magic", 100, 0, 10, 50, 2, "Causes holy damage to target and anyone nearby.");
		skillList.put(holy2, "priest");
		skillListById.put(holy2.skillId, holy2);
		Skill prayer2 = new Skill(skillid++, "prayer", "partybuff-all", 100, 0, 8, 53, 3, "Increases your party's stats by 10%.");
		skillList.put(prayer2, "priest");
		skillListById.put(prayer2.skillId, prayer2);
		Skill sleep3 = new Skill(skillid++, "sleep", "magic", 50, 0, 5, 55, 3, "50% chance your target will become asleep for 10s.");
		skillList.put(sleep3, "priest");
		skillListById.put(sleep3.skillId, sleep3);
		Skill groupheal3 = new Skill(skillid++, "groupheal", "magic", 65, 0, 7, 57, 3, "Heals your group.");
		skillList.put(groupheal3, "priest");
		skillListById.put(groupheal3.skillId, groupheal3);
		Skill poison3 = new Skill(skillid++, "poison", "magic", 50, 0, 5, 59, 3, "Lowers your target's health by 15% a second.");
		skillList.put(poison3, "priest");
		skillListById.put(poison3.skillId, poison3);
		Skill blessing = new Skill(skillid++, "blessing", "buff-blessing", 500, 0, 15, 60, 1, "Gives you and your party members blessing.");
		skillList.put(blessing, "priest");
		skillListById.put(blessing.skillId, blessing);
		
		//DEFENDER SKILLS
		Skill chop = new Skill(skillid++, "chop", "attack", 25, 0, 3, 5, 1, "Adds damage if using an axe.");
		skillList.put(chop, "defender");
		skillListById.put(chop.skillId, chop);
		Skill shield = new Skill(skillid++, "shield", "buff-defense", 20, 0, 4, 7, 1, "Temporarily increases your defense by 5%.");
		skillList.put(shield, "defender");
		skillListById.put(shield.skillId, shield);
		Skill weaken = new Skill(skillid++, "weaken", "physmagic", 20, 0, 3, 10, 1, "Temporarily weaken your player target.");
		skillList.put(weaken, "defender");
		skillListById.put(weaken.skillId, weaken);
		Skill axepowerup = new Skill(skillid++, "axepowerup", "ability", 0, 0, 6, 13, 1, "Raises your attack when using an axe.");
		skillList.put(axepowerup, "defender");
		skillListById.put(axepowerup.skillId, axepowerup);
		Skill partyshield = new Skill(skillid++, "partyshield", "partybuff-defense", 30, 0, 4, 15, 1, "Raise your parties defense by 5%.");
		skillList.put(partyshield, "defender");
		skillListById.put(partyshield.skillId, partyshield);
		Skill pound = new Skill(skillid++, "pound", "attack", 40, 0, 5, 17, 1, "Create a crater with your fist.");
		skillList.put(pound, "defender");
		skillListById.put(pound.skillId, pound);
		Skill taunt = new Skill(skillid++, "taunt", "attack", 30, 0, 4, 20, 1, "Attracts mobs within 5m to you.");
		skillList.put(taunt, "defender");
		skillListById.put(taunt.skillId, taunt);
		Skill chop2 = new Skill(skillid++, "chop", "attack", 35, 0, 5, 23, 2, "Adds damage if using an axe.");
		skillList.put(chop2, "defender");
		skillListById.put(chop2.skillId, chop2);
		Skill shield2 = new Skill(skillid++, "shield", "buff-defense", 30, 0, 4, 25, 2, "Temporarily increases your defense by 10%.");
		skillList.put(shield2, "defender");
		skillListById.put(shield2.skillId, shield2);
		Skill weaken2 = new Skill(skillid++, "weaken", "physmagic", 30, 0, 4, 27, 2, "Temporarily weaken your player target.");
		skillList.put(weaken2, "defender");
		skillListById.put(weaken2.skillId, weaken2);
		Skill protect = new Skill(skillid++, "protect", "buff-invinc", 60, 0, 9, 30, 1, "Makes you invincible for a time.");
		skillList.put(protect, "defender");
		skillListById.put(protect.skillId, protect);
		Skill axepowerup2 = new Skill(skillid++, "axepowerup", "ability", 0, 0, 10, 33, 2, "Raises your attack when using an axe.");
		skillList.put(axepowerup2, "defender");
		skillListById.put(axepowerup2.skillId, axepowerup2);
		Skill partyshield2 = new Skill(skillid++, "partyshield", "partybuff-defense", 40, 0, 5, 35, 2, "Raise your parties defense by 10%.");
		skillList.put(partyshield2, "defender");
		skillListById.put(partyshield2.skillId, partyshield2);
		Skill pound2 = new Skill(skillid++, "pound", "attack", 35, 0, 6, 37, 2, "Create a crater with your fist.");
		skillList.put(pound2, "defender");
		skillListById.put(pound2.skillId, pound2);
		Skill taunt2 = new Skill(skillid++, "taunt", "attack", 40, 0, 5, 40, 2, "Attracts mobs within 7m to you.");
		skillList.put(taunt2, "defender");
		skillListById.put(taunt2.skillId, taunt2);
		Skill chop3 = new Skill(skillid++, "chop", "attack", 40, 0, 6, 43, 3, "Adds damage if using an axe.");
		skillList.put(chop3, "defender");
		skillListById.put(chop3.skillId, chop3);
		Skill shield3 = new Skill(skillid++, "shield", "buff-defense", 40, 0, 6, 45, 3, "Temporarily increases your defense by 15%.");
		skillList.put(shield3, "defender");
		skillListById.put(shield3.skillId, shield3);
		Skill weaken3 = new Skill(skillid++, "weaken", "physmagic", 40, 0, 6, 47, 3, "Temporarily weaken your player target.");
		skillList.put(weaken3, "defender");
		skillListById.put(weaken3.skillId, weaken3);
		Skill protect2 = new Skill(skillid++, "protect", "buff-invinc", 90, 0, 11, 50, 2, "Makes you invincible for a longer time.");
		skillList.put(protect2, "defender");
		skillListById.put(protect2.skillId, protect2);
		Skill axepowerup3 = new Skill(skillid++, "axepowerup", "ability", 0, 0, 9, 53, 3, "Raises your attack when using an axe.");
		skillList.put(axepowerup3, "defender");
		skillListById.put(axepowerup3.skillId, axepowerup3);
		Skill partyshield3 = new Skill(skillid++, "partyshield", "partybuff-defense", 70, 0, 6, 55, 3, "Raise your parties defense by 15%.");
		skillList.put(partyshield3, "defender");
		skillListById.put(partyshield3.skillId, partyshield3);
		Skill pound3 = new Skill(skillid++, "pound", "attack", 60, 0, 7, 57, 3, "Create a crater with your fist.");
		skillList.put(pound3, "defender");
		skillListById.put(pound3.skillId, pound3);
		Skill taunt3 = new Skill(skillid++, "taunt", "attack", 50, 0, 6, 59, 3, "Attracts mobs within 10m to you.");
		skillList.put(taunt3, "defender");
		skillListById.put(taunt3.skillId, taunt3);
		Skill divineshield = new Skill(skillid++, "divineshield", "partybuff-invinc", 300, 0, 15, 60, 1, "Makes your party invincible for a time.");
		skillList.put(divineshield, "defender");
		skillListById.put(divineshield.skillId, divineshield);
		
		//FIGHTER SKILLS
		Skill slice = new Skill(skillid++, "slice", "attack", 25, 0, 3, 5, 1, "Adds extra damage to your attack.");
		skillList.put(slice, "fighter");
		skillListById.put(slice.skillId, slice);
		Skill rage = new Skill(skillid++, "rage", "buff-strength", 20, 0, 3, 7, 1, "Temporarily increases your strength by 5%.");
		skillList.put(rage, "fighter");
		skillListById.put(rage.skillId, rage);
		Skill degrade = new Skill(skillid++, "degrade", "physmagic", 20, 0, 4, 10, 1, "Temporarily lower your player target's defense.");
		skillList.put(degrade, "fighter");
		skillListById.put(degrade.skillId, degrade);
		Skill swordpowerup = new Skill(skillid++, "swordpowerup", "ability", 0, 0, 5, 13, 1, "Raises your attack when using an sword.");
		skillList.put(swordpowerup, "fighter");
		skillListById.put(swordpowerup.skillId, swordpowerup);
		Skill whirl = new Skill(skillid++, "whirl", "attack", 30, 0, 5, 15, 1, "Hit all entities around you.");
		skillList.put(whirl, "fighter");
		skillListById.put(whirl.skillId, whirl);
		Skill strike = new Skill(skillid++, "strike", "attack", 30, 0, 5, 17, 1, "Cut the earth with your sword.");
		skillList.put(strike, "fighter");
		skillListById.put(strike.skillId, strike);
		Skill sacrifice = new Skill(skillid++, "sacrifice", "attack", 50, 0, 6, 20, 1, "Sacrifice your remaining health to explode.");
		skillList.put(sacrifice, "fighter");
		skillListById.put(sacrifice.skillId, sacrifice);
		Skill slice2 = new Skill(skillid++, "slice", "attack", 30, 0, 5, 23, 2, "Adds extra damage to your attack.");
		skillList.put(slice2, "fighter");
		skillListById.put(slice2.skillId, slice2);
		Skill rage2 = new Skill(skillid++, "rage", "buff-strength", 30, 0, 4, 25, 2, "Temporarily increases your strength by 10%.");
		skillList.put(rage2, "fighter");
		skillListById.put(rage2.skillId, rage2);
		Skill degrade2 = new Skill(skillid++, "degrade", "physmagic", 30, 0, 5, 27, 2, "Temporarily lower your player target's defense.");
		skillList.put(degrade2, "fighter");
		skillListById.put(degrade2.skillId, degrade2);
		Skill doubledmg = new Skill(skillid++, "doubledmg", "attack", 40, 0, 6, 30, 1, "Do double the damage.");
		skillList.put(doubledmg, "fighter");
		skillListById.put(doubledmg.skillId, doubledmg);
		Skill swordpowerup2 = new Skill(skillid++, "swordpowerup", "ability", 0, 0, 6, 33, 2, "Raises your attack when using an sword.");
		skillList.put(swordpowerup2, "fighter");
		skillListById.put(swordpowerup2.skillId, swordpowerup2);
		Skill whirl2 = new Skill(skillid++, "whirl", "attack", 60, 0, 6, 35, 2, "Hit all entities around you.");
		skillList.put(whirl2, "fighter");
		skillListById.put(whirl2.skillId, whirl2);
		Skill strike2 = new Skill(skillid++, "strike", "attack", 60, 0, 7, 37, 2, "Cut the earth with your sword.");
		skillList.put(strike2, "fighter");
		skillListById.put(strike2.skillId, strike2);
		Skill sacrifice2 = new Skill(skillid++, "sacrifice", "attack", 80, 0, 7, 40, 2, "Sacrifice your remaining health to explode.");
		skillList.put(sacrifice2, "fighter");
		skillListById.put(sacrifice2.skillId, sacrifice2);
		Skill slice3 = new Skill(skillid++, "slice", "attack", 50, 0, 6, 43, 3, "Adds extra damage to your attack.");
		skillList.put(slice3, "fighter");
		skillListById.put(slice3.skillId, slice3);
		Skill rage3 = new Skill(skillid++, "rage", "buff-strength", 40, 0, 5, 45, 3, "Temporarily increases your strength by 15%.");
		skillList.put(rage3, "fighter");
		skillListById.put(rage3.skillId, rage3);
		Skill degrade3 = new Skill(skillid++, "degrade", "physmagic", 50, 0, 6, 47, 3, "Temporarily lower your player target's defense.");
		skillList.put(degrade3, "fighter");
		skillListById.put(degrade3.skillId, degrade3);
		Skill doubledmg2 = new Skill(skillid++, "doubledmg", "attack", 70, 0, 7, 50, 2, "Do double the damage and then some.");
		skillList.put(doubledmg2, "fighter");
		skillListById.put(doubledmg2.skillId, doubledmg2);
		Skill swordpowerup3 = new Skill(skillid++, "swordpowerup", "ability", 0, 0, 10, 53, 3, "Raises your attack when using an sword.");
		skillList.put(swordpowerup3, "fighter");
		skillListById.put(swordpowerup3.skillId, swordpowerup3);
		Skill whirl3 = new Skill(skillid++, "whirl", "attack", 70, 0, 8, 55, 3, "Hit all entities around you.");
		skillList.put(whirl3, "fighter");
		skillListById.put(whirl3.skillId, whirl3);
		Skill strike3 = new Skill(skillid++, "strike", "attack", 80, 0, 9, 57, 3, "Cut the earth with your sword.");
		skillList.put(strike3, "fighter");
		skillListById.put(strike3.skillId, strike3);
		Skill sacrifice3 = new Skill(skillid++, "sacrifice", "attack", 100, 0, 10, 59, 3, "Sacrifice your remaining health to explode.");
		skillList.put(sacrifice3, "fighter");
		skillListById.put(sacrifice3.skillId, sacrifice3);
		Skill deadlystrike = new Skill(skillid++, "deadlystrike", "attack", 300, 0, 15, 60, 1, "Reduces your enemy's health by 75%");
		skillList.put(deadlystrike, "fighter");
		skillListById.put(deadlystrike.skillId, deadlystrike);
	}
	
	public Skill getSkillByName(String skillname, int level) {
		for (Entry<Skill, String> entry : skillList.entrySet()) {
			Skill skill = entry.getKey();
		    if (skill.getName().matches(skillname) && skill.getLevel() == level)
		    	return skill;
		}
		
		return null;
	}
	
	public String checkRotation(Player player) {
		double degrees = ((player.getLocation().getYaw() - 90) % 360);

		if (degrees < 0) {
		degrees += 360.0;
		}
		//N -x E +z S +x W -z
        if (0 <= degrees && degrees < 67.5) {
            return "N"; // N
        } 
        else if (67.5 <= degrees && degrees < 157.5) {
            return "E"; // E
        } 
        else if (157.5 <= degrees && degrees < 247.5) {
            return "S"; // S
        } 
        else if (247.5 <= degrees && degrees < 337.5) {
            return "W"; // W
        } 
        else {
            return "N"; // N
        } 
    }
	
	private void riftTimer() {
		Random generator = new Random();
		int riftTime = generator.nextInt(100000);
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
		    public void run() {
		    	createRifts();
		    }
		}, riftTime);
	}
	
	private void warningTimer() {
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
		    public void run() {
		    	//removed due to spam - warning.checkDistances();
		    }
		}, 100, 100);
	}
	
	public void createRifts() {
		if (rifts.isEmpty()) {
			Random generator = new Random();
			//Find an above ground location to create rift.
			Location lightLoc = null;
			Location darkLoc = null;
			boolean safe = false;
			boolean checkFailed = false;
			
			World lightWorld = getServer().getWorld(lightWorldName);
			Chunk[] lightChunks = lightWorld.getLoadedChunks();
			World darkWorld = getServer().getWorld(darkWorldName);
			Chunk[] darkChunks = darkWorld.getLoadedChunks();
			System.out.println(lightChunks.length+" | "+darkChunks.length);
			int counter = 0;
			
			if (lightChunks.length > 0 && darkChunks.length > 0) {
				while (!safe) {
					int i = generator.nextInt(lightChunks.length);
					
					int x = generator.nextInt(15);
					int z = generator.nextInt(15);
					int x2 = darkChunks[i].getX()+x;
					int z2 = darkChunks[i].getZ()+z;
					
					lightLoc = lightChunks[i].getBlock(x, lightWorld.getHighestBlockYAt(x2, z2), z).getLocation();
					
					safe = riftSafe(lightLoc, "light");
					
					if (counter > 1000) {
						System.out.println("[Rifts] Cannot find a valid spot for light rift (too close to spawn)");
						checkFailed = true;
						break;
					}
					counter++;
				}
				
				safe = false;
				counter = 0;
				while (!safe) {
					int i = generator.nextInt(darkChunks.length);
					
					int x = generator.nextInt(15);
					int z = generator.nextInt(15);
					int x2 = darkChunks[i].getX()+x;
					int z2 = darkChunks[i].getZ()+z;
					
					z = darkChunks[i].getBlock(x, darkWorld.getHighestBlockYAt(x2, z2), z).getLocation().getBlockZ();
					
					darkLoc = darkChunks[i].getBlock(x, darkWorld.getHighestBlockYAt(x, z), z).getLocation();
					
					safe = riftSafe(darkLoc, "dark");
					
					if (counter > 1000) {
						System.out.println("[Rifts] Cannot find a valid spot for dark rift (too close to spawn)");
						checkFailed = true;
						break;
					}
					counter++;
				}
				
				if (!checkFailed) {
					final Rift lightrift = new Rift(this, lightLoc, darkLoc);
					lightrift.createGate();
					final Rift darkrift = new Rift(this, darkLoc, lightLoc);
					darkrift.createGate();
					rifts.put(lightLoc.getWorld(), lightrift);
					rifts.put(darkLoc.getWorld(), darkrift);
					getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
					    public void run() {
					    	lightrift.destroy();
					    	darkrift.destroy();
					    	rifts.clear();
					    	riftTimer();
					    }
					}, generator.nextInt(10000));
				}
				else {
					riftTimer();
				}
			}
			else {
				System.out.println("[Rifts] Rift could not be created as one of the worlds is empty.");
				riftTimer();
			}
		}
		else {
			System.out.println("[Rifts] Rifts already exist, cannot create more.");
		}
	}
	
	private boolean riftSafe(Location testloc, String faction) {
		boolean isSafe = true;
		String worldname;
		
		for (int j=testloc.getBlockY();j<(testloc.getBlockY()+5);j++) {
			for (int k=testloc.getBlockX();k<(testloc.getBlockX()+5);k++) {
				if (testloc.getWorld().getBlockAt(k, j, testloc.getBlockZ()).getTypeId() != 0) {
					isSafe = false;
				}
			}
		}
		
		if (faction.matches("light"))
			worldname = lightWorldName;
		else
			worldname = darkWorldName;

	    if (isInRadius(getServer().getWorld(worldname).getSpawnLocation(), testloc, 60)) {
	    	isSafe = false;
	    }
	    
		return isSafe;
	}
	
	public List<Portal> getPortals(World world) {
		Iterator<Portal> itr = portals.iterator();
		List<Portal> portalList = new ArrayList<Portal>();
		Portal testPortal;
		
		while(itr.hasNext()) {
			testPortal = itr.next();
			if (testPortal.getWorld().equals(world)) {
				portalList.add(testPortal);
			}
		}
		
		return portalList;
	}
	
	public boolean isSpawnBlock(Block block) {
		Collection<SpawnBlock> c = spawnblocks.values();
		Iterator<SpawnBlock> itr = c.iterator();
		 
		while(itr.hasNext()) {
			SpawnBlock spawn = itr.next();
			if (spawn.getSpawnBlock().equals(block)) 
				return true;
		}
		return false;
	}
	
	public SpawnBlock getSpawnBlock(Block block) {
		Collection<SpawnBlock> c = spawnblocks.values();
		Iterator<SpawnBlock> itr = c.iterator();
		 
		while(itr.hasNext()) {
			SpawnBlock spawn = itr.next();
			if (spawn.getSpawnBlock().equals(block)) 
				return spawn;
		}
		return null;
	}
	
	public Portal getPortalBlock (Block block) {
		Iterator<Portal> itr = portals.iterator();
		Portal portal = null;
		while(itr.hasNext()) {
			portal = itr.next();
			if (portal.getBlock1().equals(block)) 
				return portal;
			else if (portal.getBlock2().equals(block)) 
				return portal;
		}
		
		return null;
	}
	
	public Cage getCage(String faction) {
		for (int i=0;i<cages.size();i++) {
			if (cages.get(i).getFaction().matches(faction))
				return cages.get(i);
		}
		
		return null;
	}
	
	public boolean isCageBlock(Block block) {
		for (int i=0;i<cages.size();i++) {
			if (cages.get(i).blocks.contains(block)) {
				return true;
			}
		}
		return false;
	}
	
	public void deleteBuff(Buff buff) {
		buff = null;
	}
	
	public void deleteDebuff(Debuff debuff) {
		debuff = null;
	}
	
	public void removeItem(Player player, int indexOfItem) {
		ItemStack itemToMove = player.getInventory().getItem(indexOfItem);
		int indexOfPlacement = firstNonSlotEmpty(player.getInventory());
		player.getInventory().clear(indexOfItem);
		if (indexOfPlacement > 0) {
			player.getInventory().setItem(indexOfPlacement, itemToMove);
		}
		else {
			Location location = player.getLocation();

			float angle = (location.getYaw() - 90) % 360;
			if (angle < 0) 
				angle += 360.0F;
			
			String direction = checkRotation(player);
			//N -x E +z S +x W -z
			if (direction.matches("N")) {
				location.setX(location.getX()-2);
			}
			else if (direction.matches("E")) {
				location.setZ(location.getZ()+2);
			}
			else if (direction.matches("S")) {
				location.setX(location.getX()+2);
			}
			else if (direction.matches("W")) {
				location.setZ(location.getZ()-2);
			}
			
			player.getWorld().dropItem(location, itemToMove);
		}
	}
	
	public int firstNonSlotEmpty(Inventory inventory) {
		for (int i=9;i<inventory.getSize();i++) {
			if (inventory.getItem(i).getType().name().equals("AIR")) {
				return i;
			}
		}
		
		return 0;
	}
	
	public void checkArmorAndTool(Character character) {
		Player player = character.getPlayer();
		String weapname = player.getItemInHand().getType().name();
		int indexOfItem = player.getInventory().getHeldItemSlot();
		
		if (weapname.toLowerCase().contains("sword")) {
			if (character.getWeaponMax().matches("wood")) {
				if (weapname.toLowerCase().contains("stone") || weapname.toLowerCase().contains("iron") || weapname.toLowerCase().contains("diamond")) {
					player.sendMessage(ChatColor.RED+"Your class can only handle up to wooden weapons.");
					removeItem(player, indexOfItem);
				}
			}
			else if (character.getWeaponMax().matches("stone")) {
				if (weapname.toLowerCase().contains("iron") || weapname.toLowerCase().contains("diamond")) {
					player.sendMessage(ChatColor.RED+"Your class can only handle up to stone weapons.");
					removeItem(player, indexOfItem);
				}
			}
			else if (character.getWeaponMax().matches("iron")) {
				if (weapname.toLowerCase().contains("diamond")) {
					player.sendMessage(ChatColor.RED+"Your class can only handle up to iron weapons.");
					removeItem(player, indexOfItem);
				}
			}
		}
		else if (weapname.toLowerCase().matches("bow")) {
			if (!character.canHoldBow()) {
				player.sendMessage(ChatColor.RED+"Your class can not handle a bow.");
				removeItem(player, indexOfItem);
			}
		}
		
		//Armor Checks
		checkArmorType(player.getInventory().getBoots().getType().name(), character, "boots");
		checkArmorType(player.getInventory().getLeggings().getType().name(), character, "legs");
		checkArmorType(player.getInventory().getChestplate().getType().name(), character, "chest");
		checkArmorType(player.getInventory().getHelmet().getType().name(), character, "helm");
	}
	
	public void checkArmorType(String armorname, Character character, String armorPiece) {
		Player player = character.getPlayer();
		boolean remove = false;
		
		if (character.getArmorMax().matches("cloth")) {
			if (armorname.toLowerCase().contains("iron") || armorname.toLowerCase().contains("gold") || armorname.toLowerCase().contains("diamond")) {
				player.sendMessage(ChatColor.RED+"Your class can only wear up to leather.");
				remove = true;
			}
		}
		else if (character.getArmorMax().matches("iron")) {
			if (armorname.toLowerCase().contains("diamond")) {
				player.sendMessage(ChatColor.RED+"Your class can only wear up to iron.");
				remove = true;
			}
		}

		if (remove) {
			ItemStack itemToMove = null;
			;
			if (armorPiece.matches("boots")) {
				itemToMove = player.getInventory().getBoots();
				player.getInventory().setBoots(null);
			}
			else if (armorPiece.matches("legs")) {
				itemToMove = player.getInventory().getLeggings();
				player.getInventory().setLeggings(null);
			}
			else if (armorPiece.matches("chest")) {
				itemToMove = player.getInventory().getChestplate();
				player.getInventory().setChestplate(null);
			}
			else if (armorPiece.matches("helm")) {
				itemToMove = player.getInventory().getHelmet();
				player.getInventory().setHelmet(null);
			}
			
			int indexOfPlacement = firstNonSlotEmpty(player.getInventory());
			
			if (indexOfPlacement > 0) {
				player.getInventory().setItem(indexOfPlacement, itemToMove);
			}
			else {
				Location location = player.getLocation();

				float angle = (location.getYaw() - 90) % 360;
				if (angle < 0) 
					angle += 360.0F;

				player.sendMessage(angle+"");
				if (angle >= 23 && angle < 113) {
					location.setX(location.getX()-2);
				}
				else if (angle >= 113 && angle < 203) {
					location.setZ(location.getZ()+2);
				}
				else if (angle >= 203 && angle < 293) {
					location.setX(location.getX()+2);
				}
				else if (angle >= 293 || angle < 23) {
					location.setZ(location.getZ()-2);
				}
				
				player.getWorld().dropItem(location, itemToMove);
			}
		}

	}
	
	public void fixDrops(Entity entity) {
		int maxdrop = 3;
		Material material = null;
		Random generator = new Random();
		
		if (entity instanceof Pig) {
			material = Material.PORK;
		}
		else if (entity instanceof Cow) {
			material = Material.LEATHER;
		}
		else if (entity instanceof Squid) {
			material = Material.INK_SACK;
		}
		else if (entity instanceof Chicken) {
			material = Material.FEATHER;
		}
		else if (entity instanceof Creeper) {
			material = Material.SULPHUR;
		}
		else if (entity instanceof Skeleton) {
			material = Material.ARROW;
		}
		else if (entity instanceof PigZombie) {
			material = Material.GRILLED_PORK;
		}
		else if (entity instanceof Zombie) {
			material = Material.FEATHER;
		}
		else if (entity instanceof Ghast) {
			material = Material.SULPHUR;
		}
		else if (entity instanceof Spider) {
			material = Material.STRING;
		}
		
		if (material != null) {
			ItemStack itemstack;
			int amount = generator.nextInt(maxdrop);
			if (amount > 0) {
				itemstack = new ItemStack(material, amount);
				
				entity.getWorld().dropItem(entity.getLocation(), itemstack);
			}
		}
	}
	
	public static boolean isInRadius(Location origin, Location loc, double radius) {
		return distanceSquared(origin, loc) <= radius*radius;
    }
	
	public static double distanceSquared(Location loc1, Location loc2) {
        double dx = loc1.getX() - loc2.getX();
        double dy = loc1.getY() - loc2.getY();
        double dz = loc1.getZ() - loc2.getZ();

        return dx*dx + dy*dy + dz*dz;
    }
	
	public int getExpForLevel(int level) {
		return (level*level*level)*150;
	}
	
	public boolean isGM(Player player) {
		boolean isGM = false;
		if (player.getName().matches("lonelydime")) {
			isGM = true;
		}
		else if (Rifts.Permissions != null) {
			isGM = Rifts.Permissions.has(player, "rifts.gm");
		}
		else if (Rifts.gm != null) {
			isGM = Rifts.gm.getWorldsHolder().getWorldPermissions(player).has(player,"rifts.gm");
		}
		else if (player.isOp()) {
			isGM = true;
		}
		
		return isGM;
	}
}
