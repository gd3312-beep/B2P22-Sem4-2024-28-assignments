package com.sem4.assignments.week3and4;

import java.util.ArrayList;
import java.util.List;

public class PortfolioReturnSorting {
    public List<Asset> mergeSortByReturnRate(List<Asset> assets) {
        if (assets.size() <= 1) {
            return new ArrayList<>(assets);
        }

        int mid = assets.size() / 2;
        List<Asset> left = mergeSortByReturnRate(new ArrayList<>(assets.subList(0, mid)));
        List<Asset> right = mergeSortByReturnRate(new ArrayList<>(assets.subList(mid, assets.size())));
        return merge(left, right);
    }

    public List<Asset> quickSortByReturnDescAndVolatilityAsc(List<Asset> assets) {
        List<Asset> list = new ArrayList<>(assets);
        quickSort(list, 0, list.size() - 1);
        return list;
    }

    private List<Asset> merge(List<Asset> left, List<Asset> right) {
        List<Asset> merged = new ArrayList<>();
        int i = 0;
        int j = 0;

        while (i < left.size() && j < right.size()) {
            if (left.get(i).returnRate <= right.get(j).returnRate) {
                merged.add(left.get(i));
                i++;
            } else {
                merged.add(right.get(j));
                j++;
            }
        }

        while (i < left.size()) {
            merged.add(left.get(i));
            i++;
        }

        while (j < right.size()) {
            merged.add(right.get(j));
            j++;
        }

        return merged;
    }

    private void quickSort(List<Asset> list, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(list, low, high);
            quickSort(list, low, pivotIndex - 1);
            quickSort(list, pivotIndex + 1, high);
        }
    }

    private int partition(List<Asset> list, int low, int high) {
        Asset pivot = list.get(high);
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comesBefore(list.get(j), pivot)) {
                i++;
                Asset temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }

        Asset temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);
        return i + 1;
    }

    private boolean comesBefore(Asset current, Asset pivot) {
        if (current.returnRate > pivot.returnRate) {
            return true;
        }
        if (current.returnRate == pivot.returnRate) {
            return current.volatility < pivot.volatility;
        }
        return false;
    }

    public static class Asset {
        public final String assetId;
        public final double returnRate;
        public final double volatility;

        public Asset(String assetId, double returnRate, double volatility) {
            this.assetId = assetId;
            this.returnRate = returnRate;
            this.volatility = volatility;
        }

        @Override
        public String toString() {
            return assetId + ":" + returnRate;
        }
    }
}
