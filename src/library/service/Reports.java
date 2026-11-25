package library.service;

import library.data.LibraryStore;
import library.model.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/** Person 3: two separate patterns with different responsibilities. */
public final class Reports {
    private Reports() {}
    public enum Type { ACTIVE, OVERDUE, FINES }

    /** Template Method: fixed report workflow; subclasses supply the varying steps. */
    public abstract static class Report {
        protected final LibraryStore store;
        protected final LocalDate date;
        protected Report(LibraryStore store, LocalDate date) { this.store = store; this.date = date; }
        public final String generate() {
            List<Loan> rows = selectRows();
            String body = rows.isEmpty() ? "No records." : rows.stream().map(Loan::toString).collect(Collectors.joining("\n"));
            return "=== " + title() + " ===\nAs of: " + date + "\n" + body + "\n" + footer(rows);
        }
        protected abstract String title();
        protected abstract List<Loan> selectRows();
        protected String footer(List<Loan> rows) { return "Records: " + rows.size(); }
    }
    public static final class ActiveReport extends Report {
        public ActiveReport(LibraryStore store, LocalDate date) { super(store, date); }
        protected String title() { return "Active Loans"; }
        protected List<Loan> selectRows() { return store.loans().stream().filter(Loan::isActive).toList(); }
    }
    public static final class OverdueReport extends Report {
        public OverdueReport(LibraryStore store, LocalDate date) { super(store, date); }
        protected String title() { return "Overdue Loans"; }
        protected List<Loan> selectRows() { return store.loans().stream().filter(l -> l.isOverdue(date)).toList(); }
    }
    public static final class FineReport extends Report {
        public FineReport(LibraryStore store, LocalDate date) { super(store, date); }
        protected String title() { return "Recorded Late Fees"; }
        protected List<Loan> selectRows() { return store.loans().stream().filter(l -> !l.isActive() && l.getFine().signum() > 0).toList(); }
        protected String footer(List<Loan> rows) {
            BigDecimal total = rows.stream().map(Loan::getFine).reduce(new BigDecimal("0.00"), BigDecimal::add);
            return "Records: " + rows.size() + " | Total recorded fees: SAR " + total;
        }
    }

    /** Factory Method: creator subclasses decide which Report product to instantiate. */
    public abstract static class ReportCreator {
        protected abstract Report createReport(LibraryStore store, LocalDate date);
        public final String generate(LibraryStore store, LocalDate date) {
            return createReport(store, date).generate();
        }
    }
    public static final class ActiveReportCreator extends ReportCreator {
        protected Report createReport(LibraryStore store, LocalDate date) { return new ActiveReport(store, date); }
    }
    public static final class OverdueReportCreator extends ReportCreator {
        protected Report createReport(LibraryStore store, LocalDate date) { return new OverdueReport(store, date); }
    }
    public static final class FineReportCreator extends ReportCreator {
        protected Report createReport(LibraryStore store, LocalDate date) { return new FineReport(store, date); }
    }
    public static ReportCreator creator(Type type) {
        if (type == null) throw new IllegalArgumentException("Report type is required.");
        return switch (type) {
            case ACTIVE -> new ActiveReportCreator();
            case OVERDUE -> new OverdueReportCreator();
            case FINES -> new FineReportCreator();
        };
    }
}
