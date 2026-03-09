package com.sem4.assignments;

import com.sem4.assignments.problem1.UsernameAvailabilityChecker;
import com.sem4.assignments.problem10.MultiLevelCacheSystem;
import com.sem4.assignments.problem2.FlashSaleInventoryManager;
import com.sem4.assignments.problem3.DNSCacheWithTTL;
import com.sem4.assignments.problem4.PlagiarismDetector;
import com.sem4.assignments.problem5.RealTimeAnalyticsDashboard;
import com.sem4.assignments.problem6.DistributedRateLimiter;
import com.sem4.assignments.problem7.AutocompleteSystem;
import com.sem4.assignments.problem8.ParkingLotManager;
import com.sem4.assignments.problem9.TransactionAnalyzer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class AssignmentRunner {
    public static void main(String[] args) {
        runProblem1();
        runProblem2();
        runProblem3();
        runProblem4();
        runProblem5();
        runProblem6();
        runProblem7();
        runProblem8();
        runProblem9();
        runProblem10();
    }

    private static void runProblem1() {
        UsernameAvailabilityChecker checker = new UsernameAvailabilityChecker();
        checker.registerUsername("john_doe", 1L);
        checker.checkAvailability("john_doe");
        checker.checkAvailability("john_doe");
        checker.checkAvailability("jane_smith");
        System.out.println("P1 available(jane_smith): " + checker.checkAvailability("jane_smith"));
        System.out.println("P1 suggestions(john_doe): " + checker.suggestAlternatives("john_doe", 3));
        System.out.println("P1 most attempted: " + checker.getMostAttempted().orElse(null));
    }

    private static void runProblem2() {
        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();
        manager.addProduct("IPHONE15_256GB", 2);
        System.out.println("P2 stock: " + manager.checkStock("IPHONE15_256GB"));
        System.out.println("P2 purchase #1: " + manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println("P2 purchase #2: " + manager.purchaseItem("IPHONE15_256GB", 67890));
        System.out.println("P2 purchase #3: " + manager.purchaseItem("IPHONE15_256GB", 99999));
    }

    private static void runProblem3() {
        DNSCacheWithTTL.UpstreamDnsResolver resolver = domain -> new DNSCacheWithTTL.ResolvedAddress("172.217.14.206", 5);
        try (DNSCacheWithTTL cache = new DNSCacheWithTTL(100, resolver)) {
            System.out.println("P3 first resolve: " + cache.resolve("google.com"));
            System.out.println("P3 second resolve: " + cache.resolve("google.com"));
            System.out.println("P3 stats: " + cache.getCacheStats());
        }
    }

    private static void runProblem4() {
        PlagiarismDetector detector = new PlagiarismDetector(5);
        detector.indexDocument("essay_089.txt", "hash tables are useful for constant time lookups in many systems");
        detector.indexDocument("essay_092.txt", "hash tables are useful for constant time lookups in many systems and analytics pipelines");
        PlagiarismDetector.AnalysisReport report = detector.analyzeDocument("essay_123.txt",
                "hash tables are useful for constant time lookups in many systems and applications");
        System.out.println("P4 report: " + report);
    }

    private static void runProblem5() {
        RealTimeAnalyticsDashboard dashboard = new RealTimeAnalyticsDashboard();
        dashboard.processEvent(new RealTimeAnalyticsDashboard.PageViewEvent("/article/breaking-news", "user_123", "google"));
        dashboard.processEvent(new RealTimeAnalyticsDashboard.PageViewEvent("/article/breaking-news", "user_456", "facebook"));
        dashboard.processEvent(new RealTimeAnalyticsDashboard.PageViewEvent("/sports/championship", "user_789", "direct"));
        System.out.println("P5 dashboard: " + dashboard.getDashboardSnapshot());
    }

    private static void runProblem6() {
        DistributedRateLimiter limiter = new DistributedRateLimiter(3);
        System.out.println("P6 check #1: " + limiter.checkRateLimit("abc123"));
        System.out.println("P6 check #2: " + limiter.checkRateLimit("abc123"));
        System.out.println("P6 check #3: " + limiter.checkRateLimit("abc123"));
        System.out.println("P6 check #4: " + limiter.checkRateLimit("abc123"));
        System.out.println("P6 status: " + limiter.getRateLimitStatus("abc123"));
    }

    private static void runProblem7() {
        AutocompleteSystem autocomplete = new AutocompleteSystem();
        autocomplete.updateFrequency("java tutorial", 5);
        autocomplete.updateFrequency("javascript", 3);
        autocomplete.updateFrequency("java download", 4);
        System.out.println("P7 search(jav): " + autocomplete.search("jav", 3));
        System.out.println("P7 search(jvaa typo): " + autocomplete.search("jvaa", 3));
    }

    private static void runProblem8() {
        ParkingLotManager parkingLot = new ParkingLotManager(10, 5.0);
        System.out.println("P8 park 1: " + parkingLot.parkVehicle("ABC-1234"));
        System.out.println("P8 park 2: " + parkingLot.parkVehicle("ABC-1235"));
        System.out.println("P8 nearest spot from 0: " + parkingLot.findNearestAvailableSpot(0));
        System.out.println("P8 exit: " + parkingLot.exitVehicle("ABC-1234"));
        System.out.println("P8 stats: " + parkingLot.getStatistics());
    }

    private static void runProblem9() {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();
        List<TransactionAnalyzer.Transaction> transactions = List.of(
                new TransactionAnalyzer.Transaction(1, 500, "Store A", "acc1", Instant.parse("2026-03-09T10:00:00Z")),
                new TransactionAnalyzer.Transaction(2, 300, "Store B", "acc2", Instant.parse("2026-03-09T10:15:00Z")),
                new TransactionAnalyzer.Transaction(3, 200, "Store C", "acc3", Instant.parse("2026-03-09T10:30:00Z")),
                new TransactionAnalyzer.Transaction(4, 500, "Store A", "acc4", Instant.parse("2026-03-09T10:45:00Z"))
        );
        System.out.println("P9 two-sum: " + analyzer.findTwoSum(transactions, 500));
        System.out.println("P9 two-sum window: " + analyzer.findTwoSumWithinWindow(transactions, 500, Duration.ofHours(1)));
        System.out.println("P9 k-sum: " + analyzer.findKSum(transactions, 3, 1000));
        System.out.println("P9 duplicates: " + analyzer.detectDuplicates(transactions));
    }

    private static void runProblem10() {
        MultiLevelCacheSystem cache = new MultiLevelCacheSystem(2, 3, 2);
        cache.putInDatabase(new MultiLevelCacheSystem.VideoData("video_123", "Intro to Hash Tables", "payload", System.currentTimeMillis()));
        cache.putInDatabase(new MultiLevelCacheSystem.VideoData("video_999", "Cache Systems", "payload", System.currentTimeMillis()));

        System.out.println("P10 get video_123 #1: " + cache.getVideo("video_123"));
        System.out.println("P10 get video_123 #2: " + cache.getVideo("video_123"));
        System.out.println("P10 get video_999: " + cache.getVideo("video_999"));
        System.out.println("P10 stats: " + cache.getStatistics());
    }
}
