package com.sem4.assignments.problem5;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class RealTimeAnalyticsDashboard {
    private final ConcurrentMap<String, LongAdder> pageViews = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> uniqueVisitorsByPage = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> sourceCounts = new ConcurrentHashMap<>();
    private final LongAdder totalEvents = new LongAdder();

    public void processEvent(PageViewEvent event) {
        if (event == null || event.url() == null || event.userId() == null || event.source() == null) {
            return;
        }

        pageViews.computeIfAbsent(event.url(), key -> new LongAdder()).increment();
        uniqueVisitorsByPage.computeIfAbsent(event.url(), key -> ConcurrentHashMap.newKeySet()).add(event.userId());
        sourceCounts.computeIfAbsent(event.source().toLowerCase(), key -> new LongAdder()).increment();
        totalEvents.increment();
    }

    public DashboardSnapshot getDashboardSnapshot() {
        Map<String, Long> pageViewSnapshot = pageViews.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().sum()));

        List<TopPage> topPages = pageViewSnapshot.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new TopPage(
                        entry.getKey(),
                        entry.getValue(),
                        uniqueVisitorsByPage.getOrDefault(entry.getKey(), Set.of()).size()))
                .collect(Collectors.toList());

        long totalSourceVisits = sourceCounts.values().stream().mapToLong(LongAdder::sum).sum();
        List<SourceStat> sourceStats = new ArrayList<>();

        for (Map.Entry<String, LongAdder> sourceEntry : sourceCounts.entrySet()) {
            long count = sourceEntry.getValue().sum();
            double share = totalSourceVisits == 0 ? 0.0 : (count * 100.0) / totalSourceVisits;
            sourceStats.add(new SourceStat(sourceEntry.getKey(), count, share));
        }

        sourceStats.sort(Comparator.comparingDouble(SourceStat::percentage).reversed());

        return new DashboardSnapshot(Instant.now(), totalEvents.sum(), topPages, sourceStats);
    }

    public record PageViewEvent(String url, String userId, String source) { }

    public record TopPage(String url, long views, int uniqueVisitors) { }

    public record SourceStat(String source, long visits, double percentage) { }

    public record DashboardSnapshot(
            Instant generatedAt,
            long totalEvents,
            List<TopPage> topPages,
            List<SourceStat> sourceStats
    ) { }
}
