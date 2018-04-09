import java.util.ArrayList;
import java.util.Collections;

import bc.MapLocation;
import bc.Planet;
import bc.Unit;


public class AStar {
		
	public static Tile[][] tiles;
	
	private ArrayList<Tile> tilesToSearch = new ArrayList<Tile>();
	private ArrayList<Tile> searchedTiles = new ArrayList<Tile>();
	
	private boolean allowDiag = false;
	
	
	public AStar(boolean diag) {
		allowDiag = diag;
		
	}

	
	public Path aStar(int startX, int startY, int targetX, int targetY, Unit currentUnit, int toThisUnitId) throws Exception {
		///Path path = new Path();
		
		//Tile lastSearched = null;
		int maxSearchDist;
		
		//System.out.println("StartX: " + startX + " StartY: " + startY);
		MapLocation unitLoc = currentUnit.location().mapLocation();
		Planet planet = unitLoc.getPlanet();
		
		Tile target = null;
		//System.out.println("TargetX: " + targetX + " TargetY: " + targetY);
		
		tilesToSearch.clear();
		searchedTiles.clear();
		
		
		
		if (planet == Planet.Earth) {
			tiles = GameManager.earthTiles;
			maxSearchDist = 130;
		} else {
			tiles = GameManager.marsTiles;
			maxSearchDist = 500;
		}
		
		
		tiles[startX][startY].cost = 0;
		tiles[startX][startY].posInPath = 0;
		
		//tiles[startX][startY].setSearched(false);
		
		addToTilesToSearch(startX, startY);
		
		
		while (tilesToSearch.size() != 0) {
			
			Tile current = tilesToSearch.get(0);
			
			//System.out.println("CurrentX: " + current.x + " CurrentY: " + current.y);
			
			tilesToSearch.remove(current);
			addToSearched(current);
			
			//lastSearched = current;
			
			if (current.x == targetX && current.y == targetY) {
				target = current;
				//System.out.println("Found Target Early!");
				tilesToSearch.clear();
				continue;
			}
			
			
			
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					
					if ((x == 0) && (y == 0)) {
						continue;
					}
					
					if (!allowDiag) {
						if ((x != 0) && (y != 0)) {
							continue;
						}
					}
					
					
					
					int currentNeighborX = current.x + x;
					int currentNeighborY = current.y + y;
					
					
					
					boolean isOffBoard = (currentNeighborX < 0 || currentNeighborY < 0 || currentNeighborX >= tiles.length || currentNeighborY >= tiles[currentNeighborX].length);
					boolean canSeeLoc = canSee(new MapLocation(planet, currentNeighborX, currentNeighborY));
					
					
					
					if (!isOffBoard && tiles[currentNeighborX][currentNeighborY].getIsPassable()) {
						

						if (currentNeighborX == startX && currentNeighborY == startY)
							continue;
						
						
						Tile currentNeighbor = tiles[currentNeighborX][currentNeighborY];
						MapLocation currentNeighborLoc = new MapLocation(planet, currentNeighborX, currentNeighborY);
						
						
						if (distanceSquaredBetween(unitLoc, currentNeighborLoc) > maxSearchDist) {
							continue;
						}
						
						
						if (canSeeLoc) 
							if (!(currentNeighborX == targetX && currentNeighborY == targetY))
								if (!GameManager.checkIfOccupiable(currentNeighborLoc))
									continue;
						
						
						
						
						if (currentNeighborX == targetX && currentNeighborY == targetY) {
							target = currentNeighbor;
							
							//System.out.println("Found Target!");
							tilesToSearch.clear();
							//continue;
						}
						
					
						
						
						
						float nextStepCost = current.cost + 1;
						
						if (nextStepCost < currentNeighbor.cost) {
							if (tilesToSearch.contains(currentNeighbor)) {
								tilesToSearch.remove(currentNeighbor);
							}
							if (searchedTiles.contains(currentNeighbor)) {
								searchedTiles.remove(currentNeighbor);
							}
						}
						
						if (!(tilesToSearch.contains(currentNeighbor)) && !(searchedTiles.contains(currentNeighbor))) {
							currentNeighbor.cost = nextStepCost;
							//tiles[currentNeighborX][currentNeighborY].cost = nextStepCost;
							currentNeighbor.heuristic = 1;
							currentNeighbor.setParent(current);
							//tiles[currentNeighborX][currentNeighborY].setParent(current);
							addToTilesToSearch(currentNeighbor);
							
							
							
						} else {
							//continue;
						}
						
					} else {
						//continue;
					}
					
					
				}
			}
			
			
		
		}
		
