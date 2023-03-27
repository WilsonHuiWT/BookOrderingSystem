package csci3170project;
import java.util.Scanner;
import java.sql.*;
import java.io.*;


class CSCI3170Project {
    public static void main(String[] args) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            } 
        catch(Exception x) {
            System.err.println("Unable to load the driver class!");
        }
        Connection conn = null;
        
        String dbURL = "jdbc:mysql://localhost:3306/book?autoReconnect=true&useSSL=false";
        String user = "root";
        String pass = "csci3170";
        conn = DriverManager.getConnection(dbURL, user, pass);
        if (conn != null) {
            DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
            System.out.println("Driver name: " + dm.getDriverName());
            System.out.println("Driver version: " + dm.getDriverVersion());
            System.out.println("Product name: " + dm.getDatabaseProductName());
            System.out.println("Product version: " + dm.getDatabaseProductVersion());
        }
        Statement st = conn.createStatement();
        System.out.println(st);
        String Sql = "SELECT * FROM book"; 
        ResultSet rs = st.executeQuery("SELECT * FROM book");
        while (rs.next()) {
            System.out.println(rs.getString("ISBN") 
            + " " + rs.getString("Title") 
            + " " + rs.getString("Price") 
            + " " + rs.getString("Inventory_Quantity"));
          }
        // Create a scanner object for user input
        Scanner scanner = new Scanner(System.in);

        // Initialize the system date and database records
        long millis = System.currentTimeMillis();
        Date systemDate = new Date (millis);
        int books = 999;
        int customers = 999;
        int orders = 999;

        // Display the welcome message and the menu options
        System.out.println("===== Welcome to Book Ordering Management System =====");
        System.out.println(" + System Date: " + systemDate);
        System.out.println(" + Database Records: Books (" + books + "), Customers (" + customers + "), Orders (" + orders + ")");
        System.out.println("——————————————————————————");
        System.out.println(" > 1. Database Initialization");
        System.out.println(" > 2. Customer Operation");
        System.out.println(" > 3. Bookstore Operation");
        System.out.println(" > 4. Quit");

        // Prompt the user to enter a query
        System.out.print(">>> Please Enter Your Query: ");
        int query = scanner.nextInt();

        // Process the query based on the user input
        switch (query) {
            case 1:
                // Create an object of DatabaseInitialization class and call its method
                DatabaseInitialization di = new DatabaseInitialization();
                di.initializeDatabase();
                break;
            case 2:
                // Create an object of CustomerOperation class and call its method
                CustomerOperation co = new CustomerOperation();
                co.performCustomerOperation();
                break;
            case 3:
                // Create an object of BookstoreOperation class and call its method
                BookstoreOperation bo = new BookstoreOperation();
                bo.performBookstoreOperation();
                break;
            case 4:
                // Quit
                break;
            default:
                // Invalid input
                System.out.println("Invalid query. Please enter a number between 1 and 4.");
                break;
        }

        // Close the scanner object
        scanner.close();
        rs.close();
        st.close();
        conn.close();   
    }
}

// The class that handles the database initialization
class DatabaseInitialization {

    // Declare the instance variables for system date and database records
    private String systemDate;
    private int books;
    private int customers;
    private int orders;

    // Define the constructor that initializes the instance variables with default values
    public DatabaseInitialization() {
        systemDate = "2000-00-00";
        books = 999;
        customers = 999;
        orders = 999;
    }

    // Define the getter methods for the instance variables
    public String getSystemDate() {
        return systemDate;
    }

    public int getBooks() {
        return books;
    }

    public int getCustomers() {
        return customers;
    }

    public int getOrders() {
        return orders;
    }

    // Define the method that performs the database initialization
    public void initializeDatabase() {
        // Write your code here to initialize the database
        // Database Initialization
                // Read SQL query from txt files
                try {
                    // read the SQL query from the file
                    
                    BufferedReader br = new BufferedReader(new FileReader("query.txt"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                      sb.append(line);
                    }
                    br.close();
                    
                    // get the connection to the database
                    Connection conn = null;
                    
                    String dbURL = "jdbc:mysql://localhost:3306/book?autoReconnect=true&useSSL=false";
                    String user = "root";
                    String pass = "csci3170";
                    conn = DriverManager.getConnection(dbURL, user, pass);
                    // create a prepared statement with the query
                    String query1 = sb.toString();
                    PreparedStatement ps = conn.prepareStatement(query1);
                    
                    // set the parameter for the session ID
                    // ps.setString(1, sessionID);
                    // execute the query and get the result set
                    ResultSet res = ps.executeQuery();
                    // process the result set
                    while (res.next()) {
                      // do something with each row
                    }
                    // close the resources
                    res.close();
                    ps.close();
                  } catch (Exception e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                  }
    }
}

// The class that handles the customer operation
class CustomerOperation {
    // Define the method that performs the customer operation
    public void performCustomerOperation() {
        // Write your code here to perform the customer operation
    }
}

// The class that handles the bookstore operation
class BookstoreOperation {

    // Define the method that performs the bookstore operation
    public void performBookstoreOperation() {
        // Write your code here to perform the bookstore operation
    }
}
