package tw.fengqing.spring.springbucks.repository;

import tw.fengqing.spring.springbucks.model.Coffee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeRepository extends JpaRepository<Coffee, Long> {
}
