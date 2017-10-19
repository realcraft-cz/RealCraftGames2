package com.paintball;

public class PaintballPlayer {

	private int pistols = 64;

	public PaintballPlayer(){
	}

	public int getPistols(){
		return pistols;
	}

	public void addPistols(int pistols){
		this.pistols += pistols;
		if(this.pistols > 64) this.pistols = 64;
	}
}