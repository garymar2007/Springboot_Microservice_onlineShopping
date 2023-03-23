package com.gary.inventoryservice.repository;

import com.gary.inventoryservice.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<ProductInventory, Long> {
    List<ProductInventory> findBySkuCodeIn(List<String> skuCode);
}
