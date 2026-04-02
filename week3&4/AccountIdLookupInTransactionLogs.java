package com.sem4.assignments.week3and4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountIdLookupInTransactionLogs {
    public LinearSearchResult linearSearchFirstAndLast(List<String> accountIds, String target) {
        int first = -1;
        int last = -1;
        int comparisons = 0;

        for (int i = 0; i < accountIds.size(); i++) {
            comparisons++;
            if (accountIds.get(i).equals(target)) {
                if (first == -1) {
                    first = i;
                }
                last = i;
            }
        }

        return new LinearSearchResult(first, last, comparisons);
    }

    public BinarySearchResult binarySearchWithCount(List<String> accountIds, String target) {
        List<String> sorted = new ArrayList<>(accountIds);
        Collections.sort(sorted);

        Counter counter = new Counter();
        int index = binarySearch(sorted, target, counter);
        int count = 0;

        if (index != -1) {
            int left = index;
            int right = index;

            while (left - 1 >= 0 && sorted.get(left - 1).equals(target)) {
                left--;
            }
            while (right + 1 < sorted.size() && sorted.get(right + 1).equals(target)) {
                right++;
            }
            count = right - left + 1;
        }

        return new BinarySearchResult(sorted, index, count, counter.value);
    }

    private int binarySearch(List<String> list, String target, Counter counter) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            counter.value++;
            int result = list.get(mid).compareTo(target);

            if (result == 0) {
                return mid;
            } else if (result < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return -1;
    }

    private static class Counter {
        int value;
    }

    public static class LinearSearchResult {
        public final int firstIndex;
        public final int lastIndex;
        public final int comparisons;

        public LinearSearchResult(int firstIndex, int lastIndex, int comparisons) {
            this.firstIndex = firstIndex;
            this.lastIndex = lastIndex;
            this.comparisons = comparisons;
        }

        @Override
        public String toString() {
            return "LinearSearchResult{firstIndex=" + firstIndex
                    + ", lastIndex=" + lastIndex + ", comparisons=" + comparisons + "}";
        }
    }

    public static class BinarySearchResult {
        public final List<String> sortedAccountIds;
        public final int foundIndex;
        public final int count;
        public final int comparisons;

        public BinarySearchResult(List<String> sortedAccountIds, int foundIndex, int count, int comparisons) {
            this.sortedAccountIds = sortedAccountIds;
            this.foundIndex = foundIndex;
            this.count = count;
            this.comparisons = comparisons;
        }

        @Override
        public String toString() {
            return "BinarySearchResult{sortedAccountIds=" + sortedAccountIds + ", foundIndex=" + foundIndex
                    + ", count=" + count + ", comparisons=" + comparisons + "}";
        }
    }
}
