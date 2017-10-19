package com.paintball;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.games.player.GamePlayer;

public enum PaintballTeamType {
	RED, BLUE;

	public String toString(){
		return this.name().toLowerCase();
	}

	public String toName(){
		switch(this){
			case RED: return "Red";
			case BLUE: return "Blue";
		}
		return null;
	}

	public ChatColor getChatColor(){
		switch(this){
			case RED: return ChatColor.RED;
			case BLUE: return ChatColor.BLUE;
		}
		return ChatColor.WHITE;
	}

	public Color getColor(){
		switch(this){
			case RED: return Color.RED;
			case BLUE: return Color.BLUE;
		}
		return Color.WHITE;
	}

	public void setPlayerInventory(GamePlayer gPlayer){
		ItemStack itemStack;
		LeatherArmorMeta meta;

		itemStack = new ItemStack(Material.LEATHER_HELMET,1);
		meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setHelmet(itemStack);

		itemStack = new ItemStack(Material.LEATHER_CHESTPLATE,1);
		meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setChestplate(itemStack);

        itemStack = new ItemStack(Material.LEATHER_LEGGINGS,1);
        meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setLeggings(itemStack);

        itemStack = new ItemStack(Material.LEATHER_BOOTS,1);
        meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(this.getColor());
        itemStack.setItemMeta(meta);
        gPlayer.getPlayer().getInventory().setBoots(itemStack);
	}
}