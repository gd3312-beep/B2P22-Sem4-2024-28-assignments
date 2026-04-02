package com.sem4.assignments.week3and4;

public class RiskThresholdBinaryLookup {
    public SearchResult linearSearch(int[] values, int target) {
        int comparisons = 0;
        for (int i = 0; i < values.length; i++) {
            comparisons++;
            if (values[i] == target) {
                return new SearchResult(i, comparisons, true);
            }
        }
        return new SearchResult(-1, comparisons, false);
    }

    public InsertionPointResult binaryInsertionPoint(int[] sortedValues, int target) {
        int low = 0;
        int high = sortedValues.length;
        int comparisons = 0;

        while (low < high) {
            int mid = (low + high) / 2;
            comparisons++;
            if (sortedValues[mid] < target) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }

        return new InsertionPointResult(low, comparisons);
    }

    public FloorCeilingResult findFloorAndCeiling(int[] sortedValues, int target) {
        Integer floor = null;
        Integer ceiling = null;
        int low = 0;
        int high = sortedValues.length - 1;
        int comparisons = 0;

        while (low <= high) {
            int mid = (low + high) / 2;
            comparisons++;

            if (sortedValues[mid] == target) {
                floor = sortedValues[mid];
                ceiling = sortedValues[mid];
                return new FloorCeilingResult(floor, ceiling, comparisons);
            }

            if (sortedValues[mid] < target) {
                floor = sortedValues[mid];
                low = mid + 1;
            } else {
                ceiling = sortedValues[mid];
                high = mid - 1;
            }
        }

        return new FloorCeilingResult(floor, ceiling, comparisons);
    }

    public static class SearchResult {
        public final int index;
        public final int comparisons;
        public final boolean found;

        public SearchResult(int index, int comparisons, boolean found) {
            this.index = index;
            this.comparisons = comparisons;
            this.found = found;
        }

        @Override
        public String toString() {
            return "SearchResult{index=" + index + ", comparisons=" + comparisons + ", found=" + found + "}";
        }
    }

    public static class InsertionPointResult {
        public final int insertionIndex;
        public final int comparisons;

        public InsertionPointResult(int insertionIndex, int comparisons) {
            this.insertionIndex = insertionIndex;
            this.comparisons = comparisons;
        }

        @Override
        public String toString() {
            return "InsertionPointResult{insertionIndex=" + insertionIndex
                    + ", comparisons=" + comparisons + "}";
        }
    }

    public static class FloorCeilingResult {
        public final Integer floor;
        public final Integer ceiling;
        public final int comparisons;

        public FloorCeilingResult(Integer floor, Integer ceiling, int comparisons) {
            this.floor = floor;
            this.ceiling = ceiling;
            this.comparisons = comparisons;
        }

        @Override
        public String toString() {
            return "FloorCeilingResult{floor=" + floor + ", ceiling=" + ceiling
                    + ", comparisons=" + comparisons + "}";
        }
    }
}
