package com.csc2013;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

/**
 * 
 * This class contains the logic to run a DungeonMaze game.
 * 
 * @author GE
 *
 */
public class DungeonMaze extends BasicGame {
    
	private Player player;
	private SchoolPlayer school;
	private AIPlayer ai;
	private Map map;
	@SuppressWarnings("unused")	private float gameTime = 0;  // This is not unused
	private boolean lastAction = true;
	private float playerMoveTime = 0;
	private final float moveTime = 50;
	private int steps = 0;
	private boolean gameRunning = true;
	private PlayerType curPlayer;
	private String mapName = "";
	private int mapTracker = 0;
	private int personTracker = 0;
	private int visionTracker = 0;
	private int score[][][] = new int[Tournament.vision.length][Tournament.players.length][Tournament.maps.length];
	
	public enum PlayerType {
	        School, Human, AI
	};
	
    public enum BoxType {
        Open, Blocked, Door, Exit, Key
    };
    
    public enum MoveType {
        Open, Blocked
    };
    
    public enum Direction {
        North, South, East, West
    };
    
    public enum Actionx {
        Move, Pickup, Use
    };
    
    public enum Action {
        North, South, East, West, Pickup, Use
    };


	public DungeonMaze() {
		super("Dungeon Maze");
	}
	
	public DungeonMaze(String mapName, PlayerType type) {
		super("Dungeon Maze");
		this.mapName = mapName;
		this.curPlayer = type;
	}
	
	// Build GameContainer, limit FPS to 60, instantiate the map and player
	public void init(GameContainer container) throws SlickException {
		// suppress Slick2Ds output
		container.setVerbose(false);
		
		//set the vision each time init() is called
		PlayerVision.distanceToView = Tournament.vision[visionTracker];

		// if all maps run through go back to first map				
		if (mapTracker == Tournament.maps.length) {
			personTracker++;
			mapTracker = 0;
		}
		this.mapName = Tournament.maps[mapTracker];
		// If all players are run through restart with the next set of vision
		if (personTracker == Tournament.players.length) { 
			visionTracker++;
			personTracker = 0;

			if (visionTracker < Tournament.vision.length) {
				PlayerVision.distanceToView = Tournament.vision[visionTracker];
			}
		}
        
		if (visionTracker == Tournament.vision.length) { //END GAME
			System.out.println();
			System.out.println("****** FINAL RESULTS ******");
			System.out.println("Maximum steps allowed: " + Tournament.maxSteps);
			for (int v = 0; v < Tournament.vision.length; v++) {
				System.out.println("--- Results for Vision Distance Set at " + Tournament.vision[v] + " ---");
				System.out.print("\t");
				for (int i = 0; i < Tournament.maps.length; i++) {
					System.out.print(Tournament.maps[i] + "\t");
				}
				System.out.print("\n");
				for (int r = 0; r < Tournament.players.length; r++) {
					System.out.print(Tournament.players[r] + "\t");
					for (int c = 0; c < Tournament.maps.length; c++) {
						System.out.print(score[v][r][c] + "\t\t");
					}
					System.out.print("\n");
				}
				System.out.print("\n");
			}
			container.setForceExit(false);
			container.exit();
		} else { // if (re)starting a game
			//set the current player
			this.curPlayer = Tournament.players[personTracker];
			
			// set up rendering options
			container.setVSync(true);
			container.setTargetFrameRate(60);
			container.setShowFPS(false);
			
			// set up map and player objects
			map = new Map();
			map.setMap(mapName);
			player = new Player(map);
			
			// Set players here
			if (curPlayer == PlayerType.AI) {
				ai = new AIPlayer();
			} else if (curPlayer == PlayerType.School) {
				school = new SchoolPlayer();
			}
			
			// output for each run what the tournament settings are
			System.out.println("-----");
			System.out.println("Starting a DungeonMaze run with the following settings:");
			System.out.println("Map: " + Tournament.maps[mapTracker]);
			System.out.println("Player: " + Tournament.players[personTracker]);
			System.out.println("Vision distance: " + Tournament.vision[visionTracker]);
		}
	}
	
