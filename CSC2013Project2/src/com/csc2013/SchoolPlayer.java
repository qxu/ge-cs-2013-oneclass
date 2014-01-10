package com.csc2013;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.csc2013.DungeonMaze.Action;
import com.csc2013.DungeonMaze.BoxType;

/**
 * Westhill's SchoolPlayer implementation. The final version was in multiple
 * classes with a very nice GUI showing how the path-finding algorithms worked.
 * Very nice for showing to our comp-sci class a breadth-first implementation of
 * Dijstrika's algorithm, as well as an a* algorithm to find a specific
 * destination even faster. Unfortunately, the guys at GE wanted just the
 * SchoolPlayer class, so we fit it all into one massive class and cut out the
 * debugger class to improve performance (and since this is the official final
 * version, we don't need it anyways).
 * 
 * @author [Westhill/NullPointerException]
 * @see <a href="https://github.com/qxu/ge-cs-2013">GitHub final version
 *      (seperate classes, includes debugger)</a>
 * @see <a href="https://github.com/qxu/ge-cs-2013-oneclass">GitHub one-class
 *      version (everything is in one SchoolPlayer.java class)</a>
 */
public class SchoolPlayer {
	/*
	 * A map to store the points with their BoxTypes.
	 */
	private final PlayerMap map;

	/*
	 * The last directional move. This means we ignore Action.Pickup and
	 * Action.Open
	 */
	private Action lastMove;

	/**
	 * Creates a {@code SchoolPlayer}.
	 */
	public SchoolPlayer() {
		this.map = new PlayerMap();
	}

	/**
	 * Gets the move.<br>
	 * Updates the map.<br>
	 * <br>
	 * If an exit is found, the action to the nearest exit is returned. Then the
	 * algorithm searches for keys, then for unknown spaces. Doors are unlocked
	 * as necessary to explore the unknown spaces.
	 * 
	 * @param vision
	 * @param keyCount
	 * @param lastAction
	 * @return Action
	 */
	public Action nextMove(PlayerVision vision, int keyCount, boolean lastAction) {
		updateMap(vision);

		Action move = getMove(keyCount, lastAction);

		if (this.map.getPlayerPosition().execute(move).getType() == BoxType.Door) {
			move = Action.Use;
		}

		if (move != Action.Use && move != Action.Pickup) {
			this.map.movePlayer(move);
			this.lastMove = move;
		}

		return move;
	}

	/*
	 * Calculates the best move. It tries to find an exit, then it tries to find
	 * a key, then it tries to uncover space.
	 */
	private Action getMove(int keyCount, boolean lastAction) {
		PlayerMap map = this.map;
		MapPoint player = map.getPlayerPosition();

		Action exitAction = ActionAlgorithms.actionTo(map, this.lastMove,
				player, BoxType.Exit);
		if (exitAction != null)
			return exitAction;

		if (keyCount < 8) {
			Action keyAction = ActionAlgorithms.actionTo(map, this.lastMove,
					player, BoxType.Key);
			if (keyAction != null)
				return keyAction;
		}

		Action coverSpaceAction = ActionAlgorithms.discoveryChannel(player,
				this.lastMove, keyCount);
		if (coverSpaceAction != null)
			return coverSpaceAction;

		System.out.println("?!!?!");
		throw new RuntimeException("out of moves");
	}

