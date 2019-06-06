package com.games.arena;

public class GameArenaDimension {

	private int x,y,z;

	public GameArenaDimension(int x,int y,int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}

	public int getZ(){
		return z;
	}

	public static class GameArenaDimensionDefault extends GameArenaDimension {

		public GameArenaDimensionDefault(){
			super(256,256,256);
		}
	}
}