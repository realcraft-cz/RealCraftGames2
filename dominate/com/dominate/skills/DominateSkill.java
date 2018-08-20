package com.dominate.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;

import com.dominate.Dominate;
import com.dominate.DominateUser;
import com.games.Games;
import com.games.player.GamePlayer;
import com.games.utils.Title;

public abstract class DominateSkill implements Listener, Runnable {

	public static final String BLOCK_CHAR = "\u258C";
	public static final int BLOCK_CHARS = 20;

	private DominateSkillType type;
	private DominateUser dPlayer;
	private GamePlayer gPlayer;
	private boolean running = false;
	private int cooldown = -1;
	private int power = 100;
	private Dominate game;
	private BukkitTask task = null;
	private BukkitTask task2 = null;

	public DominateSkill(DominateSkillType type,Dominate game,DominateUser dPlayer){
		this.type = type;
		this.dPlayer = dPlayer;
		this.gPlayer = dPlayer.getGamePlayer();
		this.game = game;
	}

	public DominateSkillType getType(){
		return type;
	}

	public DominateUser getDominateUser(){
		return dPlayer;
	}

	public GamePlayer getGamePlayer(){
		return gPlayer;
	}

	public Player getPlayer(){
		return gPlayer.getPlayer();
	}

	public Dominate getGame(){
		return game;
	}

	public void setEnabled(boolean enabled){
		if(enabled){
			Bukkit.getPluginManager().registerEvents(this,Games.getInstance());
			if(this.getType().getRunningSpeed() > 0){
				task = Bukkit.getScheduler().runTaskTimer(Games.getInstance(),this,this.getType().getRunningSpeed(),this.getType().getRunningSpeed());
			}
			task2 = Bukkit.getScheduler().runTaskTimerAsynchronously(Games.getInstance(),new Runnable(){
				@Override
				public void run(){
					DominateSkill.this.runCooldown();
					DominateSkill.this.runPower();
				}
			},4,4);
			this.updateInventory();
		} else {
			HandlerList.unregisterAll(this);
			if(task != null){
				task.cancel();
				task = null;
			}
			if(task2 != null){
				task2.cancel();
				task2 = null;
			}
			this.clear();
		}
		this.setRunning(false);
		this.setCooldown(-1);
		this.setPower(100);
	}

	public boolean isRunning(){
		return running;
	}

	public void setRunning(boolean running){
		this.running = running;
	}

	public boolean isCooldown(){
		return (cooldown != -1);
	}

	public int getCooldown(){
		return cooldown;
	}

	public void setCooldown(int cooldown){
		this.cooldown = cooldown;
	}

	public boolean hasLowPower(){
		return (this.getPower() < this.getType().getPower());
	}

	public int getPower(){
		return power;
	}

	public void setPower(int power){
		this.power = power;
		gPlayer.getPlayer().setExp(this.getPower()*0.01f);
	}

	public boolean trigger(){
		return this.trigger(null);
	}

	public boolean trigger(Entity entity){
		if(this.isRunning()) return false;
		if(this.isCooldown()) return false;
		if(this.hasLowPower()) return false;
		this.setCooldown(this.getType().getCooldown());
		if(this.getType().getPower() > 0) this.setPower(this.getPower()-this.getType().getPower());
		this.activate(entity);
		return true;
	}

	public abstract void activate(Entity entity);
	public abstract void recharged();
	public abstract void clear();
	public abstract void updateInventory();

	public void runCooldown(){
		if(this.isCooldown()){
			String message = "";
			int cooldown = (BLOCK_CHARS-(int)(this.getCooldown()*((float)BLOCK_CHARS/this.getType().getCooldown())));
			for(int i=0;i<BLOCK_CHARS;i++){
				if(cooldown > i) message += "§a";
				else message += "§c";
				message += BLOCK_CHAR;
			}
			Title.sendActionBar(gPlayer.getPlayer(),message);
			this.setCooldown(this.getCooldown()-1);
			if(!this.isCooldown()){
				Title.sendActionBar(gPlayer.getPlayer(),"");
				this.recharged();
			}
		}
	}

	public void runPower(){
		if(this.getPower() < 100){
			this.setPower(this.getPower()+1);
		}
	}

	@EventHandler
	public void PlayerDeathEvent(PlayerDeathEvent event){
		if(event.getEntity().equals(gPlayer.getPlayer())){
			this.setCooldown(-1);
			Title.sendActionBar(gPlayer.getPlayer(),"");
		}
	}

	public enum DominateSkillType {
		FIRE, ARROW_FIRE, ARROW_EXPLOSIVE, ARROW_BLINDNESS, ARROW_GROUP, FIREBALL, ICETRAP, FROSTWALK, SMASHDOWN, JUMP, RECALL, WEB, WATER_BOTTLE, SOUP;

		public String getName(){
			switch(this){
				default:break;
			}
			return null;
		}

		public Material getMaterial(){
			switch(this){
				case ARROW_FIRE: return Material.TIPPED_ARROW;
				case ARROW_EXPLOSIVE: return Material.TIPPED_ARROW;
				case ARROW_BLINDNESS: return Material.TIPPED_ARROW;
				case FIRE: return Material.BLAZE_POWDER;
				case FIREBALL: return Material.FIRE_CHARGE;
				case ICETRAP: return Material.ICE;
				case FROSTWALK: return Material.ICE;
				case RECALL: return Material.IRON_SWORD;
				case WEB: return Material.COBWEB;
				case WATER_BOTTLE: return Material.POTION;
				case SOUP: return Material.MUSHROOM_STEW;
				default:break;
			}
			return null;
		}

		public int getAmount(){
			switch(this){
				case ARROW_FIRE: return 32;
				case ARROW_EXPLOSIVE: return 32;
				case ARROW_BLINDNESS: return 32;
				case WEB: return 3;
				case WATER_BOTTLE: return 2;
				default:break;
			}
			return 1;
		}

		public int getRunningSpeed(){
			switch(this){
				case FIRE: return 20;
				case ARROW_GROUP: return 2;
				case ICETRAP: return 1;
				case SMASHDOWN: return 1;
				case JUMP: return 1;
				case RECALL: return 1;
				case WEB: return 1;
				default:break;
			}
			return 0;
		}

		public int getCooldown(){
			switch(this){
				case ARROW_FIRE: return 12*5;
				case ARROW_EXPLOSIVE: return 12*5;
				case ARROW_BLINDNESS: return 12*5;
				case ICETRAP: return 16*5;
				default:break;
			}
			return -1;
		}

		public int getPower(){
			switch(this){
				case SMASHDOWN: return 100;
				case JUMP: return 30;
				case RECALL: return 30;
				default:break;
			}
			return 0;
		}

		public ItemStack getItemStack(){
			ItemStack item = new ItemStack(this.getMaterial(),this.getAmount());
			if(this == ARROW_FIRE){
				PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
				potionMeta.setBasePotionData(new PotionData(PotionType.FIRE_RESISTANCE));
				item.setItemMeta(potionMeta);
			}
			else if(this == ARROW_EXPLOSIVE){
				PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
				potionMeta.setBasePotionData(new PotionData(PotionType.WEAKNESS));
				item.setItemMeta(potionMeta);
			}
			else if(this == ARROW_BLINDNESS){
				PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
				potionMeta.setBasePotionData(new PotionData(PotionType.SPEED));
				item.setItemMeta(potionMeta);
			}
			return item;
		}
	}
}