	/*
	 * Updates the map's BoxTypes from the given vision around the current
	 * player position.
	 * 
	 * So some grid geometry got us those coordinates to update around each
	 * point.
	 * 
	 * You might wonder why we have to subtract/add 1 to the the x/y coordinate.
	 * That's because the west, east, north, and south arrays start at index 0,
	 * but at index 0, the point is distance 1 from the player.
	 */
	private void updateMap(PlayerVision vision) {
		MapPoint player = this.map.getPlayerPosition();

		int playerX = player.x;
		int playerY = player.y;

		int westOffset = vision.mWest;
		int eastOffset = vision.mEast;
		int northOffset = vision.mNorth;
		int southOffset = vision.mSouth;

		MapBox[] west = vision.West;
		MapBox[] east = vision.East;
		MapBox[] north = vision.North;
		MapBox[] south = vision.South;

		PlayerMap map = this.map;

		for (int i = westOffset - 1; i >= 0; --i) {
			MapBox cell = west[i];
			map.set(playerX - i - 2, playerY, cell.West);
			map.set(playerX - i - 1, playerY - 1, cell.North);
			map.set(playerX - i - 1, playerY + 1, cell.South);
		}
		for (int i = eastOffset - 1; i >= 0; --i) {
			MapBox cell = east[i];
			map.set(playerX + i + 2, playerY, cell.East);
			map.set(playerX + i + 1, playerY - 1, cell.North);
			map.set(playerX + i + 1, playerY + 1, cell.South);
		}
		for (int i = northOffset - 1; i >= 0; --i) {
			MapBox cell = north[i];
			map.set(playerX, playerY - i - 2, cell.North);
			map.set(playerX - 1, playerY - i - 1, cell.West);
			map.set(playerX + 1, playerY - i - 1, cell.East);
		}
		for (int i = southOffset - 1; i >= 0; --i) {
			MapBox cell = south[i];
			map.set(playerX, playerY + i + 2, cell.South);
			map.set(playerX - 1, playerY + i + 1, cell.West);
			map.set(playerX + 1, playerY + i + 1, cell.East);
		}

		MapBox current = vision.CurrentPoint;

		map.set(playerX, playerY, current.hasKey() ? BoxType.Key : BoxType.Open);

		map.set(playerX - 1, playerY, current.West);
		map.set(playerX + 1, playerY, current.East);
		map.set(playerX, playerY - 1, current.North);
		map.set(playerX, playerY + 1, current.South);
	}

	/**
	 * A 2-dimensional player map that stores MapPoints and their corresponding
	 * BoxTypes. This is implemented using a {@link HashMap} mapping the
	 * MapPoint to its BoxType. The player map also stores the location of the
	 * player.
	 * 
	 * @author jqx
	 */
	private static class PlayerMap {
		/*
		 * The gridRef is needed to check if a MapPoint already exists. If it
		 * does, then it can be retrieved using gridRef.get(point), where point
		 * is any point holding the desired coordinates.
		 */
		private Map<MapPoint, MapPoint> gridRef;

		/*
		 * The typeMap maps a MapPoint to its BoxType.
		 */
		private Map<MapPoint, BoxType> typeMap;

		private MapPoint playerPosition;

		/**
		 * Constructs a new player map with the player starting at coordinates
		 * (0, 0).
		 */
		public PlayerMap() {
			this(0, 0);
		}

		/**
		 * Constructs a new player map with the player starting at the given
		 * coordinates.
		 * 
		 * @param x
		 *            the x coordinate
		 * @param y
		 *            the y coordinate
		 */
		public PlayerMap(int x, int y) {
			this.gridRef = new HashMap<>();
			this.typeMap = new HashMap<>();
			this.playerPosition = new MapPoint(0, 0, this);
		}

		/**
		 * Returns a localized version of the given point. If the given point
		 * exists in this player map, then {@code get(point)} returns the a
		 * {@code MapPoint} that is backed by this map. If there is no point in
		 * this map with the same coordinates of the given point, then this
		 * method returns {@code null}.
		 * 
		 * @param point
		 *            the point to get the localized version of
		 * @return the localized point, or {@code null} if none
		 */
		public MapPoint get(MapPoint point) {
			return this.gridRef.get(point);
		}

		/**
		 * Returns the type of the coordinates of the given point in this map.
		 * 
		 * @param point
		 *            the coordinates
		 * @return the type
		 */
		public BoxType getTypeOf(MapPoint point) {
			return this.typeMap.get(point);
		}

		/**
		 * Moves the player in this map.
		 * 
		 * @param move
		 *            the move
		 */
		public void movePlayer(Action move) {
			this.playerPosition = this.playerPosition.execute(move);
		}

		public MapPoint set(int x, int y, BoxType type) {
			if (type == null)
				throw new NullPointerException();
			MapPoint newPoint = new MapPoint(x, y, this);
			MapPoint existingPoint = this.gridRef.get(newPoint);
			if (existingPoint == null) {
				this.gridRef.put(newPoint, newPoint);
				this.typeMap.put(newPoint, type);
				return newPoint;
			} else {
				this.typeMap.put(existingPoint, type);
				return existingPoint;
			}
		}