//		if (target == null) {
//			//System.out.println("No path found.....");
//			return null;
//		}
		
		
			

		//path = new Path();
		
		//If target Location wasn't found, go as far as possible..
		if (target == null) {
			//System.out.println("Target out of range.. going as far as possible.");
			
			Tile closest = target = searchedTiles.get(searchedTiles.size() - 1);
			int closestDist = 500;
			for (int i = searchedTiles.size() - 1; i >= 0; i--) {
				int temp = AbsSub(targetX, searchedTiles.get(i).x) + AbsSub(targetY, searchedTiles.get(i).y);
				if (temp < closestDist) {
					closestDist = temp;
					closest = searchedTiles.get(i);
				}
			
			}
			
			target = closest;
		}
		
		
		MapLocation start = new MapLocation(planet, startX, startY);
		MapLocation end = new MapLocation(planet, target.x, target.y);
		
		
		Path path = null;
		if (toThisUnitId != -1) {
			path = new Path(start, end, toThisUnitId);
		} else {
			path = new Path(start, end);
		}
		
		
		if (target.x == startX && target.y == startY) {
			path.prependStep(startX, startY);
			return path;
		}
		
		//System.out.println("StartLoc: " + startX + ", " + startY);
		//System.out.println("TARGET LOC: " + target.x + ", " + target.y);
		
		while (!(target.x == startX && target.y == startY)) {
			//System.out.println("Prepeding..." + target.x + ", " + target.y);
			path.prependStep(target.x, target.y);
			//if (target.parent != null) {
				target = target.parent;
			//}
		}
		//path.prependStep(startX, startY);
		
		return path;
		
	}
	
	public Path aStarIncludeStartLocInPath(int startX, int startY, int targetX, int targetY, Unit currentUnit, int toThisUnitId) throws Exception {
		///Path path = new Path();
		
		//Tile lastSearched = null;
		
		////System.out.println("StartX: " + startX + " StartY: " + startY);
		
		Tile target = null;
		////System.out.println("TargetX: " + targetX + " TargetY: " + targetY);
		
		MapLocation unitLoc = currentUnit.location().mapLocation();
		Planet planet = unitLoc.getPlanet();
		
		tilesToSearch.clear();
		searchedTiles.clear();
		
		
		if (planet == Planet.Earth)
			tiles = GameManager.earthTiles;
		else {
			tiles = GameManager.marsTiles;
		}
		
		
		tiles[startX][startY].cost = 0;
		tiles[startX][startY].posInPath = 0;
		//tiles[startX][startY].setSearched(false);
		
		addToTilesToSearch(startX, startY);
		
		
		while (tilesToSearch.size() != 0) {
			
			Tile current = tilesToSearch.get(0);
			
			////System.out.println("CurrentX: " + current.x + " CurrentY: " + current.y);
			
			tilesToSearch.remove(current);
			addToSearched(current);
			
			//lastSearched = current;
			
			if (current == tiles[targetX][targetY]) {
				target = current;
				////System.out.println("Found Target Early!");
				tilesToSearch.clear();
				continue;
			}
			
			
			
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					
					if ((x == 0) && (y == 0)) {
						continue;
					}
					
					if (!allowDiag) {
						if ((x != 0) && (y != 0)) {
							continue;
						}
					}
					
					
					
					int currentNeighborX = current.x + x;
					int currentNeighborY = current.y + y;
					
					
					
					boolean isOffBoard = (currentNeighborX < 0 || currentNeighborY < 0 || currentNeighborX >= tiles.length || currentNeighborY >= tiles[currentNeighborX].length);
					boolean canSeeLoc = canSee(new MapLocation(currentUnit.location().mapLocation().getPlanet(), currentNeighborX, currentNeighborY));
					
					
					
					if (!isOffBoard && tiles[currentNeighborX][currentNeighborY].getIsPassable()) {
						
						
						Tile currentNeighbor = tiles[currentNeighborX][currentNeighborY];
						
						if (canSeeLoc) 
							if (!GameManager.checkIfOccupiable(new MapLocation(currentUnit.location().mapLocation().getPlanet(), currentNeighborX, currentNeighborY)))
								continue;
						
						
						if (currentNeighbor == tiles[targetX][targetY]) {
							target = currentNeighbor;
							
							////System.out.println("Found Target!");
							tilesToSearch.clear();
							//continue;
						}
						
						
						
						if (currentNeighbor == tiles[startX][startY])
							continue;
						
						
						
						float nextStepCost = current.cost + 1;
						
						if (nextStepCost < currentNeighbor.cost) {
							if (tilesToSearch.contains(currentNeighbor)) {
								tilesToSearch.remove(currentNeighbor);
							}
							if (searchedTiles.contains(currentNeighbor)) {
								searchedTiles.remove(currentNeighbor);
							}
						}
						
						if (!(tilesToSearch.contains(currentNeighbor)) && !(searchedTiles.contains(currentNeighbor))) {
							currentNeighbor.cost = nextStepCost;
							//tiles[currentNeighborX][currentNeighborY].cost = nextStepCost;
							currentNeighbor.heuristic = 1;
							currentNeighbor.setParent(current);
							//tiles[currentNeighborX][currentNeighborY].setParent(current);
							addToTilesToSearch(currentNeighbor);
							
							
							
						} else {
							//continue;
						}
						
					} else {
						//continue;
					}
					
					
				}
			}
			
			
		
		}
		
