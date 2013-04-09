package com.csc2013;
import org.newdawn.slick.SlickException;
import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.MoveType;

public class MapBox {
    
    final BoxType North;
    final BoxType South;
    final BoxType East;
    final BoxType West;

    final MoveType NorthMove;
    final MoveType SouthMove;
    final MoveType EastMove;
    final MoveType WestMove;
    private boolean hasConsumable;
    private boolean isEnd;
    String ConsumableType = null;
    
    public MapBox() throws SlickException {
        North = BoxType.Open;       
        South = BoxType.Open;
        East = BoxType.Open;
        West = BoxType.Open; 
        
        NorthMove = MoveType.Open;
        SouthMove = MoveType.Open;
        EastMove = MoveType.Open;
        WestMove = MoveType.Open;
        
        hasConsumable = false;
        isEnd = false;
    }
    
    public MapBox(BoxType N, BoxType S, BoxType E, BoxType W, boolean consumable, boolean end) {
        North = N;
        South = S;
        East = E;
        West = W;
        
        if(N == BoxType.Open || N == BoxType.Key) {NorthMove = MoveType.Open;} else {NorthMove = MoveType.Blocked;}
        if(S == BoxType.Open || S == BoxType.Key) {SouthMove = MoveType.Open;} else {SouthMove = MoveType.Blocked;}
        if(E == BoxType.Open || E == BoxType.Key) {EastMove = MoveType.Open;} else {EastMove = MoveType.Blocked;}
        if(W == BoxType.Open || W == BoxType.Key) {WestMove = MoveType.Open;} else {WestMove = MoveType.Blocked;}
        
        hasConsumable = consumable;
        if(consumable) {
            ConsumableType = "hi";
        }
        isEnd = end;
    }
    
    public boolean hasKey() {
        return hasConsumable;
    }
    
    public void consume() {
        hasConsumable = false;
    }
    
    public boolean isEnd() {
        return isEnd;
    }

    
}