		/**
		 * Returns the point where the player is located.
		 * 
		 * @return the player's position
		 */
		public MapPoint getPlayerPosition() {
			return this.playerPosition;
		}

		/**
		 * Finds all points in this map with the given type. Returns an empty
		 * set if the type is {@code null}.
		 * 
		 * @param type
		 * @return
		 */
		public Set<MapPoint> find(BoxType type) {
			Set<MapPoint> found = new HashSet<>();
			for (Map.Entry<MapPoint, BoxType> entry : this.typeMap.entrySet()) {
				if (entry.getValue() == type) {
					found.add(entry.getKey());
				}
			}
			return found;
		}
	}

	/**
	 * Stores the coordinates of a point with references to the points west,
	 * east, north, and south for quick lookup. A {@code MapPoint} must belong
	 * to a {@code PlayerMap}. The west, east, north, and south point references
	 * are initialized lazily.
	 * 
	 * @author jqx
	 */
	private static class MapPoint {
		public final int x;
		public final int y;

		private MapPoint west;
		private MapPoint east;
		private MapPoint north;
		private MapPoint south;

		private PlayerMap map;

		/**
		 * Constructs a new {@code MapPoint} from at the given coordinates (x,
		 * y). The point will be based from the {@code PlayerMap} given.
		 * 
		 * @param x
		 *            the x coordinate
		 * @param y
		 *            the y coordinate
		 * @param map
		 *            the map to reference
		 */
		public MapPoint(int x, int y, PlayerMap map) {
			if (map == null)
				throw new NullPointerException();
			this.x = x;
			this.y = y;
			this.map = map;
		}

		/**
		 * Gets the {@code BoxType} of this point.
		 * 
		 * @return the {@code BoxType} of this point
		 */
		public BoxType getType() {
			return this.map.getTypeOf(this);
		}

		/**
		 * Returns the point west of this point.
		 * 
		 * @return the point to the west
		 */
		public MapPoint west() {
			MapPoint cachedW = this.west;
			if (cachedW != null)
				return cachedW;
			MapPoint w = new MapPoint(this.x - 1, this.y, this.map);
			MapPoint mapW = this.map.get(w);
			return (mapW != null) ? (this.west = mapW) : w;
		}

		/**
		 * Returns the point east of this point.
		 * 
		 * @return the point to the east
		 */
		public MapPoint east() {
			MapPoint cachedE = this.east;
			if (cachedE != null)
				return cachedE;
			MapPoint e = new MapPoint(this.x + 1, this.y, this.map);
			MapPoint mapE = this.map.get(e);
			return (mapE != null) ? (this.east = mapE) : e;
		}

		/**
		 * Returns the point north of this point.
		 * 
		 * @return the point to the north
		 */
		public MapPoint north() {
			MapPoint cachedN = this.north;
			if (cachedN != null)
				return cachedN;
			MapPoint n = new MapPoint(this.x, this.y - 1, this.map);
			MapPoint mapN = this.map.get(n);
			return (mapN != null) ? (this.north = mapN) : n;
		}

		/**
		 * Returns the point south of this point.
		 * 
		 * @return the point to the south
		 */
		public MapPoint south() {
			MapPoint cachedS = this.south;
			if (cachedS != null)
				return cachedS;
			MapPoint s = new MapPoint(this.x, this.y + 1, this.map);
			MapPoint mapS = this.map.get(s);
			return (mapS != null) ? (this.south = mapS) : s;
		}

		/**
		 * Calculates the {@code Action} to get from this point to the given
		 * destination point. The action is determined as follows:<br>
		 * 
		 * <blockquote>
		 * 
		 * <pre>
		 *             Action.North
		 * Action.West (this point) Action.East
		 *             Action.South
		 * </pre>
		 * 
		 * </blockquote>
		 * 
		 * Ties are currently given in favor to the vertical directions:
		 * {@code Action.North} and {@code Action.South}. If this point's
		 * coordinates equals the destinations coordinates, it returns
		 * {@code Action.North} since it yolos and doesn't give a shit.
		 * 
		 * @param dest
		 * @return
		 */
		public Action actionTo(MapPoint dest) {
			int dx = dest.x - this.x;
			int dy = dest.y - this.y;

			return (Math.abs(dx) > Math.abs(dy)) ? ((dx > 0) ? Action.East
					: Action.West) : ((dy > 0) ? Action.South : Action.North);
		}

