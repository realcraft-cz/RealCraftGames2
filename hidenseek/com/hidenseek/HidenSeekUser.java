package com.hidenseek;

import com.games.player.GamePlayer;
import com.games.utils.Glow;
import com.hidenseek.HidenSeekTeam.HidenSeekTeamType;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import realcraft.bukkit.utils.Particles;

public class HidenSeekUser {

	private static final int WEAPON_DAMAGE = 10;
	private static final int WEAPON_TIMEOUT = 100;

	private HidenSeek game;
	private GamePlayer gPlayer;

	private long spawnTime;
	private long meowTime;
	private long fireworkTime;
	private long blockTime;
	private int fireworks = 5;
	private int solidCountdown = 5;
	private int tracker = 1;
	private int weaponDamage = 0;
	private long weaponTime = 0;

	private boolean solid = false;
	private DisguiseType type;
	private ArmorStand stand;
	private FallingBlock block;
	private Block origBlock;

	public HidenSeekUser(HidenSeek game,GamePlayer gPlayer){
		this.game = game;
		this.gPlayer = gPlayer;
	}

	public HidenSeek getGame(){
		return game;
	}

	public GamePlayer getGamePlayer(){
		return gPlayer;
	}

	public long getSpawnTime(){
		return spawnTime;
	}

	public void setSpawnTime(long spawnTime){
		this.spawnTime = spawnTime;
	}

	public long getMeowTime(){
		return meowTime;
	}

	public void setMeowTime(long meowTime){
		this.meowTime = meowTime;
	}

	public long getFireworkTime(){
		return fireworkTime;
	}

	public void setFireworkTime(long fireworkTime){
		this.fireworkTime = fireworkTime;
	}

	public long getBlockTime(){
		return blockTime;
	}

	public void setBlockTime(long blockTime){
		this.blockTime = blockTime;
	}

	public int getFireworks(){
		return fireworks;
	}

	public void setFireworks(int fireworks){
		this.fireworks = fireworks;
	}

