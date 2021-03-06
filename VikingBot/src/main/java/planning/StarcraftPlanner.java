package planning;

import agents.IntelligenceAgent;
import agents.StrategyAgent;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.ReflectiveHashableStateFactory;
import planning.actions.*;

public class StarcraftPlanner {
    private SparseSampling sparsePlanner;
    private StarcraftEnvironment game;
    private Policy sparcePolicy;
    private IntelligenceAgent intelligenceAgent;
    private SharedPriorityQueue Actions;

    private StrategyAgent strat;

    public StarcraftPlanner(IntelligenceAgent intelligenceAgent, StrategyAgent strategy) {
        this.intelligenceAgent = intelligenceAgent;
        strat = strategy;
    }

    /**
     * intialize everything to use the ai planning.
     * @param queue the queue to be shared between everything for sending
     *              actions from the planner to the bot.
     */
    public void Initalize(SharedPriorityQueue queue){
        this.Actions = queue;

        SADomain domain = new SADomain();
        //add actions to the domain
        domain.addActionType(new AttackActionType());
        domain.addActionType(new BuildActionType());
        domain.addActionType(new ExpandActionType());
        domain.addActionType(new ScoutActionType());
        domain.addActionType(new TrainActionType());
        //domain.addActionType(new UpgradeActionType());
        domain.addActionTypes(new GatherActionType());

        RewardFunction initalreward = new PlanningRewardFunction(GameStatus.EARLY);

        StarcraftModel model = new StarcraftModel(initalreward);
        domain.setModel(model);

        HashableStateFactory factory = new ReflectiveHashableStateFactory();

        //Tmake sure the enviorment is initalized with everything it needs or something
        game = new StarcraftEnvironment(initalreward, intelligenceAgent, model, strat);

        //NOTE TO FUTURE SELVES: consider adjusting the discount factor.
        float DiscountFactor = 0.5f;
        sparsePlanner = new SparseSampling(domain, DiscountFactor, factory, 3, 1);

        //get initial policy for planning
        sparcePolicy = new GreedyQPolicy(sparsePlanner);

        //NOTE TO FUTURE SELVES: consider adjusting this.
        sparsePlanner.setForgetPreviousPlanResults(false);
        //sparsePlanner.setComputeExactValueFunction(true);

        //enqueue 3 actions
        SparsePlanStep();
        SparsePlanStep();
        SparsePlanStep();
    }

    /**
     * Exicutes an action in the enviorment with the spare planner.
     */
    public void SparsePlanStep(){
        if (roomInQueue()) {
            Action todo = sparcePolicy.action(game.currentObservation());
            Actions.EnQueue(todo);
        }
    }

    /**
     *
     * @param rf A reward funtion for evaluating the reward gained from reaching
     *           a node. More reward => bot prioratizing reaching that node => setting
     *           the bot's goals.
     */
    public void setGoal(RewardFunction rf){
        game.UpdateRewardFunction(rf);
    }


    public void ExecuteAction(){
        game.executeAction(Actions.DeQueue());
    }

    public Boolean roomInQueue() {
        return Actions.size() <= 10;
    }
}
