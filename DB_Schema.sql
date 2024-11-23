-- Create the database
CREATE DATABASE IF NOT EXISTS GoodReads;

-- Use the database
USE GoodReads;

-- Create the Books table
CREATE TABLE Books (
    ISBN INT PRIMARY KEY AUTO_INCREMENT,
    Title VARCHAR(255) NOT NULL,
    Author VARCHAR(255) NOT NULL,
    Genre VARCHAR(100),
    Publisher VARCHAR(100),
    YearPublished INT,
    TotalCopies INT DEFAULT 0,
    CopiesAvailable INT DEFAULT 0,
    InformationUpdateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) AUTO_INCREMENT = 1;

-- Create the Members table
CREATE TABLE Members (
    MemberID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(255) NOT NULL,
    Address VARCHAR(255),
    Phone VARCHAR(20),
    Email VARCHAR(100),
    MembershipDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    Authorized BOOLEAN DEFAULT FALSE,
    Deleted BOOLEAN DEFAULT FALSE,
    InformationUpdateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) AUTO_INCREMENT = 1;

-- Create the MemberPasswords table
CREATE TABLE MemberPasswords (
    MemberID INT PRIMARY KEY,
    Password VARCHAR(255) NOT NULL,
    FOREIGN KEY (MemberID) REFERENCES Members(MemberID)
);

-- Create the Transactions table
CREATE TABLE Transactions (
    TransactionID INT PRIMARY KEY AUTO_INCREMENT,
    MemberID INT,
    ISBN INT,
    BorrowDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    ReturnDate DATETIME,
    Status ENUM('Borrowed', 'Returned') DEFAULT 'Borrowed',
    InformationUpdateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (MemberID) REFERENCES Members(MemberID),
    FOREIGN KEY (ISBN) REFERENCES Books(ISBN)
) AUTO_INCREMENT = 1;

-- Create the Staff table
CREATE TABLE Staff (
    StaffID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(255) NOT NULL,
    Role VARCHAR(50),
    ContactInfo VARCHAR(100),
    InformationUpdateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) AUTO_INCREMENT = 1;

-- Create the StaffPasswords table
CREATE TABLE StaffPasswords (
    StaffID INT PRIMARY KEY,
    Password VARCHAR(255) NOT NULL,
    FOREIGN KEY (StaffID) REFERENCES Staff(StaffID)
);

-- Populate the Books table with more information
INSERT INTO Books (Title, Author, Genre, Publisher, YearPublished, TotalCopies, CopiesAvailable) VALUES
    ('To Kill a Mockingbird', 'Harper Lee', 'Fiction', 'J.B. Lippincott & Co.', 1960, 5, 4),
    ('1984', 'George Orwell', 'Dystopian', 'Secker & Warburg', 1949, 3, 1),
    ('Pride and Prejudice', 'Jane Austen', 'Romance', 'T. Egerton, Whitehall', 1813, 7, 6),
    ('The Great Gatsby', 'F. Scott Fitzgerald', 'Fiction', 'Charles Scribner''s Sons', 1925, 4, 4),
    ('The Catcher in the Rye', 'J.D. Salinger', 'Fiction', 'Little, Brown and Company', 1951, 2, 1),
    ('Moby Dick', 'Herman Melville', 'Adventure', 'Harper & Brothers', 1851, 6, 5),
    ('The Hobbit', 'J.R.R. Tolkien', 'Fantasy', 'George Allen & Unwin', 1937, 8, 7);

-- Populate the Members table with more information
INSERT INTO Members (Name, Address, Phone, Email, MembershipDate, Authorized) VALUES 
    ('Ahmed Al Mansoori', '123 Sheikh Zayed Rd, Dubai', '+971-50-123-4567', 'ahmed.almansoori@example.ae', '2023-01-15', '1'),
    ('Fatima Al Rashed', '456 Corniche St, Abu Dhabi', '+971-50-234-5678', 'fatima.alrashed@example.ae', '2023-03-22', '1'),
    ('Layla Al Shamsi', '101 Al Qusais, Dubai', '+971-50-456-7890', 'layla.alshamsi@example.ae', '2023-07-10', '1'),
    ('Mariam Hussein', '202 Al Falah St, Al Ain', '+971-50-567-8901', 'mariam.hussein@example.ae', '2023-08-05', '1'), 
    ('Omar Khaled', '34 Al Reem Island, Abu Dhabi', '+971-52-678-2345', 'omar.khaled@example.ae', '2023-02-14', '1'),  
    ('Nour Al-Hassan', '56 Jumeirah St, Dubai', '+971-50-456-7891', 'nour.alhassan@example.ae', '2023-04-21', '1'), 
    ('Amina Al-Farsi', '45 Al Qurm, Abu Dhabi', '+971-50-234-5678', 'amina.alfarsi@example.ae', '2023-10-01', '1');
    
    
INSERT INTO Staff (Name, Role, ContactInfo) VALUES
    ('Aditya Chatterjee', 'Librarian', '0501234567'),
    ('Ayah Miqdady', 'Librarian', '0521234567'),
    ('Rashed Almheiri', 'Librarian', '0551234567'),
    ('Sultan Alhosani', 'Librarian', '0561234567');

-- Populate the Transactions table with more information
INSERT INTO Transactions (MemberID, ISBN, BorrowDate, ReturnDate, Status) VALUES
    (1, 2, '2024-10-26', NULL, 'Borrowed'),
    (2, 3, '2024-11-06', NULL, 'Borrowed'),
    (3, 4, '2024-05-20', '2024-06-20', 'Returned'),
    (4, 5, '2024-07-21', NULL, 'Borrowed'),
    (5, 1, '2024-08-10', '2024-09-10', 'Returned'),
    (6, 2, '2024-09-15', NULL, 'Borrowed'),
    (7, 6, '2024-10-05', NULL, 'Borrowed'),
    (5, 7, '2024-11-01', NULL, 'Borrowed'),
    (1, 1, '2024-11-10', NULL, 'Borrowed');

INSERT INTO StaffPasswords (StaffID, Password) VALUES
    (1, '0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e'), # password1
    (2, '6cf615d5bcaac778352a8f1f3360d23f02f34ec182e259897fd6ce485d7870d4'), # password2
    (3, '5906ac361a137e2d286465cd6588ebb5ac3f5ae955001100bc41577c3d751764'), # password3
    (4, 'b97873a40f73abedd8d685a7cd5e5f85e4a9cfb83eac26886640a0813850122b'); # password4
    

INSERT INTO MemberPasswords (MemberID, Password) VALUES
    (1, '0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e'), # password1
    (2, '6cf615d5bcaac778352a8f1f3360d23f02f34ec182e259897fd6ce485d7870d4'), # password2
    (3, '5906ac361a137e2d286465cd6588ebb5ac3f5ae955001100bc41577c3d751764'), # password3
    (4, 'b97873a40f73abedd8d685a7cd5e5f85e4a9cfb83eac26886640a0813850122b'), # password4
    (5, '8b2c86ea9cf2ea4eb517fd1e06b74f399e7fec0fef92e3b482a6cf2e2b092023'), # password5
    (6, '598a1a400c1dfdf36974e69d7e1bc98593f2e15015eed8e9b7e47a83b31693d5'), # password6
    (7, '5860836e8f13fc9837539a597d4086bfc0299e54ad92148d54538b5c3feefb7c'); # password7
    
    