-- =====================================================================
-- BACSE202 Course Project - Food Delivery & Restaurant Order Management
-- Phase 2 | Stage 6: Data Population (DML)
-- Run AFTER 01_create_tables.sql
-- Inserts are ordered parent -> child so every foreign key is satisfied.
-- =====================================================================

USE food_delivery;

-- ---------------------------------------------------------------------
-- USERS : 1-5 Restaurant Managers | 6-10 Delivery Agents | 11-22 Customers
-- ---------------------------------------------------------------------
INSERT INTO USERS (User_ID, First_Name, Last_Name, Email) VALUES
(1,  'Rahul',     'Menon',    'rahul.menon@spicehub.in'),
(2,  'Priya',     'Sharma',   'priya.sharma@pizzapoint.in'),
(3,  'Karthik',   'Iyer',     'karthik.iyer@dosacorner.in'),
(4,  'Ananya',    'Reddy',    'ananya.reddy@dragonwok.in'),
(5,  'Vikram',    'Singh',    'vikram.singh@sweettooth.in'),
(6,  'Suresh',    'Kumar',    'suresh.kumar@fooddel.in'),
(7,  'Manoj',     'Das',      'manoj.das@fooddel.in'),
(8,  'Ravi',      'Teja',     'ravi.teja@fooddel.in'),
(9,  'Imran',     'Khan',     'imran.khan@fooddel.in'),
(10, 'Ganesh',    'Babu',     'ganesh.babu@fooddel.in'),
(11, 'Arjun',     'Nair',     'arjun.nair@gmail.com'),
(12, 'Meera',     'Pillai',   'meera.pillai@gmail.com'),
(13, 'Sneha',     'Gupta',    'sneha.gupta@gmail.com'),
(14, 'Rohan',     'Verma',    'rohan.verma@gmail.com'),
(15, 'Aisha',     'Khan',     'aisha.khan@gmail.com'),
(16, 'Nikhil',    'Joshi',    'nikhil.joshi@gmail.com'),
(17, 'Divya',     'Krishnan', 'divya.krishnan@gmail.com'),
(18, 'Aditya',    'Rao',      'aditya.rao@gmail.com'),
(19, 'Kavya',     'Menon',    'kavya.menon@gmail.com'),
(20, 'Siddharth', 'Jain',     'siddharth.jain@gmail.com'),
(21, 'Pooja',     'Patel',    'pooja.patel@gmail.com'),
(22, 'Farhan',    'Ali',      'farhan.ali@gmail.com');

-- ---------------------------------------------------------------------
-- USER_PHONE (multivalued - some users have two numbers)
-- ---------------------------------------------------------------------
INSERT INTO USER_PHONE (User_ID, Phone) VALUES
(1, '9840011223'), (2, '9840022334'), (3, '9840033445'),
(4, '9840044556'), (5, '9840055667'), (5, '8056677889'),
(6, '9003112233'), (7, '9003223344'), (8, '9003334455'),
(9, '9003445566'), (10,'9003556677'),
(11,'9876501234'), (11,'9123405678'),
(12,'9988776655'),
(13,'9445566778'), (13,'7708899001'),
(14,'9566012345'),
(15,'9789023456'),
(16,'8807034567'), (16,'9361045678'),
(17,'9894056789'),
(18,'9952067890'),
(19,'7397078901'),
(20,'9600089012'), (20,'8925090123'),
(21,'9790101234'),
(22,'6380112345');

-- ---------------------------------------------------------------------
-- Subclasses of USER (disjoint)
-- ---------------------------------------------------------------------
INSERT INTO RESTAURANT_MANAGER (Manager_ID) VALUES (1),(2),(3),(4),(5);

INSERT INTO DELIVERY_AGENT (Agent_ID, Vehicle_No) VALUES
(6,  'TN23AB1234'),
(7,  'TN23CD5678'),
(8,  'TN23EF9012'),
(9,  'TN23GH3456'),
(10, 'TN23JK7890');

INSERT INTO CUSTOMER (Customer_ID) VALUES
(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),(21),(22);

