package com.csc2013;

import org.newdawn.slick.SlickException;

import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Action;
import com.csc2013.DungeonMaze.MoveType;

public class PlayerController {
	private Action nextMove;
	private Action lastDirection;
	private String name = "Sample AI";

	public PlayerController() throws SlickException {
		lastDirection = Action.East;
		nextMove = Action.East;
        System.out.println("delte this");
	}
	
	public String getName() {
		return name;
	}

	/* 
	 * vision is a class of type PlayerVision that contains information about all the directions you can see
	 * 
	 * 
	 * vision.CurrentPoint is a class of type MapBox containing what lies in each direction as:
     *     MoveType (Open, Blocked)
	 *     BoxType (Open, Door, Blocked, Key, Exit)
	 *     
	 * To look in each direction past your current point use the following variables in conjunction with their repsective arrays
	 * 
	 * vision.North[vision.mNorth]
     * vision.South[vision.mSouth]
     * vision.East[vision.mEast]
     * vision.West[vision.mWest]
     * 
     * where vision.mDirection is how many open spaces are in that direction vision.Direction is the array of MapBox objects in that direction
	 *     
	 * lastAction is a boolean of the result of your last action i.e. if you tried to move and that direction was a wall, it would be false
	 * Action is what you tell your player to do (move in a direction, pickup a key, or use it to open a door) 
	 * 
	 * We have provided a very basic AI for moving around
	 * 
	 * To pickup a key, simply move your player onto the space and check if CurrentPoint.hasKey() is true, then return Action.Pickup
	 * 
	 * To open a door, simply move to a space next to it and return Action.Use and it will automatically open the door next to you
	 * Note: There will only ever be one door next to your character at a time
	 * 
	 * Lastly if you move onto the Exit space, the game will complete and the number of steps you took will be tracked
	 * 
	 * not a valid move - returns false (counts as a step)
	 * 
	 * pass in 2d array of boxes to mapbox
	 */
	public Action nextMove(final PlayerVision vision, final int keyCount, final boolean lastAction) {
	    if(!lastAction) { 
	        System.out.print(vision.CurrentPoint.North);
            System.out.print("-");
            System.out.print(vision.CurrentPoint.South);
            System.out.print("-");
            System.out.print(vision.CurrentPoint.East);
            System.out.print("-");
            System.out.print(vision.CurrentPoint.West);
            System.out.print("\n");
	    }

        if(vision.CurrentPoint.South == BoxType.Exit) {nextMove =  Action.South; return nextMove;}
        if(vision.CurrentPoint.North == BoxType.Exit) {nextMove =  Action.North; return nextMove;}
        if(vision.CurrentPoint.West == BoxType.Exit) {nextMove =  Action.West; return nextMove;}
        if(vision.CurrentPoint.East == BoxType.Exit) {nextMove =  Action.East; return nextMove;}
        
	    if(vision.CurrentPoint.hasKey()) { return Action.Pickup; }

        if(vision.CurrentPoint.South == BoxType.Key) {nextMove =  Action.South; return nextMove;}
        if(vision.CurrentPoint.North == BoxType.Key) {nextMove =  Action.North; return nextMove;}
        if(vision.CurrentPoint.West == BoxType.Key) {nextMove =  Action.West; return nextMove;}
        if(vision.CurrentPoint.East == BoxType.Key) {nextMove =  Action.East; return nextMove;}
        
        if(keyCount > 0) {
            if(vision.CurrentPoint.South == BoxType.Door || vision.CurrentPoint.North == BoxType.Door || vision.CurrentPoint.East == BoxType.Door || vision.CurrentPoint.West == BoxType.Door){ return Action.Use; } 
        }
        
        if(vision.CurrentPoint.SouthMove == MoveType.Open) {nextMove =  Action.South;}
        else if(vision.CurrentPoint.NorthMove == MoveType.Open) {nextMove =  Action.North;}
        else if(vision.CurrentPoint.WestMove == MoveType.Open) {nextMove =  Action.West;}
        else if(vision.CurrentPoint.EastMove == MoveType.Open) {nextMove =  Action.East;}

		if(lastDirection == Action.North) {
			if(vision.CurrentPoint.North == BoxType.Open){ nextMove = Action.North; } 
            else if (vision.CurrentPoint.West == BoxType.Open) { nextMove = Action.West; }
            else if (vision.CurrentPoint.East == BoxType.Open) { nextMove = Action.East; }
            else if (vision.CurrentPoint.South == BoxType.Open) { nextMove = Action.South; }
		}
		if(lastDirection == Action.South) {
			if(vision.CurrentPoint.South == BoxType.Open){ nextMove =  Action.South; } 
            else if (vision.CurrentPoint.West == BoxType.Open) { nextMove = Action.West; }
            else if (vision.CurrentPoint.East == BoxType.Open) { nextMove = Action.East; }
            else if (vision.CurrentPoint.North == BoxType.Open) { nextMove = Action.North; }
		}
		if(lastDirection == Action.East) {
			if(vision.CurrentPoint.East == BoxType.Open){ nextMove =  Action.East; } 
            else if (vision.CurrentPoint.North == BoxType.Open) { nextMove = Action.North; }
            else if (vision.CurrentPoint.South == BoxType.Open) { nextMove = Action.South; }
            else if (vision.CurrentPoint.West == BoxType.Open) { nextMove = Action.West; }
		}
		if(lastDirection == Action.West) {
			if(vision.CurrentPoint.West == BoxType.Open){ nextMove =  Action.West; }
            else if (vision.CurrentPoint.South == BoxType.Open) { nextMove = Action.South; }
			else if (vision.CurrentPoint.North == BoxType.Open) { nextMove = Action.North; }
			else if (vision.CurrentPoint.East == BoxType.Open) { nextMove = Action.East; }
		}

		lastDirection = nextMove;

		return nextMove;
	}
}