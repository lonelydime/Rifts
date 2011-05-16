package com.lonelydime.Rifts;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class SpawnBlock {
	public static Rifts plugin;
	Location location;
	Block spawnBlock;
	String creator;
	int uses;
	List<Player> boundPlayers = new ArrayList<Player>();
	
	public SpawnBlock(Rifts instance, Block block, Player player, int usesLeft) {
		plugin = instance;
		this.spawnBlock = block;
		this.location = block.getLocation();
		this.creator = player.getName();
		this.uses = usesLeft;
		this.boundPlayers.add(player);
	}
	
	public Location RespawnBindBlock() {
		Location spawnLoc = this.location;
		spawnLoc.setY(spawnLoc.getY()+2);
		
		return spawnLoc;
	}
	
	public Block getSpawnBlock() {
		return this.spawnBlock;
	}
	
	public int getSpawnBlockUses() {
		return this.uses;
	}
	
	public String getSpawnCreator() {
		return this.creator;
	}
	
	public List<Player> getSpawnBinders() {
		return this.boundPlayers;
	}
	
	public void DestroySpawnBlock(String playername) {
		String destroyedby = "The spawn block you are bound to expired!";
		
		if (!playername.matches("SpawnBlockOverUse") && !playername.matches("[serverstop]")) {
			destroyedby = "The spawn block you are bound to was destroyed by "+playername;
		}
		
		if (!playername.matches("SpawnBlockOverUse") && !playername.matches("[serverstop]")) {
			this.messageAllPlayers(ChatColor.RED+destroyedby);
		}
		
		for (Player player : this.boundPlayers) {
			this.unbindSpawn(player);
		}
		
		if (this.spawnBlock.getTypeId() == 49) {		
			this.spawnBlock.setTypeId(0);
			this.spawnBlock.getFace(BlockFace.UP).setTypeId(0);
		}
	}
	
	public void bindToBlock(Player player) {
		this.boundPlayers.add(player);
		plugin.spawnblocks.put(player, this);
	}
	
	public void unbindSpawn(Player player) {
		this.boundPlayers.remove(player);
		plugin.spawnblocks.remove(player);
		
		Block signBlock = null;
		
		if (this.spawnBlock.getFace(BlockFace.NORTH).getTypeId() == 68) {
			signBlock = this.spawnBlock.getFace(BlockFace.NORTH);
		}
		else if (this.spawnBlock.getFace(BlockFace.EAST).getTypeId() == 68) {
			signBlock = this.spawnBlock.getFace(BlockFace.EAST);					
		}
		else if (this.spawnBlock.getFace(BlockFace.SOUTH).getTypeId() == 68) {
			signBlock = this.spawnBlock.getFace(BlockFace.SOUTH);
		}
		else if (this.spawnBlock.getFace(BlockFace.WEST).getTypeId() == 68) {
			signBlock = this.spawnBlock.getFace(BlockFace.WEST);
		}
		
		//if the creator leaves, destroy the block.
		if (this.creator.matches(player.getName())) {
			DestroySpawnBlock(player.getName());
		}
		else {
			//remove name from sign
			BlockState state = signBlock.getState();

			Sign sign = (Sign)state;
			  
			if (sign.getLine(1).matches(player.getName()))
				  sign.setLine(1, "");
			else if (sign.getLine(2).matches(player.getName()))
				  sign.setLine(2, "");
			else if (sign.getLine(3).matches(player.getName()))
				  sign.setLine(3, "");
			sign.update();
		}
		
		player.sendMessage(ChatColor.GOLD+"You have been removed from the spawn block.");
		
	}
	
	public void messageAllPlayers(String message) {
		int i = 0;
		while (i < this.boundPlayers.size()) {
			boundPlayers.get(i).sendMessage(message);
			i++;
		}
	}
	
	public void setUsesLeft(int usesLeft) {
		this.uses = usesLeft;
	}

}
