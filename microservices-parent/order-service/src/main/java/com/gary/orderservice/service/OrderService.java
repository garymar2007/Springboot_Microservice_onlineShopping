package com.gary.orderservice.service;

import com.gary.orderservice.dto.InventoryResponse;
import com.gary.orderservice.dto.OrderLineItemsDto;
import com.gary.orderservice.dto.OrderRequest;
import com.gary.orderservice.dto.OrderResponse;
import com.gary.orderservice.event.OrderPlacedEvent;
import com.gary.orderservice.model.Order;
import com.gary.orderservice.model.OrderLineItems;
import com.gary.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    private final Tracer tracer;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        order.setOrderLineItemsList(orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()));

        List<String> skuCodeList = order.getOrderLineItemsList()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        log.info("Calling inventory service");

        Span inventoryServiceLookup = tracer.nextSpan().name("InverntoryServiceLookup");
        try(Tracer.SpanInScope spanInSceope = tracer.withSpan(inventoryServiceLookup.start())){
            //Invoke Inventory service to see whether product is in stock - block(): synchronous way
            //Using eureka service discovery plus loadbalancer to invoke multiple instances of inventory-service.
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodeList).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            boolean result = Arrays.stream(inventoryResponseArray)
                    .allMatch(InventoryResponse::isInStock);
            if(result) {
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
                return "Order Placed Successfully";
            } else {
                throw new IllegalArgumentException("Product is out of stock, please try again later");
            }
        }finally {
            inventoryServiceLookup.end();
        }
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orderList = orderRepository.findAll();
        List<OrderResponse> orderResponses = new ArrayList<>();
        for(Order o : orderList) {
            OrderResponse or = new OrderResponse();
            or.setId(o.getId());
            or.setOrderNumber(o.getOrderNumber());
            or.setOrderLineItemsDtoList(o.getOrderLineItemsList()
                    .stream()
                    .map(this::dtoToMap)
                    .collect(Collectors.toList()));
            orderResponses.add(or);
        }

        return orderResponses;
    }

    private OrderLineItemsDto dtoToMap(OrderLineItems items) {
        return new OrderLineItemsDto(items.getId(), items.getSkuCode(),
                items.getPrice(), items.getQuantity());

    }

    private OrderLineItems mapToDto(OrderLineItemsDto dto) {
        return new OrderLineItems(dto.getId(), dto.getSkuCode(),
                dto.getPrice(), dto.getQuantity());

    }
}
