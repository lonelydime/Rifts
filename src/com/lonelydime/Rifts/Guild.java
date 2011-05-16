package com.lonelydime.Rifts;

import java.util.ArrayList;
import java.util.List;

public class Guild {
	private Rifts plugin;
	private String guildLeader, guildName;
	private int guildPoints, guildId, guildGold, guildSize;
	private List<Character> onlineMembers = new ArrayList<Character>();
	private List<String> totalMembers = new ArrayList<String>();
	
	public Guild(int id, Rifts instance, String name, String leader, int size) {
		this.plugin = instance;
		this.guildLeader = leader;
		this.guildName = name;
		this.guildPoints = 0;
		this.guildId = id;
		this.guildGold = 0;
		this.guildSize = size;
		plugin.getServer();
	}
	
	public String getName() {
		return this.guildName;
	}
	
	public String getLeader() {
		return this.guildLeader;
	}
	
	public int getPoints() {
		return this.guildPoints;
	}
	
	public int getGold() {
		return this.guildGold;
	}
	
	public int getId() {
		return this.guildId;
	}
	
	public int getSize() {
		return this.guildSize;
	}
	
	//These are only for players online, not a comprehensive list
	public List<Character> getOMembers() {
		return this.onlineMembers;
	}
	
	public void addOMember (Character character) {
		this.onlineMembers.add(character);
	}
	
	public void removeOMember (Character character) {
		this.onlineMembers.remove(character);
	}
	
	//This is a comprehensive list, not all players will be online
	public List<String> getTMembers() {
		return this.totalMembers;
	}
	
	public void addTMember (String playerName) {
		this.totalMembers.add(playerName);
	}
	
	public void removeTMember (String playerName) {
		this.totalMembers.remove(playerName);
	}
	
	public void sendGuildMessage(String message) {
		for (Character character:this.onlineMembers)
			character.getPlayer().sendMessage(message);
	}
}
