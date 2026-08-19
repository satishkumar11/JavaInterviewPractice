package JavaBasics;

/* THREAD SAFETY — StringBuffer

   BEHIND THE SCENES — the actual JDK source of the method you are calling:

     public synchronized StringBuffer append(String str) {  // one thread at a time
         toStringCache = null;                              // saved text is now stale — discard
         super.append(str);                                 // parent does the real work
         return this;                                       // hand back the same object, for chaining
     }

   synchronized   locks the monitor of THIS object. Both threads share one sb,
                  so they queue for one lock. Compiles to monitorenter/monitorexit.
   toStringCache  StringBuffer caches the last toString() result. The contents just
                  changed, so the cache is now wrong and must be cleared.
   super.append   StringBuffer holds no logic. The parent, AbstractStringBuilder,
                  owns the char[] and does ensureCapacity -> copy chars -> count += len.
   return this    returns the SAME object, which is what makes chaining work:
                  sb.append("a").append("b")

   StringBuilder extends the same parent with the same wrapper, minus 'synchronized'.
   That one keyword is the entire difference between the two classes. */

public class StringBufferThreadSafety {

    public static void main(String[] args) throws InterruptedException {
        StringBuffer sb = new StringBuffer();

        Thread1 t1 = new Thread1(sb);
        Thread1 t2 = new Thread1(sb);

        t1.start();                       // start() = run in a NEW thread (run() would just call it here)
        t2.start();

        t1.join();                        // wait for the thread to finish; without this we'd print early
        t2.join();

        System.out.println(sb.length());  // always 2000

        // ---- LIMIT: each call is atomic, a SEQUENCE of calls is not ----
        // if (sb.length() < 2500) sb.append("x");   // race: lock released between the two calls
        synchronized (sb) {                          // hold the same lock across both
            if (sb.length() < 2500) sb.append("x");
        }
        System.out.println(sb.length());             // 2001
    }
}


class Thread1 extends Thread {
    StringBuffer sb;                     // shared reference — both threads point at the SAME object

    public Thread1(StringBuffer sb) {
        this.sb = sb;
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
    }
}

/* CHEAT SHEET
   Without a lock, append() would break because it does
     ensureCapacity -> write char -> count += len
   and 'count += len' is read-modify-write. Two threads read the same count,
   write to the same index, and one update is lost.

   synchronized gives TWO things:
     1. Atomicity  — one thread at a time holds the object's lock (monitor)
     2. Visibility — unlock flushes writes, lock refreshes them (happens-before),
                     so t2 is guaranteed to see t1's changes, not a cached copy

   Thread-safe means NO CORRUPTION, not deterministic order.
   Appending "a" and "b" gives 2000 chars in an arbitrary order — all runs correct.

   In real code: don't share a buffer. Give each thread its own builder
   and join the results at the end. No lock, no contention.
*/