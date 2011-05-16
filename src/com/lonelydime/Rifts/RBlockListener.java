package com.lonelydime.Rifts;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.iConomy.system.Account;
import com.iConomy.system.Holdings;

public class RBlockListener extends BlockListener {
	public static Rifts plugin;
	
	public RBlockListener(Rifts instance) {
	    plugin = instance;
	}
	
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		Block obsidianBlock = null;
		boolean canCreate = true;
		boolean hasEnough = true;
		
		if (event.getLine(0).toLowerCase().contains("@setspawn") ) {
			if (Rifts.Permissions != null) {
				canCreate = Rifts.Permissions.has(player, "spawnblock.create");
			}
			else if (Rifts.gm != null) {
				canCreate = Rifts.gm.getWorldsHolder().getWorldPermissions(player).has(player,"spawnblock.create");
			}
			
			
			if (event.getBlock().getFace(BlockFace.EAST).getTypeId() == 49) {
				obsidianBlock = event.getBlock().getFace(BlockFace.EAST);
			}
			else if (event.getBlock().getFace(BlockFace.WEST).getTypeId() == 49) {
				obsidianBlock = event.getBlock().getFace(BlockFace.WEST);
			}
			else if (event.getBlock().getFace(BlockFace.SOUTH).getTypeId() == 49) {
				obsidianBlock = event.getBlock().getFace(BlockFace.SOUTH);
			}
			else if (event.getBlock().getFace(BlockFace.NORTH).getTypeId() == 49) {
				obsidianBlock = event.getBlock().getFace(BlockFace.NORTH);
			}
			
			if (obsidianBlock != null) {
				//creation of spawn blocks
				if (canCreate) {
					if (plugin.checkiConomy()) {
						//costs iconomy
						if (plugin.spawniconomyCost > 0) {
							
							if (plugin.checkiConomy()) {
								Account account = com.iConomy.iConomy.getAccount(player.getName());
								if (account != null) {
									Holdings balance = com.iConomy.iConomy.getAccount(player.getName()).getHoldings();
									if (balance.hasEnough(plugin.spawniconomyCost)) {
										balance.subtract(plugin.spawniconomyCost);
										player.sendMessage("You used "+plugin.spawniconomyCost+" coins to create a spawn block.");
										hasEnough = true;
									}
								}
							}
							
							else {
								player.sendMessage("You need "+plugin.spawniconomyCost+" coins to create a spawn block.");
								hasEnough = false;
							}
						}
					}
					
					if (hasEnough) {
						SpawnBlock spawnblock = new SpawnBlock(plugin, obsidianBlock, player, plugin.spawnUses);
						
						plugin.spawnblocks.put(player, spawnblock);
						player.sendMessage(ChatColor.AQUA+"You have created a spawn block.");
						
						Block glowBlock = obsidianBlock.getFace(BlockFace.UP);
						glowBlock.setTypeId(89);
						event.setLine(0, player.getName());
					}
				}
			}
		}
		
