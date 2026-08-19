package JavaBasics;

/* ================================================================
   INTERFACE
   PART 1 = the basics (your original code). Learn this first.
   PART 2 = everything else. Only read after Part 1 feels obvious.
   ================================================================ */


/* ################################################################
   PART 1 — THE BASICS
   ################################################################ */

interface Payment {

    int base = 100;                    // implicitly public static final => a CONSTANT
    // read as Payment.base; cannot be reassigned

    void calculateFee();               // implicitly public abstract => no body
    // every implementing class MUST provide it

    default void print() {             // Java 8: a method WITH a body, inherited by implementors
        System.out.println("Hello World");
    }
}

class CardPayment implements Payment {

    int charges = 10;                  // instance field: one copy PER OBJECT (unlike static base)
    int min = 10;
    int max = 20;

    public void calculateFee() {       // 'public' is MANDATORY — interface methods are public,
        System.out.println((int) (Math.random() * (max - min + 1)) + min);
    }                                  // and overriding can widen access, never narrow it
}

/* Part 1 in four lines:
     - interface = a contract. It says WHAT, not HOW.
     - fields are always public static final (constants), never state.
     - methods are always public abstract, unless marked default/static/private.
     - implementations must be declared public. */


/* ################################################################
   PART 2 — THE DETAILS
   ################################################################ */

interface Payment2 {

    int base = 100;

    void calculateFee();

    default void print() {
        System.out.println("Hello World");
        log("print() was called");           // default methods may call private helpers
    }
    // Why default exists: lets you add a method to an already-published interface
    // without breaking every existing implementor. Real case: Collection.stream().

    private void log(String msg) {           // Java 9+: private interface method
        System.out.println("[LOG] " + msg);  // shared helper, hidden from implementors
    }

    static Payment2 noOp() {                 // Java 8: static interface method (utility/factory)
        return () -> System.out.println("no fee");
    }
    // NOT inherited. Call Payment2.noOp(), never cardPayment.noOp().
}

interface Refundable {
    default void print() {                   // same signature as Payment2.print() -> diamond problem
        System.out.println("Refundable print");
    }
}

interface Auditable { }
// Marker interface: no members, exists only to tag a type (Serializable, Cloneable).

class UpiPayment implements Payment2, Refundable, Auditable {
    // A class implements MANY interfaces (Java's answer to multiple inheritance)
    // but extends only ONE class.

    public void calculateFee() {
        System.out.println("UPI fee: 0");
    }

    public void print() {
        // DIAMOND PROBLEM: both parents supply a default print(),
        // so the compiler FORCES you to override and choose.
        Payment2.super.print();              // syntax to reach a specific parent's default
        Refundable.super.print();
    }
}


public class InterfaceDemo {                 // only ONE public top-level type per file,
    // and its name must match the filename
    public static void main(String[] args) {
        basics();
        advanced();
    }

    // ---------------- PART 1 ----------------
    static void basics() {
        CardPayment cardPayment = new CardPayment();
        cardPayment.calculateFee();
        cardPayment.print();                 // inherited default method

//      cardPayment.base = 20;               // ✗ cannot assign — base is final
        System.out.println(Payment.base);    // ✓ reading a constant is fine
    }

    // ---------------- PART 2 ----------------
    static void advanced() {

        Payment2.noOp().calculateFee();      // static interface method — via interface name only

        // Polymorphism: the REFERENCE type decides what compiles
        Payment p = new CardPayment();
        p.calculateFee();                    // runs CardPayment's version (dynamic dispatch)
//      System.out.println(p.charges);       // ✗ Payment declares no 'charges'

        new UpiPayment().print();            // diamond resolved explicitly

        // Lambda: legal because Payment2 has exactly ONE abstract method
        Payment2 lambda = () -> System.out.println("lambda fee");
        lambda.calculateFee();
        lambda.print();                      // default methods still available on a lambda

        // Anonymous class: how the same thing was done before Java 8
        Payment2 anon = new Payment2() {
            public void calculateFee() {
                System.out.println("anon fee");
            }
        };
        anon.calculateFee();
    }
}


/* ================================================================
   CHEAT SHEET

   Implicit modifiers inside an interface
     field   -> public static final     (constant, never state)
     method  -> public abstract         (unless default / static / private)

   Method kinds
     abstract  no body, implementor must provide it
     default   Java 8, has a body, inherited, overridable
     static    Java 8, has a body, NOT inherited, call via InterfaceName
     private   Java 9, has a body, helper for default/static methods only

   Interface vs abstract class
     state          none (only constants)   | instance fields allowed
     constructor    none                    | yes
     multiple       implement many          | extend only one
     methods        public by default       | any access modifier
     use it for     a capability / contract | shared partial implementation

   Rules worth memorising
     - Overriding may widen access, never narrow it.
     - Two default methods with the same signature => you MUST override; use X.super.m().
     - Exactly one abstract method => functional interface => lambda-compatible.
     - Static interface methods are not inherited by implementing classes.
   ================================================================ */