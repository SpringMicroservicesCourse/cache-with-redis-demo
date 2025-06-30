package tw.fengqing.spring.springbucks.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tw.fengqing.spring.springbucks.model.Coffee;
import tw.fengqing.spring.springbucks.model.CoffeeOrder;
import tw.fengqing.spring.springbucks.model.OrderState;
import tw.fengqing.spring.springbucks.repository.CoffeeOrderRepository;

import jakarta.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
@Transactional
public class CoffeeOrderService {
    @Autowired
    private CoffeeOrderRepository orderRepository;

    public CoffeeOrder createOrder(String customer, Coffee...coffee) {
        CoffeeOrder order = CoffeeOrder.builder()
                .customer(customer)
                .items(List.of(coffee))
                .state(OrderState.INIT)
                .build();
        CoffeeOrder saved = orderRepository.save(order);
        log.info("New Order: {}", saved);
        return saved;
    }

    public boolean updateState(CoffeeOrder order, OrderState state) {
        if (order.getState() != state) {
            log.info("Order {} , {}", order.getState(), state);
            order.setState(state);
            orderRepository.save(order);
            log.info("Updated Order: {}", order);
            return true;
        } else {
            log.warn("Order {} is already in state {}", order, state);
            return false;
        }
    }
}
