package com.lonelydime.Rifts;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Rift {
	public static Rifts plugin;
	World riftWorld;
	Location location;
	Location teleTo;
	HashSet<Block> blocks = new HashSet<Block>();
	
	public Rift(Rifts instance, Location startLoc, Location teleToLoc) {
		plugin = instance;
		riftWorld = startLoc.getWorld();
		location = startLoc;
		location.setX(location.getBlockX());
		location.setY(location.getBlockY());
		location.setZ(location.getBlockZ());
		
		teleTo = teleToLoc;
		teleTo.setX(teleTo.getBlockX());
		teleTo.setY(teleTo.getBlockY());
		teleTo.setZ(teleTo.getBlockZ());
		
	}
	
	public void createGate() {
		System.out.println("Creating rift - World: "+location.getWorld().getName()+". Loc: "+location.getBlockX()+", "+location.getBlockY()+", "+location.getBlockZ());
		int gateBlockId = 49;
		World world = location.getWorld();
		Location temploc = location;
		
		//gate bottom
		for (int i=0;i<2;i++) {
			temploc.setX(temploc.getX()+1);
			world.getBlockAt(temploc).setTypeId(gateBlockId);
			blocks.add(world.getBlockAt(temploc));
		}
		
		//gate middle
		temploc.setX(temploc.getX()-3);
		for (int j=0;j<3;j++) {
			temploc.setY(temploc.getY()+1);
			for (int i=0;i<4;i++) {
				temploc.setX(temploc.getX()+1);
				if (i == 0 || i == 3) {
					world.getBlockAt(temploc).setTypeId(gateBlockId);
				}
				blocks.add(world.getBlockAt(temploc));
			}
			temploc.setX(temploc.getX()-4);
		}
		
		//gate top
		temploc.setY(temploc.getY()+1);
		temploc.setX(location.getX()+1);
		for (int i=0;i<2;i++) {
			temploc.setX(temploc.getX()+1);
			world.getBlockAt(temploc).setTypeId(gateBlockId);
			blocks.add(world.getBlockAt(temploc));
		}
		
		temploc.setY(temploc.getY()-3);
		temploc.setX(temploc.getX()-1);
		world.getBlockAt(temploc).setTypeId(51);
		
		plugin.messageAllPlayers(world, ChatColor.DARK_RED+"A new rift has appeared!");
	}
	
	public Block[] getTeleportBlocks() {
		Block[] teleBlocks = {null, null};
		teleBlocks[0] = location.getWorld().getBlockAt(location.getBlockX()+1, location.getBlockY(), location.getBlockZ());
		teleBlocks[1] = location.getWorld().getBlockAt(location.getBlockX()+2, location.getBlockY(), location.getBlockZ());
		
		return teleBlocks;
	}
	
	public HashSet<Block> getBlocks() {
		return blocks;
	}
	
	public boolean isRiftBlock(Block block) {
		boolean isRift = false;
		
		Iterator<Block> itr = this.blocks.iterator();

		while(itr.hasNext()) {
			if (block.equals(itr.next())) 
				isRift = true;
		}
		
		return isRift;
	}
	
	public void destroy() {
		World world = this.location.getWorld();
		Iterator<Block> itr = blocks.iterator();

		while(itr.hasNext()) {
			itr.next().setTypeId(0);
		}
		plugin.messageAllPlayers(world, ChatColor.DARK_RED+"The rift has disappeared!");
	}
}
