package ML.Learning;

import ML.Actions.Action;
import ML.Data.DataManager;
import ML.QTable;
import ML.States.State;
import ML.States.StateSpaceManager;
import ML.RewardFunction;
import bwapi.UnitType;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.Map;
import java.io.Serializable;

public class SARSA implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final double LEARNING_RATE = .1;
    private static final double DISCOUNT_FACTOR = .6;

    private DataManager dataManager;
    private final UnitType type;
    private QTable qTable;
    private final StateSpaceManager spaceManager;

    /**
     * Initializes the SARSA learning algorithm, creates/loads the DataManager, sets the StateSpaceManager, and
     * creates a new QTable from the set of possible States and Actions.
     * @param type the type of unit the learning algorithm is working around.
     * @param spaceManager the StateSpaceManager.
     */
    public SARSA(UnitType type, StateSpaceManager spaceManager) {
        this.type = type;
        dataManager = new DataManager(type);
        this.spaceManager = spaceManager;
        this.qTable = new QTable(this.spaceManager.getStateSet(), this.spaceManager.getActionList());
    }

    /**
     * Computes the Q-Value for SARSA which requires the current State, Action executed, next State, and reward.
     * This uses the following update function:
     *      Q(s_(t), a_(t)) <-- Q(s_(t), a_(t)) + a * [r_(t) + g * Q(s_(t+1), a_(t+1)) - Q(s_(t), a_(t)]
     *
     * Where the terms are defined as the following:
     *      Q(s, a): the Q-Value function that returns a Q-Value for a (State, Action) pair.
     *      s_(t): the State at time step t.
     *      a_(t): the Action at time step t.
     *      a: the learning rate (alpha).
     *      r_(t): the reward received at time step t.
     *      g: the discount factor (gamma).
     * @param current the current State.
     * @param action the Action executed in the current State.
     * @param next the resulting next State from the (current, action) pair.
     * @param reward the reward produced from executing the current Action in the current State.
     * @return returns a double which is the resulting Q-Value produced by the defined update function.
     */
    public double computerQValue(State current, Action action, State next, double reward) {
        // SARSA => q-value = q-value + LEARNING_FACTOR * (reward + (DISCOUNT_FACTOR * next_q-value) - q-value)
        double qvalue = qTable.get(current).get(action);
        double errorvalue = LEARNING_RATE * (reward + (DISCOUNT_FACTOR * qTable.getMaxValue(next)) - qvalue);

        return qvalue + errorvalue;
    }

    /**
     * Updates the QTable with the resulting Q-Value produced from the (current, action, next) tuple. This method
     * calculates the produced reward and Q-Value then puts the Q-Value into the (State, (Action, double)) map.
     * @param current the current State.
     * @param action the Action executed in the current State.
     * @param next the resulting next State.
     */
    public void updateQTable(State current, Action action, State next) {
        // Get the reward and calculate the qvalue given the reward
        double reward = RewardFunction.getRewardValue(current, action, next);
        double qvalue = computerQValue(current, action, next, reward);

        dataManager.addDataPoint(current, action, next, reward, qvalue);
        dataManager.addState(current);

        // Get the current qvalue and update it with the new qvalue
        Map<Action, Double> actionDoubleMap = qTable.get(current);
        actionDoubleMap.put(action, qvalue);
        qTable.put(current, actionDoubleMap);
    }

    /**
     * Attempts to load the QTable from disk.
     */
    public void loadQTable() {
        FileInputStream fis = null;
        try {
            File f = new File("TrainingFiles/Tables/" + type.toString() + "Table.ser");
            fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            qTable = (QTable) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Attempts to store the QTable to disk.
     */
    public void storeQTable() {
        FileOutputStream fos = null;

        try {
            File f = new File("TrainingFiles/Tables/" + type.toString() + "Table.ser");
            f.getParentFile().mkdirs();
            fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(qTable);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null) {
                    fos.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Attempts to load the DataManager from disk.
     */
    public void loadDataManager() {
        FileInputStream fis = null;

        try {
            File f = new File("TrainingFiles/Tables/" + type.toString() + "Data.ser");
            fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            dataManager = (DataManager) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Attempts to store the DataManager to disk.
     */
    public void storeDataManager() {
        FileOutputStream fos = null;

        try {
            File f = new File("TrainingFiles/Tables/" + type.toString() + "Data.ser");
            f.getParentFile().mkdirs();
            fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dataManager);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null) {
                    fos.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Gets the type of unit the SARSA learning algorithm is working with.
     * @return returns the respective type of unit.
     */
    public UnitType getType() {
        return type;
    }

    /**
     * Gets the QTable being used by the SARSA learning algorithm.
     * @return returns the QTable.
     */
    public QTable getQTable() {
        return qTable;
    }

    /**
     * Gets the StateSpaceManager being used by the SARSA learning algorithm.
     * @return returns the StateSpaceManager.
     */
    public StateSpaceManager getSpaceManager() {
        return spaceManager;
    }

    /**
     * Gets the DataManager being used by the SARSA learning algorithm.
     * @return returns the DataManager.
     */
    public DataManager getDataManager() {
        return dataManager;
    }
}
