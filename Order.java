import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import kareltherobot.*;
import java.awt.Color;
import java.util.Scanner;
import java.util.concurrent.locks.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CyclicBarrier;

public class Order implements Directions {
    public ArrayList<Train> lineATrains = new ArrayList<>();
    public ArrayList<Train> lineBTrains = new ArrayList<>();
    public ArrayList<Train> creationOrderTrains = new ArrayList<>();
    private final int TOTAL_TRAINS = 32;
    public int[][] map = new int[36][21];
    public volatile boolean waitingFor420 = true;
    public volatile boolean isReturningToDepot = false;
    
    // Semáforos para zonas críticas
    private final Semaphore intersectionSemaphore = new Semaphore(1);
    private final Semaphore cisnerosToSanAntonioSemaphore = new Semaphore(1);
    private final Semaphore lineBExitSemaphore = new Semaphore(1); // Para (18,11)
    private final Semaphore lineATallerSemaphore = new Semaphore(1); // Para (19,12)

    // Nuevo semáforo para el retorno al taller
    private final Semaphore depotEntranceSemaphore = new Semaphore(1); // Permite solo 1 tren a la vez
    private final Object depotLock = new Object();
    private volatile boolean trainEntering = false;

    // Coordenadas de estaciones iniciales
    private final int[] NIQUIA_COORDS = {35, 19};
    private final int[] ESTRELLA_COORDS = {16, 1};
    private final int[] SANJAVIER_COORDS = {1, 11};

    // Coordenadas de zonas críticas
    public final int[] INTERSECTION_COORDS = {32, 16};
    public final int[] CISNEROS_COORDS = {13, 12};
    public final int[] SAN_ANTONIO_COORDS = {14, 15};
    public final int[] LINE_B_EXIT_COORDS = {18, 11}; // Nueva zona crítica
    public final int[] LINE_A_TALLER_COORDS = {19, 12}; // Nueva zona crítica
    public final int[] DEPOT_ENTRANCE_COORDS = {14, 32}; // Punto de entrada al taller

    public Order() {
        initializeTrains();
    }

    private void initializeTrains() {
        int posColumna = 15;
        int posFila = 34;
        Direction tipoDireccion = North;
        int contador = 0;

        while (lineATrains.size() < 22 || lineBTrains.size() < 10) {
            if (contador % 3 == 0) {
                Train trenB = new Train(posFila, posColumna, tipoDireccion, 0, Color.GREEN, "routeSJ", this);
                lineBTrains.add(trenB);
                creationOrderTrains.add(trenB);
            } else if (contador % 3 == 1) {
                Train trenA = new Train(posFila, posColumna, tipoDireccion, 0, Color.BLUE, "routeAN", this);
                lineATrains.add(trenA);
                creationOrderTrains.add(trenA);
            } else {
                Train trenA = new Train(posFila, posColumna, tipoDireccion, 0, Color.BLUE, "routeAE", this);
                lineATrains.add(trenA);
                creationOrderTrains.add(trenA);
            }
            map[posFila][posColumna] = 1;

            if (tipoDireccion == North) posFila++;
            else if (tipoDireccion == South) posFila--;
            else if (tipoDireccion == East) posColumna++;
            else if (tipoDireccion == West) posColumna--;

            if (posColumna == 15 && posFila == 35) tipoDireccion = West;
            if (posColumna == 1 && posFila == 35) tipoDireccion = South;
            if (posColumna == 1 && posFila == 34) tipoDireccion = East;
            if (posColumna == 14 && posFila == 34) tipoDireccion = South;
            if (posColumna == 14 && posFila == 32) tipoDireccion = East;

            contador++;
        }

        lineATrains.get(lineATrains.size() - 1).setInitialTrain(true);
        lineATrains.get(lineATrains.size() - 2).setInitialTrain(true);
        lineBTrains.get(lineBTrains.size() - 1).setInitialTrain(true);
    }

