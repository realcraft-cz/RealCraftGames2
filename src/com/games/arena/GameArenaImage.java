package com.games.arena;

import com.games.Games;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GameArenaImage {

	private static final int WIDTH = 128;
	private static final int HEIGHT = 128;

	private GameArena arena;
	private ItemStack[] images;

	public GameArenaImage(GameArena arena){
		this.arena = arena;
		this.getImages();
	}

	public GameArena getArena(){
		return arena;
	}

	public ItemStack[] getImages(){
		if(images == null){
			World world = Bukkit.getWorld("world");
			File file = new File(Games.getInstance().getDataFolder()+"/"+arena.getGame().getType().getName()+"/"+arena.getName()+"/"+"image.png");
			if(file.exists()){
				BufferedImage origImage = this.loadImage(file);
				if(origImage != null){
					int width = origImage.getWidth();
					int height = origImage.getHeight();

					int columns = (int) Math.ceil(width/WIDTH);
					int rows = (int) Math.ceil(height/HEIGHT);

					int remindX = width % WIDTH;
					int remindY = height % HEIGHT;

					if(remindX > 0) columns ++;
					if(remindY > 0) rows ++;

					BufferedImage[] cutImages = new BufferedImage[columns*rows];
					int imageX;
					int imageY = (remindY == 0 ? 0 : (remindY-HEIGHT)/2);
					for(int i=0;i<rows;i++){
						imageX = (remindX == 0 ? 0 : (remindX-WIDTH)/2);
						for(int a=0;a<columns;a++){
							cutImages[i*columns+a] = this.cutImage(origImage,imageX,imageY);
							imageX += WIDTH;
						}
						imageY += HEIGHT;
					}

					ItemStack[] items = new ItemStack[columns*rows];
					int index = 0;
					for(BufferedImage image : cutImages){
						MapView map = Bukkit.getServer().createMap(world);
						map.getRenderers().clear();
						map.addRenderer(new CustomMapRenderer(image));
						ItemStack item = new ItemStack(Material.FILLED_MAP);
						MapMeta meta = (MapMeta)item.getItemMeta();
						meta.setMapId(map.getId());
						item.setItemMeta(meta);
						items[index++] = item;
					}
					images = items;
				}
			}
		}
		return images;
	}

	private BufferedImage loadImage(File file){
		try {
			return ImageIO.read(file);
		} catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}

	private BufferedImage cutImage(BufferedImage image,int x,int y){
		BufferedImage newImage = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = newImage.getGraphics();
		graphics.drawImage(image,-x,-y,null);
		graphics.dispose();
		return newImage;
	}

	private class CustomMapRenderer extends MapRenderer {

		private BufferedImage image;

		public CustomMapRenderer(BufferedImage image){
			this.image = image;
		}

		@Override
		public void render(MapView map, MapCanvas canvas, Player player){
			if(image == null) return;
			canvas.drawImage(0,0,image);
			image = null;
		}
	}
}