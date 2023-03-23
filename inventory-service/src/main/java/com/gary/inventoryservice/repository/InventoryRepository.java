package com.gary.inventoryservice.repository;

import com.gary.inventoryservice.inventory.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<ProductInventory, Long> {
    Optional<ProductInventory> findBySkuCode(String skuCode);
}
