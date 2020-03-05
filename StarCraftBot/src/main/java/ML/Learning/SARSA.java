package ML.Learning;

import ML.Actions.Action;
import ML.QTable;
import ML.States.State;
import ML.States.StateSpaceManager;
import ML.RewardFunction;
import bwapi.UnitType;

import java.util.Map;

public class SARSA {

    private static final double LEARNING_FACTOR = .5;
    private static final double DISCOUNT_FACTOR = .6;
    private static final double INITIAL_QVAL = Double.NEGATIVE_INFINITY;

    private UnitType type;
    private QTable qTable;
    private StateSpaceManager spaceManager;

    public SARSA(UnitType type, StateSpaceManager spaceManager) {
        this.type = type;
        this.spaceManager = spaceManager;
        this.qTable = new QTable(this.spaceManager.getStateSet(), this.spaceManager.getActionList());
    }

    public double computerQValue(State current, Action action, State next, double reward) {
        // SARSA => q-value = q-value + LEARNING_FACTOR * (reward + (DISCOUNT_FACTOR * next_q-value) - q-value)
        double qvalue = qTable.get(current.getKeyVal(current)).get(action);
        double errorvalue = LEARNING_FACTOR * (reward + (DISCOUNT_FACTOR * qTable.getMaxValue(next)) - qvalue);

        return qvalue + errorvalue;
    }

    public void updateQTable(State current, Action action, State next) {
        // Get the reward and calculate the qvalue given the reward
        double reward = RewardFunction.getRewardValue(current, action, next);
        double qvalue = computerQValue(current, action, next, reward);

        // Get the current qvalue and update it with the new qvalue
        Map<Action, Double> actionDoubleMap = qTable.get(current.getKeyVal(current));
        actionDoubleMap.put(action, qvalue);
        qTable.put(current.getKeyVal(current), actionDoubleMap);
    }

    public void loadQTable() {

    }

    public void storeQTable() {

    }

    public UnitType getType() {
        return type;
    }

    public QTable getQTable() {
        return qTable;
    }

    public StateSpaceManager getSpaceManager() {
        return spaceManager;
    }
}
