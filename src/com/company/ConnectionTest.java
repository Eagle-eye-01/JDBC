package com.company;

import java.sql.*;
import java.util.Scanner;

class DBConnection {
    // Configure your MySQL credentials here
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

public class ConnectionTest {
    public static void main(String[] args) throws Exception {
        try (Scanner sc = new Scanner(System.in);
             Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS employee (" +
                    "id INT PRIMARY KEY, name VARCHAR(30), dept VARCHAR(20))");

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

            System.out.print("Department to update: ");
            String oldDept = sc.next();
            System.out.print("New department: ");
            String newDept = sc.next();

            con.setAutoCommit(false);
            try {
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
        }
    }
}
