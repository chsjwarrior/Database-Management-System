# Database-Management-System
This is an implementation of a simple Java database management system. I implemented this program when i was studying computer science.
Not all commands are working, because a changed the program and i lose code

CREATE DATABASE MyDatabase;

SET DATABASE MyDatabase; // this is equals USE MyDatabase

CREATE TABLE Employees (
id INTEGER PRIMARY KEY,
name VARCHAR(100),
role VARCHAR(50)
);

DESCRIBE Employes;

INSERT INTO Employees (id, name, role) VALUES
(1, "Carlos", "Chief Executive Officer");

SELECT * FROM Employees;
