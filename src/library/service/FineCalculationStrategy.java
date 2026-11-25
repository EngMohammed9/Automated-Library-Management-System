package library.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Person 2: Strategy pattern contract. */
public interface FineCalculationStrategy {
    BigDecimal calculate(LocalDate dueDate, LocalDate returnDate);
}
