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
    private int niquiaTrainsActuallyAssigned = 0; // Counter for verification

    public Order() {
        int columnPos = 15;
        int rowPos = 34;
        Direction currentDirection = North;
        int niquiaCount = 0;

        this.allTrainsAtStationsBarrier = new CyclicBarrier(TOTAL_TRAINS, () -> {
            System.out.println("************************************************************");
            System.out.println("TODOS LOS " + TOTAL_TRAINS +
                    " TRENES HAN LLEGADO A SUS ESTACIONES INICIALES.");
            System.out.println("INICIANDO OPERACIÓN COMERCIAL GLOBALMENTE.");
            System.out.println("************************************************************");
        });

        // Step 1: Pre-determine routes and colors for all trains
        String[] routes = new String[TOTAL_TRAINS];
        Color[] colors = new Color[TOTAL_TRAINS];

        // Assign routes/colors for i=29, 30, 31 explicitly
        colors[31] = Color.BLUE;
        routes[31] = "routeAN"; // This is the 1st Niquia train counted
        colors[30] = Color.BLUE;
        routes[30] = "routeAE";
        colors[29] = Color.GREEN;
        routes[29] = "routeSJ";

        // Assign routes/colors for i= 0, 1, 2 explicitly
        colors[0] = Color.GREEN;
        routes[0] = "routeSJ";
        colors[1] = Color.BLUE;
        routes[1] = "routeAE";
        colors[2] = Color.BLUE;
        routes[2] = "routeAE";

        List<Integer> blueTrainCandidateIndices_0_to_28 = new ArrayList<>();
        int tempLineBCount = 0; // Simulates lineBTrains.size() for decision making for i=0..28

        // Determine colors for trains i=0 to 28 and identify blue train candidates
        for (int i = 3; i < 29; i++) { // Loop for i from 0 to 28
            if (i < 6) {
                colors[i] = Color.BLUE; // These are initially blue
                // Trenes 0 y 1 van a La Estrella, tren 2 a Niquía
            } else if ((i - 3) % 3 == 0 && tempLineBCount < 10) {
                colors[i] = Color.GREEN;
                routes[i] = "routeSJ"; // Green trains go to San Javier
                tempLineBCount++;
            } else {
                colors[i] = Color.BLUE; // Otherwise, it's a blue train
            }

            if (colors[i] == Color.BLUE) {
                blueTrainCandidateIndices_0_to_28.add(i);
            }
        }

        // Step 2: Assign routes for the blue trains in slots i=0 to 28.
        // We need to assign 5 more "routeAN" (since i=31 is already one).
        // Prioritize those with larger 'i' values from the candidate list.
        Collections.sort(blueTrainCandidateIndices_0_to_28, Collections.reverseOrder()); // Sorts indices descending

        int anRoutesToAssign = 5;
        for (int trainIndex_i : blueTrainCandidateIndices_0_to_28) {
            if (routes[trainIndex_i] == null) { // Ensure it's not already assigned (e.g. green)
                if (anRoutesToAssign > 0) {
                    routes[trainIndex_i] = "routeAN";
                    anRoutesToAssign--;
                } else {
                    routes[trainIndex_i] = "routeAE";
                }
            }
        }

        for (int i = 0; i < TOTAL_TRAINS; i++) {
            // Ensure all blue trains have a route if not set above (should not happen with
            // this logic)
            if (colors[i] == Color.BLUE && routes[i] == null) {
                routes[i] = "routeAE"; // Default for any missed blue trains
            }

            Train train = new Train(rowPos, columnPos, currentDirection, 0, colors[i],
                    routes[i], this, allTrainsAtStationsBarrier);
            creationOrderTrains.add(train);

            // Add to specific line lists and count Niquia trains
            if (colors[i] == Color.GREEN) {
                lineBTrains.add(train);
            } else if (colors[i] == Color.BLUE) {
                lineATrains.add(train);
                if (routes[i].equals("routeAN")) {
                    niquiaTrainsActuallyAssigned++;
                }
            }

            map[rowPos][columnPos] = 1; // Mark initial position on map

            // Update position for next train in workshop (as per your original logic)
            if (currentDirection == North)
                rowPos++;
            else if (currentDirection == South)
                rowPos--;
            else if (currentDirection == East)
                columnPos++;
            else if (currentDirection == West)
                columnPos--;

            // Turn logic for workshop placement (as per your original logic)
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

        System.out.println("Total Niquia (routeAN) trains assigned: " + niquiaTrainsActuallyAssigned);
        if (niquiaTrainsActuallyAssigned != 6) {
            System.err.println("WARNING: Expected 6 Niquia trains, but got " + niquiaTrainsActuallyAssigned);
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
                        Thread.sleep(50); // Espera y verifica
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
            boolean trainProgressedEnough = false;

            // Check if train is at its final designated initial station
            int[] targetStation = getStationCoordinates(t.getRoute()); // Niquia, La Estrella, or San Javier
            if (t.getRow() == targetStation[0] && t.getColumn() == targetStation[1]) {
                trainProgressedEnough = true;
            }

            // Trenes en niquia -> 6
            if (!trainProgressedEnough && t.getRoute().equals("routeAN")) {
                if (!trainProgressedEnough && t.getColumn() == 20 && t.getRow() == 35) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 20 && t.getRow() == 34) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 19 && t.getRow() == 34) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 18 && t.getRow() == 34) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 17 && t.getRow() == 34) {
                    trainProgressedEnough = true;
                }
            }

            // Trenes en la estrella -> 16
            if (!trainProgressedEnough && t.getRoute().equals("routeAE")) {
                
                if (!trainProgressedEnough && t.getColumn() == 10 && t.getRow() == 2) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 10 && t.getRow() == 1) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 11 && t.getRow() == 2) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 12 && t.getRow() == 2) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 12 && t.getRow() == 3) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 12 && t.getRow() == 4) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 12 && t.getRow() == 5) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 13 && t.getRow() == 5) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 13 && t.getRow() == 6) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 13 && t.getRow() == 7) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 13 && t.getRow() == 8) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 13 && t.getRow() == 9) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 13 && t.getRow() == 10) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 13 && t.getRow() == 11) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 14 && t.getRow() == 11) {
                    trainProgressedEnough = true;
                }
            }

            // Trenes en san javier -> 10
            if (!trainProgressedEnough && t.getRoute().equals("routeSJ")) {
                if (!trainProgressedEnough && t.getColumn() == 1 && t.getRow() == 17) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 2 && t.getRow() == 17) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 2 && t.getRow() == 16) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 2 && t.getRow() == 15) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 3 && t.getRow() == 15) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 4 && t.getRow() == 15) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 5 && t.getRow() == 15) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 6 && t.getRow() == 15) {
                    trainProgressedEnough = true;
                }

                if (!trainProgressedEnough && t.getColumn() == 7 && t.getRow() == 15) {
                    trainProgressedEnough = true;
                }
            }
        }

        // All trains in this batch have reached either their final station or a
        // specified intermediate point.
        return true;
    }

    private int[] getStationCoordinates(String route) {
        switch (route) {
            case "routeAN":
                return new int[] { 35, 19 }; // Niquía
            case "routeAE":
                return new int[] { 1, 11 }; // La Estrella
            case "routeSJ":
                return new int[] { 16, 1 }; // San Javier
            default:
                throw new IllegalArgumentException("Ruta inválida");
        }
    }

}