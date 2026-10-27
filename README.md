# JDBC Employee Management & Transaction Processing

A Java application demonstrating relational database connectivity using **Java Database Connectivity (JDBC)** with **MySQL Connector/J**. The project covers fundamental and advanced JDBC concepts including driver registration, connection pooling/lifecycle, dynamic DDL execution, parameterized DML statements (`PreparedStatement`), cursor-based result retrieval (`ResultSet`), and manual ACID transaction management (`commit` and `rollback`).

---

## 📸 Output & Execution Screenshots

### 1. Terminal Execution
Running the program, reading terminal inputs, displaying queried records, and executing the transaction:

![Terminal Execution](screenshots/terminal_execution.png)

---

### 2. Database Verification (MySQL Server)

| Before Transaction (Initial State) | After Transaction (Committed State) |
| :---------------------------------: | :----------------------------------: |
| ![MySQL Before Update](screenshots/mysql_before_update.png) | ![MySQL After Update](screenshots/mysql_after_update.png) |
| *Record `106 (Punya)` assigned to `Cse`* | *Record `106 (Punya)` updated to `Management`* |

---

## 🧠 In-Depth Code Explanation

The codebase is organized within `src/com/company/ConnectionTest.java` and contains two core classes: `DBConnection` (utility layer) and `ConnectionTest` (driver/application layer).

```
com.company
├── DBConnection    -> Manages driver loading and provides Connection objects
└── ConnectionTest  -> Contains main() logic: DDL, batch DML, queries, and transaction boundaries
```

---

### 1. `DBConnection` Class — Connection & Driver Management

```java
class DBConnection {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "company";
    private static final String USER = "root";
    private static final String PASSWORD = "Gourav@2007";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

- **Configuration Constants**: Defines connection parameters (`HOST`, `PORT`, `DATABASE`, `USER`, `PASSWORD`) and constructs a standard JDBC URL formatted as:
  `jdbc:mysql://<host>:<port>/<database>`.
- **Static Initialization Block (`static { ... }`)**:
  - Executes when the `DBConnection` class is first loaded into memory by the JVM.
  - `Class.forName("com.mysql.cj.jdbc.Driver")` dynamically loads and registers the MySQL Type 4 pure Java driver with Java's `DriverManager`.
- **Factory Method (`getConnection()`)**:
  - Invokes `DriverManager.getConnection(URL, USER, PASSWORD)`.
  - Establishes a physical TCP socket connection with the MySQL server, completes database authentication, and returns an active `java.sql.Connection` object.

---

### 2. `ConnectionTest` Class — Execution Lifecycle & Queries

#### A. Resource Management with Try-with-Resources
```java
try (Scanner sc = new Scanner(System.in);
     Connection con = DBConnection.getConnection();
     Statement st = con.createStatement()) {
    ...
}
```
- Implements Java's **try-with-resources** pattern (`AutoCloseable` interface).
- Guarantees deterministic resource cleanup: `Scanner`, `Connection`, `Statement`, `PreparedStatement`, and `ResultSet` instances are closed automatically in reverse order of allocation when the block exits, preventing connection pooling starvation, file handle exhaustion, and memory leaks.

---

#### B. Dynamic Schema Setup (DDL)
```java
st.executeUpdate("CREATE TABLE IF NOT EXISTS employee (" +
        "id INT PRIMARY KEY, name VARCHAR(30), dept VARCHAR(20))");
```
- Uses `Statement.executeUpdate()` to dispatch Data Definition Language (DDL).
- Automatically initializes the relational table schema with:
  - `id`: Integer datatype configured with a `PRIMARY KEY` constraint (enforcing uniqueness and non-nullability).
  - `name`: Variable-length string up to 30 characters (`VARCHAR(30)`).
  - `dept`: Variable-length string up to 20 characters (`VARCHAR(20)`).

---

