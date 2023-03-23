package com.gary.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gary.orderservice.dto.OrderLineItemsDto;
import com.gary.orderservice.dto.OrderRequest;
import com.gary.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

    @Autowired
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.0.30");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);

    }

    @Test
    void shouldCreateOrder() throws Exception{
        OrderRequest orderRequest = getOrderRequest();
        String requestStr = objectMapper.writeValueAsString(orderRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestStr))
                .andExpect(status().isCreated());
        Assertions.assertEquals(1, orderRepository.findAll().size());

    }

    private OrderRequest getOrderRequest() {
        List<OrderLineItemsDto> orderLineItemsDtos = new ArrayList<>();
        OrderLineItemsDto orderLineItemsDto = new OrderLineItemsDto(1001L,"iPhone 13",
                BigDecimal.valueOf(1200), 1);
        orderLineItemsDtos.add(orderLineItemsDto);
        OrderRequest or = new OrderRequest();
        or.setOrderLineItemsDtoList(orderLineItemsDtos);
        return or;

    }

}
