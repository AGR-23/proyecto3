import kareltherobot.*;
import java.awt.Color;

public class Train extends Robot implements Runnable, Directions 
{
    int column;
    int row;
    String route;
    Order orderManager;

    public Train(int street, int avenue, Direction direction, int beeps, Color color, String route, Order orderManager) 
    {
        super(street, avenue, direction, beeps, color);
        this.column = avenue;
        this.row = street;
        this.route = route;
        this.orderManager = orderManager;

        World.setupThread(this);
    }

    @Override
    public void run() 
    {
        exitDepot();
    }

    // turn right by doing three left turns
    public void turnRight() {
        turnLeft();
        turnLeft();
        turnLeft();
    }

    public void goToInitialPosition() 
    {
        if (this.route.equals("routeAN")) 
        {
            System.out.println("1");
            goToAN();
        }
        if (this.route.equals("routeAE")) 
        {
            System.out.println("2");
            goToAE();
        }
        if (this.route.equals("routeSJ")) 
        {
            System.out.println("3");
            goToSJ();
        }
    }

    public void exitDepot() 
    {
        while (column != 16 || row != 32) 
        {
            if (column == 15 && row == 35 && !facingEast())   turnLeft();
            if (column == 1  && row == 35 && !facingSouth())  turnLeft();
            if (column == 1  && row == 34 && !facingWest())   turnLeft();
            if (column == 14 && row == 34 && !facingSouth()) turnRight();
            if (column == 14 && row == 32 && !facingEast())  turnLeft();

            moveAndUpdateCoordinates();
        }
        System.out.println("I'M HERE");
        goToInitialPosition();
    }

    public void moveAndUpdateCoordinates() 
    {
        int prevRow    = row;
        int prevColumn = column;

        if      (facingNorth()) row++;
        else if (facingSouth()) row--;
        else if (facingEast())  column++;
        else if (facingWest())  column--;

        if (orderManager.map[row][column] == 1) 
        {
            row    = prevRow;
            column = prevColumn;
            System.out.println("collision!");
        } 
        else 
        {
            orderManager.map[prevRow][prevColumn] = 0;
            orderManager.map[row][column]         = 1;
            move();
        }
    }

    public void goToAN() 
    {
        while (column != 19 || row != 35) 
        {
            if (column == 17 && row == 32) turnLeft();
            if (column == 17 && row == 34) turnRight();
            if (column == 20 && row == 34) turnLeft();
            if (column == 20 && row == 35) turnLeft();
            moveAndUpdateCoordinates();
        }
    }

    public void goToAE() 
    {
        while (column != 11 || row != 1) 
        {
            if (column == 16 && row == 32) turnRight();
            if (column == 16 && row == 29) turnRight();
            if (column == 15 && row == 29) turnLeft();
            if (column == 15 && row == 26) turnRight();
            if (column == 13 && row == 26) turnLeft();
            if (column == 13 && row == 23) turnRight();
            if (column == 11 && row == 23) turnLeft();
            if (column == 11 && row == 18) turnLeft();
            if (column == 16 && row == 18) turnRight();
            if (column == 16 && row == 11) turnRight();
            if (column == 13 && row == 11) turnLeft();
            if (column == 13 && row == 5)  turnRight();
            if (column == 12 && row == 5)  turnLeft();
            if (column == 12 && row == 2)  turnRight();
            if (column == 10 && row == 2)  turnLeft();
            if (column == 10 && row == 1)  turnLeft();
            moveAndUpdateCoordinates();
        }
    }

    public void goToSJ() 
    {
        while (column != 1 || row != 16) 
        {
            if (column == 16 && row == 32) turnRight();
            if (column == 16 && row == 29) turnRight();
            if (column == 15 && row == 29) turnLeft();
            if (column == 15 && row == 26) turnRight();
            if (column == 13 && row == 26) turnLeft();
            if (column == 13 && row == 23) turnRight();
            if (column == 11 && row == 23) turnLeft();
            if (column == 11 && row == 14) turnRight();
            if (column == 7  && row == 14) turnRight();
            if (column == 7  && row == 15) turnLeft();
            if (column == 2  && row == 15) turnRight();
            if (column == 2  && row == 17) turnLeft();
            if (column == 1  && row == 17) turnLeft();
            moveAndUpdateCoordinates();
        }
    }

    public void goToSA() 
    {
        while (column != 15 || row != 14) 
        {
            if (column == 1  && row == 14) turnLeft();
            if (column == 6  && row == 14) turnRight();
            if (column == 6  && row == 13) turnLeft();
            if (column == 14 && row == 13) turnLeft();
            if (column == 14 && row == 14) turnRight();
            moveAndUpdateCoordinates();
        }
        turnLeft();
        turnLeft();
    }
}