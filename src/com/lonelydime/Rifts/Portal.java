package com.lonelydime.Rifts;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Portal {
	public static Rifts plugin;
	World riftWorld;
	Block portalBlock1;
	Block portalBlock2;
	Location teleLoc;
	String owningFaction;
	List<Block> portalBlocks = new ArrayList<Block>();
	
	public Portal(Rifts instance, Block portal1, Block portal2, String faction) {
		plugin = instance;
		riftWorld = portal1.getWorld();
		portalBlock1 = portal1;
		portalBlock2 = portal2;
		owningFaction = faction;
		if (portal1.getWorld().getName().matches(plugin.lightWorldName)) {
			teleLoc = plugin.getCage("light").getCenter();
			teleLoc.setY(teleLoc.getY()+2);
		}
		else if (portal1.getWorld().getName().matches(plugin.darkWorldName)) {
			teleLoc = plugin.getCage("dark").getCenter();
			teleLoc.setY(teleLoc.getY()+2);
		}
		else {
			if (owningFaction.matches("light")) {
				Cage cage = plugin.getCage("light");
				teleLoc = cage.getCenter();
			}
			else {
				Cage cage = plugin.getCage("dark");
				teleLoc = cage.getCenter();
			}
		}
	}
	
	public World getWorld() {
		World world = portalBlock1.getWorld();
		
		return world;
	}
	
	public Block getBlock1() {
		return portalBlock1;
	}
	
	public Block getBlock2() {
		return portalBlock2;
	}
	
	public Location getTeleLocation() {
		return teleLoc;
	}
	
	public String getFaction() {
		return owningFaction;
	}
	
	public void destroy() {
		plugin.portals.remove(this);
		plugin.data.removePortal(this);
	}
}
