package ML.Actions;

import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;

public class MoveUpRight extends Action implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private ActionType type = ActionType.MOVEUPRIGHT;

    public ActionType getType() {
        return this.type;
    }

    /**
     * This function orders units to move diagonally up right
     * @param game  The game initialized at the start of the program
     * @param unit The unit that needs a command
     */
    public void doAction(Game game, Unit unit) {

        Position movePos;
        movePos = new Position(unit.getX() + 8, unit.getY() - 8);
        if (unit.hasPath(movePos)) { // check if it can move there
            unit.move(movePos);
        }
    }
}