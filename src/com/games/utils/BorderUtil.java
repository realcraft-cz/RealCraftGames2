package com.games.utils;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_14_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_14_R1.PacketPlayOutWorldBorder.EnumWorldBorderAction;
import net.minecraft.server.v1_14_R1.WorldBorder;

public class BorderUtil {

	public static void setBorder(Player player,Location center,double size){
		WorldBorder border = new WorldBorder();
		border.setCenter(center.getX(),center.getZ());
		border.setSize(size);
		border.setWarningDistance(0);
		border.world = ((CraftWorld)center.getWorld()).getHandle();
		PacketPlayOutWorldBorder packet;
		packet = new PacketPlayOutWorldBorder(border,EnumWorldBorderAction.SET_CENTER);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
		packet = new PacketPlayOutWorldBorder(border,EnumWorldBorderAction.SET_SIZE);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
		packet = new PacketPlayOutWorldBorder(border,EnumWorldBorderAction.INITIALIZE);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}
}