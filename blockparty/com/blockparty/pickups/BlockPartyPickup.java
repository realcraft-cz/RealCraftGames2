package com.blockparty.pickups;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

import com.blockparty.BlockParty;
import com.games.Games;
import com.games.player.GamePlayer;
import com.games.utils.RandomUtil;

import realcraft.bukkit.utils.Particles;
import ru.beykerykt.lightapi.LightAPI;

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
		location = game.getArena().getRandomPickupLocation();
		location.getBlock().setType(Material.BEACON);
		location.getWorld().playSound(location,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f);
	}

	public void remove(){
		if(location.getBlock().getType() == Material.BEACON) Particles.SPELL_WITCH.display(0.2f,0.2f,0.2f,0.5f,16,location.clone().add(0.5f,0.5f,0.5f),64);
		location.getBlock().setType(Material.AIR);
		Bukkit.getScheduler().runTaskLater(Games.getInstance(),new Runnable(){
			@Override
			public void run(){
				LightAPI.updateChunk(location,Bukkit.getOnlinePlayers());
			}
		},5);
	}

	public abstract void activate(GamePlayer gPlayer);
	public abstract void clear();

	public enum BlockPartyPickupType {
		JUMP, BLINDNESS, COLORBLINDNESS, SNOWBALLS, SHOVELS, SILVERFISH, BABYZOMBIE, ACID, THUNDERSTORM;

		public String getMessage(){
			switch(this){
				case JUMP: return "§fziskal §d§lvysoke skoky";
				case BLINDNESS: return "§faktivoval §7§lslepotu";
				case COLORBLINDNESS: return "§faktivoval §a§lb§c§la§3§lr§e§lv§d§lo§7§lslepost";
				case SNOWBALLS: return "§frozhazel §b§lsnehove koule§f, kryjte se!";
				case SHOVELS: return "§frozhazel §e§llopaty§f, podkopej ostatni!";
				case SILVERFISH: return "§fvypustil §3§lsilverfishe§f, utikejte!";
				case BABYZOMBIE: return "§fvypustil §2§lmale zombiky§f, brante se!";
				case ACID: return "§frozlil §6§lkyselinu§f, dejte pozor!";
				case THUNDERSTORM: return "§fseslal §9§lbourku§f, dejte pozor!";
				default:break;
			}
			return null;
		}

		public static BlockPartyPickupType getRandomType(){
			return BlockPartyPickupType.values()[RandomUtil.getRandomInteger(0,BlockPartyPickupType.values().length-1)];
		}
	}
}