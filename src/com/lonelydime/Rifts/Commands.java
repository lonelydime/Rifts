package com.lonelydime.Rifts;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands {
	Rifts plugin;
	
	public Commands(Rifts instance) {
		plugin = instance;
	}
	
	public boolean sendCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String command = cmd.getName();
		boolean canUseCommand = true;
		boolean isGM = false;
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			isGM = plugin.isGM(player);
		}
		else {
			isGM = true;
		}
		
		/**********************************************************
		 * 
		 * Player and console commands
		 * 
		 *********************************************************/
		if (command.equals("class")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player)sender;
					Character character = plugin.characters.get(player);
					if (character != null) {
						String playersClass = character.getClassName();
						String aoran = "a";
						if (playersClass.matches("archer"))
							aoran = "an";
						String className = playersClass.substring(0, 1).toUpperCase()+playersClass.substring(1);
						player.sendMessage(ChatColor.AQUA+"You are "+aoran+" "+ChatColor.GOLD+className);
					}
					return true;
				}
				else
					return false;
			}
			else if (args.length == 1) {
				Player player = plugin.getServer().getPlayer(args[0]);
				if (player != null) {
					if (plugin.characters.containsKey(player)) {
						Character character = plugin.characters.get(player);
						String playersClass = character.getClassName();
						String aoran = "a";
						if (playersClass.matches("archer"))
							aoran = "an";
						String className = playersClass.substring(0, 1).toUpperCase()+playersClass.substring(1);
						
						sender.sendMessage(ChatColor.GREEN+args[0]+ChatColor.AQUA+" is "+aoran+" "+ChatColor.GOLD+className);
					}
					else
						sender.sendMessage(ChatColor.RED+args[0]+" is not registered with Rifts");
				}
				else {
					sender.sendMessage(ChatColor.RED+"That player is not online.");
				}
				return true;
			}
			else
				return false;
		}
		
		/**********************************************************
		 * 
		 * Admin only commands
		 * 
		 *********************************************************/
		
		else if (command.equals("savechars") && isGM) {
			Collection<Character> c = plugin.characters.values();
			Iterator<Character> itr = c.iterator();

			while(itr.hasNext()) {
				plugin.data.characterSave((Character)itr.next());
			}
			sender.sendMessage(ChatColor.DARK_GREEN+"All characters saved.");
			return true;
		}

		else if (command.equals("setclass") && isGM) {
			if (args.length == 2) {
				Player player = plugin.getServer().getPlayer(args[0]);
				if (player != null) {
					if (plugin.characters.containsKey(player)) {
						Character character = plugin.characters.get(player);
						if (args[1].matches("archer") || args[1].matches("mage") || args[1].matches("priest") || args[1].matches("defender") || 
								args[1].matches("fighter")) {
							plugin.data.changeClass(character, args[1]);
							character.setClassName(args[1]);
							character.setSkillList("");
							plugin.reloadCharacter(player);
						}
					}
					else {
						sender.sendMessage(ChatColor.RED+"Player is not registered.");
					}
				}
				else {
					sender.sendMessage(ChatColor.RED+"Player does not exist.");
				}
				return true;
			}
			
			return false;
		}
		
		else if (command.equals("setlevel") && isGM) {
			if (args.length == 2) {
				Player player = plugin.getServer().getPlayer(args[0]);
				if (player != null) {
					if (plugin.characters.containsKey(player)) {
						Character character = plugin.characters.get(player);
						try {
							int newLevel = Integer.parseInt(args[1]);

							plugin.data.changeLevel(character, newLevel);
							character.setLevel((byte)newLevel);
							plugin.reloadCharacter(player);
						}
						catch (Exception e) {
							System.out.println("Error changing level: "+e);
						}
					}
					else {
						sender.sendMessage(ChatColor.RED+"Player is not registered.");
					}
				}
				else {
					sender.sendMessage(ChatColor.RED+"Player does not exist.");
				}
				return true;
			}
			
			return false;
		}
		
		else if (command.equals("fullheal") && isGM) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player)sender;
					if (plugin.characters.containsKey(player)) {
						Character character = plugin.characters.get(player);
						character.setMana(character.getTotalMana());
						player.setHealth(20);
					}
				}
				return true;
			}
			
			return false;
		}
		
		else if (command.equals("setstats") && isGM) {
			if (args.length == 2) {
				Player player = plugin.getServer().getPlayer(args[0]);
				if (player != null) {
					if (plugin.characters.containsKey(player)) {
						Character character = plugin.characters.get(player);
						character.setFreeStats(Short.parseShort(args[1]));
					}
					else {
						sender.sendMessage(ChatColor.RED+"Player is not registered.");
					}
				}
				else {
					sender.sendMessage(ChatColor.RED+"Player does not exist.");
				}
				return true;
			}
			
			return false;
		}
		
		else if (command.equals("setskills") && isGM) {
			if (args.length == 2) {
				Player player = plugin.getServer().getPlayer(args[0]);
				if (player != null) {
					if (plugin.characters.containsKey(player)) {
						Character character = plugin.characters.get(player);
						character.setFreeSkills(Short.parseShort(args[1]));
					}
					else {
						sender.sendMessage(ChatColor.RED+"Player is not registered.");
					}
				}
				else {
					sender.sendMessage(ChatColor.RED+"Player does not exist.");
				}
				return true;
			}
			
			return false;
		}
		
		else if (command.equals("toworld") && isGM && (plugin.useFactions || plugin.usePvp)) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				if (args.length == 1) {
					World world = plugin.getServer().getWorld(args[0]);
					if (world != null) {
						Location teleLoc = world.getSpawnLocation();
						teleLoc.setY(teleLoc.getY()+2);
						player.teleport(teleLoc);
					}
					else {
						player.sendMessage(ChatColor.RED+"That world does not exist.");
					}
					
					return true;
				}
			}
			else {
				sender.sendMessage(ChatColor.RED+"This is a player only command.");
				return true;
			}
			
			return false;
		}
		
		else if (command.equals("createrift") && isGM && plugin.useFactions) {
			if (sender instanceof Player) {
				plugin.createRifts();
				
				return true;
			}
			else {
				sender.sendMessage(ChatColor.RED+"This is a player only command.");
				return true;
			}
		}
		
		/**********************************************************
		 * 
		 * Player only commands
		 * 
		 *********************************************************/
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			if (command.equals("joinrifts")) {
				if (!plugin.characters.containsKey(player)) {
					String factionToJoin;
					String classToJoin;
					
					if (args.length != 2 && !(args.length == 1 && (plugin.autoJoin || !plugin.useFactions))) {
						return false;
					}
					
					if (plugin.autoJoin || !plugin.useFactions) {
						classToJoin = args[0].toLowerCase();
						factionToJoin = "auto";
					}
					else {
						factionToJoin = args[0].toLowerCase();
						classToJoin = args[1].toLowerCase();
					}

					if (!(factionToJoin.matches("light") || factionToJoin.matches("dark") || factionToJoin.matches("auto"))) {
						player.sendMessage(ChatColor.DARK_RED+"Please choose factions light or dark.");
						return true;
					}
					
					if (!(classToJoin.matches("archer") || classToJoin.matches("priest") || classToJoin.matches("fighter") 
						|| classToJoin.matches("defender") || classToJoin.matches("mage"))) {
						player.sendMessage(ChatColor.DARK_RED+"Please choose classes archer, fighter, priest, defender or mage.");
						return true;
					}
					
					if (factionToJoin.matches("auto")) {
						if (plugin.useFactions) {
							int lights = plugin.data.getTotalMembers("characters", "faction", "light");
							int darks = plugin.data.getTotalMembers("characters", "faction", "dark");
							if (lights > darks) {
								factionToJoin = "light";
							}
							else
								factionToJoin = "dark";
						}
						else {
							factionToJoin = "light";
						}
					}
					
					plugin.data.createPlayer(player, factionToJoin, classToJoin);
					String aoran = "a";
					if (classToJoin.matches("archer"))
						aoran = "an";
					
					if (plugin.useFactions)
						player.sendMessage(ChatColor.AQUA+"You have joined the "+ChatColor.GREEN+factionToJoin+ChatColor.AQUA+" as "+aoran+" "+ChatColor.GOLD+classToJoin);
					else
						player.sendMessage(ChatColor.AQUA+"You have joined as "+aoran+" "+ChatColor.GOLD+classToJoin);
					
					//Create instance of character for the player
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
						character.setTutorialLevel((byte)0);
						character.setupSkills();
					}
					catch(Exception e) {
						System.out.println("Error setting up character: "+e);
					}
					
					//world transport
					if (plugin.useFactions) {
						String worldname = null;
						
						if (charInfo[1].matches("light")) {
							worldname = plugin.lightWorldName;
						}
						else if (charInfo[1].matches("dark")) {
							worldname = plugin.darkWorldName;
						}
						
						World world = plugin.getServer().getWorld(worldname);
						Location loc = world.getSpawnLocation();
						loc.setY(loc.getY()+2);
			            player.teleport(loc);
			            player.setCompassTarget(loc);
						
			            String worldName = charInfo[1];
			            if (character.getFaction().matches("dark"))
			            	worldName = ChatColor.GRAY+worldName;
			            else
			            	worldName = ChatColor.YELLOW+worldName;
			            player.sendMessage(ChatColor.AQUA+"Welcome to the world of the "+worldName);
					}
					
		            player.sendMessage(ChatColor.GOLD+"Lets get started!  Go break a block.");
		            return true;
				}
				else {
					player.sendMessage("You already exist in the Rifts world.");
					return true;
				}
				
				
			}
			
			else if (command.equals("statshelp")) {
				player.sendMessage(ChatColor.GREEN+"Str"+ChatColor.AQUA+" - How much damage you do.");
				player.sendMessage(ChatColor.GREEN+"Def"+ChatColor.AQUA+" - How much damage you absorb.");
				player.sendMessage(ChatColor.GREEN+"Int"+ChatColor.AQUA+" - How strong your magic is.");
				player.sendMessage(ChatColor.GREEN+"Agl"+ChatColor.AQUA+" - How often you dodge attacks.");
				player.sendMessage(ChatColor.GREEN+"Str"+ChatColor.AQUA+" - How often you land physical attacks.");
				player.sendMessage(ChatColor.GREEN+"Spr"+ChatColor.AQUA+" - How fast your mana regens and accruate your magic attacks are.");
				
				return true;
			}
			
			/**********************************************************
			 * 
			 * Registered player only commands
			 * 
			 *********************************************************/
			
			if (plugin.characters.containsKey(player)) {
				Character character = plugin.characters.get(player);
				/*
				 * Level command:
				 * Displays the character's current level.
				 */
				
				if (command.equals("level")) {
					player.sendMessage(ChatColor.AQUA+"You are level "+ChatColor.GOLD+character.getLevel());
					
					return true;
				}
				
				else if (command.equals("stats")) {
					if (args.length == 0) {
						player.sendMessage(ChatColor.AQUA+"----------------------------------");
						player.sendMessage(ChatColor.AQUA+"Str: "+ChatColor.GREEN+String.format("%03d", character.getStr())+ChatColor.AQUA+" | Def: "+ChatColor.GREEN+String.format("%03d", character.getDef()));
						player.sendMessage(ChatColor.AQUA+"Spr: "+ChatColor.GREEN+String.format("%03d", character.getSpr())+ChatColor.AQUA+" | Int: "+ChatColor.GREEN+String.format("%03d", character.getInt()));
						player.sendMessage(ChatColor.AQUA+"Dex: "+ChatColor.GREEN+String.format("%03d", character.getDex())+ChatColor.AQUA+" | Agl: "+ChatColor.GREEN+String.format("%03d", character.getAgl()));
						player.sendMessage(ChatColor.AQUA+"Mana: "+ChatColor.GREEN+character.getTotalMana());
						player.sendMessage(ChatColor.AQUA+"----------------------------------");
						player.sendMessage(ChatColor.AQUA+"You have "+ChatColor.GREEN+character.getFreeStats()+ChatColor.AQUA+" free stats.");
						return true;
					}
					else if (args.length == 3) {
						int statsnum = 0;
						
						if (args[0].toLowerCase().matches("add")) {
							try {
								statsnum = Integer.parseInt(args[1]);
							}
							catch (Exception e) {
								return false;
							}
							if (statsnum <= character.getFreeStats()) {
								if (args[2].toLowerCase().matches("str")) {
									character.setStr((short)(character.getStr()+statsnum));
								}
								else if (args[2].toLowerCase().matches("def")) {
									character.setDef((short)(character.getDef()+statsnum));
								}
								else if (args[2].toLowerCase().matches("spr")) {
									character.setSpr((short)(character.getSpr()+statsnum));
									character.setTotalMana((short)(character.getTotalMana()+(statsnum*5)));
								}
								else if (args[2].toLowerCase().matches("int")) {
									character.setInt((short)(character.getInt()+statsnum));
								}
								else if (args[2].toLowerCase().matches("agl")) {
									character.setAgl((short)(character.getAgl()+statsnum));
								}
								else if (args[2].toLowerCase().matches("dex")) {
									character.setDex((short)(character.getDex()+(statsnum)));
								}
								else {
									return false;
								}
								
								character.setFreeStats((short)(character.getFreeStats()-statsnum));
								player.sendMessage(""+ChatColor.GREEN+statsnum+ChatColor.AQUA+" points have been added to "+args[2].toLowerCase()+".");
							}
							else {
								player.sendMessage(ChatColor.RED+"You do not have that many free stats.");
							}
						}
						
						else
							return false;
						
						return true;
					}
					
					return false;
				}
				
				/*
				 * Party command:
				 * 0 Arguments - displays current player/class list of player's current party.
				 * 1 Argument - Adds that player to party, given they aren't in one and sender is party leader.
				 * 2 Arguments - Changes the loot or invite type.
				 */
				
				else if (command.equals("party")) {
					if (args.length == 0) {
						
						if (plugin.parties.containsKey(player)) {
							Party party = plugin.parties.get(player);
							Player[] playerList = party.returnMembers();
							Character testcharacter = null;
							String playersClass;
							
							sender.sendMessage(ChatColor.GRAY+"Your Party");
							sender.sendMessage(ChatColor.WHITE+"----------------------------------");
							
							for (int i = 0;i<playerList.length;i++) {
								testcharacter = plugin.characters.get(playerList[i]);
								playersClass = testcharacter.getClassName();
								if (party.isLeader(playerList[i].getName())) {
									sender.sendMessage(ChatColor.GOLD+playerList[i].getName()+ChatColor.DARK_AQUA+" - "+playersClass);
								}
								else {
									sender.sendMessage(ChatColor.GREEN+playerList[i].getName()+ChatColor.DARK_AQUA+" - "+playersClass);
								}
							}
							
							sender.sendMessage(ChatColor.WHITE+"----------------------------------");
							sender.sendMessage(ChatColor.AQUA+"Loot type: "+party.lootType);
							return true;
						}
						else {
							sender.sendMessage(ChatColor.RED+"You are not in a party.");
							return true;
						}
					}
					
					else if (args.length == 1) {
						Player playerToJoin = plugin.getServer().getPlayer(args[0]);
			
						if (playerToJoin != null) {
							//make sure you can only party people on the same world as you
							if (player.getWorld().getName().matches(playerToJoin.getWorld().getName())) {
								if (plugin.parties.containsKey(playerToJoin)) {
									sender.sendMessage(ChatColor.RED+playerToJoin.getName()+" is already in a party.");
									return true;
								}
								
								if (plugin.parties.containsKey(player)) {
									Party party = plugin.parties.get(player);
									if (party.allowInvites || party.isLeader(player.getName())) {
										if (party.add(playerToJoin)) {
											sender.sendMessage(ChatColor.GREEN+playerToJoin.getName()+ChatColor.AQUA+" has been added to the party.");
											playerToJoin.sendMessage(ChatColor.GREEN+player.getName()+ChatColor.AQUA+" has added you to their party.  /leaveparty to leave.");
										}
										else {
											sender.sendMessage(ChatColor.RED+"Your party is full.");
										}
									}
									else {
										sender.sendMessage(ChatColor.RED+"You must be leader to add people");
									}
									return true;
								}
								
								
								//create the party
								Party newParty = new Party(player, plugin);
								newParty.add(playerToJoin);
								
								plugin.parties.put(player, newParty);
								plugin.parties.put(playerToJoin, newParty);
								
								player.sendMessage(ChatColor.AQUA+"Created party!");
								playerToJoin.sendMessage(ChatColor.GREEN+player.getName()+ChatColor.AQUA+" added you to their party!");
								
								return true;
							}
							
							else {
								sender.sendMessage(ChatColor.RED+playerToJoin.getName()+" is on a different world.");
								return true;
							}
						}
						else {
							sender.sendMessage(ChatColor.RED+args[0]+" is not online.");
							return true;
						}
					}
					//misc party commands
					else if (args.length == 2) {
						if (plugin.parties.containsKey(player)) {
							Party party = plugin.parties.get(player);
							if (party.isLeader(player.getName())) {
								if (args[0].matches("loot")) {
									if (args[1].matches("leader") || args[1].matches("random")) {
										party.setLoot(args[1]);
										party.sendPartyMessage(ChatColor.AQUA+"The loot type was changed to "+ChatColor.GOLD+args[1]);
										return true;
									}
									return false;
								}
								
								else if (args[0].matches("invites")) {
									if (args[1].matches("public") || args[1].matches("leader")) {
										String message;
										if (args[1].matches("public")) {
											party.setInvite(true);
											message = "Public invites allowed.  Type /party <name> to add people.";
										}
										else {
											party.setInvite(false);
											message = "Public invites deactivated.  Leader only invites";
										}
										
										party.sendPartyMessage(ChatColor.AQUA+message);
										return true;
									}
									return false;
								}
								
								else if (args[0].matches("leader")) {
									Player newLeader = plugin.getServer().getPlayer(args[1]);
									if (newLeader != null) {
										if (plugin.parties.get(newLeader).equals(plugin.parties.get(player))) {
											party.changeLeader(newLeader);
											
											String message = ChatColor.GREEN+newLeader.getName()+ChatColor.AQUA+" is the new party leader.";
											party.sendPartyMessage(message);
										}
										else {
											player.sendMessage(ChatColor.RED+args[0]+" is not in your party.");
										}
									}
									else {
										player.sendMessage(ChatColor.RED+"That player is not online.");
									}
								}
							}
							else {
								player.sendMessage(ChatColor.RED+"Only the party leader can issue party commands.");
							}
						}
					}
				}
				
				/*
				 * Leaveparty Command:
				 * Allows the player to leave their party.  If they aren't in one, tell them.
				 */
				
				else if (command.equals("leaveparty")) {
					if (plugin.parties.containsKey(player)) {
						Party party = plugin.parties.get(player);
						party.leave(player);
						plugin.parties.remove(player);
						player.sendMessage(ChatColor.RED+"You left the party.");
						String message = ChatColor.RED+player.getName()+" has left the party.";
						party.sendPartyMessage(message);
						
						//Disbands the party if there's only 1 person left in it.
						if (party.members.size() == 1) {
							party.sendPartyMessage(ChatColor.RED+"The party has disbanded.");
							
							Player[] playerList = party.returnMembers();
							
							for (int i = 0;i<playerList.length;i++) {
								plugin.parties.remove(playerList[i]);
							}
							
							party = null;
							
						}
						return true;
					}
					else {
						sender.sendMessage(ChatColor.RED+"You are not in a party.");
						return true;
					}
				}
				
				/*
				 * Disband command
				 * Allows the leader of the party to disband the party.
				 */
				
				else if (command.equals("disbandparty")) {
					if (plugin.parties.containsKey(player)) {
						if (args.length == 1) {
							Party party = plugin.parties.get(player);
							if (party.isLeader(player.getName())) {
								party.sendPartyMessage(ChatColor.RED+"The party has disbanded.");
								
								Player[] playerList = party.returnMembers();
								
								for (int i = 0;i<playerList.length;i++) {
									plugin.parties.remove(playerList[i]);
								}
								party = null;
							}
							else {
								player.sendMessage(ChatColor.RED+"You are not the party leader.");
							}
							return true;
						}
						else {
							return false;
						}
					}
					else {
						sender.sendMessage(ChatColor.RED+"You are not in a party.");
						return true;
					}
				}
				
				/*
				 * Hide command
				 * Hides miss statements or exp
				 */
				
				else if (command.equals("hide")) {
					if (args.length == 1) {
						if (args[0].matches("exp")) {
							character.setShowExp((byte)0);
							player.sendMessage(ChatColor.AQUA+"Experience notification has been turned off.");
						}
						else if (args[0].matches("miss")) {
							character.setShowMiss((byte)0);
							player.sendMessage(ChatColor.AQUA+"Miss notification has been turned off.");
						}
						return true;
					}
					else
						return false;
				}
				
				/*
				 * Show command
				 * Shows miss statements or exp
				 */
				
				else if (command.equals("show")) {
					if (args.length == 1) {
						if (args[0].matches("exp")) {
							character.setShowExp((byte)1);
							player.sendMessage(ChatColor.AQUA+"Experience notification has been turned on.");
						}
						else if (args[0].matches("miss")) {
							character.setShowMiss((byte)1);
							player.sendMessage(ChatColor.AQUA+"Miss notification has been turned on.");
						}
						return true;
					}
					else
						return false;
				}
				
				/*
				 * Exp command
				 * Shows a user's experience
				 */
				
				else if (command.equals("exp")) {
					if (args.length == 0) {
						int nextLevel = character.getLevel()+1;
						float expNl = plugin.getExpForLevel(nextLevel);
						long barNum = Math.round((character.getExp()/expNl)*50);
						String expBar;
						int expCount = 0;
						
						sender.sendMessage(ChatColor.AQUA+"Experience: "+character.getExp()+"/"+Math.round(expNl));
						expBar = ChatColor.GOLD+"["+ChatColor.YELLOW;

						for (int i=0;i<=barNum;i++) {
							expBar = expBar+"|";
							expCount++;
						}
						expBar = expBar+ChatColor.AQUA+"";
						for (int i=expCount;i<50;i++) {
							expBar = expBar+"|";
						}
						
						expBar = expBar+ChatColor.GOLD+"]";
						sender.sendMessage(expBar);
						return true;
					}
					else if (args.length == 1) {
						String offoron;
						if (args[0].matches("true")) {
							character.setShowExp((byte)1);
							offoron = "on";
						}
						else if (args[0].matches("false")) {
							character.setShowExp((byte)0);
							offoron = "off";	
						}
						else
							return false;
						
						player.sendMessage(ChatColor.AQUA+"Experience notification has been turned "+offoron);
						return true;
					}
					else
						return false;
				}
				

				else if (command.equals("skills")) {
					int level = character.getLevel();
					boolean hasSkills = false;
					player.sendMessage(ChatColor.AQUA+"Current Skill Points: "+character.getFreeSkills());
					player.sendMessage(ChatColor.AQUA+"-------------------------------------");
					String displayName;
					for (Entry<Skill, String> entry : plugin.skillList.entrySet()) {
					    Skill skill = entry.getKey();
					    String classname = entry.getValue();
					    
					    if (classname.matches(character.getClassName()) && level >= skill.getRequiredLevel() && ((skill.getLevel() == 1 && !character.hasSkill(skill.getName(), skill.getLevel())) || 
					    		(skill.getLevel() > 1 && character.hasSkill(skill.getName(), (skill.getLevel() - 1))) && (!character.hasSkill(skill.getName(), skill.getLevel())))) {
					    	hasSkills = true;
					    	displayName = skill.getName().substring(0, 1).toUpperCase()+skill.getName().substring(1);
					    	player.sendMessage(ChatColor.GREEN+displayName+ChatColor.AQUA+" - "+ChatColor.YELLOW+"lvl "+skill.getLevel()+ChatColor.AQUA+" - "+ChatColor.GOLD+skill.getReqPoints()+ChatColor.AQUA+" - "+ChatColor.YELLOW+skill.getDescription());
					    }
					}
					if (!hasSkills)
						player.sendMessage(ChatColor.AQUA+"No available skills.");
					return true;
				}
				
				else if (command.equals("skilllist")) {
					//show available skills
					if (args.length == 0) {
						boolean hasSkills = false;
						Skill skill;
						String displayName;
						if (character.getActiveSkill() != null)
							player.sendMessage(ChatColor.GREEN+"Active Skill: "+ChatColor.GOLD+character.getActiveSkill().getName());
						player.sendMessage(ChatColor.AQUA+"Skill List");
						player.sendMessage(ChatColor.AQUA+"----------------------------------");
						List<Skill> skills = character.getAvailableSkills();
						Iterator<Skill> itr = skills.iterator();
						while(itr.hasNext()) {
							skill = itr.next();
							if (!skill.getName().matches("axepowerup") && !skill.getName().matches("swordpowerup")) {
								if (!character.hasSkill(skill.getName(), skill.getLevel()+1)) {
									displayName = skill.getName().substring(0, 1).toUpperCase()+skill.getName().substring(1);
									player.sendMessage(ChatColor.GREEN+displayName+" lvl "+skill.getLevel()+ChatColor.AQUA+" - "+ChatColor.GOLD+skill.manaUsage+"MP"+ChatColor.AQUA+" - "+ChatColor.YELLOW+skill.getDescription());
								}
							}
							hasSkills = true;
						}
						
						if (!hasSkills)
							player.sendMessage(ChatColor.GOLD+"You have no skills yet.");

						return true;
					}
					
					return false;
				}
				
				else if (command.equals("skill")) {
					//Use a skill
					if (args.length == 0) {
						//show available skills
						if (args.length == 0) {
							boolean hasSkills = false;
							Skill skill;
							String displayName;
							if (character.getActiveSkill() != null)
								player.sendMessage(ChatColor.GREEN+"Active Skill: "+ChatColor.GOLD+character.getActiveSkill().getName());
							player.sendMessage(ChatColor.AQUA+"Skill List");
							player.sendMessage(ChatColor.AQUA+"----------------------------------");
							List<Skill> skills = character.getAvailableSkills();
							Iterator<Skill> itr = skills.iterator();
							while(itr.hasNext()) {
								skill = itr.next();
								if (!skill.getName().matches("axepowerup") && !skill.getName().matches("swordpowerup")) {
									if (!character.hasSkill(skill.getName(), skill.getLevel()+1)) {
										displayName = skill.getName().substring(0, 1).toUpperCase()+skill.getName().substring(1);
										player.sendMessage(ChatColor.GREEN+displayName+" lvl "+skill.getLevel()+ChatColor.AQUA+" - "+ChatColor.GOLD+skill.manaUsage+"MP"+ChatColor.AQUA+" - "+ChatColor.YELLOW+skill.getDescription());
									}
								}
								hasSkills = true;
							}
							
							if (!hasSkills)
								player.sendMessage(ChatColor.GOLD+"You have no skills yet.");

							return true;
						}
					}
					else if (args.length == 1) {
						
						if (args[0].matches("off")) {
							if (character.getActiveSkill() != null) {
								String skillname = character.getActiveSkill().getName().substring(0, 1).toUpperCase()+character.getActiveSkill().getName().substring(1);
								character.removeActiveSkill();
								
								player.sendMessage(ChatColor.AQUA+skillname+" has been turned off.");
							}
							else {
								player.sendMessage(ChatColor.RED+"You do not have an active skill set.");
							}
						}
						else {
							String skillname = args[0].toLowerCase();
							if (character.hasSkill(skillname, 1)) {
								Skill skill = plugin.getSkillByName(skillname, character.getHighestSkillLevel(skillname));
								
								if (skill.getName().matches("sacrifice")) {
									final Player exploder = player;
									final float explosionstr = skill.getLevel() * 2F * (player.getHealth()/20);
									player.sendMessage(ChatColor.DARK_RED+"You will blow up in 5...");
									plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
									    public void run() {
									    	exploder.sendMessage(ChatColor.DARK_RED+"4...");
									    }
									}, 20);
									plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
									    public void run() {
									    	exploder.sendMessage(ChatColor.DARK_RED+"3...");
									    }
									}, 40);
									plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
									    public void run() {
									    	exploder.sendMessage(ChatColor.DARK_RED+"2...");
									    }
									}, 60);
									plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
									    public void run() {
									    	exploder.sendMessage(ChatColor.DARK_RED+"1...");
									    }
									}, 80);
									plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
									    public void run() {
									    	plugin.explosion(exploder, exploder.getWorld().getBlockAt(exploder.getLocation()), explosionstr);
									    	exploder.setHealth(0);
									    }
									}, 100);
								}
								else {
									character.setActiveSkill(skillname);
									String displayName = skillname.substring(0, 1).toUpperCase()+skillname.substring(1);
									player.sendMessage(ChatColor.GOLD+displayName+" skill activated!");
									//STANDARD BUFFS, 5%
									if (skill.getType().contains("buff")) {
										if (character.getMana() >= skill.getManaReq()) {
											String[] split = skill.getType().split("-");
											//20 ticks per second
											float buffPercent = .05F * skill.skillLevel;
											if (character.hasBuff(skill.getName()) > -1) {
												player.sendMessage(ChatColor.RED+"You already have that buff on.");
											}
											else {
												if (split[1].matches("sneak"))
													character.addBuff(new Buff(character, "sneak", "sneak", 0, (100*skill.getLevel()), plugin));
												else
													character.addBuff(new Buff(character, skill.getName(), split[1], buffPercent, 600, plugin));
												character.setMana((short)(character.getMana()-skill.getManaReq()));
											}
										}
										else {
											player.sendMessage(ChatColor.RED+"You do not have enough mana for "+displayName+".");
										}
									}
									else if (skill.getType().contains("partybuff")) {
										if (character.getMana() >= skill.getManaReq()) {
											if (plugin.parties.containsKey(player)) {
												String[] split = skill.getType().split("-");
												Party party = plugin.parties.get(player);
												//20 ticks per second
												for (int i=0;i<party.members.size();i++) {
													Character character2 = plugin.characters.get(party.members.get(i));
													float buffPercent = .05F * skill.skillLevel;
													character2.addBuff(new Buff(character2, skill.getName(), split[1], buffPercent, 600, plugin));
												}
												character.setMana((short)(character.getMana()-skill.getManaReq()));
											}
											else {
												player.sendMessage(ChatColor.RED+"You are not in a party.");
											}
										}
										else {
											player.sendMessage(ChatColor.RED+"You do not have enough mana for "+displayName+".");
										}
									}
								}
							}
						}
						
						return true;
					}
					else if (args.length == 2) {
						if (args[0].matches("add")) {
							String skillname = args[1].toLowerCase();
							if (character.hasSkill(skillname, 2)) {
								Skill skill = plugin.getSkillByName(skillname, 3);
								if (skill != null) {
									if ((character.getLevel() >= skill.getRequiredLevel()) && (character.getFreeSkills() >= skill.getReqPoints()) && !character.hasSkill(skillname, 3)) {
										character.addSkill(skill);
										character.setFreeSkills((short)(character.getFreeSkills() - skill.getReqPoints()));
										String displayName = skill.getName().substring(0, 1).toUpperCase()+skill.getName().substring(1);
										player.sendMessage(ChatColor.GOLD+"You now have "+displayName+" level "+skill.getLevel()+"!");
										if (skill.getType().matches("buff") || skill.getType().matches("partybuff")) {
											player.sendMessage(ChatColor.AQUA+"To use your buff, just type "+ChatColor.GREEN+"/skill <name>"+ChatColor.AQUA+".");
										}
										else if (skill.getType().matches("ability")) {
											player.sendMessage(ChatColor.AQUA+"Your skill has increased your melee ability.");
											player.sendMessage(ChatColor.AQUA+"It is not a skill you can use, it's always on!");
										}
										if (skill.getName().matches("axepowerup")) {
											character.setAxePowerup((byte)skill.getLevel());		
										}
										else if (skill.getName().matches("swordpowerup")) {
											character.setSwordPowerup((byte)skill.getLevel());		
										}
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have access to that skill at this time.");
									}
								}
								else
									player.sendMessage(ChatColor.RED+"That skill does not exist at level 3");
							}
							else if (character.hasSkill(skillname, 1)) {
								Skill skill = plugin.getSkillByName(skillname, 2);
								if (skill != null) {
									if ((character.getLevel() >= skill.getRequiredLevel()) && (character.getFreeSkills() >= skill.getReqPoints())) {
										character.addSkill(skill);
										character.setFreeSkills((short)(character.getFreeSkills() - skill.getReqPoints()));
										String displayName = skill.getName().substring(0, 1).toUpperCase()+skill.getName().substring(1);
										player.sendMessage(ChatColor.GOLD+"You now have "+displayName+" level "+skill.getLevel()+"!");
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have access to that skill at this time.");
									}
								}
								else
									player.sendMessage(ChatColor.RED+"That skill does not exist at level 2");
							}
							else {
								Skill skill = plugin.getSkillByName(skillname, 1);
								if (skill != null) {
									if ((character.getLevel() >= skill.getRequiredLevel()) && (character.getFreeSkills() >= skill.getReqPoints())) {
										character.addSkill(skill);
										character.setFreeSkills((short)(character.getFreeSkills() - skill.getReqPoints()));
										String displayName = skill.getName().substring(0, 1).toUpperCase()+skill.getName().substring(1);
										player.sendMessage(ChatColor.GOLD+"You now have "+displayName+" level "+skill.getLevel()+"!");
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have access to that skill at this time.");
									}
								}
								else
									player.sendMessage(ChatColor.RED+"That skill does not exist.");
							}
						}
						
						else if (args[0].matches("teleport")) {
							Skill skill = plugin.getSkillByName("teleport", character.getHighestSkillLevel(args[0]));
							if (skill == null) {
								player.sendMessage(ChatColor.RED+"You do not have that skill.");
								return true;
							}
							
							if (!(character.getMana() >= skill.getManaReq())) {
								player.sendMessage(ChatColor.RED+"You do not have enough mana for teleport.");
								return true;
							}
							if (skill.getLevel() == 1 && args.length < 2) {
								player.sendMessage(ChatColor.RED+"You must type in the name of the party member you wish to teleport.");
								return true;
							}
							
							if (!plugin.parties.containsKey(player)) {
								player.sendMessage(ChatColor.RED+"You must be in a party to use this skill.");
								return true;
							}
							
							Party party = plugin.parties.get(player);
							
							if (skill.getLevel() == 1) {
								if (plugin.getServer().getPlayer(args[1]) == null) {
									player.sendMessage(ChatColor.RED+"That player does not exist.");
									return true;
								}
								Player playerToTele = plugin.getServer().getPlayer(args[1]);
								
								if (!party.equals(plugin.parties.get(playerToTele))) {
									player.sendMessage(ChatColor.RED+"That player is not in your party.");
									return true;
								}
								else {
									playerToTele.teleport(player);
									character.setMana((short)(character.getMana()-skill.getManaReq()));
								}
							}
							else if (skill.getLevel() == 2) {
								for (Player playerToTele : party.returnMembers()) {
									playerToTele.teleport(player);
								}
								character.setMana((short)(character.getMana()-skill.getManaReq()));
							}
						}
						
						return true;
					}
					
					return false;
				}
				
				else if (command.equals("status")) {
					if (args.length == 0) {
						float totalMana = character.getTotalMana();
						float manaPercent = (character.getMana()/totalMana);
						int manaDiff = Math.round(manaPercent*50);
						int manaCount = 0;
						String manaBar;
						int nextLevel = character.getLevel()+1;
						float expNl = plugin.getExpForLevel(nextLevel);
						long barNum = Math.round((character.getExp()/expNl)*50);
						String expBar;
						int expCount = 0;
						
						player.sendMessage(ChatColor.GREEN+player.getName());
						player.sendMessage(ChatColor.YELLOW+"Level "+character.getLevel()+" "+character.getClassName());
						
						expBar = ChatColor.GOLD+"["+ChatColor.YELLOW;

						for (int i=0;i<=barNum;i++) {
							expBar = expBar+"|";
							expCount++;
						}
						expBar = expBar+ChatColor.AQUA+"";
						for (int i=expCount;i<=50;i++) {
							expBar = expBar+"|";
						}
						
						expBar = expBar+ChatColor.GOLD+"]";
						sender.sendMessage(expBar+ChatColor.AQUA+" Experience: "+character.getExp()+"/"+Math.round(expNl));
						
						manaBar = ChatColor.GOLD+"["+ChatColor.YELLOW+"";
						for (int i=0;i<=manaDiff;i++) {
							manaBar = manaBar+"|";
							manaCount++;
						}
						manaBar = manaBar+ChatColor.AQUA+"";
						for (int i=manaCount;i<50;i++) {
							manaBar = manaBar+"|";
						}
						manaBar = manaBar+ChatColor.GOLD+"]";
						
						player.sendMessage(manaBar+ChatColor.AQUA+" Mana: "+character.getMana()+"/"+character.getTotalMana());
					}
					return true;
				}
				
				/*
				 * Spawnblock command
				 * Shows uses left on a spawnblock as well as allows you to leave one remotely.
				 */
				else if (command.equals("spawnblock")) {
					if (args.length > 0) {
						if (Rifts.Permissions != null) {
							canUseCommand = Rifts.Permissions.has(player, "bindspawn.cmds");
						}
						else if (Rifts.gm != null) {
							canUseCommand = Rifts.gm.getWorldsHolder().getWorldPermissions(player).has(player,"bindspawn.cmds");
						}
						
						if (canUseCommand) {
							if (args[0].matches("uses")) {
								if (plugin.spawnblocks.containsKey(player)) {
									SpawnBlock spawn = plugin.spawnblocks.get(player);
									String spawnstext = "spawns";
									String areis = "are";
									int usesLeft = spawn.getSpawnBlockUses();
									if (usesLeft == 1) {
										spawnstext = "spawn";
										areis = "is";
									}
									
									player.sendMessage(ChatColor.AQUA+"There "+areis+" "+ChatColor.GREEN+usesLeft+ChatColor.AQUA+" "+spawnstext+" remaining.");
								}
								else {
									player.sendMessage(ChatColor.RED+"You are not bound.");
								}
								return true;
							}
							else if (args[0].matches("bound")) {
								if (plugin.spawnblocks.containsKey(player)) {
									player.sendMessage(ChatColor.GREEN+"You are bound.");
								}
								else {
									player.sendMessage(ChatColor.RED+"You are not bound.");
								}
								return true;
							}
							else if (args[0].matches("leave")) {
								if (plugin.spawnblocks.containsKey(player)) {
									SpawnBlock spawn = plugin.spawnblocks.get(player);
									spawn.unbindSpawn(player);
								}
								return true;
							}
						}
						
						else {
							player.sendMessage("You do not have access to that command.");
							return true;
						}
					}
					else
						return false;
				}
				
				else if (command.equals("resetcharacter")) {
					plugin.data.removeCharacter(character);
					plugin.characters.remove(player);
					character = null;
					player.sendMessage(ChatColor.RED+"Your character has been deleted.");
					if (plugin.autoJoin)
						player.sendMessage(ChatColor.DARK_GREEN+"Type "+ChatColor.GOLD+"/joinrifts [class]"+ChatColor.DARK_GREEN+" to join the server.");
			    	else
			    		player.sendMessage(ChatColor.DARK_GREEN+"Type "+ChatColor.GOLD+"/joinrifts [light/dark] [class]"+ChatColor.DARK_GREEN+" to join the server.");
					
					player.sendMessage(ChatColor.DARK_GREEN+"Available classes: "+ChatColor.DARK_AQUA+"Archer, Defender, Fighter, Priest, Mage.");
					
					player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
					
					return true;
				}
				return false;
			}
			else {
				sender.sendMessage(ChatColor.RED+"You have not registered with /joinrifts.");
				return true;
			}
		}
		
		return false;
	}
}
