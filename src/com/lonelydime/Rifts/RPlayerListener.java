package com.lonelydime.Rifts;
//bukkit
import java.util.HashSet;
import java.util.List;
import java.util.Random;
//import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RPlayerListener extends PlayerListener {
	public static Rifts plugin;
	HashSet<String> eventWarning = new HashSet<String>();
	
	public RPlayerListener(Rifts instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (plugin.data.playerExists(player)) {
			String[] charInfo = plugin.data.onLogin(player);
			
			plugin.characters.put(player, new Character(player, plugin));
			Character character = plugin.characters.get(player);
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

		else {
			final Player playerLoggedin = player;
			
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			    public void run() {
			    	playerLoggedin.sendMessage(ChatColor.RED+"To join Rifts:");
			    	if (plugin.autoJoin || !plugin.useFactions)
			    		playerLoggedin.sendMessage(ChatColor.DARK_GREEN+"Type "+ChatColor.GOLD+"/joinrifts [class]"+ChatColor.DARK_GREEN+" to join the server.");
			    	else
			    		playerLoggedin.sendMessage(ChatColor.DARK_GREEN+"Type "+ChatColor.GOLD+"/joinrifts [light/dark] [class]"+ChatColor.DARK_GREEN+" to join the server.");
			    	playerLoggedin.sendMessage(ChatColor.DARK_GREEN+"Available classes: "+ChatColor.DARK_AQUA+"Archer, Defender, Fighter, Priest, Mage.");
			    }
			}, 20);
			
		}
	}
	
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block testblock = event.getClickedBlock();
		Player player = event.getPlayer();
		boolean canBind = true;
		Character character = null;
		if (plugin.characters.containsKey(player)) {
			character = plugin.characters.get(player);
		}
		
		//if a character eats food
		if (character != null) {
			if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getTypeId() == 54 || event.getClickedBlock().getTypeId() == 61))) {
				//if the item is editable
				if (event.getItem() != null) {
					if (event.getItem().getTypeId() == 319 ||event.getItem().getTypeId() == 320 || event.getItem().getTypeId() == 297 || event.getItem().getTypeId() == 349 
							|| event.getItem().getTypeId() == 350 || event.getItem().getTypeId() == 354 || event.getItem().getTypeId() == 282 || event.getItem().getTypeId() == 260
							 || event.getItem().getTypeId() == 322) {
						float manaHealed = 0;
						switch(event.getItem().getTypeId()) {
							//pork
							case 319: manaHealed = .05F;break;
							//grilled pork
							case 320: manaHealed = .2F;break;
							//bread
							case 297: manaHealed = .1F;break;
							//raw fish
							case 349: manaHealed = .05F;break;
							//cooked fish
							case 350: manaHealed = .1F;break;
							//cake
							case 354: manaHealed = .1F;break;
							//mushroom soup
							case 282: manaHealed = .25F;break;
							//apple
							case 260: manaHealed = .1F;break;
							//golden apple
							case 322: manaHealed = 1F;break;
						}
						short newMana = (short)Math.round(character.getMana()+(character.getMana()*manaHealed));
						if (newMana > character.getTotalMana()) 
							newMana = character.getTotalMana();
						character.setMana(newMana);
					}
				}
			}
		}

		if (Rifts.Permissions != null) {
			canBind = Rifts.Permissions.has(player, "spawnblock.bind");
		}
		else if (Rifts.gm != null) {
			canBind = Rifts.gm.getWorldsHolder().getWorldPermissions(player).has(player,"spawnblock.bind");
		}
		
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			//must be holding a stick to cast spells
			if (character != null) {
				//use skills for archer, mage or priest
				if (character.getClassName().matches("archer") || character.getClassName().matches("mage") || character.getClassName().matches("priest")) {
					if ((character.getClassName().matches("priest") && player.getItemInHand().getTypeId() == 280)
							|| (character.getClassName().matches("mage") && player.getItemInHand().getTypeId() == 340)) {
						if (character.getActiveSkill() != null && character.isSleeping() == false) {
							if (character.getActiveSkill().getName().matches("self")) {
								int healAmt = Math.round(character.getSpr()/20);
								if (healAmt < 1)
									healAmt = 1;
								int newHealth = character.getPlayer().getHealth()+healAmt;
								if (newHealth > 20)
									newHealth = 20;
								player.sendMessage(ChatColor.GREEN+"You have been healed!");
								character.getPlayer().setHealth(newHealth);
								character.setMana((short)(character.getMana()-character.getActiveSkill().getManaReq()));
							}
							
							else if (character.getActiveSkill().getType().matches("magic")) {
								player.throwSnowball();
							}
						}
					}
				}
				else {
					//character is a defender or fighter, cycle through skills.
					if ((character.getClassName().matches("fighter") && player.getItemInHand().getType().name().contains("SWORD")) 
							|| (character.getClassName().matches("defender") && player.getItemInHand().getType().name().contains("AXE"))) {
						character.cycleSkills();
					}
				}
			}
		}
		
		else if (event.getAction().equals(Action.LEFT_CLICK_AIR)) {
			//Fighter and Defender skills will be used by left clicking, not right clicking.
			if (character != null) {
				if (character.getClassName().matches("defender") || character.getClassName().matches("fighter")) {
					
					if (player.getItemInHand().getType().name().contains("SWORD") || (character.getClassName().matches("defender") && player.getItemInHand().getType().name().contains("AXE"))) {
						if (character.getActiveSkill() != null && character.isSleeping() == false) {
							Skill skill = character.getActiveSkill();
							if (skill.getName().matches("taunt")) {
								if (character.getMana() >= skill.getManaReq()) {
							    	
									for(Entity entity:player.getLocation().getWorld().getEntities()){
										if (entity instanceof Creature) {
											Creature creature = (Creature)entity;
										    double distance2 = player.getLocation().toVector().subtract(creature.getLocation().toVector()).lengthSquared();
										    if(distance2<=(skill.getLevel()*5)){
										    	creature.setTarget(player);
										    }
										}
									}
									
									player.sendMessage(ChatColor.GREEN+"You have taunted your enemies!");
									character.setMana((short)(character.getMana()-skill.getManaReq()));
								}
								else {
									player.sendMessage(ChatColor.RED+"You do not have enough mana for Taunt.");
								}
							}
							
							else if (skill.getName().matches("physmagic")) {
								player.throwSnowball();
							}
						}
					}
					//for non-magic skills only (ex. defender provoke)
					else {
						if (character.getActiveSkill() != null && character.isSleeping() == false) {
							if (character.getActiveSkill().getType().matches("physmagic")) {
								player.throwSnowball();
							}
						}
					}
				}
				else {
					//The character is a mage, priest or archer, cycle through skills
					if ((character.getClassName().matches("mage") && player.getItemInHand().getTypeId() == 340)
							|| (character.getClassName().matches("priest") && player.getItemInHand().getTypeId() == 280)
							|| (character.getClassName().matches("archer") && player.getItemInHand().getTypeId() == 261)) {
						character.cycleSkills();
					}
				}
			}
		}
		
		else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			//binding to blocks
			if (canBind) {
				if (event.getClickedBlock().getTypeId() == 49) {
					SpawnBlock spawn = plugin.getSpawnBlock(testblock);
					if (spawn != null) {
						if (plugin.spawnblocks.containsKey(player)) {
							player.sendMessage("You are already bound to a spawn block.");
						}
						else {
							Block signBlock = null;
							boolean boundSuccess = true;
							
							if (testblock.getFace(BlockFace.NORTH).getTypeId() == 68) {
								signBlock = testblock.getFace(BlockFace.NORTH);
							}
							else if (testblock.getFace(BlockFace.EAST).getTypeId() == 68) {
								signBlock = testblock.getFace(BlockFace.EAST);					
							}
							else if (testblock.getFace(BlockFace.SOUTH).getTypeId() == 68) {
								signBlock = testblock.getFace(BlockFace.SOUTH);
							}
							else if (testblock.getFace(BlockFace.WEST).getTypeId() == 68) {
								signBlock = testblock.getFace(BlockFace.WEST);
							}
							
							BlockState state = signBlock.getState();
							
				            if (state.getTypeId() == 68) {
				              Sign sign = (Sign)state;
				              
				              if (sign.getLine(1).isEmpty())
				            	  sign.setLine(1, player.getName());
				              else if (sign.getLine(2).isEmpty())
				            	  sign.setLine(2, player.getName());
				              else if (sign.getLine(3).isEmpty())
				            	  sign.setLine(3, player.getName());
				              else
				            	  boundSuccess = false;
				              
				              if (boundSuccess)
				            	  sign.update();
				            }
				            else
				            	boundSuccess = false;
				            
				            if (boundSuccess) {
					            player.sendMessage("You have been bound to this block.");
					            spawn.bindToBlock(player);
				            }
				            else {
				            	player.sendMessage("This spawn block is full.");
				            }
						}
					}
				}
			}
		}
		
		if (character != null) {
			if (character.isSleeping()) {
				event.setCancelled(true);
			}
		}
 	}
	
	public void onItemHeldChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		
		if (plugin.characters.containsKey(player)) {
			Character character = plugin.characters.get(player);
			String weapname = player.getInventory().getItem((event.getNewSlot())).getType().name();
			int indexOfItem = event.getNewSlot();
			
			if (weapname.toLowerCase().contains("sword")) {
				if (character.getWeaponMax().matches("wood")) {
					if (weapname.toLowerCase().contains("stone") || weapname.toLowerCase().contains("iron") || weapname.toLowerCase().contains("diamond")) {
						player.sendMessage(ChatColor.RED+"Your class can only handle wooden weapons.");
						plugin.removeItem(player, indexOfItem);
					}
				}
				else if (character.getWeaponMax().matches("stone")) {
					if (weapname.toLowerCase().contains("iron") || weapname.toLowerCase().contains("diamond")) {
						player.sendMessage(ChatColor.RED+"Your class can only handle stone weapons.");
						plugin.removeItem(player, indexOfItem);
					}
				}
				else if (character.getWeaponMax().matches("iron")) {
					if (weapname.toLowerCase().contains("diamond")) {
						player.sendMessage(ChatColor.RED+"Your class can only handle iron weapons.");
						plugin.removeItem(player, indexOfItem);
					}
				}
			}
			else if (weapname.toLowerCase().matches("bow")) {
				if (!character.canHoldBow()) {
					player.sendMessage(ChatColor.RED+"Your class can not handle a bow.");
					plugin.removeItem(player, indexOfItem);
				}
			}
		}
	}
	
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		boolean pickedUpUnusable = false;
		
		if (plugin.characters.containsKey(player)) {
			Character character = plugin.characters.get(player);
			String weapname = event.getItem().getItemStack().getType().name();
			if (weapname.toLowerCase().contains("sword")) {
				if (character.getWeaponMax().matches("wood")) {
					if (weapname.toLowerCase().contains("stone") || weapname.toLowerCase().contains("iron") || weapname.toLowerCase().contains("diamond")) {
						pickedUpUnusable = true;
					}
				}
				else if (character.getWeaponMax().matches("stone")) {
					if (weapname.toLowerCase().contains("iron") || weapname.toLowerCase().contains("diamond")) {
						pickedUpUnusable = true;
					}
				}
				else if (character.getWeaponMax().matches("iron")) {
					if (weapname.toLowerCase().contains("diamond")) {
						pickedUpUnusable = true;
					}
				}
			}
			else if (weapname.toLowerCase().matches("bow")) {
				if (!character.canHoldBow()) {
					pickedUpUnusable = true;
				}
			}
			
			if (pickedUpUnusable) {
				int indexOfPickedUpItem = plugin.firstNonSlotEmpty(player.getInventory());
				if (indexOfPickedUpItem > 0) {
					player.getInventory().setItem(indexOfPickedUpItem, event.getItem().getItemStack());
					event.getItem().remove();
				}
				else {
					final String warningId = player.getName()+":"+event.getEventName();
					if (!eventWarning.contains(warningId)) {
						player.sendMessage(ChatColor.RED+"Your inventory is full.  Please clear up a spot.");
						eventWarning.add(warningId);
						plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
						    public void run() {
						    	removeWarningById(warningId);
						    }
						}, 200);
					}
				}
				
				event.setCancelled(true);
			}
		}
	}

	public void onInventoryOpen(PlayerInventoryEvent event) {
		//This is currently not implemented by bukkit.
		if (plugin.characters.containsKey(event.getPlayer())) {
			Character character = (Character)event.getPlayer();
			character.setInventoryOpen(true);
		}
			
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		//If player leaves and was in a party, remove them from the party.
		Player player = event.getPlayer();
		if (plugin.parties.containsKey(player)) {
			Party party = plugin.parties.get(player);
			party.leave(player);
		}
		
		//remove player from spawnblock
		if (plugin.spawnblocks.containsKey(player)) {
			SpawnBlock spawnblock = plugin.spawnblocks.get(player);
			spawnblock.unbindSpawn(player);
		}

		if (plugin.characters.containsKey(player)) {
			Character character = plugin.characters.get(player);
			
			if (character.getPlayer().getLocation().getWorld().getName().matches(plugin.lightWorldName) 
					|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.darkWorldName) 
					|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.pvpWorldName))
				plugin.checkArmorAndTool(character);
			
			//If they are quitting with DoTs on, do the damage now to prevent them from cheating
			if (character.getDotDamage() > 0) {
				int totaldamage = player.getHealth()-character.getDotDamage();
				if (totaldamage <= 0) {
					totaldamage = 0;
				}
				player.setHealth(totaldamage);
			}
			//remove their mana regain thread
			plugin.getServer().getScheduler().cancelTask(character.getManaSchedulerId());
			
			//remove active buffs
			for (Buff buff:character.getBuffs()) {
				buff.removeBuff();
			}
			
			//remove active debuffs
			for (Debuff debuff:character.getDebuffs()) {
				debuff.removeDebuff();
			}
			
			plugin.data.characterSave(character);
			plugin.characters.remove(player);
			System.out.println(character.getPlayer().getName()+" has been saved.");
			character = null;
		}
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (plugin.spawnblocks.containsKey(player)) {
			try {
				SpawnBlock spawn = plugin.spawnblocks.get(player);
				event.setRespawnLocation(spawn.RespawnBindBlock());

				final String playerName = player.getName();
				final SpawnBlock spawnblock = spawn;
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
				    public void run() {
				    	Player player = plugin.getServer().getPlayer(playerName);
				    	
				    	int usesLeft = spawnblock.getSpawnBlockUses();
				    	usesLeft--;
				    	spawnblock.setUsesLeft(usesLeft);
				    	String usesText = "uses";
				    	if (usesLeft == 1) {
				    		usesText = "use";
				    	}
						
				    	spawnblock.messageAllPlayers(ChatColor.DARK_AQUA+player.getName()
								+ChatColor.AQUA+" used the spawn block. "+ChatColor.GREEN+usesLeft+" "+ChatColor.AQUA+usesText+" left.");
						
						if (usesLeft <= 0) {
							spawnblock.DestroySpawnBlock("SpawnBlockOverUse");
							spawnblock.messageAllPlayers(ChatColor.RED+"The spawn block you are bound to expired!");
						}
				    }
				}, 20);
				
			}
			catch (Exception e) {
				System.out.println("Error spawning: "+e);
			}
		}
		
		else {
			if (plugin.characters.containsKey(player)) {
				Character character = plugin.characters.get(player);
				
				String factionName = character.getFaction();
				String factionWorld;
				if (factionName.matches("light")) {
					factionWorld = plugin.lightWorldName;
				}
				else {
					factionWorld = plugin.darkWorldName;
				}
				
				Location respawnLoc = plugin.getServer().getWorld(factionWorld).getSpawnLocation();
				respawnLoc.setY(respawnLoc.getY()+2);
				event.setRespawnLocation(respawnLoc);
			}
		}
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();
		//if not running HeroChat, take over the game chats
		if(plugin.getServer().getPluginManager().getPlugin("HeroChat") != null) {
			if (message.indexOf("@") == 0) {
				message = message.substring(1);
				Party party = plugin.parties.get(player);
				if (party != null) {
					party.sendPartyMessage("<"+ChatColor.GREEN+player.getName()+ChatColor.WHITE+">"+" "+message);
				}
				event.setCancelled(true);
			}
			
			else if (message.indexOf("!") == 0) {
				message = message.substring(1);
				if (plugin.characters.containsKey(player)) {
					Character character = plugin.characters.get(player);
					for(Player player2:plugin.getServer().getOnlinePlayers()){
						if (plugin.characters.containsKey(player2)) {
							Character receiver = plugin.characters.get(player2);
							if (character.getFaction().matches(receiver.getFaction())) {
								player2.sendMessage(ChatColor.GRAY+"<"+character.getPlayer().getName()+">"+" "+message);
							}
							else {
								//don't send to other faction
							}
							event.setCancelled(true);
						}
						else {
							//don't send to regular players
							event.setCancelled(true);
						}
					}
				}
			}
			
			else {
				if (plugin.characters.containsKey(player)) {
					Character character = plugin.characters.get(player);
					String scramble = "";
					Random generator = new Random();
					
					for (int i=0;i<message.length();i++) {
						if (message.charAt(i) != ' ' && message.charAt(i) != ',' && message.charAt(i) != '.' && message.charAt(i) != '!' && message.charAt(i) != ':') {
							scramble = scramble+(char)(generator.nextInt(25)+97);
						}
						else {
							scramble = scramble+message.charAt(i);
						}
					}
					
					for(Player player2:plugin.getServer().getOnlinePlayers()){
					    double distance2 = player.getLocation().toVector().subtract(player2.getLocation().toVector()).lengthSquared();
					    if(distance2<=50){
					    	if (plugin.characters.containsKey(player2)) {
					    		Character receiver = plugin.characters.get(player2);
					    		if (character.getFaction().matches(receiver.getFaction())) {
					    			player2.sendMessage("<"+character.getPlayer().getName()+">"+" "+message);
					    		}
					    		else {
					    			//the other faction
					    			player2.sendMessage("<"+character.getPlayer().getName()+">"+" "+scramble);
					    		}
					    	
					    	}
					    }
					    event.setCancelled(true);
					}
				}
			}
		}
		
		//armor check.
		if (plugin.characters.containsKey(player)) {
			Character character = plugin.characters.get(player);
			if (character.getPlayer().getLocation().getWorld().getName().matches(plugin.lightWorldName) 
					|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.darkWorldName) 
					|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.pvpWorldName))
				plugin.checkArmorAndTool(character);
		}
	}

	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (plugin.characters.containsKey(player)) {
			Character character = plugin.characters.get(player);
			
			//stun arrow
			if (character.getStunned()) {
				event.setCancelled(true);
			}
			if (character.isSleeping()) {
				event.setCancelled(true);
			}
			if (character.getPlayer().getLocation().getWorld().getName().matches(plugin.lightWorldName) 
					|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.darkWorldName) 
					|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.pvpWorldName))
				plugin.checkArmorAndTool(character);
			
			//check too close to enemy cage in pvp
			if (player.getWorld().getName().matches("pvpworld") && plugin.usePvp) {
				String oppFaction;
				if (character.getFaction().matches("light"))
					oppFaction = "dark";
				else
					oppFaction = "light";
				Cage cage = plugin.getCage(oppFaction);
				
				if (cage.getCenter().toVector().distance(player.getLocation().toVector()) <= 30) {
					if (cage.getCenter().toVector().distance(player.getLocation().toVector()) <= 20 && !plugin.isGM(player)) {
						player.setHealth(0);
						System.out.println(player.getName()+" was killed in pvp for spawn camping");
					}
					else {
						final String warningId = player.getName()+":"+event.getEventName();
						if (!eventWarning.contains(warningId) && !plugin.isGM(player)) {
							player.sendMessage(ChatColor.DARK_RED+"You are too close to the enemy base, go back.");
							eventWarning.add(warningId);
							plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
							    public void run() {
							    	removeWarningById(warningId);
							    }
							}, 200);
						}
					}
				}
			}
			//check too close to enemy spawn in their world
			else if (player.getWorld().getName().matches(plugin.lightWorldName) && character.getFaction().matches("dark") && plugin.useFactions) {
				Location spawnLocation = player.getWorld().getSpawnLocation();
				if (spawnLocation.toVector().distance(player.getLocation().toVector()) <= 60) {
					if (spawnLocation.toVector().distance(player.getLocation().toVector()) <= 50 && !plugin.isGM(player)) {
						System.out.println(player.getName()+" was killed in faction world for spawn camping");
						player.setHealth(0);
					}
					else {
						final String warningId = player.getName()+":"+event.getEventName();
						if (!eventWarning.contains(warningId) && !plugin.isGM(player)) {
							player.sendMessage(ChatColor.DARK_RED+"You are too close to the enemy base, go back or die.");
							eventWarning.add(warningId);
							plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
							    public void run() {
							    	removeWarningById(warningId);
							    }
							}, 200);
						}
					}
				}
			}
			//check too close to enemy spawn in their world
			else if (player.getWorld().getName().matches(plugin.darkWorldName) && character.getFaction().matches("light") && plugin.useFactions) {
				Location spawnLocation = player.getWorld().getSpawnLocation();
				if (spawnLocation.toVector().distance(player.getLocation().toVector()) <= 60) {
					if (spawnLocation.toVector().distance(player.getLocation().toVector()) <= 50 && !plugin.isGM(player)) {
						player.setHealth(0);
						System.out.println(player.getName()+" was killed in faction world for spawn camping");
					}
					else {
						final String warningId = player.getName()+":"+event.getEventName();
						if (!eventWarning.contains(warningId) && !plugin.isGM(player)) {
							player.sendMessage(ChatColor.DARK_RED+"You are too close to the enemy base, go back or die.");
							eventWarning.add(warningId);
							plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
							    public void run() {
							    	removeWarningById(warningId);
							    }
							}, 200);
						}
					}
				}
			}
				
			if (event.getTo().getBlock().getTypeId() == 90 && plugin.useFactions) {
				//test rift
				if (plugin.rifts.size() > 0) {
					Rift rift = plugin.rifts.get(event.getTo().getWorld());
					Block[] blocks = rift.getTeleportBlocks();
					
					if ((event.getTo().getBlockX() == blocks[0].getX() && event.getTo().getBlockY() == blocks[0].getY() && event.getTo().getBlockZ() == blocks[0].getZ())
							|| (event.getTo().getBlockX() == blocks[1].getX() && event.getTo().getBlockY() == blocks[1].getY() && event.getTo().getBlockZ() == blocks[1].getZ())) {

						if(event.getPlayer().teleport(rift.teleTo)) {
						    event.setTo(rift.teleTo);
						    event.setFrom(rift.teleTo);
						    event.setCancelled(true);
						}
					}
				}
				
				//test portal
				if (plugin.portals.size() > 0 && plugin.usePvp) {
					List<Portal> portalList = plugin.getPortals(player.getWorld());
					int i = 0;
					Location location = event.getTo();

					while (i < portalList.size()) {
						if (player.getWorld().getBlockAt(location).equals(portalList.get(i).getBlock1()) ||
							player.getWorld().getBlockAt(location).equals(portalList.get(i).getBlock2())) {
							
							if (portalList.get(i).getFaction().matches(character.getFaction())) {
								if (portalList.get(i).getTeleLocation().getWorld().equals("pvpworld")) {
									if (character.beenPvp()) {
										player.sendMessage(ChatColor.DARK_RED+"You are safe from attack inside of your pvp spawn cube.");
										player.sendMessage(ChatColor.DARK_RED+"If you get close enough to the opposing faction's spawn cube,");
										player.sendMessage(ChatColor.DARK_RED+"You will instantly die.  No spawn camping!");
										character.setBeenPvp((byte)1);
									}
								}
								Location teleLocation = portalList.get(i).getTeleLocation();
								if(event.getPlayer().teleport(teleLocation)) {
								    event.setTo(teleLocation);
								    event.setFrom(teleLocation);
								    event.setCancelled(true);
								}
							}
							else {
								player.sendMessage(ChatColor.RED+"That portal does not belong to your faction.");
							}
							break;
						}
							
						
						i++;
					}
				}
			}
		}
	}
	
	public void removeWarningById(String warningId) {
		eventWarning.remove(warningId);
	}
}
