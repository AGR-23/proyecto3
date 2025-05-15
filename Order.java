import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import kareltherobot.*;
import java.awt.Color;

public class Order implements Directions {
    public ArrayList<Train> lineATrains = new ArrayList<>();
    public ArrayList<Train> lineBTrains = new ArrayList<>();
    public ArrayList<Train> creationOrderTrains = new ArrayList<>();
    private final int TOTAL_TRAINS = 32;
    public int[][] map = new int[36][21];
    private Semaphore batchSemaphore = new Semaphore(1); // Semaforo para controlar lotes
    private CyclicBarrier allTrainsAtStationsBarrier;

    public Order() {
        int columnPos = 15;
        int rowPos = 34;
        Direction currentDirection = North;
        int niquiaCount = 0;

        // Inicializa la barrera para que espere a TOTAL_TRAINS hilos (trenes).
        // El Runnable opcional se ejecuta cuando todos los trenes han llegado a la
        // barrera.
        this.allTrainsAtStationsBarrier = new CyclicBarrier(TOTAL_TRAINS, () -> {
            System.out.println("************************************************************");
            System.out.println("TODOS LOS " + TOTAL_TRAINS + " TRENES HAN LLEGADO A SUS ESTACIONES INICIALES.");
            System.out.println("INICIANDO OPERACIÓN COMERCIAL GLOBALMENTE.");
            System.out.println("************************************************************");
        });

        // Crear 32 trenes (22 azules + 10 verdes)
        for (int i = 0; i < 32; i++) {
            Train train;
            if (i < 3) { // Primeros 3 trenes son azules a Niquía
                train = new Train(rowPos, columnPos, currentDirection, 0, Color.BLUE, "routeAN", this,
                        allTrainsAtStationsBarrier);
                lineATrains.add(train);
                niquiaCount++;
            } else {
                if ((i - 3) % 3 == 0 && lineBTrains.size() < 10) { // Tren verde (Línea B)
                    train = new Train(rowPos, columnPos, currentDirection, 0, Color.GREEN, "routeSJ", this,
                            allTrainsAtStationsBarrier);
                    lineBTrains.add(train);
                } else { // Tren azul (Línea A)
                    String route = (niquiaCount < 5) ? "routeAN" : "routeAE";
                    train = new Train(rowPos, columnPos, currentDirection, 0, Color.BLUE, route, this,
                            allTrainsAtStationsBarrier);
                    lineATrains.add(train);
                    if (route.equals("routeAN"))
                        niquiaCount++;
                }
            }
            creationOrderTrains.add(train);
            map[rowPos][columnPos] = 1;

            // Actualizar posición
            if (currentDirection == North)
                rowPos++;
            else if (currentDirection == South)
                rowPos--;
            else if (currentDirection == East)
                columnPos++;
            else if (currentDirection == West)
                columnPos--;

            // Giro en esquinas
            if (columnPos == 15 && rowPos == 35)
                currentDirection = West;
            else if (columnPos == 1 && rowPos == 35)
                currentDirection = South;
            else if (columnPos == 1 && rowPos == 34)
                currentDirection = East;
            else if (columnPos == 14 && rowPos == 34)
                currentDirection = South;
            else if (columnPos == 14 && rowPos == 32)
                currentDirection = East;
        }
    }

    // En Order.java
    public void startAllTrainsInBatches(int batchSize) {
        // Necesitarás un ExecutorService si no lo tienes ya a nivel de clase o método
        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_TRAINS); // O un tamaño adecuado

        List<Train> trainsToStart = new ArrayList<>(this.creationOrderTrains); // Usa la lista poblada
        Collections.reverse(trainsToStart);

        for (int i = 0; i < trainsToStart.size(); i += batchSize) {
            try {
                batchSemaphore.acquire(); // Espera a que el lote anterior esté en estación
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupción al esperar por batchSemaphore");
                return; // O manejar de otra forma
            }

            List<Train> currentBatch = new ArrayList<>();
            for (int j = 0; j < batchSize && (i + j) < trainsToStart.size(); j++) {
                currentBatch.add(trainsToStart.get(i + j));
            }

            for (Train train : currentBatch) {
                executor.execute(train); // Inicia el método run() del tren en un nuevo hilo
            }

            // El hilo que espera a que ESTE LOTE llegue a la estación
            // para liberar el batchSemaphore para el SIGUIENTE LOTE.
            final List<Train> batchToCheck = new ArrayList<>(currentBatch); // Copia efectiva para la lambda
            new Thread(() -> {
                while (!allTrainsInStation(batchToCheck)) {
                    try {
                        Thread.sleep(200); // Espera y verifica
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Hilo de espera de lote interrumpido.");
                        return;
                    }
                }

                batchSemaphore.release(); // Libera para el siguiente lote
            }).start();
        }

        // No apagues el executor inmediatamente si los trenes siguen operando.
        // executor.shutdown(); // Podrías querer apagarlo más tarde, o usar un executor
        // gestionado externamente.
    }

    private boolean allTrainsInStation(List<Train> batch) {
        for (Train t : batch) {
            int[] target = getStationCoordinates(t.getRoute());
            if (t.getRow() != target[0] || t.getColumn() != target[1])
                return false;
        }
        return true;
    }

    private int[] getStationCoordinates(String route) {
        switch (route) {
            case "routeAN":
                return new int[] { 19, 35 }; // Niquía
            case "routeAE":
                return new int[] { 11, 1 }; // La Estrella
            case "routeSJ":
                return new int[] { 1, 16 }; // San Javier
            default:
                throw new IllegalArgumentException("Ruta inválida");
        }
    }
}