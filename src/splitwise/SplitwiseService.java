package splitwise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

// SPLITWISE — expense splitting between friends/groups.
// Design: Strategy pattern for the 3 split types (equal/exact/percent), a
// netted per-pair balance ledger (amounts in cents), and a greedy
// debt-simplification algorithm to minimize settle-up transactions.

// A person who can pay for or owe part of an expense.
class User {
    private final String id;
    private final String name;

    public User(String id, String name) { this.id = id; this.name = name; }

    public String getId() { return id; }
    public String getName() { return name; }
}

// One participant's share of a single expense.
class ExpenseSplit {
    private final String userId;
    private final long amountCents;

    public ExpenseSplit(String userId, long amountCents) { this.userId = userId; this.amountCents = amountCents; }

    public String getUserId() { return userId; }
    public long getAmountCents() { return amountCents; }
}

// A single bill: who paid, how much, and how it's split among participants.
class Expense {
    private final String id;
    private final String description;
    private final String paidByUserId;
    private final long totalAmountCents;
    private final List<ExpenseSplit> splits;

    public Expense(String id, String description, String paidByUserId, long totalAmountCents, List<ExpenseSplit> splits) {
        this.id = id;
        this.description = description;
        this.paidByUserId = paidByUserId;
        this.totalAmountCents = totalAmountCents;
        this.splits = splits;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public String getPaidByUserId() { return paidByUserId; }
    public long getTotalAmountCents() { return totalAmountCents; }
    public List<ExpenseSplit> getSplits() { return splits; }
}

// One "who pays whom how much" step in a simplified settle-up plan.
class SettlementTransaction {
    private final String fromUserId;
    private final String toUserId;
    private final long amountCents;

    public SettlementTransaction(String fromUserId, String toUserId, long amountCents) {
        this.fromUserId = fromUserId; this.toUserId = toUserId; this.amountCents = amountCents;
    }

    public String getFromUserId() { return fromUserId; }
    public String getToUserId() { return toUserId; }
    public long getAmountCents() { return amountCents; }
}

// Strategy interface — every split type must turn a total into per-user shares.
interface SplitStrategy {
    List<ExpenseSplit> calculateSplits(long totalAmountCents, List<User> participants);
}

// Divide evenly. Integer division drops cents, so the leftover remainder is
// handed one cent at a time to the first few participants, keeping the sum exact.
class EqualSplitStrategy implements SplitStrategy {
    public List<ExpenseSplit> calculateSplits(long totalAmountCents, List<User> participants) {
        int n = participants.size();
        long baseShare = totalAmountCents / n;
        long remainder = totalAmountCents % n;

        List<ExpenseSplit> splits = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            long share = baseShare + (i < remainder ? 1 : 0);
            splits.add(new ExpenseSplit(participants.get(i).getId(), share));
        }
        return splits;
    }
}

// Caller supplies the exact amount each participant owes; must sum to the total.
class ExactSplitStrategy implements SplitStrategy {
    private final Map<String, Long> exactAmountsCentsByUserId;

    public ExactSplitStrategy(Map<String, Long> exactAmountsCentsByUserId) { this.exactAmountsCentsByUserId = exactAmountsCentsByUserId; }

    public List<ExpenseSplit> calculateSplits(long totalAmountCents, List<User> participants) {
        List<ExpenseSplit> splits = new ArrayList<>();
        long sum = 0;
        for (User user : participants) {
            Long amount = exactAmountsCentsByUserId.get(user.getId());
            if (amount == null) {
                throw new IllegalArgumentException("Missing exact amount for user " + user.getId());
            }
            sum += amount;
            splits.add(new ExpenseSplit(user.getId(), amount));
        }
        if (sum != totalAmountCents) {
            throw new IllegalArgumentException("Exact amounts (" + sum + ") don't add up to total (" + totalAmountCents + ")");
        }
        return splits;
    }
}

// Caller supplies each participant's percentage; must sum to 100. The last
// participant absorbs any rounding remainder so the split still sums exactly.
class PercentSplitStrategy implements SplitStrategy {
    private final Map<String, Double> percentagesByUserId;

    public PercentSplitStrategy(Map<String, Double> percentagesByUserId) { this.percentagesByUserId = percentagesByUserId; }