//		if (target == null) {
//			//System.out.println("No path found.....");
//			return null;
//		}
		
		
			

		//path = new Path();
		
		//If target Location wasn't found, go ass far as possible..
		if (target == null) {
			////System.out.println("Target out of range.. going as far as possible.");
			
			Tile closest = target = searchedTiles.get(searchedTiles.size() - 1);
			int closestDist = 500;
			for (int i = searchedTiles.size() - 1; i >= 0; i--) {
				int temp = AbsSub(targetX, searchedTiles.get(i).x) + AbsSub(targetY, searchedTiles.get(i).y);
				if (temp < closestDist) {
					closestDist = temp;
					closest = searchedTiles.get(i);
				}
			
			}
			
			target = closest;
		}
		
		
		MapLocation start = new MapLocation(currentUnit.location().mapLocation().getPlanet(), startX, startY);
		MapLocation end = new MapLocation(currentUnit.location().mapLocation().getPlanet(), target.x, target.y);
		
		
		Path path = null;
		if (toThisUnitId != -1) {
			path = new Path(start, end, toThisUnitId);
		} else {
			path = new Path(start, end);
		}
		
		
		if (target == tiles[startX][startY]) {
			path.prependStep(startX, startY);
			return path;
		}
		
		////System.out.println("StartLoc: " + startX + ", " + startY);
		////System.out.println("TARGET LOC: " + target.x + ", " + target.y);
		
		while (target != tiles[startX][startY]) {
			////System.out.println("Prepending..." + target.x + ", " + target.y);
			path.prependStep(target.x, target.y);
			//if (target.parent != null) {
				target = target.parent;
			//}
		}
		path.prependStep(startX, startY);
		
		return path;
		
	}
	
	public Path aStarKarbonitePath(int startX, int startY, Unit currentUnit, int maxSearchDist) {
		//Search for closest initial karbonite
		
		Tile target = null;
		////System.out.println("TargetX: " + targetX + " TargetY: " + targetY);
		
		tilesToSearch.clear();
		searchedTiles.clear();
		
		MapLocation unitLoc = currentUnit.location().mapLocation();
		Planet planet = unitLoc.getPlanet();
		
		
		if (planet == Planet.Earth)
			tiles = GameManager.earthTiles;
		else {
			tiles = GameManager.marsTiles;
		}
		
		
		tiles[startX][startY].cost = 0;
		tiles[startX][startY].posInPath = 0;
		//tiles[startX][startY].setSearched(false);
		
		addToTilesToSearch(startX, startY);
		
		
		while (tilesToSearch.size() != 0) {
			
			Tile current = tilesToSearch.get(0);
			
			////System.out.println("CurrentX: " + current.x + " CurrentY: " + current.y);
			
			tilesToSearch.remove(current);
			addToSearched(current);
			
			//lastSearched = current;
			
			if (current.getInitKarbonite() > 0) {
				target = current;
				////System.out.println("Found Target Early!");
				tilesToSearch.clear();
				continue;
			}
			
			
			
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					
					if ((x == 0) && (y == 0)) {
						continue;
					}
					
					if (!allowDiag) {
						if ((x != 0) && (y != 0)) {
							continue;
						}
					}
					
					
					
					int currentNeighborX = current.x + x;
					int currentNeighborY = current.y + y;
					
					
					
					boolean isOffBoard = (currentNeighborX < 0 || currentNeighborY < 0 || currentNeighborX >= tiles.length || currentNeighborY >= tiles[currentNeighborX].length);
					boolean canSeeLoc = canSee(new MapLocation(currentUnit.location().mapLocation().getPlanet(), currentNeighborX, currentNeighborY));
					
					
					
					if (!isOffBoard && tiles[currentNeighborX][currentNeighborY].getIsPassable()) {
						
						
						Tile currentNeighbor = tiles[currentNeighborX][currentNeighborY];
						
						if (canSeeLoc) 
							if (!GameManager.checkIfOccupiable(new MapLocation(currentUnit.location().mapLocation().getPlanet(), currentNeighborX, currentNeighborY)))
								continue;
						
						
						if (currentNeighbor.getInitKarbonite() > 0) {
							target = currentNeighbor;
							
							////System.out.println("Found Target!");
							tilesToSearch.clear();
							//continue;
						}
						
						
						
						if (currentNeighbor == tiles[startX][startY])
							continue;
						
						
						
						float nextStepCost = current.cost + 1;
						
						if (nextStepCost < currentNeighbor.cost) {
							if (tilesToSearch.contains(currentNeighbor)) {
								tilesToSearch.remove(currentNeighbor);
							}
							if (searchedTiles.contains(currentNeighbor)) {
								searchedTiles.remove(currentNeighbor);
							}
						}
						
						if (!(tilesToSearch.contains(currentNeighbor)) && !(searchedTiles.contains(currentNeighbor))) {
							currentNeighbor.cost = nextStepCost;
							//tiles[currentNeighborX][currentNeighborY].cost = nextStepCost;
							currentNeighbor.heuristic = 1;
							currentNeighbor.setParent(current);
							//tiles[currentNeighborX][currentNeighborY].setParent(current);
							addToTilesToSearch(currentNeighbor);
							
							
							
						} else {
							//continue;
						}
						
					} else {
						//continue;
					}
					
					
				}
			}
			
			
		
		}
		