	public void useWeapon(){
		if (gPlayer.getPlayer().getGameMode() == GameMode.ADVENTURE) {
			return;
		}

		if(weaponTime+(WEAPON_TIMEOUT*2) >= System.currentTimeMillis()){
			return;
		}

		weaponDamage += WEAPON_DAMAGE;
		weaponTime = System.currentTimeMillis();
		if(weaponDamage >= 100){
			weaponDamage = 120;
			gPlayer.getPlayer().setCooldown(Material.IRON_AXE, 40);
			gPlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
			gPlayer.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(0);
			if (gPlayer.getPlayer().getInventory().getItem(0) != null) {
				gPlayer.getPlayer().getInventory().getItem(0).setDurability((short)250);
			}
			gPlayer.getPlayer().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
			gPlayer.getPlayer().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_FIRE_EXTINGUISH,1f,1f);
		}
	}

	public boolean isWeaponActive(){
		return gPlayer.getPlayer().getGameMode() == GameMode.ADVENTURE;
	}

	public int getEntityId(){
		return block != null ? block.getEntityId() : 0;
	}

	public Block getOriginalBlock(){
		return origBlock;
	}

	public FallingBlock getBlock(){
		return this.block;
	}

	public boolean isSolid(){
		return this.solid;
	}

	public ItemStack getItemStack(){
		if(type == DisguiseType.BLOCK){
			Material material = this.getBlock().getMaterial();
			return new ItemStack(material);
		}
		else if(type == DisguiseType.ENTITY){
			/*if(DisguiseAPI.isDisguised(gPlayer.getPlayer())){
				switch(DisguiseAPI.getDisguise(gPlayer.getPlayer()).getType()){
					case COW: return new ItemStack(Material.MILK_BUCKET);
					case PIG: return new ItemStack(Material.PORKCHOP);
					case SHEEP: return new ItemStack(Material.WHITE_WOOL);
					case CHICKEN: return new ItemStack(Material.EGG);
					case RABBIT: return new ItemStack(Material.RABBIT_HIDE);
					default: return new ItemStack(Material.AIR);
				}
			}*/
		}
		return null;
	}

	public void disguiseRandomBlock(){
		this.disguiseBlock(game.getArena().getRandomBlock());
	}

	public void disguiseBlock(Block baseBlock){
		this.cancelDisguise();
		type = DisguiseType.BLOCK;

		this._spawnStand(gPlayer.getPlayer().getLocation().add(0, -0.7, 0));
		this._spawnBlock(gPlayer.getPlayer().getLocation(), baseBlock.getBlockData());

		for(Player player2 : Bukkit.getOnlinePlayers()){
			if(!player2.equals(gPlayer.getPlayer())){
				player2.hidePlayer(gPlayer.getPlayer());
			}
		}

		gPlayer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,2,false,false));
	}

	protected void _spawnStand(Location location) {
		stand = (ArmorStand) gPlayer.getPlayer().getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		stand.setInvulnerable(true);
		stand.setInvisible(true);
		stand.setGravity(false);
		stand.setPersistent(false);
		stand.setSmall(true);
	}

	protected void _spawnBlock(Location location, BlockData blockData) {
		block = gPlayer.getPlayer().getWorld().spawnFallingBlock(location, blockData);
		block.setGravity(false);
		block.setInvulnerable(true);
		block.setPersistent(false);
		block.setDropItem(false);

		stand.addPassenger(block);
	}

	public void respawnBlock(Location location){
		stand.eject();

		BlockData blockData = block.getBlockData();
		block.remove();

		this._spawnBlock(location, blockData);
	}

	/*public void disguiseEntity(Entity original){
		this.cancelDisguise();
		type = DisguiseType.ENTITY;
		MobDisguise disguise = new MobDisguise(me.libraryaddict.disguise.disguisetypes.DisguiseType.getType(original));
		disguise.setViewSelfDisguise(true);
		DisguiseAPI.disguiseToAll(gPlayer.getPlayer(),disguise);
		gPlayer.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
	}*/

	public void cancelDisguise(){
		this.setSolid(false);
		solidCountdown = 5;
		//DisguiseAPI.undisguiseToAll(gPlayer.getPlayer());
		for(Player player2 : Bukkit.getOnlinePlayers()){
			if(!player2.equals(gPlayer.getPlayer())){
				player2.showPlayer(gPlayer.getPlayer());
			}
		}
		if(block != null) block.remove();
		if(stand != null) stand.remove();
	}

	public void reset(){
		fireworkTime = System.currentTimeMillis()+(40*1000);
		fireworks = 5;
		solidCountdown = 5;
		solid = false;
		spawnTime = 0;
		weaponDamage = 0;
	}

	@SuppressWarnings("deprecation")
	public boolean setSolid(boolean solid){
		boolean success = false;
		if(solid){
			if(!this.isSolid()){
				Block block = gPlayer.getPlayer().getLocation().getBlock();
				if(block.getType() == Material.AIR || block.getType() == Material.WATER){
					if(solid == true){
						origBlock = block;
						for(Player player2 : Bukkit.getOnlinePlayers()){
							if(!player2.equals(gPlayer.getPlayer())){
								player2.sendBlockChange(gPlayer.getPlayer().getLocation().getBlock().getLocation(),this.getBlock().getBlockData());
								PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(this.getEntityId());
								((CraftPlayer)player2).getHandle().b.a(packet);
							}
						}

						Particles.BLOCK_CRACK.display(Bukkit.createBlockData(this.getBlock().getMaterial()),0.3f,0.3f,0.3f,0.0f,64,origBlock.getLocation().add(0.5,0.7,0.5),64);
						gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_STONE_PLACE,1,1);
						this.solid = true;
						success = true;
					}
				}
			}
		} else {
			if(this.isSolid()){
				if(origBlock != null){
					Location location = origBlock.getLocation();
					BlockData data = origBlock.getBlockData();
					origBlock = null;
					for(Player player2 : Bukkit.getOnlinePlayers()){
						player2.sendBlockChange(location,data);
					}
					this.respawnBlock(location);
					gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_BAT_HURT,1f,1f);
				}
				this.solid = false;
				success = true;
			}
			this.solidCountdown = 5;
		}
		return success;
	}

	public void run(){
		if(this.type == DisguiseType.BLOCK){
			Location loc = gPlayer.getPlayer().getLocation();
			if(this.isSolid()){
				loc.setX(origBlock.getX()+0.5);
				loc.setY(origBlock.getY()-0.74);
				loc.setZ(origBlock.getZ()+0.5);
			} else {
				loc.add(0.0,-0.70,0.0);
			}

			if (stand != null && stand.isDead()) {
				this._spawnStand(loc);
			}

			if (block != null && block.isDead()) {
				this._spawnBlock(loc, block.getBlockData());
			}

			stand.setTicksLived(1);
			block.setTicksLived(1);

			((CraftArmorStand)stand).getHandle().c(loc.getX(), loc.getY(), loc.getZ());
		}
	}

	public void runTracker(){
		ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
		if(itemStack.getType() == Material.RECOVERY_COMPASS){
			double distance = this.getDistanceOfNearestHider(gPlayer);
			int tmpTracker = 10;
			if(distance < 4.0) tmpTracker = 2;
			else if(distance < 5.0) tmpTracker = 3;
			else if(distance < 6.0) tmpTracker = 4;
			else if(distance < 7.0) tmpTracker = 5;
			else if(distance < 8.0) tmpTracker = 6;
			else if(distance < 9.0) tmpTracker = 7;
			else if(distance < 10.0) tmpTracker = 8;
			else if(distance < 11.0) tmpTracker = 9;
			else tmpTracker = 10;
			if(tracker+1 > tmpTracker){
				tracker = 1;
				gPlayer.getPlayer().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.BLOCK_NOTE_BLOCK_HAT,1,1);
			} else {
				tracker += 1;
			}
		}
	}

	public void runWeaponDamage(){
		if(weaponTime+WEAPON_TIMEOUT < System.currentTimeMillis()){
			if (weaponDamage > 0) {
				weaponDamage -= 1;
				if (weaponDamage <= 100) {
					if (gPlayer.getPlayer().getGameMode() == GameMode.ADVENTURE) {
						gPlayer.getPlayer().setGameMode(GameMode.SURVIVAL);
						gPlayer.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(100);
						if (gPlayer.getPlayer().getInventory().getItem(0) != null) {
							gPlayer.getPlayer().getInventory().getItem(0).setDurability((short)0);
						}
					}
				}
			}
		}
		if(gPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) gPlayer.getPlayer().getInventory().setHeldItemSlot(0);
	}

	public void updateWeaponDamage(){
		gPlayer.getPlayer().setExp((Math.min(weaponDamage, 100)) * 0.01f);
	}

	private double getDistanceOfNearestHider(GamePlayer gPlayer){
		double distance = Double.MAX_VALUE;
		GamePlayer nearestPlayer = null;
		for(GamePlayer player2 : game.getTeams().getTeam(HidenSeekTeamType.HIDERS).getPlayers()){
			if(gPlayer.getPlayer().getWorld().equals(player2.getPlayer().getWorld())){
				double tmpDistance = player2.getPlayer().getLocation().distanceSquared(gPlayer.getPlayer().getLocation());
				if(tmpDistance < distance){
					distance = tmpDistance;
					nearestPlayer = player2;
				}
			}
		}
		return (nearestPlayer != null ? nearestPlayer.getPlayer().getLocation().distance(gPlayer.getPlayer().getLocation()) : Double.MAX_VALUE);
	}

	public void runDisguiseCountdown(){
		if(type == DisguiseType.BLOCK && !this.isSolid()){
			ItemStack itemStack = this.getItemStack();
			ItemMeta meta = itemStack.getItemMeta();
			if(meta != null){
				if(solidCountdown == 0) meta.addEnchant(Glow.getGlow(),10,true);
				itemStack.setItemMeta(meta);
			}
			gPlayer.getPlayer().getInventory().setItem(8,itemStack);
			Block block = gPlayer.getPlayer().getLocation().getBlock();
			gPlayer.getPlayer().setExp(1-(solidCountdown/5.0f));
			if((block.getType() == Material.AIR || block.getType() == Material.WATER) && block.getRelative(BlockFace.DOWN).getType() != Material.AIR){
				if(solidCountdown == 0) this.setSolid(true);
				else solidCountdown --;
			}
			else solidCountdown = 5;
		}
		else if(type == DisguiseType.ENTITY){
			gPlayer.getPlayer().getInventory().setItem(8,this.getItemStack());
		}
	}

	public enum DisguiseType {
		BLOCK, ENTITY
	}
}