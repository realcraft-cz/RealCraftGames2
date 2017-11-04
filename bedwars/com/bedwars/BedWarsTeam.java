package com.bedwars;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.games.Games;
import com.games.game.GameState;
import com.games.player.GamePlayer;
import com.games.utils.EntityUtil;

import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.GenericAttributes;

public class BedWarsTeam {

	private BedWars game;
	private BedWarsTeamType type;
	private ArrayList<GamePlayer> players = new ArrayList<GamePlayer>();

	private Inventory inventory;
	private ArrayList<Block> chests = new ArrayList<Block>();

	public BedWarsTeam(BedWars game,BedWarsTeamType type){
		this.game = game;
		this.type = type;
	}

	public BedWars getGame(){
		return game;
	}

	public BedWarsTeamType getType(){
		return type;
	}

	public void resetTeam(){
		players.clear();
	}

	public ArrayList<GamePlayer> getPlayers(){
		return players;
	}

	public boolean isPlayerInTeam(GamePlayer gPlayer){
		return players.contains(gPlayer);
	}

	public Location getSpawnLocation(){
		return game.getArena().getTeamSpawn(type);
	}

	public Location getBedLocation(){
		return game.getArena().getTeamBed(type);
	}

	public Block getBedBlock(){
		return this.getBedLocation().getBlock();
	}

	public boolean hasBed(){
		return (this.getBedBlock().getType() == Material.BED_BLOCK);
	}

	public Inventory getInventory(){
		if(inventory == null) inventory = Bukkit.createInventory(null,InventoryType.ENDER_CHEST,"Tymova truhla");
		return inventory;
	}

	public void resetInventory(){
		inventory = null;
	}

	public void addChest(Block chest){
		chests.add(chest);
		if(inventory == null) this.resetInventory();
	}

	public void removeChest(Block chest){
		chests.remove(chest);
		if(chests.size() == 0){
			this.resetInventory();
		}
	}

	public ArrayList<Block> getChests(){
		return chests;
	}

	public void resetChests(){
		chests = new ArrayList<Block>();
	}

	public void addPlayer(GamePlayer gPlayer){
		players.add(gPlayer);
		Games.getEssentials().getUser(gPlayer.getPlayer()).setNickname(type.getChatColor()+gPlayer.getPlayer().getName()+"§r");
		Games.getEssentials().getUser(gPlayer.getPlayer()).setDisplayNick();
	}

	public void removePlayer(GamePlayer gPlayer){
		players.remove(gPlayer);
		Games.getEssentials().getUser(gPlayer.getPlayer()).setNickname(Games.getEssentials().getUser(gPlayer.getPlayer()).getName());
		Games.getEssentials().getUser(gPlayer.getPlayer()).setDisplayNick();
	}

	public void setPlayerInventory(GamePlayer gPlayer){
		ItemStack itemStack;
		LeatherArmorMeta meta;

		itemStack = new ItemStack(Material.LEATHER_HELMET,1);
		meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getType().getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setHelmet(itemStack);

        itemStack = new ItemStack(Material.LEATHER_CHESTPLATE,1);
		meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getType().getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setChestplate(itemStack);

        itemStack = new ItemStack(Material.LEATHER_LEGGINGS,1);
		meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getType().getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setLeggings(itemStack);

        itemStack = new ItemStack(Material.LEATHER_BOOTS,1);
		meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getType().getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setBoots(itemStack);
	}

	public void destroyBed(GamePlayer gPlayer){
		if(players.size() > 0){
			for(GamePlayer gPlayer2 : game.getPlayers()){
				gPlayer2.getPlayer().playSound(gPlayer2.getPlayer().getLocation(),Sound.ENTITY_ENDERDRAGON_GROWL,1f,1f);
			}
			game.sendMessage("§c\u2623 §b"+gPlayer.getPlayer().getName()+" §7znicil postel tymu "+this.getType().getChatColor()+this.getType().toName());
			gPlayer.getSettings().addInt("beds",1);
		}
	}

	public void spawnSheep(Location location){
		Entity sheep = location.getWorld().spawnEntity(location,EntityType.SHEEP);
    	((Sheep)sheep).setAdult();
    	((Sheep)sheep).setAgeLock(true);
    	((Sheep)sheep).setInvulnerable(true);
    	((Sheep)sheep).setRemoveWhenFarAway(false);
    	((Sheep)sheep).setColor(this.getType().getDyeColor());

    	Entity tnt = location.getWorld().spawnEntity(location,EntityType.PRIMED_TNT);
    	((TNTPrimed)tnt).setFuseTicks(8*20);
    	((TNTPrimed)tnt).setIsIncendiary(false);
    	sheep.addPassenger(tnt);

    	EntityUtil.clearPathfinders(sheep);
    	new BukkitRunnable(){
    		@Override
    		public void run(){
    			GamePlayer gPlayer = BedWarsTeam.this.getNearestEnemy(sheep.getLocation());
    			if(gPlayer != null && !sheep.isDead()){
    				((EntityInsentient) ((CraftEntity)sheep).getHandle()).getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(128);
    				((EntityInsentient) ((CraftEntity)sheep).getHandle()).getNavigation().a(((CraftEntity)gPlayer.getPlayer()).getHandle(),2);
    			}
    			if(game.getState() != GameState.INGAME || sheep.isDead() || tnt.isDead()){
    				this.cancel();
    			}
    		}
    	}.runTaskTimer(Games.getInstance(),1,10);

    	new BukkitRunnable(){
    		@Override
    		public void run(){
    			if(game.getState() != GameState.INGAME || sheep.isDead() || tnt.isDead()){
    				sheep.remove();
    				tnt.remove();
    				this.cancel();
    			}
    		}
    	}.runTaskTimer(Games.getInstance(),2,2);
	}

	public GamePlayer getNearestEnemy(Location location){
		double distance = Double.MAX_VALUE;
		GamePlayer nearest = null;
		for(BedWarsTeam team : game.getTeams().getTeams()){
			if(team.getType() == this.getType()) continue;
			for(GamePlayer gPlayer : team.getPlayers()){
				double tmpDistance = gPlayer.getPlayer().getLocation().distanceSquared(location);
				if(tmpDistance < distance){
					distance = tmpDistance;
					nearest = gPlayer;
				}
			}
		}
		return nearest;
	}

	public enum BedWarsTeamType {
		RED, BLUE, GREEN, YELLOW;

		public String toString(){
			return this.name().toLowerCase();
		}

		public String toName(){
			switch(this){
				case RED: return "Red";
				case BLUE: return "Blue";
				case GREEN: return "Green";
				case YELLOW: return "Yellow";
			}
			return null;
		}

		public Color getColor(){
			switch(this){
				case RED: return Color.RED;
				case BLUE: return Color.BLUE;
				case GREEN: return Color.GREEN;
				case YELLOW: return Color.YELLOW;
			}
			return Color.WHITE;
		}

		public ChatColor getChatColor(){
			switch(this){
				case RED: return ChatColor.RED;
				case BLUE: return ChatColor.BLUE;
				case GREEN: return ChatColor.GREEN;
				case YELLOW: return ChatColor.YELLOW;
			}
			return ChatColor.WHITE;
		}

		public DyeColor getDyeColor(){
			switch(this){
				case RED: return DyeColor.RED;
				case BLUE: return DyeColor.BLUE;
				case GREEN: return DyeColor.LIME;
				case YELLOW: return DyeColor.YELLOW;
			}
			return DyeColor.WHITE;
		}
	}
}