package JavaBasics;

/* NOT THREAD SAFE — StringBuilder

   BEHIND THE SCENES — the JDK source, same as StringBuffer but WITHOUT the lock:

     public StringBuilder append(String str) {   // <-- no 'synchronized'
         super.append(str);
         return this;
     }

   The parent's append() does three steps:
     ensureCapacity(count + len)  ->  write chars at index 'count'  ->  count += len

   'count += len' is a READ-MODIFY-WRITE. Both threads read count=5, both write
   their char at index 5, both store count=6. Two appends, one character kept. */

public class StringBuilderNotThreadSafety {
    public static void main(String[] args) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        Task t1 = new Task(sb);
        Task t2 = new Task(sb);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println(sb.length());
        // Rarely 2000. Different every run — lost updates.
        // Can also throw ArrayIndexOutOfBoundsException: one thread grows the
        // array and swaps it while the other is mid-write into the old one.
        // Run it 5-10 times to see the non-determinism.
    }
}

class Task extends Thread {
    private StringBuilder sb;          // both threads point at the SAME object

    public Task(StringBuilder sb) {
        this.sb = sb;
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
    }
}

/* FIXES, in order of preference
   1. Confinement  — each thread builds its own StringBuilder, join at the end (no lock at all)
   2. synchronized (sb) { sb.append("a"); }  — works, but you pay for a lock per call
   3. StringBuffer  — same thing, lock already built in

   Two bugs are at play, not one:
     atomicity  — count += len can interleave
     visibility — count is not volatile, so a thread may read a stale cached value
   synchronized fixes both; that is why it is the fix and not just "locking".
*/