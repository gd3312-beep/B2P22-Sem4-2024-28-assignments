package com.sem4.assignments.problem2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventoryManager {
    private final ConcurrentMap<String, ProductInventory> inventoryByProduct = new ConcurrentHashMap<>();

    public void addProduct(String productId, int initialStock) {
        inventoryByProduct.compute(productId, (id, inventory) -> {
            if (inventory == null) {
                return new ProductInventory(Math.max(initialStock, 0));
            }
            inventory.stock.addAndGet(Math.max(initialStock, 0));
            return inventory;
        });
    }

    public int checkStock(String productId) {
        ProductInventory inventory = inventoryByProduct.get(productId);
        return inventory == null ? 0 : Math.max(inventory.stock.get(), 0);
    }

    public PurchaseResult purchaseItem(String productId, long userId) {
        ProductInventory inventory = inventoryByProduct.get(productId);
        if (inventory == null) {
            return new PurchaseResult(PurchaseStatus.PRODUCT_NOT_FOUND, 0, -1, "Product not found");
        }

        while (true) {
            int currentStock = inventory.stock.get();
            if (currentStock <= 0) {
                inventory.waitingList.add(userId);
                int position = inventory.waitingList.size();
                return new PurchaseResult(PurchaseStatus.WAITLISTED, 0, position,
                        "Stock unavailable. Added to waiting list at position " + position);
            }

            if (inventory.stock.compareAndSet(currentStock, currentStock - 1)) {
                return new PurchaseResult(PurchaseStatus.SUCCESS, currentStock - 1, -1,
                        "Purchase successful");
            }
        }
    }

    public int restock(String productId, int quantity) {
        if (quantity <= 0) {
            return checkStock(productId);
        }

        ProductInventory inventory = inventoryByProduct.computeIfAbsent(productId, key -> new ProductInventory(0));
        return inventory.stock.addAndGet(quantity);
    }

    public List<Long> getWaitingListSnapshot(String productId) {
        ProductInventory inventory = inventoryByProduct.get(productId);
        if (inventory == null) {
            return List.of();
        }
        return new ArrayList<>(inventory.waitingList);
    }

    private static final class ProductInventory {
        private final AtomicInteger stock;
        private final ConcurrentLinkedQueue<Long> waitingList = new ConcurrentLinkedQueue<>();

        private ProductInventory(int stock) {
            this.stock = new AtomicInteger(stock);
        }
    }

    public enum PurchaseStatus {
        SUCCESS,
        WAITLISTED,
        PRODUCT_NOT_FOUND
    }

    public record PurchaseResult(PurchaseStatus status, int remainingStock, int waitingListPosition, String message) { }
}
