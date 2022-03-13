package com.hidenseek;

import com.games.player.GamePlayer;
import com.games.utils.Glow;
import com.hidenseek.HidenSeekTeam.HidenSeekTeamType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftFallingBlock;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
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
	private boolean weaponActive = true;
	private long weaponTime = 0;

	private int entityId;
	private boolean solid = false;
	private DisguiseType type;
	private HidenSeekArmorStand stand;
	private HidenSeekFallingBlock block;
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
		if(weaponActive){
			weaponDamage += WEAPON_DAMAGE;
			weaponTime = System.currentTimeMillis();
			if(weaponDamage >= 100){
				weaponDamage = 100;
				weaponActive = false;
				gPlayer.getPlayer().getInventory().remove(Material.IRON_AXE);
				gPlayer.getPlayer().getWorld().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_BREAK,1f,1f);
			}
		}
	}

	public boolean isWeaponActive(){
		return weaponActive;
	}

	public int getEntityId(){
		return entityId;
	}

	public Block getOriginalBlock(){
		return origBlock;
	}

	public CraftFallingBlock getBlock(){
		return ((CraftFallingBlock) this.block.getBukkitEntity());
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

		stand = new HidenSeekArmorStand(((CraftWorld)gPlayer.getPlayer().getWorld()).getHandle(),gPlayer.getPlayer().getLocation().getX(),gPlayer.getPlayer().getLocation().getY()-0.70,gPlayer.getPlayer().getLocation().getZ());
		stand.noclip = true;
		stand.setNoGravity(true);
		stand.setInvisible(true);
		stand.setSmall(true);
		((CraftWorld)gPlayer.getPlayer().getWorld()).getHandle().addEntity(stand,SpawnReason.CUSTOM);
		IBlockData ibd = ((CraftBlock)baseBlock).getNMS();
		block = new HidenSeekFallingBlock(((CraftWorld)gPlayer.getPlayer().getWorld()).getHandle(),gPlayer.getPlayer().getLocation().getX(),gPlayer.getPlayer().getLocation().getY(),gPlayer.getPlayer().getLocation().getZ(),ibd);
		block.dropItem = false;
		((CraftWorld)gPlayer.getPlayer().getWorld()).getHandle().addEntity(block,SpawnReason.CUSTOM);
		this.entityId = block.getId();
		stand.getBukkitEntity().setPassenger(block.getBukkitEntity());

		for(Player player2 : Bukkit.getOnlinePlayers()){
			if(!player2.equals(gPlayer.getPlayer())){
				player2.hidePlayer(gPlayer.getPlayer());
			}
		}
		gPlayer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,2,false,false));
	}

	public void respawnBlock(Location location){
		stand.getBukkitEntity().eject();
		IBlockData ibd = block.getBlock();
		if(block != null) block.remove();
		block = new HidenSeekFallingBlock(((CraftWorld)gPlayer.getPlayer().getWorld()).getHandle(),location.getX(),location.getY(),location.getZ(),ibd);
		block.dropItem = false;
		((CraftWorld)gPlayer.getPlayer().getWorld()).getHandle().addEntity(block,SpawnReason.CUSTOM);
		this.entityId = block.getId();
		stand.getBukkitEntity().setPassenger(block.getBukkitEntity());
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
		if(stand != null) stand.getBukkitEntity().remove();
	}

	public void reset(){
		fireworkTime = System.currentTimeMillis()+(40*1000);
		fireworks = 5;
		solidCountdown = 5;
		solid = false;
		spawnTime = 0;
		weaponDamage = 0;
		weaponActive = true;
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
					for(Player player2 : Bukkit.getOnlinePlayers()){
						player2.sendBlockChange(origBlock.getLocation(),origBlock.getBlockData());
					}
					this.respawnBlock(origBlock.getLocation());
					origBlock = null;
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
			}
			else loc.add(0.0,-0.70,0.0);
			stand.setPosition(loc.getX(),loc.getY(),loc.getZ());
		}
	}

	public void runTracker(){
		ItemStack itemStack = gPlayer.getPlayer().getInventory().getItemInMainHand();
		if(itemStack.getType() == Material.COMPASS){
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
			weaponDamage -= 1;
			if(weaponDamage <= 0){
				weaponDamage = 0;
				if(!weaponActive){
					weaponActive = true;
					gPlayer.getPlayer().getInventory().setItem(0,new ItemStack(Material.IRON_AXE));
					gPlayer.getPlayer().playSound(gPlayer.getPlayer().getLocation(),Sound.ENTITY_ITEM_PICKUP,1,1);
				}
			}
		}
		if(gPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) gPlayer.getPlayer().getInventory().setHeldItemSlot(0);
	}

	public void updateWeaponDamage(){
		gPlayer.getPlayer().setExp(weaponDamage*0.01f);
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

	public class HidenSeekArmorStand extends EntityArmorStand {
		public HidenSeekArmorStand(World world, double d0, double d1, double d2){
			super(world, d0, d1, d2);
		}

		@Override
		public NBTTagCompound f(NBTTagCompound nbttagcompound){
			return null;
		}

		@Override
		public boolean d_() {
			return super.d_();
		}
	}

	public class HidenSeekFallingBlock extends EntityFallingBlock {
		private boolean dead = false;

		public HidenSeekFallingBlock(World world, double d0, double d1, double d2, IBlockData iblockdata) {
			super(world, d0, d1, d2, iblockdata);
		}

		public void remove(){
			this.dead = true;
			super.getBukkitEntity().remove();
		}

		@Override
		public void die(){
			if(this.dead) super.die();
		}

		@Override
		public NBTTagCompound f(NBTTagCompound nbttagcompound){
			return null;
		}
	}
}