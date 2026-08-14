package singletonpattern;

// SINGLETON PATTERN — Only ONE object of this class can ever exist, and everyone
// shares that same one (e.g., one database connection for the whole app).

// The Singleton class — only ONE object of this class will ever exist
class DatabaseConnection {

    // 1. The single instance, held privately inside the class itself.
    // "static" means it belongs to the class, not to any individual object.
    private static DatabaseConnection instance;

    // 2. Private constructor — stops anyone outside from doing "new DatabaseConnection()"
    private DatabaseConnection() {
        System.out.println("Creating the one and only DatabaseConnection...");
    }

    // 3. Public static method — the ONLY way to get the instance.
    // "synchronized" makes it safe even if multiple threads call this at the same time.
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection(); // created only once, the first time it's needed
        }
        return instance; // every later call just returns the same object
    }

    public void query(String sql) {
        System.out.println("Running query: " + sql);
    }
}

// Main class to test it
public class SingletonPatternDemo {
    public static void main(String[] args) {
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        db1.query("SELECT * FROM users");

        DatabaseConnection db2 = DatabaseConnection.getInstance();
        db2.query("SELECT * FROM orders");

        // Proving db1 and db2 are actually the SAME object
        System.out.println("Are db1 and db2 the same instance? " + (db1 == db2));
    }
}