//		if (target == null) {
//			//System.out.println("No path found.....");
//			return null;
//		}
		
		
			

		//path = new Path();
		
		//If target Location wasn't found, go as far as possible..
//		if (target == null) {
//			////System.out.println("Target out of range.. going as far as possible.");
//			
//			Tile closest = target = searchedTiles.get(searchedTiles.size() - 1);
//			int closestDist = 500;
//			for (int i = searchedTiles.size() - 1; i >= 0; i--) {
//				int temp = AbsSub(targetX, searchedTiles.get(i).x) + AbsSub(targetY, searchedTiles.get(i).y);
//				if (temp < closestDist) {
//					closestDist = temp;
//					closest = searchedTiles.get(i);
//				}
//			
//			}
//			
//			target = closest;
			
			
//		}
		
		
		MapLocation start = new MapLocation(currentUnit.location().mapLocation().getPlanet(), startX, startY);
		
		if (target == null) {
			Path p = new Path(start, start);
			p.prependStep(startX, startY);
			return p;
		}
		
		
		MapLocation end = new MapLocation(currentUnit.location().mapLocation().getPlanet(), target.x, target.y);
		
		
		Path path = new Path(start, end);
		
	
		if (target == tiles[startX][startY]) {
			path.prependStep(startX, startY);
			return path;
		}
		
		////System.out.println("StartLoc: " + startX + ", " + startY);
		////System.out.println("TARGET LOC: " + target.x + ", " + target.y);
		
		while (target != tiles[startX][startY]) {
			//System.out.println("Prepeding..." + target.x + ", " + target.y);
			path.prependStep(target.x, target.y);
			//if (target.parent != null) {
				target = target.parent;
			//}
		}
		//path.prependStep(startX, startY);
		
		return path;
		
		
	}
	
	
	public MapLocation aStarKarboniteLoc(int startX, int startY, Unit currentUnit, int maxSearchDist) {
		//Search for closest initial karbonite
		
		//Tile target = null;
		////System.out.println("TargetX: " + targetX + " TargetY: " + targetY);
		MapLocation unitLoc = currentUnit.location().mapLocation();
		Planet planet = unitLoc.getPlanet();
		
		
		
		if (planet == Planet.Earth)
			tiles = GameManager.earthTiles;
		else {
			tiles = GameManager.marsTiles;
		}
		
		
		tilesToSearch.clear();
		searchedTiles.clear();
		tiles[startX][startY].cost = 0;
		tiles[startX][startY].posInPath = 0;
		//tiles[startX][startY].setSearched(false);
		
		addToTilesToSearch(startX, startY);
		
		
		while (tilesToSearch.size() != 0) {
			
			Tile current = tilesToSearch.get(0);
			
			////System.out.println("CurrentX: " + current.x + " CurrentY: " + current.y);
			////System.out.println("Karbonite here: " + current.getInitKarbonite());
			tilesToSearch.remove(current);
			addToSearched(current);
			
			//lastSearched = current;
			
			if (current.getInitKarbonite() > 0) {
				//target = current;
				////System.out.println("Found Karbonite Early!");
				//tilesToSearch.clear();
				return new MapLocation(planet, current.x, current.y);
			}
			
			
			
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					
					if ((x == 0) && (y == 0)) {
						continue;
					}
					
					if (!allowDiag) {
						if ((x != 0) && (y != 0)) {
							continue;
						}
					}
					
					
					
					int currentNeighborX = current.x + x;
					int currentNeighborY = current.y + y;
					
					
					
					boolean isOffBoard = (currentNeighborX < 0 || currentNeighborY < 0 || currentNeighborX >= tiles.length || currentNeighborY >= tiles[currentNeighborX].length);
					boolean canSeeLoc = canSee(new MapLocation(planet, currentNeighborX, currentNeighborY));
					
					
					
					if (!isOffBoard && tiles[currentNeighborX][currentNeighborY].getIsPassable()) {
						
						
						Tile currentNeighbor = tiles[currentNeighborX][currentNeighborY];
						MapLocation currentNeighborLoc = new MapLocation(planet, currentNeighborX, currentNeighborY);
						
						
						if (distanceSquaredBetween(unitLoc, currentNeighborLoc) > maxSearchDist) {
							continue;
						}
						
						
						
						
						if (canSeeLoc) 
							if (!GameManager.checkIfOccupiable(currentNeighborLoc) || GameManager.checkIfOccupied(currentNeighborLoc))
								continue;
						
						
						
						if (currentNeighbor.getInitKarbonite() > 0) {
							//target = currentNeighbor;
							
							////System.out.println("Found Karbonite!");
							//tilesToSearch.clear();
							//continue;

							return new MapLocation(planet, currentNeighborX, currentNeighborY);
						}
						
						
						if (currentNeighborX == startX && currentNeighborY == startY)
							continue;
						
						
						
						float nextStepCost = current.cost + 1;
						
						if (nextStepCost < currentNeighbor.cost) {
							if (tilesToSearch.contains(currentNeighbor)) {
								tilesToSearch.remove(currentNeighbor);
							}
							if (searchedTiles.contains(currentNeighbor)) {
								searchedTiles.remove(currentNeighbor);
							}
						}
						
						if (!(tilesToSearch.contains(currentNeighbor)) && !(searchedTiles.contains(currentNeighbor))) {
							currentNeighbor.cost = nextStepCost;
							//tiles[currentNeighborX][currentNeighborY].cost = nextStepCost;
							currentNeighbor.heuristic = 1;
							currentNeighbor.setParent(current);
							//tiles[currentNeighborX][currentNeighborY].setParent(current);
							addToTilesToSearch(currentNeighbor);
							
							
							
						} else {
							//continue;
						}
						
					} else {
						//continue;
					}
					
					
				}
			}
			
			
		
		}
		
		
		return null;
		
		
