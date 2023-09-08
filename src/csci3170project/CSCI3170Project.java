package csci3170project;

import java.util.Scanner;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class CSCI3170Project {
    public static void main(String[] args) throws SQLException, ParseException, IOException {

        Connection conn = null;
        String classN = "oracle.jdbc.driver.OracleDriver";
        String dbURL = "jdbc:oracle:thin:@//db18.cse.cuhk.edu.hk:1521/oradb.cse.cuhk.edu.hk";
        String user = "h050";
        String pass = "clotGomy";

        try {
            Class.forName(classN);
            conn = DriverManager.getConnection(dbURL, user, pass);
        } catch (Exception error) {
            error.printStackTrace();
            System.err.println("Message of Exception: " + error.getMessage());
            return;
        } finally {
            // System.out.println("Connection process is done.");
        }
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("===== Welcome to Book Ordering Management System =====");
        Scanner scanner = new Scanner(System.in); // User input desired field
        boolean flag = true;
        while (flag) {
            int field = 0;
            while (true) {         
                System.out.println("The current date and time is "+ formatter1.format((System.currentTimeMillis())) + "\n"+
                "----- Main Menu -----\n" +
                        "Please select your user type: \n" +
                        "1. Admins\n" +
                        "2. General Users\n" +
                        "3. Employees or Managers\n" +
                        "4. Exit the program\n");
                System.out.print("Enter the number: ");
                String input = scanner.nextLine();
                // Check if the input is an integer.
                try {
                    field = Integer.parseInt(input);
                    if (field == 1 || field == 2 || field == 3 || field == 4) {
                        //System.out.println("Correct Input.");
                        break;
                    }else {
                        System.out.println("Invalid input. Please enter an integer 1,2,3 or 4.");
                    }
                } catch (NumberFormatException error) {
                    System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter an integer 1,2,3 or 4.");
                }
            }

            
            String[] sql_create = {
                "CREATE TABLE BOOK (ISBN CHAR(13) PRIMARY KEY, Title VARCHAR(100) NOT NULL, Price NUMBER CHECK (Price >= 0), Inventory_Quantity NUMBER CHECK (Inventory_Quantity >= 0))",
                "CREATE TABLE CUSTOMER (CID VARCHAR(10) PRIMARY KEY, Name VARCHAR(50) NOT NULL, Address VARCHAR(200) NOT NULL)",
                "CREATE TABLE ORDERS (OID VARCHAR(10) NOT NULL, CID VARCHAR(10) NOT NULL, Order_Date DATE NOT NULL, Order_ISBN CHAR(13) NOT NULL, Order_Quantity NUMBER CHECK (Order_Quantity > 0) NOT NULL, Shipping_Status VARCHAR(10) NOT NULL, PRIMARY KEY (OID, Order_ISBN),  FOREIGN KEY (CID) REFERENCES Customer (CID), FOREIGN KEY (Order_ISBN) REFERENCES Book (ISBN))",
                "CREATE TABLE AUTHORS (aid VARCHAR(10) PRIMARY KEY, aname VARCHAR(50) NOT NULL)",
                "CREATE TABLE PLACE (CID VARCHAR(10) NOT NULL, OID VARCHAR(10) NOT NULL, Order_ISBN CHAR(13) NOT NULL, PRIMARY KEY (Order_ISBN, OID), FOREIGN KEY (CID) REFERENCES Customer (CID), FOREIGN KEY (Order_ISBN) REFERENCES Book (ISBN))",
                "CREATE TABLE INCLUDE (OID VARCHAR(10) NOT NULL, ISBN CHAR(13) NOT NULL, Quantity NUMBER CHECK (Quantity > 0) NOT NULL, PRIMARY KEY (OID, ISBN), FOREIGN KEY (ISBN) REFERENCES Book (ISBN))",
                "CREATE TABLE WRITTENBY (ISBN CHAR(13) NOT NULL, aid VARCHAR(10) NOT NULL, PRIMARY KEY (ISBN, aid), FOREIGN KEY (ISBN) REFERENCES Book (ISBN), FOREIGN KEY (aid) REFERENCES Authors (aid))"
            };

            String[] sql_drop = new String[] {
                    "DROP TABLE INCLUDE",
                    "DROP TABLE WRITTENBY",
                    "DROP TABLE PLACE",
                    "DROP TABLE ORDERS",
                    "DROP TABLE CUSTOMER",
                    "DROP TABLE BOOK",
                    "DROP TABLE AUTHORS",
            };

            if (field == 1) {
                // Perform Admin actions
                boolean flag1 = true;
                while (flag1) {
                    int field1 = 0;
                    while (true) {
                        
                        System.out.println("\nThe current date and time is "+ formatter1.format((System.currentTimeMillis())) + "\n"+
                        "----- Admin Interface: Main Page -----\n" +
                        "There are 4 types of admin actions:\n" +
                        "1. Initialize the database(Create Tables)\n" +
                        "2. Load init records from local files\n" +
                        "3. Reset all the existing records\n" +
                        "4. Drop all the existing records\n" +
                        "5. Overview of all the database records\n"+
                        "6. Back to Main Menu\n");
                        System.out.print("Select your action: ");
                        String input = scanner.nextLine();
                        // Check if the input is correct.
                        try {
                            field1 = Integer.parseInt(input);
                            if (field1 >=1 && field1 <= 6) {
                                break;
                            }else {
                                System.out.println("Invalid input. Please enter an integer 1-6.");
                            }
                        } catch (NumberFormatException error) {
                            System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter an integer 1-6.");
                        }
                    }

                    if (field1 == 1) {
                        // initialize the database (Create Tables)
                        try {
                            Statement stmt = conn.createStatement();
                            for (String sql : sql_create) {
                                String tableName = sql.split("\\s")[2];
                                ResultSet rs = stmt.executeQuery(
                                        "SELECT table_name FROM user_tables WHERE table_name = '" + tableName + "'");
                                if (!rs.next()) {
                                    stmt.executeUpdate(sql);
                                    System.out.println("Table " + tableName + " created successfully.");
                                } else {
                                    System.out.println("Table " + tableName + " is created already.");
                                }
                            }
                            stmt.close();
                            System.out.println("Tables created successfully.");
                        } catch (Exception error) {
                            System.err.println("Message of Exception: " + error.getMessage());
                            continue;
                        } 

                    } else if (field1 == 2) {
                        
                        if(checkExist(conn) == true){
                            try{
                                InsertCSV(conn);
                            }catch (Exception error) {
                                System.err.println("Message of Exception: " + error.getMessage());
                                continue;
                            }
                        }
                        else{
                            System.out.println("Tables are not existed. Please create the table before inserting the table record!");
                        }
                        
                    } else if (field1 == 3) {
                        // reset all the existing records
                        try {
                            // Check whether table exists
                            Statement stmt = conn.createStatement();
                            for (String sql : sql_drop) {
                                String tableName = sql.split("\\s")[2];
                                ResultSet rs = stmt.executeQuery(
                                        "SELECT table_name FROM user_tables WHERE table_name = '" + tableName + "'");
                                // drop exist table
                                if (rs.next()) {
                                    stmt.executeUpdate(sql);
                                    System.out.println("Table " + tableName + " dropped successfully.");
                                }
                            }
                            System.out.println();
                            // Create tables
                            for (String sql : sql_create) {
                                String tableName = sql.split("\\s")[2];
                                stmt.executeUpdate(sql);
                                System.out.println("Table " + tableName + " Created successfully.");
                            }
                            System.out.println();
                            // Create records
                            stmt.close();
                            InsertCSV(conn);
                            System.out.println("Tables and Records reset successfully.");

                        } catch (Exception error) {
                            System.err.println("Message of Exception: " + error.getMessage());
                            continue;
                        }

                    } else if (field1 == 4) {
                        // Drop all existing records
                        if(checkExist(conn)==true){
                            try {
                                Statement stmt = conn.createStatement();
                                for (String sql : sql_drop) {
                                    String tableName = sql.split("\\s")[2];
                                    ResultSet rs = stmt.executeQuery(
                                            "SELECT table_name FROM user_tables WHERE table_name = '" + tableName + "'");
                                    if (rs.next()) {
                                        // Check tables exist1
                                        stmt.executeUpdate(sql);
                                        System.out.println("Table " + tableName + " dropped successfully.");
                                    } else {
                                        System.out.println("Table " + tableName + " is dropped already.");
                                    }
                                }
                                stmt.close();
                                System.out.println("Records dropped successfully.");
                                }catch (Exception error) {
                                System.err.println("Message of Exception: " + error.getMessage());
                                continue;
                            }
                        }
                        else{
                            System.out.println("Tables are not existed. Please create the table first before dropping the table record!");
                        }
                    }   

                    else if(field1 == 5){
                        if(checkExist(conn)==true)
                            printAllTable(conn);
                        else
                            System.out.println("Tables are not existed. Please create the table first before showing the table record!");
                    }   

                    else if (field1 == 6) {
                        System.out.println("\nReturning to Back to Main Menu\n");
                        flag1 = false;
                        break;
                    } else {
                        System.err.println("\nInvalid Input. Please enter again.");
                    }
                }

            }

            else if (field == 2) {
                
                if(checkExist(conn) !=true){
                    System.out.println("\nDatabase is not available. Please contact the admin.\n You will be redirected to the Main Menu\n");
                    continue;
                }
                
                // Perform User actions
                boolean flag2 = true;
                while (flag2) {
                    int field2 = 0;
                    while (true) {         
                        System.out.println("\nThe current date and time is "+ formatter1.format((System.currentTimeMillis())) + "\n"+
                            "----- General User Main Page -----\n" +
                                    "There are 4 types of general actions:\n" +
                                    "1. Book Search\n" +
                                    "2. Place an Order\n" +
                                    "3. Check History Orders\n" +
                                    "4. Back to Main Menu\n");
                        System.out.print("Select your action: ");
                        
                        // Check if the input is correct.
                        if(scanner.hasNextInt()){
                            field2 = scanner.nextInt();
                            if (field2 >=1 && field2 <= 4) 
                                break;
                            else
                                System.out.println("Invalid input. Please enter an integer 1-4.");//Input is integer but not in [1,4]
                        }
                        else{
                            // Not an integer
                            scanner.next();
                            System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter an integer 1-4.");
                        }
    
                    }
                    if (field2 == 1) {
                        // User search book
                        boolean flag5 = true;
                        
                        while (flag5) {
                            int selectfield = 0;
                            while (true) {         
                                System.out.println("\nWhich field would you like to search?\n" +
                                "1. Book ISBN\n" +
                                "2. Book Title\n" +
                                "3. Author Name\n" +
                                "4. Back to General User Main Page\n");
                                System.out.print("Enter your search field: ");
                                // Check if the input is correct.
                                if(scanner.hasNextInt()){
                                    selectfield = scanner.nextInt();
                                    if (selectfield >=1 && selectfield <= 4) 
                                        break;
                                    else
                                        System.out.println("Invalid input. Please enter an integer 1-4.");//Input is integer but not in [1,4]
                                }
                                else{
                                    // Not an integer
                                    scanner.next();
                                    System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter an integer 1-4.");
                                }
                               }
                            
                            boolean returnkey1 = false;
                            switch (selectfield) {
                                case 1:
                                    String isbn = "";
                                    scanner.nextLine(); // turn to consume the newline characters/string
                                    returnkey1 = false;
                                    while (returnkey1 == false) {
                                        System.out.println("\nNote: Complete book ISBN is required. For example, you should enter X-XXXX-XXXX-X where X are digits.\n");
                                        System.out.print("Please enter the complete book ISBN (including the hyphen '-'): ");
                                        try {
                                            isbn = scanner.nextLine();
                                        } catch (Exception error) {
                                            System.err.println("Message of Exception: " + error.getMessage());
                                            continue;
                                        }
                                        if (isbn != "" && isbn != null && isbn.length() == 13)
                                            break;
                                        else{
                                            System.out.println("\nError Message: Invalid ISBN. Please enter again.\n");
                                            int choice = returnkeytemp(scanner, "Book ISBN");
                                            if (choice == 1)
                                                returnkey1 = true;
                                            
                                            else if (choice == 2)
                                                continue;// Re-enter the request book ISBN
                                        }   
                                    }

                                    try {
                                        searchbook(1, isbn, conn);
                                    } catch (Exception error) {
                                        System.err.println("Message of Exception: " + error.getMessage());
                                        continue;
                                    }
                                    break;

                                case 2:
                                    String booktitle = "";
                                    returnkey1 = false;
                                    scanner.nextLine(); // turn to consume the newline characters/string
                                    while (true) {
                                        System.out.print("Enter Book Title: ");
                                        try {
                                            booktitle = scanner.nextLine();
                                        } catch (Exception error) {
                                            System.err.println("Message of Exception: " + error.getMessage());
                                            continue;
                                        }
                                        if (booktitle != "" && booktitle != null && booktitle.length() <= 100)
                                            break;
                                        else{
                                            System.out.println(
                                                    "\nError Message: Invalid Book Title. Please enter again.\n");
                                            int choice = returnkeytemp(scanner, "Book Title");
                                            if (choice == 1)
                                                returnkey1 = true;
                                            
                                            else if (choice == 2)
                                                continue;// Re-enter the request book Title
                                        }
                                            
                                    }

                                    if(returnkey1)
                                        break;

                                    try {
                                        searchbook(2, booktitle, conn);
                                    } catch (Exception error) {
                                        System.err.println("Message of Exception: " + error.getMessage());
                                        continue;
                                    }
                                    break;

                                case 3:
                                    String authorname = "";
                                    returnkey1 = false;
                                    scanner.nextLine(); // turn to consume the newline characters/string
                                    while (true) {
                                        System.out.print("\nNote: Please enter one author name for each searching. (No complete name is required.)\n"+"Enter Author Name: ");
                                        try {
                                            authorname = scanner.nextLine();
                                        } catch (Exception error) {
                                            System.err.println("Message of Exception: " + error.getMessage());
                                            continue;
                                        }
                                        if (authorname != "" && authorname != null && authorname.length() <= 50)
                                            break;
                                        else{
                                            System.out.println(
                                                    "\nError Message: Invalid author name. Please enter again.\n");
                                            int choice = returnkeytemp(scanner, "Book Authors");
                                            if (choice == 1)
                                                returnkey1 = true;
                        
                                            else if (choice == 2)
                                                continue;// Re-enter the request book authors
                                        }
                                            
                                    }
                                    
                                    if(returnkey1)
                                        break;
     
                                    try {
                                        searchbook(3, authorname, conn);
                                    } catch (Exception error) {
                                        System.err.println("Message of Exception: " + error.getMessage());
                                        continue;
                                    }

                                    break;

                                case 4:
                                    System.out.println("\nReturning to General User Main Page\n");
                                    flag5 = false;
                                    break;

                                default:
                                    System.out.println("Error Message: Invalid Input. Please enter again.");
                                    break;
                            }
                        }
                    }

                    else if (field2 == 2) {
                        scanner.nextLine();
                        while (true) {
                            System.out.println("\n----- Welcome to Shopping Cart -----\n" +
                                    "In order to place an order, you need to enter (1) your User ID and (2) Book ISBN.\n");
                            boolean returnkey = false;
                            boolean leaving_shopping_cart = false;
                            String userID = "";
                            boolean reenter= false;
                            // Input User ID
                            while(true) {
                                System.out.print("Enter your user ID: ");
                                try {
                                    userID = scanner.nextLine();
                                } catch (Exception error) {
                                    error.getStackTrace();
                                    System.err.println("Message of Exception: " + error.getMessage());
                                    continue;
                                }
                                if (userID != "" && userID != null && userID.length() <= 10) {
                                    boolean checkID = checkUserID(userID, conn);
                                    if (checkID == true) {
                                        System.out.println(
                                                "\nSuccess Message: User ID is valid. Please enter the book ISBN below.\n");
                                        break;
                                    } else {
                                            System.out.println(
                                                "\nError Message: This User ID does not exist. You can \n(1) Enter the User ID again or \n(2) Leave the Shopping Cart.\n");
                                            while(true){
                                                System.out.print("Enter your choice: ");
                                                int choice = -1;
                                                if(scanner.hasNextInt()){
                                                    choice = scanner.nextInt();
                                                    scanner.nextLine();
                                                }
                                                else{
                                                    System.out.println("\nInvalid input. Please Enter again.\n");
                                                    scanner.next();
                                                    continue;
                                                }
                                                if(choice == 1)
                                                    returnkey = true;
                                                else if(choice == 2)
                                                    leaving_shopping_cart = true;
                                                else{
                                                    System.out.println("\nInvalid input. Please Enter again.\n");
                                                    continue;
                                                }   
                                                break;
                                            }
                                    }
                                } else {
                                    System.out.println(
                                        "\nError Message: Invalid input.\n You can \n(1) Enter the User ID again or \n(2) Leave the Shopping Cart.\n");
                                        while(true){
                                            System.out.print("Enter your choice: ");
                                            int choice = -1;
                                            if(scanner.hasNextInt()){
                                                choice = scanner.nextInt();
                                                scanner.nextLine();
                                            }
                                            else{
                                                System.out.println("\nInvalid input. Please Enter again.\n");
                                                scanner.next();
                                                continue;
                                            }
                                            if(choice == 1)
                                                returnkey = true;
                                            else if(choice == 2)
                                                leaving_shopping_cart = true;
                                            else{
                                                System.out.println("\nInvalid input. Please Enter again.\n");
                                                continue;
                                            }   
                                            break;
                                        }
                                }
                                if(leaving_shopping_cart)
                                        break;
                                if (returnkey)
                                    break;// return to Shopping Cart
                            } 
                            if (returnkey)
                                continue;// return to Shopping Cart
                            if (leaving_shopping_cart)
                                break;// return to Shopping Cart
                            
                                
                            // Input Order Quantity
                            int counter = 0;
                            
                            while(true){
                                System.out.print("How many different books you would like to order?\n"); 
                                System.out.print("Enter the number: ");
                                if(scanner.hasNextInt()){
                                    counter = scanner.nextInt();
                                    
                                }
                                else{
                                    scanner.next();
                                    System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter a positive integer.");
                                    int choice = returnkey(scanner);
                                    if (choice == 1){
                                        returnkey = true;
                                        break;
                                    }
                                    else if (choice == 2)
                                        continue;// Re-enter the request book Quantity
                                }
                                if (counter > 0) { // Yes
                                    if (counter == 1) // an order with only 1 book
                                        System.out.println("\nNote: You want to order 1 book.");
                                    else if (counter > 1) // an order with more than 1 book (some of them can be duplicated)
                                        System.out.printf("\nNote: You want to order %d different books. (It can be duplicated.)\n", counter);
                                    break;
                                } 
                                else{ // No
                                    System.out.println("\nError Message: Invalid quantity. You cannot order 0 or negative number of books. Please input again. ---\n");
                                    int choice = returnkey(scanner);
                                    if (choice == 1){
                                        returnkey = true;
                                        break;
                                    }
                                    else if (choice == 2)
                                        continue;// Re-enter the request book Quantity
                                }
                            }

                            if (returnkey)
                                continue;

                            // Input Order Book ISBN && Book Quantity
                            int tracenum = 1;
                            String[] bookISBN = new String[counter];
                            int[] bookQuantity = new int[counter];
                            int duplicatednum = 0;
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            boolean checkorderID = false;
                            String orderIDStr = "";
                            while (!checkorderID) {
                                orderIDStr = orderID(); // generate random order ID
                                checkorderID = checkUniqueOID(orderIDStr, conn); // check if the generated random order ID is unique
                            }

                            //System.out.printf("Order ID: %s", orderIDStr);
                            scanner.nextLine();
                            while (tracenum == 1 || tracenum != counter) {

                                boolean flagISBNcheck = false;
                                boolean orderbookquantitycheck = false;
                                boolean duplicated = false;

                                int duplicated_node = 0;

                                do {
                                    duplicated = false;
                                    if (tracenum == 1)
                                        System.out.printf("\nNote: You are entering the data of %dst order\n",
                                                tracenum);
                                    else if (tracenum == 2)
                                        System.out.printf("\nNote: You are entering the data of %dnd order\n",
                                                tracenum);
                                    else if (tracenum == 3)
                                        System.out.printf("\nNote: You are entering the data of %drd order\n",
                                                tracenum);
                                    else if (tracenum > 3)
                                        System.out.printf("\nNote: You are entering the data of %dth order\n",
                                                tracenum);
                                    
                                    while (true) {
                                        String temp = "";
                                        System.out.print("Enter your book ISBN: ");
                                        
                                        temp = scanner.nextLine();

                                        if (temp != "" && temp != null && temp.length() <= 13) {

                                            boolean checkISBN = checkbookISBN(temp, conn);
                                            if (tracenum == 1 && checkISBN == true) {//
                                                bookISBN[tracenum - 1] = temp;
                                                break;
                                            }

                                            else if (tracenum != 1 && checkISBN == true) { //
                                                // Check duplicated records;
                                                for (int i = 0; i < bookISBN.length; i++) {
                                                    if (temp.equals(bookISBN[i])) {
                                                        duplicated = true;
                                                        duplicated_node = i; // record the duplicated entry
                                                        duplicatednum += 1;
                                                        break;
                                                    }
                                                }

                                                if (duplicated == true) {
                                                    System.out.println(
                                                            "\nNote: Book ISBN is duplicated. After you enter the order book quantity, we will help you combine the order.");
                                                    break; // break the while loop
                                                }
                                                else 
                                                    bookISBN[tracenum - duplicatednum - 1] = temp; // insert the non-duplicated ISBN into string array


                                                System.out.println("\nSuccess Message: Book ISBN is valid.\n");
                                                flagISBNcheck = true;
                                                break;
                                            } else {
                                                System.out.println(
                                                        "\nError Message: Book ISBN is invalid. Please enter the Book ISBN again.\n");
                                                int choice = returnkey(scanner);

                                                // Return to Shopping Cart
                                                if (choice == 1)
                                                    returnkey = true;

                                                else if (choice == 2)
                                                    continue;// Re-enter the request book Quantity

                                                if (returnkey)
                                                    break;// return to Shopping Cart

                                            }
                                        } else {
                                            System.out.println(
                                                    "\nError Message: Book ISBN is invalid. Please enter the Book ISBN again.\n");
                                            // Return to Shopping Cart
                                            int choice = returnkey(scanner);
                                            if (choice == 1)
                                                returnkey = true;

                                            else if (choice == 2)
                                                continue;// Re-enter the request book Quantity

                                            if (returnkey)
                                                break;// return to Shopping Cart

                                        }

                                        if (returnkey)
                                            break;
                                    }

                                    if (returnkey)
                                        break;

                                    while (true) {
                                        int tempQuan = 0;
                                        boolean quantityavailabilitycheck = false;
                                        System.out.print("\nEnter your request book Quantity: ");
                                        if(scanner.hasNextInt()){
                                            tempQuan = scanner.nextInt();
                                            scanner.nextLine();
                                        }
                                            
                                        else{
                                            scanner.next();
                                            System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter a positive integer.");
                                            int choice = returnkey(scanner);
                                            if (choice == 1)
                                                    returnkey = true;

                                            else if (choice == 2)
                                                continue;// Re-enter the request book Quantity

                                            if (returnkey)
                                                break;// return to Shopping Cart
                                        }
                    
                                        if (tracenum == 1 && tempQuan > 0) {//
                                            quantityavailabilitycheck = checkbookQuantity(bookISBN[tracenum - 1],
                                                    tempQuan, conn);
                                            if (quantityavailabilitycheck) {
                                                bookQuantity[tracenum - 1] = tempQuan;
                                                break;
                                            } else {// unfinised
                                                System.out.println("\nNote: Inventory Shortage. Sorry you cannot order this book.\n");
                                                int choice = returnkey(scanner);
                                                if (choice == 1)
                                                    returnkey = true;

                                                else if (choice == 2)
                                                    continue;// Re-enter the request book Quantity

                                                if (returnkey)
                                                    break;// return to Shopping Cart
                                            }
                                        }
                                        if (tracenum != 1 && tempQuan > 0) {
                                            // If the order book ISBN is already entered
                                            if (duplicated == true) {
                                                quantityavailabilitycheck = checkbookQuantity(bookISBN[duplicated_node],
                                                        bookQuantity[duplicated_node] + tempQuan, conn);
                                                if (quantityavailabilitycheck) {
                                                    bookQuantity[duplicated_node] += tempQuan;
                                                    System.out.println(
                                                            "\nSuccess Message:Input of the quantity of duplicated order is finished.\n");
                                                } else {
                                                    System.out.println("\nERROR Message:Inventory Shortage.\n");
                                                    int choice = returnkey(scanner);

                                                    // Return to Shopping Cart
                                                    if (choice == 1) {
                                                        returnkey = true;
                                                    } else if (choice == 2) {
                                                        continue;// Re-enter the request book Quantity
                                                    }

                                                    if (returnkey)
                                                        break;// return to Shopping Cart

                                                }
                                            } else {// If the order book ISBN is not entered
                                                quantityavailabilitycheck = checkbookQuantity(
                                                        bookISBN[tracenum - duplicatednum - 1], tempQuan, conn);
                                                if (quantityavailabilitycheck) {
                                                    bookQuantity[tracenum - duplicatednum - 1] = tempQuan;
                                                    System.out.println(
                                                            "\nSuccess Message: Book Quantity input is valid.\n");
                                                } else {
                                                    System.out.println(
                                                            "\nERROR Message:Inventory Shortage. Please enter again.\n");

                                                    int choice = returnkey(scanner);

                                                    // Return to Shopping Cart
                                                    if (choice == 1) {
                                                        returnkey = true;
                                                    } else if (choice == 2) {
                                                        continue;// Re-enter the request book Quantity
                                                    }

                                                    if (returnkey)
                                                        break;// return to Shopping Cart
                                                }

                                            }
                                            orderbookquantitycheck = true;
                                            break;
                                        } else {
                                            System.out.println(
                                                    "\nError Message: Book Quantity is invalid. Please enter the Book ISBN again.\n");
                                            int choice = returnkey(scanner);

                                            // Return to Shopping Cart
                                            if (choice == 1) {
                                                returnkey = true;
                                            } else if (choice == 2) {
                                                continue;// Re-enter the request book Quantity
                                            }

                                            if (returnkey)
                                                break;// return to Shopping Cart
                                        }
                                    }

                                    if (returnkey)
                                        break;// return to Shopping Cart

                                    tracenum += 1;
                                } while (tracenum <= counter);
                                
                                    if (tracenum >= counter && checkorderID) {
                                            System.out.println("The order is processing.\n");
                                            printOrderDetail(counter, duplicatednum, orderIDStr, userID, bookISBN,
                                                    bookQuantity, formatter.format(date), conn);
                                            updateDatabase(counter, duplicatednum, orderIDStr, userID, bookISBN,
                                                    bookQuantity, formatter.format(date), conn);
                                            
                                        break;
                                    }
                   
                                if (returnkey)
                                    break;// return to Shopping Cart
                            }

                            if (returnkey)
                                continue;
                            // User stay or leave the page
                            int useroption = 0;
                            System.out.println(
                                    "\nYou may:\n1. Stay at this page\n2. Return to the General User Main Page\n");

                            // Accept user input
                            while (true) {        
                                System.out.print("Enter your choice: ");
                                String input = scanner.nextLine();
                                // Check if the input is correct.
                                try {
                                    useroption = Integer.parseInt(input);
                                    if (useroption == 1 || useroption == 2) {
                                        break;
                                    } else {
                                        System.out.println("\nError Message: Invalid Input. Please enter again.\n");
                                    }
                                } catch (NumberFormatException error) {
                                    System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter an integer 1 or 2.");
                                }
                            }
                            // User are allowed to stay or leave the page
                            if (useroption == 1)
                                continue;// stay
                            else
                                break;// leave

                        }
                    }

                    else if (field2 == 3) {
                        scanner.nextLine();
                        System.out.println("\n----- Welcome to check your history orders -----\n" +
                                "In order to check your history orders, you need to enter your user ID below.\n");
                        boolean returnkey = false;
                        boolean returnEnterKey = false;
                        while (true) {
                            returnkey = false;
                            returnEnterKey = false;
                            String userID = "";
                            boolean flagID = false;
                            do {
                                System.out.print("Enter your user ID: ");
                                try {
                                    userID = scanner.nextLine();
                                } catch (Exception error) {
                                    error.getStackTrace();
                                    System.err.println("Message of Exception: " + error.getMessage());
                                    continue;
                                }

                                if (userID != "" && userID != null && userID.length() <= 10) {
                                    flagID = true;
                                } else {
                                    System.out.println("\nInvalid user ID. Please enter again.\n");
                                    System.out.println("You may: \n1. Enter the input again\n2. Return to the General User Main Page\n");
                                    int choice = -1;
                                    while(true){
                                        System.out.print("Enter your choice: ");
                                        if(scanner.hasNextInt()){
                                            choice = scanner.nextInt();
                                            scanner.nextLine();
                                        }
                                        else{
                                            scanner.next();
                                            System.out.println("\nWarning: Numerical data overflow occurs. Please enter your choice again.\n");
                                            continue;
                                        }
                                        if(choice == 1){
                                            flagID = false;
                                            break;
                                        }
                                        else if(choice == 2){
                                            returnkey = true;
                                            break;
                                        }
                                    }
                                    if(returnkey)
                                        break; 
                                }
                                
                            } while (flagID == false);
                            if(returnkey)
                                break;
                            // Check Order History Process
                            checkOrderHistory(userID, conn);

                            // User stay or leave the page
                            int useroption = 0;
                            System.out.println(
                                    "\nYou may:\n1. Stay at this page\n2. Return to the General User Main Page\n");

                            // Accept user input
                            
                            while (true) {        
                                System.out.print("Enter your choice: ");
                                String input = scanner.nextLine();
                                // Check if the input is correct.
                                try {
                                    useroption = Integer.parseInt(input);
                                    if (useroption == 1 || useroption == 2) {
                                        break;
                                    } else {
                                        System.out.println("\nError Message: Invalid Input. Please enter again.\n");
                                    }
                                } catch (NumberFormatException error) {
                                    System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter an integer 1 or 2.");
                                }
                            }

                            // User are allowed to stay or leave the page
                            if (useroption == 1)
                                continue;// stay
                            else
                                break;// leave
                        }

                    }

                    else if (field2 == 4) {
                        System.out.println("\nReturning to Main Menu\n");
                        flag2 = false;
                        break;
                    } else {
                        System.err.println("\nInvalid Input. Please enter again.");
                    }
                }

            } else if (field == 3) {
                if(checkExist(conn) !=true){
                    System.out.println("\nDatabase is not available. Please contact the admin.\n You will be redirected to the Main Menu\n");
                    continue;
                }

                // Perform Employees and Managers actions
                boolean flag3 = true;
                while (flag3) {
                    int field3 = 0;
                    // Accept user input
                            
                    while (true) {
                        System.out.println("\nThe current date and time is "+ formatter1.format((System.currentTimeMillis())) + "\n"+
                        "----- Employees and Managers Interface: Main Page -----\n" +
                                "There are 4 types of employees and managers actions:\n" +
                                "1. Order Update\n" + "2. Order Query\n" + "3. N Most Popular Books\n" + "4. Overview of all the database records\n"
                                + "5. Back to Main Menu\n");
                        System.out.print("Select your action: ");
                        // Check if the input is correct.
                        String input = scanner.nextLine();
                        try {
                            field3 = Integer.parseInt(input);
                            if (field3 >= 1 && field3<=5) {
                                break;
                            } else {
                                System.out.println("\nError Message: Invalid Input. Please enter again.\n");
                            }
                        } catch (NumberFormatException error) {
                            System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter an integer 1-5.");
                        }
                    }

                    if(field3 == 1){
                        displayAllOrders(conn);
                        boolean exitMenu = false;
                        String orderID = "";
                        boolean flagOIDcheck = false;
                        // Input User ID
                        while(exitMenu == false){
                            do{
                                System.out.print("Enter the order ID to be updated: ");
                                try{
                                    orderID = scanner.nextLine();
                                }catch(Exception error){
                                    error.getStackTrace();
                                    System.err.println("Message of Exception: " + error.getMessage());
                                    continue;
                                }
                                if(orderID != "" && orderID != null && orderID.length() <= 10){
                                    boolean checkID = checkOrderID(orderID, conn);
                                    if(checkID == true){
                                        System.out.println("\nSuccess Message: Order ID is valid. Please choose the new shipping status below.\n");
                                        flagOIDcheck = true;
                                        break;
                                    }
                                    else{
                                        System.out.println("\nError Message: Order ID is invalid. Please enter the order ID again.\n"); 
                                    }
                                }
                                else{
                                    System.out.println("\nError Message: Order ID is invalid. Please enter the order ID again.\n");
                                }
    
                            }while(flagOIDcheck == false);
                            int selectStatusfield = 0;
                            while (true) {
                                System.out.println("\nWhich shipping status would you like to update?(Input an integer 1-5)\n"+
                                "1. ordered\n"+
                                "2. shipped\n"+
                                "3. received\n"+
                                "4. Re-enter orderID\n"+
                                "5. Return to employee main page");
                                System.out.print("Enter your choice: ");
                                // Check if the input is correct.
                                String input = scanner.nextLine();
                                try {
                                    selectStatusfield = Integer.parseInt(input);
                                    break;
                                } catch (NumberFormatException error) {
                                    System.err.println("Message of Exception: Invalid input/Overflow occurs. Please enter an integer 1-5.");
                                }
                            }
                            if (selectStatusfield >= 1 && selectStatusfield<=3) {
                                updateOrder(orderID, selectStatusfield, conn);
                            } 
                            else if(selectStatusfield == 4){
                                flagOIDcheck = false; //update the boolean for checking the new OID input
                                continue;
                            }
                            else if(selectStatusfield == 5){
                                exitMenu = true;
                                continue;
                            }
                            else {
                                System.out.println("\nError Message: Invalid Input. Please enter again.\n");
                            }

                            // ask if the user wants to update another order ID or leave the menu
                            System.out.println("Do you want to update another order?");
                            System.out.println("1. Yes");
                            System.out.println("2. No");
                            int choice = 0;
                            while(true){
                                System.out.print("Enter your choice: ");
                                String input = scanner.nextLine();
                                try {
                                    choice = Integer.parseInt(input);
                                    if (choice == 1) {
                                        flagOIDcheck = false; //update the boolean for checking the new OID input
                                        break;//stay
                                    } else if (choice == 2) {
                                        exitMenu = true;
                                        break;//leave
                                    } else {
                                        System.out.println("\nError Message: Input is invalid. Please enter an integer 1 or 2.\n");
                                        continue;//re-enter 
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid input/Overflow occurs. Please enter an integer 1 or 2.");
                                }
                                
                            }
                        }
                        
                    }
                    else if(field3 == 2){
                        OrderQuery(conn); //query all the orders
                    }
                    else if(field3 == 3){
                        while (true) {
                            System.out.print("Enter the number of most popular books to retrieve: ");
                            String input = scanner.nextLine();
                            // Check if the input N is an integer.
                            try {
                                int n = Integer.parseInt(input);
                                if (n == 0) {
                                    System.out.println("The result is an empty.");
                                    break;
                                } else if (n > 0) {
                                    System.out.println("Retrieving " + n + " most popular books...");
                                    nMostPopularBook(n, conn);
                                    break;
                                } else {
                                    System.out.println("Invalid input. Please enter a non-negative integer.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input/Overflow occurs. Please enter a non-negative integer.");
                            }
                        }
                    }
                    else if(field3 == 4){
                        printAllTable(conn);
                    }
                    else if(field3 == 5){
                        System.out.println("\nReturning to Main Menu\n");
                        flag3 = false;
                        break;
                    }
                    else{
                        System.err.println("\nInvalid Input. Please enter again.\n");
                    }
                }
            }

            else if (field == 4) {
                // Exit the program
                System.out.println("Thank you for browsing our bookstore.");
                flag = false;
                break;
            } else {
                // Error checking
                System.err.println("\nInvalid Input. Please enter again.\n");
            }
        }

        scanner.close();
        conn.close();
    }

    private static void InsertCSV(Connection conn) throws IOException, SQLException {
        BufferedReader br = new BufferedReader(new FileReader("source.csv"));
        
        List<String[]> records = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(";");
            records.add(values);
        }
        br.close();

        List<String[]> books = new ArrayList<>();
        List<String[]> customers = new ArrayList<>();
        List<String[]> orders = new ArrayList<>();

        for (String[] record : records) {
            String recordType = record[0];
            switch (recordType) {
                case "Book":
                    books.add(record);
                    break;
                case "Customer":
                    customers.add(record);
                    break;
                case "ORDERS":
                    orders.add(record);
                    break;
                default:
                    break;
            }
        }

        // Hash set for checking duplication of authors
        Set<String> authorSet = new HashSet<String>();
        List<String[]> authors = new ArrayList<String[]>();
        for (String[] book : books) {
            for (int i = 5; i < book.length; i += 2) {
                String[] authorAid = { book[i], book[i + 1] };
                if (!authorSet.contains(book[i])) {
                    authorSet.add(book[i]);
                    authors.add(authorAid);
                }
            }
        }

        List<String[]> bookAuthors = new ArrayList<String[]>();
        for (String[] book : books) {
            for (int i = 6; i < book.length; i += 2) {
                String[] isbnAuthor = { book[1], book[i] };
                bookAuthors.add(isbnAuthor);
            }
        }

        // insert book records
        try {
            Statement stmt = conn.createStatement();
            for (String[] book : books) {
                String sqlInsertBook = "INSERT INTO Book (ISBN, Title, Price, Inventory_Quantity) " +
                        "VALUES " + "(" + book[1] + "," + book[2] + "," + book[3] + "," + book[4] + ")";
                stmt.executeUpdate(sqlInsertBook);
            }
            stmt.close();
            System.out.println("BOOK inserted successfully.");
        } catch (Exception error) {
            System.err.println("Message of Exception: " + error.getMessage());
        }

        // insert customer records
        try {
            Statement stmt = conn.createStatement();
            for (String[] customer : customers) {
                String sqlInsertCustomer = "INSERT INTO Customer (CID, Name, Address) " +
                        "VALUES " + "(" + customer[1] + "," + customer[2] + "," + customer[3] + ")";
                stmt.executeUpdate(sqlInsertCustomer);
            }
            stmt.close();
            System.out.println("CUSTOMER inserted successfully.");
        } catch (Exception error) {
            System.err.println("Message of Exception: " + error.getMessage());
        }

        // insert order records
        try {
            Statement stmt = conn.createStatement();
            for (String[] order : orders) {
                String sqlInsertOrder = "INSERT INTO ORDERS (OID, CID, Order_Date, Order_ISBN, Order_Quantity, Shipping_Status) "
                        + "VALUES " + "(" + order[1] + "," + order[2] + ", TO_DATE(" + order[3] +
                        ", 'YYYY-MM-DD HH24:MI:SS')," + order[4] + "," + order[5] + "," + order[6] + ")";
                // System.out.println(sqlInsertOrder);
                stmt.executeUpdate(sqlInsertOrder);
            }
            stmt.close();
            System.out.println("ORDERS inserted successfully.");
        } catch (Exception error) {
            System.err.println("Message of Exception: " + error.getMessage());
        }

        // insert authors records
        try {
            Statement stmt = conn.createStatement();
            for (String[] author : authors) {
                String sqlInsertAuthor = "INSERT INTO Authors (ANAME, AID) " + "VALUES " +
                        "(" + author[0] + "," + author[1] + ")";
                // System.out.println(sqlInsertAuthor);
                stmt.executeUpdate(sqlInsertAuthor);
            }
            stmt.close();
            System.out.println("AUTHORS inserted successfully.");
        } catch (Exception error) {
            System.err.println("Message of Exception: " + error.getMessage());
        }

        // insert include relation
        try {
            Statement stmt = conn.createStatement();
            for (String[] order : orders) {
                String sqlInsertInclude = "INSERT INTO include (OID, ISBN, Quantity) " + "VALUES " +
                        "(" + order[1] + "," + order[4] + "," + order[5] + ")";
                // System.out.println(sqlInsertInclude);
                stmt.executeUpdate(sqlInsertInclude);
            }
            stmt.close();
            System.out.println("INCLUDE inserted successfully.");
        } catch (Exception error) {
            System.err.println("Message of Exception: " + error.getMessage());
        }

        // // insert place relation
        try {
            Statement stmt = conn.createStatement();
            for (String[] order : orders) {
                String sqlInsertPlace = "INSERT INTO place (OID, CID, ORDER_ISBN) " + "VALUES" +
                        "(" + order[1] + "," + order[2] + "," + order[4] + ")";
                // System.out.println(sqlInsertPlace);
                stmt.executeUpdate(sqlInsertPlace);
            }
            stmt.close();
            System.out.println("PLACE inserted successfully.");
        } catch (Exception error) {
            System.err.println("Message of Exception: " + error.getMessage());
        }

        // insert writtenby relation
        try {
            Statement stmt = conn.createStatement();

            for (String[] bookAuthor : bookAuthors) {
                String sqlInsertWrittenBy = "INSERT INTO writtenby (ISBN, AID) " + "VALUES " +
                        "(" + bookAuthor[0] + "," + bookAuthor[1] + ")";
                // System.out.println(sqlInsertWrittenBy);
                stmt.executeUpdate(sqlInsertWrittenBy);
            }
            stmt.close();
            System.out.println("WRITTENBY inserted successfully.");
        } catch (Exception error) {
            System.err.println("Message of Exception: " + error.getMessage());
        }
        

    }

    public static void printAllTable(Connection conn) throws SQLException{
        // Print book
        Statement stmtBook = conn.createStatement();
        ResultSet rsBook = stmtBook.executeQuery("SELECT * FROM book");
        System.out.println("\nRecords in Book Database:\n"+
                "---------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-15s | %-100s | %-5s | %-20s |\n", "ISBN", "Title",
                "Price", "Inventory Quantity");
        System.out.println(
                "---------------------------------------------------------------------------------------------------------------------------------------------------------");
        while (rsBook.next()) {
            String isbn = rsBook.getString("isbn");
            String title = rsBook.getString("title");
            int price = rsBook.getInt("price");
            int quantity = rsBook.getInt("Inventory_Quantity");
            System.out.printf("| %-15s | %-100s | %-5d | %-20d |\n", isbn, title, price,
                    quantity);
        }
        System.out.println(
                "---------------------------------------------------------------------------------------------------------------------------------------------------------");
        rsBook.close();
        stmtBook.close();
        System.out.println();

        //Print author
        Statement stmtAuthor = conn.createStatement();
        ResultSet rsAuthor = stmtAuthor.executeQuery("SELECT * FROM Authors");
        System.out.println("\nRecords in Author Database:\n"+"--------------------------------------------------------------");
        System.out.printf("| %-5s | %-50s |\n", "AID", "Author Name");
        System.out.println("--------------------------------------------------------------");
        while (rsAuthor.next()) {
            String aid = rsAuthor.getString("aid");
            String authorName = rsAuthor.getString("aname");
            System.out.printf("| %-5s | %-50s |\n", aid, authorName);
        }
        System.out.println("--------------------------------------------------------------");
        stmtAuthor.close();
        rsAuthor.close();
        System.out.println();

        //Print customer
        System.out.println("\nRecords in Customers Database: \n");        
        Statement stmtAddressCount = conn.createStatement();
        ResultSet rsAddressCount = stmtAddressCount.executeQuery("SELECT address FROM customer");
        
        ArrayList<String> addressList = new ArrayList<String>();
        
        while (rsAddressCount.next()) {
            String customerAddress = rsAddressCount.getString("address");
            addressList.add(customerAddress);
        }
        int maxLength = 0;
        for (String str : addressList) {
            if (str.length() > maxLength) {
                maxLength = str.length();
            }
        }  
        stmtAddressCount.close();
        rsAddressCount.close();
                
        Statement stmtCustomer = conn.createStatement();
        ResultSet rsCustomer = stmtCustomer.executeQuery("SELECT * FROM customer");
        
        for (int i = 0; i < maxLength+70; i++) {
            System.out.print("-");
        }
        System.out.println();
        System.out.printf("| %-10s | %-50s | %-"+maxLength+"s |\n", "CID", "Customr Name","Address");
        for (int i = 0; i < maxLength+70; i++) {
            System.out.print("-");
        }
        System.out.println();
        
        while (rsCustomer.next()) {
            String cid = rsCustomer.getString("cid");
            String customerName = rsCustomer.getString("name");
            String customerAddress = rsCustomer.getString("address");
            System.out.printf("| %-10s | %-50s | %-"+maxLength+"s |\n", cid, customerName, customerAddress);
        }
        for (int i = 0; i < maxLength+70; i++) {
            System.out.print("-");
        }
        System.out.println();
        stmtCustomer.close();
        rsCustomer.close();
        System.out.println();
        
        //Print order
        Statement stmtOrder = conn.createStatement();
        // Execute a query to retrieve the table data
        ResultSet rsOrder = stmtOrder.executeQuery("SELECT * FROM orders");
        // Print the table data
        System.out.println("\nRecords in Orders Database:\n"+"------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-8s | %-10s | %-10s | %-15s | %-20s | %-20s |\n","OID", "CID", "Order Date", "Order ISBN", "Order Quantity", "Shipping Status");
        System.out.println("------------------------------------------------------------------------------------------------------");
        while (rsOrder.next()) {
            String oid = rsOrder.getString("oid");
            String cid = rsOrder.getString("cid");
            String orderDateStr = rsOrder.getString("order_date");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDate orderDate = LocalDate.parse(orderDateStr, formatter);
            String formattedDate =
            orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String orderISBN = rsOrder.getString("order_isbn");
            int quantity = rsOrder.getInt("order_Quantity");
            String status = rsOrder.getString("Shipping_Status");
            System.out.printf("| %-8s | %-10s | %-10s | %-15s | %-20s | %-20s |\n", oid, cid, formattedDate, orderISBN, quantity, status );
        }
        System.out.println("------------------------------------------------------------------------------------------------------");
        stmtOrder.close();
        rsOrder.close();
        System.out.println();

        // Print include
        Statement stmtInclude = conn.createStatement();
        // Execute a query to retrieve the table data
        ResultSet rsInclude = stmtInclude.executeQuery("SELECT * FROM include");
        // Print the table data
        System.out.println("\nRecords in Include Database:\n"+"----------------------------------------------------------");
        System.out.printf("| %-8s | %-20s | %-20s |\n", "OID", "ISBN", "Order Quantity");
        System.out.println("----------------------------------------------------------");
        while (rsInclude.next()) {
            String oid = rsInclude.getString("oid");
            String orderISBN = rsInclude.getString("isbn");
            int quantity = rsInclude.getInt("Quantity");
            System.out.printf("| %-8s | %-20s | %-20d |\n", oid, orderISBN, quantity);
        }
        System.out.println("----------------------------------------------------------");
        stmtInclude.close();
        rsInclude.close();
        System.out.println();

        // Print place
        Statement stmtPlace = conn.createStatement();
        // Execute a query to retrieve the table data
        ResultSet rsPlace = stmtPlace.executeQuery("SELECT * FROM place");
        // Print the table data
        System.out.println("\nRecords in Place Database:\n"+"-------------------------------------------");
        System.out.printf("| %-10s | %-8s | %-15s |\n", "CID", "OID", "Order ISBN");
        System.out.println("-------------------------------------------");
        while (rsPlace.next()) {
            String cid = rsPlace.getString("cid");
            String oid = rsPlace.getString("oid");
            String order_isbn = rsPlace.getString("order_isbn");
            System.out.printf("| %-10s | %-8s | %-15s |\n", cid, oid, order_isbn);
        }
        System.out.println("-------------------------------------------");
        stmtPlace.close();
        rsPlace.close();
        System.out.println();

        // Print writtenby
        Statement stmtWrittenby = conn.createStatement(); // Execute a query to retrieve the table data
        ResultSet rsWrittenby = stmtWrittenby.executeQuery("SELECT * FROM writtenby");
        // Print the table data
        System.out.println("\nRecords in Writtenby Database:\n"+"---------------------------");
        System.out.printf("| %-15s | %-5s |\n", "ISBN", "AID");
        System.out.println("---------------------------");
        while (rsWrittenby.next()) {
            String isbn = rsWrittenby.getString("isbn");
            String aid = rsWrittenby.getString("aid");
            System.out.printf("| %-15s | %-5s |\n", isbn, aid);
        }
        System.out.println("---------------------------");
        stmtWrittenby.close();
        rsWrittenby.close();
        System.out.println();

    }

    public static void searchbook(int searchfield, String searchkw, Connection conn) throws SQLException {
        // System.out.println("Successfully entering the search process");

        // Search by ISBN
        String sql1 = "SELECT B.ISBN, B.Title, LISTAGG(A.aname, ', ') WITHIN Group (ORDER BY A.aname) As NameOfAuthors, B.Price ,B.Inventory_Quantity "
                +
                "From Book B " +
                "LEFT OUTER JOIN Writtenby W ON (B.ISBN = W.ISBN) " +
                "LEFT OUTER JOIN Authors A ON (W.aid = A.aid) " +
                "WHERE (A.aname is NOT NULL) AND (? is NOT NULL) AND (B.ISBN = ?) " +
                "GROUP BY B.ISBN, B.Title, B.Price, B.Inventory_Quantity ";
        // LISTAGG function is used to concatenates values of the "aname" column of
        // authors

        // Search by Book Title
        String sql2 = "SELECT B.ISBN, B.Title, LISTAGG(A.aname, ', ') WITHIN Group (ORDER BY A.aname) As NameOfAuthors, B.Price ,B.Inventory_Quantity "
                +
                "From Book B " +
                "Left Outer Join Writtenby W ON (B.ISBN = W.ISBN) " +
                "Left Outer Join Authors A ON (W.aid = A.aid) " +
                "Where (A.aname is NOT NULL) AND (? is NOT NULL) AND (B.Title Like ?) " +
                "GROUP BY B.ISBN, B.Title, B.Price, B.Inventory_Quantity ";
        // LISTAGG function is used to concatenates values of the "aname" column of
        // authors

        // Search by Author Name
        String sql3 = "SELECT B.ISBN, B.Title, LISTAGG(A.aname, ', ') WITHIN Group (ORDER BY A.aname) As NameOfAuthors, B.Price ,B.Inventory_Quantity "
                +
                "From Book B " +
                "Left Outer Join Writtenby W ON (B.ISBN = W.ISBN) " +
                "Left Outer Join Authors A ON (W.aid = A.aid) " +
                "Where (A.aname is NOT NULL) AND (? is NOT NULL) AND (A.aname Like ?) " +
                "GROUP BY B.ISBN, B.Title, B.Price, B.Inventory_Quantity ";
        // LISTAGG function is used to concatenates values of the "aname" column of
        // authors

        PreparedStatement pstmt1 = conn.prepareStatement(sql1), pstmt2 = conn.prepareStatement(sql2),
                pstmt3 = conn.prepareStatement(sql3);
        ResultSet rs = null, rs1 = null;

        // Query Execution
        if (searchfield == 1) {
            pstmt1.setString(1, searchkw);
            pstmt1.setString(2, searchkw);
            rs = pstmt1.executeQuery();
        } else if (searchfield == 2) {
            pstmt2.setString(1, searchkw);
            pstmt2.setString(2, "%" + searchkw + "%");
            rs = pstmt2.executeQuery();
        }

        else if (searchfield == 3) {
            pstmt3.setString(1, searchkw);
            pstmt3.setString(2, "%" + searchkw + "%");
            rs = pstmt3.executeQuery();
        }

        // Print the search results
        if (rs.next()) {
            String authornamelist = "";
            System.out.println("\nSearch Results:");
            displaySearchTitle(rs, conn);

            do {
                String isbn = rs.getString("ISBN");
                String title = rs.getString("TITLE");
                int price = rs.getInt("PRICE");
                int inventory_quantity = rs.getInt("INVENTORY_QUANTITY");
                String name_authors = rs.getString("NAMEOFAUTHORS");

                // Handle search by author name (Find out all authors related to the book)
                if (searchfield == 3) {
                    PreparedStatement pstmt4 = conn.prepareStatement(
                            "SELECT LISTAGG(A.aname, ', ') WITHIN Group (ORDER BY A.aname) As NameOfAuthors FROM Book B, Writtenby W, Authors A Where B.ISBN = ? AND B.ISBN = W.ISBN AND W.aid = A.aid");
                    pstmt4.setString(1, isbn);
                    rs1 = pstmt4.executeQuery();
                    while (rs1.next()) {
                        authornamelist += rs1.getString("NAMEOFAUTHORS");
                    }
                }

                // Print out the results if user searched by book ISBN or book title
                if (searchfield == 1 || searchfield == 2) {
                    displaySearchData(name_authors, isbn, title, price, inventory_quantity);
                }

                // Print out the results if user search by the book author name
                else if (searchfield == 3) {
                    displaySearchData(authornamelist, isbn, title, price, inventory_quantity);
                }

            } while (rs.next());
        } else {
            System.out.println("No book is found. Please search again."); // Print out message if no book is found.
        }
    }

    private static void displaySearchTitle(ResultSet rs, Connection conn) throws SQLException {
        String isbn = rs.getString("ISBN");
        String authornamelist = "";
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT LISTAGG(A.aname, ', ') WITHIN Group (ORDER BY A.aname) As NameOfAuthors FROM Book B, Writtenby W, Authors A Where B.ISBN = ? AND B.ISBN = W.ISBN AND W.aid = A.aid");
        pstmt.setString(1, isbn);
        rs = pstmt.executeQuery();
        while (rs.next()) {
            authornamelist += rs.getString("NAMEOFAUTHORS");
        }
        String[] result = separateAuthorNames(authornamelist);

        if (result.length == 1)
            System.out.printf("| %-15s | %-100s | %-5s | %-18s | %-50s    |\n", "ISBN", "Title", "Price",
                    "Inventory Quantity", "Authors");
        else if (result.length > 1)
            System.out.printf(
                    "(Note that if a book is written by more than 1 authors, the author names will be shown in (#)AuthorName.)\n"
                            + "| %-15s | %-100s | %-5s | %-18s | %-50s    |\n",
                    "ISBN", "Title", "Price", "Inventory Quantity", "Authors");
    }

    // Separate the author name string into different lines
    private static String[] separateAuthorNames(String str) {
        String[] result = String.format("%-50s", str).split(", ");
        return result;
    }

    // Display the results
    private static void displaySearchData(String str, String isbn, String title, int price, int inventory_quantity) {
        String[] result = separateAuthorNames(str);

        for (int i = 0; i < result.length; i++) {
            if (i == 0 && result.length != 1) {
                System.out.printf("| %-15s | %-100s | %-5d | %-18d |(%d) %-50s |\n", isbn, title, price,
                        inventory_quantity, i + 1, result[0]);
            } else if (i == 0 && result.length == 1) {
                System.out.printf("| %-15s | %-100s | %-5d | %-18d | %-50s    |\n", isbn, title, price, inventory_quantity,
                        result[0]);
            } else if (result.length > 1) {
                System.out.printf("| %-15s | %-100s | %-5s | %-18s |(%d) %-50s |\n", "", "", "", "", i + 1, result[i]);
            }
        }
    }

    public static void checkOrderHistory(String userID, Connection conn) throws SQLException {
        System.out.println("Successfully enter the function");

        // Prepare SQL Statement to search the required user order history
        String sql = "Select O.OID, O.CID, O.Order_date, O.Order_ISBN, O.Order_Quantity, O.Shipping_Status " +
                "From Orders O " +
                "Left Outer Join Place P ON (O.CID = P.CID) " +
                "Left Outer Join CUSTOMER C ON (P.CID = C.CID) " +
                "Where C.CID = O.CID AND C.CID = ? " +
                "GROUP BY O.OID, O.CID, O.Order_date, O.Order_ISBN, O.Order_Quantity, O.Shipping_Status " +
                "Order BY O.OID ASC "; // Sort the OID in the ascending order

        PreparedStatement pstmt1 = conn.prepareStatement(sql);
        pstmt1.setString(1, userID);
        ResultSet rs = pstmt1.executeQuery();
        if (rs.next()) {
            System.out.printf("\nOrder History:\nYour User ID: %s\n" + "| %-10s | %-25s | %-15s | %-20s | %-20s |\n",
                    userID, "OID", "Order Date", "Order ISBN", "Order Quantity", "Shipping Status");
            do {
                String orderID = rs.getString("OID");
                String orderDATE = rs.getString("ORDER_DATE");
                String orderISBN = rs.getString("ORDER_ISBN");
                String orderInventory = rs.getString("Order_Quantity");
                String orderShippingSt = rs.getString("Shipping_Status");

                displayOrderHistoryData(orderID, orderDATE, orderISBN, orderInventory, orderShippingSt);

            } while (rs.next());
        } else {
            System.out.println("\nNo order history. You did not place any order.");
        }
    }

    // Display Order History
    private static void displayOrderHistoryData(String orderID, String orderDATE, String orderISBN,
            String orderInventory, String orderShippingSt) {
        System.out.printf("| %-10s | %-25s | %-15s | %-20s | %-20s |\n", orderID, orderDATE, orderISBN, orderInventory,
                orderShippingSt);
    }

    private static boolean checkUserID(String userID, Connection conn) throws SQLException {
        String sql = "Select * " +
                "From CUSTOMER C " +
                "Where C.CID = ?";
        PreparedStatement pstmt1 = conn.prepareStatement(sql);
        pstmt1.setString(1, userID);
        ResultSet rs = pstmt1.executeQuery();
        if (!rs.next()) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkbookISBN(String isbn, Connection conn) throws SQLException {
        String sql = "Select * " +
                "From Book B " +
                "Where B.ISBN = ? ";
        PreparedStatement pstmt1 = conn.prepareStatement(sql);
        pstmt1.setString(1, isbn);
        ResultSet rs = pstmt1.executeQuery();
        if (rs.next()) {
            String bookISBN = rs.getString("ISBN");
            System.out.printf("\nSuccess Message: The book ISBN %s is valid.\n", bookISBN);
            return true;
        } else
            return false;
    }

    private static boolean checkbookQuantity(String isbn, int quantity, Connection conn) throws SQLException {
        String sql = "Select * " +
                "From Book B " +
                "Where B.ISBN = ? AND B.Inventory_Quantity >= ? ";
        PreparedStatement pstmt1 = conn.prepareStatement(sql);
        pstmt1.setString(1, isbn);
        pstmt1.setInt(2, quantity);
        ResultSet rs = pstmt1.executeQuery();
        if (rs.next()) {
            String bookISBN = rs.getString("ISBN");
            System.out.printf("\nSuccess Message: The book ISBN %s are still available for ordering.\n", bookISBN);
            return true;
        } else
            return false;
    }

    private static void printOrderDetail(int counter, int duplicatednum, String oID, String userID, String[] bookISBN,
            int[] bookQuantity, String date, Connection conn) {
        System.out.println("Your order details:");
        System.out.printf("Your order ID is: %s\n", oID);
        System.out.printf("User ID: %s\n", userID);
        System.out.printf("Total number of orders: %d\n", counter - duplicatednum);
        if (duplicatednum > 0)
            System.out.printf("(Total number of duplicated orders: %d)\n", duplicatednum);
        System.out.printf("The order date is: %s\n", date);
        System.out.printf("\nOrder Book Details: \n|%-13s | %-15s |\n", "Book ISBN", "Book Quantity");

        for (int i = 0; i < bookISBN.length - duplicatednum; i++)
            System.out.printf("|%-13s | %-15d |\n", bookISBN[i], bookQuantity[i]);

    };

    private static int returnkeytemp(Scanner scanner, String title) {
        int choice = -1;
        while (true) {
            System.out.printf("Action:\n1. Return to select the search fields.\n2. Changing input: %s\n", title);
            System.out.print("\nInput your action: ");
            String input = scanner.nextLine();
            // Check if the input is an integer.
            try {
                choice = Integer.parseInt(input);
                if (choice == 1) {
                    System.out.println("\nNote: Returning to select the search fields.");
                    return choice;
                } else if (choice == 2) {
                    System.out.printf("\nNote: You are allowed to enter %s again.", title);
                    return choice;
                } else {
                    System.out.println("\nError Message: Invalid input. Please enter again");
                    continue;
                }
            } catch (NumberFormatException error) {
                System.err.println("\nMessage of Exception: Invalid input/Overflow occurs. Please enter an integer 1 or 2.\n");
            }
        }
    }


    private static int returnkey(Scanner scanner) {
        int choice = -1;
        while (true) {
            System.out.println("Action:\n1. Return to Shopping Cart Page\n2. Changing input");
            System.out.print("\nInput your action: ");
            if(scanner.hasNext()){
                choice = scanner.nextInt();
                scanner.nextLine();
            }
            else{
                scanner.next();
                System.out.println("\nInvalid input. Please enter again.\n");
                continue;
            }
            if (choice == 1) {
                System.out.println("\nNote: Returning to Shopping Cart Page.\n");
                return choice;
            } else if (choice == 2) {
                System.out.println("\nNote: You are allowed to enter again.\n");
                return choice;
            } else {
                System.out.println("\nError Message: Invalid input. Please enter again\n");
                continue;
            }
        }
    }

    // Generate order ID
    private static String orderID() {
        String str = "";
        Random randomnum = new Random();
        int count = 1;
        while(count <=8){
            str += randomnum.nextInt(10);
            count += 1;
        }
        return str;
    }

    // Check if the generated order ID is unique
    private static boolean checkUniqueOID(String str, Connection conn) throws SQLException {

        String sql = "Select * " +
                "From Orders " +
                "Where Orders.OID = ?";
        ResultSet rs1 = null;
        try {
            PreparedStatement pstmt1 = conn.prepareStatement(sql);
            pstmt1.setString(1, str);
            rs1 = pstmt1.executeQuery();
            if (rs1.next()) {
                return false;
            } else {
                return true;
            }
        } catch (Exception error) {
            error.getStackTrace();
            return false;
        } finally {
            if (rs1 != null) {
                try {
                    rs1.close();
                } catch (Exception error) {
                    error.getStackTrace();
                }
            }
        }

    }

    public static void updateDatabase(int counter, int duplicatednum, String oID, String userID, String[] bookISBN,
            int[] bookQuantity, String date, Connection conn) throws SQLException, ParseException {

        for (int i = 0; i < bookISBN.length - duplicatednum; i++) {// Insert Place 
            // Update the Book inventory quantity
            String sql4 = "INSERT INTO PLACE (CID, OID, ORDER_ISBN) " +
                "VALUES (?, ?, ?)";
    
            // Insert the information of order
            String sql5 = "INSERT INTO ORDERS (OID, CID, ORDER_DATE, ORDER_ISBN, ORDER_QUANTITY, SHIPPING_STATUS) " +
            "VALUES (?, ?, TO_DATE(?,'YYYY-MM-DD HH24:MI:SS'), ?, ?, ?)";
            
            try {
                // Insert Order information 
                PreparedStatement pstmt5 = conn.prepareStatement(sql5);
                pstmt5.setString(1, oID);
                pstmt5.setString(2, userID);
                pstmt5.setString(3, date);
                pstmt5.setString(4, bookISBN[i]);
                pstmt5.setInt(5, bookQuantity[i]);
                pstmt5.setString(6, "Ordered");
                int rs5 = pstmt5.executeUpdate();

                // Insert Place
                PreparedStatement pstmt4 = conn.prepareStatement(sql4);
                pstmt4.setString(1, userID);
                pstmt4.setString(2, oID);
                pstmt4.setString(3, bookISBN[i]);
                int rs4 = pstmt4.executeUpdate();
                
                if(rs4 > 0 && rs5>0){
                   
                    // get original inventory quantity
                    String sql1 = "Select B.Inventory_Quantity " +
                    "From Book B " +
                    "Where B.ISBN = ? ";

                    // update sql
                    String sql2 = "Update Book " +
                            "Set Book.Inventory_Quantity = Book.Inventory_Quantity - ? " +
                            "Where Book.ISBN = ? ";

                    // detect update
                    String sql3 = "Select B.Inventory_Quantity " +
                            "From Book B " +
                            "Where B.ISBN = ? ";
                    // Update the Book information if the order information has been placed
                    PreparedStatement pstmt1 = conn.prepareStatement(sql1);
                    pstmt1.setString(1, bookISBN[i]);
        
                    PreparedStatement pstmt2 = conn.prepareStatement(sql2);
                    pstmt2.setInt(1, bookQuantity[i]);
                    pstmt2.setString(2, bookISBN[i]);
        
                    PreparedStatement pstmt3 = conn.prepareStatement(sql3);
                    pstmt3.setString(1, bookISBN[i]);
        
                    ResultSet rs1 = pstmt1.executeQuery();
                    ResultSet rs2 = pstmt2.executeQuery();
                    ResultSet rs3 = pstmt3.executeQuery();
                
                    if (rs1.next() && rs3.next()) {
                        int originalQ = rs1.getInt("INVENTORY_QUANTITY");
                        int updatedQ = rs3.getInt("INVENTORY_QUANTITY");
                        checkUpdate(bookQuantity[i], originalQ, updatedQ);
                    }
                }

            } catch (Exception error) {
                error.getStackTrace();
                System.out.println("\nThe order cannot be placed.\n");
            }

        }

        System.out.println("\nSuccess Message: Order has been placed.\nThank you for ordering books.\n");
    };
    private static void checkUpdate(int adjustQ, int originalQ, int updatedQ) {
        if (originalQ - adjustQ == updatedQ) {
            System.out.println("\nBook Inventory Quantity updated successfully.\n");
        }
    };
    private static void displayOrderData(String orderID,String orderCID, String orderDATE, String orderISBN, String orderInventory, String orderShippingSt){
        System.out.printf("| %-10s | %-10s | %-25s | %-15s | %-20s | %-20s |\n", orderID, orderCID, orderDATE, orderISBN, orderInventory, orderShippingSt);
    }
    private static boolean checkOrderID(String orderID, Connection conn) throws SQLException{
        String sql = "Select * "+
        "From Orders O " +
        "Where O.OID = ?";
        PreparedStatement pstmt1 = conn.prepareStatement(sql);
        pstmt1.setString(1, orderID);
        ResultSet rs = pstmt1.executeQuery();
        if(!rs.next()){
            return false;
        }
        else{
            return true;
        }
    }

    private static void displayAllOrders(Connection conn) throws SQLException{
        String sql = "SELECT OID, CID, Order_Date, Shipping_Status, " +
        "LISTAGG(Order_ISBN || ' (' || Order_Quantity || ')', ', ') " +
        "WITHIN GROUP (ORDER BY Order_ISBN) AS \"Book_List (format:Book_ISBN(Quantity))\" " +
        "FROM ORDERS " +
        "GROUP BY OID, CID, Order_Date, Shipping_Status";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        // Retrieve the information of the most popular books.
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        int[] columnWidths = new int[columnCount];
        int[] initialWidths = new int[columnCount];
        if(rs.next()){
            do {
                for (int i = 1; i <= columnCount; i++) {
                    String columnValue = rs.getString(i);
                    if (columnValue != null) {
                        int maxLength = columnValue.length();
                        columnWidths[i - 1] = Math.max(maxLength, initialWidths[i - 1]);
                    }
                }
                initialWidths = columnWidths;
            }while(rs.next());
            // Print the column headers using the maximum width of each column
            System.out.printf("| %-5s", "No.");
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                columnWidths[i - 1] = Math.max(columnName.length(), initialWidths[i - 1]); //Check the column width with column name
                System.out.printf("| %-" + (columnWidths[i - 1]+3)+ "s", columnName);
            }
            System.out.println("|");
            // Close the current ResultSet
            rs.close();
        }else{
            System.out.println("\nThere is no order record.\n");
        }
        
        // Re-execute the query to reset the ResultSet
        rs = statement.executeQuery(sql);
        int number = 0;
        while (rs.next()) {
            number=number+1;
            System.out.printf("| %-5s", number);
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = rs.getString(i);
                if (columnValue != null) {
                    System.out.printf("| %-" +(columnWidths[i - 1]+3) + "s", columnValue);
                } else {
                    System.out.printf("| %-" + (columnWidths[i - 1]+3) + "s", "");
                }
            }
            System.out.println("|");
        }

        // Close the statement.
        rs.close();
        statement.close();
        
    }
    public static void updateOrder(String orderID, int updatedShippingStatusField, Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql;
        sql = "SELECT Shipping_Status FROM Orders WHERE OID = '" + orderID + "'";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            String newStatus = null;
            String originalStatus = rs.getString("Shipping_Status");
            if (originalStatus.equals("ordered")) {
                if (updatedShippingStatusField == 1) {
                    System.out.println("Fail to update! The ordered is already ordered!");
                } else if (updatedShippingStatusField == 2) {
                    newStatus = "shipped";
                    UpdateStatus(orderID, newStatus, conn);
                    System.out.println("Update successfully! The ordered is already shipped!");
                } else if (updatedShippingStatusField == 3) {
                    newStatus = "received";
                    UpdateStatus(orderID, newStatus, conn);
                    System.out.println("Update successfully! The ordered is already received!");
                }
            } else if (originalStatus.equals("shipped")) {
                if (updatedShippingStatusField == 1) {
                    newStatus = "ordered";
                    UpdateStatus(orderID, newStatus, conn);
                    System.out.println("Update successfully! The ordered is back to not shipped!");
                } else if (updatedShippingStatusField == 2) {
                    System.out.println("Fail to update! The ordered is already shipped!");
                } else if (updatedShippingStatusField == 3) {
                    newStatus = "received";
                    UpdateStatus(orderID, newStatus, conn);
                    System.out.println("Update successfully! The ordered is already received!");
                }
            } else if (originalStatus.equals("received")) {
                if (updatedShippingStatusField == 1) {
                    System.out.println("Update successfully! The ordered is back to ordered status!");
                } else if (updatedShippingStatusField == 2) {
                    System.out.println("Update successfully! The ordered is back to shipped status!");
                } else if (updatedShippingStatusField == 3) {
                    System.out.println("Fail to update! The ordered is already received!");
                }
            }
        }
    }
    public static void UpdateStatus(String orderID,String newStatus, Connection conn) throws SQLException{
        String sql = "UPDATE Orders SET Shipping_Status = ? WHERE OID = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, newStatus);
        pstmt.setString(2, orderID);
        int rowsUpdated = pstmt.executeUpdate();
        System.out.println(rowsUpdated + " row(s) updated.");
    }
    public static void OrderQuery(Connection conn) throws SQLException{
        Statement statement = conn.createStatement();
        System.out.println("Not Shipped Orders :");  //Shipping Status = 'ordered'
        String sql = "SELECT OID, CID, Order_Date, Shipping_Status, " +
        "LISTAGG(Order_ISBN || ' (' || Order_Quantity || ')', ', ') " +
        "WITHIN GROUP (ORDER BY Order_ISBN) AS \"Book_List (format:Book_ISBN(Quantity))\" " +
        "FROM ORDERS " +
        "WHERE Shipping_Status = 'ordered'"+
        "GROUP BY OID, CID, Order_Date, Shipping_Status";
        ResultSet rs = statement.executeQuery(sql);

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        int[] columnWidths = new int[columnCount];
        int[] initialWidths = new int[columnCount];
        if(rs.next()){
            do {
                for (int i = 1; i <= columnCount; i++) {
                    String columnValue = rs.getString(i);
                    if (columnValue != null) {
                        int maxLength = columnValue.length();
                        columnWidths[i - 1] = Math.max(maxLength, initialWidths[i - 1]);
                    }
                }
                initialWidths = columnWidths;
            }while(rs.next());
            // Print the column headers using the maximum width of each column
            System.out.printf("| %-5s", "No.");
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                columnWidths[i - 1] = Math.max(columnName.length(), initialWidths[i - 1]); //Check the column width with column name
                System.out.printf("| %-" + (columnWidths[i - 1]+3)+ "s", columnName);
            }
            System.out.println("|");
            // Close the current ResultSet
            rs.close();
        }else{
            System.out.println("\nThere is no order that is still not shipped.\n");
        }
        
        // Re-execute the query to reset the ResultSet
        rs = statement.executeQuery(sql);
        int number = 0;
        while (rs.next()) {
            number=number+1;
            System.out.printf("| %-5s", number);
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = rs.getString(i);
                if (columnValue != null) {
                    System.out.printf("| %-" +(columnWidths[i - 1]+3) + "s", columnValue);
                } else {
                    System.out.printf("| %-" + (columnWidths[i - 1]+3) + "s", "");
                }
            }
            System.out.println("|");
        }

        // Close the statement.
        rs.close();


        System.out.println("Shipped Orders :");  //Shipping Status = 'shipped'
        sql = "SELECT OID, CID, Order_Date, Shipping_Status, " +
        "LISTAGG(Order_ISBN || ' (' || Order_Quantity || ')', ', ') " +
        "WITHIN GROUP (ORDER BY Order_ISBN) AS \"Book_List (format:Book_ISBN(Quantity))\" " +
        "FROM ORDERS " +
        "WHERE Shipping_Status = 'shipped'"+
        "GROUP BY OID, CID, Order_Date, Shipping_Status";
        rs = statement.executeQuery(sql);
        ResultSetMetaData metaData2 = rs.getMetaData();
        int columnCount2 = metaData2.getColumnCount();
        int[] columnWidths2 = new int[columnCount2];
        int[] initialWidths2 = new int[columnCount2];
        if(rs.next()){
            do {
                for (int i = 1; i <= columnCount2; i++) {
                    String columnValue = rs.getString(i);
                    if (columnValue != null) {
                        int maxLength = columnValue.length();
                        columnWidths2[i - 1] = Math.max(maxLength, initialWidths2[i - 1]);
                    }
                }
                initialWidths2 = columnWidths2;
            }while(rs.next());
            // Print the column headers using the maximum width of each column
            System.out.printf("| %-5s", "No.");
            for (int i = 1; i <= columnCount2; i++) {
                String columnName = metaData.getColumnName(i);
                columnWidths2[i - 1] = Math.max(columnName.length(), initialWidths2[i - 1]); //Check the column width with column name
                System.out.printf("| %-" + (columnWidths2[i - 1]+3)+ "s", columnName);
            }
            System.out.println("|");
            // Close the current ResultSet
            rs.close();
        }else{
            System.out.println("\nThere is no order that is shipped.\n");
        }
        
        // Re-execute the query to reset the ResultSet
        rs = statement.executeQuery(sql);
        int number2 = 0;
        while (rs.next()) {
            number2=number2+1;
            System.out.printf("| %-5s", number2);
            for (int i = 1; i <= columnCount2; i++) {
                String columnValue = rs.getString(i);
                if (columnValue != null) {
                    System.out.printf("| %-" +(columnWidths2[i - 1]+3) + "s", columnValue);
                } else {
                    System.out.printf("| %-" + (columnWidths2[i - 1]+3) + "s", "");
                }
            }
            System.out.println("|");
        }
        rs.close();

        System.out.println("Received Orders :");  //Shipping Status = 'received'
        sql = "SELECT OID, CID, Order_Date, Shipping_Status, " +
        "LISTAGG(Order_ISBN || ' (' || Order_Quantity || ')', ', ') " +
        "WITHIN GROUP (ORDER BY Order_ISBN) AS \"Book_List (format:Book_ISBN(Quantity))\" " +
        "FROM ORDERS " +
        "WHERE Shipping_Status = 'received'"+
        "GROUP BY OID, CID, Order_Date, Shipping_Status";
        rs = statement.executeQuery(sql);
        ResultSetMetaData metaData3 = rs.getMetaData();
        int columnCount3 = metaData3.getColumnCount();
        int[] columnWidths3 = new int[columnCount3];
        int[] initialWidths3 = new int[columnCount3];
        if(rs.next()){
            do {
                for (int i = 1; i <= columnCount3; i++) {
                    String columnValue = rs.getString(i);
                    if (columnValue != null) {
                        int maxLength = columnValue.length();
                        columnWidths3[i - 1] = Math.max(maxLength, initialWidths3[i - 1]);
                    }
                }
                initialWidths3 = columnWidths3;
            }while(rs.next());
            // Print the column headers using the maximum width of each column
            System.out.printf("| %-5s", "No.");
            for (int i = 1; i <= columnCount3; i++) {
                String columnName = metaData.getColumnName(i);
                columnWidths3[i - 1] = Math.max(columnName.length(), initialWidths3[i - 1]); //Check the column width with column name
                System.out.printf("| %-" + (columnWidths3[i - 1]+3)+ "s", columnName);
            }
            System.out.println("|");
            // Close the current ResultSet
            rs.close();
        }else{
            System.out.println("\nThere is no order that is received.\n");
        }
        
        // Re-execute the query to reset the ResultSet
        rs = statement.executeQuery(sql);
        int number3 = 0;
        while (rs.next()) {
            number3=number3+1;
            System.out.printf("| %-5s", number3);
            for (int i = 1; i <= columnCount3; i++) {
                String columnValue = rs.getString(i);
                if (columnValue != null) {
                    System.out.printf("| %-" +(columnWidths3[i - 1]+3) + "s", columnValue);
                } else {
                    System.out.printf("| %-" + (columnWidths3[i - 1]+3) + "s", "");
                }
            }
            System.out.println("|");
        }
        rs.close();
        statement.close();
    }
    public static void nMostPopularBook(int n, Connection conn) throws SQLException{
        // Execute the SQL query to select the ISBN of the N most popular books.
        String sql = "SELECT *\n" +
                "FROM (\n" +
                "SELECT O.Order_ISBN AS ISBN, Title,\n" +
                "LISTAGG(DISTINCT A.aname, ', ') WITHIN GROUP (ORDER BY A.aname) AS Authors,\n" +
                "Price, Inventory_Quantity,\n" +
                "SUM(O.Order_Quantity) AS Total_Sales\n" +
                "FROM ORDERS O\n" +
                "LEFT JOIN Book B ON O.Order_ISBN = B.ISBN\n" +
                "LEFT JOIN WrittenBy W ON B.ISBN = W.ISBN\n" +
                "LEFT JOIN Authors A ON W.aid = A.aid\n" +
                "GROUP BY O.Order_ISBN, Title, Price, Inventory_Quantity\n" +
                "ORDER BY Total_Sales DESC)\n" +
                "WHERE ROWNUM <= ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, n); // Set the value of N
        ResultSet rs = statement.executeQuery();

        // Retrieve the information of the most popular books.
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        int[] columnWidths = new int[columnCount];
        int[] initialWidths = new int[columnCount];
        if(rs.next()){
            do {
                for (int i = 1; i <= columnCount; i++) {
                    String columnValue = rs.getString(i);
                    if (columnValue != null) {
                        int maxLength = columnValue.length();
                        columnWidths[i - 1] = Math.max(maxLength, initialWidths[i - 1]);
                    }
                }
                initialWidths = columnWidths;
            }while(rs.next());
            // Print the column headers using the maximum width of each column
            System.out.printf("| %-5s", "RANK");
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                columnWidths[i - 1] = Math.max(columnName.length(), initialWidths[i - 1]); //Check the column width with column name
                System.out.printf("| %-" + (columnWidths[i - 1]+3)+ "s", columnName);
            }
            System.out.println("|");
            // Close the current ResultSet
            rs.close();
        }else{
            System.out.println("\nThere is no the most popular book.\n");
        }
        
        // Re-execute the query to reset the ResultSet
        rs = statement.executeQuery();
        int rank = 0;
        while (rs.next()) {
            rank=rank+1;
            System.out.printf("| %-5s", rank);
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = rs.getString(i);
                if (columnValue != null) {
                    System.out.printf("| %-" +(columnWidths[i - 1]+3) + "s", columnValue);
                } else {
                    System.out.printf("| %-" + (columnWidths[i - 1]+3) + "s", "");
                }
            }
            System.out.println("|");
        }
        if (rank<n){
            System.out.println("Comment: The maximum number of the most popular books from the record is only " + rank +" books.");
        }

        // Close the statement.
        rs.close();
        statement.close();
    }

    // check if the tables exists before some executions
    public static boolean checkExist(Connection conn) throws SQLException {
        // Assume we have an array of table names
        String[] tableNames = { "INCLUDE", "WRITTENBY", "PLACE", "ORDERS", "CUSTOMER", "BOOK", "AUTHORS" };
    
        // Check if each table exists
        for (String tableName : tableNames) {
            String query = "SELECT table_name FROM user_tables WHERE table_name = '" + tableName + "'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next()) {
                rs.close();
                stmt.close();
                return false;
            }
            rs.close();
            stmt.close();
        }
        return true;
    }
}
