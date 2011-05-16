package com.lonelydime.Rifts;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.util.Vector;

public class REntityListener extends EntityListener {
	public static Rifts plugin;
	//damage multipliers.  The lower, the stronger. (stat / multiplier) = damage (1 = half heart)
	private int meleeMultiplier = 20;
	private int arrowMultiplier = 25;
	private int magicMultiplier = 25;
	//character level + 1 * mobMultiplier = attack
	private float mobMultiplier = .2F;
	
	public REntityListener(Rifts instance) {
	    plugin = instance;
	}
	
	public void onEntityDamage(EntityDamageEvent event) {
		int defense = 0;
		int attack = 0;
		boolean overrideDamage = false;
		
		//makes natural damage always do damage
		if (!(event.getCause().equals(DamageCause.BLOCK_EXPLOSION) || event.getCause().equals(DamageCause.ENTITY_ATTACK) || event.getCause().equals(DamageCause.ENTITY_EXPLOSION))) {
			return;
		}
		
		//reduces damage
		if (event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			
			if (plugin.characters.containsKey(player)) {
				Character character = plugin.characters.get(player);
				//hack around inventory hooks missing
				if (character.getPlayer().getLocation().getWorld().getName().matches(plugin.lightWorldName) 
						|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.darkWorldName) 
						|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.pvpWorldName))
					plugin.checkArmorAndTool(character);
				
				if (event.getEntity() instanceof Skeleton) {
					System.out.println("Damage: "+event.getCause().name());
				}
				
				//if invincible.
				if (character.getInvincible()) {
					event.setCancelled(true);
				}
			}
		}
		
		if (event instanceof EntityDamageByProjectileEvent) {
			EntityDamageByProjectileEvent damageEvent = (EntityDamageByProjectileEvent)event;
			Player player = null;
			
			if (damageEvent.getProjectile() instanceof Snowball) {
				if (attackMisses(damageEvent.getDamager(), damageEvent.getEntity(), "magic")) {
					damageEvent.setCancelled(true);
				}
			}
			else {
				if (attackMisses(damageEvent.getDamager(), damageEvent.getEntity(), "melee")) {
					damageEvent.setCancelled(true);
				}
			}
			
			if (!damageEvent.isCancelled()) {
				if (damageEvent.getDamager() instanceof Player)
					player = (Player)damageEvent.getDamager();
				
				if (player != null) {
					if (plugin.characters.containsKey(player)) {
						Character character = plugin.characters.get(player);
						
						if (damageEvent.getEntity() instanceof Player) {
							if (plugin.characters.containsKey((Player)damageEvent.getEntity())) {
								Character defender = plugin.characters.get((Player)damageEvent.getEntity());
								//same faction, cancel damage
								if (defender.getFaction().matches(character.getFaction()) && plugin.useFactions) {
									if (character.getActiveSkill() != null) {
										Skill testSkill = character.getActiveSkill();
										if (!(testSkill.getName().matches("heal") || testSkill.getName().matches("groupheal") || testSkill.getName().matches("dispel"))) {
											damageEvent.setCancelled(true);
											return;
										}
									}
									else {
										damageEvent.setCancelled(true);
										return;
									}
								}
							}
						}
						
						if (damageEvent.getProjectile() instanceof Arrow) {
							//default damage 4.
							addEntityDamage(damageEvent.getEntity(), character);

							attack = characterAttack(character, "arrow");
							
							//archer skills
							if (character.getActiveSkill() != null) {
								Skill skill = character.getActiveSkill();
								//ARCHER: FIREARROW
								if (skill.getName().matches("firearrow")) {
									
									if (character.getMana() >= skill.getManaReq()) {
										damageEvent.getEntity().setFireTicks(skill.skillLevel*100);
										character.setMana((short)(character.getMana()-skill.getManaReq()));
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for fire arrow.");
										character.removeActiveSkill();
									}
								}
								//ARCHER impale
								else if (skill.getName().matches("impale")) {
									if (character.getMana() >= skill.getManaReq()) {
										boolean attachDot = true;
										
										if (event.getEntity() instanceof Player) {
											
											Player effected = (Player)event.getEntity();
											if (plugin.characters.containsKey(effected)) {
												
												Character character2 = plugin.characters.get(effected);
												if (character2.hasDot("bleeding") > -1) {
													attachDot = false;
												}

											}
										}
										
										if (attachDot) {
											new DoT((LivingEntity)event.getEntity(), "bleeding", 1, skill.getLevel()*100, plugin);
											character.setMana((short)(character.getMana()-skill.getManaReq()));
										}
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for impale.");
										character.removeActiveSkill();
									}
								}
								//ARCHER: PIERCE
								else if (skill.getName().matches("pierce")) {
									if (character.getMana() >= skill.getManaReq()) {
										if (event.getEntity() instanceof Player) {
											Player effected = (Player)event.getEntity();
											if (plugin.characters.containsKey(effected)) {
												defense = (int)Math.round(defense - (defense*(.5*skill.getLevel())));
												character.setMana((short)(character.getMana()-skill.getManaReq()));
											}
										}
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for pierce.");
										character.removeActiveSkill();
									}
								}
								//ARCHER: ICEARROW
								else if (skill.getName().matches("icearrow")) {
									if (character.getMana() >= skill.getManaReq()) {
										if (event.getEntity() instanceof Player) {
											Player effected = (Player)event.getEntity();
											if (plugin.characters.containsKey(effected)) {
												Character characterHit = plugin.characters.get(player);
												characterHit.setStunned(true);
												character.setMana((short)(character.getMana()-skill.getManaReq()));
												final Character stuck = character;
												plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
												    public void run() {
												    	stuck.setStunned(false);
												    }
												}, skill.skillLevel*100);
											}
										}
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for stun arrow.");
										character.removeActiveSkill();
									}
								}
								//ARCHER: TNTARROW
								else if (skill.getName().matches("tntarrow")) {
									if (character.getMana() >= skill.getManaReq()) {
										Block block = event.getEntity().getWorld().getBlockAt(event.getEntity().getLocation());
										float explosionStrenth = skill.skillLevel;
										addEntityDamage(event.getEntity(), character);
										attack = (int)Math.round((character.getStr()/arrowMultiplier)+(skill.getLevel()*1.5));
										plugin.explosion(player, block, explosionStrenth);
										character.setMana((short)(character.getMana()-skill.getManaReq()));
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for tnt arrow.");
										character.removeActiveSkill();
									}
								}
								//ARCHER: ARROW RAIN
								else if (skill.getName().matches("arrowrain")) {
									if (character.getMana() >= skill.getManaReq()) {
										Location location = event.getEntity().getLocation();
										World world = location.getWorld();
										Random generator = new Random();
										
										if (world.getHighestBlockYAt(location) == location.getY()) {
											for (int i=0;i<100;i++) {
												Location temp = location;
												Vector vector = new Vector();
												
												int randx = location.getBlockX()+(generator.nextInt(6)-3);
												int randz = location.getBlockZ()+(generator.nextInt(6)-3);
												temp.setX(randx);
												temp.setY(127);
												temp.setZ(randz);
												
												vector.setX(randx);
												vector.setY(127);
												vector.setZ(randz);
												world.spawnArrow(temp, vector, 0F, 0F);
											}
											character.setMana((short)(character.getMana()-skill.getManaReq()));
										}
										else {
											player.sendMessage(ChatColor.RED+"Your target is not outside.");
										}
										
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for Arrow Rain.");
										character.removeActiveSkill();
									}
								}
							}
						}
						else if (damageEvent.getProjectile() instanceof Snowball) {
							if (character.getActiveSkill() != null) {
								Skill skill = character.getActiveSkill();
								//MAGE: FIREBALL
								if (skill.getName().matches("fireball")) {
									if (character.getMana() >= skill.getManaReq()) {
										Entity entity = damageEvent.getEntity();
										entity.setFireTicks(40);
										attack = (int)Math.round((character.getInt()/magicMultiplier)+(skill.getLevel()*1.5));
										int radius = skill.getLevel()-1;
										Location currLoc = entity.getLocation();
										World world = entity.getWorld();
										world.getBlockAt(currLoc).setType(Material.FIRE);
										
										int px = currLoc.getBlockX();
										int py = currLoc.getBlockY();
										int pz = currLoc.getBlockZ();
										for(int x=-radius;x<=radius;x++) {
											for(int z=-radius;z<=radius;z++) {
												int xpos = px + x;
												int zpos = pz + z;
												if (world.getBlockAt(xpos, py, zpos).getTypeId() == 0)
													world.getBlockAt(xpos, py, zpos).setType(Material.FIRE);
											}
										}
										
										for(Entity entity2:entity.getLocation().getWorld().getEntities()){
										    double distance2 = entity.getLocation().toVector().subtract(entity2.getLocation().toVector()).lengthSquared();
										    if(distance2<=5){
										    	//entity2.setHealth(healee.getHealth()+healBy);
										    	addEntityDamage(entity2, character);
										    }
										}
										
										character.setMana((short)(character.getMana()-skill.getManaReq()));
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for Fireball.");
									}
									
								}
								//MAGE: BLAST
								else if (skill.getName().matches("blast")) {
									if (character.getMana() >= skill.getManaReq()) {
										Entity entity = event.getEntity();
										Block block = entity.getWorld().getBlockAt(event.getEntity().getLocation());
										float explosionStrength = skill.skillLevel;
										addEntityDamage(entity, character);
										attack = (int)Math.round((character.getInt()/magicMultiplier)+(skill.getLevel()*1.5));
										//cause explosion
										plugin.explosion(player, block, explosionStrength);
										
										character.setMana((short)(character.getMana()-skill.getManaReq()));
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for Blast.");
									}
								}
								//MAGE/PRIEST: Magic Arrow
								else if (skill.getName().matches("magicarrow")) {
									if (character.getMana() >= skill.getManaReq()) {
										attack = (int)Math.round((character.getInt()/magicMultiplier)+(skill.getLevel()*1.5));
										character.setMana((short)(character.getMana()-skill.getManaReq()));
										addEntityDamage(damageEvent.getEntity(), character);
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for Magic Arrow.");
									}
								}
								//MAGE: Bolt
								else if (skill.getName().matches("bolt")) {
									if (character.getMana() >= skill.getManaReq()) {
										Entity entity = damageEvent.getEntity();
										attack = (int)Math.round((character.getInt()/magicMultiplier)+(skill.getLevel()*1.5));
										entity.getWorld().strikeLightning(entity.getLocation());
										addEntityDamage(entity, character);
										character.setMana((short)(character.getMana()-skill.getManaReq()));
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for Bolt.");
									}
								}
								//MAGE: Plague
								else if (skill.getName().matches("plague")) {
									if (character.getMana() >= skill.getManaReq()) {
										Entity entity = damageEvent.getEntity();
										boolean attachDot = true;
										if (entity instanceof Player) {
											if (plugin.characters.containsKey((Player)entity)) {
												Character character2 = plugin.characters.get((Player)entity);
												if (character2.hasDot("diseased") > -1) {
													attachDot = false;
												}
											}
										}
										if (attachDot) {
											addEntityDamage(damageEvent.getEntity(), character);
											new DoT((LivingEntity)entity, "diseased", skill.getLevel()*5, 100, plugin);
											character.setMana((short)(character.getMana()-skill.getManaReq()));
										}
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for Plague.");
									}
								}
								//MAGE: entangle
								else if (skill.getName().matches("entangle")) {
									if (character.getMana() >= skill.getManaReq()) {
										Entity entity = damageEvent.getEntity();
										List<Block> webBlocks = new ArrayList<Block>();
										int radius = skill.getLevel();
										Location currLoc = entity.getLocation();
										World world = entity.getWorld();
										Block webBlock = world.getBlockAt(currLoc);
										webBlock.setType(Material.WEB);
										webBlocks.add(webBlock);
										plugin.webs.add(webBlock);
										
										int px = currLoc.getBlockX();
										int py = currLoc.getBlockY();
										int pz = currLoc.getBlockZ();
										
										for(int x=-radius;x<=radius;x++) {
											for(int z=-radius;z<=radius;z++) {
												int xpos = px + x;
												int zpos = pz + z;
												webBlock = world.getBlockAt(xpos, py, zpos);
												
												if (webBlock.getTypeId() == 0) {
													webBlocks.add(webBlock);
													plugin.webs.add(webBlock);
													webBlock.setType(Material.WEB);
												}
											}
										}
										//System.out.println(webBlocks.get(0).getX()+":"+webBlocks.get(0).getY()+":"+webBlocks.get(0).getZ());
										character.setMana((short)(character.getMana()-skill.getManaReq()));
										final List<Block> resetBlocks = webBlocks;
										//webBlocks = null;
										//removes webs automatically
										plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
										    public void run() {
										    	for(Block block:resetBlocks) {
										    		block.setTypeId(0);
										    		plugin.webs.remove(block);
										    	}
										    }
										}, 200);
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for Entangle.");
									}
								}
								//MAGE: Meteor
								else if (skill.getName().matches("meteor")) {
									if (character.getMana() >= skill.getManaReq()) {
										final Entity entity = damageEvent.getEntity();
										final Character finalChar = character;
										plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
										    public void run() {
										    	Meteor meteor = new Meteor(plugin, (LivingEntity)entity, finalChar);
												if (meteor.create())
													finalChar.setMana((short)(finalChar.getMana()-finalChar.getActiveSkill().getManaReq()));
										    }
										}, 5);
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for Meteor.");
									}
								}
								//PRIEST: HEAL
								else if (skill.getName().matches("heal")) {
									if (event.getEntity() instanceof Player) {
										if (character.getMana() >= skill.getManaReq()) {
											Player healee = (Player)event.getEntity();
											int healBy = Math.round((character.getSpr()/magicMultiplier)+(1.5F*skill.skillLevel));
											int healTotal = healee.getHealth()+healBy;
											if (healTotal > 20)
												healTotal = 20;
											healee.setHealth(healTotal);
											character.setMana((short)(character.getMana()-skill.getManaReq()));
										}
										else {
											player.sendMessage(ChatColor.RED+"You do not have enough mana for Heal.");
										}
									}
								}
								//PRIEST: GROUP HEAL
								else if (skill.getName().matches("groupheal")) {
									if (event.getEntity() instanceof Player) {
										if (character.getMana() >= skill.getManaReq()) {
											Player healee = (Player)event.getEntity();
											int healBy = Math.round((character.getSpr()/(magicMultiplier+10))+(1.5F*skill.skillLevel));
											int healTotal = healee.getHealth()+healBy;
											if (healTotal > 20)
												healTotal = 20;
											healee.setHealth(healTotal);
											
											for(Player player2:healee.getLocation().getWorld().getPlayers()){
											    double distance2 = healee.getLocation().toVector().subtract(player2.getLocation().toVector()).lengthSquared();
											    if(distance2<=5){
											    	int healTotal2 = healee.getHealth()+healBy;
													if (healTotal2 > 20)
														healTotal2 = 20;
											    	player2.setHealth(healTotal2);
											    }
											}
											character.setMana((short)(character.getMana()-skill.getManaReq()));
										}
										else {
											player.sendMessage(ChatColor.RED+"You do not have enough mana for Group Heal.");
										}
									}
								}
								//PRIEST: DISPEL
								else if (skill.getName().matches("dispel")) {
									if (event.getEntity() instanceof Player) {
										if (character.getMana() >= skill.getManaReq()) {
											Player healee = (Player)event.getEntity();
											if (plugin.characters.containsKey(healee)) {
												Character charHealee = plugin.characters.get(healee);
												if (charHealee.getDots().size() > 0) {
													for (DoT dot : charHealee.getDots()) {
														dot.removeDot();
													}
													healee.sendMessage(ChatColor.GREEN+player.getName()+" has restored you!");
												}
												character.setMana((short)(character.getMana()-skill.getManaReq()));
											}
										}
										else {
											player.sendMessage(ChatColor.RED+"You do not have enough mana for Dispel.");
										}
									}
								}
								//PRIEST: SLEEP
								else if (skill.getName().matches("sleep")) {
									if (event.getEntity() instanceof Player) {
										if (character.getMana() >= skill.getManaReq()) {
											Player sleeper = (Player)event.getEntity();
											if (plugin.characters.containsKey(sleeper)) {
												Character sleeping = plugin.characters.get(sleeper);
												int percent = 10;
												int timeSleep = 5;
												if (skill.getLevel() == 2) {
													percent = 30;
													timeSleep = 7;
												}
												if (skill.getLevel() == 3) {
													percent = 50;
													timeSleep = 10;
												}
												
												Random generator = new Random();
												if (generator.nextInt(100) <= percent) {
													sleeper.sendMessage(ChatColor.AQUA+"You are now sleeping!");
													player.sendMessage(ChatColor.AQUA+sleeper.getName()+" is now sleeping!");
													sleeping.setSleeping(true);
													final Character sleepingChar = sleeping;
													plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
														public void run() {
															sleepingChar.setSleeping(false);
														}
													}, (timeSleep*20));
												}
											}
											character.setMana((short)(character.getMana()-skill.getManaReq()));
										}
										else {
											player.sendMessage(ChatColor.RED+"You do not have enough mana for Sleep.");
										}
									}
								}
								//PRIEST: Poison
								else if (skill.getName().matches("poison")) {
									if (character.getMana() >= skill.getManaReq()) {
										Entity entity = damageEvent.getEntity();
										boolean attachDot = true;
										if (entity instanceof Player) {
											if (plugin.characters.containsKey((Player)entity)) {
												Character character2 = plugin.characters.get((Player)entity);
												if (character2.hasDot("poison") > -1) {
													attachDot = false;
												}
											}
										}
										if (attachDot) {
											addEntityDamage(damageEvent.getEntity(), character);
											new DoT((LivingEntity)entity, "poison", skill.getLevel()*5, 100, plugin);
											character.setMana((short)(character.getMana()-skill.getManaReq()));
										}
									}
									else {
										player.sendMessage(ChatColor.RED+"You do not have enough mana for Poison.");
									}
								}
								
								//PRIEST: HOLY
								else if (skill.getName().matches("holy")) {
									if (event.getEntity() instanceof LivingEntity) {
										if (character.getMana() >= skill.getManaReq()) {
											LivingEntity damagee = (LivingEntity)event.getEntity();
											//bypasses defense
											int damage = character.getInt()/magicMultiplier;
									    	damage++;
									    	addEntityDamage(damagee, character);
									    	int newHealth = damagee.getHealth()-damage;
									    	if (newHealth <= 0) {
									    		plugin.fixDrops(damagee);
									    		newHealth = 0;
									    	}
									    	damagee.setHealth(newHealth);
									    	
											for(Entity entity:damagee.getLocation().getWorld().getEntities()){
												if (entity instanceof LivingEntity) {
													LivingEntity lEntity = (LivingEntity)entity;
												    double distance2 = damagee.getLocation().toVector().subtract(lEntity.getLocation().toVector()).lengthSquared();
												    if(distance2<=(skill.getLevel()*5)){
												    	addEntityDamage(lEntity, character);
												    	int newHealth2 = damagee.getHealth()-damage;
												    	if (newHealth2 <= 0) {
												    		plugin.fixDrops(damagee);
												    		newHealth2 = 0;
												    	}
												    	lEntity.setHealth(newHealth2);
												    }
												}
											}
											character.setMana((short)(character.getMana()-skill.getManaReq()));
										}
										else {
											player.sendMessage(ChatColor.RED+"You do not have enough mana for Holy.");
										}
									}
								}
								//DEFENDER: WEAKEN
								else if (skill.getName().matches("weaken")) {
									if (event.getEntity() instanceof Player) {
										if (character.getMana() >= skill.getManaReq()) {
											Player weak = (Player)event.getEntity();
											if (plugin.characters.containsKey(weak)) {
												Character weakened = plugin.characters.get(weak);
												if (weakened.hasDebuff("weaken") > 0) {
													//Character chara, String buffName, String buffType, float amount, int time, Rifts instance
													weakened.addDebuff(new Debuff(weakened, "weaken", "weaken", (.05F*skill.getLevel()), 600, plugin));
												}
												character.setMana((short)(character.getMana()-skill.getManaReq()));
											}
										}
										else {
											player.sendMessage(ChatColor.RED+"You do not have enough mana for Weaken.");
										}
									}
								}
								//FIGHTER: DEGRADE
								else if (skill.getName().matches("degrade")) {
									if (event.getEntity() instanceof Player) {
										if (character.getMana() >= skill.getManaReq()) {
											Player weak = (Player)event.getEntity();
											if (plugin.characters.containsKey(weak)) {
												Character weakened = plugin.characters.get(weak);
												if (weakened.hasDebuff("degrade") > 0) {
													//Character chara, String buffName, String buffType, float amount, int time, Rifts instance
													weakened.addDebuff(new Debuff(weakened, "degrade", "defense", (.05F*skill.getLevel()), 600, plugin));
												}
												character.setMana((short)(character.getMana()-skill.getManaReq()));
											}
										}
										else {
											player.sendMessage(ChatColor.RED+"You do not have enough mana for Degrade.");
										}
									}
								}
							}
						}
					}
				}
				
				//attacker is mob
				else {
					if (damageEvent.getEntity() instanceof Player) {
						Player defender = (Player)damageEvent.getEntity();
						if (plugin.characters.containsKey(defender)) {
							Character defend = plugin.characters.get(defender);
							
							attack = damageEvent.getDamage() + (int)Math.round((defend.getLevel()+1) * mobMultiplier);
						}
					}
					
				}
			}
			
		}
		else if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent)event;
			Player player = null;
			Character character = null;
			Skill skill = null;
			
			if (damageEvent.getDamager() instanceof Player)
				player = (Player)damageEvent.getDamager();
			
			if (player != null) {
				if (plugin.characters.containsKey(player)) {
					character = plugin.characters.get(player);
					if (character.getActiveSkill() != null)
						skill = character.getActiveSkill();
				}
			}
			
			if (skill != null) {
				if (skill.getName().matches("chop") && player.getItemInHand().getType().name().contains("AXE")) {
					if (character.getMana() >= skill.getManaReq()) {
						attack = attack + skill.getLevel()*2;
						character.setMana((short)(character.getMana()-skill.getManaReq()));
					}
					else {
						player.sendMessage(ChatColor.RED+"You do not have enough mana for Chop.");
					}
				}
				
				else if (skill.getName().matches("slice") && player.getItemInHand().getType().name().contains("SWORD")) {
					if (character.getMana() >= skill.getManaReq()) {
						attack = attack + skill.getLevel()*2;
						character.setMana((short)(character.getMana()-skill.getManaReq()));
					}
					else {
						player.sendMessage(ChatColor.RED+"You do not have enough mana for Slice.");
					}
				}
				
				else if (skill.getName().matches("whirl") && player.getItemInHand().getType().name().contains("SWORD")) {
					if (character.getMana() >= skill.getManaReq()) {
						System.out.println("whirl on");
						LivingEntity damagee = (LivingEntity)damageEvent.getEntity();
						attack = (int)Math.round(attack + skill.getLevel()*1.5);
				    	
						for(Entity entity:damagee.getLocation().getWorld().getEntities()){
							if (entity instanceof LivingEntity) {
								LivingEntity lEntity = (LivingEntity)entity;
							    double distance2 = damagee.getLocation().toVector().subtract(lEntity.getLocation().toVector()).lengthSquared();
							    if(distance2<=(skill.getLevel()*4)){
							    	addEntityDamage(lEntity, character);
							    	if (lEntity instanceof Player) {
							    		Player playerHit = (Player)lEntity;
							    		if (!playerHit.equals(player)) {
								    		if (plugin.characters.containsKey(playerHit)) {
								    			Character characterHit = plugin.characters.get(playerHit);
								    			int newHealth = playerHit.getHealth()-(attack - characterHit.getDef());
								    			if (newHealth <= 0) {
								    				newHealth = 0;
								    				plugin.fixDrops(entity);
								    			}
								    			else if (newHealth > playerHit.getHealth())
								    				newHealth = playerHit.getHealth() - 1;
								    			playerHit.setHealth(newHealth);
								    		}
								    		else {
								    			int newHealth = playerHit.getHealth()-(attack - character.getLevel());
								    			if (newHealth <= 0) {
								    				newHealth = 0;
								    				plugin.fixDrops(entity);
								    			}
								    			else if (newHealth > playerHit.getHealth())
								    				newHealth = playerHit.getHealth() - 1;
								    			
								    			playerHit.setHealth(newHealth);
								    			
								    		}
							    		}
							    	}
							    	else {
						    			int newHealth = lEntity.getHealth()-(attack - character.getLevel());
						    			if (newHealth < 0) {
						    				plugin.deathby.put(lEntity.getEntityId(), character.getPlayer().getName());
						    				newHealth = 0;
						    				plugin.fixDrops(entity);
						    			}
						    			else if (newHealth > lEntity.getHealth())
						    				newHealth = lEntity.getHealth() - 1;
						    			
						    			lEntity.setHealth(newHealth);
						    		}
							    }
							}
						}
						character.setMana((short)(character.getMana()-skill.getManaReq()));
					}
					else {
						System.out.println("whirl off");
						player.sendMessage(ChatColor.RED+"You do not have enough mana for Whirl.");
					}
				}
				else if (skill.getName().matches("deadlystrike") && player.getItemInHand().getType().name().contains("SWORD")) {
					if (character.getMana() >= skill.getManaReq()) {
						overrideDamage = true;
						event.setDamage(15);
						character.setMana((short)(character.getMana()-skill.getManaReq()));
					}
					else {
						player.sendMessage(ChatColor.RED+"You do not have enough mana for Deadly Strike.");
					}
				}
			}
			
			//else if (attackMisses(damageEvent.getDamager(), damageEvent.getEntity())) {
			if (attackMisses(damageEvent.getDamager(), damageEvent.getEntity(), "melee")) {
				damageEvent.setCancelled(true);
			}
			
			else {
				if (player != null) {
					if (character != null) {
						//hack around inventory hooks missing
						if (character.getPlayer().getLocation().getWorld().getName().matches(plugin.lightWorldName) 
								|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.darkWorldName) 
								|| character.getPlayer().getLocation().getWorld().getName().matches(plugin.pvpWorldName))
							plugin.checkArmorAndTool(character);
						if (damageEvent.getEntity() instanceof Player) {
							if (plugin.characters.containsKey((Player)damageEvent.getEntity())) {
								Character defender = plugin.characters.get((Player)damageEvent.getEntity());
								//same faction, cancel damage
								if (defender.getFaction().matches(character.getFaction()) && plugin.useFactions) {
									damageEvent.setCancelled(true);
									return;
								}
							}
						}
						
						attack = characterAttack(character, "melee");
						
						addEntityDamage(damageEvent.getEntity(), character);
					}
				}
				
				//attacker is mob
				else {
					if (damageEvent.getEntity() instanceof Player) {
						Player defender = (Player)damageEvent.getEntity();
						if (plugin.characters.containsKey(defender)) {
							Character defend = plugin.characters.get(defender);
							
							attack = damageEvent.getDamage() + (int)Math.round((defend.getLevel()+1) * mobMultiplier);
						}
					}
					
				}
			}
			
		}

		if (!overrideDamage) {
			event.setDamage(attack - defense);
		}
	}
	
	public void onEntityDeath(EntityDeathEvent event) {
		plugin.death(event.getEntity());
	}
	
	public int characterAttack(Character character, String type) {
		int damage = 0;
		if (type.matches("melee"))
			damage = Math.round(character.getStr()/meleeMultiplier);
		else
			damage = Math.round(character.getStr()/arrowMultiplier);
		byte level = character.getLevel();
		Player player = character.getPlayer();
		String weaponName = player.getItemInHand().getType().name();
		
		if (damage < 1)
			damage = 1;
		
		if (weaponName.contains("SWORD") || (character.getClassName().matches("defender") && weaponName.contains("AXE"))) {
			if (weaponName.contains("WOOD"))
				damage = (int)Math.round(damage + (level*.025));
			else if (weaponName.contains("STONE"))
				damage = (int)Math.round(damage + (level*.05));
			else if (weaponName.contains("IRON"))
				damage = (int)Math.round(damage + (level*.075));
			else if (weaponName.contains("DIAMOND"))
				damage = (int)Math.round(damage + (level*.1));
		}
		
		//add damage from axe/sword power up
		if (character != null) {
			if (character.getClassName().matches("defender") && character.getAxePowerup() > 0 && character.getPlayer().getItemInHand().getType().name().contains("AXE")) {
				damage = damage + character.getAxePowerup();
			}	
			else if (character.getClassName().matches("fighter") && character.getSwordPowerup() > 0 && character.getPlayer().getItemInHand().getType().name().contains("SWORD")) {
				damage = damage + character.getSwordPowerup();
			}
		}
		
		if (character.getActiveSkill() != null) {
			Skill skill = character.getActiveSkill();
			if (skill.getName().matches("doubledmg")) {
				if (character.getMana() >= skill.getManaReq()) {
					damage = damage*2+((skill.getLevel()-1)*3);
					character.setMana((short)(character.getMana()-skill.getManaReq()));
				}
				else {
					character.getPlayer().sendMessage(ChatColor.RED+"You do not have enough mana for Double Dmg.");
				}
			}
		}
		
		return damage;
	}
	
	public int characterDefense(Character character, String type) {
		int defense = 0;
		if (type.matches("melee"))
			defense = Math.round(character.getDef()/meleeMultiplier);
		else
			defense = Math.round(character.getDef()/arrowMultiplier);
		int armorDef = 0;
		byte level = character.getLevel();
		Player player = character.getPlayer();
		String helmName = player.getInventory().getHelmet().getType().name();
		String plateName = player.getInventory().getChestplate().getType().name();
		String pantsName = player.getInventory().getLeggings().getType().name();
		String bootName = player.getInventory().getBoots().getType().name();
		
		if (defense < 1) {
			defense = 1;
		}

		if (helmName.contains("LEATHER"))
			armorDef = (int)Math.round(armorDef + (level*.025));
		else if (helmName.contains("IRON") || helmName.contains("GOLD"))
			armorDef = (int)Math.round(armorDef + (level*.05));
		else if (helmName.contains("DIAMOND"))
			armorDef = (int)Math.round(armorDef + (level*.1));
		
		if (plateName.contains("LEATHER"))
			armorDef = (int)Math.round(armorDef + (level*.025));
		else if (plateName.contains("IRON") || helmName.contains("GOLD"))
			armorDef = (int)Math.round(armorDef + (level*.05));
		else if (plateName.contains("DIAMOND"))
			armorDef = (int)Math.round(armorDef + (level*.1));
	
		if (pantsName.contains("LEATHER"))
			armorDef = (int)Math.round(armorDef + (level*.025));
		else if (pantsName.contains("IRON") || helmName.contains("GOLD"))
			armorDef = (int)Math.round(armorDef + (level*.05));
		else if (pantsName.contains("DIAMOND"))
			armorDef = (int)Math.round(armorDef + (level*.1));
	
		if (bootName.contains("LEATHER"))
			armorDef = (int)Math.round(armorDef + (level*.025));
		else if (bootName.contains("IRON") || helmName.contains("GOLD"))
			armorDef = (int)Math.round(armorDef + (level*.05));
		else if (bootName.contains("DIAMOND"))
			armorDef = (int)Math.round(armorDef + (level*.1));
		
		armorDef = Math.round(armorDef/3);
		defense = defense + armorDef;
		
		return defense;
	}
	
	public boolean attackMisses(Entity attacker, Entity defender, String attackType) {
		Random generator = new Random();
		int dexterity = 0;
		int agility = 0;
		Character attackChar = null;
		Character defendChar = null;
		
		if (attacker instanceof Player) {
			Player player = (Player)attacker;
			if (plugin.characters.containsKey(player)) {
				attackChar = plugin.characters.get(player);
				if (attackType.matches("melee"))
					dexterity = attackChar.getDex() + generator.nextInt(20);
				else if (attackType.matches("magic"))
					dexterity = attackChar.getSpr() + generator.nextInt(20);
			}
			else {
				dexterity = generator.nextInt(30);
			}
		}
		
		if (defender instanceof Player) {
			Player player = (Player)defender;
			if (plugin.characters.containsKey(player)) {
				defendChar = plugin.characters.get(player);
				agility = defendChar.getAgl() + generator.nextInt(20);
			}
			else {
				agility = generator.nextInt(30);
			}
		}
		
		//if neither the attacker or defender is a character, return because we don't care about missing.
		if (defendChar == null && attackChar == null) {
			return false;
		}
		
		//sets agility and dexterity for mobs
		if (defendChar != null && attackChar == null && dexterity == 0) {
			dexterity = defendChar.getLevel() + generator.nextInt(20);
		}
		if (attackChar != null && defendChar == null && agility == 0) {
			agility = attackChar.getLevel() + generator.nextInt(20);
		}
		
		//lower chances of dodging
		agility = agility/2;
		if (agility > dexterity) {
			if (attackChar != null) {
				if (attackChar.showMiss())
					attackChar.getPlayer().sendMessage(ChatColor.BLUE+"Attack missed!");
			}
			if (defendChar != null) {
				if (defendChar.showMiss())
					defendChar.getPlayer().sendMessage(ChatColor.BLUE+"Attack dodged!");
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	public void addEntityDamage(Entity entity, Character character) {
		if (plugin.deathby.size() < (plugin.getServer().getMaxPlayers() *10))
			plugin.deathby.clear();
		
		if (plugin.deathby.containsKey(entity.getEntityId()))
			plugin.deathby.remove(entity.getEntityId());
		
		plugin.deathby.put(entity.getEntityId(), character.getPlayer().getName());
	}
}