		/**
		 * Returns the point after executing the move.<br>
		 * <br>
		 * <table>
		 * <tr>
		 * <td>Move</td>
		 * <td>Return value</td>
		 * </tr>
		 * <tr>
		 * <td>{@code Action.West}</td>
		 * <td>{@code west()}
		 * </tr>
		 * </td>
		 * <tr>
		 * <td>{@code Action.East}</td>
		 * <td>{@code east()}</td>
		 * </tr>
		 * <tr>
		 * <td>{@code Action.North}</td>
		 * <td>{@code north()}</td>
		 * </tr>
		 * <tr>
		 * <td>{@code Action.South}</td>
		 * <td>{@code south()}</td>
		 * </tr>
		 * <tr>
		 * <td>{@code Action.Pickup}</td>
		 * <td>{@code this}</td>
		 * </tr>
		 * <tr>
		 * <td>{@code Action.Use}</td>
		 * <td>{@code this}</td>
		 * </tr>
		 * </table>
		 * 
		 * @param move
		 *            the move
		 * @return the point after executing the move.
		 * @throws NullPointerException
		 *             if the move given is {@code null}.
		 */
		public MapPoint execute(Action move) {
			switch (move) {
			case West:
				return west();
			case East:
				return east();
			case North:
				return north();
			case South:
				return south();
			case Pickup:
			case Use:
				return this;
			default:
				throw new AssertionError();
			}
		}

		/**
		 * Returns the Manhattan distance from this point to the destination
		 * point.
		 * 
		 * @param dest
		 *            the destination point
		 * @return the Manhattan distance
		 * 
		 * @see <a href="http://en.wikipedia.org/wiki/Taxicab_geometry">Taxicab
		 *      geometry - Wikipedia</a>
		 */
		public int distanceTo(MapPoint dest) {
			return Math.abs(this.x - dest.x) + Math.abs(this.y - dest.y);
		}

		/**
		 * A convenience method for iterating over the neighbors of this point.
		 * 
		 * @return the neighbors of this point.
		 */
		public Iterable<MapPoint> getNeighbors() {
			return Arrays.asList(new MapPoint[] { west(), east(), north(),
					south() });
		}

		/**
		 * Returns the hash code for this point by multiplying the x coordinate
		 * by 31 and adding it with the y coordinate.
		 * 
		 * @return the hash code for this point
		 */
		@Override
		public int hashCode() {
			return (31 * this.x) + this.y;
		}

