package com.bedwars.specials;

import com.bedwars.BedWars;
import com.bedwars.BedWarsTeam;

public abstract class BedWarsSpecialTeam extends BedWarsSpecial {

	private BedWarsTeam team;

	public BedWarsSpecialTeam(BedWarsSpecialType type,BedWars game,BedWarsTeam team){
		super(type,game);
		this.team = team;
	}

	public BedWarsTeam getTeam(){
		return team;
	}
}