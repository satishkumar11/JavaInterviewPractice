package JavaBasics;

/* IS-A RELATIONSHIP = inheritance (extends)
   Say it out loud: "A Dog IS-A Animal" -> makes sense -> use extends */

class Animal {
    void eat() {
        System.out.println("eating");
    }
}

class Dog extends Animal {      // Dog IS-A Animal — inherits everything Animal has
    void bark() {
        System.out.println("Bhow");
    }   // Dog's own extra behaviour
}


public class IsARelation {
    public static void main(String[] args) {
        Dog d = new Dog();
        d.eat();                // inherited — no dot-chain needed, Dog OWNS this method
        d.bark();               // Dog's own method

        // ---- UPCASTING: parent reference holding a child object ----
        Animal a = new Dog();   // legal only because Dog IS-A Animal
        a.eat();
//      a.bark();               // ✗ won't compile — the REFERENCE type is Animal,
        //   and Animal has no bark(). Compiler checks the
        //   reference, not the actual object.

        // ---- instanceof: proves the IS-A link at runtime ----
        System.out.println(d instanceof Animal);   // true
        System.out.println(a instanceof Dog);      // true — object is still a Dog

        // ---- DOWNCASTING: going back to the child type ----
        Dog back = (Dog) a;     // explicit cast needed; ClassCastException if 'a' weren't a Dog
        back.bark();
    }
}

/* KEY POINTS
   - extends = IS-A. One parent only (no multiple inheritance of classes).
   - Upcasting is automatic; downcasting needs an explicit cast.
   - Reference type decides what COMPILES; object type decides what RUNS.
   - Test before using extends: "X IS-A Y" — if it sounds wrong, use HAS-A (a field) instead.
*/