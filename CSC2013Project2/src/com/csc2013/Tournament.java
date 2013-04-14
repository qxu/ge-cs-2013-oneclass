package com.csc2013;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import com.csc2013.DungeonMaze.PlayerType;

/**
 * 
 * This class starts a tournament consisting of Dungeon Maze games
 * 
 * @author GE
 *
 */
public class Tournament {
	/*
	 * This variable controls how many steps a player can take before starting the next game
	 */
	public final static int maxSteps = 400;
	/*
	 * Possible maps to use are:
	 * map01.tmx
	 * map02.tmx
	 * map03.tmx
	 */
	public final static String maps[] = {"test13.tmx"};
	/*
	 * This is how many spaces ahead players can see.  Acceptable values 
	 * are 1 through 15.	
	 */
	public final static int vision[] = {1};
	/*
	 * Possible players are:
	 * PlayerType.AI
	 * PlayerType.School
	 * PlayerType.Human
	 */
	public final static PlayerType players[] = {PlayerType.School};
	
	public static void main(String[] argv) {
		DungeonMaze dm = new DungeonMaze();
		AppGameContainer container;
		try {
			container = new AppGameContainer(dm, 640, 480, false);
			container.setVerbose(false); // suppress Slick2Ds output
			container.start();
		} catch (SlickException e) {
			System.out.println("There is an internal issue with the game framework.  Please send the following error code to CSC.CaseStudy@ge.com.");
			e.printStackTrace();
		}
	}
}