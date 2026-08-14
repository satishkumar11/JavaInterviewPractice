import java.util.*;
import java.util.stream.Collectors;

/**
 * Java 8 Stream API practice — EPAM prep.
 *
 * THE ONE RULE THAT COVERS EVERYTHING BELOW:
 *   groupingBy(classifier, downstream)
 *   The downstream collector decides what each bucket becomes.
 *
 * SELF-CHECK BEFORE YOU TYPE:
 *   Read the declared type on the left. Ask what the downstream produces.
 *   If they don't match, the downstream is wrong.
 */
public class Employee {

    // private final: encapsulation + immutability. Interviewers notice.
    private final int id;
    private final String name;
    private final String city;
    private final String department;
    private final double salary;
    private final int yearOfJoining;

    public Employee(int id, String name, String city, String department,
                    double salary, int yearOfJoining) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.department = department;
        this.salary = salary;
        this.yearOfJoining = yearOfJoining;
    }

    public int getId()            { return id; }
    public String getName()       { return name; }
    public String getCity()       { return city; }
    public String getDepartment() { return department; }
    public double getSalary()     { return salary; }
    public int getYearOfJoining() { return yearOfJoining; }

    @Override
    public String toString() {
        return name + "(" + department + "/" + (int) salary + ")";
    }

    public static void main(String[] args) {

        List<Employee> employees = Arrays.asList(
                new Employee(1, "Satish", "Noida",     "Backend",  90000,  2018),
                new Employee(2, "Virat",  "Bengaluru", "Backend",  75000,  2020),
                new Employee(3, "Rohit",  "Noida",     "Frontend", 120000, 2016),
                new Employee(4, "Anjali", "Bengaluru", "Backend",  68000,  2021),
                new Employee(5, "Priya",  "Noida",     "QA",       55000,  2019),
                new Employee(6, "Amit",   "Pune",      "Frontend", 45000,  2023),
                new Employee(7, "Zara",   "Noida",     "Backend",  90000,  2019)
        );
        // NOTE: Satish and Zara both earn 90000 — deliberate tie, watch it below.

        // =====================================================================
        // 1. FILTER -> SORT -> MAP
        //    Backend, salary > 70k, joined before 2021, names, salary desc.
        // =====================================================================
        List<String> topBackendNames = employees.stream()
                .filter(e -> "Backend".equalsIgnoreCase(e.getDepartment()))  // literal first: null-safe
                .filter(e -> e.getSalary() > 70000)
                .filter(e -> e.getYearOfJoining() < 2021)                    // one filter per business rule;
                // laziness fuses them into one pass
                .sorted(Comparator.comparingDouble(Employee::getSalary).reversed())
                .map(Employee::getName)
                .collect(Collectors.toList());

        System.out.println("1. Top backend names : " + topBackendNames);
        // [Satish, Zara, Virat]
        // Satish before Zara because sorted() is STABLE — equal elements keep encounter order.
        // For deterministic tie-break regardless of input order, append:
        //   .thenComparing(Employee::getName)
        // Careful: .reversed() binds to the comparator before it, so name stays ascending.

        // =====================================================================
        // 2. groupingBy, NO downstream
        //    Default downstream is toList(), so you get the full objects.
        // =====================================================================
        Map<String, List<Employee>> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment));

        System.out.println("2. By department     : " + byDepartment);
        // Declare Map, not HashMap — groupingBy is declared to return Map and
        // will not implicitly narrow. Key order is HashMap bucket order: DO NOT RELY ON IT.

        // =====================================================================
        // 3. downstream = mapping(fn, toList())
        //    Transform each element, then collect.
        //    mapping() ALWAYS takes 2 args — there is no one-arg form.
        // =====================================================================
        Map<String, List<String>> namesByDepartment = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.mapping(Employee::getName, Collectors.toList())));

        System.out.println("3. Names by dept     : " + namesByDepartment);
        // {QA=[Priya], Backend=[Satish, Virat, Anjali, Zara], Frontend=[Rohit, Amit]}

        // =====================================================================
        // 4. downstream = counting()  ->  produces Long
        //    Already the type we want. NO collectingAndThen needed.
        // =====================================================================
        Map<String, Long> countByCity = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getCity,
                        Collectors.counting()));

        System.out.println("4. Count by city     : " + countByCity);
        // {Bengaluru=2, Pune=1, Noida=4}

        // =====================================================================
        // 5. downstream = averagingDouble(fn)  ->  produces Double
        //    Also already the right type. No wrapper.
        // =====================================================================
        Map<String, Double> avgSalaryByDepartment = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.averagingDouble(Employee::getSalary)));

        System.out.println("5. Avg salary/dept   : " + avgSalaryByDepartment);
        // {QA=55000.0, Backend=80750.0, Frontend=82500.0}

        // =====================================================================
        // 6. downstream = mapping(fn, joining(", "))
        //    joining() only accepts CharSequence, so mapping() must convert
        //    Employee -> String first. That's why mapping is mandatory here.
        // =====================================================================
        Map<String, String> namesJoinedByDepartment = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.mapping(Employee::getName, Collectors.joining(", "))));

        System.out.println("6. Names joined      : " + namesJoinedByDepartment);
        // joining() has 3 forms: joining(), joining(delim), joining(delim, prefix, suffix)

        // =====================================================================
        // 7. downstream = collectingAndThen(maxBy(cmp), Optional::get)
        //    THE ONLY COMMON REASON collectingAndThen EXISTS:
        //    maxBy/minBy produce Optional<T>, and we want plain T.
        //
        //    collectingAndThen(downstreamCollector, finisherFunction)
        //      arg 1 = a COLLECTOR (does the work)
        //      arg 2 = a FUNCTION  (cleans up the result)
        //    Neither argument is a comparator — the comparator goes inside maxBy.
        // =====================================================================
        Map<String, Employee> highestPaidByDepartment = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary)),
                                Optional::get)));

        System.out.println("7. Highest paid/dept : " + highestPaidByDepartment);
        // Backend = Satish, not Zara: maxBy keeps the FIRST maximum
        // (it only replaces on strictly greater).
        //
        // Optional::get is normally an anti-pattern, but it is SAFE here:
        // groupingBy never creates an empty group, so maxBy always has a value.
        // Say this out loud if challenged.

        // =====================================================================
        // 8. Same shape, minBy — lowest paid per department.
        // =====================================================================
        Map<String, Employee> lowestPaidByDepartment = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparingDouble(Employee::getSalary)),
                                Optional::get)));

        System.out.println("8. Lowest paid/dept  : " + lowestPaidByDepartment);

        // =====================================================================
        // 9. THREE-ARG groupingBy: classifier, MAP FACTORY, downstream.
        //    Use when key order matters.
        //      TreeMap::new       -> keys sorted
        //      LinkedHashMap::new -> first-encountered order
        //    Same slot as the LinkedHashMap trick in "first repeating character".
        // =====================================================================
        Map<String, Long> countByDeptSorted = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        TreeMap::new,
                        Collectors.counting()));

        System.out.println("9. Count sorted keys : " + countByDeptSorted);
        // {Backend=4, Frontend=2, QA=1}  — alphabetical, guaranteed

        // =====================================================================
        // 10. partitioningBy — a Predicate, always exactly 2 keys (true/false),
        //     both present even when a side is empty. That's the difference
        //     from groupingBy, where absent groups simply don't appear.
        // =====================================================================
        Map<Boolean, List<String>> splitBySalary = employees.stream()
                .collect(Collectors.partitioningBy(
                        e -> e.getSalary() > 70000,
                        Collectors.mapping(Employee::getName, Collectors.toList())));

        System.out.println("10. High earners     : " + splitBySalary.get(true));
        System.out.println("    Rest             : " + splitBySalary.get(false));
    }
}

