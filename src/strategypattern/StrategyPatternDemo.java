package strategypattern;

// STRATEGY PATTERN — Swap out *how* something is done at runtime
// (e.g., pay by card, UPI, or cash — same checkout, different method).

// 1. Strategy interface — a contract that every payment method must follow
interface PaymentStrategy {
    void pay(int amount);
}

// 2. Concrete strategies — each is a different way to "pay"
class CreditCardPayment implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using Credit Card.");
    }
}

class UpiPayment implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using UPI.");
    }
}

class CashPayment implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using Cash.");
    }
}

// 3. Context — uses a strategy, but doesn't know/care which one
class ShoppingCart {
    private PaymentStrategy paymentStrategy;

    // The strategy is passed in from outside — this is called "dependency injection"
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout(int amount) {
        paymentStrategy.pay(amount); // delegates the actual work to whichever strategy was set
    }
}

// Main class to test it
public class StrategyPatternDemo {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();

        cart.setPaymentStrategy(new CreditCardPayment());
        cart.checkout(500); // Paid 500 using Credit Card.

        cart.setPaymentStrategy(new UpiPayment());
        cart.checkout(200); // Paid 200 using UPI.

        cart.setPaymentStrategy(new CashPayment());
        cart.checkout(100); // Paid 100 using Cash.
    }
}
