import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.*;

public class Train extends Robot implements Runnable, Directions {
    private static int idCounter = 1;
    private int id;
    public int row;
    public int column;
    public String route;
    Order orderManager;
    private boolean isInitialTrain;

    public Train(int street, int avenue, Direction dir, int beeps, Color color,
            String route, Order orderManager) {
        super(street, avenue, dir, beeps, color);
        this.id = idCounter++;
        this.row = street;
        this.column = avenue;
        this.route = route;
        this.orderManager = orderManager;
        World.setupThread(this);
    }

    @Override
    public void run() {
        exitDepot();
        goToInitialStation();
        
        // Si es un tren inicial, esperar por el input
        if (isInitialTrain) {
            while (orderManager.waitingFor420) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        while (!orderManager.isReturningToDepot) {
            startCommercialRoute();
        }
        
        // Cuando es 11:00, retornar al taller
        returnToDepot();
    }

    private void exitDepot() {
        while (column != 16 || row != 32) {
            if (column == 15 && row == 35 && !facingWest()) turnLeft();
            if (column == 1 && row == 35 && !facingSouth()) turnLeft();
            if (column == 1 && row == 34 && !facingEast()) turnLeft();
            if (column == 14 && row == 34 && !facingSouth()) turnRight();
            if (column == 14 && row == 32 && !facingEast()) turnLeft();
            moverActualizandoCoord();
        }
    }

    private void moverActualizandoCoord() {
        int filaAntes = row;
        int columnaAntes = column;
        int nuevaFila = row;
        int nuevaColumna = column;

        if (facingNorth()) nuevaFila++;
        else if (facingSouth()) nuevaFila--;
        else if (facingEast()) nuevaColumna++;
        else if (facingWest()) nuevaColumna--;

        // Verificar límites del mapa
        if (nuevaFila < 0 || nuevaFila >= orderManager.map.length ||
            nuevaColumna < 0 || nuevaColumna >= orderManager.map[0].length) {
            return;
        }

        boolean needsIntersectionLock = isNearIntersection(nuevaFila, nuevaColumna);
        boolean isSanAntonio = (nuevaFila == 14 && nuevaColumna == 15);
        boolean leavingSanAntonio = (filaAntes == 14 && columnaAntes == 15);
        
        try {
            if (needsIntersectionLock) {
                orderManager.acquireIntersection();
            }
            
            // Verificar si podemos movernos
            if (orderManager.map[nuevaFila][nuevaColumna] == 0) {
                orderManager.updateMap(filaAntes, columnaAntes, nuevaFila, nuevaColumna);
                row = nuevaFila;
                column = nuevaColumna;
                move();
                
                // Si estamos saliendo de San Antonio, liberar el semáforo
                if (leavingSanAntonio) {
                    orderManager.releaseCisnerosToSanAntonio();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (needsIntersectionLock) {
                orderManager.releaseIntersection();
            }
        }

        // Si no se pudo mover, esperar un tiempo mínimo
        if (filaAntes == row && columnaAntes == column) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isNearIntersection(int fila, int columna) {
        // Verificar si estamos en o cerca de la intersección crítica (16,32)
        return (fila == 32 && columna == 16) || // La intersección misma
               (fila == 32 && columna == 15) || // Celda antes
               (fila == 32 && columna == 17) || // Celda después
               (fila == 31 && columna == 16) || // Celda arriba
               (fila == 33 && columna == 16);   // Celda abajo
    }

    private boolean isNearCisneros(int fila, int columna) {
        // Verificar si estamos en la zona Cisneros-San Antonio
        return (fila == 14 && columna == 14) || // Cisneros
               (fila == 13 && columna == 14) || // San Antonio
               (fila == 14 && columna == 13) || // Aproximación desde el oeste
               (fila == 14 && columna == 15) || // Aproximación desde el este
               (fila == 13 && columna == 13) || // Diagonal NO
               (fila == 13 && columna == 15);   // Diagonal NE
    }

    private void goToInitialStation() {
        switch (route) {
            case "routeAN":
                irAN();
                break;
            case "routeAE":
                irAE();
                break;
            case "routeSJ":
                irASJ();
                break;
        }
    }

    private void startCommercialRoute() {
            switch (route) {
                case "routeAN":
                while (!orderManager.isReturningToDepot) {
                    rutaAE();
                    if (orderManager.isReturningToDepot) break;
                    rutaAN();
                }
                if (orderManager.isReturningToDepot) {
                    returnToDepot();
                }
                    break;
                
                case "routeAE":
                while (!orderManager.isReturningToDepot) {
                    rutaAN();
                    if (orderManager.isReturningToDepot) break;
                    rutaAE();
                }
                if (orderManager.isReturningToDepot) {
                    returnToDepot();
                }
                    break;
                
                case "routeSJ":
                while (!orderManager.isReturningToDepot) {
                    rutaASA();
                    if (orderManager.isReturningToDepot) break;
                    rutaASJ();
                }
                if (orderManager.isReturningToDepot) {
                    returnToDepot();
                }
                    break;
        }
    }

    public void turnRight() {
        turnLeft();
        turnLeft();
        turnLeft();
    }

    public void irAN() {
        while (column != 19 || row != 35) {
            if (column == 17 && row == 32 && !facingNorth()) turnLeft();
            if (column == 17 && row == 34 && !facingEast()) turnRight();
            if (column == 20 && row == 34 && !facingNorth()) turnLeft();
            if (column == 20 && row == 35 && !facingWest()) turnLeft();
            moverActualizandoCoord();
        }
    }

    public void irAE() {
        while (column != 11 || row != 1) {
            if (column == 16 && row == 32 && !facingSouth()) turnRight();
            if (column == 16 && row == 29 && !facingWest()) turnRight();
            if (column == 15 && row == 29 && !facingSouth()) turnLeft();
            if (column == 15 && row == 26 && !facingWest()) turnRight();
            if (column == 13 && row == 26 && !facingSouth()) turnLeft();
            if (column == 13 && row == 23 && !facingWest()) turnRight();
            if (column == 11 && row == 23 && !facingSouth()) turnLeft();
            if (column == 11 && row == 18 && !facingEast()) turnLeft();
            if (column == 16 && row == 18 && !facingSouth()) turnRight();
            if (column == 16 && row == 11 && !facingWest()) turnRight();
            if (column == 13 && row == 11 && !facingSouth()) turnLeft();
            if (column == 13 && row == 5 && !facingWest()) turnRight();
            if (column == 12 && row == 5 && !facingSouth()) turnLeft();
            if (column == 12 && row == 2 && !facingWest()) turnRight();
            if (column == 10 && row == 2 && !facingSouth()) turnLeft();
            if (column == 10 && row == 1 && !facingEast()) turnLeft();
            moverActualizandoCoord();
        }
    }

    public void irASJ() {
        while (column != 1 || row != 16) {
            if (column == 16 && row == 32 && !facingSouth()) turnRight();
            if (column == 16 && row == 29 && !facingWest()) turnRight();
            if (column == 15 && row == 29 && !facingSouth()) turnLeft();
            if (column == 15 && row == 26 && !facingWest()) turnRight();
            if (column == 13 && row == 26 && !facingSouth()) turnLeft();
            if (column == 13 && row == 23 && !facingWest()) turnRight();
            if (column == 11 && row == 23 && !facingSouth()) turnLeft();
            if (column == 11 && row == 14 && !facingWest()) turnRight();
            if (column == 7 && row == 14 && !facingNorth()) turnRight();
            if (column == 7 && row == 15 && !facingWest()) turnLeft();
            if (column == 2 && row == 15 && !facingNorth()) turnRight();
            if (column == 2 && row == 17 && !facingWest()) turnLeft();
            if (column == 1 && row == 17 && !facingSouth()) turnLeft();
            moverActualizandoCoord();
        }
    }

    public void rutaAN() {
        while (column != 19 || row != 35) {
            waitStation();
            if (column == 13 && row == 1 && !facingNorth()) turnLeft();
            if (column == 13 && row == 4 && !facingEast()) turnRight();
            if (column == 14 && row == 4 && !facingNorth()) turnLeft();
            if (column == 14 && row == 10 && !facingEast()) turnRight();
            if (column == 17 && row == 10 && !facingNorth()) turnLeft();
            if (column == 17 && row == 19 && !facingWest()) turnLeft();
            if (column == 12 && row == 19 && !facingNorth()) turnRight();
            if (column == 12 && row == 22 && !facingEast()) turnRight();
            if (column == 14 && row == 22 && !facingNorth()) turnLeft();
            if (column == 14 && row == 25 && !facingEast()) turnRight();
            if (column == 16 && row == 25 && !facingNorth()) turnLeft();
            if (column == 16 && row == 28 && !facingEast()) turnRight();
            if (column == 17 && row == 28 && !facingNorth()) turnLeft();
            if (column == 17 && row == 34 && !facingEast()) turnRight();
            if (column == 20 && row == 34 && !facingNorth()) turnLeft();
            if (column == 20 && row == 35 && !facingWest()) turnLeft();
            moverActualizandoCoord();
        }
    }

    public void rutaAE() {
        while (column != 11 || row != 1) {
            waitStation();
            if (column == 16 && row == 35 && !facingSouth()) turnLeft();
            if (column == 16 && row == 29 && !facingWest()) turnRight();
            if (column == 15 && row == 29 && !facingSouth()) turnLeft();
            if (column == 15 && row == 26 && !facingWest()) turnRight();
            if (column == 13 && row == 26 && !facingSouth()) turnLeft();
            if (column == 13 && row == 23 && !facingWest()) turnRight();
            if (column == 11 && row == 23 && !facingSouth()) turnLeft();
            if (column == 11 && row == 18 && !facingEast()) turnLeft();
            if (column == 16 && row == 18 && !facingSouth()) turnRight();
            if (column == 16 && row == 11 && !facingWest()) turnRight();
            if (column == 13 && row == 11 && !facingSouth()) turnLeft();
            if (column == 13 && row == 5 && !facingWest()) turnRight();
            if (column == 12 && row == 5 && !facingSouth()) turnLeft();
            if (column == 12 && row == 2 && !facingWest()) turnRight();
            if (column == 10 && row == 2 && !facingSouth()) turnLeft();
            if (column == 10 && row == 1 && !facingEast()) turnLeft();
            moverActualizandoCoord();
        }
    }

    public void rutaASA() {
        while (column != 15 || row != 14) {
            waitStation();
            if (column == 1 && row == 14 && !facingEast()) turnLeft();
            if (column == 6 && row == 14 && !facingSouth()) turnRight();
            if (column == 6 && row == 13 && !facingEast()) turnLeft();
            if (column == 14 && row == 13 && !facingNorth()) turnLeft();
            if (column == 14 && row == 14 && !facingEast()) turnRight();
            moverActualizandoCoord();
        }
        turnLeft();
        turnLeft();
    }

    public void rutaASJ() {
        while (column != 1 || row != 16) {
            waitStation();
            if (column == 7 && row == 14 && !facingNorth()) turnRight();
            if (column == 7 && row == 15 && !facingWest()) turnLeft();
            if (column == 2 && row == 15 && !facingNorth()) turnRight();
            if (column == 2 && row == 17 && !facingWest()) turnLeft();
            if (column == 1 && row == 17 && !facingSouth()) turnLeft();
            moverActualizandoCoord();
        }
    }

    public void waitStation() {
        if (nextToABeeper()) {
            try {
                // Si estamos en Cisneros (fila 13, columna 12), esperar y verificar San Antonio
                if (row == 13 && column == 12) {
                    Thread.sleep(2000); // Espera normal en la estación
                    
                    // Si es hora de retornar y estamos en una estación terminal, iniciar retorno
                    if (orderManager.isReturningToDepot) {
                        returnToDepot();
                        return;
                    }
                    
                    // Esperar a que San Antonio esté libre
                    while (orderManager.map[14][15] == 1) {
                        Thread.sleep(100);
                    }
                    
                    // Adquirir el semáforo solo cuando San Antonio esté libre
                    orderManager.acquireCisnerosToSanAntonio();
                } else {
                    Thread.sleep(2000);
                    
                    // Si es hora de retornar y estamos en una estación terminal, iniciar retorno
                    int[] niquiaCoords = orderManager.getNiquiaCoords();
                    int[] estrellaCoords = orderManager.getEstrellaCoords();
                    
                    if (orderManager.isReturningToDepot && 
                        ((row == niquiaCoords[0] && column == niquiaCoords[1]) ||
                         (row == estrellaCoords[0] && column == estrellaCoords[1]) ||
                         (row == orderManager.SAN_ANTONIO_COORDS[0] && column == orderManager.SAN_ANTONIO_COORDS[1]))) {
                        returnToDepot();
                        return;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setInitialTrain(boolean isInitial) {
        this.isInitialTrain = isInitial;
    }

    public String getRoute() {
        return route;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    private void returnToDepot() {
        if (route.startsWith("routeA")) {
            // TODOS los trenes de línea A primero van a Niquía
            if (row != orderManager.getNiquiaCoords()[0] || column != orderManager.getNiquiaCoords()[1]) {
                rutaAN(); // Ir a Niquía sin importar la ruta original
            }
            // Una vez en Niquía, ir al taller
            goBackN();
        } else {
            // Trenes de línea B
            if (row != orderManager.getSanJavierCoords()[0] || column != orderManager.getSanJavierCoords()[1]) {
                rutaASJ();
            }
            try {
                Thread.sleep(1000); // Pequeña pausa para asegurar la llegada
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
            goBackS();
        }
        
        // Asegurarse de que el tren llegue al final del taller
        enterDepot();
    }
    
    private void enterDepot() {
        // Coordenadas del taller (1,34) aprox
        while (column != 1 || row != 34) {
            if (column == 14 && row == 32 && !facingWest()) turnLeft();
            if (column == 10 && row == 32 && !facingWest()) turnRight();
            if (column == 3 && row == 32 && !facingSouth()) turnRight();
            if (column == 3 && row == 34 && !facingWest()) turnRight();
            moverActualizandoCoord();
            
            // Pausa para evitar bloqueos
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void goBackN() { // Desde la posición Niquía
        int count = 0;
        // Ir al punto de intersección (14,32)
        while ((column != 14 || row != 32) && count < 100) {
            if (column == 19 && row == 35 && !facingWest()) turnLeft();
            if (column == 17 && row == 35 && !facingSouth()) turnLeft();
            if (column == 17 && row == 34 && !facingWest()) turnRight();
            if (column == 15 && row == 34 && !facingSouth()) turnLeft();
            if (column == 15 && row == 32 && !facingWest()) turnRight();
            moverActualizandoCoord();
            count++;
            
            // Breve pausa para control
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void goBackS() { // Desde la posición San Javier
        int count = 0;
        // Ir al punto de intersección (14,32)
        while ((column != 14 || row != 32) && count < 100) {
            if (column == 1 && row == 16 && !facingEast()) turnLeft();
            if (column == 6 && row == 16 && !facingNorth()) turnRight();
            if (column == 6 && row == 17 && !facingEast()) turnLeft();
            if (column == 11 && row == 17 && !facingNorth()) turnLeft();
            if (column == 11 && row == 23 && !facingEast()) turnRight();
            if (column == 13 && row == 23 && !facingNorth()) turnLeft();
            if (column == 13 && row == 26 && !facingEast()) turnRight();
            if (column == 15 && row == 26 && !facingNorth()) turnLeft();
            if (column == 15 && row == 32 && !facingWest()) turnRight();
            moverActualizandoCoord();
            count++;
            
            // Breve pausa para control
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
