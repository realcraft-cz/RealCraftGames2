package com.games.player;

import java.util.HashMap;

public class GamePlayerSettings {

	private HashMap<String,Boolean> storageBoolean = new HashMap<String,Boolean>();
	private HashMap<String,Integer> storageInteger = new HashMap<String,Integer>();
	private HashMap<String,Long> storageLong = new HashMap<String,Long>();
	private HashMap<String,Double> storageDouble = new HashMap<String,Double>();
	private HashMap<String,String> storageString = new HashMap<String,String>();
	private HashMap<String,Enum<?>> storageEnum = new HashMap<String,Enum<?>>();

	public boolean getBoolean(String path){
		return (storageBoolean.get(path) == null ? false : storageBoolean.get(path));
	}

	public void setBoolean(String path,boolean result){
		storageBoolean.put(path,result);
	}

	public int getInt(String path){
		return (storageInteger.get(path) == null ? 0 : storageInteger.get(path));
	}

	public void setInt(String path,int result){
		storageInteger.put(path,result);
	}

	public void addInt(String path,int result){
		storageInteger.put(path,(storageInteger.get(path) == null ? 0 : storageInteger.get(path))+result);
	}

	public long getLong(String path){
		return storageLong.get(path);
	}

	public void setLong(String path,long result){
		storageLong.put(path,result);
	}

	public double getDouble(String path){
		return storageDouble.get(path);
	}

	public void setDouble(String path,double result){
		storageDouble.put(path,result);
	}

	public String getString(String path){
		return storageString.get(path);
	}

	public void setString(String path,String result){
		storageString.put(path,result);
	}

	public Enum<?> getEnum(String path){
		return storageEnum.get(path);
	}

	public void setEnum(String path,Enum<?> result){
		storageEnum.put(path,result);
	}
}