		else if ((event.getLine(0).toLowerCase().matches("@pvp") && !event.getPlayer().getWorld().getName().matches("pvpworld") && plugin.usePvp)
				|| (event.getLine(0).toLowerCase().matches("@home") && event.getPlayer().getWorld().getName().matches("pvpworld") && plugin.usePvp)) {
			//create portal stuff
			if (Rifts.Permissions != null) {
				canCreate = Rifts.Permissions.has(player, "riftportal.create");
			}
			else if (Rifts.gm != null) {
				canCreate = Rifts.gm.getWorldsHolder().getWorldPermissions(player).has(player,"riftportal.create");
			}
			
			if (canCreate) {
				if (event.getBlock().getFace(BlockFace.EAST).getTypeId() == 49) {
					obsidianBlock = event.getBlock().getFace(BlockFace.EAST);
				}
				else if (event.getBlock().getFace(BlockFace.WEST).getTypeId() == 49) {
					obsidianBlock = event.getBlock().getFace(BlockFace.WEST);
				}
				else if (event.getBlock().getFace(BlockFace.SOUTH).getTypeId() == 49) {
					obsidianBlock = event.getBlock().getFace(BlockFace.SOUTH);
				}
				else if (event.getBlock().getFace(BlockFace.NORTH).getTypeId() == 49) {
					obsidianBlock = event.getBlock().getFace(BlockFace.NORTH);
				}
				
				if (obsidianBlock != null) {
					Block portal1 = null;
					
					if (obsidianBlock.getFace(BlockFace.EAST).getTypeId() == 90) {
						portal1 = obsidianBlock.getFace(BlockFace.EAST);
					}
					else if (obsidianBlock.getFace(BlockFace.WEST).getTypeId() == 90) {
						portal1 = obsidianBlock.getFace(BlockFace.WEST);
					}
					else if (obsidianBlock.getFace(BlockFace.SOUTH).getTypeId() == 90) {
						portal1 = obsidianBlock.getFace(BlockFace.SOUTH);
					}
					else if (obsidianBlock.getFace(BlockFace.NORTH).getTypeId() == 90) {
						portal1 = obsidianBlock.getFace(BlockFace.NORTH);
					}
					
					if (portal1 != null) {
						boolean bottomPortal = false;
						//gets the bottom portal
						while (!bottomPortal) {
							if (portal1.getFace(BlockFace.DOWN).getTypeId() == 90) {
								portal1 = portal1.getFace(BlockFace.DOWN);
							}
							else {
								bottomPortal = true;
							}
						}
						//gets the second bottom portal
						Block portal2 = null;
						if (portal1.getFace(BlockFace.EAST).getTypeId() == 90) {
							portal2 = portal1.getFace(BlockFace.EAST);
						}
						else if (portal1.getFace(BlockFace.WEST).getTypeId() == 90) {
							portal2 = portal1.getFace(BlockFace.WEST);
						}
						else if (portal1.getFace(BlockFace.SOUTH).getTypeId() == 90) {
							portal2 = portal1.getFace(BlockFace.SOUTH);
						}
						else if (portal1.getFace(BlockFace.NORTH).getTypeId() == 90) {
							portal2 = portal1.getFace(BlockFace.NORTH);
						}
						
						if (portal1 != null && portal2 != null) {
							if (plugin.characters.containsKey(event.getPlayer())) {
								Character character = plugin.characters.get(event.getPlayer());
								Portal portal = new Portal(plugin, portal1, portal2, character.getFaction());
								plugin.portals.add(portal);
								plugin.data.createPortal(portal1, portal2, character.getFaction());
								if (event.getLine(0).toLowerCase().matches("@pvp")) {
									event.setLine(0, "[Portal to PvP]");
									event.setLine(1, "Only carry what");
									event.setLine(2, "you need.");
								}
								else {
									event.setLine(0, "["+character.getFaction()+" home]");
									event.setLine(1, "Welcome back.");
								}
							}
						}
					}
				}
			}
			else {
				player.sendMessage(ChatColor.RED+"You do not have permissions to create portals");
			}
		}
		
	}
	
	public void onBlockPlace(BlockPlaceEvent event) {
		if (plugin.mustRift && !plugin.characters.containsKey(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
	}
	
	public void onBlockDamage(BlockDamageEvent event) {
		//If it's a sign, don't allow anyone but the creator to break it.
		Block block = event.getBlock();
		
		if (plugin.mustRift && !plugin.characters.containsKey(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		
		if (block.getType().name().equals("WALL_SIGN")) {
			Block testblock = null;
			SpawnBlock spawnblock = null;
			
			if (block.getFace(BlockFace.EAST).getTypeId() == 49) {
				testblock = block.getFace(BlockFace.EAST);
			}
			else if (block.getFace(BlockFace.WEST).getTypeId() == 49) {
				testblock = block.getFace(BlockFace.WEST);
			}
			else if (block.getFace(BlockFace.SOUTH).getTypeId() == 49) {
				testblock = block.getFace(BlockFace.SOUTH);
			}
			else if (block.getFace(BlockFace.NORTH).getTypeId() == 49) {
				testblock = block.getFace(BlockFace.NORTH);
			}
			
			if (testblock != null) {
				spawnblock = plugin.getSpawnBlock(testblock);
			}
			
			if (spawnblock != null) {
				String creatorName = spawnblock.getSpawnCreator();
				//if the player isn't the creator
				
				if (!creatorName.matches(event.getPlayer().getName())) {
					event.setCancelled(true);
					BlockState state = block.getState();
		            Sign sign = (Sign)state;
		            sign.update();
					event.getPlayer().sendMessage("Only "+creatorName+" can break the sign.");
				}
			}
		}
		
		//if it's obsidian, alert the players attached their spawn block is being destroyed
		else if (block.getType().name().equals("OBSIDIAN")) {
			Block testblock = block;
			SpawnBlock spawnblock = null;
			
			spawnblock = plugin.getSpawnBlock(testblock);
						
			if (spawnblock != null) {
				spawnblock.messageAllPlayers(ChatColor.RED+"The spawn block you are bound to is under attack!");
			}
			
			//if this is a rift block, do not allow it to be destroyed.
			else if (plugin.rifts.containsKey(testblock.getWorld())) {
				Rift rift = plugin.rifts.get(testblock.getWorld());
				if (rift.isRiftBlock(testblock)) {
					event.getPlayer().sendMessage(ChatColor.RED+"You cannot destroy a rift.");
					event.setCancelled(true);
				}
			}
		}
		
		else if (block.getType().name().equals("GLOWSTONE")) {
			Block testblock = block.getFace(BlockFace.DOWN);
			
			if (plugin.isSpawnBlock(testblock)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED+"That block cannot be destroyed.");
			}
			
			if (block.getWorld().getName().equals(plugin.pvpWorldName)) {
				if (plugin.isCageBlock(block)) {
					event.setCancelled(true);
				}
			}
		}
		
		else if (block.getType().name().equals("GLASS") && plugin.usePvp) {
			if (block.getWorld().getName().equals(plugin.pvpWorldName)) {
				if (plugin.isCageBlock(block)) {
					event.setCancelled(true);
				}
			}
		}
		
		else if (block.getType() == Material.WEB) {
			if (plugin.webs.contains(block)) {
				event.setCancelled(true);
			}
		}
		
		//Skills
		Player player = event.getPlayer();
		if (plugin.characters.containsKey(player)) {
			Character character = plugin.characters.get(player);
			if (character.getActiveSkill() != null) {
				Skill skill = character.getActiveSkill();
				//DEFENDER: POUND
				if (skill.getName().matches("pound")) {
					if (player.getItemInHand().getTypeId() == 0) {
						if (character.getMana() >= skill.getManaReq()) {
							int normalDef = character.getDef();
							float explosionStrenth = (skill.skillLevel+.5F);
							
							character.setDef((short)10000);
							//causes explosion
							plugin.explosion(player, block, explosionStrenth);
							
							character.setDef((short)normalDef);
							character.setMana((short)(character.getMana()-skill.getManaReq()));
						}
						else {
							player.sendMessage(ChatColor.RED+"You do not have enough mana for pound.");
							character.removeActiveSkill();
						}
					}
				}
				//FIGHTER: STRIKE
				else if (skill.getName().matches("strike")) {
					if (player.getItemInHand().getType().name().contains("SWORD")) {
						if (character.getMana() >= skill.getManaReq()) {
							Block block2 = event.getBlock();
							int normalDef = character.getDef();
							float explosionStrength = (skill.skillLevel);

							character.setDef((short)10000);
							String direction = plugin.checkRotation(player);
							//causes explosion
							for (int i=0;i<15;i=i+2) {
								//N -x E +z S +x W -z
								if (direction.matches("N"))
									block2 = block2.getWorld().getBlockAt(block.getX()-1, block2.getY(), block2.getZ());
								else if (direction.matches("S"))
									block2 = block2.getWorld().getBlockAt(block.getX()+1, block2.getY(), block2.getZ());
								else if (direction.matches("E"))
									block2 = block2.getWorld().getBlockAt(block.getX(), block2.getY(), block2.getZ()-1);
								else if (direction.matches("W"))
									block2 = block2.getWorld().getBlockAt(block.getX(), block2.getY(), block2.getZ()+1);
								
								plugin.explosion(player, block, explosionStrength);
							}
							character.setDef((short)normalDef);
							character.setMana((short)(character.getMana()-skill.getManaReq()));
						}
						else {
							player.sendMessage(ChatColor.RED+"You do not have enough mana for Strike.");
							character.removeActiveSkill();
						}
					}
				}
			}
		}
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		Block testblock = event.getBlock();
		
		if (testblock.getType().name().equals("OBSIDIAN")) {
			//if block is spawn block
			if (plugin.isSpawnBlock(testblock)) {
				SpawnBlock spawnblock = plugin.getSpawnBlock(testblock);
				spawnblock.DestroySpawnBlock(event.getPlayer().getName());
			}
			
			else {
				findDestroyPortal(testblock);
			}
			
			//add exp for obsidian
			if (!event.isCancelled()) {
				int exp = Math.round(100 * (plugin.experience.get("blocks")/5));
				plugin.addExp(event.getPlayer(), exp, false, "block");
			}
		}
		
		else if (event.getBlock().getType().name().equals("WALL_SIGN")) {
			Block obsidiantest = null;
			SpawnBlock spawnblock = null;
			
			if (testblock.getFace(BlockFace.EAST).getTypeId() == 49) {
				obsidiantest = event.getBlock().getFace(BlockFace.EAST);
			}
			else if (testblock.getFace(BlockFace.WEST).getTypeId() == 49) {
				obsidiantest = event.getBlock().getFace(BlockFace.WEST);
			}
			else if (testblock.getFace(BlockFace.SOUTH).getTypeId() == 49) {
				obsidiantest = event.getBlock().getFace(BlockFace.SOUTH);
			}
			else if (testblock.getFace(BlockFace.NORTH).getTypeId() == 49) {
				obsidiantest = event.getBlock().getFace(BlockFace.NORTH);
			}
			
			if (obsidiantest != null) {
				spawnblock = plugin.getSpawnBlock(obsidiantest);
				
				findDestroyPortal(obsidiantest);
			}
			
			if (spawnblock != null) {
				spawnblock.DestroySpawnBlock(event.getPlayer().getName());
			}
		}
		
		else if (!event.isCancelled()){
			Material material = event.getBlock().getType();
			String matName = material.name();
			int exp = 0;
			Player player = event.getPlayer();
			
			if (plugin.characters.containsKey(player)) {
				
				//general blocks
				if (matName.matches("STONE") || matName.matches("GRASS") || matName.matches("DIRT") || matName.matches("COBBLESTONE") ||
					matName.matches("WOOD") || matName.matches("SAND") || matName.matches("GRAVEL") || matName.matches("LOG") ||
					matName.matches("SANDSTONE")) {
					exp = 10;
				}
				else if (matName.matches("GOLD_ORE")) {
					exp = 30;
				}
				else if (matName.matches("IRON_ORE")) {
					exp = 15;
				}
				else if (matName.matches("COAL_ORE")) {
					exp = 15;
				}
				else if (matName.matches("MOSSY_COBBLESTONE")) {
					exp = 20;
				}
				else if (matName.matches("MOB_SPAWNER")) {
					exp = 1000;
				}
				else if (matName.matches("DIAMOND_ORE")) {
					exp = 100;
				}
				else if (matName.matches("ICE")) {
					exp = 10;
				}
				else if (matName.matches("NETHERRACK")) {
					exp = 20;
				}
				else if (matName.matches("SOUL_SAND")) {
					exp = 30;
				}
				else if (matName.matches("GLOWSTONE")) {
					exp = 30;
				}
				else if (matName.matches("LAPIS_ORE")) {
					exp = 70;
				}
				else if (matName.matches("CLAY")) {
					exp = 20;
				}
				else if (matName.matches("REDSTONE_ORE") || matName.matches("GLOWING_REDSTONE_ORE")) {
					exp = 50;
				}
				
				exp = Math.round(exp * (plugin.experience.get("blocks")/5));
				
				plugin.addExp(player, exp, false, "block");			
			}
		}
		
	}
	
	public void findDestroyPortal(Block testblock) {
		Block portal1 = null;
		if (testblock.getFace(BlockFace.EAST).getTypeId() == 90) {
			portal1 = testblock.getFace(BlockFace.EAST);
		}
		else if (testblock.getFace(BlockFace.WEST).getTypeId() == 90) {
			portal1 = testblock.getFace(BlockFace.WEST);
		}
		else if (testblock.getFace(BlockFace.SOUTH).getTypeId() == 90) {
			portal1 = testblock.getFace(BlockFace.SOUTH);
		}
		else if (testblock.getFace(BlockFace.NORTH).getTypeId() == 90) {
			portal1 = testblock.getFace(BlockFace.NORTH);
		}
		else if (testblock.getFace(BlockFace.UP).getTypeId() == 90) {
			portal1 = testblock.getFace(BlockFace.UP);
		}
		else if (testblock.getFace(BlockFace.DOWN).getTypeId() == 90) {
			portal1 = testblock.getFace(BlockFace.DOWN);
		}
		
		if (portal1 != null) {
			boolean bottomPortal = false;
			//gets the bottom portal
			while (!bottomPortal) {
				if (portal1.getFace(BlockFace.DOWN).getTypeId() == 90) {
					portal1 = portal1.getFace(BlockFace.DOWN);
				}
				else {
					bottomPortal = true;
				}
			}
		}
		Portal portal = plugin.getPortalBlock(portal1);
		
		if (portal != null) {
			portal.destroy();
			portal = null;
		}
	}
}