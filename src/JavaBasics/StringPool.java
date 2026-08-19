package JavaBasics;

/* STRING POOL — REVISION
   == asks "same object?"   .equals() asks "same characters?"   Always use .equals() */

public class StringPool {

    public static void main(String[] args) {

        // 1. Literals are POOLED — only one "Hello" object exists
        String a = "Hello";
        String b = "Hello";
        System.out.println(a == b);           // true  — same pooled object
        System.out.println(a.equals(b));      // true

        // 2. 'new' always BYPASSES the pool — fresh object every time
        String c = new String("Hello");
        System.out.println(a == c);           // false
        System.out.println(a.equals(c));      // true — content still matches

        // 3. intern() returns the pooled copy (must be reassigned)
        c = c.intern();
        System.out.println(a == c);           // true

        // 4. THE TRAP: compile-time vs runtime concatenation
        String d = "Hel" + "lo";
        System.out.println(a == d);           // true  — both literals, compiler folds it

        String part = "Hel";
        System.out.println(a == part + "lo"); // false — variable => built at runtime, not pooled

        final String fixed = "Hel";
        System.out.println(a == fixed + "lo");// true  — final + literal = compile-time constant

        // 5. String is IMMUTABLE — methods return a new object, never edit
        String s = "Sita";
        s.concat("pur");
        System.out.println(s);                // "Sita" — unchanged
    }
}

/* CHEAT SHEET
   Pooled:      "literal",  "Hel"+"lo",  final p="Hel"; p+"lo",  x.intern()
   Not pooled:  new String("x"),  variable+"lo",  sb.toString()

   Why immutable: thread-safe sharing, stable hashCode for HashMap keys,
                  makes the pool possible, validated paths/URLs can't be altered.

   Building in a loop: use StringBuilder. String += in a loop is O(n^2).
   StringBuffer = synchronized StringBuilder; rarely needed.
*/