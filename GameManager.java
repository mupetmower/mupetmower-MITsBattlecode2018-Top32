import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import bc.Direction;
import bc.GameController;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;
import bc.Team;
import bc.Unit;
import bc.UnitType;
import bc.VecMapLocation;
import bc.VecUnit;
import bc.VecUnitType;

/*
 * 
 * Alex Frye - mupetmower
 */
public class GameManager {

	private int earthWidth;
	private int earthHeight;
	private PlanetMap earthStartMap;
	
	private int marsWidth;
	private int marsHeight;
	private PlanetMap marsStartMap;
	
	private MapLocation startingLoc;
	
	public static Tile earthTiles[][];
	public static Tile marsTiles[][];
	private AStar scout;
	
	public static GameController controller;
	
	private Team myTeam;
	private Team enemyTeam;
	
	Random random = new Random();
	
	private int startingWorkers = 0;
	private int totalWorkers = 0;
	
	private ArrayList<MapLocation> startingLocations = new ArrayList<MapLocation>();
	private ArrayList<MapLocation> symStartingLocations = new ArrayList<MapLocation>();
	private ArrayList<MapLocation> symStartingLocationsVisited = new ArrayList<MapLocation>();
	int symStartingLocationsVisitedTotal = 0;
	
	private ArrayList<MapLocation> startingLocationsMars = new ArrayList<MapLocation>();
	private ArrayList<MapLocation> symStartingLocationsMars = new ArrayList<MapLocation>();
	private ArrayList<MapLocation> symStartingLocationsVisitedMars = new ArrayList<MapLocation>();
	int symStartingLocationsVisitedTotalMars = 0;
	
	private final int factoryCost = 200;
	private final int rocketCost = 150;
	private final int replicateCost = 60;
	
	private int workerHarvestAmount = 3;
	
	
	int totalFactories = 0;
	int totalRocketsMade = 0;
	int totalRocketsSent = 0;
	
	int rocketOnEarthId = -1;
	Unit rocket;
	MapLocation rocketLoc;
	boolean rocketReady = false;
	
	int rocketTurns = 0;
	
	int totalFighters = 0;
	int totalRangers = 0;
	int totalMages = 0;
	
	int turnsOnMars = 0;
	
	int workersOnRocket = 0;
	
	int round = 0;
    int karbonite = 0;
    int totalUnits = 0;
			
    int totalStartingKarbonite = 0;
    
	//private ArrayList visibleUnits = new ArrayList();
	
	ArrayList<Integer> factoryIds = new ArrayList<Integer>();
	
	HashMap<Integer, Path> unitsWithPaths = new HashMap<Integer, Path>();
	HashMap<Integer, Path> healersWithPaths = new HashMap<Integer, Path>();
	HashMap<Integer, Path> workersWithPaths = new HashMap<Integer, Path>();
	
	private ArrayList<MapLocation> rocketLanding = new ArrayList<MapLocation>();
	private ArrayList<Integer> rocketsOnMars = new ArrayList<Integer>();
	
	boolean marsRep = false;
	
	private ArrayList<MapLocation> myFactoryLocs = new ArrayList<MapLocation>();
	private ArrayList<MapLocation> enemyFactoryLocs = new ArrayList<MapLocation>();
	
	
	
