package splitwise.blueprint;

import java.util.List;
import java.util.Map;

// STRUCTURE ONLY — same classes, fields, and method signatures as
// splitwise.SplitwiseService, but every body is a stub. No business logic here.

class User {
    private final String id;
    private final String name;

    public User(String id, String name) { this.id = null; this.name = null; }

    public String getId() { return null; }
    public String getName() { return null; }
}

class ExpenseSplit {
    private final String userId;
    private final long amountCents;

    public ExpenseSplit(String userId, long amountCents) { this.userId = null; this.amountCents = 0; }

    public String getUserId() { return null; }
    public long getAmountCents() { return 0; }
}

class Expense {
    private final String id;
    private final String description;
    private final String paidByUserId;
    private final long totalAmountCents;
    private final List<ExpenseSplit> splits;

    public Expense(String id, String description, String paidByUserId, long totalAmountCents, List<ExpenseSplit> splits) {
        this.id = null;
        this.description = null;
        this.paidByUserId = null;
        this.totalAmountCents = 0;
        this.splits = null;
    }

    public String getId() { return null; }
    public String getDescription() { return null; }
    public String getPaidByUserId() { return null; }
    public long getTotalAmountCents() { return 0; }
    public List<ExpenseSplit> getSplits() { return null; }
}

class SettlementTransaction {
    private final String fromUserId;
    private final String toUserId;
    private final long amountCents;

    public SettlementTransaction(String fromUserId, String toUserId, long amountCents) {
        this.fromUserId = null;
        this.toUserId = null;
        this.amountCents = 0;
    }

    public String getFromUserId() { return null; }
    public String getToUserId() { return null; }
    public long getAmountCents() { return 0; }
}

interface SplitStrategy {
    List<ExpenseSplit> calculateSplits(long totalAmountCents, List<User> participants);
}

class EqualSplitStrategy implements SplitStrategy {
    public List<ExpenseSplit> calculateSplits(long totalAmountCents, List<User> participants) { return null; }
}

class ExactSplitStrategy implements SplitStrategy {
    private final Map<String, Long> exactAmountsCentsByUserId;

    public ExactSplitStrategy(Map<String, Long> exactAmountsCentsByUserId) { this.exactAmountsCentsByUserId = null; }

    public List<ExpenseSplit> calculateSplits(long totalAmountCents, List<User> participants) { return null; }
}

class PercentSplitStrategy implements SplitStrategy {
    private final Map<String, Double> percentagesByUserId;

    public PercentSplitStrategy(Map<String, Double> percentagesByUserId) { this.percentagesByUserId = null; }

    public List<ExpenseSplit> calculateSplits(long totalAmountCents, List<User> participants) { return null; }
}

public class SplitwiseService {
    private final Map<String, User> usersById;
    private final Map<String, Map<String, Long>> balances;
    private final List<Expense> expenses;

    public SplitwiseService(List<User> users) {
        this.usersById = null;
        this.balances = null;
        this.expenses = null;
    }

    public Expense addExpense(String paidByUserId, String description, long totalAmountCents, List<String> participantUserIds, SplitStrategy splitStrategy) { return null; }
    public long getBalance(String userA, String userB) { return 0; }
    public void settle(String payerUserId, String payeeUserId, long amountCents) { }
    public List<SettlementTransaction> simplifyDebts() { return null; }

    private void adjustBalance(String debtorId, String creditorId, long amountCents) { }
}
