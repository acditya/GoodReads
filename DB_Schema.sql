-- -- Create the database
-- CREATE DATABASE GoodReads;

-- -- Use the database
-- USE GoodReads;

-- Create the Books table
CREATE TABLE Books (
    BookID INT PRIMARY KEY AUTO_INCREMENT,
    Title VARCHAR(255) NOT NULL,
    Author VARCHAR(255) NOT NULL,
    Genre VARCHAR(100),
    Publisher VARCHAR(100),
    YearPublished INT,
    CopiesAvailable INT DEFAULT 0
) AUTO_INCREMENT = 1;

-- Create the Members Tables
CREATE TABLE Members (
    MemberID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(255) NOT NULL,
    Address VARCHAR(255),
    Phone VARCHAR(20),
    Email VARCHAR(100),
    MembershipDate DATETIME DEFAULT CURRENT_TIMESTAMP
) AUTO_INCREMENT = 1;

CREATE TABLE MemberPasswords (
    MemberID INT PRIMARY KEY,
    Password VARCHAR(255) NOT NULL DEFAULT 'password',
    FOREIGN KEY (MemberID) REFERENCES Members(MemberID)
);

-- Create the Transactions Table
CREATE TABLE Transactions (
    TransactionID INT PRIMARY KEY AUTO_INCREMENT,
    MemberID INT,
    BookID INT,
    BorrowDate DATETIME DEFAULT CURRENT_TIMESTAMP, 
    ReturnDate DATETIME,
    Status ENUM('Borrowed', 'Returned') DEFAULT 'Borrowed',
    FOREIGN KEY (MemberID) REFERENCES Members(MemberID),
    FOREIGN KEY (BookID) REFERENCES Books(BookID)
) AUTO_INCREMENT = 1;

-- Create the Staff Tables
CREATE TABLE Staff (
    StaffID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(255) NOT NULL,
    Role VARCHAR(50),
    ContactInfo VARCHAR(100)
) AUTO_INCREMENT = 1;

CREATE TABLE StaffPasswords (
    StaffID INT PRIMARY KEY,
    Password VARCHAR(255) NOT NULL,
    FOREIGN KEY (StaffID) REFERENCES Staff(StaffID)
);

-- Basic Database Population INSERTS
INSERT INTO Books (Title, Author, Genre, Publisher, YearPublished, CopiesAvailable) VALUES
    ('To Kill a Mockingbird', 'Harper Lee', 'Fiction', 'J.B. Lippincott & Co.', 1960, 5),
    ('1984', 'George Orwell', 'Dystopian', 'Secker & Warburg', 1949, 3),
    ('Pride and Prejudice', 'Jane Austen', 'Romance', 'T. Egerton, Whitehall', 1813, 7),
    ('The Great Gatsby', 'F. Scott Fitzgerald', 'Fiction', 'Charles Scribner''s Sons', 1925, 4),
    ('The Catcher in the Rye', 'J.D. Salinger', 'Fiction', 'Little, Brown and Company', 1951, 2);

INSERT INTO Staff (Name, Role, ContactInfo) VALUES
    ('Aditya Chatterjee', 'Librarian', '0501234567'),
    ('Ayah Miqdady', 'Librarian', '0521234567'),
    ('Rashed Almheiri', 'Librarian', '0551234567'),
    ('Sultan Alhosani', 'Librarian', '0561234567');

INSERT INTO StaffPasswords (StaffID, Password) VALUES
    (1, 'password1'),
    (2, 'password2'),
    (3, 'password3'),
    (4, 'password4');

INSERT INTO Members (Name, Address, Phone, Email, MembershipDate) VALUES 
    ('Ahmed Al Mansoori', '123 Sheikh Zayed Rd, Dubai', '+971-50-123-4567', 'ahmed.almansoori@example.ae', '2023-01-15'),
    ('Fatima Al Rashed', '456 Corniche St, Abu Dhabi', '+971-50-234-5678', 'fatima.alrashed@example.ae', '2023-03-22'),
    ('Layla Al Shamsi', '101 Al Qusais, Dubai', '+971-50-456-7890', 'layla.alshamsi@example.ae', '2023-07-10'),
    ('Mariam Hussein', '202 Al Falah St, Al Ain', '+971-50-567-8901', 'mariam.hussein@example.ae', '2023-08-05'), 
    ('Omar Khaled', '34 Al Reem Island, Abu Dhabi', '+971-52-678-2345', 'omar.khaled@example.ae', '2023-02-14'),  
    ('Nour Al-Hassan', '56 Jumeirah St, Dubai', '+971-50-456-7891', 'nour.alhassan@example.ae', '2023-04-21'), 
    ('Amina Al-Farsi', '45 Al Qurm, Abu Dhabi', '+971-50-234-5678', 'amina.alfarsi@example.ae', '2023-10-01');

INSERT INTO MemberPasswords (MemberID, Password) VALUES
    (1, 'password1'),
    (2, 'password2'),
    (3, 'password3'),
    (4, 'password4'),
    (5, 'password5'),
    (6, 'password6'),
    (7, 'password7');

INSERT INTO Transactions(MemberID,BookID,BorrowDate,Status)VALUES
    (1,2,'2024-10-26','Borrowed'),
    (2,3,'2024-11-06','Borrowed'),
    (3,4,'2024-5-20','Borrowed'),
    (4,5,'2024-7-21','Borrowed'),
    
    (1,2,'2024-10-28','Returned'),
    (2,3,'2024-12-10','Returned');