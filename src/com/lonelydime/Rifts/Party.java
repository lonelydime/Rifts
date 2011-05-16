package com.lonelydime.Rifts;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class Party {
	boolean isFull;
	boolean allowInvites;
	List<Player> members = new ArrayList<Player>();
	String lootType = "random";
	String leader;
	int partySize;
	
	public static Rifts plugin;
	
	public Party(Player starter, Rifts instance) {
		members.add(starter);
		lootType = "random";
		leader = starter.getName();
		isFull = false;
		allowInvites = false;
		plugin = instance;
		partySize = 4;
	}
	
	public boolean isLeader(String name) {
		boolean isLeader = false;
		if (name.matches(this.leader)) {
			isLeader = true;
		}
		
		return isLeader;
	}
	
	public boolean add(Player player) {
		boolean success = false;
		if (members.size() < this.partySize) {
			plugin.parties.put(player, this);
			this.members.add(player);
			success = true;
		}
		
		return success;
	}
	
	public boolean leave(Player player) {
		boolean success = false;
		
		plugin.parties.remove(player);
		this.members.remove(player);
		
		return success;
	}
	
	public Player[] returnMembers() {
		Player[] playerList = new Player[this.members.size()];
		int i=0;
		
		while(i < this.members.size()) {
			playerList[i] = this.members.get(i);
			i++;
		}
		
		return playerList;
	}
	
	public void sendPartyMessage(String message) {
		for (int i=0;i<this.members.size();i++)
			this.members.get(i).sendMessage(message);
	}
	
	public void changeLeader(Player player) {
		this.leader = player.getName();
	}
	
	public void setLoot(String loot) {
		this.lootType = loot;
	}
	
	public void setInvite(boolean invite) {
		this.allowInvites = invite;
	}
}
