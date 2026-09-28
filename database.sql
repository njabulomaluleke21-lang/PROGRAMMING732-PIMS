CREATE DATABASE IF NOT EXISTS pims;
USE pims;
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS medicines;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS users;
CREATE TABLE users(user_id INT PRIMARY KEY AUTO_INCREMENT,username VARCHAR(50) UNIQUE NOT NULL,password_hash CHAR(64) NOT NULL,role ENUM('Admin','Cashier') NOT NULL,full_name VARCHAR(100) NOT NULL);
CREATE TABLE suppliers(supplier_id INT PRIMARY KEY AUTO_INCREMENT,name VARCHAR(100) NOT NULL,contact_person VARCHAR(100),phone VARCHAR(20),email VARCHAR(100),address TEXT);
CREATE TABLE medicines(medicine_id INT PRIMARY KEY AUTO_INCREMENT,name VARCHAR(150) NOT NULL,company VARCHAR(100),medicine_type VARCHAR(50),price DECIMAL(10,2) NOT NULL,quantity_in_stock INT NOT NULL,reorder_level INT NOT NULL,expiry_date DATE NOT NULL,supplier_id INT,FOREIGN KEY(supplier_id) REFERENCES suppliers(supplier_id) ON UPDATE CASCADE ON DELETE SET NULL);
CREATE TABLE sales(sale_id INT PRIMARY KEY AUTO_INCREMENT,sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,total_amount DECIMAL(10,2) NOT NULL,user_id INT NOT NULL,FOREIGN KEY(user_id) REFERENCES users(user_id));
CREATE TABLE sale_items(sale_item_id INT PRIMARY KEY AUTO_INCREMENT,sale_id INT NOT NULL,medicine_id INT NOT NULL,quantity_sold INT NOT NULL,price_at_sale DECIMAL(10,2) NOT NULL,FOREIGN KEY(sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,FOREIGN KEY(medicine_id) REFERENCES medicines(medicine_id));
INSERT INTO users(username,password_hash,role,full_name) VALUES
('admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','Admin','System Administrator'),
('cashier','c246650737293ddc18fc357393db78d1ecc9d1fd1af95469115e4a29f983359a','Cashier','Front Desk Cashier');
INSERT INTO suppliers(name,contact_person,phone,email,address) VALUES
('MediSupply SA','Thandi Mokoena','011-555-1000','sales@medisupply.co.za','Johannesburg'),
('PharmaLink','David Naidoo','011-555-2000','sales@pharmalink.co.za','Pretoria');
INSERT INTO medicines(name,company,medicine_type,price,quantity_in_stock,reorder_level,expiry_date,supplier_id) VALUES
('Paracetamol 500mg','HealthCo','Tablet',25.99,120,20,'2027-03-20',1),
('Amoxicillin 250mg','PharmaCare','Capsule',49.50,18,25,'2027-01-15',2),
('Cough Syrup 100ml','Wellness Labs','Syrup',68.75,7,10,'2026-10-15',1);
