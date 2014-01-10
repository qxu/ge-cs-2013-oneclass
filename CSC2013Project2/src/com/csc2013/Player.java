package com.csc2013;

import java.awt.Point;
import org.newdawn.slick.Animation;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.Action;

public class Player {

	private Animation player;
	private Point playerLocation;
	private int moveSize = 1;
	private MapBox currentMoves;
	private int keys = 0;
	private Map playerMap;

	public Player(Map map) throws SlickException {

		// Load character graphics, run the sprite animations, and start the
		// player
		SpriteSheet sheet = new SpriteSheet("res/flashy.png", 16, 16);
		player = new Animation();
		player.setAutoUpdate(true);

		playerLocation = new Point(20, 14);

		currentMoves = map.getMapBox(playerLocation);

		playerMap = map;

		for (int frame = 0; frame < 3; frame++) {
			player.addFrame(sheet.getSprite(frame, 0), 150);
		}

	}

	//
	public void setMapBox() {
		currentMoves = playerMap.getMapBox(playerLocation);
	}

	public Point getPlayerGridLocation() {
		return playerLocation;
	}

	// Return player x,y coordinates
	public Point getPlayerLocation() {
		Point pixelLoc = new Point((int) (playerLocation.getX() * 16),
				(int) (playerLocation.getY() * 16));
		return pixelLoc;
	}

	// Return player animation
	public Animation getPlayerAnimation() {
		return player;
	}

	// Move method, checks for validity then moves if valid
	// I made 3 move methods because the actual setting part has to be private
	public boolean move(Action action) {
		boolean moved = moveCheck(action);
		if (moved) {
			setPlayerLocation(action);
			/*
			 * System.out.print(playerLocation.getX()); System.out.print("-");
			 * System.out.print(playerLocation.getY()); System.out.print("\n");
			 */
			setMapBox();
		}
		return moved;
	}

	// Check the BoxType player is trying to move to, if Open:true Door:(key
	// logic) else:false
	// Need to get the Map and etc when Brian is done
	private boolean moveCheck(Action action) {
		// System.out.print(currentMoves.North);
		// System.out.print("-\n");
		switch (action) {
		case North:
			if (currentMoves.North == BoxType.Open
					|| currentMoves.North == BoxType.Key
					|| currentMoves.North == BoxType.Exit) {
				return true;
			}
			break;
		case South:
			if (currentMoves.South == BoxType.Open
					|| currentMoves.South == BoxType.Key
					|| currentMoves.South == BoxType.Exit) {
				return true;
			}
			break;
		case East:
			if (currentMoves.East == BoxType.Open
					|| currentMoves.East == BoxType.Key
					|| currentMoves.East == BoxType.Exit) {
				return true;
			}
			break;
		case West:
			if (currentMoves.West == BoxType.Open
					|| currentMoves.West == BoxType.Key
					|| currentMoves.West == BoxType.Exit) {
				return true;
			}
			break;
		case Pickup:
			if (currentMoves.hasKey()) {
				keys++;
				playerMap.pickup(playerLocation);
				// System.out.print("key pickup\n");
				return true;
			}
			break;
		case Use:
			if (keys <= 0) {
				// System.out.print("no keys\n");
				return false;
			} else if (playerMap.unlockDoor(playerLocation)) {
				keys--;
				return true;
			}
			break;
		default:
			return false;
		}
		return false;

	}

	public MapBox getCurrentMove() {
		return playerMap.getMapBox(playerLocation);
	}

	private void setPlayerLocation(Action action) {

		switch (action) {
		case North:
			playerLocation.y -= moveSize;
			break;
		case South:
			playerLocation.y += moveSize;
			break;
		case East:
			playerLocation.x += moveSize;
			break;
		case West:
			playerLocation.x -= moveSize;
			break;
		default:
			break;
		}

	}

	public int getKeys() {
		return keys;
	}

	public boolean end() {
		if (currentMoves.isEnd()) {
			return true;
		}
		return false;
	}
}
