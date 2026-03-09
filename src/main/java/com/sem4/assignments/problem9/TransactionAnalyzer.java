package com.sem4.assignments.problem9;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionAnalyzer {
    public List<TransactionPair> findTwoSum(List<Transaction> transactions, long targetAmount) {
        Map<Long, List<Transaction>> amountToTransactions = new HashMap<>();
        List<TransactionPair> pairs = new ArrayList<>();

        for (Transaction transaction : transactions) {
            long complement = targetAmount - transaction.amount();
            List<Transaction> matches = amountToTransactions.getOrDefault(complement, Collections.emptyList());
            for (Transaction match : matches) {
                pairs.add(new TransactionPair(match.id(), transaction.id()));
            }

            amountToTransactions.computeIfAbsent(transaction.amount(), key -> new ArrayList<>()).add(transaction);
        }

        return pairs;
    }

    public List<TransactionPair> findTwoSumWithinWindow(
            List<Transaction> transactions,
            long targetAmount,
            Duration window
    ) {
        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort((a, b) -> a.timestamp().compareTo(b.timestamp()));

        ArrayDeque<Transaction> slidingWindow = new ArrayDeque<>();
        Map<Long, List<Transaction>> activeAmounts = new HashMap<>();
        List<TransactionPair> pairs = new ArrayList<>();

        for (Transaction current : sorted) {
            while (!slidingWindow.isEmpty()) {
                Transaction oldest = slidingWindow.peekFirst();
                if (Duration.between(oldest.timestamp(), current.timestamp()).compareTo(window) <= 0) {
                    break;
                }

                slidingWindow.pollFirst();
                List<Transaction> bucket = activeAmounts.get(oldest.amount());
                if (bucket != null) {
                    bucket.remove(oldest);
                    if (bucket.isEmpty()) {
                        activeAmounts.remove(oldest.amount());
                    }
                }
            }

            long complement = targetAmount - current.amount();
            for (Transaction match : activeAmounts.getOrDefault(complement, Collections.emptyList())) {
                pairs.add(new TransactionPair(match.id(), current.id()));
            }

            slidingWindow.addLast(current);
            activeAmounts.computeIfAbsent(current.amount(), key -> new ArrayList<>()).add(current);
        }

        return pairs;
    }

    public List<KSumMatch> findKSum(List<Transaction> transactions, int k, long targetAmount) {
        if (k < 2 || transactions.isEmpty()) {
            return List.of();
        }

        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort((a, b) -> Long.compare(a.amount(), b.amount()));

        List<KSumMatch> results = new ArrayList<>();
        backtrack(sorted, 0, k, targetAmount, new ArrayList<>(), results);
        return results;
    }

    public List<DuplicateAlert> detectDuplicates(List<Transaction> transactions) {
        Map<String, List<Transaction>> grouped = new HashMap<>();

        for (Transaction transaction : transactions) {
            String key = transaction.amount() + "|" + transaction.merchant().toLowerCase();
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(transaction);
        }

        List<DuplicateAlert> alerts = new ArrayList<>();
        for (List<Transaction> group : grouped.values()) {
            if (group.size() < 2) {
                continue;
            }

            Set<String> accountIds = new HashSet<>();
            List<Long> transactionIds = new ArrayList<>();
            for (Transaction transaction : group) {
                accountIds.add(transaction.accountId());
                transactionIds.add(transaction.id());
            }

            if (accountIds.size() > 1) {
                alerts.add(new DuplicateAlert(
                        group.getFirst().amount(),
                        group.getFirst().merchant(),
                        accountIds,
                        transactionIds));
            }
        }

        return alerts;
    }

    private void backtrack(
            List<Transaction> transactions,
            int start,
            int k,
            long target,
            List<Transaction> chosen,
            List<KSumMatch> results
    ) {
        if (k == 0 && target == 0) {
            List<Long> ids = chosen.stream().map(Transaction::id).toList();
            results.add(new KSumMatch(ids, chosen.stream().mapToLong(Transaction::amount).sum()));
            return;
        }

        if (k == 0 || start >= transactions.size()) {
            return;
        }

        for (int i = start; i < transactions.size(); i++) {
            Transaction current = transactions.get(i);
            chosen.add(current);
            backtrack(transactions, i + 1, k - 1, target - current.amount(), chosen, results);
            chosen.remove(chosen.size() - 1);
        }
    }

    public record Transaction(
            long id,
            long amount,
            String merchant,
            String accountId,
            Instant timestamp
    ) { }

    public record TransactionPair(long firstTransactionId, long secondTransactionId) { }

    public record KSumMatch(List<Long> transactionIds, long totalAmount) { }

    public record DuplicateAlert(long amount, String merchant, Set<String> accountIds, List<Long> transactionIds) { }
}