	public GameManager() {

		try {
			Init();
			MainLoop();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	private void Init() throws Exception{
    	try
    	{
    		//System.out.println("Starting Init()");
	    	controller = new GameController();
	    	
	    	//System.out.println("Enqueing rocket research");
	    	if (controller.planet() == Planet.Earth) {		    	
		    	controller.queueResearch(UnitType.Mage);
		    	controller.queueResearch(UnitType.Healer);
		    	controller.queueResearch(UnitType.Mage);
		    	controller.queueResearch(UnitType.Rocket);
		    	controller.queueResearch(UnitType.Mage);
		    	controller.queueResearch(UnitType.Healer);
		    	controller.queueResearch(UnitType.Ranger);
		    	controller.queueResearch(UnitType.Ranger);
		    	controller.queueResearch(UnitType.Ranger);
	    	}
	    	
	    		    	
	    	//System.out.println("Getting Teams");
	    	SetTeamVars();
	    		
	    	//System.out.println("Getting starting maps");    	
	    
	    	
	    
	    	
	    	
	    	if (controller.planet() == Planet.Earth) {
	    		getEarthMapValues();
	    		
		    	//System.out.println("Setting earthTiles");
		    	earthTiles = InitEarthTiles();	    	
		    	
		    	//System.out.println("Finding all passable terrain");
		    	findAllPassableEarth();
		    	
		    	//System.out.println("Finding all starting karbonite");
		    	findAllStartingKarbonite();
		    	
		    	
		    	
		    	
		    	startingWorkers = (int)controller.myUnits().size();
		    	//System.out.println("Starting Workers: " + startingWorkers);
		    	totalWorkers = startingWorkers;
		    	
		    	GetStartingLocations(controller.myUnits());
		    	
	    	}
	    	
	    	
    	
    		getMarsMapValues();
	    	//System.out.println("Setting marsTiles");
	    	marsTiles = InitMarsTiles();	    	
	    	
	    	//System.out.println("Finding all passable terrain");
	    	findAllPassableMars();
	    			    	
		  
	    	
	    	
	    	InitAStar();
    	
    	} catch (Exception ex) {
    		 //System.out.println("Problem in Init! -- " + ex.toString());
    		 ex.printStackTrace();
    	}
    }
    
    
    private void MainLoop() throws Exception{
    	////System.out.println("Starting Main Loop");
    	
    	//int firstFactoryID = -1;
    	
        
    	////System.out.println("Starting While Loop for Running State");
        while (true) {
        	
        	
        	//System.out.println("Current round: " + controller.round());     
            ////System.out.println("Current karbonite: " + controller.karbonite());  
            ////System.out.println("Current myUnits() size: " + controller.myUnits().size());
            
            //System.out.println("Time left: " + controller.getTimeLeftMs());
            	
   
        	
            
            System.runFinalization();
            System.gc();
            	
            
            VecUnit myUnits = controller.myUnits();
            if (controller.planet() == Planet.Earth) {
        		
        		
                totalUnits = (int)myUnits.size();
                
                if (round > 350 && totalUnits < 10 && !marsRep) {
                	VecUnitType r = controller.researchInfo().queue();
                	boolean check = false;
                	Check: for (int j = 0; j < r.size(); j++) {
                		if (r.get(j) == UnitType.Knight) {
                			check = true;
                			marsRep = true;
                			break Check;
                		}
                	}
                	
                	if (!check) {
                		controller.queueResearch(UnitType.Knight);
                	}
                	
                }
                
                if (controller.researchInfo().getLevel(UnitType.Worker) > 0) {
                	workerHarvestAmount = 4;
                }
        	} else {
        		 if (round > 350 && !marsRep) {
                 	VecUnitType r = controller.researchInfo().queue();
                 	
                 	Check: for (int j = 0; j < r.size(); j++) {
                 		if (r.get(j) == UnitType.Knight) {
                 			marsRep = true;
                 			break Check;
                 		}
                 	}
        		 }
        	}
            
            round = (int)controller.round();
            karbonite = (int)controller.karbonite();
            
            
            
            
            
            try {
            	
            	
            	if (round % 50 == 0) {
            		totalWorkers--;
            	}
            	
            	            	
            	////System.out.println("Getting Units");
                

                
	            for (int i = 0; i < myUnits.size(); i++) 
	            {
	            	Unit unit = myUnits.get(i);
	            	
	            	
	            	if (unit.location().isInSpace())
                		continue;
                	
                	if (unit.location().isInGarrison()) {	
                		continue;
                	}
                	
	            	
	            	UnitType unitType = unit.unitType();
	            	int unitId = unit.id();
	            	
	            	
	            	
	            	
	                if (unitType == UnitType.Worker) //If unit is worker
	                {
	                	if (round == 1 || round == 2 || round == 3) {
	                		replicateAnywhere(unitId);
	                		totalWorkers++;
	                	}
	                	
	                	if (unit.health() < 40) {
	                		totalWorkers--;
	                	}
	                	
	                	
	                	MapLocation unitLoc = unit.location().mapLocation();
	                	
//	                	VecUnit enemies = controller.senseNearbyUnitsByTeam(unitLoc, unit.visionRange()+50, enemyTeam);
//	                	
//	                	if (enemies.size() > 0) {
//	                		VecUnit allFactories = controller.senseNearbyUnitsByType(unitLoc, unit.visionRange(), UnitType.Factory);
//	            			ArrayList<Unit> factories = new ArrayList<Unit>();    			
//	            			for (int j = 0; j < allFactories.size(); j++) {
//	            				Unit f = allFactories.get(j);	//maybe change this to not create a new Unit object to hold reference
//	            				if (f.team() == myTeam)
//	            					if (f.structureIsBuilt() == 0) //only get UNBUILT factories
//	            						factories.add(f);
//	            			}
//	            			if (factories.size() == 0) {
//		                		Unit enemy = closestEnemyToUnit(unit, enemies);
//		                		tryMove(unitId, oppositeDir(unitLoc.directionTo(enemy.location().mapLocation())));
//	            			}
//	                	}
	                	
	                	if (unitLoc.getPlanet() == Planet.Earth) {
	                		if (!rocketReady) {
		                	//long msLeft = System.currentTimeMillis();
	            			////System.out.println("Time in ms before worker logic: " + msLeft); 
	            			
	                		
	            			NewWorkerLogic2(unit);
		                	
		                	//long msLeft2 = System.currentTimeMillis();
	                		////System.out.println("Time in ms after woker logic: " + msLeft2);  
	                		
	                		////System.out.println("WorkerLogic took " + (msLeft2 - msLeft)); 
	                		} else {
	                			//rocket ready
	                			int distSqrToRocket = distanceSquaredBetween(unitLoc, rocketLoc);
	                			if ((distSqrToRocket < 7 && totalWorkers > 3 && workersOnRocket < 1 && totalRocketsMade < 1) || (distSqrToRocket < 6 && round > 500) || totalUnits < 15) {
	                				if (distSqrToRocket <= 2) {
	                					if (controller.canLoad(rocketOnEarthId, unitId)) {
		                					controller.load(rocketOnEarthId, unitId);
	                						totalWorkers--;
	                						workersOnRocket++;
	                					}
	                				} else {
	                					if (!tryMove(unitId, unitLoc.directionTo(rocketLoc))) {
	                						Path newPath = findPathToLocation(unit, rocketLoc);
	                						if (!newPath.isAtEnd()) {
	                							workersWithPaths.put(unitId, newPath);
	                							takeNextStepWorker(unitId, unit.location().mapLocation());
	                						} else {
	                							tryMoveRandom(unitId);
	                						}
	                					}
	                					workersWithPaths.remove(unitId);
		                				
	                				}
	                			} else { //not close to rocket
	                				//long msLeft = System.currentTimeMillis();
	    	            			////System.out.println("Time in ms before worker logic: " + msLeft); 
	    	            			
	    	            			NewWorkerLogic2(unit);
	    		                	
	    		                	//long msLeft2 = System.currentTimeMillis();
	    	                		////System.out.println("Time in ms after woker logic: " + msLeft2);  
	    	                		
	    	                		////System.out.println("WorkerLogic took " + (msLeft2 - msLeft)); 
	                			}
	                		}
	                	} else {
	                		//mars
	                		if (karbonite > rocketCost + replicateCost || round > 600 || marsRep) {
	                			replicateAnywhere(unitId);
	                		}
	                		
	                		if (controller.getTimeLeftMs() < 200)
	                			continue;
	                		
	                		WorkerFindKarboniteMars(unit, unitId, unitLoc);
	                	}
	                	
	                
	                } else if (unitType == UnitType.Factory)
	                {
	                	
	                	if (unit.health() < 40) {
	                		totalFactories--;
	                	}
	                	//System.out.println("Factory --");
	                	
	                	//firstFactoryID = unit.id();
	                	if (controller.researchInfo().getLevel(UnitType.Rocket) > 0 && totalRocketsMade < 12 && karbonite <= rocketCost + 40) {
	                		continue;
	                	}
	                	
	                	if ((totalFactories <= 3 && karbonite < factoryCost + 40) || (totalWorkers < 5 && karbonite <= replicateCost + 40) || (round > 550 && karbonite < replicateCost))
	                		continue;
	                	
	                	
	                ////System.out.println("Testing if Unit is in garrison and max hp");
	                	if (unit.structureGarrison().size() > 0)
	                	{
	                		unloadUnitAnywhere(unitId);
	                	}
	                	
	                	
	                	////System.out.println("Testing ProduceRobot");
	                	if (controller.canProduceRobot(unitId, UnitType.Ranger))
	                	{
	                		if (totalFighters % 9 == 0) {
	                			controller.produceRobot(unitId, UnitType.Healer);
		                		totalFighters++;
	                		} else {
	                			if (totalFighters % 6 != 0) {
		                			////System.out.println("Producing Robot");
			                		controller.produceRobot(unitId, UnitType.Ranger);
			                		totalRangers++;
			                		totalFighters++;
		                		} else {
		                			controller.produceRobot(unitId, UnitType.Mage);
		                			totalMages++;
			                		totalFighters++;
		                		}
	                		}
	                		
	                	
	                		
	                	}
	                	
	                	
	                	
	                	                
	                } else if (unitType == UnitType.Ranger) {
	                	//System.out.println("Ranger --");
	                	
                	
                		MapLocation unitLoc = unit.location().mapLocation();
                		if (unitLoc.getPlanet() == Planet.Earth) {
	                		if (!rocketReady) {
                		
		                		if (totalFighters < 6 && controller.senseNearbyUnitsByTeam(unitLoc, unit.attackRange(), enemyTeam).size() == 0) {
		                			tryMoveRandom(unitId);
		                			continue;
		                		}
		                		
		                		if (controller.isMoveReady(unitId)) {
		                			//long msLeft = System.currentTimeMillis();
		                			////System.out.println("Time in ms before ranger logic: " + msLeft); 
		                				                			
		                			NewRangerSearchLogic2(unit);
		                			
		                			//long msLeft2 = System.currentTimeMillis();
		                    		////System.out.println("Time in ms after ranger logic: " + msLeft2);  
		                    		
		                    		////System.out.println("RangerLogic took " + (msLeft2 - msLeft));
		                		}
	                		} else { //get in rocket if close
	                			int distSqrToRocket = distanceSquaredBetween(unitLoc, rocketLoc);
	                			if ((distSqrToRocket < 40 && totalRocketsMade > 0) || (distSqrToRocket < 55 && round > 500) || totalUnits < 15) {
	                				
	                				if (distSqrToRocket <= 2) {
	                					if (controller.canLoad(rocketOnEarthId, unitId))
		                					controller.load(rocketOnEarthId, unitId);
	                				} else {
	                					if (unitsWithPaths.containsKey(unitId)) {
	                						Path currentPath = unitsWithPaths.get(unitId);
	                						if (currentPath.getEnd() != rocketLoc && !currentPath.isAtEnd()) {
	                							Path newPath = findPathToLocation(unit, rocketLoc);
		                						if (!newPath.isAtEnd()) {
		                							unitsWithPaths.put(unitId, newPath);
		                							takeNextStep(unitId, unitLoc);
		                							
		                						} else {
		                							//tryMoveRandom(unitId);
		                						}
	                						} else {
	                							takeNextStep(unitId, unitLoc);
	                						}
	                					} else {
		                					if (!tryMove(unitId, unitLoc.directionTo(rocketLoc))) {
		                						Path newPath = findPathToLocation(unit, rocketLoc);
		                						if (!newPath.isAtEnd()) {
		                							unitsWithPaths.put(unitId, newPath);
		                							takeNextStep(unitId, unitLoc);
		                							
		                						} else {
		                							//tryMoveRandom(unitId);
		                						}
		                					}
	                					}
	                					//unitsWithPaths.remove(unitId);
	                					
		                				
	                				}
	                			} else {
	                				//not close to rocket
	                				NewRangerSearchLogic2(unit);
	                			}
	                			
	                		}
                		} else { //mars
                			NewRangerSearchLogic2(unit);
                		}
	                	
	                			
	                } else if (unitType == UnitType.Mage) {
	                	//System.out.println("Mage --");
                	
                		MapLocation unitLoc = unit.location().mapLocation();
                		if (unitLoc.getPlanet() == Planet.Earth) {
	                		if (!rocketReady) {
                		
		                		if (totalFighters < 9 && controller.senseNearbyUnitsByTeam(unitLoc, unit.attackRange(), enemyTeam).size() == 0) {
		                			tryMoveRandom(unitId);
		                			continue;
		                		}
		                		
		                		if (controller.isMoveReady(unitId)) {
		                			//long msLeft = System.currentTimeMillis();
		                			////System.out.println("Time in ms before ranger logic: " + msLeft); 
		                				                			
		                			NewMageSearchLogic2(unit);
		                			
		                			//long msLeft2 = System.currentTimeMillis();
		                    		////System.out.println("Time in ms after ranger logic: " + msLeft2);  
		                    		
		                    		////System.out.println("RangerLogic took " + (msLeft2 - msLeft));
		                		}
	                		} else { //get in rocket if close
	                			int distSqrToRocket = distanceSquaredBetween(unitLoc, rocketLoc);
	                			unitsWithPaths.remove(unitId);
	                			if ((distSqrToRocket < 40 && totalRocketsMade > 0) || (distSqrToRocket < 55 && round > 500) || totalUnits < 15) {
	                				if (distSqrToRocket <= 2) {
	                					if (controller.canLoad(rocketOnEarthId, unitId))
		                					controller.load(rocketOnEarthId, unitId);
	                				} else {
	                					if (!tryMove(unitId, unitLoc.directionTo(rocketLoc))) {
	                						Path newPath = findPathToLocation(unit, rocketLoc);
	                						if (!newPath.isAtEnd()) {
	                							unitsWithPaths.put(unitId, newPath);
	                							takeNextStep(unitId, unitLoc);
	                							
	                						} else {
	                							//tryMoveRandom(unitId);
	                						}
	                					}
	                					unitsWithPaths.remove(unitId);
	                							                				
	                				}
	                			} else {
	                				//not close to rocket
	                				NewMageSearchLogic2(unit);
	                			}
	                			
	                		}
                		} else { //mars
                			NewMageSearchLogic2(unit);
                		}
	                	
	                	
	                } else if (unitType == UnitType.Healer) {
	                
	                	MapLocation unitLoc = unit.location().mapLocation();
                		if (unitLoc.getPlanet() == Planet.Earth) {
	                		if (!rocketReady) {
                		
		                		if (totalFighters < 16 && controller.senseNearbyUnitsByTeam(unitLoc, unit.attackRange(), enemyTeam).size() == 0) {
		                			tryMoveRandom(unitId);
		                			continue;
		                		}
		                		
		                		if (controller.isMoveReady(unitId)) {
		                			//long msLeft = System.currentTimeMillis();
		                			////System.out.println("Time in ms before ranger logic: " + msLeft); 
		                				                			
		                			HealerLogic1(unit, unitId);
		                			
		                			//long msLeft2 = System.currentTimeMillis();
		                    		////System.out.println("Time in ms after ranger logic: " + msLeft2);  
		                    		
		                    		////System.out.println("RangerLogic took " + (msLeft2 - msLeft));
		                		}
	                		} else { //get in rocket if close
	                			int distSqrToRocket = distanceSquaredBetween(unitLoc, rocketLoc);
	                			unitsWithPaths.remove(unitId);
	                			if ((distSqrToRocket < 35 && totalRocketsMade > 0) || (distSqrToRocket < 55 && round > 500) || totalUnits < 15) {
	                				if (distSqrToRocket <= 2) {
	                					if (controller.canLoad(rocketOnEarthId, unitId))
		                					controller.load(rocketOnEarthId, unitId);
	                				} else {
	                					if (!tryMove(unitId, unitLoc.directionTo(rocketLoc))) {
	                						Path newPath = findPathToLocation(unit, rocketLoc);
	                						if (!newPath.isAtEnd()) {
	                							unitsWithPaths.put(unitId, newPath);
	                							takeNextStep(unitId, unitLoc);
	                							
	                						} else {
	                							//tryMoveRandom(unitId);
	                						}
	                					}
	                					unitsWithPaths.remove(unitId);
	                							                				
	                				}
	                			} else {
	                				//not close to rocket
	                				HealerLogic1(unit, unitId);
	                			}
	                			
	                		}
                		} else { //mars
                			HealerLogic1(unit, unitId);
                		}
	                
	            	} else if (unitType == UnitType.Rocket) {
	                	

	                	MapLocation unitLoc = unit.location().mapLocation();
	                	//System.out.println("Rocket --");
	                
	                	if (unitLoc.getPlanet() == Planet.Earth) {
		                	if (unit.structureIsBuilt() > 0) {
		                		//broadcast that rocket is ready
		                		rocketReady = true;
		                		
		                		int garrison = (int)unit.structureGarrison().size();
		                	
		                		
		                		if (totalRocketsMade == 0 && garrison > 0) {
		                			MarsSearch: for (int x = 0; x < marsTiles.length; x++) {
		                				for (int y = 0; y < marsTiles[x].length; y++) {
		                					if (marsTiles[x][y].getIsPassable()) {
		                						if (!rocketLanding.contains(marsTiles[x][y].mapLoc)) {
			                						if (controller.canLaunchRocket(unitId, marsTiles[x][y].mapLoc)) {				                						
				                						rocketOnEarthId = -1;
				                						rocketLoc = null;
				                						rocket = null;
				                						rocketLanding.add(marsTiles[x][y].mapLoc);
				                						rocketReady = false;
				                						totalRocketsMade++;
				                						workersOnRocket = 0;
				                						controller.launchRocket(unitId, marsTiles[x][y].mapLoc);
				                						break MarsSearch;
				                					}
			                					}
		                					}
		                				}
		                			}
		                		} else {
		                			if (garrison > 6) {
		                				MarsSearch: for (int x = marsTiles.length - 1; x >= 0 ; x--) {
			                				for (int y = marsTiles[x].length - 1; y >= 0; y--) {
			                					if (marsTiles[x][y].getIsPassable()) {
			                						if (!rocketLanding.contains(marsTiles[x][y].mapLoc)) {				                						
				                						rocketOnEarthId = -1;
				                						rocketLoc = null;
				                						rocket = null;
				                						rocketLanding.add(marsTiles[x][y].mapLoc);
				                						rocketReady = false;
				                						totalRocketsMade++;
				                						workersOnRocket = 0;
				                						controller.launchRocket(unitId, marsTiles[x][y].mapLoc);
				                						break MarsSearch;
			                						}
			                					}
			                				}
		                				}
		                			}
		                		}
		                		
		                		
		                		if (unit.health() < unit.maxHealth() && garrison > 0) {
		                			MarsSearch: for (int x = marsTiles.length - 1; x >= 0 ; x--) {
		                				for (int y = marsTiles[x].length - 1; y >= 0; y--) {
		                					if (marsTiles[x][y].getIsPassable()) {
		                						if (!rocketLanding.contains(marsTiles[x][y].mapLoc)) {
			                						if (controller.canLaunchRocket(unitId, marsTiles[x][y].mapLoc)) {				                						
				                						rocketOnEarthId = -1;
				                						rocketLoc = null;
				                						rocket = null;
				                						rocketLanding.add(marsTiles[x][y].mapLoc);
				                						rocketReady = false;
				                						totalRocketsMade++;
				                						workersOnRocket = 0;
				                						controller.launchRocket(unitId, marsTiles[x][y].mapLoc);
				                						break MarsSearch;
				                					}
			                					}
		                					}
		                				}
		                			}
		                		}
		                		
		                		if (round - rocketTurns > 25) {
		                			if (garrison > 0) {
		                				MarsSearch: for (int x = marsTiles.length - 1; x >= 0 ; x--) {
			                				for (int y = marsTiles[x].length - 1; y >= 0; y--) {
			                					if (marsTiles[x][y].getIsPassable()) {
			                						if (!rocketLanding.contains(marsTiles[x][y].mapLoc)) {
				                						if (controller.canLaunchRocket(unitId, marsTiles[x][y].mapLoc)) {				                						
					                						rocketOnEarthId = -1;
					                						rocketLoc = null;
					                						rocket = null;
					                						rocketLanding.add(marsTiles[x][y].mapLoc);
					                						rocketReady = false;
					                						totalRocketsMade++;
					                						workersOnRocket = 0;
					                						controller.launchRocket(unitId, marsTiles[x][y].mapLoc);
					                						break MarsSearch;
					                					}
				                					}
			                					}
			                				}
		                				}
	                				
		                			} else {
		                				if (round < 550) {
		                					rocketOnEarthId = -1;
	                						rocketLoc = null;
	                						rocket = null;
	                						rocketReady = false;	                						
	                						workersOnRocket = 0;
		                					controller.disintegrateUnit(unitId);
		                				}
		                			}
		                		}
		                	} 
	                	} else { //mars
	                	
	                		int garrison = (int)unit.structureGarrison().size();
	                		if (garrison > 0) {
	                			if (!rocketsOnMars.contains(unitId)) {
	                				rocketsOnMars.add(unitId);
	                				symStartingLocationsMars.add(getSymmetricalLoc(unitLoc));
	                			}
	                			
	                			for (int j = 0; j < garrison; j++)
	                				unloadUnitAnywhere(unit.id());
	                		}
	                		
	                	}
	                }
	            }
	            
	            myUnits.delete();
	            
            } catch(Exception ex) {
            	 System.out.println("Problem in main loop! -- " + ex.toString());
            	 ex.printStackTrace();
            } 
            
            
            
            // Submit the actions we've done, and wait for our next turn.
            controller.nextTurn();
        }
    }
    
    
    private void HealerLogic1(Unit unit, int unitId) {
    	
    	if (controller.getTimeLeftMs() < 300)
    		return;
    	
    	try {
    		MapLocation unitLoc = unit.location().mapLocation();
    		
    		VecUnit nearbyAllies = controller.senseNearbyUnitsByTeam(unitLoc, unit.attackRange(), myTeam);
    		//ArrayList<Integer> nearbyHurtAllies = new ArrayList<Integer>();
    		
    		int lowestHpId = -1;
    		
    		if (nearbyAllies.size() > 0) {
    			if (nearbyAllies.size() == 1) {
    				Unit a = nearbyAllies.get(0);
    				if (a.health() < a.maxHealth() + unit.damage()) {
    					lowestHpId = a.id();    					
    				}
    			} else {
    				int lowestHp = 5000;    	    		
    	    		for (int i = 0; i < nearbyAllies.size(); i++) {
    	    			Unit a = nearbyAllies.get(i);
    	    			if (a.unitType() != UnitType.Factory && a.unitType() != UnitType.Rocket) {
    	    				if (a.health() < a.maxHealth() + unit.damage()) {
    	    					if (a.health() < lowestHp) {
    	    						lowestHpId = a.id();
    	    					}
    	    				}
    	    			}
    	    		}
    			}
    		}
    		
    		VecUnit enemies = controller.senseNearbyUnitsByTeam(unitLoc, unit.visionRange() + 50, enemyTeam);
			boolean enemyThreat = false;
			Unit enemy = null;
    		//if can see enemy
    		if (enemies.size() > 0) {
    			EnemySearch: for (int i = 0; i < enemies.size(); i++) {
    				UnitType t = enemies.get(i).unitType();
    				if (t == UnitType.Ranger || t == UnitType.Mage || t == UnitType.Knight) {
    					enemyThreat = true;
    					enemy = closestEnemyToUnit(unit, enemies);
    					break EnemySearch;
    				}
    			}
    			
    		}
    		
    		
    		if (lowestHpId != -1) {
    			//need to heal
    			tryHeal(unitId, lowestHpId);
    			if (enemyThreat) {    				
    				healersWithPaths.remove(unitId);
    				tryMoveFuzzy(unitId, oppositeDir(unitLoc.directionTo(enemy.location().mapLocation())));
    			}
    		} else {
    			if (enemyThreat) {    				
    				healersWithPaths.remove(unitId);
    				tryMoveFuzzy(unitId, oppositeDir(unitLoc.directionTo(enemy.location().mapLocation())));
    			} else {
	    			if (healersWithPaths.containsKey(unitId) && !healersWithPaths.get(unitId).isAtEnd()) {
	    				takeNextStepHealer(unitId, unitLoc);
	    			} else {
	    				healersWithPaths.remove(unitId);
						//System.out.println("Unit is too close to end of path.. getting new path to new location..");
						//get new Location from nextLocations(from symStartLocations())
						MapLocation newLoc;
						if (unitLoc.getPlanet() == Planet.Earth)
							newLoc = getNextLocation();
						else
							newLoc = getNextLocationMars(); 
						//System.out.println("Next location is " + newLoc);
						//Find path to this new location
						Path newPath = findPathToLocation(unit, newLoc);
						//System.out.println("Found new path to location");
						
						//assign path to unit
						if (!newPath.isAtEnd()) {
							healersWithPaths.put(unitId, newPath);
						
							//System.out.println("Taking step..");
	    					//Now unit should have a path.. take it's next step.
							takeNextStepHealer(unitId, unitLoc);
							//System.out.println("Took step");
						} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
	    			}
    			}
    		}
    		
    		
    		
    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    	
    }
    
    private void tryHeal(int unitId, int allyId) {
    	if (controller.isHealReady(unitId) && controller.canHeal(unitId, allyId)) {
    		controller.heal(unitId, allyId);
    	}
    }
    
    
    private void ReplicationLogic(int unitId, MapLocation closestKarbonite) {
    	int round = (int)controller.round();
    	int karbonite = (int)controller.karbonite();
    	
    	if (rocketReady && totalRocketsMade < 9 && karbonite <= rocketCost + replicateCost && totalWorkers > 4)
    		return;
    	
    	if (round > 20 && round < 150 && karbonite < factoryCost)
    		return;
    	
    	if (totalStartingKarbonite < 100) {
    		
    		
			if ((round < 12 && totalWorkers < 8 ) || (totalWorkers < 8 && totalFactories >= 4)) {
				if (replicateAnywhere(unitId))
					totalWorkers++;
			}
		} else {
			if ((round < 23 && totalWorkers < 11) || (totalWorkers < 11 && totalFactories >= 4)) {
				if (replicateAnywhere(unitId))
					totalWorkers++;
			}
		}
    }
    
    
    private Direction oppositeDir(Direction dir) {
    	switch (dir) {
    		case North:
    			return Direction.South;
    		case Northeast:
    			return Direction.Southwest;
    		case East:
    			return Direction.West;
    		case Southeast:
    			return Direction.Northwest;
    		case South:
    			return Direction.North;
    		case Southwest:
    			return Direction.Northeast;
    		case West:
    			return Direction.East;
    		case Northwest:
    			return Direction.Southeast;
    			default:
    				return Direction.Center;
    	}
    }
    
    private void NewWorkerLogic2(Unit unit) {
    	try {
    		int msLeft = controller.getTimeLeftMs();
        	if (msLeft < 400) {
        		//System.out.println("Only 400ms left.. skipping turn..");
        		return;
        	}
        	
        	resetAStarValues();
        	
        	int unitId = unit.id();
    		//System.out.println("Worker unitId: " + unitId);
    		MapLocation unitLoc = unit.location().mapLocation();
    		//System.out.println("Worker Location: " + unitLoc);
    		//int karbonite = (int)controller.karbonite();
    		//System.out.println("Karbonite: " + karbonite);
    		
    		MapLocation closestKarbonite = findClosestKarboniteLoc(unit, unitLoc);
    	
    		
    		//rocket research done.
    		if (controller.researchInfo().getLevel(UnitType.Rocket) > 0) {
    			
    			//replicate if needed
    			ReplicationLogic(unitId, closestKarbonite);
    			
    			if (rocketOnEarthId == -1) {
    				VecUnit nearby = controller.senseNearbyUnitsByTeam(unitLoc, 45, myTeam);
    				int nearbyAllies = 0;
    				AllyCheck: for (int n = 0; n < nearby.size(); n++) {
    					UnitType a = nearby.get(n).unitType();
    					if (a == UnitType.Factory || a == UnitType.Ranger || a == UnitType.Mage) {
    						nearbyAllies++;
    						break AllyCheck;
    					}
    				}
    				
    				if (totalRocketsMade < 1 || nearbyAllies > 0) {    				
	    				Direction dir = blueprintUnitAnywhereGetDir(unitId, UnitType.Rocket);
	    				if (dir != Direction.Center) {
	    					//blueprint worked get new rocketId and location
	    					rocketLoc = unitLoc.add(dir);
	    					rocket = controller.senseUnitAtLocation(rocketLoc);
	    					rocketOnEarthId = rocket.id(); 
	    					//totalRocketsMade++;
	    					rocketTurns = (int)controller.round();
	    				} else {
	    					//rocket blueprint didnt work.. get karbonite
	    					
	    					WorkerFindKarbonite(unit, unitId, unitLoc);
	    				}    				
    				} else {
    					
    					WorkerFindKarbonite(unit, unitId, unitLoc);
    					
    				}
    			} else { //rocket already on earth
    				if (controller.canSenseUnit(rocketOnEarthId)) {
	    				if (controller.unit(rocketOnEarthId).structureIsBuilt() == 0) {
	    					if (distanceSquaredBetween(unitLoc, rocketLoc) < 12) {
	    						if (distanceSquaredBetween(unitLoc, rocketLoc) <= 2) {
	    							if (tryBuild(unitId, rocketOnEarthId))
	    								rocketTurns = (int)controller.round();
	    						} else {
			    					
		    						if (!tryMove(unitId, unit.location().mapLocation().directionTo(rocketLoc))) {
	            						Path newPath = findPathToLocation(unit, rocketLoc);
	            						if (!newPath.isAtEnd()) {
	            							workersWithPaths.put(unitId, newPath);
	            							takeNextStepWorker(unitId, unit.location().mapLocation());
	            							if (tryBuild(unitId, rocketOnEarthId))
	            								rocketTurns = (int)controller.round();
	            						} else {
	            							//tryMoveRandom(unitId);
	            						}
	            					}
		    						if (tryBuild(unitId, rocketOnEarthId))
        								rocketTurns = (int)controller.round();
	    						}
	    					}
	    					
	    				} else { //rocket is built
	    					int distSqrToRocket = distanceSquaredBetween(unitLoc, rocketLoc);
                			if ((distSqrToRocket < 5 && totalWorkers > 5 && workersOnRocket < 1 && totalRocketsMade < 1) || (distSqrToRocket < 6 && round > 500) || totalUnits < 15) {
		    					if (distSqrToRocket <= 2) {
	            					if (controller.canLoad(rocketOnEarthId, unitId)) {
	                					controller.load(rocketOnEarthId, unitId);
	                					workersOnRocket++;
	            					}
	            				} else {
	            					if (!tryMove(unitId, unit.location().mapLocation().directionTo(rocketLoc))) {
	            						Path newPath = findPathToLocation(unit, rocketLoc);
	            						if (!newPath.isAtEnd()) {
	            							workersWithPaths.put(unitId, newPath);
	            							takeNextStepWorker(unitId, unit.location().mapLocation());
	            						} else {
	            							//tryMoveRandom(unitId);
	            							
	            						}
	            					}
	            				}
            				} else {
            					WorkerFindKarbonite(unit, unitId, unitLoc);
            				}
	    				}
    				} else {
    					WorkerFindKarbonite(unit, unitId, unitLoc);
    				}
    			}
    		} else { //rocket research not done
    			
    			//replicate if needed
    			ReplicationLogic(unitId, closestKarbonite);
    			
    			//get all factories on my team
    			VecUnit allFactories = controller.senseNearbyUnitsByType(unitLoc, unit.visionRange(), UnitType.Factory);
    			ArrayList<Unit> factories = new ArrayList<Unit>();    			
    			for (int i = 0; i < allFactories.size(); i++) {
    				Unit f = allFactories.get(i);	//maybe change this to not create a new Unit object to hold reference
    				if (f.team() == myTeam)
    					if (f.structureIsBuilt() == 0) //only get UNBUILT factories
    						factories.add(f);
    			}
    			
    			
    			if (controller.round() > 4 && totalFactories <= 4) {
    				//not enough factories
				
					//if (!blueprintUnitAnywhere(unitId, UnitType.Factory)) {
					//blueprint didnt work 
						if (factories.size() > 0) {
	    					//if closestFactory is pretty close AND isnt built already
	    					Unit closestFactory = getClosestFactoryToUnit(unitLoc, factories);
	        				MapLocation closestFactoryLoc = closestFactory.location().mapLocation();
	        				if (distanceSquaredBetween(unitLoc, closestFactoryLoc) < 25) {
	        					if (!tryBuild(unitId, closestFactory.id())) {
		    						//tryBuild clsoestFactory didnt work
		    						//if (!tryMove(unitId, unitLoc.directionTo(closestFactoryLoc))) {
		    							//tryMove in dir to closestFactory didnt work.. get path
		    							Path newPath = findPathToLocation(unit, closestFactoryLoc);
		    							if (newPath.getLength() > 0) {
		    								workersWithPaths.put(unitId, newPath);
		    								if (takeNextStepWorker(unitId, unitLoc))
		    									tryBuild(unitId, closestFactory.id());
		    							} else {
		    								//move random
		    								tryMoveRandom(unitId);
		    								tryBuild(unitId, closestFactory.id());
		    								tryHarvestAnywhere(unitId, unit);
		    							}
//		    						} else { //tryMove workers, tryBuild
//		    							tryBuild(unitId, closestFactory.id());
//		    						}
		    					}
	        				} else { //too far,b blueprint or get karbonite
	        					Direction dir = blueprintUnitAnywhereGetDir(unitId, UnitType.Factory);
	        					if (dir != Direction.Center) {
	        						totalFactories++;
	        						myFactoryLocs.add(unitLoc.add(dir));
	        					} else { //blueprint worked	            					
	            					WorkerFindKarbonite(unit, unitId, unitLoc);
	            				}
	        				}
						} else { //no nearby factories.. blueprint or find karbonite
							Direction dir = blueprintUnitAnywhereGetDir(unitId, UnitType.Factory);
        					if (dir != Direction.Center) {
        						totalFactories++;
        						myFactoryLocs.add(unitLoc.add(dir));
        					} else { //blueprint worked	            					
            					WorkerFindKarbonite(unit, unitId, unitLoc);
            				}
        				}
//    				} else { //blueprint worked
//    					totalFactories++;
//    				}
    					
    				
    			} else { //are enough factories
    				
    				//SAME AS ABOVE EXCEPT STARTS AT TRYBUILD()
    				
    				if (factories.size() > 0) {
	    				//if closestFactory is pretty close AND isnt built already
						Unit closestFactory = getClosestFactoryToUnit(unitLoc, factories);
	    				MapLocation closestFactoryLoc = closestFactory.location().mapLocation();
	    				if (distanceSquaredBetween(unitLoc, closestFactoryLoc) < 25) {
	    					if (!tryBuild(unitId, closestFactory.id())) {
	    						//tryBuild clsoestFactory didnt work
	    						//if (!tryMove(unitId, unitLoc.directionTo(closestFactoryLoc))) {
	    							//tryMove in dir to closestFactory didnt work.. get path
	    							Path newPath = findPathToLocation(unit, closestFactoryLoc);
	    							if (newPath.getLength() > 0) {
	    								workersWithPaths.put(unitId, newPath);
	    								if (takeNextStepWorker(unitId, unitLoc))
	    									tryBuild(unitId, closestFactory.id());
	    							} else {
	    								//move random
	    								tryMoveRandom(unitId);
	    							}
//	    						} else { //tryMove workers, tryBuild
//	    							tryBuild(unitId, closestFactory.id());
//	    						}
	    					}
	    				} else { //too far/already built, get karbonite
	    					WorkerFindKarbonite(unit, unitId, unitLoc);
	    				}
    				} else { //no factories near
    					WorkerFindKarbonite(unit, unitId, unitLoc);
    				}
    				
    				
    				//END SAME AS ABOVE
    			}
    			
    			
    		}
    		
    		
    		
    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    }
    
    private void WorkerFindKarbonite(Unit unit, int unitId, MapLocation unitLoc) throws Exception {
    	
    	int msLeft = controller.getTimeLeftMs();
    	if (msLeft < 300) {
    		//System.out.println("Only 400ms left.. skipping turn..");
    		return;
    	}
    	
    	VecUnit enemies = controller.senseNearbyUnitsByTeam(unitLoc, unit.visionRange(), enemyTeam);
	 	if (enemies.size() > 0) {        		
    		Unit enemy = closestEnemyToUnit(unit, enemies);
    		if (!tryMove(unitId, oppositeDir(unitLoc.directionTo(enemy.location().mapLocation())))) {
    			MapLocation closestKarbonite = findClosestKarboniteLoc(unit, unitLoc);
    	    	if (closestKarbonite == null) {
    	    		tryMoveRandom(unitId);
    	    		return;
    	    	}
    	    	if (!tryHarvestAnywhere(unitId, unit)) {
    	    		if (workersWithPaths.containsKey(unitId) && !workersWithPaths.get(unitId).isAtEnd()) {
    	    			//continue path
    	    			if (!takeNextStepWorker(unitId, unitLoc)) {
    	    				Path newPath = findPathToLocation(unit, closestKarbonite);
    	    				
    	    				if (newPath.getLength() > 0) {
    	    					workersWithPaths.put(unitId, newPath);
    		    				if (!takeNextStepWorker(unitId, unitLoc)) {
    		    					tryMoveRandom(unitId);
    		    				}
    		    				
    		    				tryHarvestAnywhere(unitId, unit);
    	    				} else {
    	    					tryMoveRandom(unitId);
    	    				}
    	    			}
    	    			//after step, tryHarvest
    	    			tryHarvestAnywhere(unitId, unit);
    	    		} else {
    	    			//trymove dir to closest
    	    			//if (!tryMove(unit, unitLoc.directionTo(closestKarbonite))) {
    	    				//couldnt move, get path
    	    				Path newPath = findPathToLocation(unit, closestKarbonite);
    	    				
    	    				if (newPath.getLength() > 0) {
    	    					workersWithPaths.put(unitId, newPath);
    	    					if (!takeNextStepWorker(unitId, unitLoc)) {
    		    					tryMoveRandom(unitId);
    		    				}
    		    				
    		    				tryHarvestAnywhere(unitId, unit);
    	    				} else {
    	    					tryMoveRandom(unitId);
    	    					tryHarvestAnywhere(unitId, unit);
    	    				}
    	//    			} else {//could move, tryHarvest anywhere(NOTE - might change to harvest at closestKarb loc
    	//    				tryHarvestAnywhere(unitId, unit);
    	//    			}
    	    			
    	    		}
    	    	}
    		}
    		tryHarvestAnywhere(unitId, unit);
    	} else {
    	
	    	MapLocation closestKarbonite = findClosestKarboniteLoc(unit, unitLoc);
	    	if (closestKarbonite == null) {
	    		tryMoveRandom(unitId);
	    		return;
	    	}
	    	if (!tryHarvestAnywhere(unitId, unit)) {
	    		if (workersWithPaths.containsKey(unitId) && !workersWithPaths.get(unitId).isAtEnd()) {
	    			//continue path
	    			if (!takeNextStepWorker(unitId, unitLoc)) {
	    				Path newPath = findPathToLocation(unit, closestKarbonite);
	    				
	    				if (newPath.getLength() > 0) {
	    					workersWithPaths.put(unitId, newPath);
		    				if (!takeNextStepWorker(unitId, unitLoc)) {
		    					tryMoveRandom(unitId);
		    				}
		    				
		    				tryHarvestAnywhere(unitId, unit);
	    				} else {
	    					tryMoveRandom(unitId);
	    				}
	    			}
	    			//after step, tryHarvest
	    			tryHarvestAnywhere(unitId, unit);
	    		} else {
	    			//trymove dir to closest
	    			//if (!tryMove(unit, unitLoc.directionTo(closestKarbonite))) {
	    				//couldnt move, get path
	    				Path newPath = findPathToLocation(unit, closestKarbonite);
	    				
	    				if (newPath.getLength() > 0) {
	    					workersWithPaths.put(unitId, newPath);
	    					if (!takeNextStepWorker(unitId, unitLoc)) {
		    					tryMoveRandom(unitId);
		    				}
		    				
		    				tryHarvestAnywhere(unitId, unit);
	    				} else {
	    					tryMoveRandom(unitId);
	    					tryHarvestAnywhere(unitId, unit);
	    				}
	//    			} else {//could move, tryHarvest anywhere(NOTE - might change to harvest at closestKarb loc
	//    				tryHarvestAnywhere(unitId, unit);
	//    			}
	    			
	    		}
	    	}
    	}
    }
    
    private void WorkerFindKarboniteMars(Unit unit, int unitId, MapLocation unitLoc) throws Exception {
    	MapLocation closestKarbonite = closestKarboniteInVisionMars(unitLoc, (int)unit.visionRange() - 10);
    	if (closestKarbonite == null) {
    		tryMoveRandom(unitId);	//NOTE - EVENTUALL CHANGE TO BUGMOVE
    		return;
    	}
    	if (!tryHarvestAnywhere(unitId, unit)) {
    		if (workersWithPaths.containsKey(unitId) && !workersWithPaths.get(unitId).isAtEnd()) {
    			//continue path
    			if (!takeNextStepWorker(unitId, unitLoc)) {
    				Path newPath = findPathToLocation(unit, closestKarbonite);
    				
    				if (newPath.getLength() > 0) {
    					workersWithPaths.put(unitId, newPath);
	    				if (!takeNextStepWorker(unitId, unitLoc)) {
	    					tryMoveRandom(unitId);
	    				}
	    				
	    				tryHarvestAnywhere(unitId, unit);
    				} else {
    					tryMoveRandom(unitId);
    				}
    			}
    			//after step, tryHarvest
    			tryHarvestAnywhere(unitId, unit);
    		} else {
    			//trymove dir to closest
    			//if (!tryMove(unit, unitLoc.directionTo(closestKarbonite))) {
    				//couldnt move, get path
    				Path newPath = findPathToLocation(unit, closestKarbonite);
    				
    				if (newPath.getLength() > 0) {
    					workersWithPaths.put(unitId, newPath);
    					if (!takeNextStepWorker(unitId, unitLoc)) {
	    					tryMoveRandom(unitId);
	    				}
	    				
	    				tryHarvestAnywhere(unitId, unit);
    				} else {
    					tryMoveRandom(unitId);
    					tryHarvestAnywhere(unitId, unit);
    				}
//    			} else {//could move, tryHarvest anywhere(NOTE - might change to harvest at closestKarb loc
//    				tryHarvestAnywhere(unitId, unit);
//    			}
    			
    		}
    	}
    }
    
    
    private MapLocation closestKarboniteInVisionMars(MapLocation unitLoc, int visionRange) {
    	VecMapLocation locs = controller.allLocationsWithin(unitLoc, visionRange);
    	ArrayList<MapLocation> locWithKarbonite = new ArrayList<MapLocation>();
    	for (int i = 0; i < locs.size(); i++) {    		
    		if (controller.karboniteAt(marsTiles[locs.get(i).getX()][locs.get(i).getY()].mapLoc) > 0) {
    			locWithKarbonite.add(locs.get(i));
    		}
    	}
    	
    	if (locWithKarbonite.isEmpty()) {
    		locs.delete();
    		return null;
    	}
    	
    	MapLocation closestLoc = locWithKarbonite.get(0);
    	int closestDist = 501;
    	for (MapLocation l : locWithKarbonite) {
    		int temp = distanceSquaredBetween(l, unitLoc); 
    		if (temp < closestDist) {
    			closestDist = temp;
    			closestLoc = l;
    		}
    	}
    	    	
    	locs.delete();
    	
    	return closestLoc;
    }
    
    
    private boolean tryHarvestAnywhere(int unitId, Unit unit) throws Exception {
    	if (tryHarvest(unitId, unit, Direction.Center)) {
    		return true;
    	} else if (tryHarvest(unitId, unit, Direction.Southeast)) {
    		return true;
    	} else if (tryHarvest(unitId, unit, Direction.South)) {
    		return true;
    	} else if (tryHarvest(unitId, unit, Direction.Southwest)) {
    		return true;
    	} else if (tryHarvest(unitId, unit, Direction.West)) {
    		return true;
    	} else if (tryHarvest(unitId, unit, Direction.Northwest)) {
    		return true;
    	} else if (tryHarvest(unitId, unit, Direction.North)) {
    		return true;
    	} else if (tryHarvest(unitId, unit, Direction.Northeast)) {
    		return true;
    	} else if (tryHarvest(unitId, unit, Direction.East)) {
    		return true;
    	}
    	return false;
    }
    
    
    private boolean tryHarvest(int unitId, Unit unit, Direction dir) throws Exception {
    	MapLocation unitLoc = unit.location().mapLocation();
    	
    	if (controller.canHarvest(unitId, dir)) {
			controller.harvest(unitId, dir);
			
			
			MapLocation harvestLoc = unitLoc.add(dir);
			int x = harvestLoc.getX();
			int y = harvestLoc.getY();
			
			
		
			if (unitLoc.getPlanet() == Planet.Earth)
				earthTiles[x][y].setInitKarbonite(earthTiles[x][y].getInitKarbonite() - workerHarvestAmount);
			
			return true;
		}
		return false;
	}
    
    
    private void tryMoveRandom(int unitId) throws Exception {
    	if (controller.isMoveReady(unitId)) {
    		//get random dir
    		int r = random.nextInt(7);
    		tryMoveAnywhereFromDir(unitId, r);
    	}
    	
    }
    
    
    private boolean tryMoveAnywhereFromDir(int unitId, int d) throws Exception {
    	int i2 = d;
    	for (int i = 0; i < dirs.length; i++) {
    		if (i2 == dirs.length) {
    			i2 = 0;
    		}
    		
    		if (tryMove(unitId, dirs[i2])) {
    			return true;
    		}
    		
    		i2++;
    	}
    	return false;
    }
    
    private Direction intToDir(int i) {
    	if (i >= 0 && i <= 7) {
    		return dirs[i];
    	}
    	return null;
    }
    
    private int dirToInt(Direction dir) {
    	switch (dir) {
	    	case North:
				return 0;
			case Northeast:
				return 1;
			case East:
				return 2;
			case Southeast:
				return 3;
			case South:
				return 4;
			case Southwest:
				return 5;
			case West:
				return 6;
			case Northwest:
				return 7;
			default:
				return 8;
    	}
    }
    
    private void NewWorkerLogic1(Unit unit) throws Exception {
    	int msLeft = controller.getTimeLeftMs();
    	if (msLeft < 300) {
    		//System.out.println("Only 300ms left.. skipping turn..");
    		return;
    	}
    	
    	resetAStarValues();
    	
    	int unitId = unit.id();
		//System.out.println("Worker unitId: " + unitId);
		MapLocation unitLoc = unit.location().mapLocation();
		//System.out.println("Worker Location: " + unitLoc);
		int karbonite = (int)controller.karbonite();
		//System.out.println("Karbonite: " + karbonite);
		
		MapLocation closestKarboniteLoc = findClosestKarboniteLoc(unit, unitLoc);
		//System.out.println("ClosestKarboniteLoc: " + closestKarboniteLoc);
    	
    	try {
    		Path currentPath = null;
    		int currentPathLength = 0;
    		int currentPathIndex = 0;
    		if (workersWithPaths.containsKey(unitId)) {
    			currentPath = workersWithPaths.get(unitId);
    			//System.out.println("Unit has path... ");
    			currentPathLength = currentPath.getLength();
    			//System.out.println("Path length: " + currentPathLength);
    			currentPathIndex = currentPath.getCurrentStepIndex();
    			//System.out.println("Path index: " + currentPathIndex);
    		}
    		
    		VecUnit allFactories = controller.senseNearbyUnitsByType(unitLoc, unit.visionRange(), UnitType.Factory);
			ArrayList<Unit> factories = new ArrayList<Unit>();
			//System.out.println("For allFactories in range, getting my Team's only");
			for (int i = 0; i < allFactories.size(); i++) {
				Unit f = allFactories.get(i);	//maybe change this to not create a new Unit object to hold reference
				if (f.team() == myTeam)
					factories.add(f);
			}
			//System.out.println("MyTeam Factories nearby unit: " + factories.size());
    		
    		
    		//System.out.println("Testing if rocket research complete");
    		//if rocket research is not complete
    		if (controller.researchInfo().getLevel(UnitType.Rocket) == 0) {
    			//System.out.println("Research is still not finished.. is there enough karbonite to build factory after replication");
    			//if there is enough karbonite to make a factory after replicating and there arent too many workers
        		if (karbonite > (factoryCost) && totalWorkers < startingWorkers * 2.5) {
        			//System.out.println("There is enough karbonite and not too many workers. replicating.");
        			//replicate anywhere
        			replicateAnywhere(unitId);        			
        			totalWorkers++;
        			//System.out.println("Replicated.. TotalWorkers: " + totalWorkers);
        		}
        		//System.out.println("Checking if not too many factories");
        		//if there arent too many factories, and there is enough karbonite
        		if (totalFactories <= startingWorkers && karbonite >= factoryCost) {
        			//System.out.println("Not too many factories.. and enough karbonite to build trying blueprinting");
        			//Blueprint a new factory anywhere
        			if (blueprintUnitAnywhere(unitId, UnitType.Factory)) {
        				totalFactories++;
        				//System.out.println("Blueprinted factory.. TotalFactories: " + totalFactories);
        			} else {
        				//System.out.println("Couldnt blueprint");
  //Maybe Add pathingtokarbonite      				
        			}
        		} else { //too many factories, or not enough karbonite
        			//System.out.println("Either too many factories, or not enoguh karbonite. Karbonite: " + karbonite);
        			//if factory near unit
        			if (factories.size() > 0) {
        				//System.out.println("Unit sees a factory");
        				//and factory is adjacent
        				Unit closestFactory = getClosestFactoryToUnit(unitLoc, factories);
        				MapLocation closestFactoryLoc = closestFactory.location().mapLocation();
        				if (distanceSquaredBetween(unitLoc, closestFactoryLoc) <= 2) {
        					//System.out.println("Unit is adjacent to closest on its team, trying to build.");
        					if (!tryBuild(unitId, closestFactory.id())) {
        						//System.out.println("Couldnt build.. checkign if has path.");
        						//since couldnt build, if has path, continue on path if not end 
        						if (workersWithPaths.containsKey(unitId) && currentPathIndex < currentPathLength) {
        							//System.out.println("Has path nad not near end.. taking step");
        							takeNextStepWorker(unitId, unitLoc);
        							MapLocation newUnitLoc = unitLoc.add(unitLoc.directionTo(new MapLocation(unitLoc.getPlanet(), currentPath.getX(currentPathIndex), currentPath.getY(currentPathIndex))));
        							//System.out.println("Next location after taking step: " + newUnitLoc);
        							if (distanceSquaredBetween(newUnitLoc, closestKarboniteLoc) <= 2) {
        								if (tryHarvest(unitId, unit, closestKarboniteLoc));
        									//System.out.println("Havested");
        							}		
        							if (distanceSquaredBetween(newUnitLoc, closestFactory) <= 2) {
        								if (tryBuild(unitId, closestFactory.id()));
        									//System.out.println("Built..");
        							}
        							
        						} else { // new path
        							//System.out.println("No path/near end of currentPath.. trying to move in dir of closestFactory");
        							//try to move to factory
        							
	        						if (!tryMove(unitId, unitLoc.directionTo(closestFactoryLoc))) {
	        							//System.out.println("Couldnt move in its dir, getting path to it");
	        							
	        							//If couldnt move towards that dir, get new Path to rocket
	        							Path newPath = findPathToLocation(unit, closestFactoryLoc);
	        							
	        							//System.out.println("Found path.. assigning to unit");
	        							//assignPath, take next step
	        							workersWithPaths.put(unitId, newPath);
	        							takeNextStepWorker(unitId, unitLoc);
	        							//System.out.println("Took next step.. tryBuild");
	        							//try to build, since too a step closer.
	        							if (tryBuild(unitId, rocketOnEarthId));
	        								//System.out.println("Built on it");
	        						} else {
	        							if (distanceSquaredBetween(new MapLocation(unitLoc.getPlanet(), currentPath.getX(currentPathIndex), currentPath.getY(currentPathIndex)), closestFactory) <= 2) {
	        								if (tryBuild(unitId, closestFactory.id()));
	        									//System.out.println("Built on it");
	        							}
	        						}
        						}
        					} //tryBuild workered
        				} else { //factory too far.. 
        					//get karbonite
        					WorkerGetClosestKarbonite(unit, unitId, unitLoc, closestKarboniteLoc, currentPathIndex, currentPathLength);
        				}        				
        			} else {//no nearby factory
        				//get karbonite
        				WorkerGetClosestKarbonite(unit, unitId, unitLoc, closestKarboniteLoc, currentPathIndex, currentPathLength);				
        			}        			
        		}        		
    		} else { //rocket research IS done
    			if (rocketOnEarthId == -1) { //no rocket on earth
    				//System.out.println("Rocket research done.. no rocket on earth, blueprinting");
    				Direction dir = blueprintUnitAnywhereGetDir(unitId, UnitType.Rocket);
    				if (dir != Direction.Center) {
    					//System.out.println("Blueprint worked");
    					//blueprint worked get new rocketId and location
    					rocketLoc = unitLoc.add(dir);
    					rocket = controller.senseUnitAtLocation(rocketLoc);
    					rocketOnEarthId = controller.senseUnitAtLocation(rocketLoc).id();
    					    					
    				} else { //blueprint didnt work.. go get karbonite
    					//System.out.println("Blueprint didnt work. getting karbonite");
    					WorkerGetClosestKarbonite(unit, unitId, unitLoc, closestKarboniteLoc, currentPathIndex, currentPathLength);					
    				}
    			} else {//rocket already on earth
    				//System.out.println("Research done, rocket already on earth.. seeing if near");
    				//if rocket is very near me, build on it if not complete
    				if (distanceSquaredBetween(unitLoc, rocketLoc) <= 4) {
    					//System.out.println("It is near unit");
    					if (rocket.structureIsBuilt() == 0) {
    						//System.out.println("And not built");
    						//rocket not built not built, try build
    						if (!tryBuild(unitId, rocketOnEarthId)) {
    							//System.out.println("Couldnt build on it..");
    							//couldnt build get path if dont have
    							if (workersWithPaths.containsKey(unitId) && currentPathIndex < currentPathLength) {
    								//System.out.println("Continueing on path");
    								takeNextStepWorker(unitId, unitLoc);
    								MapLocation newUnitLoc = new MapLocation(unitLoc.getPlanet(), currentPath.getX(currentPathIndex), currentPath.getY(currentPathIndex));
    								//System.out.println("took step on path.. new loc: " + newUnitLoc);
    								if (distanceSquaredBetween(newUnitLoc, rocketLoc) <= 2) {
    									//System.out.println("Close enough, tryBuild.");
    									tryBuild(unitId, rocketOnEarthId);
    								}
    							} else {
    								//get path to rocket
    								Path newPath = findPathToLocation(unit, rocketLoc);
    								
    								//if new path is not empty
    								if (newPath.getLength() > 0) {
    									//System.out.println("Path found, and not empty.. assigning and taking step.");
    									//assignPath, take next step
    									workersWithPaths.put(unitId, newPath);
    									takeNextStepWorker(unitId, unitLoc);
        								MapLocation newUnitLoc = new MapLocation(unitLoc.getPlanet(), currentPath.getX(currentPathIndex), currentPath.getY(currentPathIndex));
    									//System.out.println("Took step.. New loc: " + newUnitLoc);
    									//try to build if close enough
    									if (distanceSquaredBetween(newUnitLoc, closestKarboniteLoc) <= 2) {
    										//System.out.println("tryHarvest..");
    										tryBuild(unitId, rocketOnEarthId);
    									}
    								}
    							}
    						}//trybuild worked
    						
    					} else { //rocket is built, find karbonite
    						//System.out.println("But it's already built.. getting karboite");
        					WorkerGetClosestKarbonite(unit, unitId, unitLoc, closestKarboniteLoc, currentPathIndex, currentPathLength);
    					}
    				} else { //not close enough to roket
    					//System.out.println("Not close enough to rocket.. getting karbonite");
    					WorkerGetClosestKarbonite(unit, unitId, unitLoc, closestKarboniteLoc, currentPathIndex, currentPathLength);
    				}
    			}
    			
    		}
    		
    		
    		
    		
    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		
    	}
    	
    	
    	
    	
    }
    
    
    private void WorkerGetClosestKarbonite(Unit unit, int unitId, MapLocation unitLoc, MapLocation closestKarboniteLoc, int currentPathIndex, int currentPathLength) throws Exception {
    	try {
    		
        	resetAStarValues();

        	
        	
	    	//to harvest karbonite, continue on path or get new path
			if (workersWithPaths.containsKey(unitId) && currentPathIndex < currentPathLength) {
				//System.out.println("Unit has path and is not at end.. taking step.");
				Path currentPath = workersWithPaths.get(unitId);
				takeNextStepWorker(unitId, unitLoc);
				MapLocation newUnitLoc = new MapLocation(unitLoc.getPlanet(), currentPath.getX(currentPathIndex), currentPath.getY(currentPathIndex));

				//System.out.println("New location: " + newUnitLoc);
				if (distanceSquaredBetween(newUnitLoc, closestKarboniteLoc) <= 2) {
					tryHarvest(unitId, unit, closestKarboniteLoc);
				}
//				if (distanceSquaredBetween(newUnitLoc, closestFactory) <= 2) {
//					tryBuild(unitId, closestFactory.id());
//				}
			} else {
				//see if unit is close enough to harvest closestKarbonite
				if (!tryHarvest(unitId, unit, closestKarboniteLoc)) {
					//System.out.println("Could not harvest closest, tyrMove in dir ofclosest");
					//if not, try to move in direction of closestKarbonite
					//if unit cannot move in dir of closestKarboniteLoc
					if (!tryMove(unitId, unitLoc.directionTo(closestKarboniteLoc))) {
						//System.out.println("could not move in dir of closest, getting path to it");
						//get path to closestKarbonite
						Path newPath = findPathToLocation(unit, closestKarboniteLoc);
						
						//if new path is not empty
						if (newPath.getLength() > 0) {
							//System.out.println("Path found, and not empty.. assigning and taking step.");
							//assignPath, take next step
							workersWithPaths.put(unitId, newPath);
							takeNextStepWorker(unitId, unitLoc);
							MapLocation newUnitLoc = new MapLocation(unitLoc.getPlanet(), newPath.getX(currentPathIndex), newPath.getY(currentPathIndex));
							//System.out.println("New location: " + newUnitLoc);
							//try to harvest
							if (distanceSquaredBetween(newUnitLoc, closestKarboniteLoc) <= 2) {
								tryHarvest(unitId, unit, closestKarboniteLoc);
							}
						}
					} else { //tryMoveWorkerd trying to harvest
						//System.out.println("tryMoveWorkerd trying to harvest");
						if (distanceSquaredBetween(unit.location().mapLocation(), closestKarboniteLoc) <= 2) {
							tryHarvest(unitId, unit, closestKarboniteLoc);
						}
					}
				}
			}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    }

    
    private void WorkerLogic(Unit unit) throws Exception {
    	int msLeft = controller.getTimeLeftMs();
    	
    	
    	if (msLeft < 300) {
    		//System.out.println("Only 50ms left.. skipping turn..");
    		return;
    	}
    	
    	
    	
    	try {
    		int unitId = unit.id();
    		//System.out.println("Worker unitId: " + unitId);
    		MapLocation unitLoc = unit.location().mapLocation();
    		
    		int karbonite = (int)controller.karbonite();
    		//System.out.println("Karbonite: " + karbonite);
    		
    		MapLocation closestKarboniteLoc = findClosestKarboniteLoc(unit, unitLoc);
    		//System.out.println("ClosestKarboniteLoc: " + closestKarboniteLoc);
    		
    		//System.out.println("Testing if rocket research complete");
    		//if rocket research is not complete
    		if (controller.researchInfo().getLevel(UnitType.Rocket) == 0) {
    			//System.out.println("Research is still not finished.. is there enough karbonite to build factory after replication");
    			//if there is enough karbonite to make a factory after replicating and there arent too many workers
        		if (karbonite > (factoryCost) && totalWorkers < startingWorkers * 2.5) {
        			//System.out.println("There is enough karbonite and not too many workers. replicating.");
        			//replicate anywhere
        			replicateAnywhere(unitId);        			
        			totalWorkers++;
        			//System.out.println("Replicated.. TotalWorkers: " + totalWorkers);
        		}
        		
        		//System.out.println("Checking if not too many factories");
        		//if there arent too many factories, and there is enough karbonite
        		if (totalFactories <= startingWorkers && karbonite >= factoryCost) {
        			//System.out.println("Not too many factories.. and enough karbonite to build trying blueprinting");
        			//Blueprint a new factory anywhere
        			if (blueprintUnitAnywhere(unitId, UnitType.Factory)) {
        				totalFactories++;
        				//System.out.println("Blueprinted factory.. TotalFactories: " + totalFactories);
        			} else {
        				//System.out.println("Couldnt blueprint");
        			}
        		} else { //if there are already enough factories
        			//System.out.println("There are enough factories or not enough karbonite to build... can unit see a nearby factory? Getting all facctories");
        			//if unit can see nearby factories        			
        			VecUnit allFactories = controller.senseNearbyUnitsByType(unitLoc, unit.visionRange(), UnitType.Factory);
        			ArrayList<Unit> factories = new ArrayList<Unit>();
        			//System.out.println("For allFactories in range, getting my Team's only");
        			for (int i = 0; i < allFactories.size(); i++) {
        				Unit f = allFactories.get(i);	//maybe change this to not create a new Unit object to hold reference
        				if (f.team() == myTeam)
        					factories.add(f);
        			}
        			//System.out.println("MyTeam Factories nearby unit: " + factories.size());
        			if (factories.size() > 0) {
        				//System.out.println("Since there is at least 1, getting the closest.");
        				//find closest factory
        				Unit closestFactory = getClosestFactoryToUnit(unitLoc, factories);
        				
        				int closestFactoryId = closestFactory.id();
        				MapLocation closestFactoryLoc = closestFactory.location().mapLocation();
        				//System.out.println("Closest Factory: " + closestFactoryLoc);
        				//if closestFactory is not built,
        				if (closestFactory.structureIsBuilt() == 0) {
        					//System.out.println("Closest Factory is not fully built yet.. if unit is close enouhg(2 blocks), tryBuild.");
        					//if unit is close enough to factory to build (dist^2 <= 2)
        					if (distanceSquaredBetween(unitLoc, closestFactoryLoc) <= 2) {
        						//try to build
        						if (tryBuild(unitId, closestFactoryId));
        							//System.out.println("Was close enough.. built on it");
        					} else {
        						//System.out.println("Since not close enough to build, see if not too far away");
        						//since not close enough to build, try to move closer if it isnt too far away..
        						if (distanceSquaredBetween(unitLoc, closestFactoryLoc) < 2) {
	        							
	        						//System.out.println("Not too far, so see if unit has path already..");
	        						if (workersWithPaths.containsKey(unitId)) {
	        							//System.out.println("Unit DOES have path.. see if near end.");
	        							//since it has path.. continue if not near end
	        							Path currentPath = workersWithPaths.get(unitId);
	        							if (currentPath.getCurrentStepIndex() < currentPath.getLength() - 1) {
	        								//System.out.println("Not near end.. taking step.");
	        								takeNextStepWorker(unitId, unitLoc);
	        								//not sure if path was to factory or to karbonite.. so tryHarvest and tryBuild..
	        								//try to build, since too a step closer.
		        							if (tryBuild(unitId, closestFactoryId));
		        								//System.out.println("Built on factory");
		        							if (tryHarvest(unitId, unit, closestKarboniteLoc));
		        								//System.out.println("Harvest Closest Karbonite");
	        							} else { //since path at end, try Move
	        								//System.out.println("Path IS near end.. tryMove.");
	        								//try to move to factory
			        						if (!tryMove(unitId, unitLoc.directionTo(closestFactoryLoc))) {
			        							//System.out.println("Couldnt move in its dir, getting path to it");
			        							
			        							//If couldnt move towards that dir, get new Path to closestFactoryLoc
			        							Path newPath = findPathToFactory(unit, closestFactory);
			        							
			        							//System.out.println("Found path.. assigning to unit");
			        							//assignPath, take next step
			        							workersWithPaths.put(unitId, newPath);
			        							takeNextStepWorker(unitId, unitLoc);
			        							//System.out.println("Took next step.. tryBuild");
			        							//try to build, since too a step closer.
			        							if (tryBuild(unitId, closestFactoryId));
			        								//System.out.println("Built on it");
			        						} else { //since could move, tryBuild
			        							if (tryBuild(unitId, closestFactoryId));
			        								//System.out.println("Built on it");
			        							
			        						}
	        							}
	        						} else { //since no path..        							
	        							//try to move to factory
		        						if (!tryMove(unitId, unitLoc.directionTo(closestFactoryLoc))) {
		        							//System.out.println("Couldnt move in its dir, getting path to it");
		        							
		        							//If couldnt move towards that dir, get new Path to closestFactoryLoc
		        							Path newPath = findPathToFactory(unit, closestFactory);
		        							
		        							//System.out.println("Found path.. assigning to unit");
		        							//assignPath, take next step
		        							workersWithPaths.put(unitId, newPath);
		        							takeNextStepWorker(unitId, unitLoc);
		        							//System.out.println("Took next step.. tryBuild");
		        							//try to build, since too a step closer.
		        							if (tryBuild(unitId, closestFactoryId));
		        								//System.out.println("Built on it");
		        						}
	        						}
        						} else { //it is too faar, look for karbonite
        							//System.out.println("Too far.. goona get karbonite instead.");
        							WorkerClosestKarboniteLogic(unit, unitId, unitLoc, closestKarboniteLoc);
        						}
        						
        					}
        				} else { //since closest factory to unit IS built,
        					//System.out.println("Closest Factory IS built.. finding closest karbonite");
        					//start looking for karbonite
        					        					        					
        					//if unit has a path already
        					WorkerClosestKarboniteLogic(unit, unitId, unitLoc, closestKarboniteLoc);
        					
        				}
        			} else {	//since unit can NOT see any factories,
        				//System.out.println("Unit can NOT see any factories.. start collecting and finding karbonite");
        				//Start finding/collecting karbonite
    					//find the closest karbonite to unit
    					
    					//Check if unit has path.. if not get new one
    					WorkerClosestKarboniteLogic(unit, unitId, unitLoc, closestKarboniteLoc);
        			}
        			
        			
        		}
        		
        		
        	} else { //Since rocket research is finished!! -- make a rocket or collect karbonite
        		//System.out.println("Rocket research IS finished! checking if rocket on earth");
        		//if a rocket is not on earth,
        		if (rocketOnEarthId == -1) {
        			//System.out.println("Rocket is on earth, seeing if enoguh karbonite");
        			//if there is enough karbonite
        			if (karbonite > rocketCost) {
        				//System.out.println("There is enough karbonite, tring to blueprint");
        				//blueprint rocket
        				Direction newRocketDir = blueprintUnitAnywhereGetDir(unitId, UnitType.Rocket);
        				//if blueprint worked, assign rocketOnEarthId
        				if (newRocketDir != Direction.Center) {
        					//System.out.println("Blueprint workrked.. dir to newRocket is: " + newRocketDir + ", getting id....");
        					//assigning rockerOnearthId
        					rocketOnEarthId = controller.senseUnitAtLocation(unitLoc.add(newRocketDir)).id();  
        					//System.out.println("New Rocket Id is: " + rocketOnEarthId);
        				}
        			} else { //not enough karbonite to blueprint
        				//System.out.println("Not enough karbonite to blueprint.. lookign for closest.");
        				//start looking for karbonite
        				WorkerClosestKarboniteLogic(unit, unitId, unitLoc, closestKarboniteLoc);
        			}
        		} else { //since there IS a rocket already on earth...
        			//System.out.println("There IS a rocket on earth already");
        			//System.out.println("RocketID: " + rocketOnEarthId);
        			//if unit can sense a rocket
        			VecUnit allRockets = controller.senseNearbyUnitsByType(unitLoc, unit.visionRange(), UnitType.Rocket);
        			
        			if (allRockets.size() > 0) {
        				ArrayList<Unit> rockets = new ArrayList<Unit>();
        				//if rocket is on myTeam
        				for (int i = 0; i < allRockets.size(); i++) {
        					if (allRockets.get(i).team() == myTeam) {
        						rockets.add(allRockets.get(i));
        					}
        				}
        				
        				//if unit does sense nearby rocket on team,
        				if (rockets.size() > 0) {
        					
        					Unit rocket = rockets.get(0);
        					
        					MapLocation rocketLoc = rocket.location().mapLocation();
                			
                				
        					//System.out.println("Rocket is near and on myTeam..");
	        				//rocket nearby is on team.. getting location
	        				
	        				//System.out.println("Rocket Location: " + rocketLoc);
	        				//if it is built.. 
	        				if (rocket.structureIsBuilt() < 0) {
	        					//System.out.println("Rocket is NOT fully built.. checking if close (5 tiles)");
	        					//if rocket is pretty close to unit.. for now trying 5 blocks away
	                			if (distanceSquaredBetween(unitLoc, rocketLoc) < 5) {
	                				//System.out.println("Unit is close enough.. distanceSqurdBetween: " + distanceSquaredBetween(unitLoc, rocketLoc) + ".. trying to build..");
	                				//try to build
	                				if (!tryBuild(unitId, rocketOnEarthId)) {
	                					//System.out.println("Couldnt build.. tryMove in dir if no path already");
	                					if (workersWithPaths.containsKey(unitId)) {
		        							//System.out.println("Unit DOES have path.. see if near end.");
		        							//since it has path.. continue if not near end
		        							Path currentPath = workersWithPaths.get(unitId);
		        							if (currentPath.getCurrentStepIndex() < currentPath.getLength() - 1) {
		        								//System.out.println("Not near end.. taking step.");
		        								takeNextStepWorker(unitId, unitLoc);
		        								//not sure if path was to factory or to karbonite.. so tryHarvest and tryBuild..
		        								//try to build, since too a step closer.
			        							if (tryBuild(unitId, rocketOnEarthId));
			        								//System.out.println("Built on rocket");
			        							if (tryHarvest(unitId, unit, closestKarboniteLoc));
			        								//System.out.println("Harvest Closest Karbonite");
		        							}
		        						} else { //since no path..        							
		        							//try to move to rocket
			        						if (!tryMove(unitId, unitLoc.directionTo(rocketLoc))) {
			        							//System.out.println("Couldnt move in its dir, getting path to it");
			        							
			        							//If couldnt move towards that dir, get new Path to rocket
			        							Path newPath = findPathToLocation(unit, rocketLoc);
			        							
			        							//System.out.println("Found path.. assigning to unit");
			        							//assignPath, take next step
			        							workersWithPaths.put(unitId, newPath);
			        							takeNextStepWorker(unitId, unitLoc);
			        							//System.out.println("Took next step.. tryBuild");
			        							//try to build, since too a step closer.
			        							tryBuild(unitId, rocketOnEarthId);
			        							//System.out.println("Built on it");
			        						} 
		        						}
	                					
	                				}
	                			} else { // since distance to rocket is pretty far away, lets find some karbonite!
	                				//System.out.println("Dist to rocket is pretty far away.. collecting karbonite");
	                				//Start finding/collecting karbonite
	                				WorkerClosestKarboniteLogic(unit, unitId, unitLoc, closestKarboniteLoc);
    	        				}
            				} else { //since rocket is fully built..
            					//System.out.println("Rocket IS fully built.... collecting karbonite");
                				//Start finding/collecting karbonite
            					WorkerClosestKarboniteLogic(unit, unitId, unitLoc, closestKarboniteLoc);
            					
            				}
                			        			
        				
	        			} else { //since cant sense any rockets on my team, but there is a rocket supposed to be on Earth.. 
	        					//someone else must have just blueprinted one and its same turn maybe
	        				
	        				//System.out.println("Cant sense nearby rockets on my team.. going to find karbonite");
	        				//Lets go find karbonite..
	        				WorkerClosestKarboniteLogic(unit, unitId, unitLoc, closestKarboniteLoc);
	        				
	        			}
        			} else { //since I am not near the rocket on earth
        				//System.out.println("Unit is NOT nearby any rocket,... looking for karbonite");
        				//Lets go find karbonite..
        				WorkerClosestKarboniteLogic(unit, unitId, unitLoc, closestKarboniteLoc);
        			}
        			
        		}
        		
        	}
    		
    		
    		
    	} catch (Exception ex) {
    		//System.out.println("Problem in Worker Logic");
    		ex.printStackTrace();
    	}
    }
    
    
    private void WorkerClosestKarboniteLogic(Unit unit, int unitId, MapLocation unitLoc, MapLocation closestKarboniteLoc) throws Exception {
    	Path newPath = null;
		
		if (workersWithPaths.containsKey(unitId)) {
			Path currentPath = workersWithPaths.get(unitId);
			//System.out.println("Unit alreay has a path, though.. testing if near end");
			if (currentPath.getCurrentStepIndex() < currentPath.getLength() - 1) {
				//System.out.println("Not near end of path, so taking step..");
				//continue on path
				takeNextStepWorker(unitId, unitLoc);
				//System.out.println("Took step.. since path it has SHOULD already be to closest karbonite, tryHarvest");
				//try to harvest
				if (tryHarvest(unitId, unit, closestKarboniteLoc));
					//System.out.println("Harvested closest");
			} else {
				//System.out.println("Since path is at end, trying to harvest closest karbonite.");
				//check if can harvest closestKarbonite
				if (!tryHarvest(unitId, unit, closestKarboniteLoc)) {
					//System.out.println("could not harvest closest, trying to move in dir of closest");
					//if not, try to move in direction of closestKarbonite
					//if unit cannot move in dir of closestKarboniteLoc
					if (!tryMove(unitId, unit.location().mapLocation().directionTo(closestKarboniteLoc))) {
						//System.out.println("Couldnt move to dir of closest, either.. getting new path to closest karbonite");
							
						//get path to closestKarbonite
						newPath = findPathToLocation(unit, closestKarboniteLoc);
						
						//if new path is not empty
						if (newPath.getLength() > 0) {
							//System.out.println("Path found, and not empty.. assigning and taking step.");
							//assignPath, take next step
							workersWithPaths.put(unitId, newPath);
							takeNextStepWorker(unitId, unitLoc);
							//System.out.println("Took step.. tryHarvest since closeer to it");
							//try to harvest
							if (tryHarvest(unitId, unit, closestKarboniteLoc));
								//System.out.println("Harvested closest");
						}
						
					} else { //tryMoveWorkerd trying to harvest
						//System.out.println("tryMoveWorkerd trying to harvest");
						if (tryHarvest(unitId, unit, closestKarboniteLoc));
							//System.out.println("Harvested closest");
					}
				} 
			}
		} else {
			//System.out.println("Since unit has no path, tryHarvest closest");
			//since unit has no path,
			//see if unit is close enough to harvest closestKarbonite
			if (!tryHarvest(unitId, unit, closestKarboniteLoc)) {
				//System.out.println("Could not harvest closest, tyrMove in dir ofclosest");
				//if not, try to move in direction of closestKarbonite
				//if unit cannot move in dir of closestKarboniteLoc
				if (!tryMove(unitId, unitLoc.directionTo(closestKarboniteLoc))) {
					//System.out.println("could not move in dir of closest, getting path to it");
					//get path to closestKarbonite
					newPath = findPathToLocation(unit, closestKarboniteLoc);
					
					//if new path is not empty
					if (newPath.getLength() > 0) {
						//System.out.println("Path found, and not empty.. assigning and taking step.");
						//assignPath, take next step
						workersWithPaths.put(unitId, newPath);
						takeNextStepWorker(unitId, unitLoc);
						//System.out.println("Took step.. tryHarvest since closeer to it");
						//try to harvest
						if (tryHarvest(unitId, unit, closestKarboniteLoc));
							//System.out.println("Harvested closest");
					}
				} else { //tryMoveWorkerd trying to harvest
					//System.out.println("tryMoveWorkerd trying to harvest");
					if (tryHarvest(unitId, unit, closestKarboniteLoc));
						//System.out.println("Harvested closest");
				}
			}
		}
    }
    
    
    
    
    
    
   
    
    private ArrayList<Integer> enemiesOnBlast = new ArrayList<Integer>();
    
    
    private void NewRangerSearchLogic1(Unit unit) throws Exception {
    	
    	int msLeft = controller.getTimeLeftMs();
    	
    	
    	if (msLeft < 200) {
    		//System.out.println("Only 50ms left.. skipping turn..");
    		return;
    	}
    	
    	resetAStarValues();
    	try {
    		//System.out.println("Unit: " + unit.id());	
    		
    		int unitId = unit.id();
    		MapLocation unitLoc = unit.location().mapLocation();
    		Planet planet = unitLoc.getPlanet();
    		//System.out.println("Unit location is: " + unitLoc);
    		//System.out.println("Checking if visiting location in list");
    		if (planet == Planet.Earth) {
    			checkIfVisitingLocation(unit);
    		} else {
    			checkIfVisitingLocationMars(unit);
    		}
    		
    		
    		VecUnit enemies = controller.senseNearbyUnitsByTeam(unitLoc, unit.visionRange(), enemyTeam);
			
    		//if can see enemy
    		if (enemies.size() > 0) {
    			
    			for (int i = 0; i < enemiesOnBlast.size(); i++) {
    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    						enemiesOnBlast.remove(i);
    					return;
    				}
    			}
    			
    			
    			//System.out.println("Enemies seen: " + enemies.size() + " by unit: " + unit.id());
    			//if enemy is in attack range
    			Unit enemy = closestEnemyToUnit(unit, enemies);
    			int enemyId = enemy.id();
    			MapLocation enemyLoc = enemy.location().mapLocation();
    			//System.out.println("Closest Enemy is at " + enemy.location().mapLocation());
    			int distance = distanceSquaredBetween(unitLoc, enemyLoc);
    			//System.out.println("Distance Squared Between unit " + unit.id() + " and enemy is " + distance);
    			if (distance <= unit.attackRange()) {
    				//if enemy isnt too close to ranger
    				if (distance > unit.rangerCannotAttackRange()) {
    					enemiesOnBlast.add(enemyId);
    					//System.out.println("Enemy in range.. Attacking..");
    					for (int i = 0; i < enemiesOnBlast.size(); i++) {
    	    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    	    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    	    						enemiesOnBlast.remove(i);
    	    					return;
    	    				}
    	    			}
    					
    					
//    					tryMove(unitId, oppositeDir(unitLoc.directionTo(enemy.location().mapLocation())));
    					//System.out.println("Attacked enemy.");
    				} else {
    					//need to back up
    					//System.out.println("Too close to enemy.");
    					
    					if (tryMove(unitId, oppositeDir(unitLoc.directionTo(enemyLoc)))) {
    						//if (distance >= unit.rangerCannotAttackRange())

    						for (int i = 0; i < enemiesOnBlast.size(); i++) {
    		    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    		    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    		    						enemiesOnBlast.remove(i);
    		    					return;
    		    				}
    		    			}
	    					
    					
    						//}
    					}
    				}
    			} else {	//if enemy is not in attack range,
    				//if unit has path
    				//System.out.println("Enemy not in range");
    				if (unitsWithPaths.containsKey(unitId)) {
    					//System.out.println("Unit DOES have a path...");
    					//if path is pathToEnemy(or just a path to a unit in general)
    					Path currentPath = unitsWithPaths.get(unitId);
    					if (currentPath.getToUnitId() == enemyId) {
    						//System.out.println("Path is to an enemy/unit - taking step");
    						//leaving as just if(path is to a unit) take step because this means it probably has a current "mission"
    						//and if path is not near end
    						if (currentPath.getCurrentStepIndex() < currentPath.getLength() && distanceBetween(unit, PathStepToMapLoc(planet, currentPath, currentPath.getLength() - 1)) > 3) {
    	    					//System.out.println("Not too close to end of path.. taking step.");
	    						//take step closer to unit(probably enemy, but maybe rocket, later on)
	    						takeNextStep(unitId, unitLoc);
	    						//System.out.println("Took step...");
	    						return;
    						} else {
    							if (msLeft < 500) {
    								//System.out.println("Only 500ms left.. skipping turn..");
    					    		return;
    					    	}
    							unitsWithPaths.remove(unitId);
    							//since path is near end, need new path to enemy
    							//System.out.println("Too close to end of path.. getting new path to enemy.");
    							Path newPath = findPathToEnemy(unit, enemy);
    							//System.out.println("Found path to enemy");
    							//assign path to unit
        						unitsWithPaths.put(unitId, newPath);
        						
        						if (!newPath.isAtEnd()) {
        							takeNextStep(unitId, unitLoc);
        							//System.out.println("Took step");	
            					} else {
        							//unitsWithPaths.remove(unitId);
        							//tryMoveRandom(unitId);
        						}
        						
        						//If close enough to enemy after taking step, try to attack.
        						//if (distance <= unit.attackRange()) {
        							//System.out.println("Enemy is in range after taking that step...");
        							//Unit took a step closer to enemy, try to attack
        						for (int i = 0; i < enemiesOnBlast.size(); i++) {
        		    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
        		    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
        		    						enemiesOnBlast.remove(i);
        		    					return;
        		    				}
        		    			}
    	    					
    	    						//System.out.println("Attacked!");
        						//}
    						}
    					} else {	//If unit's path is not to a unit
    						if (msLeft < 500) {
    							//System.out.println("Only 500ms left.. skipping turn..");
    				    		return;
    				    	}
    						//System.out.println("Unit's path is not to an enemy/unit.. ");
    						//get a new path to the spotted enemy
    						Path newPath = null;
    						//System.out.println("Checking if any nearby units already have a path to enemy");
    						PathIteration: for (Path path : unitsWithPaths.values()) {	//for each path currently in use
    							//if the path is to the spotted enemy
    							if (path.getToUnitId() == enemyId) {
    								//System.out.println("Found a nearby unit with a path to enemy.. getting closest step.");
    								//get the step closest to me in the path,
    								Path.Step closestStep = getClosestStepToUnit(unitLoc, path);
    								//AStar to that step,
    								MapLocation m = new MapLocation(planet, closestStep.getX(), closestStep.getY());

    								//System.out.println("Closest step is at: " + m + "..... getting path to that step..");
    								newPath = findPathToLocation(unit, m);
    								
    								//Append all steps from that step to end onto recently found path
    								int closestStepIndex = path.getIndexOf(closestStep);
    								//System.out.println("Appending step from index " + closestStepIndex);
    								for (int i = closestStepIndex + 1; i < path.getLength(); i++) {
    									newPath.appendStep(path.getStep(i));
    									//System.out.println("Appended step " + i);
    								}
    								
    								//newPath.resetStepIndex();
    								//Assign path to unit
    		    					unitsWithPaths.put(unitId, newPath);
    								break PathIteration;
    							}
    						}
    						//If the currently used paths do not go to enemy,
    						if (newPath == null) {
    							//System.out.println("No nearby units had a path to enemy.. finding own path..");
    							//Find a new path to enemy
    							newPath = findPathToEnemy(unit, enemy);
    							//System.out.println("Found path to enemy");
    							//assign path to unit
        						unitsWithPaths.put(unitId, newPath);
    						}
    						
    						//System.out.println("Taking step");
    						//Now unit should have a path.. take it's next step.
    						if (!newPath.isAtEnd()) {
    							takeNextStep(unitId, unitLoc);
    							//System.out.println("Took step");	
        					} else {
    							//unitsWithPaths.remove(unitId);
    							//tryMoveRandom(unitId);
    						}
    						
    						//If close enough to enemy after taking step, try to attack.
    						//if (distance <= unit.attackRange()) {
    							//System.out.println("Enemy is in range after taking that step...");
    							//Unit took a step closer to enemy, try to attack
    						for (int i = 0; i < enemiesOnBlast.size(); i++) {
    		    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    		    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    		    						enemiesOnBlast.remove(i);
    		    					return;
    		    				}
    		    			}
	    						//System.out.println("Attacked!");
    						//}
    					}
    				} else {
    					//Since this unit has no path, find new Path to new Location
    					if (msLeft < 500) {
    						//System.out.println("Only 500ms left.. skipping turn..");
    			    		return;
    			    	}
    					//System.out.println("Unit does NOT have path..");
    					//Look around and see if unit can see any other units that already have paths
    					Path newPath = null;
    					//System.out.println("Checking if any nearby units have a path already..");
    					PathIteration: for (int unitWithPathId : unitsWithPaths.keySet()) {
    						//if unit can see the unit with a path and their path is not at end
    						if (!unitsWithPaths.get(unitWithPathId).isAtEnd()) {
	    						if (controller.canSenseUnit(unitWithPathId)) {
	    							//System.out.println("A nearby unit does have a path.. getting closest step to unit");
	    							//get the closest step to unit
	    							Path path = unitsWithPaths.get(unitWithPathId);
	    							Path.Step closestStep = getClosestStepToUnit(unitLoc, path);
									
									//AStar to that step,
									MapLocation m = new MapLocation(planet, closestStep.getX(), closestStep.getY());
									//System.out.println("Closest step is at: " + m + "..... getting path to that step..");
									newPath = findPathToLocation(unit, m);
									
									//Append all steps from that step to end onto recently found path
									int closestStepIndex = path.getIndexOf(closestStep);
									//System.out.println("Appending step from index " + closestStepIndex);
									
									for (int i = closestStepIndex + 1; i < path.getLength(); i++) {
										newPath.appendStep(path.getStep(i));
	
	    								//System.out.println("Appended step " + i);
									}
	
									//newPath.resetStepIndex();
									//assign path to unit
			    					unitsWithPaths.put(unitId, newPath);
									break PathIteration;
	    						}
	    						
	    					}
    					}
    					//If no unit around had a path/no units are around
    					if (newPath == null) {
							//System.out.println("No nearby units had a path... finding new path to new location..");
    						unitsWithPaths.remove(unitId);
    						//get new Location from nextLocations(from sysStartLocations())
    						MapLocation newLoc;
    						if (planet == Planet.Earth)
    							newLoc = getNextLocation();
    						else
    							newLoc = getNextLocationMars();
    						//System.out.println("Next location is " + newLoc);
    						//Find path to this new location
    						newPath = findPathToLocation(unit, newLoc);
    						//System.out.println("Found new path to location");
    						
    						//assign path to unit
							unitsWithPaths.put(unitId, newPath);
    					}
    					
    					//System.out.println("Taking step..");
    					//Now unit should have a path.. take it's next step.
    					if (!newPath.isAtEnd()) {
							takeNextStep(unitId, unitLoc);
							//System.out.println("Took step");	
    					} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				}    				
    			}
    		} else {
    			if (msLeft < 500) {
    				//System.out.println("Only 500ms left.. skipping turn..");
    	    		return;
    	    	}
    			//System.out.println("Saw no enemies nearby.. checking if unit has a path already");
    			//Since unit can not see any enemies, check if they have a path
    			if (unitsWithPaths.containsKey(unitId)) {
    				//System.out.println("Unit DOES have a path.. checkign to see if it is near the end.");
    				Path currentPath = unitsWithPaths.get(unitId);
    				//if unit has a path, and is not too close to the end of that path
    				if (currentPath.getCurrentStepIndex() < currentPath.getLength() && distanceBetween(unit, PathStepToMapLoc(planet, currentPath, currentPath.getLength() - 1)) > 2) {
    					//System.out.println("Not too close to end of path.. taking step.");
    					//take next step
    					takeNextStep(unitId, unitLoc);
    					//System.out.println("Took step.");
    					return;
    				} else {
    					unitsWithPaths.remove(unitId);
    					//System.out.println("Unit is too close to end of path.. getting new path to new location..");
    					//get new Location from nextLocations(from sysStartLocations())
    					MapLocation newLoc;
						if (planet == Planet.Earth)
							newLoc = getNextLocation();
						else
							newLoc = getNextLocationMars(); 
						//System.out.println("Next location is " + newLoc);
						//Find path to this new location
						Path newPath = findPathToLocation(unit, newLoc);
						//System.out.println("Found new path to location");
						
						//assign path to unit
						if (!newPath.isAtEnd()) {
							unitsWithPaths.put(unitId, newPath);
						
							//System.out.println("Taking step..");
	    					//Now unit should have a path.. take it's next step.
							takeNextStep(unitId, unitLoc);
							//System.out.println("Took step");
						} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				}
    			} else {
    				if (msLeft < 500) {
    					//System.out.println("Only 500ms left.. skipping turn..");
    		    		return;
    		    	}
    				//System.out.println("Unit does NOT have path.. checking if nearby units have a path..");
    				//Look around and see if unit can see any other units that already have paths
    				Path newPath = null;
    				PathIteration: for (int unitWithPathId : unitsWithPaths.keySet()) {
    					//if unit can see the unit with a path, and its path not at end
    					if (!unitsWithPaths.get(unitWithPathId).isAtEnd()) {
	    					if (controller.canSenseUnit(unitWithPathId)) {
	    						//System.out.println("A nearby unit does have a path.. getting closest step.");
	    						//get the closest step to unit
	    						Path path = unitsWithPaths.get(unitWithPathId);
	    						Path.Step closestStep = getClosestStepToUnit(unitLoc, path);
	    						//System.out.println("My loc is: " + unit.location().mapLocation());
	    						//AStar to that step,
	    						MapLocation m = new MapLocation(planet, closestStep.getX(), closestStep.getY());
	    						//System.out.println("Closest step is at: " + m + "..... getting path to that step..");
	    						newPath = findPathToLocation(unit, m);
	    						//Prepending newPaths steps to path at closest Step index.
	    						
	    						//Append all steps from that step to end onto recently found path
	    						int closestStepIndex = path.getIndexOf(closestStep);
	    						//System.out.println("Appending step from index " + closestStepIndex);
	    						for (int i = closestStepIndex + 1; i < path.getLength(); i++) {
	    							newPath.appendStep(path.getStep(i));
	    							//System.out.println("Appended step " + i);
	    						}
	    						
	    						//newPath.resetStepIndex();
	    						//Assign path to unit
	        					unitsWithPaths.put(unitId, newPath);
	    						break PathIteration;
	    					}
	    					
	    				}
    				}
    				
    				//If no unit around had a path/no units are around
    				if (newPath == null) {
    					unitsWithPaths.remove(unitId);
    					//System.out.println("No nearby units had paths.. getting new path.");
    					//get new Location from nextLocations(from sysStartLocations())
    					MapLocation newLoc;
						if (planet == Planet.Earth)
							newLoc = getNextLocation();
						else
							newLoc = getNextLocationMars();
    					//System.out.println("Nec location is at: " + newLoc);
    					//Find path to this new location
    					newPath = findPathToLocation(unit, newLoc);
    					//System.out.println("Found path to location.");
    					
    					//assign path to unit
    					if (!newPath.isAtEnd()) {
    						unitsWithPaths.put(unitId, newPath);
    					} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				}
    				//System.out.println("Taking step..");
    				//Now unit should have a path.. take it's next step.
        			if (!newPath.isAtEnd()) {
        				takeNextStep(unitId, unitLoc);
        			} else {
        				//unitsWithPaths.remove(unitId);
        				//tryMoveRandom(unitId);
        			}
    				
    			}
    			
				
				
    		}
			
	    	
	    	
	    	
	    	
	    	
    	} catch (Exception ex) {
    		//System.out.println("Problem in NewRangerSearchLogic1! -- " + ex.toString());
       	 	ex.printStackTrace();
    	}
    }	
    
    
    private void NewRangerSearchLogic2(Unit unit) throws Exception {
    	
    	int msLeft = controller.getTimeLeftMs();
    	
    	
    	if (msLeft < 200) {
    		//System.out.println("Only 50ms left.. skipping turn..");
    		return;
    	}
    	
    	resetAStarValues();
    	try {
    		//System.out.println("Unit: " + unit.id());	
    		
    		int unitId = unit.id();
    		MapLocation unitLoc = unit.location().mapLocation();
    		Planet planet = unitLoc.getPlanet();
    		//System.out.println("Unit location is: " + unitLoc);
    		//System.out.println("Checking if visiting location in list");
    		if (planet == Planet.Earth) {
    			checkIfVisitingLocation(unit);
    		} else {
    			checkIfVisitingLocationMars(unit);
    		}
    		
    		
    		VecUnit enemies = controller.senseNearbyUnitsByTeam(unitLoc, unit.visionRange() + 70, enemyTeam);
			
    		//if can see enemy
    		if (enemies.size() > 0) {
    			
    			for (int i = 0; i < enemiesOnBlast.size(); i++) {
    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    						enemiesOnBlast.remove(i);
    					return;
    				}
    			}
    			
    			for(int e = 0; e < enemies.size(); e++) {
    				if (enemies.get(e).unitType() == UnitType.Factory) {
    					MapLocation f = enemies.get(e).location().mapLocation();
    					if (!enemyFactoryLocs.contains(f)) {
    						enemyFactoryLocs.add(f);
    					}
    				}
    			}
    			
    			//System.out.println("Enemies seen: " + enemies.size() + " by unit: " + unit.id());
    			//if enemy is in attack range
    			Unit enemy = closestEnemyToUnit(unit, enemies);
    			Integer enemyId = enemy.id();
    			MapLocation enemyLoc = enemy.location().mapLocation();
    			//System.out.println("Closest Enemy is at " + enemy.location().mapLocation());
    			int distance = distanceSquaredBetween(unitLoc, enemyLoc);
    			//System.out.println("Distance Squared Between unit " + unit.id() + " and enemy is " + distance);
    			if (distance <= unit.attackRange()) {
    				//if enemy isnt too close to ranger
    				if (distance > unit.rangerCannotAttackRange()) {
    					
    					//System.out.println("Enemy in range.. Attacking..");
    					if (tryAttack(unitId, enemyId)) {
    						enemiesOnBlast.add(enemyId);
        					//System.out.println(enemiesOnBlast.toString());
        					
            				if (!controller.canSenseUnit(enemyId))
            					enemiesOnBlast.remove(enemyId);
            				return;
            			}
    					
//    					tryMove(unitId, oppositeDir(unitLoc.directionTo(enemy.location().mapLocation())));
    					//System.out.println("Attacked enemy.");
    				} else {
    					//need to back up
    					//System.out.println("Too close to enemy.");
    					
    					if (tryMove(unitId, oppositeDir(unitLoc.directionTo(enemyLoc)))) {
    						//if (distance >= unit.rangerCannotAttackRange())

    						for (int i = 0; i < enemiesOnBlast.size(); i++) {
    		    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    		    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    		    						enemiesOnBlast.remove(i);
    		    					return;
    		    				}
    		    			}
    					
    						//}
    					}
    				}
    			} else {	//if enemy is not in attack range,
    				//if unit has path
    				//System.out.println("Enemy not in range");
    				if (unitsWithPaths.containsKey(unitId)) {
    					//System.out.println("Unit DOES have a path...");
    					//if path is pathToEnemy(or just a path to a unit in general)
    					Path currentPath = unitsWithPaths.get(unitId);
    					if (currentPath.getToUnitId() == enemyId) {
    						//System.out.println("Path is to an enemy/unit - taking step");
    						//leaving as just if(path is to a unit) take step because this means it probably has a current "mission"
    						//and if path is not near end
    						if (!currentPath.isAtEnd()) {
    	    					//System.out.println("Not too close to end of path.. taking step.");
	    						//take step closer to unit(probably enemy, but maybe rocket, later on)
	    						takeNextStep(unitId, unitLoc);
	    						//System.out.println("Took step...");
	    						
    						} else {
    							if (msLeft < 500) {
    								//System.out.println("Only 500ms left.. skipping turn..");
    					    		return;
    					    	}
    							unitsWithPaths.remove(unitId);
    							//since path is near end, need new path to enemy
    							//System.out.println("Too close to end of path.. getting new path to enemy.");
    							Path newPath = findPathToEnemy(unit, enemy);
    							//System.out.println("Found path to enemy");
    							//assign path to unit
        						unitsWithPaths.put(unitId, newPath);
        						
        						if (!newPath.isAtEnd()) {
        							takeNextStep(unitId, unitLoc);
        							//System.out.println("Took step");	
            					} else {
        							//unitsWithPaths.remove(unitId);
        							//tryMoveRandom(unitId);
        						}
        						
        						//If close enough to enemy after taking step, try to attack.
        						//if (distance <= unit.attackRange()) {
        							//System.out.println("Enemy is in range after taking that step...");
        							//Unit took a step closer to enemy, try to attack
        						for (int i = 0; i < enemiesOnBlast.size(); i++) {
        		    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
        		    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
        		    						enemiesOnBlast.remove(i);
        		    					return;
        		    				}
        		    			}
    	    						//System.out.println("Attacked!");
        						//}
    						}
    					} else {	//If unit's path is not to a unit
    						if (msLeft < 500) {
    							//System.out.println("Only 500ms left.. skipping turn..");
    				    		return;
    				    	}
    						//System.out.println("Unit's path is not to an enemy/unit.. ");
    						//get a new path to the spotted enemy
    					
    							//System.out.println("No nearby units had a path to enemy.. finding own path..");
    							//Find a new path to enemy
    							Path newPath = findPathToEnemy(unit, enemy);
    							//System.out.println("Found path to enemy");
    							//assign path to unit
        						unitsWithPaths.put(unitId, newPath);
    						
    						
    						//System.out.println("Taking step");
    						//Now unit should have a path.. take it's next step.
    						if (!newPath.isAtEnd()) {
    							takeNextStep(unitId, unitLoc);
    							//System.out.println("Took step");	
        					} else {
    							//unitsWithPaths.remove(unitId);
    							//tryMoveRandom(unitId);
    						}
    						
    						//If close enough to enemy after taking step, try to attack.
    						//if (distance <= unit.attackRange()) {
    							//System.out.println("Enemy is in range after taking that step...");
    							//Unit took a step closer to enemy, try to attack
    						for (int i = 0; i < enemiesOnBlast.size(); i++) {
    		    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    		    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    		    						enemiesOnBlast.remove(i);
    		    					return;
    		    				}
    		    			}
	    					
	    						//System.out.println("Attacked!");
    						//}
    					}
    				} else {
    					//Since this unit has no path, find new Path to new Location
    					if (msLeft < 500) {
    						//System.out.println("Only 500ms left.. skipping turn..");
    			    		return;
    			    	}
    					//System.out.println("Unit does NOT have path..");
    					
    					
    					
    					//If no unit around had a path/no units are around
    					
							//System.out.println("No nearby units had a path... finding new path to new location..");
    						unitsWithPaths.remove(unitId);
    						//get new Location from nextLocations(from sysStartLocations())
    						MapLocation newLoc;
    						if (planet == Planet.Earth)
    							newLoc = getNextLocation();
    						else
    							newLoc = getNextLocationMars();
    						//System.out.println("Next location is " + newLoc);
    						//Find path to this new location
    						Path newPath = findPathToLocation(unit, newLoc);
    						//System.out.println("Found new path to location");
    						
    						//assign path to unit
							unitsWithPaths.put(unitId, newPath);
    					
    					
    					//System.out.println("Taking step..");
    					//Now unit should have a path.. take it's next step.
    					if (!newPath.isAtEnd()) {
							takeNextStep(unitId, unitLoc);
							//System.out.println("Took step");	
    					} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				}    				
    			}
    		} else {
    			if (msLeft < 500) {
    				//System.out.println("Only 500ms left.. skipping turn..");
    	    		return;
    	    	}
    			//System.out.println("Saw no enemies nearby.. checking if unit has a path already");
    			//Since unit can not see any enemies, check if they have a path
    			if (unitsWithPaths.containsKey(unitId)) {
    				//System.out.println("Unit DOES have a path.. checkign to see if it is near the end.");
    				Path currentPath = unitsWithPaths.get(unitId);
    				//if unit has a path, and is not too close to the end of that path
    				if (!currentPath.isAtEnd()) {
    					//System.out.println("Not too close to end of path.. taking step.");
    					//take next step
    					takeNextStep(unitId, unitLoc);
    					//System.out.println("Took step.");
    					
    				} else {
    					unitsWithPaths.remove(unitId);
    					//System.out.println("Unit is too close to end of path.. getting new path to new location..");
    					//get new Location from nextLocations(from sysStartLocations())
    					MapLocation newLoc;
						if (planet == Planet.Earth)
							newLoc = getNextLocation();
						else
							newLoc = getNextLocationMars(); 
						//System.out.println("Next location is " + newLoc);
						//Find path to this new location
						Path newPath = findPathToLocation(unit, newLoc);
						//System.out.println("Found new path to location");
						
						//assign path to unit
						if (!newPath.isAtEnd()) {
							unitsWithPaths.put(unitId, newPath);
						
							//System.out.println("Taking step..");
	    					//Now unit should have a path.. take it's next step.
							takeNextStep(unitId, unitLoc);
							//System.out.println("Took step");
						} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				}
    			} else {
    				if (msLeft < 500) {
    					//System.out.println("Only 500ms left.. skipping turn..");
    		    		return;
    		    	}
    				//System.out.println("Unit does NOT have path.. checking if nearby units have a path..");
    				
    				
    				//If no unit around had a path/no units are around
    			
    					unitsWithPaths.remove(unitId);
    					//System.out.println("No nearby units had paths.. getting new path.");
    					//get new Location from nextLocations(from sysStartLocations())
    					MapLocation newLoc;
						if (planet == Planet.Earth)
							newLoc = getNextLocation();
						else
							newLoc = getNextLocationMars();
    					//System.out.println("Nec location is at: " + newLoc);
    					//Find path to this new location
    					Path newPath = findPathToLocation(unit, newLoc);
    					//System.out.println("Found path to location.");
    					
    					//assign path to unit
    					if (!newPath.isAtEnd()) {
    						unitsWithPaths.put(unitId, newPath);
    					} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				
    				//System.out.println("Taking step..");
    				//Now unit should have a path.. take it's next step.
        			if (!newPath.isAtEnd()) {
        				takeNextStep(unitId, unitLoc);
        			} else {
        				//unitsWithPaths.remove(unitId);
        				//tryMoveRandom(unitId);
        			}
    				
    			}
    			
				
				
    		}
			
    		//System.out.println(enemiesOnBlast.toString());
	    	
	    	enemies.delete();
	    	
	    	
    	} catch (Exception ex) {
    		//System.out.println("Problem in NewRangerSearchLogic1! -- " + ex.toString());
       	 	ex.printStackTrace();
    	}
    }	
    
    
    
    private void NewMageSearchLogic2(Unit unit) throws Exception {
    	
    	int msLeft = controller.getTimeLeftMs();
    	
    	
    	if (msLeft < 200) {
    		//System.out.println("Only 50ms left.. skipping turn..");
    		return;
    	}
    	
    	resetAStarValues();
    	try {
    		//System.out.println("Unit: " + unit.id());	
    		
    		int unitId = unit.id();
    		MapLocation unitLoc = unit.location().mapLocation();
    		Planet planet = unitLoc.getPlanet();
    		//System.out.println("Unit location is: " + unitLoc);
    		//System.out.println("Checking if visiting location in list");
    		if (planet == Planet.Earth) {
    			checkIfVisitingLocation(unit);
    		} else {
    			checkIfVisitingLocationMars(unit);
    		}
    		
    		
    		VecUnit enemies = controller.senseNearbyUnitsByTeam(unitLoc, unit.visionRange() + 70, enemyTeam);
			
    		//if can see enemy
    		if (enemies.size() > 0) {
    			
    			for (int i = 0; i < enemiesOnBlast.size(); i++) {
    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    						enemiesOnBlast.remove(i);
    					return;
    				}
    			}
    			
    			
    			//System.out.println("Enemies seen: " + enemies.size() + " by unit: " + unit.id());
    			//if enemy is in attack range
    			Unit enemy = closestEnemyToUnit(unit, enemies);
    			Integer enemyId = enemy.id();
    			MapLocation enemyLoc = enemy.location().mapLocation();
    			//System.out.println("Closest Enemy is at " + enemy.location().mapLocation());
    			int distance = distanceSquaredBetween(unitLoc, enemyLoc);
    			//System.out.println("Distance Squared Between unit " + unit.id() + " and enemy is " + distance);
    			if (distance <= unit.attackRange()) {
    				
					//System.out.println("Enemy in range.. Attacking..");
					if (tryAttack(unitId, enemyId)) {
						enemiesOnBlast.add(enemyId);
    					//System.out.println(enemiesOnBlast.toString());
    					
        				if (!controller.canSenseUnit(enemyId))
        					enemiesOnBlast.remove(enemyId);
        				return;
        			}
					
//    				
    			} else {	//if enemy is not in attack range,
    				//if unit has path
    				//System.out.println("Enemy not in range");
    				if (unitsWithPaths.containsKey(unitId)) {
    					//System.out.println("Unit DOES have a path...");
    					//if path is pathToEnemy(or just a path to a unit in general)
    					Path currentPath = unitsWithPaths.get(unitId);
    					if (currentPath.getToUnitId() == enemyId) {
    						//System.out.println("Path is to an enemy/unit - taking step");
    						//leaving as just if(path is to a unit) take step because this means it probably has a current "mission"
    						//and if path is not near end
    						if (!currentPath.isAtEnd()) {
    	    					//System.out.println("Not too close to end of path.. taking step.");
	    						//take step closer to unit(probably enemy, but maybe rocket, later on)
	    						takeNextStep(unitId, unitLoc);
	    						//System.out.println("Took step...");
	    						
    						} else {
    							if (msLeft < 500) {
    								//System.out.println("Only 500ms left.. skipping turn..");
    					    		return;
    					    	}
    							unitsWithPaths.remove(unitId);
    							//since path is near end, need new path to enemy
    							//System.out.println("Too close to end of path.. getting new path to enemy.");
    							Path newPath = findPathToEnemy(unit, enemy);
    							//System.out.println("Found path to enemy");
    							//assign path to unit
        						unitsWithPaths.put(unitId, newPath);
        						
        						if (!newPath.isAtEnd()) {
        							takeNextStep(unitId, unitLoc);
        							//System.out.println("Took step");	
            					} else {
        							//unitsWithPaths.remove(unitId);
        							//tryMoveRandom(unitId);
        						}
        						
        						//If close enough to enemy after taking step, try to attack.
        						//if (distance <= unit.attackRange()) {
        							//System.out.println("Enemy is in range after taking that step...");
        							//Unit took a step closer to enemy, try to attack
        						for (int i = 0; i < enemiesOnBlast.size(); i++) {
        		    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
        		    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
        		    						enemiesOnBlast.remove(i);
        		    					return;
        		    				}
        		    			}
    	    						//System.out.println("Attacked!");
        						//}
    						}
    					} else {	//If unit's path is not to a unit
    						if (msLeft < 500) {
    							//System.out.println("Only 500ms left.. skipping turn..");
    				    		return;
    				    	}
    						//System.out.println("Unit's path is not to an enemy/unit.. ");
    						//get a new path to the spotted enemy
    					
    							//System.out.println("No nearby units had a path to enemy.. finding own path..");
    							//Find a new path to enemy
    							Path newPath = findPathToEnemy(unit, enemy);
    							//System.out.println("Found path to enemy");
    							//assign path to unit
        						unitsWithPaths.put(unitId, newPath);
    						
    						
    						//System.out.println("Taking step");
    						//Now unit should have a path.. take it's next step.
    						if (!newPath.isAtEnd()) {
    							takeNextStep(unitId, unitLoc);
    							//System.out.println("Took step");	
        					} else {
    							//unitsWithPaths.remove(unitId);
    							//tryMoveRandom(unitId);
    						}
    						
    						//If close enough to enemy after taking step, try to attack.
    						//if (distance <= unit.attackRange()) {
    							//System.out.println("Enemy is in range after taking that step...");
    							//Unit took a step closer to enemy, try to attack
    						for (int i = 0; i < enemiesOnBlast.size(); i++) {
    		    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    		    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    		    						enemiesOnBlast.remove(i);
    		    					return;
    		    				}
    		    			}
	    					
	    						//System.out.println("Attacked!");
    						//}
    					}
    				} else {
    					//Since this unit has no path, find new Path to new Location
    					if (msLeft < 500) {
    						//System.out.println("Only 500ms left.. skipping turn..");
    			    		return;
    			    	}
    					//System.out.println("Unit does NOT have path..");
    					
    					
    					
    					//If no unit around had a path/no units are around
    					
							//System.out.println("No nearby units had a path... finding new path to new location..");
    						unitsWithPaths.remove(unitId);
    						//get new Location from nextLocations(from sysStartLocations())
    						MapLocation newLoc;
    						if (planet == Planet.Earth)
    							newLoc = getNextLocation();
    						else
    							newLoc = getNextLocationMars();
    						//System.out.println("Next location is " + newLoc);
    						//Find path to this new location
    						Path newPath = findPathToLocation(unit, newLoc);
    						//System.out.println("Found new path to location");
    						
    						//assign path to unit
							unitsWithPaths.put(unitId, newPath);
    					
    					
    					//System.out.println("Taking step..");
    					//Now unit should have a path.. take it's next step.
    					if (!newPath.isAtEnd()) {
							takeNextStep(unitId, unitLoc);
							//System.out.println("Took step");	
    					} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				}    				
    			}
    		} else {
    			if (msLeft < 500) {
    				//System.out.println("Only 500ms left.. skipping turn..");
    	    		return;
    	    	}
    			//System.out.println("Saw no enemies nearby.. checking if unit has a path already");
    			//Since unit can not see any enemies, check if they have a path
    			if (unitsWithPaths.containsKey(unitId)) {
    				//System.out.println("Unit DOES have a path.. checkign to see if it is near the end.");
    				Path currentPath = unitsWithPaths.get(unitId);
    				//if unit has a path, and is not too close to the end of that path
    				if (!currentPath.isAtEnd()) {
    					//System.out.println("Not too close to end of path.. taking step.");
    					//take next step
    					takeNextStep(unitId, unitLoc);
    					//System.out.println("Took step.");
    					
    				} else {
    					unitsWithPaths.remove(unitId);
    					//System.out.println("Unit is too close to end of path.. getting new path to new location..");
    					//get new Location from nextLocations(from sysStartLocations())
    					MapLocation newLoc;
						if (planet == Planet.Earth)
							newLoc = getNextLocation();
						else
							newLoc = getNextLocationMars(); 
						//System.out.println("Next location is " + newLoc);
						//Find path to this new location
						Path newPath = findPathToLocation(unit, newLoc);
						//System.out.println("Found new path to location");
						
						//assign path to unit
						if (!newPath.isAtEnd()) {
							unitsWithPaths.put(unitId, newPath);
						
							//System.out.println("Taking step..");
	    					//Now unit should have a path.. take it's next step.
							takeNextStep(unitId, unitLoc);
							//System.out.println("Took step");
						} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				}
    			} else {
    				if (msLeft < 500) {
    					//System.out.println("Only 500ms left.. skipping turn..");
    		    		return;
    		    	}
    				//System.out.println("Unit does NOT have path.. checking if nearby units have a path..");
    				
    				
    				//If no unit around had a path/no units are around
    			
    					unitsWithPaths.remove(unitId);
    					//System.out.println("No nearby units had paths.. getting new path.");
    					//get new Location from nextLocations(from sysStartLocations())
    					MapLocation newLoc;
						if (planet == Planet.Earth)
							newLoc = getNextLocation();
						else
							newLoc = getNextLocationMars();
    					//System.out.println("Nec location is at: " + newLoc);
    					//Find path to this new location
    					Path newPath = findPathToLocation(unit, newLoc);
    					//System.out.println("Found path to location.");
    					
    					//assign path to unit
    					if (!newPath.isAtEnd()) {
    						unitsWithPaths.put(unitId, newPath);
    					} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				
    				//System.out.println("Taking step..");
    				//Now unit should have a path.. take it's next step.
        			if (!newPath.isAtEnd()) {
        				takeNextStep(unitId, unitLoc);
        			} else {
        				//unitsWithPaths.remove(unitId);
        				//tryMoveRandom(unitId);
        			}
    				
    			}
    			
				
				
    		}
			
    		//System.out.println(enemiesOnBlast.toString());
	    	
	    	enemies.delete();
	    	
	    	
    	} catch (Exception ex) {
    		//System.out.println("Problem in NewRangerSearchLogic1! -- " + ex.toString());
       	 	ex.printStackTrace();
    	}
    }	



    
    private void MageLogic1(Unit unit) throws Exception {
    	
    	int msLeft = controller.getTimeLeftMs();
    	
    	
    	if (msLeft < 200) {
    		//System.out.println("Only 50ms left.. skipping turn..");
    		return;
    	}
    	
    	resetAStarValues();
    	try {
    		//System.out.println("Unit: " + unit.id());	
    		
    		int unitId = unit.id();
    		MapLocation unitLoc = unit.location().mapLocation();
    		Planet planet = unitLoc.getPlanet();
    		//System.out.println("Unit location is: " + unitLoc);
    		//System.out.println("Checking if visiting location in list");

    		if (planet == Planet.Earth)
    			checkIfVisitingLocation(unit);
    		else
    			checkIfVisitingLocationMars(unit);
    		
    		
    		
    		VecUnit enemies = controller.senseNearbyUnitsByTeam(unitLoc, unit.visionRange(), enemyTeam);
			
    		//if can see enemy
    		if (enemies.size() > 0) {
    			
    			for (int i = 0; i < enemiesOnBlast.size(); i++) {
    				if (tryAttack(unitId, enemiesOnBlast.get(i))) {
    					if (!controller.canSenseUnit(enemiesOnBlast.get(i)))
    						enemiesOnBlast.remove(i);
    					return;
    				}
    			}
    			
    			
    			
    			//System.out.println("Enemies seen: " + enemies.size() + " by unit: " + unit.id());
    			//if enemy is in attack range
    			Unit enemy = closestEnemyToUnit(unit, enemies);
    			int enemyId = enemy.id();
    			MapLocation enemyLoc = enemy.location().mapLocation();
    			//System.out.println("Closest Enemy is at " + enemy.location().mapLocation());
    			int distance = distanceSquaredBetween(unitLoc, enemyLoc);
    			//System.out.println("Distance Squared Between unit " + unit.id() + " and enemy is " + distance);
    			if (distance <= unit.attackRange()) {
    				//System.out.println("Enemy in range.. Attacking..");
    				//enemiesOnBlast.add(enemyId);
    				if (tryAttack(unitId, enemyId)) {
    					enemiesOnBlast.add(enemyId);
    				//	System.out.println(enemiesOnBlast.toString());
        				if (!controller.canSenseUnit(enemyId))
        					enemiesOnBlast.remove(Integer.valueOf(enemyId));
        				return;
        			}
        			
					//tryMove(unitId, oppositeDir(unitLoc.directionTo(enemy.location().mapLocation())));

    			} else {	//if enemy is not in attack range,
    				//if unit has path
    				//System.out.println("Enemy not in range");
    				if (unitsWithPaths.containsKey(unitId)) {
    					//System.out.println("Unit DOES have a path...");
    					//if path is pathToEnemy(or just a path to a unit in general)
    					Path currentPath = unitsWithPaths.get(unitId);
    					if (currentPath.getToUnitId() == enemyId) {
    						//System.out.println("Path is to an enemy/unit - taking step");
    						//leaving as just if(path is to a unit) take step because this means it probably has a current "mission"
    						//and if path is not near end
    						if (currentPath.getCurrentStepIndex() < currentPath.getLength() && distanceBetween(unit, PathStepToMapLoc(planet, currentPath, currentPath.getLength() - 1)) > 2) {
    	    					//System.out.println("Not too close to end of path.. taking step.");
	    						//take step closer to unit(probably enemy, but maybe rocket, later on)
	    						takeNextStep(unitId, unitLoc);
	    						//System.out.println("Took step...");
	    						return;
    						} else {
    							if (msLeft < 500) {
    								//System.out.println("Only 500ms left.. skipping turn..");
    					    		return;
    					    	}
    							unitsWithPaths.remove(unitId);
    							//since path is near end, need new path to enemy
    							//System.out.println("Too close to end of path.. getting new path to enemy.");
    							Path newPath = findPathToEnemy(unit, enemy);
    							//System.out.println("Found path to enemy");
    							//assign path to unit
        						unitsWithPaths.put(unitId, newPath);
        						
        						if (!newPath.isAtEnd()) {
        							takeNextStep(unitId, unitLoc);
        							//System.out.println("Took step");	
            					} else {
        							//unitsWithPaths.remove(unitId);
        							//tryMoveRandom(unitId);
        						}
        						
        						//If close enough to enemy after taking step, try to attack.
        						//if (distance <= unit.attackRange()) {
        							//System.out.println("Enemy is in range after taking that step...");
        							//Unit took a step closer to enemy, try to attack
        						if (tryAttack(unit, enemy)) {
            						int dif = (int)enemy.health() - unit.damage();
            						if (dif <= 0)
        	    						enemiesOnBlast.remove(enemyId);
        	    					
        	    					return;
        	    				}
    	    						//System.out.println("Attacked!");
        						//}
    						}
    					} else {	//If unit's path is not to a unit
    						if (msLeft < 500) {
    							//System.out.println("Only 500ms left.. skipping turn..");
    				    		return;
    				    	}
    						//System.out.println("Unit's path is not to an enemy/unit.. ");
    						//get a new path to the spotted enemy
    						Path newPath = null;
    						//System.out.println("Checking if any nearby units already have a path to enemy");
    						PathIteration: for (Path path : unitsWithPaths.values()) {	//for each path currently in use
    							//if the path is to the spotted enemy
    							if (path.getToUnitId() == enemyId) {
    								//System.out.println("Found a nearby unit with a path to enemy.. getting closest step.");
    								//get the step closest to me in the path,
    								Path.Step closestStep = getClosestStepToUnit(unitLoc, path);
    								//AStar to that step,
    								MapLocation m = new MapLocation(planet, closestStep.getX(), closestStep.getY());

    								//System.out.println("Closest step is at: " + m + "..... getting path to that step..");
    								newPath = findPathToLocation(unit, m);
    								
    								//Append all steps from that step to end onto recently found path
    								int closestStepIndex = path.getIndexOf(closestStep);
    								//System.out.println("Appending step from index " + closestStepIndex);
    								for (int i = closestStepIndex + 1; i < path.getLength(); i++) {
    									newPath.appendStep(path.getStep(i));
    									//System.out.println("Appended step " + i);
    								}
    								
    								//newPath.resetStepIndex();
    								//Assign path to unit
    		    					unitsWithPaths.put(unitId, newPath);
    								break PathIteration;
    							}
    						}
    						//If the currently used paths do not go to enemy,
    						if (newPath == null) {
    							//System.out.println("No nearby units had a path to enemy.. finding own path..");
    							//Find a new path to enemy
    							newPath = findPathToEnemy(unit, enemy);
    							//System.out.println("Found path to enemy");
    							//assign path to unit
        						unitsWithPaths.put(unitId, newPath);
    						}
    						
    						//System.out.println("Taking step");
    						//Now unit should have a path.. take it's next step.
    						if (!newPath.isAtEnd()) {
    							takeNextStep(unitId, unitLoc);
    							//System.out.println("Took step");	
        					} else {
    							//unitsWithPaths.remove(unitId);
    							//tryMoveRandom(unitId);
    						}
    						
    						//If close enough to enemy after taking step, try to attack.
    						//if (distance <= unit.attackRange()) {
    							//System.out.println("Enemy is in range after taking that step...");
    							//Unit took a step closer to enemy, try to attack
    						if (tryAttack(unit, enemy)) {
        						int dif = (int)enemy.health() - unit.damage();
        						if (dif <= 0)
    	    						enemiesOnBlast.remove(enemyId);
    	    					
    	    					return;
    	    				}
	    						//System.out.println("Attacked!");
    						//}
    					}
    				} else {
    					//Since this unit has no path, find new Path to new Location
    					if (msLeft < 500) {
    						//System.out.println("Only 500ms left.. skipping turn..");
    			    		return;
    			    	}
    					//System.out.println("Unit does NOT have path..");
    					//Look around and see if unit can see any other units that already have paths
    					Path newPath = null;
    					//System.out.println("Checking if any nearby units have a path already..");
    					PathIteration: for (int unitWithPathId : unitsWithPaths.keySet()) {
    						//if unit can see the unit with a path, and its path not at end
        					if (!unitsWithPaths.get(unitWithPathId).isAtEnd()) {
	    						if (controller.canSenseUnit(unitWithPathId)) {
	    							//System.out.println("A nearby unit does have a path.. getting closest step to unit");
	    							//get the closest step to unit
	    							Path path = unitsWithPaths.get(unitWithPathId);
	    							Path.Step closestStep = getClosestStepToUnit(unitLoc, path);
									
									//AStar to that step,
									MapLocation m = new MapLocation(planet, closestStep.getX(), closestStep.getY());
									//System.out.println("Closest step is at: " + m + "..... getting path to that step..");
									newPath = findPathToLocation(unit, m);
									
									//Append all steps from that step to end onto recently found path
									int closestStepIndex = path.getIndexOf(closestStep);
									//System.out.println("Appending step from index " + closestStepIndex);
									
									for (int i = closestStepIndex + 1; i < path.getLength(); i++) {
										newPath.appendStep(path.getStep(i));
	
	    								//System.out.println("Appended step " + i);
									}
	
									//newPath.resetStepIndex();
									//assign path to unit
			    					unitsWithPaths.put(unitId, newPath);
									break PathIteration;
	    						}
	    						
	    					}
    					}
    					
    					//If no unit around had a path/no units are around
    					if (newPath == null) {
    						unitsWithPaths.remove(unitId);
							//System.out.println("No nearby units had a path... finding new path to new location..");
    						//get new Location from nextLocations(from sysStartLocations())
    						MapLocation newLoc;
    						if (planet == Planet.Earth)
    							newLoc = getNextLocation();
    						else
    							newLoc = getNextLocationMars(); 
    						//System.out.println("Next location is " + newLoc);
    						//Find path to this new location
    						newPath = findPathToLocation(unit, newLoc);
    						//System.out.println("Found new path to location");
    						
    						//assign path to unit
							unitsWithPaths.put(unitId, newPath);
    					}
    					
    					//System.out.println("Taking step..");
    					//Now unit should have a path.. take it's next step.
    					if (!newPath.isAtEnd()) {
							takeNextStep(unitId, unitLoc);
							//System.out.println("Took step");	
    					} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
							
    				}    				
    			}
    		} else {
    			if (msLeft < 500) {
    				//System.out.println("Only 500ms left.. skipping turn..");
    	    		return;
    	    	}
    			//System.out.println("Saw no enemies nearby.. checking if unit has a path already");
    			//Since unit can not see any enemies, check if they have a path
    			if (unitsWithPaths.containsKey(unitId)) {
    				//System.out.println("Unit DOES have a path.. checking to see if it is near the end.");
    				Path currentPath = unitsWithPaths.get(unitId);
    				//if unit has a path, and is not too close to the end of that path
    				if (currentPath.getCurrentStepIndex() < currentPath.getLength() && distanceBetween(unit, PathStepToMapLoc(planet, currentPath, currentPath.getLength() - 1)) > 2) {
    					//System.out.println("Not too close to end of path.. taking step.");
    					//take next step
    					takeNextStep(unitId, unitLoc);
    					//System.out.println("Took step.");
    					return;
    				} else {
    					//System.out.println("Unit is too close to end of path.. getting new path to new location..");
    					unitsWithPaths.remove(unitId);
    					//get new Location from nextLocations(from sysStartLocations())
    					MapLocation newLoc;
						if (planet == Planet.Earth)
							newLoc = getNextLocation();
						else
							newLoc = getNextLocationMars(); 
						//System.out.println("Next location is " + newLoc);
						//Find path to this new location
						Path newPath = findPathToLocation(unit, newLoc);
						//System.out.println("Found new path to location");
						
						//assign path to unit
						unitsWithPaths.put(unitId, newPath);
						
						//System.out.println("Taking step..");
    					//Now unit should have a path.. take it's next step.
						if (!newPath.isAtEnd()) {
							takeNextStep(unitId, unitLoc);
							//System.out.println("Took step");	
    					} else {
							//unitsWithPaths.remove(unitId);
							//tryMoveRandom(unitId);
						}
    				}
    			} else {
    				if (msLeft < 500) {
    					//System.out.println("Only 500ms left.. skipping turn..");
    		    		return;
    		    	}
    				//System.out.println("Unit does NOT have path.. checking if nearby units have a path..");
    				//Look around and see if unit can see any other units that already have paths
    				Path newPath = null;
    				PathIteration: for (int unitWithPathId : unitsWithPaths.keySet()) {
    					//if unit can see the unit with a path, and its path not at end
    					if (!unitsWithPaths.get(unitWithPathId).isAtEnd()) {
	    					if (controller.canSenseUnit(unitWithPathId)) {
	    						//System.out.println("A nearby unit does have a path.. getting closest step.");
	    						//get the closest step to unit
	    						Path path = unitsWithPaths.get(unitWithPathId);
	    						Path.Step closestStep = getClosestStepToUnit(unitLoc, path);
	    						//System.out.println("My loc is: " + unit.location().mapLocation());
	    						//AStar to that step,
	    						MapLocation m = new MapLocation(planet, closestStep.getX(), closestStep.getY());
	    						//System.out.println("Closest step is at: " + m + "..... getting path to that step..");
	    						newPath = findPathToLocation(unit, m);
	    						//Prepending newPaths steps to path at closest Step index.
	    						
	    						//Append all steps from that step to end onto recently found path
	    						int closestStepIndex = path.getIndexOf(closestStep);
	    						//System.out.println("Appending step from index " + closestStepIndex);
	    						for (int i = closestStepIndex + 1; i < path.getLength(); i++) {
	    							newPath.appendStep(path.getStep(i));
	    							//System.out.println("Appended step " + i);
	    						}
	    						
	    						//newPath.resetStepIndex();
	    						//Assign path to unit
	        					unitsWithPaths.put(unitId, newPath);
	    						break PathIteration;
	    					}
	    					
	    				}
    				}
    				
    				//If no unit around had a path/no units are around
    				if (newPath == null) {
    					unitsWithPaths.remove(unitId);
    					//System.out.println("No nearby units had paths.. getting new path.");
    					//get new Location from nextLocations(from sysStartLocations())
    					MapLocation newLoc;
						if (planet == Planet.Earth)
							newLoc = getNextLocation();
						else
							newLoc = getNextLocationMars();
    					//System.out.println("Nec location is at: " + newLoc);
    					//Find path to this new location
    					newPath = findPathToLocation(unit, newLoc);
    					//System.out.println("Found path to location.");
    					
    					//assign path to unit
    					unitsWithPaths.put(unitId, newPath);
    				}
    				
    				if (!newPath.isAtEnd()) {
						takeNextStep(unitId, unitLoc);
						//System.out.println("Took step");	
					} else {
						//unitsWithPaths.remove(unitId);
						//tryMoveRandom(unitId);
					}
    			}
    			
    		}
			
	    	
	    	
	    	
	    	
	    	
    	} catch (Exception ex) {
    		//System.out.println("Problem in NewRangerSearchLogic1! -- " + ex.toString());
       	 	ex.printStackTrace();
    	}
    }	
    
    
    
    private Path.Step getClosestStepToUnit(MapLocation m, Path path) {
    	int closestDist = 501;
    	Path.Step closestStep = path.getStep(0);
    	for (Path.Step step : path.getAllSteps()) {
    		int temp = distanceBetween(m, new MapLocation(m.getPlanet(), step.getX(), step.getY()));
    		if (temp < closestDist) {
    			closestStep = step;
    			closestDist = temp;
    		}
    	}
		return closestStep;
    }
    
    
	private Unit getClosestFactoryToUnit(MapLocation u, ArrayList<Unit> factories) {
		int closestDist = 501;
		Unit closestFactory = factories.get(0);
		Unit f;
		for (int i = 0; i < factories.size(); i++) {
			f = factories.get(i);
			int temp = distanceBetween(u, f.location().mapLocation());
			if (temp < closestDist) {
				closestFactory = f;
				closestDist = temp;
			}
		}
		return closestFactory;
	}

	
	private MapLocation getClosestLocToUnit(MapLocation u, ArrayList<MapLocation> f) {
		int closestDist = 501;
		MapLocation closestLoc = f.get(0);
		MapLocation m;
		for (int i = 0; i < f.size(); i++) {
			m = f.get(i);
			int temp = distanceBetween(u, m);
			if (temp < closestDist) {
				closestLoc = m;
				closestDist = temp;
			}
		}
		return closestLoc;
	}
	
	
	Direction[] dirs = {Direction.North, Direction.Northeast, Direction.East, Direction.Southeast, Direction.South, Direction.Southwest, Direction.West, Direction.Northwest}; 
	
	
	
	public Direction rotate(Direction dir, int amount){
		int dirIndex = 0;
	     for (int i = 0; i < dirs.length; i++) {
	    	 if (dirs[i] == dir) {
	    		 dirIndex = i;
	    	 }
	     }
	     return dirs[(dirIndex + amount) % 8];
	 }
	
	private boolean takeNextStep(int unitId, MapLocation unitLoc) throws Exception {
		Path.Step nextStep;
		Path p = unitsWithPaths.get(unitId);
		//int currIndex = p.getCurrentStepIndex();
		
		if (!p.isAtEnd()) {
			nextStep = p.getNextStep();
		} else {
			unitsWithPaths.remove(unitId);
			return false;
		}
		
		//System.out.println("Taking step to location: " + nextStep.getX() + ", " + nextStep.getY());
		if (tryMove(unitId, unitLoc.directionTo((new MapLocation(
				unitLoc.getPlanet(), nextStep.getX(), nextStep.getY()))))) {
			return true;
		} else {
			//p.setCurrentStepIndex(currIndex);
			unitsWithPaths.remove(unitId);
			tryMoveRandom(unitId);
			return false;
		}
	}
	
	
	private boolean takeNextStepHealer(int unitId, MapLocation unitLoc) throws Exception {
		Path.Step nextStep;
		Path p = healersWithPaths.get(unitId);
		//int currIndex = p.getCurrentStepIndex();
		
		if (!p.isAtEnd()) {
			nextStep = p.getNextStep();
		} else {
			healersWithPaths.remove(unitId);
			return false;
		}
		
		//System.out.println("Taking step to location: " + nextStep.getX() + ", " + nextStep.getY());
		if (tryMove(unitId, unitLoc.directionTo((new MapLocation(
				unitLoc.getPlanet(), nextStep.getX(), nextStep.getY()))))) {
			return true;
		} else {
			//p.setCurrentStepIndex(currIndex);
			healersWithPaths.remove(unitId);
			tryMoveRandom(unitId);
			return false;
		}
	}
	
	private boolean takeNextStepWorker(int unitId, MapLocation unitLoc) throws Exception {
		Path.Step nextStep;
		Path p = workersWithPaths.get(unitId);
		if (!p.isAtEnd()) {
			nextStep = p.getNextStep();
		} else {
			//System.out.println("Couldnt take step.. at end.. removing unit with path from list");
			workersWithPaths.remove(unitId);
			return false;
		}
		//System.out.println("Taking step to location: " + nextStep.getX() + ", " + nextStep.getY());
		if(unitLoc.directionTo(new MapLocation(unitLoc.getPlanet(), nextStep.getX(), nextStep.getY())) == Direction.Center) {
			return false;
		}
		return tryMove(unitId, unitLoc.directionTo((new MapLocation(
				unitLoc.getPlanet(), nextStep.getX(), nextStep.getY()))));
	}
	
	
