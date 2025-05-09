import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class Train extends Robot implements Runnable, Directions {
    public static final CyclicBarrier startBarrier = new CyclicBarrier(3); // Synchronization barrier for starting the
                                                                           // trains
    int column;
    int row;
    String route;
    Order orderManager;
    boolean isInNiquia = false; // Flag to check if the train is in Niquia
    private boolean isInitialTrain; // Indica si es uno de los 3 trenes iniciales

    public Train(int street, int avenue, Direction direction, int beeps, Color color, String route,
            Order orderManager) {
        super(street, avenue, direction, beeps, color);
        this.column = avenue;
        this.row = street;
        this.route = route;
        this.orderManager = orderManager;

        World.setupThread(this);
    }

    @Override
    public void run() {
        exitDepot();

        // Posicionamiento inicial según ruta
        switch (route) {
            case "routeAN":
                goToAN(); // Posicionarse en Niquía
                break;
            case "routeAE":
                goToAE(); // Posicionarse en La Estrella
                break;
            case "routeSJ":
                goToSJ(); // Posicionarse en San Javier
                break;
            case "routeSA":
                goToSJ(); // Posicionarse en San Antonio
                break;
        }

        // Esperar a que todos estén posicionados
        System.out.println("Train at " + route + " is positioned. Waiting for others...");
        try {
            Train.startBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        boolean cierre = false; // Variable para controlar el cierre del tren
        while (cierre != true) {
            // Todos empiezan al mismo tiempo su recorrido real
            System.out.println("Train at " + route + " starting route!");

            // Aquí empieza su recorrido según su lógica normal
            if (route.equals("routeAN")) {
                niquiaToLaEstrella();
                route = "routeAE"; // Cambia la ruta a La Estrella
            } else if (route.equals("routeAE")) {
                laEstrellaToNiquia();
                route = "routeAN"; // Cambia la ruta a Niquía
            } else if (route.equals("routeSJ")) {
                SanjavierToSanAntonio();
                route = "routeSA"; // Cambia la ruta a San Antonio
            } else if (route.equals("routeSA")) {
                SanAntonioToSanjavier();
                route = "routeSJ"; // Cambia la ruta a San Javier
            }
        }
    }

    // turn right by doing three left turns
    public void turnRight() {
        turnLeft();
        turnLeft();
        turnLeft();
    }

    public void goToInitialPosition() {
        if (this.route.equals("routeAN")) {
            System.out.println("1");
            goToAN();
        }
        if (this.route.equals("routeAE")) {
            System.out.println("2");
            goToAE();
        }
        if (this.route.equals("routeSJ")) {
            System.out.println("3");
            goToSJ();
        }
    }

    public void exitDepot()
    // Move the train from the depot to the initial position
    {
        while (column != 16 || row != 32) {
            if (column == 15 && row == 35 && !facingEast())
                turnLeft();
            if (column == 1 && row == 35 && !facingSouth())
                turnLeft();
            if (column == 1 && row == 34 && !facingWest())
                turnLeft();
            if (column == 14 && row == 34 && !facingSouth())
                turnRight();
            if (column == 14 && row == 32 && !facingEast())
                turnLeft();

            moveAndUpdateCoordinates();
        }
        System.out.println("I'M HERE");
    }

    public void moveAndUpdateCoordinates() {
        // Saving the current coordinates before moving, hello teacher
        int prevRow = row;
        int prevColumn = column;

        // Calculate the new coordinates based on the current direction
        if (facingNorth())
            row++;
        else if (facingSouth())
            row--;
        else if (facingEast())
            column++;
        else if (facingWest())
            column--;

        // Check if the new coordinates are within bounds
        if (row < 0 || row >= orderManager.map.length || column < 0 || column >= orderManager.map[0].length) {
            System.out.println("Invalid move: (" + row + ", " + column + ")");
            row = prevRow;
            column = prevColumn;
            return;
        }
        if (orderManager.map[row][column] == 1) {
            row = prevRow; // revert to previous row
            column = prevColumn; // revert to previous column
            System.out.println("collision!");
        } else {
            orderManager.map[prevRow][prevColumn] = 0; // mark the previous cell as unoccupied
            orderManager.map[row][column] = 1; // mark the new cell as occupied
            move(); // move the robot to the new coordinates
            System.out.println("Moving to: " + row + ", " + column);
        }
    }

    public void waitStation() {
        if (nextToABeeper()) {
            System.out.println("Waiting at station: " + row + ", " + column);
            try {
                Thread.sleep(3000); // wait for 3 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void goToAN() {
        while (column != 19 || row != 35) // while the train is not in Niquia
        {
            if (column == 17 && row == 32)
                turnLeft();
            if (column == 17 && row == 34)
                turnRight();
            if (column == 20 && row == 34)
                turnLeft();
            if (column == 20 && row == 35)
                turnLeft();
            moveAndUpdateCoordinates();
        }
        isInNiquia = true; // Set the flag to true when the train reaches Niquia
        System.out.println("Train ready at " + route + ". Waiting for others...");

        try {
            Train.startBarrier.await(); // Espera a los otros dos trenes
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        System.out.println("All trains ready! Starting route...");
    }

    public void niquiaToLaEstrella() {
        while (column != 11 || row != 1) { // Mientras no esté en La Estrella
            if (column == 16 && row == 35)
                turnLeft();
            if (column == 16 && row == 29)
                turnRight();
            if (column == 15 && row == 29)
                turnLeft();
            if (column == 15 && row == 26)
                turnRight();
            if (column == 13 && row == 26)
                turnLeft();
            if (column == 13 && row == 23)
                turnRight();
            if (column == 11 && row == 23)
                turnLeft();
            if (column == 11 && row == 18)
                turnLeft();
            if (column == 16 && row == 18)
                turnRight();
            if (column == 16 && row == 11)
                turnRight();
            if (column == 13 && row == 11)
                turnLeft();
            if (column == 13 && row == 5)
                turnRight();
            if (column == 12 && row == 5)
                turnLeft();
            if (column == 12 && row == 2)
                turnRight();
            if (column == 10 && row == 2)
                turnLeft();
            if (column == 10 && row == 1)
                turnLeft();

            moveAndUpdateCoordinates();
            waitStation();
        }

    }

    public void goToAE() { // While the train is not in La estrella
        while (column != 11 || row != 1) {

            if (column == 16 && row == 32)
                turnRight();
            if (column == 16 && row == 29)
                turnRight();
            if (column == 15 && row == 29)
                turnLeft();
            if (column == 15 && row == 26)
                turnRight();
            if (column == 13 && row == 26)
                turnLeft();
            if (column == 13 && row == 23)
                turnRight();
            if (column == 11 && row == 23)
                turnLeft();
            if (column == 11 && row == 18)
                turnLeft();
            if (column == 16 && row == 18)
                turnRight();
            if (column == 16 && row == 11)
                turnRight();
            if (column == 13 && row == 11)
                turnLeft();
            if (column == 13 && row == 5)
                turnRight();
            if (column == 12 && row == 5)
                turnLeft();
            if (column == 12 && row == 2)
                turnRight();
            if (column == 10 && row == 2)
                turnLeft();
            if (column == 10 && row == 1)
                turnLeft();

            moveAndUpdateCoordinates();
        }
        System.out.println("Train ready at " + route + ". Waiting for others...");

        try {
            Train.startBarrier.await(); // Espera a los otros dos trenes
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        System.out.println("All trains ready! Starting route...");
    }

    public void laEstrellaToNiquia() {
        while (column != 19 || row != 35) // while the train is not in Niquia
        {
            if (column == 13 && row == 1)
                turnLeft();
            if (column == 13 && row == 4)
                turnRight();
            if (column == 14 && row == 4)
                turnLeft();
            if (column == 14 && row == 10)
                turnRight();
            if (column == 17 && row == 10)
                turnLeft();
            if (column == 17 && row == 19)
                turnLeft();
            if (column == 12 && row == 19)
                turnRight();
            if (column == 12 && row == 22)
                turnRight();
            if (column == 14 && row == 22)
                turnLeft();
            if (column == 14 && row == 25)
                turnRight();
            if (column == 16 && row == 25)
                turnLeft();
            if (column == 16 && row == 28)
                turnRight();
            if (column == 17 && row == 28)
                turnLeft();
            if (column == 17 && row == 34)
                turnRight();
            if (column == 20 && row == 34)
                turnLeft();
            if (column == 20 && row == 35)
                turnLeft();

            moveAndUpdateCoordinates();
            waitStation();
        }

    }

    public void goToSJ() // While the train is not in San Javier
    {
        while (column != 1 || row != 16) {
            if (column == 16 && row == 32)
                turnRight();
            if (column == 16 && row == 29)
                turnRight();
            if (column == 15 && row == 29)
                turnLeft();
            if (column == 15 && row == 26)
                turnRight();
            if (column == 13 && row == 26)
                turnLeft();
            if (column == 13 && row == 23)
                turnRight();
            if (column == 11 && row == 23)
                turnLeft();
            if (column == 11 && row == 14)
                turnRight();
            if (column == 7 && row == 14)
                turnRight();
            if (column == 7 && row == 15)
                turnLeft();
            if (column == 2 && row == 15)
                turnRight();
            if (column == 2 && row == 17)
                turnLeft();
            if (column == 1 && row == 17)
                turnLeft();
            moveAndUpdateCoordinates();
        }
        System.out.println("Train ready at " + route + ". Waiting for others...");

        try {
            Train.startBarrier.await(); // Espera a los otros dos trenes
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        System.out.println("All trains ready! Starting route...");
    }

    public void SanjavierToSanAntonio() {
        while (column != 15 || row != 14) {
            if (column == 1 && row == 14)
                turnLeft();
            if (column == 6 && row == 14)
                turnRight();
            if (column == 6 && row == 13)
                turnLeft();
            if (column == 14 && row == 13)
                turnLeft();
            if (column == 14 && row == 14)
                turnRight();

            moveAndUpdateCoordinates();
            waitStation();
        }
        if (column == 15 && row == 14) // If the train is in San Antonio, turn left to go to the station
            turnLeft();
        turnLeft(); // Turn left to face the station
    }

    public void SanAntonioToSanjavier() {
        while (column != 1 || row != 16) {
            if (column == 7 && row == 14)
                turnRight();
            if (column == 7 && row == 15)
                turnLeft();
            if (column == 2 && row == 15)
                turnRight();
            if (column == 2 && row == 17)
                turnLeft();
            if (column == 1 && row == 17)
                turnLeft();

            moveAndUpdateCoordinates();
            waitStation();
        }
    }

}
