package com.gary.orderservice.service;

import com.gary.orderservice.dto.OrderLineItemsDto;
import com.gary.orderservice.dto.OrderRequest;
import com.gary.orderservice.dto.OrderResponse;
import com.gary.orderservice.model.Order;
import com.gary.orderservice.model.OrderLineItems;
import com.gary.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        order.setOrderLineItemsList(orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()));
        orderRepository.save(order);
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
        return new OrderLineItemsDto(items.getId(), items.getSkucode(),
                items.getPrice(), items.getQuantity());

    }

    private OrderLineItems mapToDto(OrderLineItemsDto dto) {
        return new OrderLineItems(dto.getId(), dto.getSkucode(),
                dto.getPrice(), dto.getQuantity());

    }
}
