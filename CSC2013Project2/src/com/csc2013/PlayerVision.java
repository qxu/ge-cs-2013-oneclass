package com.csc2013;

import java.awt.Point;

import com.csc2013.DungeonMaze.BoxType;
import com.csc2013.DungeonMaze.MoveType;

public class PlayerVision {
    MapBox CurrentPoint;
    
    MapBox[] North;
    MapBox[] South;
    MapBox[] East;
    MapBox[] West;
    
    int mNorth;
    int mSouth;
    int mEast;
    int mWest;
    public static int distanceToView = 9;
    
    
    public PlayerVision(Map map, Point playerLoc) {
        mNorth = 0;
        mSouth = 0;
        mEast = 0;
        mWest = 0;
        
        CurrentPoint = map.getMapBox(playerLoc);

        /* West */
        MapBox curPoint = map.getMapBox(playerLoc);
        while(curPoint.WestMove == MoveType.Open && mWest < distanceToView) {
            mWest++;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX()-mWest,(int)playerLoc.getY()));
        }

        West = new MapBox[mWest];
        for(int i = 1; i <= mWest; i++) {
            curPoint = map.getMapBox(new Point((int)playerLoc.getX()-i,(int)playerLoc.getY()));
            West[i-1] = curPoint;
        }
        
        /* East */
        curPoint = map.getMapBox(playerLoc);
        while(curPoint.EastMove == MoveType.Open  && mEast < distanceToView) {
            mEast++;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX()+mEast,(int)playerLoc.getY()));
        }

        East = new MapBox[mEast];
        for(int i = 1; i <= mEast; i++) {
            curPoint = map.getMapBox(new Point((int)playerLoc.getX()+i,(int)playerLoc.getY()));
            East[i-1] = curPoint;
        }
                
        /* North */
        curPoint = map.getMapBox(playerLoc);
        while(curPoint.NorthMove == MoveType.Open && mNorth < distanceToView) {
            mNorth++;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX(),(int)playerLoc.getY()-mNorth));
        }

        North = new MapBox[mNorth];
        for(int i = 1; i <= mNorth; i++) {
            curPoint = map.getMapBox(new Point((int)playerLoc.getX(),(int)playerLoc.getY()-i));
            North[i-1] = curPoint;
        }
                
        /* South */
        curPoint = map.getMapBox(playerLoc);
        while(curPoint.SouthMove == MoveType.Open && mSouth < distanceToView) {
            mSouth++;
            curPoint = map.getMapBox(new Point((int)playerLoc.getX(),(int)playerLoc.getY()+mSouth));
        }
        
        South = new MapBox[mSouth];
        for(int i = 1; i <= mSouth; i++) {
            curPoint = map.getMapBox(new Point((int)playerLoc.getX(),(int)playerLoc.getY()+i));
            South[i-1] = curPoint;
        }
    }
}