-- ---------------------------------------------------------------------
-- ADDRESS (weak entity - Address_No is unique per customer)
-- ---------------------------------------------------------------------
INSERT INTO ADDRESS (Customer_ID, Address_No, City) VALUES
(11, 1, 'Vellore'),      (11, 2, 'Katpadi'),
(12, 1, 'Katpadi'),      (12, 2, 'Gandhi Nagar'),
(13, 1, 'Sathuvachari'),
(14, 1, 'Vellore'),
(15, 1, 'Bagayam'),      (15, 2, 'Vellore'),
(16, 1, 'Katpadi'),
(17, 1, 'Gandhi Nagar'),
(18, 1, 'Vellore'),      (18, 2, 'Sathuvachari'),
(19, 1, 'Bagayam'),
(20, 1, 'Katpadi'),
(21, 1, 'Vellore'),
(22, 1, 'Gandhi Nagar');

-- ---------------------------------------------------------------------
-- RESTAURANT (each managed by exactly one manager)
-- ---------------------------------------------------------------------
INSERT INTO RESTAURANT (Restaurant_ID, Rest_Name, Manager_ID) VALUES
(1, 'Spice Hub',        1),
(2, 'Pizza Point',      2),
(3, 'Dosa Corner',      3),
(4, 'Dragon Wok',       4),
(5, 'Sweet Tooth Cafe', 5);

-- ---------------------------------------------------------------------
-- MENU_ITEM (items 1-3 match the Phase 1 normalization example)
-- ---------------------------------------------------------------------
INSERT INTO MENU_ITEM (Item_ID, Item_Name, Price, Category, Restaurant_ID) VALUES
(1,  'Chicken Biryani',      250.00, 'Main Course', 1),
(2,  'Lassi',                 60.00, 'Beverage',    1),
(3,  'Margherita Pizza',     299.00, 'Main Course', 2),
(4,  'Paneer Tikka',         220.00, 'Starter',     1),
(5,  'Butter Naan',           45.00, 'Main Course', 1),
(6,  'Gulab Jamun',           80.00, 'Dessert',     1),
(7,  'Farmhouse Pizza',      399.00, 'Main Course', 2),
(8,  'Garlic Bread',         149.00, 'Starter',     2),
(9,  'Cold Coffee',          120.00, 'Beverage',    2),
(10, 'Pizza Combo Meal',     549.00, 'Combo',       2),
(11, 'Masala Dosa',           90.00, 'Main Course', 3),
(12, 'Idli Vada',             70.00, 'Snack',       3),
(13, 'Filter Coffee',         40.00, 'Beverage',    3),
(14, 'Rava Kesari',           60.00, 'Dessert',     3),
(15, 'Mini Tiffin Combo',    150.00, 'Combo',       3),
(16, 'Veg Spring Rolls',     160.00, 'Starter',     4),
(17, 'Hakka Noodles',        190.00, 'Main Course', 4),
(18, 'Chilli Chicken',       240.00, 'Starter',     4),
(19, 'Fried Rice',           180.00, 'Main Course', 4),
(20, 'Lemon Iced Tea',        90.00, 'Beverage',    4),
(21, 'Chocolate Brownie',    140.00, 'Dessert',     5),
(22, 'Red Velvet Pastry',    120.00, 'Dessert',     5),
(23, 'Blueberry Cheesecake', 220.00, 'Dessert',     5),
(24, 'Cappuccino',           130.00, 'Beverage',    5),
(25, 'Club Sandwich',        170.00, 'Snack',       5),
(26, 'Mutton Biryani',       320.00, 'Main Course', 1),
(27, 'Veg Manchurian',       170.00, 'Starter',     4),
(28, 'Onion Uttapam',        100.00, 'Main Course', 3);

-- ---------------------------------------------------------------------
-- COUPON (Discount = percentage off the order subtotal)
-- ---------------------------------------------------------------------
INSERT INTO COUPON (Coupon_Code, Discount) VALUES
('FEST10',    10.00),
('WELCOME20', 20.00),
('VIT15',     15.00),
('DIWALI25',  25.00),
('FLAT5',      5.00);

