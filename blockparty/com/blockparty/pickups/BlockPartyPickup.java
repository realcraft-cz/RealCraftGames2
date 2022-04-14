package com.blockparty.pickups;

import com.blockparty.BlockParty;
import com.games.player.GamePlayer;
import com.games.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import realcraft.bukkit.utils.Particles;

public abstract class BlockPartyPickup implements Runnable {

	private BlockPartyPickupType type;
	private BlockParty game;
	private Location location;

	public BlockPartyPickup(BlockPartyPickupType type,BlockParty game){
		this.type = type;
		this.game = game;
	}

	public BlockPartyPickupType getType(){
		return type;
	}

	public BlockParty getGame(){
		return game;
	}

	public Location getLocation(){
		return location;
	}

	public void place(){
		location = game.getRandomPickupLocation();
		location.getBlock().setType(Material.BEACON);
		location.getWorld().playSound(location,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f);
	}

	public void remove(){
		if(location.getBlock().getType() == Material.BEACON) Particles.SPELL_WITCH.display(0.2f,0.2f,0.2f,0.5f,16,location.clone().add(0.5f,0.5f,0.5f),64);
		location.getBlock().setType(Material.AIR);
	}

	public abstract void activate(GamePlayer gPlayer);
	public abstract void clear();

	public static enum BlockPartyPickupType {

		BLINDNESS 			(BlockPartyPickupBlindness.class, 			"§faktivoval §7§lslepotu"),
		COLORBLINDNESS 		(BlockPartyPickupColorBlindness.class, 		"§faktivoval §a§lb§c§la§3§lr§e§lv§d§lo§7§lslepost"),
		SNOWBALLS 			(BlockPartyPickupSnowballs.class, 			"§frozhazel §b§lsnehove koule§f, kryjte se!"),
		SHOVELS 			(BlockPartyPickupShovels.class, 			"§frozhazel §e§llopaty§f, podkopej ostatni!"),
		SILVERFISH 			(BlockPartyPickupSilverfish.class, 			"§fvypustil §3§lsilverfishe§f, utikejte!"),
		BABYZOMBIE 			(BlockPartyPickupBabyzombie.class, 			"§fvypustil §2§lmale zombiky§f, brante se!"),
		ACID 				(BlockPartyPickupAcid.class, 				"§frozlil §6§lkyselinu§f, dejte pozor!"),
		THUNDERSTORM 		(BlockPartyPickupThunderstorm.class, 		"§fseslal §9§lbourku§f, dejte pozor!"),
		PUMPKIN 			(BlockPartyPickupPumpkin.class, 			"§fnasadil vsem na hlavu §6§ldyni§f"),
		CONFUSION 			(BlockPartyPickupConfusion.class, 			"§faktivoval §d§lnevolnost§f"),
		LEVITATION 			(BlockPartyPickupLevitation.class, 			"§faktivoval §f§llevitaci§f"),
		BEES	 			(BlockPartyPickupBees.class, 				"§fvypustil §e§lvcely§f, pozor na ne!"),
		SKELETONS 			(BlockPartyPickupSkeletons.class, 			"§fvypustil §7§lskeletony§f, pozor na sipy!"),
		;

		private final Class<?> clazz;
		private final String message;

		private BlockPartyPickupType(Class<?> clazz, String message) {
			this.clazz = clazz;
			this.message = message;
		}

		public Class<?> getClazz(){
			return clazz;
		}

		public String getMessage(){
			return message;
		}

		public static BlockPartyPickupType getRandomType(){
			//return BlockPartyPickupType.PUMPKIN;
			if (true) {
				return BlockPartyPickupType.SKELETONS;
			}
			return BlockPartyPickupType.values()[RandomUtil.getRandomInteger(0,BlockPartyPickupType.values().length-1)];
		}
	}
}