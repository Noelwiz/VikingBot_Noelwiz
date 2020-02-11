import bwapi.*;
import bwta.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ExampleBot extends DefaultBWListener {

    private BWClient bwClient;
    private Game game;
    private Player self;

    private HashMap<UnitType, java.lang.Integer> unitMemory = new HashMap<UnitType, java.lang.Integer>();
    private HashSet<Position> enemyBuildingMemory = new HashSet<Position>();
    private int baseLoc = 0;
    private ArrayList<Integer> scouts = new ArrayList<Integer>();

    public void run() {
        bwClient = new BWClient(this);
        bwClient.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("Created: " + unit.getType());
    }

    @Override
    public void onStart() {
        game = bwClient.getGame();
        self = game.self();

        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap(game);
        BWTA.analyze();
        System.out.println("Map data ready");

        int i = 0;
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
            System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
//            for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
//                System.out.print(position + ", ");
//            }
            System.out.print(baseLocation.getPosition().toString());
            System.out.println();
        }

    }

    @Override
    public void onFrame() {
        //game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
        game.drawTextScreen(10, 230, "Resources: " + self.minerals() + " minerals,  " + self.gas() + " gas, " + (self.supplyUsed() / 2) + "/" + (self.supplyTotal() / 2) + " psi");

        tabulateUnits ();
        updateEnemyBuildingMemory();
        displayUnits ();

        // If we're not at max population capacity
        if (self.supplyTotal() < 400) {
            //if we're running out of supply and have enough minerals ...
            if ((self.supplyTotal() - self.supplyUsed() <= 4) && (self.minerals() >= 100)) {
                game.drawTextScreen(10, 240, "Need to increase PSI");
                expandPopulationCapacity();
            }
        }

        // build Forge / Cannons
        if (self.minerals() >= 150) {
            // Check if we have any forges built
            game.drawTextScreen(10, 260, "Forge available");
            if (unitMemory.containsKey(UnitType.Protoss_Forge)) {
                if ((unitMemory.get(UnitType.Protoss_Forge) - getBuildingUnitsOfType(UnitType.Protoss_Forge)) > 0) {
                    if (unitMemory.containsKey(UnitType.Protoss_Photon_Cannon)) {
                        game.drawTextScreen(10, 270, String.format("Can build %d more Photon Cannons", (2 * unitMemory.get(UnitType.Protoss_Pylon)) - unitMemory.get(UnitType.Protoss_Photon_Cannon)));
                        if (unitMemory.get(UnitType.Protoss_Photon_Cannon) < (2 * unitMemory.get(UnitType.Protoss_Pylon))) {
                            buildPhotonCannon();
                        }
                    } else {
                        buildPhotonCannon();
                    }
                }
            } else {
                buildForge();
            }
        }
        
        // build Gateway
        if(self.minerals() >= 150) {
        	// Check if there is already a Gateway
        	game.drawTextScreen(10, 250, "Gateway Avalaible");
        	if(unitMemory.containsKey(UnitType.Protoss_Gateway)) { 
	    		if(unitMemory.get(UnitType.Protoss_Gateway) + getBuildingUnitsOfType(UnitType.Protoss_Gateway) < 2) {
	    			buildGateway();
	    		}
        	} else {
        		buildGateway();
        	}
        }
        
        //if it's time to attack the enemy base
        if (getUnitsOfType(UnitType.Protoss_Zealot) > 30) {
        	attackEnemyBase();
        }
        
        //if it's a worker and we have over 10 then send it as a scout
        if (getUnitsOfType(UnitType.Protoss_Probe) >= 8) {
        	if (baseLoc < BWTA.getStartLocations().size()) {
	    		Unit scout = getAvailableWorker();
	    		findEnemyBase(scout, BWTA.getStartLocations().get(baseLoc));
	    		scouts.add(scout.getID());
	    		baseLoc++;
        	}
    	}

        //iterate through my units
        for (Unit myUnit : self.getUnits()) {
            drawUnitPathing(myUnit);

            if (myUnit.getType() == UnitType.Protoss_Nexus && myUnit.isUnderAttack()) {
                attackPosition(myUnit.getPosition());
            }

            //if there's enough minerals, train a Probe
            if (myUnit.getType() == UnitType.Protoss_Nexus && self.minerals() >= 50 && (getUnitsOfType(UnitType.Protoss_Probe) < 12 || unitMemory.containsKey(UnitType.Protoss_Gateway))) {
                if (self.supplyTotal() - self.supplyUsed() > 4 && getUnitsOfType(UnitType.Protoss_Probe) < 12) {
                    myUnit.train(UnitType.Protoss_Probe);
                }
            }
            
            //if there's enough minerals, train a Zealot
            if (myUnit.getType() == UnitType.Protoss_Gateway && self.minerals() >= 100) {
            	if	(self.supplyTotal() - self.supplyUsed() > 4) {
            		myUnit.train(UnitType.Protoss_Zealot);
            	}
            } 
            
            //if it's a worker and it's idle, send it to the closest mineral patch
            if (myUnit.getType().isWorker() && myUnit.isIdle()) {
            	boolean isScout = false;
            	for(int IDs: scouts) {
            		if (myUnit.getID() == IDs) {
            			isScout = true;
            		}
            	}
            	
            	if(isScout) {
            		if(!enemyBuildingMemory.isEmpty()) {
        				for(Unit unit: self.getUnits()) {
        					if(unit.getType() == UnitType.Protoss_Nexus) {
        						gatherMinerals(myUnit, unit);
        					}
        				}
        			}
            	} else {
                	gatherMinerals(myUnit);
                }
            }
        }
    }

    /*
     * TODO: DONE
     */
    private void tabulateUnits () {
        unitMemory.clear();

        for (Unit unit : self.getUnits()) {
            if (unit.isTraining()) {
                continue;
            }

            updateUnitMemory(unit.getType(), 1);
        }
    }

    private void displayUnits () {
        int xPos = 10;
        int yPos = 20;
        int maxUnitsToDisplay = 15;
        int i = 0;
        String unitStringFormat = "%s %d";

        for (UnitType type : unitMemory.keySet()) {
            if (i >= maxUnitsToDisplay) {
                break;
            }

            game.drawTextScreen(xPos, yPos, String.format(unitStringFormat, type.toString(), unitMemory.get(type)));

            yPos += 10;
            i++;
        }
    }

    private void drawUnitPathing (Unit unit) {
        if (unit.getOrderTargetPosition().getX() == 0 && unit.getOrderTargetPosition().getY() == 0) {
            return;
        }

        // Draw unit's order pathing
        if (unit.isGatheringMinerals() || unit.isCarrying()) {
            game.drawLineMap(unit.getPosition().getX(), unit.getPosition().getY(), unit.getOrderTargetPosition().getX(),
                    unit.getOrderTargetPosition().getY(), Color.Blue);
        }
        else if (unit.isIdle()) {
            game.drawLineMap(unit.getPosition().getX(), unit.getPosition().getY(), unit.getOrderTargetPosition().getX(),
                    unit.getOrderTargetPosition().getY(), Color.Black);
        }
        else if (unit.isConstructing() || unit.isBeingConstructed()) {
            game.drawLineMap(unit.getPosition().getX(), unit.getPosition().getY(), unit.getOrderTargetPosition().getX(),
                    unit.getOrderTargetPosition().getY(), Color.Green);
        }
        else {
            game.drawLineMap(unit.getPosition().getX(), unit.getPosition().getY(), unit.getOrderTargetPosition().getX(),
                    unit.getOrderTargetPosition().getY(), Color.White);
        }

        if (unit.isAttacking() || unit.isUnderAttack()) {
            game.drawLineMap(unit.getPosition().getX(), unit.getPosition().getY(), unit.getOrderTargetPosition().getX(),
                    unit.getOrderTargetPosition().getY(), Color.Red);
        }
    }

    /*
     * TODO: DONE
     */
    private void updateUnitMemory (UnitType type, int amount) {
        if (unitMemory.containsKey(type)) {
            unitMemory.put(type, unitMemory.get(type) + amount);
        }
        else {
            unitMemory.put(type, amount);
        }
    }
    
    /*
     * TODO: DONE
     */
    private void updateEnemyBuildingMemory () {
    	// update the hashset of enemy building positions
        for (Unit enemyUnit: game.enemy().getUnits()) {
        	if (enemyUnit.getType().isBuilding()) {
        		if(!enemyBuildingMemory.contains(enemyUnit.getPosition())) {
        			enemyBuildingMemory.add(enemyUnit.getPosition());
        		}
        	}
        }
        
        //remove any destroyed buildings from the memory
        for (Position pos: enemyBuildingMemory) {
        	TilePosition tileCorrespondingToPos = new TilePosition(pos.getX()/32, pos.getY()/32);
        	
        	if(game.isVisible(tileCorrespondingToPos)) {
        		boolean buildingStillThere = false;
        		for (Unit enemyUnit: game.enemy().getUnits()) {
        			if(enemyUnit.getType().isBuilding() && enemyUnit.getOrderTargetPosition().equals(pos)) {
        				buildingStillThere = true;
        				break;
        			}
        		}
        		
        		if (buildingStillThere == false) {
        			enemyBuildingMemory.remove(pos);
        			break;
        		}
        	}
        }
    }

    /*
     * TODO: DONE
     */
    private void gatherMinerals (Unit worker) {
        Unit closestMineral = null;

        //find the closest mineral
        for (Unit neutralUnit : game.neutral().getUnits()) {
            if (neutralUnit.getType().isMineralField()) {
                if (closestMineral == null || worker.getDistance(neutralUnit) < worker.getDistance(closestMineral)) {
                    closestMineral = neutralUnit;
                }
            }
        }

        //if a mineral patch was found, send the worker to gather it
        if (closestMineral != null) {
            worker.gather(closestMineral, false);
        }
    }

    /*
     * TODO: DONE
     */
    private void gatherMinerals (Unit worker, Unit base) {
        Unit closestMineral = null;

        //find the closest mineral
        for (Unit neutralUnit : game.neutral().getUnits()) {
            if (neutralUnit.getType().isMineralField()) {
                if (closestMineral == null || base.getDistance(neutralUnit) < base.getDistance(closestMineral)) {
                    closestMineral = neutralUnit;
                }
            }
        }

        //if a mineral patch was found, send the worker to gather it
        if (closestMineral != null) {
            worker.gather(closestMineral, false);
        }
    }

    /*
     * TODO: DONE
     */
    private Unit getAvailableWorker () {
        // Find an available worker
        for (Unit unit : self.getUnits()) {
            if (unit.getType().isWorker() && !scouts.contains(unit.getID())) {
                return unit;
            }
        }

        return null;
    }
    
    /*
     * TODO: DONE
     */
    private int getUnitsOfType (UnitType type) {
    	int numOfUnits = 0;
    	
    	for (Unit unit : self.getUnits()) {
    		if (unit.getType() == type) {
    			numOfUnits++;
    		}
    	}
    	
    	return numOfUnits;
    }

    /*
     * TODO: DONE
     */
    private boolean isUnitInRadius (Position position, int radius, UnitType type) {
        List<Unit> units;
        units = game.getUnitsInRadius(position, radius);

        for (Unit unit : units) {
            if (unit.getType() == type) {
                return true;
            }
        }

        return false;
    }
    /*
     * TODO: DONE
     */
    private int getBuildingUnitsOfType (UnitType type) {
        int numberOfBuildingUnits = 0;

        for (Unit unit : self.getUnits()) {
            if (unit.getType() == type) {
                if (unit.isBeingConstructed()) {
                    numberOfBuildingUnits++;
                }
            }
        }

        return numberOfBuildingUnits;
    }

    /*
     * TODO: DONE
     */
    private void expandPopulationCapacity () {
        Unit worker;
        worker = getAvailableWorker();

        if (worker != null) {
            //get a nice place to build a supply depot
            TilePosition buildTile = game.getBuildLocation(UnitType.Protoss_Pylon, self.getStartLocation());
            //and, if found, send the worker to build it (and leave others alone - break;)
            if (buildTile != null) {
                worker.build(UnitType.Protoss_Pylon, buildTile);
            }
        }
    }

    /*
     * TODO: DONE
     */
    private Unit getPylon () {
        for (Unit unit : self.getUnits()) {
            if (unit.getType() == UnitType.Protoss_Pylon) {
                // Other checks in here?
                return unit;
            }
        }

        return null;
    }

    /*
     * TODO: DONE
     */
    private Unit getPylonWithoutType (UnitType type) {
        for (Unit unit : self.getUnits()) {
            if (unit.getType() == UnitType.Protoss_Pylon) {
                if (!isUnitInRadius(unit.getPosition(), 16, type)) {
                    return unit;
                }
            }
        }

        return null;
    }
    
    /*
     * TODO: DONE
     */
    private void buildGateway () {
    	Unit worker;

    	worker = getAvailableWorker();

    	if((worker != null)) {
    		TilePosition buildTile = game.getBuildLocation(UnitType.Protoss_Gateway, self.getStartLocation());

    		if(buildTile != null) {
    			worker.build(UnitType.Protoss_Gateway, buildTile);
    		}
    	}
    }

    /*
     * TODO: DONE
     */
    private void buildForge () {
       Unit worker, pylon;

       worker = getAvailableWorker();
       pylon = getPylon ();

        if ((worker != null) && pylon != null) {
           TilePosition buildTile = game.getBuildLocation(UnitType.Protoss_Forge, pylon.getTilePosition(), 16);

           if (buildTile != null) {
               worker.build(UnitType.Protoss_Forge, buildTile);
           }
        }
    }

    /*
     * TODO: DONE
     */
    private void buildPhotonCannon () {
        Unit worker, pylon;

        worker = getAvailableWorker();
        pylon = getPylonWithoutType(UnitType.Protoss_Photon_Cannon);

        if ((worker != null) && pylon != null) {
            TilePosition buildTile = game.getBuildLocation(UnitType.Protoss_Photon_Cannon, pylon.getTilePosition(), 16);

            if (buildTile != null) {
                worker.build(UnitType.Protoss_Photon_Cannon, buildTile);
            }
        }
    }
    
    /*
     * TODO: DONE
     */
    private void findEnemyBase (Unit scout, BaseLocation basePos) {
    	scout.attack(basePos.getPosition());
    }
    
    /*
     * TODO: DONE
     */
    private void attackEnemyBase () {
    	for (Unit myUnit: self.getUnits()) {
    		if(myUnit.getType() == UnitType.Protoss_Zealot) {
    			if(enemyBuildingMemory.iterator().hasNext()) {
    				myUnit.attack(enemyBuildingMemory.iterator().next());
    			} else {
    				myUnit.attack(BWTA.getStartLocations().get(BWTA.getStartLocations().size()).getPosition());
    			}
    		}
    	}
    }

    /*
     * TODO: DONE
     */
    private void attackPosition (Position targetPos) {
        if (unitMemory.containsKey(UnitType.Protoss_Dragoon) || unitMemory.containsKey(UnitType.Protoss_Zealot)) {
            for (Unit unit : self.getUnits()) {
                if (unit.getType() == UnitType.Protoss_Dragoon || unit.getType() == UnitType.Protoss_Zealot) {
                    unit.attack(targetPos, false);
                }
            }
        }
    }

    public static void main(String[] args) {
        new ExampleBot().run();
    }
}