-- ---------------------------------------------------------------------
-- ORDERS (26 orders across every status)
-- Orders 1-16 Delivered | 17-19 Out for Delivery | 20-22 Preparing | 23-26 Placed
-- ---------------------------------------------------------------------
INSERT INTO ORDERS (Order_ID, Order_Status, Customer_ID, Address_No, Restaurant_ID, Coupon_Code) VALUES
(1,  'Delivered',        11, 1, 1, 'FEST10'),
(2,  'Delivered',        12, 1, 2, NULL),
(3,  'Delivered',        11, 1, 1, NULL),
(4,  'Delivered',        13, 1, 3, 'VIT15'),
(5,  'Delivered',        14, 1, 4, NULL),
(6,  'Delivered',        15, 1, 5, 'WELCOME20'),
(7,  'Delivered',        16, 1, 2, NULL),
(8,  'Delivered',        17, 1, 3, NULL),
(9,  'Delivered',        18, 1, 1, 'DIWALI25'),
(10, 'Delivered',        19, 1, 4, NULL),
(11, 'Delivered',        20, 1, 5, NULL),
(12, 'Delivered',        21, 1, 2, 'FEST10'),
(13, 'Delivered',        22, 1, 3, NULL),
(14, 'Delivered',        11, 2, 4, NULL),
(15, 'Delivered',        12, 2, 1, 'VIT15'),
(16, 'Delivered',        13, 1, 5, NULL),
(17, 'Out for Delivery', 14, 1, 1, NULL),
(18, 'Out for Delivery', 15, 2, 2, NULL),
(19, 'Out for Delivery', 16, 1, 3, 'FLAT5'),
(20, 'Preparing',        17, 1, 4, NULL),
(21, 'Preparing',        18, 2, 5, NULL),
(22, 'Preparing',        19, 1, 1, NULL),
(23, 'Placed',           20, 1, 2, 'WELCOME20'),
(24, 'Placed',           21, 1, 3, NULL),
(25, 'Placed',           22, 1, 4, NULL),
(26, 'Placed',           11, 1, 5, NULL);