	@SuppressWarnings("unused")
	public void update(GameContainer container, int delta) {
		gameTime += delta;
		if(playerMoveTime > 0) {
			playerMoveTime -= delta;
		}
        player.setMapBox();
        boolean show = true;
		if(playerMoveTime <= 0 && gameRunning) {
		    if(curPlayer == PlayerType.Human) {
                PlayerVision vision = new PlayerVision(map, player.getPlayerGridLocation());
    			if (container.getInput().isKeyDown(Input.KEY_LEFT)) {show = true; lastAction = player.move(Action.West); if (lastAction == true) {steps++;}}
    			else if (container.getInput().isKeyDown(Input.KEY_RIGHT)) {show = true; lastAction = player.move(Action.East);if (lastAction == true) {steps++;}}
    			else if (container.getInput().isKeyDown(Input.KEY_UP)) {show = true; lastAction = player.move(Action.North); if (lastAction == true) {steps++;}}
    			else if (container.getInput().isKeyDown(Input.KEY_DOWN)) {show = true; lastAction = player.move(Action.South); if (lastAction == true) {steps++;}}
                else if (container.getInput().isKeyDown(Input.KEY_SPACE)) {show = true; lastAction = player.move(Action.Pickup); if (lastAction == true) {steps++;} /*printPlayerLoc(vision);*/}
                else if (container.getInput().isKeyDown(Input.KEY_ENTER)) {show = true; lastAction = player.move(Action.Use); if (lastAction == true) {steps++;} /*printPlayerVision(vision);*/}
    			else if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)){System.exit(0);}
    			else if(container.getInput().isKeyPressed(Input.KEY_0)) { 
    	            show = false;
    	        }
		    } else if (curPlayer == PlayerType.AI) {
		        PlayerVision vision = new PlayerVision(map, player.getPlayerGridLocation());
		        lastAction = player.move(ai.nextMove(vision, player.getKeys(), lastAction));
		        if (lastAction == true) {
		        	steps++;
		        }
		    } else if (curPlayer == PlayerType.School) {
		    	PlayerVision vision = new PlayerVision(map, player.getPlayerGridLocation());
		    	lastAction = player.move(school.nextMove(vision, player.getKeys(), lastAction));
		    	if (lastAction == true) {
		    		steps++;
		    	}
		    }
		    
            if (player.end() || (steps >= Tournament.maxSteps)) {
                if (steps >= Tournament.maxSteps) {
                	System.out.println("Failed to escape the dungeon and become Gollum :(");
                	// update scoreboard
                    score[visionTracker][personTracker][mapTracker] = -1;
                } else {
                	System.out.println("Escaped the dungeon in "+ steps + " steps :)");
                	// update scoreboard
                    score[visionTracker][personTracker][mapTracker] = steps;
                }

                // restart the game
                mapTracker++;
                steps = 0;
                try {
					container.reinit();
				} catch (SlickException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            playerMoveTime = moveTime;
		}
	}
	
	public void printPlayerLoc(PlayerVision vision) {
        System.out.println("---------------------------------");
        System.out.print(vision.CurrentPoint.North);
        System.out.print(vision.CurrentPoint.South);
        System.out.print(vision.CurrentPoint.East);
        System.out.print(vision.CurrentPoint.West);
        System.out.println("---------------------------------");
	}
	
	public void printPlayerVision(PlayerVision vision) {
        System.out.println("---------------------------------");
        System.out.print("North: ");
	    //System.out.print(vision.CurrentPoint.North);
	    for(int i = 0; i < vision.mNorth; i++) {
            if(vision.North[i].hasKey()) {
                System.out.print("Key");
            } 
            System.out.print(vision.North[i].North);
            System.out.print(vision.North[i].South);
            System.out.print(vision.North[i].East);
            System.out.print(vision.North[i].West);
            
            System.out.print(" ");
	    }
	    System.out.println("//");
        System.out.print("South: ");
        //System.out.print(vision.CurrentPoint.South);
        for(int i = 0; i < vision.mSouth; i++) {
            if(vision.South[i].hasKey()) {
                System.out.print("Has");
            }
            System.out.print(vision.South[i].North);
            System.out.print(vision.South[i].South);
            System.out.print(vision.South[i].East);
            System.out.print(vision.South[i].West);
            
            System.out.print(" ");
        }
        System.out.println("//");
        System.out.print("East: ");
        //System.out.print(vision.CurrentPoint.East);
        for(int i = 0; i < vision.mEast; i++) {
            if(vision.East[i].hasKey()) {
                System.out.print("Key");
            } 
            System.out.print(vision.East[i].North);
            System.out.print(vision.East[i].South);
            System.out.print(vision.East[i].East);
            System.out.print(vision.East[i].West);
            
            System.out.print(" ");
        }
        System.out.println("//");
        System.out.print("West: ");
        //System.out.print(vision.CurrentPoint.West);
        for(int i = 0; i < vision.mWest; i++) {
            if(vision.West[i].hasKey()) {
                System.out.print("Key");
            } 
            System.out.print(vision.West[i].North);
            System.out.print(vision.West[i].South);
            System.out.print(vision.West[i].East);
            System.out.print(vision.West[i].West);
            
            System.out.print(" ");
        }
        System.out.println("//");
        System.out.println("---------------------------------");
	    
	}
	
	/*
	 * Mike
	 * @see org.newdawn.slick.Game#render(org.newdawn.slick.GameContainer, org.newdawn.slick.Graphics)
	 * 
	 * Render Method: Draws the player in the center of the map and moves the map around it
	 * 				Map rendering starts at 0,0 (top left).
	 * 				Formula: x = (half of the screen width pixels) - the player's x position on the grid * 16 pixels
	 * 						 y = (half of the screen height pixels) - the player's y position on the grid * 16 pixels
	 * 
	 */
	public void render(GameContainer container, Graphics g)  {
		map.getMap().render(320 - (int) ((float)player.getPlayerGridLocation().getX()*16),
				240 - (int) ((float)player.getPlayerGridLocation().getY()*16));
		g.drawAnimation(player.getPlayerAnimation(), (640/2),(480/2));
	}
}