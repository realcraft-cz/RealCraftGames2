package com.races.arenas;

import com.races.RaceType;
import com.races.Races;

public class RaceHorseArena extends RaceArena {

	public RaceHorseArena(Races game,String name){
		super(game,name,RaceType.HORSE);
	}
}