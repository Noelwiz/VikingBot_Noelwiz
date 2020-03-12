package Actions;

import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;
import model.Units;

import java.util.ArrayList;

public class MoveRight extends Action {
    private ActionType type = ActionType.MOVERIGHT;

    public ActionType getType() {
        return this.type;
    }

    /**
     * This function orders units to move straight right
     *
     * @param game  The game initialized at the start of the program
     * @param unit  The unit that needs a command
     */
    public void doAction(Game game, Unit unit) {

        Position movePos;
        movePos = new Position(unit.getX() + 8, unit.getY());
        if (unit.hasPath(movePos)) { // check if it can move there
            unit.move(movePos);
        }
    }
}