//	private Path getNextLocationToVisit(Unit unit) throws Exception {
//		boolean backToStart = (startingLocationsVisited == startingLocations.size()) ? true : false;
//		if (backToStart) {
//			return findPathToLocation(unit, startingLocations.get(0));
//		}
//		return findPathToLocation(unit, getSymmetricalLoc(startingLocations.get(startingLocationsVisited)));
//	}
	
	private void checkIfVisitingLocation(Unit unit) throws Exception {
		if (symStartingLocationsVisitedTotal == symStartingLocations.size()) {
			//System.out.println("All Locations Visisted");
			return;
		}
		
		for (int i = 0; i < symStartingLocations.size(); i++) {
			MapLocation m = symStartingLocations.get(i);
			if (distanceSquaredBetween(m, unit) < 7) {
				//System.out.println("Location Visited: " + m);
				symStartingLocationsVisited.add(m);
				symStartingLocationsVisitedTotal++;
				//symStartingLocations.remove(m);
				
			}
		}
	}
	private void checkIfVisitingLocationMars(Unit unit) throws Exception {
		if (symStartingLocationsVisitedTotalMars == symStartingLocationsMars.size()) {
			//System.out.println("All Locations Visisted");
			return;
		}
		
		for (int i = 0; i < symStartingLocationsMars.size(); i++) {
			MapLocation m = symStartingLocationsMars.get(i);
			if (distanceSquaredBetween(m, unit) < 7) {
				//System.out.println("Location Visited: " + m);
				symStartingLocationsVisitedMars.add(m);
				symStartingLocationsVisitedTotalMars++;
				//symStartingLocations.remove(m);
				
			}
		}
	}
	
	private MapLocation getNextLocation() throws Exception {
		if (symStartingLocationsVisitedTotal == 0)
			if (symStartingLocations.size() > 0)
				return symStartingLocations.get(0);
		
		
		for (MapLocation m : symStartingLocations) {
			boolean visited = false;
			for (MapLocation v : symStartingLocationsVisited) {
				if (m.getX() == v.getX() && m.getY() == v.getY())
					visited = true;
			}
			if (!visited)
				return m;
		}
		
		return randomEarthLoc();		
		
	}
	
	private MapLocation getNextLocationMars() throws Exception {
		if (symStartingLocationsVisitedTotalMars == 0)
			if (symStartingLocationsMars.size() > 0)
				return symStartingLocationsMars.get(0);
		
		
		for (MapLocation m : symStartingLocationsMars) {
			boolean visited = false;
			for (MapLocation v : symStartingLocationsVisitedMars) {
				if (m.getX() == v.getX() && m.getY() == v.getY())
					visited = true;
			}
			if (!visited)
				return m;
		}
		
		return randomMarsLoc();		
		
	}
	
	
	private MapLocation randomEarthLoc() throws Exception {
		int x = random.nextInt(earthWidth);
		int y = random.nextInt(earthHeight);
		return new MapLocation(Planet.Earth, x, y);
	}
	
	private MapLocation randomMarsLoc() throws Exception {
		int x = random.nextInt(marsWidth);
		int y = random.nextInt(marsHeight);
		return new MapLocation(Planet.Mars, x, y);
	}
	
	
	private Path findNextPath(Unit unit) throws Exception {
		MapLocation nextLoc = null;
		if (startingWorkers == 1)
			nextLoc = getSymmetricalLoc(startingLoc);
		else {
			boolean backToStart = true;
			////System.out.println("StartingLocsSize: " + startingLocations.size());
			StartingLocationLoop: for (int i = 0; i < startingLocations.size(); i++) {
				////System.out.println("Distance from unit to symStartLoc: " + distanceSquaredBetween(getSymmetricalLoc(startingLocations.get(i)), unit));
				////System.out.println("Vision: " + unit.visionRange());
				if (distanceSquaredBetween(getSymmetricalLoc(startingLocations.get(i)), unit) > unit.visionRange()) {
					nextLoc = getSymmetricalLoc(startingLocations.get(i));
					
					backToStart = false;
					////System.out.println(unit.location().mapLocation());
					////System.out.println(getSymmetricalLoc(startingLocations.get(i)));
					break StartingLocationLoop;
				}
			}
			if (backToStart)
				return findPathToLocation(unit, startingLocations.get(0));
		}
		
		return findPathToLocation(unit, nextLoc);
	}
	

	
	private Path findPathToEnemy(Unit unit, Unit enemy) throws Exception {
		resetAStarValues();
		Path p = scout.aStar(unit.location().mapLocation().getX(), unit.location().mapLocation().getY(), 
				enemy.location().mapLocation().getX(), enemy.location().mapLocation().getY(), unit, enemy.id());
		
		return p;
	}
	
	private Path findPathToFactory(Unit unit, Unit factory) throws Exception {
		resetAStarValues();
		Path p = scout.aStar(unit.location().mapLocation().getX(), unit.location().mapLocation().getY(), 
				factory.location().mapLocation().getX(), factory.location().mapLocation().getY(), unit, factory.id());
		
		return p;
	}
	
	private Path findPathToLocation(Unit unit, MapLocation loc) throws Exception {
		resetAStarValues();
		Path p = scout.aStar(unit.location().mapLocation().getX(), unit.location().mapLocation().getY(), 
				loc.getX(), loc.getY(), unit, -1);
		
		return p;
	}
	
	private MapLocation findClosestKarboniteLoc(Unit unit, MapLocation unitLoc) {
		resetAStarValues();
		return scout.aStarKarboniteLoc(unitLoc.getX(), unitLoc.getY(), unit, 55);
		
	}
	
	private Unit closestEnemyToUnit(Unit unit, VecUnit enemies) {
		if (enemies.size() == 1)
			return enemies.get(0);
		
		Unit closest = enemies.get(0);
		int closestDist = distanceBetween(closest, unit);
			for (int i = 0; i < enemies.size(); i++) {
			int temp = AbsSub(enemies.get(i).location().mapLocation().getX(), unit.location().mapLocation().getX()) + AbsSub(enemies.get(i).location().mapLocation().getY(), unit.location().mapLocation().getY());
			if (temp < closestDist) {
				closestDist = temp;
				closest = enemies.get(i);
			}
		}
		return closest;
	}
	
	private Unit closestEnemyToUnit(Unit unit, ArrayList<Unit> enemies) {
		if (enemies.size() == 1)
			return enemies.get(0);
		
		Unit closest = enemies.get(0);
		int closestDist = distanceBetween(closest, unit);
			for (int i = 0; i < enemies.size(); i++) {
			int temp = AbsSub(enemies.get(i).location().mapLocation().getX(), unit.location().mapLocation().getX()) + AbsSub(enemies.get(i).location().mapLocation().getY(), unit.location().mapLocation().getY());
			if (temp < closestDist) {
				closestDist = temp;
				closest = enemies.get(i);
			}
		}
		return closest;
	}
	
	
	private int AbsSub(int a, int b) {
		return Math.abs(a - b);
	}
	
	private int Sqr(int x) {
		return x*x;
	}
	
	private int distanceSquaredBetween(Unit a, Unit b) {
		return Sqr(a.location().mapLocation().getX() - b.location().mapLocation().getX()) + Sqr(a.location().mapLocation().getY() - b.location().mapLocation().getY());	
	}
	
	private int distanceSquaredBetween(MapLocation a, Unit b) {
		return Sqr(a.getX() - b.location().mapLocation().getX()) + Sqr(a.getY() - b.location().mapLocation().getY());	
	}
	
	private int distanceSquaredBetween(MapLocation a, MapLocation b) {
		return Sqr(a.getX() - b.getX()) + Sqr(a.getY() - b.getY());	
	}
	
	private int distanceBetween(Unit a, Unit b) {
		return AbsSub(a.location().mapLocation().getX(), b.location().mapLocation().getX()) + AbsSub(a.location().mapLocation().getY(), b.location().mapLocation().getY());	
	}
	
	private int distanceBetween(Unit a, MapLocation b) {
		return AbsSub(a.location().mapLocation().getX(), b.getX()) + AbsSub(a.location().mapLocation().getY(), b.getY());	
	}
	
	private int distanceBetween(MapLocation a, MapLocation b) {
		return AbsSub(a.getX(), b.getX()) + AbsSub(a.getY(), b.getY());	
	}
	
	private int distanceBetweenSteps(Path.Step a, Path.Step b) {
		return AbsSub(a.getX(), b.getX()) + AbsSub(a.getY(), b.getY());	
	}
	
	
	public static boolean checkIfOccupiable(MapLocation m) {
		try {
		////System.out.println("Location " + m.getX() + ", " + m.getY() + " isOccupiable=" + controller.isOccupiable(m));
		if (controller.isOccupiable(m) > 0)
			return true;
		else
			return false;
		} catch (Exception ex)
		{
			//System.out.print("Location " + m.getX() + ", " + m.getY() + " out of range...");
			return false;
		}
	}
	
	public static boolean checkIfOccupied(MapLocation m) {
		//Unit u = controller.senseUnitAtLocation(m);
		////System.out.println("Location " + m.getX() + ", " + m.getY() + " hasUnitAt=" + controller.hasUnitAtLocation(m));
		return controller.hasUnitAtLocation(m);		
//		try {
//			Unit u = controller.senseUnitAtLocation(m);
//			if (u instanceof Unit) {
//				return true;
//			} else {
//				return false;
//			}
//		} catch (Exception ex) {
//			//System.out.println("A Unit sits in this location");
//			return false;
//		}
	}
	
	public static boolean canSeeLocation(MapLocation m) {
		return controller.canSenseLocation(m);
	}
	
