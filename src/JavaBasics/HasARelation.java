package JavaBasics;

/* HAS-A RELATIONSHIP = composition (an object held as a FIELD)
   Say it out loud: "An Employee HAS-A Address" -> makes sense -> use a field
                    "An Employee IS-A Address"  -> nonsense    -> do NOT use extends */

class Address {
    String city = "Bengaluru";
}

class Employee {
    String name = "Satish";
    Address address = new Address();   // Employee HAS-A Address
}


public class HasARelation {

    public static void main(String[] args) {
        Employee e = new Employee();

        System.out.println(e.address.city);   // reached THROUGH the field — the dot-chain
        // is the signature of HAS-A. Employee does not
        // own 'city', it owns an object that owns it.

//      Address ad = e;                       // ✗ Employee is NOT an Address, it only holds one
//      System.out.println(e.city);           // ✗ no such field on Employee

        // ---- The payoff: swap the part at RUNTIME ----
        e.address = new Address();            // impossible with extends — a parent is fixed forever
        e.address.city = "Ranchi";
        System.out.println(e.address.city);   // Ranchi
    }
}

/* KEY POINTS
   - HAS-A = a field. IS-A = extends.
   - Dot-chain (e.address.city) = HAS-A.  Direct call (d.eat()) = IS-A.
   - No limit: a class can HAVE many objects, but can EXTEND only one class.
   - Swappable at runtime, especially if the field's type is an interface.
   - Rule of thumb: FAVOUR COMPOSITION OVER INHERITANCE.
     Inheritance exposes the parent's internals and locks you in at compile time;
     composition only uses the other object's public API.

   Two flavours worth naming:
     Composition — the part dies with the whole  (Employee creates its own Address)
     Aggregation — the part outlives the whole   (Employee passed a shared Department)
*/