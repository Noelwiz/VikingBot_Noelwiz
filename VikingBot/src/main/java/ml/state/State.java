package ml.state;

import ml.model.UnitClassification;
import ml.range.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a state with all relevant features relating to micromanaging combat.
 */
public class State implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean onCoolDown;
    private final Distance closestEnemy;
    private final Units numberOfEnemies;
    private final Units numberOfFriendlies;
    private final Hp enemyHp;
    private final Hp friendlyHp;
    private final boolean skirmish;
    private final UnitClassification unitClass;

    /**
     * Initializes the state given the current information around a specific unit.
     * @param onCoolDown is the unit's weapon on cool down?
     * @param closestEnemy the distance to the closest enemy.
     * @param numberOfEnemies the total number of enemy units nearby.
     * @param numberOfFriendlies the total number friendly units nearby.
     * @param enemyHp the total HP of nearby enemies.
     * @param friendlyHp the total HP of nearby allies.
     * @param skirmish whether units should stop fighting or not
     * @param unitClass the unitClassification for the given unit
     */
    public State(boolean onCoolDown, Distance closestEnemy, Units numberOfEnemies, Units numberOfFriendlies, Hp enemyHp, Hp friendlyHp, boolean skirmish, UnitClassification unitClass) {
        this.onCoolDown = onCoolDown;
        this.closestEnemy = closestEnemy;
        this.numberOfEnemies = numberOfEnemies;
        this.numberOfFriendlies = numberOfFriendlies;
        this.enemyHp = enemyHp;
        this.friendlyHp = friendlyHp;
        this.skirmish = skirmish;
        this.unitClass = unitClass;
    }

    /**
     *
     * @return If the unit's weapon is on cool down.
     */
    public boolean isOnCoolDown() {
        return onCoolDown;
    }

    /**
     *
     * @return The distance to the closest enemy.
     */
    public Distance getClosestEnemy() {
        return closestEnemy;
    }

    /**
     *
     * @return The number of enemy units nearby.
     */
    public Units getNumberOfEnemies() {
        return numberOfEnemies;
    }

    /**
     *
     * @return The number of friendly units nearby.
     */
    public Units getNumberOfFriendlies() {
        return numberOfFriendlies;
    }

    /**
     *
     * @return The total HP of nearby enemy units.
     */
    public Hp getEnemyHp() {
        return enemyHp;
    }

    /**
     *
     * @return The total HP of nearby friendly units.
     */
    public Hp getFriendlyHp() {
        return friendlyHp;
    }

    /**
     *
     * @return The value of the goHome command
     */
    public boolean getSkirmish() { return skirmish; }

    /**
     *
     * @return The classification of the unit
     */
    public UnitClassification getUnitClass() { return unitClass; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return onCoolDown == state.onCoolDown &&
                Objects.equals(closestEnemy, state.closestEnemy) &&
                Objects.equals(numberOfEnemies, state.numberOfEnemies) &&
                Objects.equals(numberOfFriendlies, state.numberOfFriendlies) &&
                Objects.equals(enemyHp, state.enemyHp) &&
                Objects.equals(friendlyHp, state.friendlyHp);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((closestEnemy == null) ? 0 : closestEnemy.hashCode());
        result = prime * result + ((friendlyHp == null) ? 0 : friendlyHp.hashCode());
        result = prime * result + ((enemyHp == null) ? 0 : enemyHp.hashCode());
        result = prime * result + ((numberOfFriendlies == null) ? 0 : numberOfFriendlies.hashCode());
        result = prime * result + ((numberOfEnemies == null) ? 0 : numberOfEnemies.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
                "onCoolDown=" + onCoolDown +
                ", closestEnemy=" + closestEnemy +
                ", numberOfEnemies=" + numberOfEnemies +
                ", numberOfFriendlies=" + numberOfFriendlies +
                ", enemyHp=" + enemyHp +
                ", friendlyHp=" + friendlyHp +
                '}';
    }

    /**
     * Creates a condensed version of the toString() method.
     * @return A condensed version of the toString() method.
     */
    public String condensedString() {
        return "State{" +
                "Enemies (" + numberOfEnemies.getValue() + " units" +
                ", " + enemyHp.getValue() + " hp)" +
                ", Friendlies (" + numberOfFriendlies.getValue() + " units" +
                ", " + friendlyHp.getValue() + " hp)" +
                '}';
    }
}