//	private void closestVisibleLocTo(MapLocation m) {
//		
//	}
	
	
	private MapLocation getSymmetricalLoc(MapLocation m) {
		int planetWidth;
		int planetHeight;
		int x;
		int y;
		
		if (m.getPlanet() == Planet.Earth) {
			planetWidth = earthWidth;
			planetHeight = earthHeight;
		} else {
			planetWidth = marsWidth;
			planetHeight = marsHeight;
		}
		
		x = planetWidth - 1 - m.getX();
		y = planetHeight - 1 - m.getY();
		
		return new MapLocation(m.getPlanet(), x, y);		
	}
	


	
	private boolean tryMove(int id, Direction dir) throws Exception {
		if (controller.canMove(id, dir) && controller.isMoveReady(id)) {
			if (controller.isOccupiable(controller.unit(id).location().mapLocation().add(dir)) > 0) {
				controller.moveRobot(id, dir);
				//System.out.println("Moved unit. new Location: " + controller.unit(id).location().mapLocation());
				return true;
			}
		}
		return false;
	}
	
	
	private boolean tryMoveFuzzy(int id, Direction dir) throws Exception {
		
		if (!tryMove(id, dir)) {
			int d = dirToInt(dir);
			int l = d - 1;
			if (l < 0) {
				l = 7;
			}
			if (!tryMove(id, intToDir(l))) {
				int r = d + 1;
				if (r > 7) {
					r = 0;
				}
				return tryMove(id, intToDir(r));
				
			} else {
				return true;
			}
			
		} else {
			return true;
		}
		
	}
	
	
	private boolean tryAttack(Unit unit, Unit enemy) throws Exception {
		if (controller.isAttackReady(unit.id())) {
			if (controller.canAttack(unit.id(), enemy.id())) {
				controller.attack(unit.id(), enemy.id());
				return true;
			}
		}
		return false;
	}

	private boolean tryAttack(int unitId, int enemyId) throws Exception {
		if (controller.isAttackReady(unitId)) {
			if (controller.canAttack(unitId, enemyId)) {
				controller.attack(unitId, enemyId);
				return true;
			}
		}
		return false;
	}
	
	
	private boolean tryBuild(int unitId, int structId) throws Exception {
		if (controller.canBuild(unitId, structId)) {
				controller.build(unitId, structId);
				return true;
		}
		return false;
	}
	
	private boolean tryHarvest(int unitId, Unit unit, MapLocation karboniteLoc) throws Exception {
		MapLocation unitLoc = unit.location().mapLocation();
		Direction dir = unitLoc.directionTo(karboniteLoc);
		int x = unitLoc.getX();
		int y = unitLoc.getY();
		if (controller.canHarvest(unitId, dir)) {
			controller.harvest(unitId, dir);
			earthTiles[x][y].setInitKarbonite(earthTiles[x][y].getInitKarbonite() - workerHarvestAmount);
			return true;
		}
		return false;
	}
	
	private MapLocation PathStepToMapLoc(Planet p, Path path, int stepIndex) {
		return new MapLocation(p, path.getX(stepIndex), path.getY(stepIndex));
	}
		
	
	private void SetTeamVars() {
		myTeam = controller.team();
		if (myTeam.equals(Team.Red))
			enemyTeam = Team.Blue;
		else
			enemyTeam = Team.Red;
		//System.out.println("My Team: " + myTeam.toString() + ", Enemy Team: " + enemyTeam.toString());
	}
	
	private void getEarthMapValues() {
		earthStartMap = controller.startingMap(Planet.Earth);
    	earthWidth = (int)earthStartMap.getWidth();
    	earthHeight = (int)earthStartMap.getHeight();
    	//System.out.println("Earth size: " + earthWidth + ", " + earthHeight);
	}
	
	private void getMarsMapValues() {
		marsStartMap = controller.startingMap(Planet.Mars);
    	marsWidth = (int)marsStartMap.getWidth();
    	marsHeight = (int)marsStartMap.getHeight();
    	//System.out.println("Mars size: " + marsWidth + ", " + marsHeight);
	}
	
	private Tile[][] InitEarthTiles() {
		earthTiles = new Tile[earthWidth][];
    	for (int i = 0; i < earthTiles.length; i++) {
    		earthTiles[i] = new Tile[earthHeight];
    	}
    	
    	//System.out.println("EarthTiles length = " + earthTiles.length + ", EarthTiles[0] length:" + earthTiles[0].length);
    	return earthTiles;
	}
	
	private Tile[][] InitMarsTiles() {
		marsTiles = new Tile[marsWidth][];
    	for (int i = 0; i < marsTiles.length; i++) {
    		marsTiles[i] = new Tile[marsHeight];
    	}
    	
    	//System.out.println("MarsTiles length = " + marsTiles.length + ", MarsTiles[0] length:" + marsTiles[0].length);
    	return marsTiles;
	}
	
	private void findAllPassableEarth() {
		for (int x = 0; x < earthWidth; x++) {
    		for (int y = 0; y < earthHeight; y++) {
    			earthTiles[x][y] = new Tile(Planet.Earth, x, y);
//    			MapLocation temp = new MapLocation(Planet.Earth, x, y);
//    			temp.setX(x);
//    			temp.setY(y);
        		if (earthStartMap.isPassableTerrainAt(earthTiles[x][y].mapLoc) == 0) {
        			earthTiles[x][y].setIsPassable(false);
        			////System.out.println("Location " + x + ", " + y + " is not passable.");
        		}
        	}
    	}
	}
	

	private void findAllPassableMars() {
		for (int x = 0; x < marsWidth; x++) {
    		for (int y = 0; y < marsHeight; y++) {
    			marsTiles[x][y] = new Tile(Planet.Mars, x, y);
//    			MapLocation temp = new MapLocation(Planet.Mars, x, y);
//    			temp.setX(x);
//    			temp.setY(y);
        		if (marsStartMap.isPassableTerrainAt(marsTiles[x][y].mapLoc) == 0) {
        			marsTiles[x][y].setIsPassable(false);
        			////System.out.println("Location " + x + ", " + y + " is not passable.");
        		}
        	}
    	}
	}
	
	private void findAllStartingKarbonite() {
		for (int x = 0; x < earthWidth; x++) {
    		for (int y = 0; y < earthHeight; y++) {
    			MapLocation temp = new MapLocation(Planet.Earth, x, y);
    			int k = (int) earthStartMap.initialKarboniteAt(temp);
        		if (k > 0) {
        			earthTiles[x][y].setInitKarbonite(k);
        			totalStartingKarbonite += k;
        			////System.out.println("Location " + x + ", " + y + " initKarbonite=" + earthTiles[x][y].getInitKarbonite());
        		}
        	}
    	}
	}
	
	private void InitAStar() {
		scout = new AStar(true);
		//scout.setTiles(earthTiles);
	}
	
	private void resetAStarValues() {
		if (controller.planet() == Planet.Earth) {
			for (int x = 0; x < earthTiles.length; x++) {
	    		for (int y = 0; y < earthTiles[x].length; y++) {
	    			earthTiles[x][y].setSearched(false);
	    			earthTiles[x][y].cost = 0;
	    			earthTiles[x][y].posInPath = 0;    	
	    			//earthTiles[x][y].clearParent();
	    		}
			}
		} else {
			for (int x = 0; x < marsTiles.length; x++) {
	    		for (int y = 0; y < marsTiles[x].length; y++) {
	    			marsTiles[x][y].setSearched(false);
	    			marsTiles[x][y].cost = 0;
	    			marsTiles[x][y].posInPath = 0;
	    			//earthTiles[x][y].clearParent();
	    		}
			}
		}
		
	}
	
	private void GetStartingLocations(VecUnit myUnits) {
		if (startingWorkers == 1) {
			startingLoc = myUnits.get(0).location().mapLocation();
			symStartingLocations.add(getSymmetricalLoc(startingLoc));
		} else {
			for (int i = 0; i < startingWorkers; i++) {
				startingLocations.add(controller.myUnits().get(i).location().mapLocation());
				
			}
			
			
			for (MapLocation m : startingLocations) {
				symStartingLocations.add(getSymmetricalLoc(m));
			}


		}  
		
	}
	
