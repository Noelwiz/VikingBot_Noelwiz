package States;

import Range.Distance;
import Range.Hp;
import Range.Units;
import model.TerrainSector;

public class State {
    private boolean onCoolDown;
    private Distance closestEnemy;
    private Units numberOfEnemies;
    private Hp enemyHp;
    private Hp friendlyHp;
    private TerrainSector unitDirections;

    public State(boolean onCoolDown, Distance closestEnemy, Units numberOfEnemies, Hp enemyHp, Hp friendlyHp, TerrainSector unitDirections) {
        this.onCoolDown = onCoolDown;
        this.closestEnemy = closestEnemy;
        this.numberOfEnemies = numberOfEnemies;
        this.enemyHp = enemyHp;
        this.friendlyHp = friendlyHp;
        this.unitDirections = unitDirections;
    }

    public boolean isOnCoolDown() {
        return onCoolDown;
    }

    public Distance getClosestEnemy() {
        return closestEnemy;
    }

    public Units getNumberOfEnemies() {
        return numberOfEnemies;
    }

    public Hp getEnemyHp() {
        return enemyHp;
    }

    public Hp getFriendlyHp() {
        return friendlyHp;
    }

    public TerrainSector getUnitDirections() {
        return unitDirections;
    }
}