    public List<ExpenseSplit> calculateSplits(long totalAmountCents, List<User> participants) {
        double percentSum = 0;
        for (double p : percentagesByUserId.values()) {
            percentSum += p;
        }
        if (Math.abs(percentSum - 100.0) > 0.01) {
            throw new IllegalArgumentException("Percentages must sum to 100, got " + percentSum);
        }

        List<ExpenseSplit> splits = new ArrayList<>();
        long allocated = 0;
        for (int i = 0; i < participants.size(); i++) {
            User user = participants.get(i);
            Double percent = percentagesByUserId.get(user.getId());
            if (percent == null) {
                throw new IllegalArgumentException("Missing percentage for user " + user.getId());
            }

            long amount;
            if (i == participants.size() - 1) {
                amount = totalAmountCents - allocated; // last one absorbs the rounding remainder
            } else {
                amount = Math.round(totalAmountCents * (percent / 100.0));
                allocated += amount;
            }
            splits.add(new ExpenseSplit(user.getId(), amount));
        }
        return splits;
    }
}

// Orchestrator: owns users, expenses, and the balance ledger; the only public entry point.
public class SplitwiseService {
    private final Map<String, User> usersById;
    private final Map<String, Map<String, Long>> balances; // balances[A][B] = net cents A owes B (negative = B owes A)
    private final List<Expense> expenses;

    public SplitwiseService(List<User> users) {
        this.usersById = new HashMap<>();
        for (User user : users) {
            usersById.put(user.getId(), user);
        }
        this.balances = new HashMap<>();
        this.expenses = new ArrayList<>();
    }

    public Expense addExpense(String paidByUserId, String description, long totalAmountCents, List<String> participantUserIds, SplitStrategy splitStrategy) {
        if (!usersById.containsKey(paidByUserId)) {
            throw new NoSuchElementException("Unknown user: " + paidByUserId);
        }

        List<User> participants = new ArrayList<>();
        for (String userId : participantUserIds) {
            User user = usersById.get(userId);
            if (user == null) {
                throw new NoSuchElementException("Unknown user: " + userId);
            }
            participants.add(user);
        }

        List<ExpenseSplit> splits = splitStrategy.calculateSplits(totalAmountCents, participants);

        long splitSum = 0;
        for (ExpenseSplit split : splits) {
            splitSum += split.getAmountCents();
        }
        if (splitSum != totalAmountCents) {
            throw new IllegalStateException("Splits (" + splitSum + ") don't add up to total (" + totalAmountCents + ")");
        }

        for (ExpenseSplit split : splits) {
            if (!split.getUserId().equals(paidByUserId)) {
                adjustBalance(split.getUserId(), paidByUserId, split.getAmountCents());
            }
        }

        Expense expense = new Expense(UUID.randomUUID().toString(), description, paidByUserId, totalAmountCents, splits);
        expenses.add(expense);
        return expense;
    }

    // Positive = userA owes userB. Negative = userB owes userA.
    public long getBalance(String userA, String userB) {
        return balances.getOrDefault(userA, Map.of()).getOrDefault(userB, 0L);
    }

