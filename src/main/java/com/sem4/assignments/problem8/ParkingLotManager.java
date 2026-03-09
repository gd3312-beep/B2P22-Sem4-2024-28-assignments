package com.sem4.assignments.problem8;

import java.time.Instant;
import java.time.ZoneId;

public class ParkingLotManager {
    private final ParkingSpot[] table;
    private final double hourlyRate;
    private final int[] hourlyEntries = new int[24];

    private int occupiedCount;
    private long totalParkingOperations;
    private long totalProbes;

    public ParkingLotManager(int capacity, double hourlyRate) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }

        this.table = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
        this.hourlyRate = hourlyRate;
    }

    public synchronized ParkResult parkVehicle(String licensePlate) {
        String normalizedPlate = normalize(licensePlate);
        if (normalizedPlate.isBlank()) {
            return new ParkResult(false, -1, 0, "License plate cannot be blank");
        }

        if (findSpotIndex(normalizedPlate) != -1) {
            return new ParkResult(false, -1, 0, "Vehicle is already parked");
        }

        int hash = hash(normalizedPlate);
        int firstDeleted = -1;

        for (int probe = 0; probe < table.length; probe++) {
            int index = (hash + probe) % table.length;
            ParkingSpot spot = table[index];

            if (spot.status == SpotStatus.DELETED && firstDeleted == -1) {
                firstDeleted = index;
                continue;
            }

            if (spot.status == SpotStatus.EMPTY) {
                int target = firstDeleted != -1 ? firstDeleted : index;
                occupy(target, normalizedPlate, probe);
                return new ParkResult(true, target, probe, "Assigned spot #" + target);
            }
        }

        if (firstDeleted != -1) {
            occupy(firstDeleted, normalizedPlate, table.length - 1);
            return new ParkResult(true, firstDeleted, table.length - 1, "Assigned spot #" + firstDeleted);
        }

        return new ParkResult(false, -1, table.length, "Parking lot is full");
    }

    public synchronized ExitResult exitVehicle(String licensePlate) {
        String normalizedPlate = normalize(licensePlate);
        int index = findSpotIndex(normalizedPlate);
        if (index == -1) {
            return new ExitResult(false, -1, 0, 0.0, "Vehicle not found");
        }

        long now = System.currentTimeMillis();
        ParkingSpot spot = table[index];
        long durationMillis = Math.max(0, now - spot.entryEpochMillis);
        double durationHours = Math.max(0.25, durationMillis / 3_600_000.0);
        double fee = durationHours * hourlyRate;

        spot.status = SpotStatus.DELETED;
        spot.licensePlate = null;
        spot.entryEpochMillis = 0;
        occupiedCount--;

        return new ExitResult(true, index, durationMillis, fee, "Spot freed");
    }

    public synchronized int findNearestAvailableSpot(int entranceSpot) {
        if (occupiedCount == table.length) {
            return -1;
        }

        int normalizedEntrance = Math.floorMod(entranceSpot, table.length);
        for (int distance = 0; distance < table.length; distance++) {
            int right = (normalizedEntrance + distance) % table.length;
            if (isAvailable(right)) {
                return right;
            }

            int left = Math.floorMod(normalizedEntrance - distance, table.length);
            if (left != right && isAvailable(left)) {
                return left;
            }
        }

        return -1;
    }

    public synchronized ParkingStatistics getStatistics() {
        double occupancyPercent = (occupiedCount * 100.0) / table.length;
        double averageProbes = totalParkingOperations == 0 ? 0.0 : (double) totalProbes / totalParkingOperations;

        int peakHour = 0;
        int peakCount = 0;
        for (int hour = 0; hour < 24; hour++) {
            if (hourlyEntries[hour] > peakCount) {
                peakCount = hourlyEntries[hour];
                peakHour = hour;
            }
        }

        String peakHourLabel = String.format("%02d:00-%02d:00", peakHour, (peakHour + 1) % 24);
        return new ParkingStatistics(occupancyPercent, averageProbes, peakHourLabel, occupiedCount, table.length);
    }

    private void occupy(int index, String licensePlate, int probes) {
        ParkingSpot spot = table[index];
        spot.status = SpotStatus.OCCUPIED;
        spot.licensePlate = licensePlate;
        spot.entryEpochMillis = System.currentTimeMillis();

        int currentHour = Instant.ofEpochMilli(spot.entryEpochMillis)
                .atZone(ZoneId.systemDefault())
                .getHour();
        hourlyEntries[currentHour]++;

        occupiedCount++;
        totalParkingOperations++;
        totalProbes += probes;
    }

    private int findSpotIndex(String licensePlate) {
        int hash = hash(licensePlate);

        for (int probe = 0; probe < table.length; probe++) {
            int index = (hash + probe) % table.length;
            ParkingSpot spot = table[index];

            if (spot.status == SpotStatus.EMPTY) {
                return -1;
            }

            if (spot.status == SpotStatus.OCCUPIED && licensePlate.equals(spot.licensePlate)) {
                return index;
            }
        }

        return -1;
    }

    private boolean isAvailable(int index) {
        SpotStatus status = table[index].status;
        return status == SpotStatus.EMPTY || status == SpotStatus.DELETED;
    }

    private int hash(String licensePlate) {
        return Math.floorMod(licensePlate.hashCode(), table.length);
    }

    private String normalize(String licensePlate) {
        return licensePlate == null ? "" : licensePlate.trim().toUpperCase();
    }

    private static final class ParkingSpot {
        private SpotStatus status = SpotStatus.EMPTY;
        private String licensePlate;
        private long entryEpochMillis;
    }

    private enum SpotStatus {
        EMPTY,
        OCCUPIED,
        DELETED
    }

    public record ParkResult(boolean success, int spotNumber, int probes, String message) { }

    public record ExitResult(boolean success, int spotNumber, long durationMillis, double fee, String message) { }

    public record ParkingStatistics(
            double occupancyPercent,
            double averageProbes,
            String peakHour,
            int occupied,
            int capacity
    ) { }
}