    private void startRemainingTrains() {
        new Thread(() -> {
            for (int i = creationOrderTrains.size() - 4; i >= 0; i--) {
                new Thread(creationOrderTrains.get(i)).start();
                try {
                    Thread.sleep(3000); // Aumentado a 3 segundos
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public void startSystem() {
        System.out.println("Iniciando los 3 trenes iniciales...");
        
        new Thread(lineATrains.get(lineATrains.size() - 1)).start();
        new Thread(lineATrains.get(lineATrains.size() - 2)).start();
        new Thread(lineBTrains.get(lineBTrains.size() - 1)).start();

        System.out.println("Esperando que los trenes lleguen a sus posiciones iniciales...");
        while (!(map[NIQUIA_COORDS[0]][NIQUIA_COORDS[1]] == 1 && 
                map[ESTRELLA_COORDS[0]][ESTRELLA_COORDS[1]] == 1 && 
                map[SANJAVIER_COORDS[0]][SANJAVIER_COORDS[1]] == 1)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\n\n==================================");
        System.out.println("¿Qué hora es? (Ingresa '420' o '4:20' para comenzar)");
        System.out.println("==================================\n");
        
        Scanner input = new Scanner(System.in);
        String hora = input.nextLine();

        if (hora.equals("420") || hora.equals("4:20")) {
            System.out.println("\n¡Iniciando servicio comercial!");
            waitingFor420 = false;
            startRemainingTrains();
            
            // Esperar el input de las 11:00
            System.out.println("\n==================================");
            System.out.println("SISTEMA EN OPERACIÓN");
            System.out.println("Ingresa '11:00' o '11' para retornar al taller");
            System.out.println("==================================\n");
            
            String hora11 = input.nextLine();
            if (hora11.equals("11:00")|| hora11.equals("11")) {
                System.out.println("\n==================================");
                System.out.println("¡ATENCIÓN! RETORNO AL TALLER INICIADO");
                System.out.println("==================================\n");
                isReturningToDepot = true;
            }
            
        } else {
            System.out.println("Hora no válida. El programa se cerrará.");
            System.exit(0);
        }
    }

    public void acquireIntersection() throws InterruptedException {
        intersectionSemaphore.acquire();
    }

    public void releaseIntersection() {
        intersectionSemaphore.release();
    }

    public void acquireCisnerosToSanAntonio() throws InterruptedException {
        cisnerosToSanAntonioSemaphore.acquire();
    }

    public void releaseCisnerosToSanAntonio() {
        cisnerosToSanAntonioSemaphore.release();
    }

    public void acquireLineBExit() throws InterruptedException {
        lineBExitSemaphore.acquire();
    }

    public void releaseLineBExit() {
        lineBExitSemaphore.release();
    }

    public void acquireLineATaller() throws InterruptedException {
        lineATallerSemaphore.acquire();
    }

    public void releaseLineATaller() {
        lineATallerSemaphore.release();
    }

    public void acquireDepotEntrance() throws InterruptedException {
        synchronized(depotLock) {
            while (trainEntering) {
                depotLock.wait();
            }
            trainEntering = true;
            depotEntranceSemaphore.acquire();
        }
    }

    public void releaseDepotEntrance() {
        synchronized(depotLock) {
            depotEntranceSemaphore.release();
            trainEntering = false;
            depotLock.notifyAll();
            
            // Esperar un tiempo antes de permitir que entre el siguiente tren
            try {
                Thread.sleep(2000); // 2 segundos de espera entre trenes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void updateMap(int oldRow, int oldCol, int newRow, int newCol) {
        map[oldRow][oldCol] = 0;
        map[newRow][newCol] = 1;
    }

    // Getters para las coordenadas de estaciones
    public int[] getNiquiaCoords() {
        return NIQUIA_COORDS;
    }

    public int[] getEstrellaCoords() {
        return ESTRELLA_COORDS;
    }

    public int[] getSanJavierCoords() {
        return SANJAVIER_COORDS;
    }


}