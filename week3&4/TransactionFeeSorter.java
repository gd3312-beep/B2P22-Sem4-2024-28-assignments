package com.sem4.assignments.week3and4;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionFeeSorter {
    public BubbleSortResult bubbleSortByFee(List<Transaction> transactions) {
        List<Transaction> list = new ArrayList<>(transactions);
        int passes = 0;
        int swaps = 0;

        for (int i = 0; i < list.size() - 1; i++) {
            boolean changed = false;
            passes++;
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (list.get(j).fee > list.get(j + 1).fee) {
                    Transaction temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                    swaps++;
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }

        return new BubbleSortResult(list, passes, swaps);
    }

    public List<Transaction> insertionSortByFeeAndTimestamp(List<Transaction> transactions) {
        List<Transaction> list = new ArrayList<>(transactions);

        for (int i = 1; i < list.size(); i++) {
            Transaction current = list.get(i);
            int j = i - 1;

            while (j >= 0 && shouldComeAfter(list.get(j), current)) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, current);
        }

        return list;
    }

    public List<Transaction> getHighFeeOutliers(List<Transaction> transactions, double limit) {
        List<Transaction> outliers = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.fee > limit) {
                outliers.add(transaction);
            }
        }
        return outliers;
    }

    private boolean shouldComeAfter(Transaction left, Transaction right) {
        if (left.fee > right.fee) {
            return true;
        }
        if (left.fee == right.fee) {
            return left.timestamp.isAfter(right.timestamp);
        }
        return false;
    }

    public static class Transaction {
        public final String id;
        public final double fee;
        public final LocalTime timestamp;

        public Transaction(String id, double fee, LocalTime timestamp) {
            this.id = id;
            this.fee = fee;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return id + ":" + fee + "@" + timestamp;
        }
    }

    public static class BubbleSortResult {
        public final List<Transaction> sortedTransactions;
        public final int passes;
        public final int swaps;

        public BubbleSortResult(List<Transaction> sortedTransactions, int passes, int swaps) {
            this.sortedTransactions = sortedTransactions;
            this.passes = passes;
            this.swaps = swaps;
        }

        @Override
        public String toString() {
            return "BubbleSortResult{sortedTransactions=" + sortedTransactions
                    + ", passes=" + passes + ", swaps=" + swaps + "}";
        }
    }
}