//		if (target == null) {
//			//System.out.println("No path found.....");
//			return null;
//		}
		
		
			

		//path = new Path();
		
		//If target Location wasn't found, go as far as possible..
//		if (target == null) {
//			////System.out.println("Target out of range.. going as far as possible.");
//			
//			Tile closest = target = searchedTiles.get(searchedTiles.size() - 1);
//			int closestDist = 500;
//			for (int i = searchedTiles.size() - 1; i >= 0; i--) {
//				int temp = AbsSub(targetX, searchedTiles.get(i).x) + AbsSub(targetY, searchedTiles.get(i).y);
//				if (temp < closestDist) {
//					closestDist = temp;
//					closest = searchedTiles.get(i);
//				}
//			
//			}
//			
//			target = closest;
			
			
//		}
		
//		System.out.print("Either coultdnt find location of closestKarbonite, or something didnt work in searchForClosest......\n\n\n\n");
//		MapLocation start = new MapLocation(currentUnit.location().mapLocation().getPlanet(), startX, startY);
//		
//		return start;
		
		
//		MapLocation end = new MapLocation(currentUnit.location().mapLocation().getPlanet(), target.x, target.y);
//		
//		
//		Path path = new Path(start, end);
//		
//	
//		if (target == tiles[startX][startY]) {
//			path.prependStep(startX, startY);
//			return path;
//		}
//		
//		//System.out.println("StartLoc: " + startX + ", " + startY);
//		//System.out.println("TARGET LOC: " + target.x + ", " + target.y);
//		
//		while (target != tiles[startX][startY]) {
//			//System.out.println("Prepeding..." + target.x + ", " + target.y);
//			path.prependStep(target.x, target.y);
//			//if (target.parent != null) {
//				target = target.parent;
//			//}
//		}
//		//path.prependStep(startX, startY);
//		
//		return path;
		
		
	}
	
	
	public void addToTilesToSearch(Tile tile) {
		tilesToSearch.add(tile);
		Collections.sort(tilesToSearch);
	}
	
	public void addToTilesToSearch(int x, int y) {
		addToTilesToSearch(tiles[x][y]);
	}
	
	public void addToSearched(int x, int y) {
		searchedTiles.add(tiles[x][y]);
		tiles[x][y].setSearched(true);
	}
	public void addToSearched(Tile tile) {
		searchedTiles.add(tile);
		tile.setSearched(true);
	}
	
	
	public void setTiles(Tile[][] t) {
		tiles = t;
	}
	
	
	private boolean canSee(MapLocation m) {
		return GameManager.controller.canSenseLocation(m);
	}
	
	private int distanceSquaredBetween(MapLocation a, MapLocation b) {
		return Sqr(a.getX() - b.getX()) + Sqr(a.getY() - b.getY());	
	}
	
	private int Sqr(int x) {
		return x*x;
	}
	
	private int AbsSub(int a, int b) {
		return Math.abs(a - b);
	}
}
