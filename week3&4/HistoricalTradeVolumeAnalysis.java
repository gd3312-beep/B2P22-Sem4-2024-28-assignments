package com.sem4.assignments.week3and4;

import java.util.ArrayList;
import java.util.List;

public class HistoricalTradeVolumeAnalysis {
    public List<Trade> mergeSortAscending(List<Trade> trades) {
        if (trades.size() <= 1) {
            return new ArrayList<>(trades);
        }

        int mid = trades.size() / 2;
        List<Trade> left = mergeSortAscending(new ArrayList<>(trades.subList(0, mid)));
        List<Trade> right = mergeSortAscending(new ArrayList<>(trades.subList(mid, trades.size())));
        return merge(left, right);
    }

    public List<Trade> quickSortDescending(List<Trade> trades) {
        List<Trade> list = new ArrayList<>(trades);
        quickSort(list, 0, list.size() - 1);
        return list;
    }

    public List<Trade> mergeTwoSortedLists(List<Trade> first, List<Trade> second) {
        return merge(first, second);
    }

    public long getTotalVolume(List<Trade> trades) {
        long total = 0;
        for (Trade trade : trades) {
            total += trade.volume;
        }
        return total;
    }

    private List<Trade> merge(List<Trade> left, List<Trade> right) {
        List<Trade> merged = new ArrayList<>();
        int i = 0;
        int j = 0;

        while (i < left.size() && j < right.size()) {
            if (left.get(i).volume <= right.get(j).volume) {
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

    private void quickSort(List<Trade> list, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(list, low, high);
            quickSort(list, low, pivotIndex - 1);
            quickSort(list, pivotIndex + 1, high);
        }
    }

    private int partition(List<Trade> list, int low, int high) {
        long pivot = list.get(high).volume;
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (list.get(j).volume >= pivot) {
                i++;
                Trade temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }

        Trade temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);
        return i + 1;
    }

    public static class Trade {
        public final String tradeId;
        public final long volume;

        public Trade(String tradeId, long volume) {
            this.tradeId = tradeId;
            this.volume = volume;
        }

        @Override
        public String toString() {
            return tradeId + ":" + volume;
        }
    }
}
