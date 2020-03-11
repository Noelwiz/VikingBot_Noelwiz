package src.main.java.ML.States;

import src.main.java.ML.Range.*;
import bwapi.Unit;
import src.main.java.ML.model.TerrainSector;

import java.io.Serializable;

import static src.main.java.ML.Range.UnitsRange.*;

public class State implements Serializable {
    private static final long serialVersionUID = 7588180712283449263L;
    private boolean onCoolDown;
    private Distance closestEnemy;
    private Units numberOfEnemies;
    private Unit Unit;
    private Hp enemyHp;
    private Hp friendlyHp;
    private TerrainSector unitDirections;

    public State(boolean onCoolDown, Distance closestEnemy, Units numberOfEnemies, Hp enemyHp, Hp friendlyHp) {
        this.onCoolDown = onCoolDown;
        this.closestEnemy = closestEnemy;
        this.numberOfEnemies = numberOfEnemies;
        this.Unit = null;
        this.enemyHp = enemyHp;
        this.friendlyHp = friendlyHp;
        this.unitDirections = null;
    }

    public void setUnit(Unit unit) {
        this.Unit = unit;
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

    public Unit getUnit(){return Unit; }

    public Hp getEnemyHp() {
        return enemyHp;
    }

    public Hp getFriendlyHp() {
        return friendlyHp;
    }

    public TerrainSector getUnitDirections() {
        return unitDirections;
    }

    public int getKeyVal(State state) {
        int keyVal = 0;

        if(state.isOnCoolDown()) {
            keyVal+=1;
        } else {
            keyVal+=2;
        }

        if(state.getNumberOfEnemies().getRange() == SMALL) {
            keyVal+=3;
        } else if(state.getNumberOfEnemies().getRange() == MEDIUM) {
            keyVal+=4;
        } else if(state.getNumberOfEnemies().getRange() == LARGE) {
            keyVal+=5;
        }

        if(state.getFriendlyHp().getRange() == HpRange.LOW) {
            keyVal+=6;
        } else if(state.getFriendlyHp().getRange() == HpRange.MEDIUM_LOW) {
            keyVal+=7;
        } else if(state.getFriendlyHp().getRange() == HpRange.MEDIUM_HIGH) {
            keyVal+=8;
        } else {
            keyVal+=9;
        }

        if(state.getEnemyHp().getRange() == HpRange.LOW) {
            keyVal+=10;
        } else if(state.getEnemyHp().getRange() == HpRange.MEDIUM_LOW) {
            keyVal+=11;
        } else if(state.getEnemyHp().getRange() == HpRange.MEDIUM_HIGH) {
            keyVal+=12;
        } else {
            keyVal+=13;
        }

        if(state.getClosestEnemy().getRange() == DistanceRange.CLOSE) {
            keyVal+=14;
        } else if(state.getClosestEnemy().getRange() == DistanceRange.MEDIUM_CLOSE) {
            keyVal+=15;
        } else if(state.getClosestEnemy().getRange() == DistanceRange.MEDIUM_FAR) {
            keyVal+=16;
        } else {
            keyVal+=17;
        }
        return keyVal;
    }
}