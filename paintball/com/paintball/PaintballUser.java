package com.paintball;

public class PaintballUser {

	private int pistols = 64;
	private int grenades = 0;
	private long lastJump = 0;

	public PaintballUser(){
	}

	public int getPistols(){
		return pistols;
	}

	public void addPistols(int pistols){
		this.pistols += pistols;
		if(this.pistols > 64) this.pistols = 64;
	}

	public void resetPistols(){
		pistols = 64;
	}

	public int getGrenades(){
		return grenades;
	}

	public void addGrenades(int grenades){
		this.grenades += grenades;
		if(this.grenades > 16) this.grenades = 16;
	}

	public long getLastJump(){
		return lastJump;
	}

	public void updateLastJump(){
		lastJump = System.currentTimeMillis();
	}

	public void reset(){
		pistols = 64;
		grenades = 0;
		lastJump = 0;
	}
}