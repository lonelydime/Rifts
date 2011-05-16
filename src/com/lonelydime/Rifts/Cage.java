package com.lonelydime.Rifts;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Cage {
	Rifts plugin;
	String faction;
	List<Block> blocks = new ArrayList<Block>();
	Block door;
	Location center;
	
	public Cage(Rifts instance, Location location, String factionCreator, boolean setup) {
		plugin = instance;
		faction = factionCreator;
		center = location;
		
		int x, y, z;
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();
		
		for (int k=(y-1);k<=(y+5);k++) {
			for (int i=(x-5);i<=(x+5);i++) {
				for (int j=(z-5);j<=(z+5);j++) {
					Block block = location.getWorld().getBlockAt(i, k, j);
					//floor level
					if (k == (y-1)) {
						if (setup)
							block.setTypeId(89);
						blocks.add(block);
					}
					//roof level
					else if (k == (y+5)) {
						if (setup)
							block.setTypeId(20);
						blocks.add(block);
					}
					//mid level
					else {
						if (i == (x+5) && j == (z) && k == y) {
							if (setup) {
								block.setTypeIdAndData(64, (byte) 0, false);
								block.getFace(BlockFace.UP).setTypeIdAndData(64, (byte) 8, true);
							}
							blocks.add(block);
							blocks.add(block.getFace(BlockFace.UP));
						}
						else if (i == (x+5) && j == (z) && k == (y+1)) {

						}
						
						if (i == (x-5) && j == (z) && k == y) {
							if (setup) {
								block.setTypeIdAndData(64, (byte) 0, false);
								block.getFace(BlockFace.UP).setTypeIdAndData(64, (byte) 8, true);
							}
							blocks.add(block);
							blocks.add(block.getFace(BlockFace.UP));
						}
						else if (i == (x-5) && j == (z) && k == (y+1)) {

						}
						
						if (i == (x) && j == (z+5) && k == y) {
							if (setup) {
								block.setTypeIdAndData(64, (byte) 0, false);
								block.getFace(BlockFace.UP).setTypeIdAndData(64, (byte) 8, true);
							}
							blocks.add(block);
							blocks.add(block.getFace(BlockFace.UP));
						}
						else if (i == (x) && j == (z+5) && k == (y+1)) {

						}
						
						else if (i == (5) && j == (z-5) && k == y) {
							if (setup) {
								block.setTypeIdAndData(64, (byte) 0, false);
								block.getFace(BlockFace.UP).setTypeIdAndData(64, (byte) 8, true);
							}
							blocks.add(block);
							blocks.add(block.getFace(BlockFace.UP));
						}
						else if (i == (x) && j == (z-5) && k == (y+1)) {

						}
						
						else if (i == (x-5) || i == (x+5)) {
							if (setup)
								block.setTypeId(20);
							blocks.add(block);
						}
						else if (j == (z-5) || j == (z+5)) {
							if (setup)
								block.setTypeId(20);
							blocks.add(block);
						}
						else {
							if (setup)
								block.setTypeId(0);
						}
					}
				}
			}
		}
		
		if (setup) {
			//save to the database
			plugin.data.saveCage(this);
		}
	}
	
	public Location getCenter() {
		return this.center;
	}
	
	public String getFaction() {
		return this.faction;
	}
	
	public boolean isCageBlock(Block block) {
		if (this.blocks.contains(block))
			return true;
		return false;
	}
}