-- ---------------------------------------------------------------------
-- ORDER_ITEM (every item belongs to the order's restaurant)
-- ---------------------------------------------------------------------
INSERT INTO ORDER_ITEM (Order_ID, Item_ID, Quantity) VALUES
(1, 1, 2), (1, 2, 1),
(2, 3, 1),
(3, 1, 1),
(4, 11, 2), (4, 13, 2),
(5, 17, 1), (5, 18, 1), (5, 20, 2),
(6, 21, 2), (6, 24, 1),
(7, 7, 1), (7, 8, 1), (7, 9, 2),
(8, 15, 2),
(9, 26, 2), (9, 4, 1), (9, 6, 2),
(10, 19, 2), (10, 27, 1),
(11, 23, 1), (11, 22, 2),
(12, 10, 1),
(13, 11, 1), (13, 12, 1), (13, 14, 1),
(14, 17, 2), (14, 16, 1),
(15, 1, 2), (15, 5, 4),
(16, 25, 2), (16, 24, 2),
(17, 1, 1), (17, 2, 1),
(18, 3, 2),
(19, 28, 2), (19, 13, 2),
(20, 18, 1), (20, 19, 1),
(21, 21, 1), (21, 22, 1),
(22, 26, 1), (22, 5, 2),
(23, 7, 1), (23, 9, 1),
(24, 11, 3),
(25, 17, 1), (25, 20, 1),
(26, 23, 1);

-- ---------------------------------------------------------------------
-- PAYMENT (one per order)
-- UPI / Card -> Paid at order time | COD -> Pending until delivered
-- ---------------------------------------------------------------------
INSERT INTO PAYMENT (Payment_ID, Method, Pay_Status, Order_ID) VALUES
(1,  'UPI',  'Paid',    1),
(2,  'COD',  'Paid',    2),
(3,  'Card', 'Paid',    3),
(4,  'UPI',  'Paid',    4),
(5,  'Card', 'Paid',    5),
(6,  'UPI',  'Paid',    6),
(7,  'COD',  'Paid',    7),
(8,  'UPI',  'Paid',    8),
(9,  'Card', 'Paid',    9),
(10, 'COD',  'Paid',    10),
(11, 'UPI',  'Paid',    11),
(12, 'Card', 'Paid',    12),
(13, 'UPI',  'Paid',    13),
(14, 'COD',  'Paid',    14),
(15, 'UPI',  'Paid',    15),
(16, 'Card', 'Paid',    16),
(17, 'UPI',  'Paid',    17),
(18, 'COD',  'Pending', 18),
(19, 'Card', 'Paid',    19),
(20, 'UPI',  'Paid',    20),
(21, 'COD',  'Pending', 21),
(22, 'Card', 'Paid',    22),
(23, 'UPI',  'Paid',    23),
(24, 'COD',  'Pending', 24),
(25, 'Card', 'Paid',    25),
(26, 'Card', 'Failed',  26);

-- ---------------------------------------------------------------------
-- DELIVERY (only for orders that are Out for Delivery or Delivered)
-- Each agent has at most one active ('Assigned') delivery
-- ---------------------------------------------------------------------
INSERT INTO DELIVERY (Delivery_ID, Delivery_Status, Order_ID, Agent_ID) VALUES
(1,  'Delivered', 1,  6),
(2,  'Delivered', 2,  7),
(3,  'Delivered', 3,  8),
(4,  'Delivered', 4,  9),
(5,  'Delivered', 5,  10),
(6,  'Delivered', 6,  6),
(7,  'Delivered', 7,  7),
(8,  'Delivered', 8,  8),
(9,  'Delivered', 9,  9),
(10, 'Delivered', 10, 10),
(11, 'Delivered', 11, 6),
(12, 'Delivered', 12, 7),
(13, 'Delivered', 13, 8),
(14, 'Delivered', 14, 9),
(15, 'Delivered', 15, 10),
(16, 'Delivered', 16, 6),
(17, 'Assigned',  17, 6),
(18, 'Assigned',  18, 7),
(19, 'Assigned',  19, 8);

-- ---------------------------------------------------------------------
-- REVIEW (only on Delivered orders, written by the customer who ordered)
-- ---------------------------------------------------------------------
INSERT INTO REVIEW (Review_ID, Rating, Customer_ID, Order_ID) VALUES
(1,  5, 11, 1),
(2,  4, 12, 2),
(3,  4, 11, 3),
(4,  5, 13, 4),
(5,  3, 14, 5),
(6,  5, 15, 6),
(7,  4, 16, 7),
(8,  5, 18, 9),
(9,  2, 19, 10),
(10, 4, 20, 11),
(11, 5, 21, 12),
(12, 4, 22, 13),
(13, 3, 12, 15),
(14, 4, 13, 16);

-- ---------------------------------------------------------------------
-- Verification (screenshot these for the Phase 2 PPT)
-- ---------------------------------------------------------------------
SELECT 'USERS' AS Table_Name, COUNT(*) AS Row_Count FROM USERS
UNION ALL SELECT 'USER_PHONE',         COUNT(*) FROM USER_PHONE
UNION ALL SELECT 'CUSTOMER',           COUNT(*) FROM CUSTOMER
UNION ALL SELECT 'DELIVERY_AGENT',     COUNT(*) FROM DELIVERY_AGENT
UNION ALL SELECT 'RESTAURANT_MANAGER', COUNT(*) FROM RESTAURANT_MANAGER
UNION ALL SELECT 'ADDRESS',            COUNT(*) FROM ADDRESS
UNION ALL SELECT 'RESTAURANT',         COUNT(*) FROM RESTAURANT
UNION ALL SELECT 'MENU_ITEM',          COUNT(*) FROM MENU_ITEM
UNION ALL SELECT 'COUPON',             COUNT(*) FROM COUPON
UNION ALL SELECT 'ORDERS',             COUNT(*) FROM ORDERS
UNION ALL SELECT 'ORDER_ITEM',         COUNT(*) FROM ORDER_ITEM
UNION ALL SELECT 'PAYMENT',            COUNT(*) FROM PAYMENT
UNION ALL SELECT 'DELIVERY',           COUNT(*) FROM DELIVERY
UNION ALL SELECT 'REVIEW',             COUNT(*) FROM REVIEW;

-- Full order view (joins 6 tables) - shows the data fits together
SELECT o.Order_ID,
       CONCAT(u.First_Name, ' ', u.Last_Name) AS Customer,
       a.City,
       r.Rest_Name,
       o.Order_Status,
       p.Method,
       p.Pay_Status,
       o.Coupon_Code
FROM   ORDERS o
JOIN   USERS u      ON u.User_ID = o.Customer_ID
JOIN   ADDRESS a    ON a.Customer_ID = o.Customer_ID AND a.Address_No = o.Address_No
JOIN   RESTAURANT r ON r.Restaurant_ID = o.Restaurant_ID
JOIN   PAYMENT p    ON p.Order_ID = o.Order_ID
ORDER  BY o.Order_ID;
