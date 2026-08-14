package proxypattern;

// PROXY PATTERN — A stand-in object that pretends to be the real thing, but secretly
// adds extra behavior before/after the real call (e.g., BEGIN/COMMIT a transaction
// around a database save, like Spring's @Transactional).

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// A stand-in for Spring's real @Transactional annotation — just marks a method
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Transactional {
}

// 1. The interface — both the real object and the proxy implement this
interface UserService {
    void saveUser(String name);
}

// 2. The REAL object — plain business logic, no transaction code in sight
class UserServiceImpl implements UserService {

    @Transactional
    public void saveUser(String name) {
        System.out.println("Saving user to database: " + name);
        if (name.isEmpty()) {
            throw new RuntimeException("Name cannot be empty!");
        }
    }
}

// 3. The InvocationHandler — this is where the proxy's "extra behavior" lives.
// Every call made on the proxy passes through invoke() first.
class TransactionInvocationHandler implements InvocationHandler {

    private final Object realObject; // the real UserServiceImpl, hidden behind the proxy

    public TransactionInvocationHandler(Object realObject) {
        this.realObject = realObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // The annotation lives on the IMPLEMENTATION's method, not the interface's method,
        // so we look it up on the real object's class — this is what Spring does too.
        Method realMethod = realObject.getClass().getMethod(method.getName(), method.getParameterTypes());
        boolean isTransactional = realMethod.isAnnotationPresent(Transactional.class);

        if (!isTransactional) {
            return method.invoke(realObject, args); // no transaction needed, just call it directly
        }

        try {
            System.out.println("BEGIN TRANSACTION");
            Object result = method.invoke(realObject, args); // call the REAL method
            System.out.println("COMMIT");
            return result;
        } catch (Exception e) {
            System.out.println("ROLLBACK (caused by: " + e.getCause().getMessage() + ")");
            throw e.getCause(); // unwrap so the caller sees the real exception, not a reflection wrapper
        }
    }
}

// Main class to test it
public class ProxyPatternDemo {
    public static void main(String[] args) {
        UserServiceImpl realService = new UserServiceImpl();

        // Creating the dynamic proxy — this is what a Spring bean actually is at runtime
        UserService proxy = (UserService) Proxy.newProxyInstance(
                UserService.class.getClassLoader(),
                new Class[]{UserService.class},
                new TransactionInvocationHandler(realService)
        );

        // Caller just sees a normal UserService — has no idea it's talking to a proxy
        proxy.saveUser("Satish"); // succeeds -> BEGIN, save, COMMIT

        try {
            proxy.saveUser(""); // fails -> BEGIN, save throws, ROLLBACK
        } catch (Exception e) {
            System.out.println("Caller sees the exception too: " + e.getMessage());
        }
    }
}
