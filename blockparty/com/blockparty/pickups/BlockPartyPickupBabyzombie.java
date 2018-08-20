package com.blockparty.pickups;

import com.blockparty.BlockParty;
import com.games.player.GamePlayer;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.util.Vector;
import realcraft.bukkit.utils.Particles;

import java.util.ArrayList;
import java.util.Random;

public class BlockPartyPickupBabyzombie extends BlockPartyPickup {

	private ArrayList<Entity> entities = new ArrayList<Entity>();

	public BlockPartyPickupBabyzombie(BlockParty game){
		super(BlockPartyPickupType.BABYZOMBIE,game);
	}

	@Override
	public void activate(GamePlayer gPlayer){
		this.getLocation().getWorld().playSound(this.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,1f,2f);
		this.getLocation().getWorld().playSound(this.getLocation(),Sound.ENTITY_ZOMBIE_AMBIENT,1f,1f);
		Particles.EXPLOSION_LARGE.display(0,0,0,0,1,this.getLocation(),128);
		Particles.FIREWORKS_SPARK.display(0.3f,0.3f,0.3f,0.2f,32,this.getLocation().clone().add(0.5,0.5,0.5),64);
		Random random = new Random();
		for(GamePlayer gPlayer2 : this.getGame().getGamePlayers()){
			if(gPlayer2 == gPlayer) continue;
			Zombie entity = (Zombie)this.getGame().getArena().getWorld().spawnEntity(this.getLocation(),EntityType.ZOMBIE);
			entity.setVelocity(new Vector(random.nextDouble()-0.5,random.nextDouble()/4,random.nextDouble()-0.5).multiply(1.5).add(new Vector(0,0.5,0)));
			entity.setNoDamageTicks(40);
			entity.setBaby(true);
			entity.getEquipment().clear();
			entity.getEquipment().setItemInMainHandDropChance(0);
			entity.getEquipment().setItemInOffHandDropChance(0);
			entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
			entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(64);
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