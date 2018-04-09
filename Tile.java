import bc.MapLocation;
import bc.Planet;

public class Tile implements Comparable<Tile> {

	private boolean passable = true;
	private int karbonite = 0;
	
	private boolean searched = false;
	
	public Planet planet;
	public int x;
	public int y;
	
	public MapLocation mapLoc;
	
	public int posInPath = 0;
	public Tile parent;
	public float cost;
	public float heuristic;
	
	public Tile() {
		setSearched(false);
		setIsPassable(true);
		setInitKarbonite(0);
	}
	
	public Tile(Planet _planet, int _x, int _y) {
		setSearched(false);
		setIsPassable(true);
		setInitKarbonite(0);
		x = _x;
		y = _y;
		planet = _planet;
		
		mapLoc = new MapLocation(planet, x, y);
	}
	
	
	public void setIsPassable(boolean p) {
		passable = p;
	}
	public boolean getIsPassable() {
		return passable;
	}
	
	public void setInitKarbonite(int k) {
		karbonite = k;
	}
	public int getInitKarbonite() {
		return karbonite;
	}
	
	public void setSearched(boolean s) {
		searched = s;
	}
	public boolean getSearched() {
		return searched;
	}
	
	public int setParent(Tile p) {
		parent = p;
		posInPath = parent.posInPath + 1;
				
		return posInPath;
	}
	
	public void clearParent() {
		parent = null;
	}
	
	public boolean equals(Object other) {
		if (other instanceof Tile) {
			Tile o = (Tile) other;
			
			return (o.x == x) && (o.y == y);
		}
		
		return false;
	}
	
	@Override
	public int compareTo(Tile other) {
		Tile o = other;
		
		float f = heuristic + cost;
		float of = o.heuristic + o.cost;
		
		if (f < of) {
			return -1;
		} else if (f > of) {
			return 1;
		} else {
			return 0;
		}
	}
	
	

}
