package planning;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.model.FullModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.TransitionProb;
import bwapi.Race;
import bwapi.UnitType;
import knowledge.GeneralRaceProductionKnowledge;
import knowledge.ProtossGeneralKnowledge;
import knowledge.TerrenGeneralKnowledge;
import knowledge.ZergGeneralKnowledge;
import planning.actions.helpers.ActionParser;
import planning.actions.helpers.ProtossBuildingParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class StarcraftModel implements FullModel {
    private Random rng = new Random();
    /**
     * An implementation of the RewardFunction class from the Burlap API to
     * assign reward values to a given action based on the state
     */
    private RewardFunction rewardFunction;

    /**calculates the average time to train for randomizing the chance of a
     * unit finishing it's training. the units in the paranthesies are in
     * seconds, taken from the wiki.
     * incorperate more units the bot is likely to use in the furute.
     */
    private static final float AverageUnitTrainingTime = 30 * /* conversion from seconds to frames */
    (40 /* zelot*/ + 20 /* probe */ + 28 /*zergling */ + 40 /* overlord */ + 20 /* drone */)/5;

    /**
     * Constructor. Used to create a new Model
     * @param rf A reward function (see burlap api) to asign a reward value for various actions and outcomes.
     */
    public StarcraftModel(RewardFunction rf){
        rewardFunction = rf;
    }

    /**
     * Used to change the reward function actions are taken based off of.
     * @param rf A reward function (see burlap api) to asign a reward value for various actions and outcomes.
     */
    public void SetRewardFunction(RewardFunction rf){
        rewardFunction= rf;
    }


    /**
     * Possible transitions from the current state
     *
     * How it works:
     * Broadly the idea is to create a base state, with what's guaranteed to change
     * from an action being taken already incorporated. Then that base state is turned
     * into a TransitionProb with an assumed 100% chance of happening. After that the
     * TransitionProb is put in a list and sent through a bunch of functions that enumerate
     * normal possible occurrences during game play splitting each entry in the list into
     * multiple with the same total probability.
     * At the end of the function there should be a list with every reasonable permutation of what
     * could happen in the game, and with an approximate probability assigned.
     *
     * @param state current state
     * @param action action being considered
     * @return a list of most possible states that could be transitioned to.
     */
    @Override
    public List<TransitionProb> transitions(State state, Action action) {
        State baseNextState;
        List<TransitionProb> AllProbabilities = new ArrayList<TransitionProb>();
        EnvironmentOutcome defaultoutcome;

        //Get properties that don't change, or are modified a lot in different actions
        int[][] capacity = (int[][]) state.get("trainingCapacity");
        Race ourrace = (Race) state.get("playerRace");


        String actionstr = action.actionName();
        String[] arguments = actionstr.split("_");

        //System.out.println("Action taken: "+ arguments[0]);


        //aproximates time passing in an actual match between planing steps
        int newtimesincelastscout = (int) state.get("timeSinceLastScout");
        //newtimesincelastscout += Math.round(rng.nextFloat() * 30 * 600);


        /* * * start account for action specific changes * * */
        ActionParser.ActionEnum actiontype = ActionParser.GetActionType(action);
        switch (actiontype){
            case SCOUT:
                //maybe consider changing this to be a negitive number or something
                //to represent that the scout is on the way. idk. 0 is probably fine,
                //just tell the helper functions for the possibility of a new enemy base,
                //and new enemy workers.
                //newtimesincelastscout = 0;

                baseNextState = new PlanningState( (int) state.get("numWorkers"), (int) state.get("mineralProductionRate"),
                        (int) state.get("gasProductionRate"), (int) state.get("numBases"), newtimesincelastscout,
                        (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                        (int)state.get("numEnemyBases"), (UnitType)state.get("mostCommonCombatUnit"),
                        (boolean) state.get("attackingEnemyBase"),(boolean) state.get("beingAttacked"), (Race) state.get("playerRace"),(Race) state.get("enemyRace"),(GameStatus) state.get("gameStatus"),
                        capacity, (int) state.get("populationCapacity"), (int) state.get("populationUsed"), (HashMap<UnitType, Integer>) state.get("unitMemory"));


                //Todo: maybe expand the possibilites here.
                defaultoutcome = new EnvironmentOutcome(state, action,baseNextState, rewardFunction.reward(state,action,baseNextState),terminal(baseNextState));
                AllProbabilities.add(new TransitionProb(1, defaultoutcome));
                break;
            case EXPAND:
                int numworkers = (int) state.get("numWorkers");
                int numBases = (int) state.get("numBases");
                int mineralproduction = (int) state.get("mineralProductionRate");
                numBases++;

                if(ourrace == Race.Zerg){
                    capacity[0][1] += 3;
                    capacity[1][1] += 3;
                    capacity[2][1] += 3;
                    capacity[3][1] += 3;

                    baseNextState = new PlanningState( numworkers-1, mineralproduction-57,
                            (int) state.get("gasProductionRate"), numBases, newtimesincelastscout,
                            (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                            (int)state.get("numEnemyBases"), (UnitType)state.get("mostCommonCombatUnit"),
                            (boolean) state.get("attackingEnemyBase"),(boolean) state.get("beingAttacked"), ourrace,(Race) state.get("enemyRace"),(GameStatus) state.get("gameStatus"),
                            capacity, (int) state.get("populationCapacity"), (int) state.get("populationUsed"), (HashMap<UnitType, Integer>) state.get("unitMemory"));
                } else {
                    baseNextState = new PlanningState( numworkers, mineralproduction,
                            (int) state.get("gasProductionRate"), numBases, newtimesincelastscout,
                            (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                            (int)state.get("numEnemyBases"), (UnitType)state.get("mostCommonCombatUnit"),
                            (boolean) state.get("attackingEnemyBase"),(boolean) state.get("beingAttacked"), (Race) state.get("playerRace"),(Race) state.get("enemyRace"),(GameStatus)state.get("gameStatus"),
                            capacity, (int) state.get("populationCapacity"), (int) state.get("populationUsed"), (HashMap<UnitType, Integer>) state.get("unitMemory"));
                }


                defaultoutcome = new EnvironmentOutcome(state, action,baseNextState, rewardFunction.reward(state,action,baseNextState),terminal(baseNextState));
                AllProbabilities.add(new TransitionProb(1, defaultoutcome));
                break;

            case BUILD:
                numworkers = (int) state.get("numWorkers");
                mineralproduction = (int) state.get("mineralProductionRate");
                UnitType what = ProtossBuildingParser.translateBuilding(action.actionName());
                int popcapchange = 0;
                if(what == UnitType.Protoss_Pylon){
                    popcapchange += 8;
                }

                if(what == UnitType.Protoss_Gateway){
                    capacity[1][1] += 1;
                }

                //TODO: also consider the reduction in resource production temporarily
                //reduce the output of mineral or gas production by the amount one unit normally produces.
                if(ourrace == Race.Zerg){
                    baseNextState = new PlanningState( numworkers-1, mineralproduction-57,
                            (int) state.get("gasProductionRate"), (int) state.get("numBases"), newtimesincelastscout,
                            (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                            (int)state.get("numEnemyBases"), (UnitType)state.get("mostCommonCombatUnit"),
                            (boolean) state.get("attackingEnemyBase"), (boolean) state.get("beingAttacked"), ourrace,(Race) state.get("enemyRace"),(GameStatus) state.get("gameStatus"),
                            capacity, (int) state.get("populationCapacity") + popcapchange, (int) state.get("populationUsed"), (HashMap<UnitType, Integer>) state.get("unitMemory"));
                } else {
                    baseNextState = new PlanningState( numworkers, mineralproduction,
                            (int) state.get("gasProductionRate"), (int) state.get("numBases"), newtimesincelastscout,
                            (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                            (int)state.get("numEnemyBases"), (UnitType)state.get("mostCommonCombatUnit"),
                            (boolean) state.get("attackingEnemyBase"), (boolean) state.get("beingAttacked"), (Race) state.get("playerRace"),(Race) state.get("enemyRace"),(GameStatus)state.get("gameStatus"),
                            capacity, (int) state.get("populationCapacity") + popcapchange, (int) state.get("populationUsed"), (HashMap<UnitType, Integer>) state.get("unitMemory"));
                }


                defaultoutcome = new EnvironmentOutcome(state, action,baseNextState, rewardFunction.reward(state,action,baseNextState),terminal(baseNextState));
                AllProbabilities.add(new TransitionProb(1, defaultoutcome));
                break;

            case ATTACK:
                boolean attackingenemybase = true;
                for( int i  = 0; i < arguments.length; i++) {
                    if(arguments[i] != null && !arguments[i].isEmpty() && arguments[i].startsWith("what=")){
                        String target = arguments[i].substring("what=".length());
                        if(target.equals("army") || target.equals("defend")){
                            attackingenemybase = false;
                        }
                    }
                }

                baseNextState = new PlanningState( (int) state.get("numWorkers"), (int) state.get("mineralProductionRate"),
                        (int) state.get("gasProductionRate"), (int) state.get("numBases"), newtimesincelastscout,
                        (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                        (int)state.get("numEnemyBases"), (UnitType)state.get("mostCommonCombatUnit"),
                        attackingenemybase, (boolean) state.get("beingAttacked"),(Race) state.get("playerRace"),(Race) state.get("enemyRace"),(GameStatus)state.get("gameStatus"),
                        capacity, (int) state.get("populationCapacity"), (int) state.get("populationUsed"), (HashMap<UnitType, Integer>) state.get("unitMemory"));


                //TODO: add possibility of deleting an enemy base
                //TODO: add posibilites of unit death
                //add the change for just attacking.
                defaultoutcome = new EnvironmentOutcome(state, action,baseNextState, rewardFunction.reward(state,action,baseNextState),terminal(baseNextState));

                if(attackingenemybase){
                    AllProbabilities.add(new TransitionProb(0.75, defaultoutcome));
                } else {
                    AllProbabilities.add(new TransitionProb(1, defaultoutcome));
                }

                //posibility of destroying an enemy base
                if(attackingenemybase){
                    baseNextState = new PlanningState( (int) state.get("numWorkers"), (int) state.get("mineralProductionRate"),
                            (int) state.get("gasProductionRate"), (int) state.get("numBases"), newtimesincelastscout,
                            (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                            ((int)state.get("numEnemyBases")) - 1, (UnitType)state.get("mostCommonCombatUnit"),
                            attackingenemybase, (boolean) state.get("beingAttacked"),(Race) state.get("playerRace"),(Race) state.get("enemyRace"),(GameStatus)state.get("gameStatus"),
                            capacity, (int) state.get("populationCapacity"), (int) state.get("populationUsed"), (HashMap<UnitType, Integer>) state.get("unitMemory"));

                    defaultoutcome = new EnvironmentOutcome(state, action,baseNextState, rewardFunction.reward(state,action,baseNextState),terminal(baseNextState));
                    AllProbabilities.add(new TransitionProb(0.25, defaultoutcome));
                }
                break;

            //TODO: GET A MORE DETAILED LOOK AT THE TRAINING ARGUMENTS TO ADJUST, especially for protoss
            case TRAIN:

                if(ourrace == Race.Zerg){
                    capacity[0][0] += 1;
                    capacity[0][1] -= 1;

                    capacity[1][0] += 1;
                    capacity[1][1] -= 1;

                    capacity[2][0] += 1;
                    capacity[2][1] -= 1;

                    capacity[3][0] += 1;
                    capacity[3][1] -= 1;


                    baseNextState = new PlanningState( (int) state.get("numWorkers"), (int) state.get("mineralProductionRate"),
                            (int) state.get("gasProductionRate"), (int) state.get("numBases"), newtimesincelastscout,
                            (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                            (int)state.get("numEnemyBases"), (UnitType)state.get("mostCommonCombatUnit"),
                            (boolean) state.get("attackingEnemyBase"), (boolean) state.get("beingAttacked"), ourrace,(Race) state.get("enemyRace"),(GameStatus) state.get("gameStatus"),
                            capacity, (int) state.get("populationCapacity"), (int) state.get("populationUsed") + 2, (HashMap<UnitType, Integer>) state.get("unitMemory"));
                } else {
                    //if we can train combat units, and rng thiks we're training combat units
                    //say we are
                    int workerchange = 0;
                    if(actionstr.contains("_what=worker")){
                        workerchange++;
                    }

                    if(capacity[1][1] > 0 && rng.nextBoolean()){
                        capacity[1][0] += 1;
                        capacity[1][1] -= 1;
                    } else {
                        capacity[0][0] += 1;
                        capacity[0][1] -= 1;
                    }

                    baseNextState = new PlanningState( (int) state.get("numWorkers") + workerchange, (int) state.get("mineralProductionRate") + 5,
                            (int) state.get("gasProductionRate"), (int) state.get("numBases"), newtimesincelastscout,
                            (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                            (int)state.get("numEnemyBases"), (UnitType)state.get("mostCommonCombatUnit"),
                            (boolean) state.get("attackingEnemyBase"),(boolean) state.get("beingAttacked"), (Race) state.get("playerRace"),(Race) state.get("enemyRace"),(GameStatus)state.get("gameStatus"),
                            capacity, (int) state.get("populationCapacity"), (int) state.get("populationUsed") + 2, (HashMap<UnitType, Integer>) state.get("unitMemory"));
                }


                defaultoutcome = new EnvironmentOutcome(state, action,baseNextState, rewardFunction.reward(state,action,baseNextState),terminal(baseNextState));
                AllProbabilities.add(new TransitionProb(1, defaultoutcome));
                break;

            //TODO after demo: probably take a more detailed look at these when we improve the bot
            case UPGRADE:
                baseNextState = state.copy();

                defaultoutcome = new EnvironmentOutcome(state, action,baseNextState, rewardFunction.reward(state,action,baseNextState),terminal(baseNextState));
                AllProbabilities.add(new TransitionProb(1, defaultoutcome));

                break;

            case GATHER:
                int mineralGatherRate = (int) state.get("mineralProductionRate");
                mineralGatherRate -= 5;
                int gasGatherRate = (int) state.get("gasProductionRate");
                gasGatherRate += 5;


                baseNextState = new PlanningState( (int) state.get("numWorkers"), mineralGatherRate,
                        gasGatherRate, (int) state.get("numBases"), newtimesincelastscout,
                        (ArrayList<CombatUnitStatus>)state.get("combatUnitStatuses"), (int) state.get("numEnemyWorkers"),
                        (int)state.get("numEnemyBases"), (UnitType)state.get("mostCommonCombatUnit"),
                        (boolean) state.get("attackingEnemyBase"),(boolean) state.get("beingAttacked"), (Race) state.get("playerRace"),(Race) state.get("enemyRace"),(GameStatus)state.get("gameStatus"),
                        capacity, (int) state.get("populationCapacity"), (int) state.get("populationUsed"), (HashMap<UnitType, Integer>) state.get("unitMemory"));

                defaultoutcome = new EnvironmentOutcome(state, action,baseNextState, rewardFunction.reward(state,action,baseNextState),terminal(baseNextState));

            case UNKNOWN:
                baseNextState = state.copy();

                defaultoutcome = new EnvironmentOutcome(state, action,baseNextState, rewardFunction.reward(state,action,baseNextState),terminal(baseNextState));
                AllProbabilities.add(new TransitionProb(1, defaultoutcome));

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + actiontype);
        }


        /* * * end account for action specific changes * * */

        /* * * start account for general changes * * */
        /* general notes: works by looping through current possibilities
         * and adding new ones based on them. So, the most effecient way is
         * to start with the things that add the least number of new options
         * and end with the ones that add the most.
         */

        //possibility of being attacked
        if(! ((boolean) state.get("attackingEnemyBase"))) {
            //TODO: Make this probability more reasonable. Check estimated # oponent units + game stage.
            AllProbabilities.addAll(attackedTransitions(state, action, AllProbabilities));
        }

        //distro of likelyhood of a new enemy base
        AllProbabilities.addAll(newEnemyBaseTransitions(state, action, AllProbabilities));

        //TODO: distribution of enemy workers likely to currently be owned
        /* Should be similar to the above enemy base transitions, where we calculate
         * if they can build a base, accept this enumartes the possible number of workers they
         * could have built in theory.
         */

        //posibility of units finishing training
        AllProbabilities.addAll(trainingCompleteProbabilities(state, action, AllProbabilities));

        /* * * end account for general changes * * */

        return AllProbabilities;
    }


    /**
     * Helper function to provide possible states if attacked.
     *
     * How it works: splits every list entery into two cases, either we're attacked or we're not.
     * @param Currentstate the current actual state
     * @param action the action to be taken
     * @param allProbabilities all possible resulting states and their probabilities so far.
     *                         WILL BE MODIFIED IN THE FUNCTION: inorder to adjust the probailities to
     *                         make sure they still add up to 1.
     * @return any new possibilities to add to the list of all probabilities outside the function.
     */
    private List<TransitionProb> attackedTransitions(State Currentstate, Action action, List<TransitionProb> allProbabilities) {
        double attackprob = 0.05;
        //IMPORTANT NOTE: the chance for us, or our enemy to lose a base is required for the planner to Sparse Sampling Planer
        //used to plan. without this, the bot will be stuck in a seemingly infinite loop on start trying to assign Q Values.
        double attackAndLooseBaseprob = 0.01;
        double noattackprob = 1-attackprob-attackAndLooseBaseprob;
        TransitionProb currentprob;
        State alternateState;

        List<TransitionProb> newProbabilities = new ArrayList<TransitionProb>();

        for(int i = 0; i < allProbabilities.size(); i++){
            currentprob = allProbabilities.get(i);
            currentprob.p = currentprob.p * noattackprob;

            //attacked
            alternateState = new PlanningState( (int) currentprob.eo.op.get("numWorkers"), (int) currentprob.eo.op.get("mineralProductionRate"),
                    (int) currentprob.eo.op.get("gasProductionRate"), (int) currentprob.eo.op.get("numBases"), (int) currentprob.eo.op.get("timeSinceLastScout"),
                    (ArrayList<CombatUnitStatus>)currentprob.eo.op.get("combatUnitStatuses"), (int) currentprob.eo.op.get("numEnemyWorkers"),
                    (int)currentprob.eo.op.get("numEnemyBases"), (UnitType)currentprob.eo.op.get("mostCommonCombatUnit"),
                    !(boolean) currentprob.eo.op.get("attackingEnemyBase"),(boolean) currentprob.eo.op.get("beingAttacked"),
                    (Race) currentprob.eo.op.get("playerRace"),(Race) currentprob.eo.op.get("enemyRace"),(GameStatus)currentprob.eo.op.get("gameStatus"),
                    (int[][])currentprob.eo.op.get("trainingCapacity"), (int) currentprob.eo.op.get("populationCapacity"), (int) currentprob.eo.op.get("populationUsed"), (HashMap<UnitType, Integer>) currentprob.eo.op.get("unitMemory"));

            newProbabilities.add(new TransitionProb(attackprob * currentprob.p, new EnvironmentOutcome(Currentstate, action,alternateState, rewardFunction.reward(Currentstate,action,alternateState),terminal(alternateState))));

            //attacked + loose base.
            alternateState = new PlanningState( (int) currentprob.eo.op.get("numWorkers"), (int) currentprob.eo.op.get("mineralProductionRate"),
                    (int) currentprob.eo.op.get("gasProductionRate"), ((int) currentprob.eo.op.get("numBases")) - 1, (int) currentprob.eo.op.get("timeSinceLastScout"),
                    (ArrayList<CombatUnitStatus>)currentprob.eo.op.get("combatUnitStatuses"), (int) currentprob.eo.op.get("numEnemyWorkers"),
                    (int)currentprob.eo.op.get("numEnemyBases"), (UnitType)currentprob.eo.op.get("mostCommonCombatUnit"),
                    !(boolean) currentprob.eo.op.get("attackingEnemyBase"),(boolean) currentprob.eo.op.get("beingAttacked"),
                    (Race) currentprob.eo.op.get("playerRace"),(Race) currentprob.eo.op.get("enemyRace"),(GameStatus)currentprob.eo.op.get("gameStatus"),
                    (int[][])currentprob.eo.op.get("trainingCapacity"), (int) currentprob.eo.op.get("populationCapacity"), (int) currentprob.eo.op.get("populationUsed"), (HashMap<UnitType, Integer>) currentprob.eo.op.get("unitMemory"));

            newProbabilities.add(new TransitionProb(attackAndLooseBaseprob * currentprob.p, new EnvironmentOutcome(Currentstate, action,alternateState, rewardFunction.reward(Currentstate,action,alternateState),terminal(alternateState))));
        }



        return newProbabilities;
    }


    //improvements: account for other regular spending, like combat units, upgrades ect
    //also making the probabilities non-uniform.
    /**
     * Helper function to provide possible states if the enemy built a new base.
     * @param Currentstate the current actual state
     * @param action the action to be taken
     * @param allProbabilities all possible resulting states and their probabilities so far.
     *                         WILL BE MODIFIED IN THE FUNCTION: inorder to adjust the probailities to
     *                         make sure they still add up to 1.
     * @return any new possibilities to add to the list of all probabilities outside the function.
     */
    private List<TransitionProb> newEnemyBaseTransitions(State Currentstate, Action action, List<TransitionProb> allProbabilities) {
        //consider giving the model accsess to the game for better time estimation
        //to ask what time it is? or something. this function want's to know
        //how long since it last checked the enemy base.
        List<TransitionProb> newProbabilities = new ArrayList<TransitionProb>();
        int timeSinceLastScout = (int) Currentstate.get("timeSinceLastScout");
        GeneralRaceProductionKnowledge enemyknowledge;
        switch ((Race) Currentstate.get("enemyRace")){
            case Protoss:
                enemyknowledge = new ProtossGeneralKnowledge();
                break;
            case Terran:
                enemyknowledge = new TerrenGeneralKnowledge();
                break;
            case Zerg:
                enemyknowledge = new ZergGeneralKnowledge();
                break;
            //case Unknown:
            //case Random:
            default:
                //TODO: MAKE ONE OF THESE FOR AVERAGES, or something.
                enemyknowledge = new ProtossGeneralKnowledge();
        }

        if (ActionParser.ActionEnum.SCOUT == ActionParser.GetActionType(action)
                || timeSinceLastScout < (30 /* fps */ * 30 /* seconds */) ){
            return newProbabilities;
        } else {
            TransitionProb currentprob;
            State alternateState;


            //figure out probability of a new base
            int numenemyworkers = (int) allProbabilities.get(0).eo.op.get("numEnemyWorkers");
            int numenemybases = (int) allProbabilities.get(0).eo.op.get("numEnemyBases");
            int workersperbase = (int) Math.floor(numenemyworkers / numenemybases);
            int[] argument = new int[numenemybases];
            for(int j = 0; j < numenemybases; j++){
                argument[j] = workersperbase;
            }
            int enemyMineralAccumulation = Math.round(enemyknowledge.AverageMineralProductionRate(argument));

            int canAffordNewBase = 0;
            //base cost
            int affordthreshhold = 400;
            //cost for making workers continuesly.
            affordthreshhold += 50 * (timeSinceLastScout/30*20);
            //pop increase
            affordthreshhold += (timeSinceLastScout/30*20)/10 * 100;
            //discount it by 40% because this probably isn't a realistic bounding
            affordthreshhold = (int) Math.round(affordthreshhold * 0.6);
            if(enemyMineralAccumulation > affordthreshhold){
                canAffordNewBase = 1;
            }


            double newBase = numenemybases/3 * canAffordNewBase;
            double noNewBase = 1 - newBase;


            if(newBase >= 0 ) {
                //enumerate possiblities
                for (int i = 0; i < allProbabilities.size(); i++) {
                    currentprob = allProbabilities.get(i);
                    currentprob.p = currentprob.p * noNewBase;

                    alternateState = new PlanningState((int) currentprob.eo.op.get("numWorkers"), (int) currentprob.eo.op.get("mineralProductionRate"),
                            (int) currentprob.eo.op.get("gasProductionRate"), (int) currentprob.eo.op.get("numBases"), (int) currentprob.eo.op.get("timeSinceLastScout"),
                            (ArrayList<CombatUnitStatus>) currentprob.eo.op.get("combatUnitStatuses"), (int) currentprob.eo.op.get("numEnemyWorkers"),
                            (int) currentprob.eo.op.get("numEnemyBases"), (UnitType) currentprob.eo.op.get("mostCommonCombatUnit"),
                            !(boolean) currentprob.eo.op.get("attackingEnemyBase"), (boolean) currentprob.eo.op.get("beingAttacked"),
                            (Race) currentprob.eo.op.get("playerRace"), (Race) currentprob.eo.op.get("enemyRace"), (GameStatus) currentprob.eo.op.get("gameStatus"),
                            (int[][]) currentprob.eo.op.get("trainingCapacity"), (int) currentprob.eo.op.get("populationCapacity"), (int) currentprob.eo.op.get("populationUsed"), (HashMap<UnitType, Integer>) currentprob.eo.op.get("unitMemory"));

                    newProbabilities.add(new TransitionProb(newBase * currentprob.p, new EnvironmentOutcome(Currentstate, action, alternateState, rewardFunction.reward(Currentstate, action, alternateState), false)));
                }
            }

            return newProbabilities;
        }
    }


    /**
     * A helper function to fill in the possible states that represent units finishing their training.
     *
     * The chance of  a unit finishing is treated like a uniform random variable.
     *
     * @param Currentstate the current actual state
     * @param action the action to be taken
     * @param allProbabilities all possible resulting states and their probabilities so far.
     *                         WILL BE MODIFIED IN THE FUNCTION: inorder to adjust the probailities to
     *                         make sure they still add up to 1.
     * @return any new possibilities to add to the list of all probabilities outside the function.
     */
    private  List<TransitionProb> trainingCompleteProbabilities(State Currentstate, Action action, List<TransitionProb> allProbabilities){
        List<TransitionProb> probabilities = new ArrayList<TransitionProb>();
        State alternateState;
        Race ourrace = (Race) Currentstate.get("playerRace");

        TransitionProb currentprob;
        List<CapacityProbabilityPair> capacities;


        CapacityProbabilityPair currentCapProbPair;

        double remainingprobability = 1;

        for(int i = 0; i < allProbabilities.size(); i++){
            currentprob = allProbabilities.get(i);
            List<CapacityProbabilityPair> currentPosibleCapacities = PosibleCapacities(ourrace, currentprob.eo.op);

            for( int j = 0; j < currentPosibleCapacities.size(); j++){
                currentCapProbPair = currentPosibleCapacities.get(j);
                remainingprobability -= currentCapProbPair.prob;

                alternateState = new PlanningState( (int) currentprob.eo.op.get("numWorkers"), (int) currentprob.eo.op.get("mineralProductionRate"),
                        (int) currentprob.eo.op.get("gasProductionRate"), (int) currentprob.eo.op.get("numBases"), (int) currentprob.eo.op.get("timeSinceLastScout"),
                        (ArrayList<CombatUnitStatus>)currentprob.eo.op.get("combatUnitStatuses"), (int) currentprob.eo.op.get("numEnemyWorkers"),
                        (int)currentprob.eo.op.get("numEnemyBases"), (UnitType)currentprob.eo.op.get("mostCommonCombatUnit"),
                        (boolean) currentprob.eo.op.get("attackingEnemyBase"), (boolean) currentprob.eo.op.get("beingAttacked"),
                        (Race) currentprob.eo.op.get("playerRace"),(Race) currentprob.eo.op.get("enemyRace"),(GameStatus)currentprob.eo.op.get("gameStatus"),
                        (int[][])currentprob.eo.op.get("trainingCapacity"), (int) currentprob.eo.op.get("populationCapacity"), (int) currentprob.eo.op.get("populationUsed"), (HashMap<UnitType, Integer>) currentprob.eo.op.get("unitMemory"));

                //TODO: FIX THIS
                //tomorrow me: I'm not sure what's wrong, it's probably a problem in the functions below
                //but I could be wrong. Either way, I'm leaving this as is for now to get it submitted.
                probabilities.add(new TransitionProb(currentCapProbPair.prob * currentprob.p, new EnvironmentOutcome(Currentstate, action,alternateState, rewardFunction.reward(Currentstate,action,alternateState),terminal(alternateState))));
            }

            //should be equal to p * (1 - AverageUnitTrainingTime/1800)
            //I think, accept that's probably wrong
            currentprob.p = currentprob.p * remainingprobability;
            if(remainingprobability < 0){
                System.err.println("ERROR: NEGATIVE PROBABILITY IS NOT REAL MATH.");
                System.err.println("Daniel messed up the probability distribution for the chance " +
                        "of units finishing training. someone should come into this code at" +
                        "the end of the traniningCompleteProbabilities function in StarcraftModel.java");
            }
        }



        return probabilities;
    }


    /**
     * A helper function for the trainingCompleteProbabilities helper function. Enumerates the possible
     * trainingCapacityies of the state given
     * @param ourrace the player's race
     * @param possibleState the current state to enumerate the possible capacities for.
     * @return
     */
    //TODO: this is missing more than a few possibilites, from different combinations of units finishing
    //training, and not finishing training.
    private List<CapacityProbabilityPair> PosibleCapacities(Race ourrace, State possibleState){
        //deviding by 1800 to make this the chance of the unit finishing
        //at a given time in a single minute.
        //TODO: someone who's smart with probability should adjust this chance here, and for protoss below
        double finishprob = AverageUnitTrainingTime/1800;
        double notfinishprob = 1 - finishprob;
        double currentSituationProbab;
        int[][] capacity = (int[][]) possibleState.get("trainingCapacity");

        List<CapacityProbabilityPair> possiblecapacities = new ArrayList<CapacityProbabilityPair>();


        if(ourrace==Race.Zerg){
            //if there's something training, flip a coin to see if it completes
            if(capacity[0][0] > 0){
                for(int unit = 0; unit<capacity[0][0]; unit++){
                    capacity[0][0] -= 1;
                    capacity[0][1] += 1;
                    capacity[1][0] -= 1;
                    capacity[1][1] += 1;
                    capacity[2][0] -= 1;
                    capacity[2][1] += 1;
                    capacity[3][0] -= 1;
                    capacity[3][1] += 1;

                    currentSituationProbab = Math.pow(finishprob, unit+1);
                    //TODO: figure out how to make the probability correct
                    //this adds the probability that all the units finished training so far.
                    possiblecapacities.add( new CapacityProbabilityPair(capacity, currentSituationProbab) );
                }
            }

        } else {
            int power = 1;
            //TODO: THIS!
            //why this isn't done: there's a lot of possible combinations
            //of one like zelots finishing, but workers not ect, and I don't
            //know how to enumerate all of those, and give their relative probabilities.
            for(int catagory = 0; catagory<capacity.length; catagory++){
                if(capacity[catagory][0] > 0){
                    for(int tainingcapacityslot = 0; tainingcapacityslot< capacity[catagory][0];tainingcapacityslot++){

                        capacity[catagory][0] -= 1;
                        capacity[catagory][1] += 1;

                        power++;
                        currentSituationProbab = Math.pow(finishprob, power);
                        possiblecapacities.add( new CapacityProbabilityPair(capacity, currentSituationProbab) );
                    }
                }
            }
        }

        return possiblecapacities;
    }


    @Override
    public EnvironmentOutcome sample(State state, Action action) {
        List<TransitionProb> trans = this.transitions(state,action);

        double chance = rng.nextDouble();

        int index = 0;
        while (chance > 0){
            chance -= trans.get(index).p;
        }

        return trans.get(index).eo;
    }

    @Override
    public boolean terminal(State state) {
        return  (int) state.get("numEnemyBases") == 0 || (int) state.get("numBases") == 0;
    }


    /**
     * A class used to store a possible training capacity, and the probability of it occurring.
     *
     * Why this exists: we don't know how much time would pass between bot actions, so we
     * have to guess, and we can't ask the game.
     */
    private static class CapacityProbabilityPair {
        int[][] cap;
        double prob;

        CapacityProbabilityPair(int[][] capacity, double probability){
            cap=capacity;
            prob=probability;
        }
    }
}