    // Records payer directly paying payee, reducing what payer owes payee.
    public void settle(String payerUserId, String payeeUserId, long amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Settlement amount must be positive");
        }
        adjustBalance(payerUserId, payeeUserId, -amountCents);
    }

    // Greedy "optimal account balancing": match the biggest overall debtor
    // against the biggest overall creditor, settle the smaller of the two,
    // repeat. This is the standard practical answer, though the mathematically
    // minimal transaction count is NP-hard to compute exactly for large groups.
    public List<SettlementTransaction> simplifyDebts() {
        Map<String, Long> netBalance = new HashMap<>();
        for (Map.Entry<String, Map<String, Long>> entry : balances.entrySet()) {
            long net = 0;
            for (long amount : entry.getValue().values()) {
                net += amount;
            }
            if (net != 0) {
                netBalance.put(entry.getKey(), net);
            }
        }

        List<Map.Entry<String, Long>> debtors = new ArrayList<>();   // net > 0: owes money overall
        List<Map.Entry<String, Long>> creditors = new ArrayList<>(); // net < 0: owed money overall
        for (Map.Entry<String, Long> entry : netBalance.entrySet()) {
            if (entry.getValue() > 0) {
                debtors.add(entry);
            } else if (entry.getValue() < 0) {
                creditors.add(entry);
            }
        }

        debtors.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));   // largest debt first
        creditors.sort((a, b) -> Long.compare(a.getValue(), b.getValue())); // most negative (owed most) first

        long[] debtorRemaining = new long[debtors.size()];
        for (int i = 0; i < debtors.size(); i++) {
            debtorRemaining[i] = debtors.get(i).getValue();
        }
        long[] creditorRemaining = new long[creditors.size()];
        for (int i = 0; i < creditors.size(); i++) {
            creditorRemaining[i] = -creditors.get(i).getValue();
        }

        List<SettlementTransaction> transactions = new ArrayList<>();
        int i = 0, j = 0;
        while (i < debtorRemaining.length && j < creditorRemaining.length) {
            long amount = Math.min(debtorRemaining[i], creditorRemaining[j]);
            transactions.add(new SettlementTransaction(debtors.get(i).getKey(), creditors.get(j).getKey(), amount));

            debtorRemaining[i] -= amount;
            creditorRemaining[j] -= amount;
            if (debtorRemaining[i] == 0) i++;
            if (creditorRemaining[j] == 0) j++;
        }

        return transactions;
    }

    // debtor owes creditor amountCents more (negative amountCents reduces the debt).
    // Kept mirrored so balances[A][B] always equals -balances[B][A].
    private void adjustBalance(String debtorId, String creditorId, long amountCents) {
        balances.computeIfAbsent(debtorId, k -> new HashMap<>()).merge(creditorId, amountCents, Long::sum);
        balances.computeIfAbsent(creditorId, k -> new HashMap<>()).merge(debtorId, -amountCents, Long::sum);
    }

    public static void main(String[] args) {
        User alice = new User("u1", "Alice");
        User bob = new User("u2", "Bob");
        User charlie = new User("u3", "Charlie");
        SplitwiseService splitwise = new SplitwiseService(List.of(alice, bob, charlie));

        // Equal split: Alice pays $100 dinner, split evenly 3 ways
        splitwise.addExpense("u1", "Dinner", 10000, List.of("u1", "u2", "u3"), new EqualSplitStrategy());

        // Exact split: Bob pays $60 groceries - Alice $20, Bob $10, Charlie $30
        Map<String, Long> exactAmounts = new HashMap<>();
        exactAmounts.put("u1", 2000L);
        exactAmounts.put("u2", 1000L);
        exactAmounts.put("u3", 3000L);
        splitwise.addExpense("u2", "Groceries", 6000, List.of("u1", "u2", "u3"), new ExactSplitStrategy(exactAmounts));

        // Percent split: Charlie pays $50 movie tickets - Alice 50%, Bob 25%, Charlie 25%
        Map<String, Double> percentages = new HashMap<>();
        percentages.put("u1", 50.0);
        percentages.put("u2", 25.0);
        percentages.put("u3", 25.0);
        splitwise.addExpense("u3", "Movie tickets", 5000, List.of("u1", "u2", "u3"), new PercentSplitStrategy(percentages));

        System.out.println("Bob owes Alice: " + splitwise.getBalance("u2", "u1") + " cents");
        System.out.println("Charlie owes Alice: " + splitwise.getBalance("u3", "u1") + " cents");
        System.out.println("Charlie owes Bob: " + splitwise.getBalance("u3", "u2") + " cents");

        splitwise.settle("u2", "u1", 2000); // Bob pays Alice $20 directly
        System.out.println("\nAfter Bob pays Alice $20 - Bob owes Alice: " + splitwise.getBalance("u2", "u1") + " cents");

        System.out.println("\nSimplified settlement plan:");
        for (SettlementTransaction t : splitwise.simplifyDebts()) {
            System.out.println(t.getFromUserId() + " pays " + t.getToUserId() + ": " + t.getAmountCents() + " cents");
        }

        try {
            Map<String, Long> badExact = new HashMap<>();
            badExact.put("u1", 1000L);
            badExact.put("u2", 1000L);
            badExact.put("u3", 1000L); // sums to 3000, not 5000
            splitwise.addExpense("u1", "Bad exact split", 5000, List.of("u1", "u2", "u3"), new ExactSplitStrategy(badExact));
        } catch (IllegalArgumentException e) {
            System.out.println("\nRejected bad exact split: " + e.getMessage());
        }

        try {
            Map<String, Double> badPercent = new HashMap<>();
            badPercent.put("u1", 50.0);
            badPercent.put("u2", 40.0);
            badPercent.put("u3", 5.0); // sums to 95, not 100
            splitwise.addExpense("u1", "Bad percent split", 5000, List.of("u1", "u2", "u3"), new PercentSplitStrategy(badPercent));
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected bad percent split: " + e.getMessage());
        }
    }
}
