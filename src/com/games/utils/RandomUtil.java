package com.games.utils;

import java.util.Random;

public class RandomUtil {
	private static Random random = new Random();

	public static boolean getRandomBoolean(){
		return random.nextBoolean();
	}

	public static double getRandomDouble(double min,double max){
		return min+Math.random()*(max-min);
	}

	public static int getRandomInteger(int min,int max){
		return random.nextInt((max - min) + 1) + min;
	}
}