#### C. Parameterized Batch Insertion (DML with `PreparedStatement`)
```java
System.out.print("Number of employees: ");
int n = sc.nextInt();

try (PreparedStatement ps = con.prepareStatement(
        "INSERT INTO employee VALUES(?,?,?)")) {
    for (int i = 0; i < n; i++) {
        System.out.print("Enter ID, name, department: ");
        ps.setInt(1, sc.nextInt());
        ps.setString(2, sc.next());
        ps.setString(3, sc.next());
        ps.executeUpdate();
    }
}
```
- **Precompilation**: `con.prepareStatement(...)` sends the SQL template with `?` positional parameter markers to the MySQL query optimizer ahead of execution.
- **SQL Injection Prevention**: Parameter substitution (`setInt(1, ...)`, `setString(2, ...)`, `setString(3, ...)`) ensures data is strongly typed and treated strictly as literals, eliminating SQL injection vulnerabilities.
- **Reusability**: Inside the loop, the statement template is reused across $n$ insertions with low overhead.

---

#### D. Cursor-Based Query Execution (`ResultSet`)
```java
try (PreparedStatement ps = con.prepareStatement(
        "SELECT * FROM employee WHERE dept=?")) {
    ps.setString(1, oldDept);
    try (ResultSet rs = ps.executeQuery()) {
        System.out.println("Selected employees:");
        while (rs.next()) {
            System.out.println(rs.getInt("id") + " "
                    + rs.getString("name") + " "
                    + rs.getString("dept"));
        }
    }
}
```
- Dispatches a read query via `ps.executeQuery()` which yields a `java.sql.ResultSet`.
- The `ResultSet` acts as an active forward-only cursor pointing initially before the first row.
- `rs.next()` advances the cursor through the result set until returning `false`.
- Column data is retrieved using typed accessors (`rs.getInt("id")`, `rs.getString("name")`, `rs.getString("dept")`).

---

#### E. ACID Transaction Management (`commit` and `rollback`)
```java
con.setAutoCommit(false);
try {
    ...
    int count;
    try (PreparedStatement ps = con.prepareStatement(
            "UPDATE employee SET dept=? WHERE dept=?")) {
        ps.setString(1, newDept);
        ps.setString(2, oldDept);
        count = ps.executeUpdate();
    }

    con.commit();
    System.out.println(count + " employees updated.");
} catch (Exception e) {
    try {
        con.rollback();
        System.out.println("Failed! Transaction rolled back.");
    } catch (SQLException rollbackError) {
        e.addSuppressed(rollbackError);
    }
    throw e;
}
```

1. **Transaction Boundary Initiation**:
   - By default, JDBC runs in autocommit mode (`con.getAutoCommit() == true`), committing each SQL statement immediately upon execution.
   - `con.setAutoCommit(false)` disables automatic commits and begins an explicit database transaction boundary.
2. **Atomic Execution**:
   - The `UPDATE` query modifies rows matching `dept = oldDept` to `dept = newDept`.
3. **Transaction Commit (`con.commit()`)**:
   - If all operations complete without errors, `con.commit()` makes all modifications permanent across the database engine storage engine (InnoDB).
4. **Failure Handling and Rollback (`con.rollback()`)**:
   - If any runtime exception or `SQLException` is thrown during the transaction, control transfers to the `catch` block.
   - `con.rollback()` rolls back the entire batch of operations to the state prior to the transaction, guaranteeing **Atomicity** (all-or-nothing execution).
5. **Suppressed Exception Preservation**:
   - If `con.rollback()` encounters a subsequent `SQLException`, `e.addSuppressed(rollbackError)` records it under the primary exception, preventing original error stack traces from being masked.

---

## 🛠️ Compilation & Execution

### 1. Compilation
Compile `ConnectionTest.java` pointing to the MySQL JDBC connector:
```powershell
javac -d bin -cp "lib/mysql-connector-j-8.3.0.jar" src/com/company/ConnectionTest.java
```

### 2. Execution
Run the compiled bytecode specifying the connector in the classpath:
```powershell
java -cp "bin;lib/mysql-connector-j-8.3.0.jar" com.company.ConnectionTest
```
