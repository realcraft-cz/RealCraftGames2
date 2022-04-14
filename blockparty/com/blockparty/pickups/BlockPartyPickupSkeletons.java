package com.blockparty.pickups;

import com.blockparty.BlockParty;
import com.games.player.GamePlayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Stray;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import realcraft.bukkit.utils.Particles;

import java.util.ArrayList;
import java.util.Random;

public class BlockPartyPickupSkeletons extends BlockPartyPickup {

	private ArrayList<Entity> entities = new ArrayList<Entity>();

	public BlockPartyPickupSkeletons(BlockParty game){
		super(BlockPartyPickupType.SKELETONS,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		this.getLocation().getWorld().playSound(this.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 2f);
		this.getLocation().getWorld().playSound(this.getLocation(), Sound.ENTITY_STRAY_AMBIENT, 1f, 1f);
		Particles.EXPLOSION_LARGE.display(0,0,0,0,1,this.getLocation(),128);
		Particles.FIREWORKS_SPARK.display(0.3f,0.3f,0.3f,0.2f,32,this.getLocation().clone().add(0.5,0.5,0.5),64);
		Random random = new Random();
		for(GamePlayer gPlayer2 : this.getGame().getGamePlayers()){
			//if(gPlayer2 == gPlayer) continue;
			Stray entity = (Stray) this.getGame().getArena().getWorld().spawnEntity(this.getLocation(), EntityType.STRAY);
			entity.setVelocity(new Vector(random.nextDouble()-0.5,random.nextDouble()/4,random.nextDouble()-0.5).multiply(1.0).add(new Vector(0,0.3,0)));
			entity.setNoDamageTicks(40);
			entity.getEquipment().setItemInMainHand(new ItemStack(Material.BOW));
			entity.setTarget(gPlayer2.getPlayer());
			entities.add(entity);
		}
	}

	@Override
	public void clear(){
		for(Entity entity : entities) entity.remove();
		entities.clear();
	}

	@Override
	public void run(){
	}
}