package com.blockparty;

import org.bukkit.Material;

public class BlockPartyBlock {

	private Material type;
	private byte data;

	public BlockPartyBlock(Material type,byte data){
		this.type = type;
		this.data = data;
	}

	public Material getType(){
		return type;
	}

	public byte getData(){
		return data;
	}
}