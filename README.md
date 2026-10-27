# Employee JDBC Management & Transaction Demo

A simple, robust Java application demonstrating how to connect to a MySQL database using JDBC, perform CRUD operations with prepared statements, and safely handle database transactions with commit and rollback.

---

## 📖 What Does This Code Do? (In Plain English)

Have you ever wondered how desktop or backend Java apps interact with databases without corrupting data when something goes wrong? This project demonstrates the complete end-to-end flow:

1. **Establishes a MySQL Connection (`DBConnection`)**:
   - Dynamically loads MySQL's modern JDBC driver (`com.mysql.cj.jdbc.Driver`).
   - Uses `DriverManager.getConnection(...)` with credentials to establish a secure database bridge to the `company` database.

2. **Automated Table Setup**:
   - The application automatically executes `CREATE TABLE IF NOT EXISTS employee (...)`.
   - If the table does not exist in your database, it creates it automatically with columns:
     - `id` (Integer, Primary Key)
     - `name` (Varchar 30)
     - `dept` (Varchar 20)

3. **Interactive Batch Insertion with `PreparedStatement`**:
   - Asks the user via terminal for the number of employees to insert.
   - For each employee, takes their ID, Name, and Department.
   - Inserts records using a parameterized query (`INSERT INTO employee VALUES(?,?,?)`). Parameterized statements protect against SQL Injection attacks and execute efficiently.

4. **Querying Filtered Records**:
   - Prompts for a target department (e.g., `Cse`).
   - Retrieves and prints all employees currently belonging to that department to give real-time feedback before making updates.

5. **Safe Transaction Handling (`commit` & `rollback`)**:
   - When modifying sensitive database records, executing statements one-by-one is risky—if the network drops or an error happens halfway through, the database could be left in an inconsistent state.
   - To prevent this, the code turns off autocommit (`con.setAutoCommit(false)`), grouping the updates into an atomic transaction:
     - It updates all employees from the old department to the new department.
     - **If successful**: `con.commit()` confirms and permanently writes the changes to the database.
     - **If an error occurs**: The `catch` block calls `con.rollback()`, discarding all changes from this operation and returning the database to its pristine prior state.

6. **Automatic Resource Cleanup (Try-with-Resources)**:
   - Every database object (`Connection`, `Statement`, `PreparedStatement`, `ResultSet`, and `Scanner`) is wrapped in Java's try-with-resources statement, ensuring connections and cursors are cleanly closed even if unexpected errors arise.

---

## 📁 Project Structure

```
Jdbc/
├── src/
│   └── com/
│       └── company/
│           └── ConnectionTest.java   # Contains DBConnection and transaction logic
├── .gitignore                        # Prevents binary class files and IDE jargon
└── README.md                         # Humanized documentation and guide
```

---

## 🚀 How to Run Locally

### 1. Prerequisites
- **Java JDK 8 or higher** installed.
- **MySQL Server** installed and running on `localhost:3306`.
- **MySQL Connector/J JAR** (e.g., `mysql-connector-j-8.3.0.jar`).
- A database named `company` created in MySQL:
  ```sql
  CREATE DATABASE company;
  ```

### 2. Configure Credentials
Open `src/com/company/ConnectionTest.java` and ensure your MySQL username and password match your local setup:
```java
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

### 3. Compile
```powershell
javac -d bin -cp "lib/mysql-connector-j-8.3.0.jar" src/com/company/ConnectionTest.java
```

### 4. Run
```powershell
java -cp "bin;lib/mysql-connector-j-8.3.0.jar" com.company.ConnectionTest
```

---

## 💡 Example Terminal Output

```text
Number of employees: 2
Enter ID, name, department: 101 Alice Cse
Enter ID, name, department: 102 Bob Cse
Department to update: Cse
New department: Management
Selected employees:
101 Alice Cse
102 Bob Cse
2 employees updated.
```
