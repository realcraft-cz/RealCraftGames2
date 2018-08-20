package com.blockparty;

import org.bukkit.Material;

public class BlockPartyBlock {

	private Material type;

	public BlockPartyBlock(Material type){
		this.type = type;
	}

	public Material getType(){
		return type;
	}
}