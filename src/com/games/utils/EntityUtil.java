package com.games.utils;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.PathfinderGoalSelector;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.lang.reflect.Field;

public class EntityUtil {

	public static void clearPathfinders(Entity entity){
		net.minecraft.server.v1_13_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
		try {
			Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
			bField.setAccessible(true);
			Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
			cField.setAccessible(true);
			((EntityInsentient) ((CraftEntity)entity).getHandle()).getNavigation().a(0);
			bField.set(((EntityInsentient) nmsEntity).goalSelector,Sets.newLinkedHashSet());
			bField.set(((EntityInsentient) nmsEntity).targetSelector,Sets.newLinkedHashSet());
			cField.set(((EntityInsentient) nmsEntity).goalSelector,Sets.newLinkedHashSet());
			cField.set(((EntityInsentient) nmsEntity).targetSelector,Sets.newLinkedHashSet());
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}