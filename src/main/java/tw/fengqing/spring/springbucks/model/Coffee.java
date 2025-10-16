package tw.fengqing.spring.springbucks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.joda.money.Money;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;

@Entity
@Table(name = "T_COFFEE")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Coffee extends BaseEntity {
    private String name;
    @Convert(converter = MoneyConverter.class)
    private Money price;
}
