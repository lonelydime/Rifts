package com.lonelydime.Rifts;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.block.Block;

public class Meteor {
	public static Rifts plugin;
	private LivingEntity target;
	private List<Block> blocks = new ArrayList<Block>();
	private Character summoner;
	private Block bottomBlock;
	private int locx, locy, locz;
	
	public Meteor(Rifts instance, LivingEntity lEntity, Character character) {
		plugin = instance;
		target = lEntity;
		summoner = character;
		locy = 127;
	}
	
	public boolean create() {
		Location iterator = this.target.getLocation();
		World world = iterator.getWorld();
		
		if (iterator.getWorld().getHighestBlockYAt(iterator) == iterator.getY()) {
			iterator.setY(127);
			int x,y,z;
			x = iterator.getBlockX();
			this.locx = x;
			y = 127;
			z = iterator.getBlockZ();
			this.locz = z;
			
			for (int k=y;k>y-5;k--) {
				for (int i=x;i<x+4;i++) {
					for (int j=z;j<z+4;j++) {
						iterator.setZ(j);
						if (iterator.getBlockY() == 127)
							world.getBlockAt(iterator).setTypeId(51);
						else
							world.getBlockAt(iterator).setTypeId(87);
						//blocks.add(iterator.getBlock());
					}
					iterator.setX(i);
				}
				iterator.setY(k);
			}
			
			bottomBlock = iterator.getBlock();
			this.locy--;
			
			final Meteor meteor = this;
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
			    public void run() {
			    	if (meteor.bottomBlock.getTypeId() == 0)
			    		meteor.move();
			    	else
			    		meteor.explode();
			    }
			}, 10);
			
			return true;
		}
		else {
			//System.out.println(iterator.getWorld().getHighestBlockYAt(iterator)+" == "+iterator.getY());
			this.summoner.getPlayer().sendMessage(ChatColor.RED+"Your target is not outside.");
			
			return false;
		}
	}
	
	private void move() {
		Location iterator = this.target.getWorld().getBlockAt(locx, locy, locz).getLocation();
		World world = iterator.getWorld();
		this.blocks.clear();
		int x,y,z;
		x = locx;
		y = locy;
		z = locz;
		
		for (int k=(y+1);k>y-5;k--) {
			for (int i=x;i<x+4;i++) {
				for (int j=z;j<z+4;j++) {
					iterator.setZ(j);
					if (iterator.getBlockY() == (locy+1))
						world.getBlockAt(iterator).setTypeId(0);
					else if (iterator.getBlockY() == locy)
						world.getBlockAt(iterator).setTypeId(51);
					else
						world.getBlockAt(iterator).setTypeId(87);
					blocks.add(iterator.getBlock());
				}
				iterator.setX(i);
			}
			iterator.setY(k);
		}
		
		locy--;
		bottomBlock = iterator.getBlock();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			System.out.println("[Rifts] Meteor move failed: "+e);
		}
		if (this.bottomBlock.getTypeId() == 0)
			this.move();
    	else
    		this.explode();
	}
	
	private void explode() {
		for (Block block:this.blocks) {
			block.setTypeId(0);
		}
		
		plugin.explosion(this.summoner.getPlayer(), bottomBlock, 8F);
	}
}
