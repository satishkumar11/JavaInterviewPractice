package JavaBasics;

/* ================================================================
   ABSTRACT CLASS
   PART 1 = the basics (your original code). Learn this first.
   PART 2 = everything else. Only read after Part 1 feels obvious.
   ================================================================ */


/* ################################################################
   PART 1 — THE BASICS
   ################################################################ */

abstract class Sound {

    void foo() {                       // concrete method: has a body, inherited as-is
        System.out.println();
    };

    abstract void horn();              // abstract method: NO body, ends in ';'
    // every concrete subclass MUST implement it
}

class Horn extends Sound {

    @Override
    void horn() {                      // the required implementation
        System.out.println("YOYO");
    }
}

/* Part 1 in three lines:
     - abstract class = cannot be instantiated. new Sound() is a compile error.
     - abstract method = no body, subclass is forced to provide one.
     - one abstract method anywhere forces the whole class to be abstract. */


/* ################################################################
   PART 2 — THE DETAILS
   ################################################################ */

abstract class Sound2 {

    // ---- STATE: abstract classes can hold instance fields. Interfaces cannot. ----
    protected String label;                  // any access modifier allowed
    private static int instanceCount = 0;    // one copy shared by all objects

    // ---- CONSTRUCTORS: an abstract class HAS them, they just run via super() ----
    Sound2() {
        this("unnamed");                     // this(...) = chaining to another constructor
    }

    Sound2(String label) {
        this.label = label;
        instanceCount++;
        // Runs whenever a subclass object is built — the subclass constructor
        // implicitly calls super() as its first statement.
    }

    abstract void horn();

    final void play() {
        // TEMPLATE METHOD — the real reason abstract classes exist.
        // Parent fixes the algorithm, subclass fills in only the varying step.
        System.out.println("-- start --");
        horn();                              // the hole the subclass plugs
        System.out.println("-- end --");
    }
    // 'final' => subclasses can call play() but cannot change the sequence.

    static int getInstanceCount() {          // static methods are allowed
        return instanceCount;
    }

    // ---- ILLEGAL COMBINATIONS ----
    // private abstract void a();   ✗ private can't be inherited, so can't be overridden
    // static  abstract void b();   ✗ static belongs to the type, overriding is runtime
    // final   abstract void c();   ✗ final forbids overriding, abstract demands it
}

abstract class Instrument extends Sound2 {
    // A subclass that leaves any abstract method unimplemented
    // must itself be abstract. It may also add new abstract methods.
    abstract void tune();
}

class Trumpet extends Instrument {            // first concrete class -> must implement BOTH
    @Override void horn() { System.out.println("PAAAP"); }
    @Override void tune() { System.out.println("tuning..."); }
}

abstract class Silence {
    // Zero abstract methods is legal. 'abstract' then means only: don't instantiate me.
    void mute() { System.out.println("..."); }
}


public class AbstractClass {

    public static void main(String[] args) {
        basics();
        advanced();
    }

    // ---------------- PART 1 ----------------
    static void basics() {
        Horn horn = new Horn();
        horn.horn();

//      Sound s0 = new Sound();               // ✗ abstract class cannot be instantiated
    }

    // ---------------- PART 2 ----------------
    static void advanced() {

        // Polymorphism: parent reference, child object
        Sound2 s = new Trumpet();
        s.horn();                             // dynamic dispatch — runs Trumpet's version
        s.play();                             // template method
        System.out.println(s.label);          // inherited state, impossible in an interface

        // Anonymous subclass — instantiating an abstract type indirectly
        Sound2 anon = new Sound2("adhoc") {
            @Override void horn() { System.out.println("anon horn"); }
        };
        anon.play();

        System.out.println("objects created: " + Sound2.getInstanceCount());
    }
}


/* ================================================================
   CHEAT SHEET

   abstract class                      | interface
   ------------------------------------|------------------------------------
   instance fields, mutable state      | constants only (public static final)
   constructors: yes                   | none
   any access modifier on members      | members implicitly public
   extend exactly ONE                  | implement MANY
   models "IS-A" / shared base         | models "CAN-DO" / capability

   Rules worth memorising
     - Cannot be instantiated, but CAN have a constructor (runs via super()).
     - A method with no body forces the class to be abstract.
     - A subclass leaving any abstract method unimplemented must be abstract.
     - abstract cannot combine with private, static, or final.
     - Zero abstract methods in an abstract class is legal.
     - Overriding may widen access, never narrow it.

   Which to choose
     Interfaces got default methods in Java 8, so behaviour is no longer the
     deciding factor. STATE and CONSTRUCTORS are — only classes have them.
   ================================================================ */