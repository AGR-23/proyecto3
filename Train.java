
import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.*;

public class Train extends Robot implements Runnable, Directions {

    // Add a unique ID for each train
    private static int idCounter = 1;
    private int id;

    public int getID() {
        return id;
    }

    public int row;
    public int column;
    public String route;
    Order orderManager;
    boolean isInNiquia = false; // Flag to check if the train is in Niquia
    private boolean isInitialTrain; // Indica si es uno de los 3 trenes iniciales
    private final CyclicBarrier allTrainsAtStationsBarrier; // Nueva barrera

    // Métodos para acceder a la posición actual (añadir getters)

    public String getRoute() {
        return this.route;
    }

    public int getRow() {
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }

    public Train(int street, int avenue, Direction dir, int beeps, Color color,
            String route, Order orderManager, CyclicBarrier allTrainsBarrier) {
        super(street, avenue, dir, beeps, color);
        this.route = route;
        this.orderManager = orderManager;
        this.row = street;
        this.column = avenue;
        this.allTrainsAtStationsBarrier = allTrainsBarrier; // Guardar la barrera
        this.id = idCounter++;
        World.setupThread(this);
    }

    @Override
    public void run() {
        exitDepot(); // 1. Salir del taller

        goToStation(); // 2. Ir a la estación inicial asignada

        // 3. Esperar a que TODOS los trenes lleguen a sus estaciones
        try {
            System.out.println("Tren " + getID() + " (" + route + ") en estación (" + row + "," + column
                    + "). Esperando en barrera global...");
            allTrainsAtStationsBarrier.await(); // Todos los trenes esperan aquí
        } catch (InterruptedException e) {
            System.err.println("Tren " + getID() + " interrumpido mientras esperaba en la barrera.");
            Thread.currentThread().interrupt(); // Restablecer estado de interrupción
            World.stop();
            return;
        } catch (BrokenBarrierException e) {
            System.err.println("Barrera rota para el tren " + getID() + ". Posiblemente otro tren falló.");
            World.stop();
            return;
        }

        // 4. Una vez la barrera se libera (todos los 32 trenes han llegado), iniciar
        // ruta comercial.
        System.out.println(
                "Tren " + getID() + " (" + route + ") iniciando ruta comercial desde (" + row + "," + column + ").");
        startCommercialRoute();
    }

    private void goToStation() {
        switch (route) {
            case "routeAN":
                goToAN();
                break;
            case "routeAE":
                goToAE();
                break;
            case "routeSJ":
                goToSJ();
                break;
        }
        System.out.println("Train " + this + " en posición: " + route);
    }

