package com.races;

public enum RaceType {
	RUN, HORSE, BOAT;

	@Override
	public String toString(){
		return this.name().toLowerCase();
	}

	public int getMaxStartInactivity(){
		switch(this){
			case RUN: return 9999;
			case HORSE: return 20;
			case BOAT: return 3;
		}
		return 0;
	}

	public int getMaxGameInactivity(){
		switch(this){
			case RUN: return 9999;
			case HORSE: return 9999;
			case BOAT: return 20;
		}
		return 0;
	}

	public static RaceType fromName(String name){
		return RaceType.valueOf(name.toUpperCase());
	}
}