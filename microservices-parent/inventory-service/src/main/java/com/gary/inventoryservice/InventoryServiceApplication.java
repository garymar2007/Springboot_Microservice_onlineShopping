package com.gary.inventoryservice;

import com.gary.inventoryservice.inventory.ProductInventory;
import com.gary.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
        return args -> {
            ProductInventory pi = new ProductInventory();
            pi.setSkuCode("iphone_13");
            pi.setQuantity(100);

            ProductInventory pi1 = new ProductInventory();
            pi1.setSkuCode("iphone_13_red");
            pi1.setQuantity(0);

            inventoryRepository.save(pi);
            inventoryRepository.save(pi1);
        };

    }
}