    private void startCommercialRoute() {
        System.out.println("INICIANDO RUTA COMERCIAL: " + route);
        while (true) {
            switch (route) {
                case "routeAN":
                    niquiaToLaEstrella();
                    route = "routeAE";
                    break;
                case "routeAE":
                    laEstrellaToNiquia();
                    route = "routeAN";
                    break;
                case "routeSJ":
                    SanjavierToSanAntonio();
                    route = "routeSA";
                    break;
                case "routeSA":
                    SanAntonioToSanjavier();
                    route = "routeSJ";
                    break;
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

    private void exitDepot() {
        while (column != 16 || row != 32) {
            synchronized (orderManager.map) {
                if (frontIsClear()) {
                    int nextRow = row;
                    int nextCol = column;
                    // Calcular la próxima celda basada en la dirección actual
                    if (facingNorth()) {
                        nextRow++;
                    } else if (facingSouth()) {
                        nextRow--;
                    } else if (facingEast()) {
                        nextCol++;
                    } else if (facingWest()) {
                        nextCol--;
                    }

                    // Verificar si la próxima celda está libre en el mapa
                    if (orderManager.map[nextRow][nextCol] == 0) {
                        moveAndUpdateCoordinates();
                    } else {
                        System.out.println(
                                "Tren " + getID() + " esperando: celda ocupada en (" + nextRow + "," + nextCol + ")");
                        try {
                            Thread.sleep(0); // Espera más corta para reintentar
                        } catch (InterruptedException e) {
                        }
                    }
                } else {
                    // Lógica de giro para paredes físicas
                    if (column == 15 && row == 35 && facingNorth()) {
                        turnLeft();
                    } else if (column == 1 && row == 35 && facingWest()) {
                        turnLeft();
                    } else if (column == 1 && row == 34 && facingSouth()) {
                        turnLeft();
                    } else if (column == 14 && row == 34 && facingEast()) {
                        turnRight();
                    } else if (column == 14 && row == 32 && facingSouth()) {
                        turnLeft();
                    } else {
                        turnRight();
                    }
                }
            }
        }
        System.out.println("Tren " + this + " salió del taller");
    }

    private boolean canMoveSafely() {
        // Verificar si la próxima celda está libre
        int nextRow = row, nextCol = column;
        if (facingNorth())
            nextRow++;
        else if (facingSouth())
            nextRow--;
        else if (facingEast())
            nextCol++;
        else if (facingWest())
            nextCol--;

        return orderManager.map[nextRow][nextCol] == 0;
    }

    private void moveAndUpdateCoordinates() {
        synchronized (orderManager.map) { // Synchronize on the shared map resource
            int prevInternalRow = this.row;
            int prevInternalCol = this.column;

            int nextInternalRow = this.row;
            int nextInternalCol = this.column;

            if (facingNorth())
                nextInternalRow++;
            else if (facingSouth())
                nextInternalRow--;
            else if (facingEast())
                nextInternalCol++;
            else if (facingWest())
                nextInternalCol--;

            // Check 1: Are the calculated next internal coordinates within bounds?
            if (nextInternalRow >= 0 && nextInternalRow < orderManager.map.length &&
                    nextInternalCol >= 0 && nextInternalCol < orderManager.map[0].length) {

                // Check 2: Is the target cell in the shared map free (not occupied by another
                // train)?
                if (orderManager.map[nextInternalRow][nextInternalCol] == 0) {

                    orderManager.map[prevInternalRow][prevInternalCol] = 0; // Vacate old spot in map

                    // Update internal coordinates to the new position
                    this.row = nextInternalRow;
                    this.column = nextInternalCol;

                    move(); // Execute the physical move (Karel)

                    // Mark new spot in the map using the now-updated internal coordinates
                    orderManager.map[this.row][this.column] = 1;

                    System.out.println("Tren " + getID() + " movido a (" + this.row + "," + this.column +
                            "). Actual Karel pos: (" + getRow() + "," + column + ")");
                } else {
                    // Target cell in map is occupied by another train.
                    System.out.println("Tren " + getID() + " at internal (" + prevInternalRow + "," + prevInternalCol +
                            ") - path to internal (" + nextInternalRow + "," + nextInternalCol +
                            ") blocked by map (another train). Holding position.");
                }
            } else {
                // Calculated next internal step is out of bounds. This indicates a flaw in
                System.err.println("Tren " + getID() + " at internal (" + prevInternalRow + "," + prevInternalCol +
                        ") - calculated next step internal (" + nextInternalRow + "," + nextInternalCol +
                        ") is out of bounds. Pathing logic error. Holding position.");
            }
        }
    }

    // All methods below are now inside the Train class

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
        while (column != 19 || row != 35) { // Corrected loop condition
            synchronized (orderManager.map) {
                if (column == 17 && row == 32 && facingEast()) {
                    turnLeft(); // Now at (32,17) facing North
                } else if (column == 17 && row == 34 && facingNorth()) {
                    turnRight(); // Now at (34,17) facing East
                } else if (column == 20 && row == 34 && facingEast()) {
                    turnLeft(); // Now at (34,20) facing North
                } else if (column == 20 && row == 35 && facingNorth()) {
                    turnLeft(); // Now at (35,20) facing West
                }

                // Step 2: After any necessary turns, check if the path in the NEW current
                // direction is clear.
                if (isNextCellFree()) {
                    moveAndUpdateCoordinates();
                } else {
                    // Path in the current (potentially new) direction is blocked
                    // This could be another train, or map boundary if turn logic is imperfect.
                    System.out.println("Train " + getID() + " at (" + row + "," + column + ") facing " +
                            getCurrentDirectionString() + ". Next cell blocked. Waiting.");
                    waitIfBlocked();
                }
            }
        }
        System.out.println("Train " + getID() + " (" + route + ") arrived at Niquia: (" + row + "," + column + ")");
    }

    // Helper method to get current direction as string for logging (optional)
    private String getCurrentDirectionString() {
        if (facingNorth())
            return "North";
        if (facingSouth())
            return "South";
        if (facingEast())
            return "East";
        if (facingWest())
            return "West";
        return "Unknown";
    }

    private boolean isNextCellFree() {
        int nextRow = row;
        int nextCol = column;

        if (facingNorth())
            nextRow++;
        else if (facingSouth())
            nextRow--;
        else if (facingEast())
            nextCol++;
        else if (facingWest())
            nextCol--;

        // Verificar que las coordenadas estén dentro del mapa
        if (nextRow < 0 || nextRow >= orderManager.map.length ||
                nextCol < 0 || nextCol >= orderManager.map[0].length) {
            return false; // Fuera de los límites del mapa
        }

        return orderManager.map[nextRow][nextCol] == 0;
    }

    private void waitIfBlocked() {
        try {
            Thread.sleep(0); // Esperar antes de reintentar
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
            synchronized (orderManager.map) {
                if (isNextCellFree()) {
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
                } else {
                    waitIfBlocked();
                }
            }
        }

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
            synchronized (orderManager.map) {
                if (isNextCellFree()) {
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
                } else {
                    waitIfBlocked();
                }
            }
        }
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