		/**
		 * Tests two points for equality. Two points are equal if their
		 * coordinates are equal, regardless of the maps they belong to.
		 */
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof MapPoint))
				return false;
			MapPoint point = (MapPoint) o;
			return (this.x == point.x) && (this.y == point.y);
		}

		/**
		 * Returns a string with the type of this point, followed by an at
		 * symbol (@), followed by the coordinates in the form (x, y). For
		 * example, a point with type {@code BoxType.Open} and coordinates x=20,
		 * y=8 will return the string: {@code "Open@(20, 8)"}.
		 * 
		 * @return the string representation of this point
		 */
		@Override
		public String toString() {
			return getType() + "@(" + this.x + ", " + this.y + ")";
		}
	}

	/**
	 * A immutable path implementation for use in path-finding algorithms such
	 * as the breadth-first search or the a* search. It is implemented in a
	 * "linked" fashion where a {@code MapPath} contains the last point, and a
	 * reference to the path before that point. This allows for extremely quick
	 * instantiation of sub-paths by just adding a point and linking that
	 * sub-path with the current path. Every path starts with a base path with
	 * one point, and longer paths are created using the {@code subPath} method.
	 * A step path that contains the first two points. These two points
	 * determine the first "step" or action to execute the path.
	 * 
	 * @author jqx
	 */
	private static class MapPath {
		private final MapPath superPath;
		private final MapPoint last;

		private final int numOfPoints;

		private final Action prevMove;
		private final int turnCount;

		/**
		 * Constructs a base path from the given point.
		 * 
		 * @param the
		 *            starting point
		 * @throws NullPointerException
		 *             if the point given is null
		 */
		public MapPath(MapPoint start) {
			if (start == null)
				throw new NullPointerException();

			this.superPath = null;
			this.last = start;

			this.numOfPoints = 1;

			this.prevMove = null;
			this.turnCount = 0;
		}

		/*
		 * Constructs a sub-path with the given super-path and the given
		 * sub-point. Note that this constructor is private and sub-paths should
		 * be created with the subPath method.
		 */
		private MapPath(MapPath superPath, MapPoint subPoint) {
			this.superPath = superPath;
			this.last = subPoint;

			this.numOfPoints = superPath.numOfPoints + 1;

			MapPoint prev = superPath.getLastPoint();
			Action prevMove = prev.actionTo(subPoint);
			this.prevMove = prevMove;
			if (superPath.prevMove == null) {
				this.turnCount = superPath.turnCount;
			} else {
				this.turnCount = (superPath.prevMove == prevMove) ? superPath.turnCount
						: superPath.turnCount + 1;
			}
		}

		/**
		 * Constructs and returns a sub-path with this path as the super-path
		 * and the last point as the sub-point.
		 * 
		 * @param last
		 * @return
		 */
		public MapPath subPath(MapPoint last) {
			if (last == null)
				throw new NullPointerException();
			return new MapPath(this, last);
		}

		/**
		 * Returns the last point of this path.
		 * 
		 * @return the last point
		 */
		public MapPoint getLastPoint() {
			return this.last;
		}

		/**
		 * Returns the number of points in this path.
		 * 
		 * @return the length of this path
		 */
		public int length() {
			return this.numOfPoints;
		}

		/**
		 * Returns the number of switches in direction from the start point
		 * iterating to the last point.
		 * 
		 * @return the turn count
		 */
		public int getTurnCount() {
			return this.turnCount;
		}

		/**
		 * A base path is a path with only one point. This path was not created
		 * with the method {@code subPath}
		 * 
		 * @return {@code true} if this path is a base path, {@code false}
		 *         otherwise
		 */
		public boolean isBasePath() {
			return length() == 1;
		}

		/**
		 * Returns the path where the first step was taken. If this path has not
		 * taken any steps (ie. it is a base path), then it returns itself.
		 * 
		 * @return the step path
		 */
		public MapPath getStepPath() {
			MapPath prevPath = this;
			for (int i = length() - 1; i >= 2; --i) {
				prevPath = prevPath.superPath;
			}
			return prevPath;
		}

		/**
		 * Returns a list view of this path. This list contains all of the
		 * points of this path in order from start to last.
		 * 
		 * @return the list
		 */
		public List<MapPoint> toList() {
			int length = length();
			MapPoint[] points = new MapPoint[length];

			MapPath nextPath = this;
			for (int i = length - 1; i >= 0; --i) {
				points[i] = nextPath.getLastPoint();
				nextPath = nextPath.superPath;
			}
			return Arrays.asList(points);
		}

		/**
		 * Returns the hash code. The hash code is similar to
		 * {@code MapPath.equals} in that it only considers the first point,
		 * second point, and the last point.
		 * 
		 * @return the hash code for this path
		 */
		@Override
		public int hashCode() {
			int length = length();
			if (length == 1)
				return getLastPoint().hashCode();
			else if (length == 2)
				return (this.superPath.hashCode() * 31)
						+ getLastPoint().hashCode();
			else
				return (getStepPath().hashCode() * 31)
						+ getLastPoint().hashCode();
		}

		/**
		 * Tests two paths for equality. Two paths are considered equal if they
		 * have equal starting points, equal second points, and equal last
		 * points. This is because those three values are the most significant
		 * parts of a MapPath given that the first two points will determine the
		 * first move taken, and considering the middle points will slow down
		 * many path-finding algorithms. This method follows the contract
		 * defined in {@code Object.equals}.
		 * 
		 * @see Object.equals
		 */
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof MapPath))
				return false;
			MapPath path = (MapPath) o;
			int length = length();
			if (isBasePath())
				return path.isBasePath()
						&& getLastPoint().equals(path.getLastPoint());
			else if (length == 2)
				return path.length() == length
						&& this.superPath.getLastPoint().equals(
								path.superPath.getLastPoint())
						&& getLastPoint().equals(path.getLastPoint());
			else
				return getStepPath().equals(path.getStepPath())
						&& getLastPoint().equals(path.getLastPoint());
		}

		/**
		 * Returns a string representation of this path. The string is of the
		 * form: <blockquote>
		 * 
		 * <pre>
		 * point[0]->point[1] .. point[n - 1]
		 * </pre>
		 * 
		 * </blockquote> where {@code point[i]} denotes the i'th point of the
		 * path and {@code n} is the length of this path. The string is also
		 * shortened if the path is too long, omitting the middle values since
		 * they are not as useful as the others.
		 * 
		 * @return the string representation of this path
		 */
		@Override
		public String toString() {
			final int beginningLength = 2;
			final int endLength = 1;

			int length = length();
			if (length < 2)
				return this.last.toString();

			List<MapPoint> path = toList();
			StringBuilder sb = new StringBuilder();
			if (length > beginningLength + endLength) {
				for (int i = 0; i < beginningLength; ++i) {
					sb.append(path.get(i)).append("->");
				}
				sb.append(" .. ");
				for (int i = length - endLength; i < length - 1; ++i) {
					sb.append(path.get(i)).append("->");
				}
			} else {
				for (int i = 0; i < length - 1; ++i) {
					sb.append(path.get(i)).append("->");
				}
			}
			return sb.append(this.last.toString()).toString();
		}
	}

	/**
	 * An implementation of the breadth-first search to search in a
	 * {@link PlayerMap}.
	 * 
	 * @author jqx
	 * 
	 */
	private static class BFSearch {
		/**
		 * Searches for a {@code BoxType} from the given start point. The search
		 * algorithm used is a Breath-first search (BFS) modified to use a past
		 * path-cost function g(x) to determine the next point to evaluate.
		 * 
		 * @param start
		 *            the starting point
		 * @param dest
		 *            the {@code BoxType} to search for
		 * @param hasKey
		 * @return the set of solution MapPaths
		 * 
		 * @see <a
		 *      href="http://en.wikipedia.org/wiki/Breadth-first_search">Breadth-first
		 *      search - Wikipedia</a>
		 */
		public static Set<MapPath> search(MapPoint start, BoxType dest,
				int keyCount) {
			Set<MapPoint> closed = new HashSet<>();
			Map<MapPath, MapPath> open = new HashMap<>();
			Map<MapPath, Integer> gScores = new HashMap<>();

			MapPath base = new MapPath(start);
			open.put(base, base);
			gScores.put(base, 0);

			Set<MapPath> found = new HashSet<>();
			Integer foundG = null;
			while (!open.isEmpty()) {
				Entry<MapPath, Integer> curEntry = getShortestPath(gScores);
				MapPath cur = curEntry.getKey();
				MapPoint curPoint = cur.getLastPoint();

				if (curPoint.getType() == dest) {
					if (foundG == null
							|| curEntry.getValue().compareTo(foundG) <= 0) {
						found.add(cur);
						foundG = curEntry.getValue();
					}
				}

				open.remove(cur);
				gScores.remove(cur);
				closed.add(curPoint);

				if (found.isEmpty()) {
					for (MapPoint neighbor : getNeighbors(curPoint, dest,
							keyCount)) {
						if (closed.contains(neighbor)) {
							continue;
						}

						MapPath subPath = cur.subPath(neighbor);

						MapPath samePath = open.get(subPath);
						int gScore = curEntry.getValue() + distanceTo(neighbor);
						if (samePath == null) {
							open.put(subPath, subPath);
							gScores.put(subPath, gScore);
						} else {
							if (subPath.getTurnCount() < samePath
									.getTurnCount()
									&& gScore <= gScores.get(samePath)) {
								// samePath.equals(subPath)
								// so, need to explicitly remove
								// samePath/subPath to re-map value
								open.remove(samePath);
								gScores.remove(samePath);
								open.put(subPath, subPath);
								gScores.put(subPath, gScore);
							}
						}
					}
				}
			}
			return found;
		}

		/*
		 * Since opening a door takes two steps, the algorithm can't just add
		 * one to get the g score of the next point. There is a penalty for
		 * going through doors. The same thing is not as severe for keys, and we
		 * wouldn't want to penalize for walking over keys, so the algorithm
		 * will just waltz over the keys. No biggie.
		 */
		private static int distanceTo(MapPoint point) {
			if (point.getType() == BoxType.Door)
				return 2;
			else
				return 1;
		}

		/*
		 * Since java's standard library does not have a nice implementation of
		 * a tree map sorted on it's values, this is a quick helper function to
		 * get the sorted g score in a g-score-map.
		 */
		private static Entry<MapPath, Integer> getShortestPath(
				Map<MapPath, Integer> gScoreMap) {
			Iterator<Entry<MapPath, Integer>> iter = gScoreMap.entrySet()
					.iterator();
			Entry<MapPath, Integer> minEntry = iter.next();
			Integer min = minEntry.getValue();
			while (iter.hasNext()) {
				Entry<MapPath, Integer> nextEntry = iter.next();
				Integer next = nextEntry.getValue();
				if (next.compareTo(min) < 0) {
					minEntry = nextEntry;
					min = next;
				}
			}
			return minEntry;
		}

		/*
		 * Gets the neighbors of a point disregarding points that cannot be
		 * reached.
		 */
		private static Iterable<MapPoint> getNeighbors(MapPoint point,
				BoxType dest, int keyCount) {
			Collection<MapPoint> neighbors = new HashSet<>(4);
			for (MapPoint neighbor : point.getNeighbors()) {
				BoxType type = neighbor.getType();
				if ((type == dest) || (type == BoxType.Open)
						|| (type == BoxType.Door && keyCount > 0)
						|| (type == BoxType.Key)) {
					neighbors.add(neighbor);
				}
			}
			return neighbors;
		}
	}

	/**
	 * A* search algorithm for {@code PlayerMap}.
	 * 
	 * @author jqx
	 */
	private static class AStarSearch {
		/**
		 * Calculates the optimal path from {@code MapPoint} start to
		 * {@code MapPoint} dest. The search algorithm used is the A* search
		 * algorithm.
		 * 
		 * @param start
		 *            the starting point
		 * @param dest
		 *            the destination point
		 * @return the optimal path
		 * 
		 * @see <a
		 *      href="http://en.wikipedia.org/wiki/A*_search_algorithm">Asearch
		 *      algorithm - Wikipedia</a>
		 */
		public static MapPath search(MapPoint start, final MapPoint dest) {
			Map<MapPoint, Integer> gScores = new HashMap<>();
			final Map<MapPoint, Integer> fScores = new HashMap<>();

			Set<MapPoint> closed = new HashSet<>();
			Queue<MapPath> open = new PriorityQueue<>(11,
					new Comparator<MapPath>() {
						@Override
						public int compare(MapPath p1, MapPath p2) {
							return fScores.get(p1.getLastPoint()).compareTo(
									fScores.get(p2.getLastPoint()));
						}
					});

			open.add(new MapPath(start));
			gScores.put(start, 0);
			fScores.put(start, start.distanceTo(dest));

			while (!open.isEmpty()) {
				MapPath cur = open.remove();
				MapPoint curPoint = cur.getLastPoint();

				if (curPoint.equals(dest))
					return cur;

				closed.add(curPoint);

				for (MapPoint neighbor : getNeighbors(cur.getLastPoint(), dest)) {
					int gScore = gScores.get(curPoint) + distanceTo(neighbor);
					if (closed.contains(neighbor)) {
						if (gScore >= gScores.get(neighbor)) {
							continue;
						}
					}
					MapPath subPath = cur.subPath(neighbor);
					if (!open.contains(subPath)) {
						gScores.put(neighbor, gScore);
						int hScore = heuristicEstimate(neighbor, dest);
						fScores.put(neighbor, gScore + hScore);
						open.add(subPath);
					}
				}
			}

			return null;
		}

		/*
		 * Returns the Manhattan distance from a point to a destination.
		 */
		private static int heuristicEstimate(MapPoint point, MapPoint dest) {
			return point.distanceTo(dest);
		}

		/*
		 * The g score to add to a point by moving one step.
		 */
		private static int distanceTo(MapPoint point) {
			return 1;
		}

		/*
		 * Gets the neighbors of a point disregarding points that cannot be
		 * reached.
		 */
		private static Iterable<MapPoint> getNeighbors(MapPoint point,
				MapPoint dest) {
			Collection<MapPoint> neighbors = new ArrayList<>(4); // lol, the
																	// only time
																	// we use an
																	// ArrayList
			for (MapPoint neighbor : point.getNeighbors()) {
				if (neighbor != null) {
					BoxType type = neighbor.getType();
					if (neighbor.equals(dest) || (type == BoxType.Open)
							|| (type == BoxType.Key)) {
						neighbors.add(neighbor);
					}
				}
			}
			return neighbors;
		}
	}

	/**
	 * Algorithms to get the best move.
	 * 
	 * @author jqx
	 */
	private static class ActionAlgorithms {
		/**
		 * Gets the best action to the closest {@code BoxType} destination.
		 * 
		 * @param map
		 * @param start
		 * @param dest
		 * @return
		 */
		public static Action actionTo(PlayerMap map, Action lastMove,
				MapPoint start, BoxType dest) {
			Set<MapPoint> destPoints = map.find(dest);
			if (destPoints.isEmpty())
				return null;

			if (destPoints.size() == 1) // use optimized AStar algorithm
				return aStarAction(start, destPoints.iterator().next());

			Set<MapPath> paths = BFSearch.search(start, dest, 0);

			if (paths.isEmpty())
				return null;

			Set<Action> moves = EnumSet.noneOf(Action.class);
			for (MapPath path : paths) {
				moves.add(getPathAction(path));
			}

			return chooseBestMove(start, lastMove, moves);
		}

		/*
		 * Returns the Action to execute the path.
		 */
		private static Action getPathAction(MapPath path) {
			MapPoint player = path.toList().get(0);
			MapPoint point = path.getStepPath().getLastPoint();
			if (player.equals(point))
				return Action.Pickup;
			return player.actionTo(point);
		}

		/*
		 * Returns the action to get from a starting point to a destination
		 * point using the a* algorithm.
		 */
		private static Action aStarAction(MapPoint start, MapPoint dest) {
			MapPath path = AStarSearch.search(start, dest);
			return getPathAction(path);
		}

		/**
		 * Finds the best action to explore the unknown.
		 * 
		 * @param start
		 * @param lastMove
		 * @param keyCount
		 * @return the best action
		 */
		public static Action discoveryChannel(MapPoint start, Action lastMove,
				int keyCount) {
			// null is an unknown
			Set<MapPath> paths = BFSearch.search(start, null, keyCount);

			if (paths.isEmpty())
				return null;

			Set<Action> moves = EnumSet.noneOf(Action.class);
			for (MapPath path : paths) {
				moves.add(getPathAction(path));
			}

			return chooseBestMove(start, lastMove, moves);
		}

		/*
		 * Chooses the best move from a set of moves.
		 */
		private static Action chooseBestMove(MapPoint start, Action lastMove,
				Set<Action> moves) {
			// continue in the same direction, if possible
			// this way, we explore edges and corners efficiently
			// and we avoid the "zigzag effect"
			if (moves.contains(lastMove))
				return lastMove;

			Set<Action> desirable = EnumSet.noneOf(Action.class);
			for (Action move : moves) {
				if (desirableEndResult(start, move)) {
					desirable.add(move);
				}
			}

			if (!desirable.isEmpty()) {
				if (desirable.size() == 1)
					return desirable.iterator().next();
				else {
					// prefer east and south to other directions.
					if (desirable.contains(Action.East))
						return Action.East;
					else if (desirable.contains(Action.South))
						return Action.South;
					else
						return desirable.iterator().next();
				}
			} else {
				// like above, prefer east and south to other directions.
				if (moves.contains(Action.East))
					return Action.East;
				else if (moves.contains(Action.South))
					return Action.South;
				else
					return moves.iterator().next();
			}
		}

		/*
		 * Finds whether continuing with the given move starting from the start
		 * point is good or not.
		 */
		private static boolean desirableEndResult(MapPoint start, Action move) {
			MapPoint cur = start;
			if (move == Action.Pickup)
				return cur.getType() == BoxType.Key;

			// this loop gets the result BoxType after continuing to
			// travel in 'move' until it hits
			while (canTravelOn(cur.getType())) {
				MapPoint next = cur.execute(move);
				if (next.equals(cur))
					return false;
				cur = next;
			}

			// if the end result is unknown, then more space is explored.
			return cur.getType() == null;
		}

		private static boolean canTravelOn(BoxType type) {
			// BoxTypes Open and Key can be traveled on.
			return type == BoxType.Open || type == BoxType.Key;
		}
	}
}
