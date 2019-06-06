package com.games.arena;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class GameArenaImage {

	private static final int WIDTH = 128;
	private static final int HEIGHT = 128;

	private GameArena arena;

	private BufferedImage image;
	private ItemStack[] images;

	public GameArenaImage(GameArena arena){
		this.arena = arena;
	}

	public GameArena getArena(){
		return arena;
	}

	public void load(byte[] bytes){
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			image = ImageIO.read(bais);
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public void setDefaultImage(){
		image = new BufferedImage(3*WIDTH,2*HEIGHT,BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0,0,3*WIDTH,2*HEIGHT);
		graphics.setColor(Color.WHITE);
		graphics.setFont(new Font("Courier New",Font.PLAIN,30));
		graphics.drawString(arena.getName(),40,60);
		graphics.dispose();
	}

	public ItemStack[] getImages(){
		if(images == null){
			World world = Bukkit.getWorld("world");
			if(image != null){
				int width = image.getWidth();
				int height = image.getHeight();

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
						cutImages[i*columns+a] = this.cutImage(image,imageX,imageY);
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
		return images;
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