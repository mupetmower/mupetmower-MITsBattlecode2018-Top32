import java.util.ArrayList;

import bc.MapLocation;
import bc.Unit;

public class Path {

	private ArrayList<Step> steps = new ArrayList<Step>();
	private MapLocation pathStart;
	private MapLocation pathEnd;
	private int toUnitId = -1;
	
	private boolean onTarget = false;
	
	private int currentStepIndex = 0;

	public Path() {
		
	}
	
	public Path(MapLocation start, MapLocation end) {
		pathStart = start;
		pathEnd = end;
	}
	
	public Path(MapLocation start, MapLocation end, int _toUnitId) {
		pathStart = start;
		pathEnd = end;
		toUnitId = _toUnitId;
	}
	
	public Path(boolean isOnTarget) {
		onTarget = isOnTarget;
	}
	
	public boolean getIsOnTarget() {
		return onTarget;
	}

	public Step getNextStep() {
		int current = currentStepIndex;
		currentStepIndex = currentStepIndex + 1;
		return getStep(current);
	}
	
	public boolean isAtEnd() {
		if (currentStepIndex < (steps.size())) 
			return false;
		else
			return true;
	}
	
	public int getCurrentStepIndex() {
		return currentStepIndex;
	}
	
	public void setCurrentStepIndex(int i) {
		currentStepIndex = i;
	}
	
	public void resetStepIndex() {
		currentStepIndex = 0;
	}
	
	public int getLength() {
		return steps.size();
	}
	
	public MapLocation getStart() {
		return pathStart;
	}
	
	public MapLocation getEnd() {
		return pathEnd;
	}
	
	public int getToUnitId() {
		return toUnitId;
	}
	
	public Step getStep(int index) {
		return (Step) steps.get(index);
	}
	
	public int getIndexOf(Step step) {
		return steps.indexOf(step);
	}
	
	public ArrayList<Step> getAllSteps() {
		return steps;
	}
	
	public int getX(int index) {
		return getStep(index).getX();
	}

	
	public int getY(int index) {
		return getStep(index).getY();
	}
	
	
	public void appendStep(int x, int y) {
		steps.add(new Step(x, y));
	}

	public void appendStep(Step step) {
		steps.add(step);
	}
	
	public void prependStep(int x, int y) {
		steps.add(0, new Step(x, y));
	}
	
	public void removeStepAt(int index) {
		steps.remove(index);
	}
	
	
	public boolean contains(int x, int y) {
		return steps.contains(new Step(x,y));
	}


	public class Step {
		
		private int x;
		private int y;
		
		
		public Step(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
				
		public int getX() {
			return x;
		}

		
		public int getY() {
			return y;
		}
		
		
		public boolean equals(Object other) {
			if (other instanceof Step) {
				Step o = (Step) other;
				
				return (o.x == x) && (o.y == y);
			}
			
			return false;
		}
	}
	
	
}