//	private void GetStartingLocationsMars() {
//		if (startingWorkers == 1) {
//			startingLoc = myUnits.get(0).location().mapLocation();
//			symStartingLocationsMars.add(getSymmetricalLoc(startingLoc));
//		} else {
//			for (int i = 0; i < startingWorkers; i++) {
//				startingLocationsMars.add(controller.myUnits().get(i).location().mapLocation());
//				
//			}
//			
//			
//			for (MapLocation m : startingLocationsMars) {
//				symStartingLocationsMars.add(getSymmetricalLoc(m));
//			}
//		}    
//		
//		
//	}
	
	private boolean replicateAnywhere(int id) {
		if (controller.canReplicate(id, Direction.North)) {
			controller.replicate(id, Direction.North);
			return true;
		}
		else if (controller.canReplicate(id, Direction.Northeast)) {
			controller.replicate(id, Direction.Northeast);
			return true;
		}
		else if (controller.canReplicate(id, Direction.East)) {
        	controller.replicate(id, Direction.East);
        	return true;
		}
		else if (controller.canReplicate(id, Direction.Southeast)) {
			controller.replicate(id, Direction.Southeast);
			return true;
		}
		else if (controller.canReplicate(id, Direction.South)) {
        	controller.replicate(id, Direction.South);
        	return true;
		}
		else if (controller.canReplicate(id, Direction.Southwest)) {
			controller.replicate(id, Direction.Southwest);
			return true;
		}
		else if(controller.canReplicate(id, Direction.West)) {
        	controller.replicate(id, Direction.West);
        	return true;
		}
		else if (controller.canReplicate(id, Direction.Northwest)) {
			controller.replicate(id, Direction.Northwest);
			return true;
		}
		return false;
	}
	
	
	
	private void unloadUnitAnywhere(int structId) {
		if (controller.canUnload(structId, Direction.North))
			controller.unload(structId, Direction.North);
		else if (controller.canUnload(structId, Direction.Northeast))
			controller.unload(structId, Direction.Northeast);
		else if (controller.canUnload(structId, Direction.East))
			controller.unload(structId, Direction.East);
		else if (controller.canUnload(structId, Direction.Southeast))
			controller.unload(structId, Direction.Southeast);
		else if (controller.canUnload(structId, Direction.South))
			controller.unload(structId, Direction.South);
		else if (controller.canUnload(structId, Direction.Southwest))
			controller.unload(structId, Direction.Southwest);
		else if (controller.canUnload(structId, Direction.West))
			controller.unload(structId, Direction.West);
		else if (controller.canUnload(structId, Direction.Northwest))
			controller.unload(structId, Direction.Northwest);
	}
	
	private boolean blueprintUnitAnywhere(int unitId, UnitType structType) {
		if (controller.canBlueprint(unitId, structType, Direction.North)) {
			controller.blueprint(unitId, structType, Direction.North);
			return true;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.Northeast)) {
			controller.blueprint(unitId, structType, Direction.Northeast);
			return true;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.East)) {
			controller.blueprint(unitId, structType, Direction.East);
			return true;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.Southeast)) {
			controller.blueprint(unitId, structType, Direction.Southeast);
			return true;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.South)) {
			controller.blueprint(unitId, structType, Direction.South);
			return true;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.Southwest)) {
			controller.blueprint(unitId, structType, Direction.Southwest);
			return true;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.West)) {
			controller.blueprint(unitId, structType, Direction.West);
			return true;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.Northwest)) {
			controller.blueprint(unitId, structType, Direction.Northwest);		
			return true;
		}
		return false;	
	}
	
	private Direction blueprintUnitAnywhereGetDir(int unitId, UnitType structType) {
		if (controller.canBlueprint(unitId, structType, Direction.North)) {
			controller.blueprint(unitId, structType, Direction.North);
			return Direction.North;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.Northeast)) {
			controller.blueprint(unitId, structType, Direction.Northeast);
			return Direction.Northeast;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.East)) {
			controller.blueprint(unitId, structType, Direction.East);
			return Direction.East;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.Southeast)) {
			controller.blueprint(unitId, structType, Direction.Southeast);
			return Direction.Southeast;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.South)) {
			controller.blueprint(unitId, structType, Direction.South);
			return Direction.South;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.Southwest)) {
			controller.blueprint(unitId, structType, Direction.Southwest);
			return Direction.Southwest;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.West)) {
			controller.blueprint(unitId, structType, Direction.West);
			return Direction.West;
		}
		else if (controller.canBlueprint(unitId, structType, Direction.Northwest)) {
			controller.blueprint(unitId, structType, Direction.Northwest);		
			return Direction.Northwest;
		}
		return Direction.Center;
	}

	private boolean unloadHere(int structId, Direction dir) {
		if (controller.canUnload(structId, dir)) {
			controller.unload(structId, dir);
			return true;
		}  else {
			return false;
		}
	}
	
	private Direction randomDir() {
		Direction d = Direction.North;
		int n = random.nextInt(8);
		switch (n) {
			case 0:
				d = Direction.North;
				break;
			case 1:
				d = Direction.Northeast;
				break;
			case 2:
				d = Direction.East;
				break;
			case 3:
				d = Direction.Southeast;
				break;
			case 4:
				d = Direction.South;
				break;
			case 5:
				d = Direction.Southwest;
				break;
			case 6:
				d = Direction.West;
				break;
			case 7:
				d = Direction.Northwest;
				break;			
		}
		
		return d;
		
		
		/*
		 *North(0),
		  Northeast(1),
		  East(2),
		  Southeast(3),
		  South(4),
		  Southwest(5),
		  West(6),
		  Northwest(7),
		  Center(8);
		 */
	}

//	private VecUnit getAllEnemyUnits() {
//		VecUnit e = new VecUnit();
//		for (int i = 0; i < controller.units().size(); i++) {
//			
//		}
//	}

	
	
	
	public static class Channel {
		public enum name { AttackChargeReady, RocketReady, EnemyUnitX, EnemyUnitY }
	}


}
