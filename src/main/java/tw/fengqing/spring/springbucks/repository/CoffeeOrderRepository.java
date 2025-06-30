package tw.fengqing.spring.springbucks.repository;

import tw.fengqing.spring.springbucks.model.CoffeeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeOrderRepository extends JpaRepository<CoffeeOrder, Long> {
}