/*
 =========================  DOWNSTREAM CHEAT SHEET  =========================

   downstream                                    produces
   -------------------------------------------   ------------------
   (omitted)                                     List<Employee>
   counting()                                    Long
   mapping(fn, toList())                         List<R>
   mapping(fn, toSet())                          Set<R>
   mapping(fn, joining(", "))                    String
   averagingDouble(fn) / summingDouble(fn)       Double
   summarizingDouble(fn)                         DoubleSummaryStatistics
   maxBy(cmp) / minBy(cmp)                       Optional<Employee>
   collectingAndThen(maxBy(cmp), Optional::get)  Employee
   groupingBy(fn2, downstream2)                  nested Map

 Only the Optional rows need collectingAndThen. If the downstream already
 produces the type you declared, do not wrap it.

 =============================  OTHER TRAPS  ================================
   toMap() with duplicate keys      -> IllegalStateException; pass a merge fn
   .thenComparing(x).reversed()     -> reverses the WHOLE chain
   Stream.of(new int[]{1,2,3})      -> Stream<int[]> of size 1; use Arrays.stream
   IntStream.average()              -> OptionalDouble, needs .orElse(0.0)
   reusing a stream variable        -> IllegalStateException
   stateful lambda + parallelStream -> silently wrong results
 ============================================================================
*/