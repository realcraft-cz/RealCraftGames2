package com.paintball;

public class PaintballUser {

	private int pistols = 64;

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
		this.pistols = 64;
	}
}