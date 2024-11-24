
# Smart Library Management System - COSC-444 Database Systems B1
# GoodReads
KU Database Systems Project, Group 6

## Project Overview
The Smart Library Management System is a comprehensive solution designed to manage library resources, including books and users. Developed using Java and embedded SQL, the system offers efficient resource management and robust user authentication with role-based access control.

## Features
### Librarian Functionalities
- **Member Management**: Add, update, approve, or delete members.
- **Book Management**: Add, update, or delete books.
- **Search**: Search for members or books using filters.
- **Logout**: Safely log out without terminating the program.

### Member Functionalities
- **Search Books**: Find books and view availability.
- **Cart Management**: Add books to the cart and checkout.
- **View Transactions**: Check borrowing and return history.
- **Profile Update**: Modify personal information like email, password, or address.

### Advanced Features
- **User Authentication**: Secure login with hashed passwords and role-based privileges.
- **Real-Time Availability**: Live updates on book and user data.
- **Error Handling**: Connection validation, I/O-based checks, and foreign key constraint enforcement.

## System Setup
1. Install MySQL and configure the GoodReads server.
2. Open the server on MySQLWorkbench and execute the provided `DB_Schema.sql` script.
3. Install a Java-supporting IDE like Eclipse or Visual Studio.
4. Configure the database connection in `DatabaseManager.java`.
5. Compile and run the `AppLauncher.java` class.

## Login Credentials (Sample for Testing)
- Librarian: ID 1, Password: password1
- Member: ID 1, Password: password1

## Key Highlights
- Secure and scalable library management.
- Enhanced user experience with real-time data and intuitive interface.
- Robust error handling and system resilience.
- Practical integration of database principles and software engineering techniques.

## Conclusion
The project demonstrates the effective use of database systems to solve real-world problems. It provides a user-friendly platform for managing library operations while ensuring data security and system reliability.

For further details or troubleshooting, refer to the user manual or contact the project contributors:
- Sultan Alhosani
- Aditya Chatterjee
- Ayah Miqdady
- Rashed Almheiri
