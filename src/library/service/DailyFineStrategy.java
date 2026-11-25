package library.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DailyFineStrategy implements FineCalculationStrategy {
    private final BigDecimal rate;
    public DailyFineStrategy(BigDecimal rate) {
        if (rate == null || rate.signum() < 0) throw new IllegalArgumentException("Rate cannot be negative.");
        this.rate = rate;
    }
    @Override public BigDecimal calculate(LocalDate dueDate, LocalDate returnDate) {
        long days = Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
        return rate.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
    }
}
