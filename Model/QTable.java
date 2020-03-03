import Actions.Action;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QTable extends HashMap<State, Map<Action, Double>> {
    /**
     * This constructor takes a Collection of States and a Collection of Actions and adds them to the State -> (Action, Double) HashMap.
     * @param states All possible states that will be included in the HashMap.
     * @param actions All possible actions that can occur.
     */
    public QTable(Collection<State> states, Collection<Action> actions) {
        super(states.size());

        for(State state : states) {
            Map<Action, Double> actionDoubleMap = new HashMap<Action, Double>();

            for(Action action : actions) {
                actionDoubleMap.put(action, 0.0);
            }

            this.put(state, actionDoubleMap);
        }
    }

    /**
     * This function takes a state and returns the action associated with the max value in the HashMap.
     * @param state The state used to search for the best (Action, Value) combination.
     * @return Returns the action relating to the max value in the HashMap.
     */
    public Action getMaxAction(State state) {
        Map<Action, Double> actionDoubleMap = this.get(state);
        Action action = null;
        double max = Double.NEGATIVE_INFINITY;

        for(Action act : actionDoubleMap.keySet()) {
            if(actionDoubleMap.get(act) > max) {
                max = actionDoubleMap.get(act);
                action = act;
            }
        }

        return action;
    }

    /**
     * This function takes a state and returns the max value in the HashMap.
     * @param state The state used to search for the best (Action, Value) combination.
     * @return The max value in the (Action, Value) pair in the HashMap
     */
    public double getMaxValue(State state) {
        Map<Action, Double> actionDoubleMap = this.get(state);
        double max = Double.NEGATIVE_INFINITY;

        for(Action act : actionDoubleMap.keySet()) {
            if(actionDoubleMap.get(act) > max) {
                max = actionDoubleMap.get(act);
            }
        }

        return max;
    }

    /**
     * This function returns a (State, Action) policy where each action nets the max value in each state.
     * @return The (State, Action) policy.
     */
    public Policy getPolicy() {
        Policy policy = new Policy();

        for(State state : this.keySet()) {
            Action bestAction = this.getMaxAction(state);
            StateAction stateAction = new StateAction(state, bestAction);
            policy.add(stateAction);
        }

        return policy;
    }
}
