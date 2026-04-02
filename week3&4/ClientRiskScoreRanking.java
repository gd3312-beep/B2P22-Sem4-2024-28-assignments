package com.sem4.assignments.week3and4;

import java.util.ArrayList;
import java.util.List;

public class ClientRiskScoreRanking {
    public BubbleRiskResult bubbleSortAscending(List<Client> clients) {
        List<Client> list = new ArrayList<>(clients);
        int swaps = 0;

        for (int i = 0; i < list.size() - 1; i++) {
            boolean changed = false;
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (list.get(j).riskScore > list.get(j + 1).riskScore) {
                    Client temp = list.get(j);
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

        return new BubbleRiskResult(list, swaps);
    }

    public List<Client> insertionSortDescending(List<Client> clients) {
        List<Client> list = new ArrayList<>(clients);

        for (int i = 1; i < list.size(); i++) {
            Client current = list.get(i);
            int j = i - 1;

            while (j >= 0 && shouldComeAfter(list.get(j), current)) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, current);
        }

        return list;
    }

    public List<Client> getTopHighestRiskClients(List<Client> clients, int count) {
        List<Client> sorted = insertionSortDescending(clients);
        if (count >= sorted.size()) {
            return sorted;
        }
        return new ArrayList<>(sorted.subList(0, count));
    }

    private boolean shouldComeAfter(Client left, Client right) {
        if (left.riskScore < right.riskScore) {
            return true;
        }
        if (left.riskScore == right.riskScore) {
            return left.accountBalance > right.accountBalance;
        }
        return false;
    }

    public static class Client {
        public final String clientId;
        public final int riskScore;
        public final double accountBalance;

        public Client(String clientId, int riskScore, double accountBalance) {
            this.clientId = clientId;
            this.riskScore = riskScore;
            this.accountBalance = accountBalance;
        }

        @Override
        public String toString() {
            return clientId + "(" + riskScore + ")";
        }
    }

    public static class BubbleRiskResult {
        public final List<Client> sortedClients;
        public final int swaps;

        public BubbleRiskResult(List<Client> sortedClients, int swaps) {
            this.sortedClients = sortedClients;
            this.swaps = swaps;
        }

        @Override
        public String toString() {
            return "BubbleRiskResult{sortedClients=" + sortedClients + ", swaps=" + swaps + "}";
        }
    }
}
