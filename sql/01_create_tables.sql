-- =====================================================================
-- BACSE202 Course Project - Food Delivery & Restaurant Order Management
-- Phase 2 | Stage 5: Database Design and Table Creation (DDL)
-- DBMS: MySQL 8.4
-- Schema follows the Phase 1 Relational Model exactly (14 relations).
-- Note: USER is a reserved word, so the table is named USERS.
-- =====================================================================

DROP DATABASE IF EXISTS food_delivery;
CREATE DATABASE food_delivery;
USE food_delivery;

-- ---------------------------------------------------------------------
-- 1. USERS  (superclass of the disjoint specialization)
-- USER (User_ID, First_Name, Last_Name, Email)
-- ---------------------------------------------------------------------
CREATE TABLE USERS (
    User_ID     INT           AUTO_INCREMENT,
    First_Name  VARCHAR(50)   NOT NULL,
    Last_Name   VARCHAR(50)   NOT NULL,
    Email       VARCHAR(100)  NOT NULL,
    CONSTRAINT pk_users        PRIMARY KEY (User_ID),
    CONSTRAINT uq_users_email  UNIQUE (Email),
    CONSTRAINT chk_users_email CHECK (Email LIKE '%_@_%._%')
);

-- ---------------------------------------------------------------------
-- 2. USER_PHONE  (multivalued attribute Phone)
-- USER_PHONE (User_ID, Phone)
-- ---------------------------------------------------------------------
CREATE TABLE USER_PHONE (
    User_ID  INT          NOT NULL,
    Phone    CHAR(10)     NOT NULL,
    CONSTRAINT pk_user_phone   PRIMARY KEY (User_ID, Phone),
    CONSTRAINT fk_phone_user   FOREIGN KEY (User_ID) REFERENCES USERS(User_ID)
                               ON DELETE CASCADE,
    CONSTRAINT chk_phone_10dig CHECK (Phone REGEXP '^[6-9][0-9]{9}$')
);

-- ---------------------------------------------------------------------
-- 3-5. Subclasses (disjoint specialization of USER)
-- ---------------------------------------------------------------------
CREATE TABLE CUSTOMER (
    Customer_ID  INT NOT NULL,
    CONSTRAINT pk_customer      PRIMARY KEY (Customer_ID),
    CONSTRAINT fk_customer_user FOREIGN KEY (Customer_ID) REFERENCES USERS(User_ID)
                                ON DELETE CASCADE
);

CREATE TABLE DELIVERY_AGENT (
    Agent_ID    INT          NOT NULL,
    Vehicle_No  VARCHAR(15)  NOT NULL,
    CONSTRAINT pk_agent         PRIMARY KEY (Agent_ID),
    CONSTRAINT uq_agent_vehicle UNIQUE (Vehicle_No),
    CONSTRAINT fk_agent_user    FOREIGN KEY (Agent_ID) REFERENCES USERS(User_ID)
                                ON DELETE CASCADE
);

CREATE TABLE RESTAURANT_MANAGER (
    Manager_ID  INT NOT NULL,
    CONSTRAINT pk_manager      PRIMARY KEY (Manager_ID),
    CONSTRAINT fk_manager_user FOREIGN KEY (Manager_ID) REFERENCES USERS(User_ID)
                               ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 6. ADDRESS  (weak entity, owner = CUSTOMER, partial key Address_No)
-- ADDRESS (Customer_ID, Address_No, City)
-- ---------------------------------------------------------------------
CREATE TABLE ADDRESS (
    Customer_ID  INT          NOT NULL,
    Address_No   INT          NOT NULL,
    City         VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_address          PRIMARY KEY (Customer_ID, Address_No),
    CONSTRAINT fk_address_customer FOREIGN KEY (Customer_ID) REFERENCES CUSTOMER(Customer_ID)
                                   ON DELETE CASCADE,
    CONSTRAINT chk_address_no      CHECK (Address_No >= 1)
);

-- ---------------------------------------------------------------------
-- 7. RESTAURANT  (MANAGES is 1:1, total on RESTAURANT side)
-- RESTAURANT (Restaurant_ID, Rest_Name, Manager_ID) - Manager_ID UNIQUE NOT NULL
-- ---------------------------------------------------------------------
CREATE TABLE RESTAURANT (
    Restaurant_ID  INT           AUTO_INCREMENT,
    Rest_Name      VARCHAR(100)  NOT NULL,
    Manager_ID     INT           NOT NULL,
    CONSTRAINT pk_restaurant         PRIMARY KEY (Restaurant_ID),
    CONSTRAINT uq_restaurant_manager UNIQUE (Manager_ID),
    CONSTRAINT fk_restaurant_manager FOREIGN KEY (Manager_ID)
                                     REFERENCES RESTAURANT_MANAGER(Manager_ID)
);

-- ---------------------------------------------------------------------
-- 8. MENU_ITEM  (OFFERS is 1:N, total on MENU_ITEM side)
-- MENU_ITEM (Item_ID, Item_Name, Price, Category, Restaurant_ID)
-- ---------------------------------------------------------------------
CREATE TABLE MENU_ITEM (
    Item_ID        INT            AUTO_INCREMENT,
    Item_Name      VARCHAR(100)   NOT NULL,
    Price          DECIMAL(8,2)   NOT NULL,
    Category       VARCHAR(30)    NOT NULL,
    Restaurant_ID  INT            NOT NULL,
    CONSTRAINT pk_menu_item       PRIMARY KEY (Item_ID),
    CONSTRAINT fk_item_restaurant FOREIGN KEY (Restaurant_ID)
                                  REFERENCES RESTAURANT(Restaurant_ID)
                                  ON DELETE CASCADE,
    CONSTRAINT uq_item_per_rest   UNIQUE (Restaurant_ID, Item_Name),
    CONSTRAINT chk_item_price     CHECK (Price > 0),
    CONSTRAINT chk_item_category  CHECK (Category IN
        ('Starter','Main Course','Dessert','Beverage','Snack','Combo'))
);

-- ---------------------------------------------------------------------
-- 9. COUPON
-- COUPON (Coupon_Code, Discount)  - Discount is a percentage
-- ---------------------------------------------------------------------
CREATE TABLE COUPON (
    Coupon_Code  VARCHAR(20)   NOT NULL,
    Discount     DECIMAL(5,2)  NOT NULL,
    CONSTRAINT pk_coupon           PRIMARY KEY (Coupon_Code),
    CONSTRAINT chk_coupon_discount CHECK (Discount > 0 AND Discount <= 50)
);

-- ---------------------------------------------------------------------
-- 10. ORDERS
-- ORDERS (Order_ID, Order_Status, Customer_ID, Address_No, Restaurant_ID, Coupon_Code)
-- (Customer_ID, Address_No) -> ADDRESS ; Coupon_Code may be NULL
-- ---------------------------------------------------------------------
CREATE TABLE ORDERS (
    Order_ID       INT          AUTO_INCREMENT,
    Order_Status   VARCHAR(20)  NOT NULL DEFAULT 'Placed',
    Customer_ID    INT          NOT NULL,
    Address_No     INT          NOT NULL,
    Restaurant_ID  INT          NOT NULL,
    Coupon_Code    VARCHAR(20)  NULL,
    CONSTRAINT pk_orders            PRIMARY KEY (Order_ID),
    CONSTRAINT fk_orders_customer   FOREIGN KEY (Customer_ID)
                                    REFERENCES CUSTOMER(Customer_ID),
    CONSTRAINT fk_orders_address    FOREIGN KEY (Customer_ID, Address_No)
                                    REFERENCES ADDRESS(Customer_ID, Address_No),
    CONSTRAINT fk_orders_restaurant FOREIGN KEY (Restaurant_ID)
                                    REFERENCES RESTAURANT(Restaurant_ID),
    CONSTRAINT fk_orders_coupon     FOREIGN KEY (Coupon_Code)
                                    REFERENCES COUPON(Coupon_Code),
    CONSTRAINT chk_order_status     CHECK (Order_Status IN
        ('Placed','Preparing','Out for Delivery','Delivered'))
);

-- ---------------------------------------------------------------------
-- 11. ORDER_ITEM  (M:N CONTAINS with attribute Quantity)
-- ORDER_ITEM (Order_ID, Item_ID, Quantity)
-- ---------------------------------------------------------------------
CREATE TABLE ORDER_ITEM (
    Order_ID  INT NOT NULL,
    Item_ID   INT NOT NULL,
    Quantity  INT NOT NULL,
    CONSTRAINT pk_order_item     PRIMARY KEY (Order_ID, Item_ID),
    CONSTRAINT fk_oi_order       FOREIGN KEY (Order_ID) REFERENCES ORDERS(Order_ID)
                                 ON DELETE CASCADE,
    CONSTRAINT fk_oi_item        FOREIGN KEY (Item_ID) REFERENCES MENU_ITEM(Item_ID),
    CONSTRAINT chk_oi_quantity   CHECK (Quantity BETWEEN 1 AND 20)
);

-- ---------------------------------------------------------------------
-- 12. PAYMENT  (PAID_BY is 1:1)
-- PAYMENT (Payment_ID, Method, Pay_Status, Order_ID) - Order_ID UNIQUE
-- ---------------------------------------------------------------------
CREATE TABLE PAYMENT (
    Payment_ID  INT          AUTO_INCREMENT,
    Method      VARCHAR(10)  NOT NULL,
    Pay_Status  VARCHAR(10)  NOT NULL DEFAULT 'Pending',
    Order_ID    INT          NOT NULL,
    CONSTRAINT pk_payment        PRIMARY KEY (Payment_ID),
    CONSTRAINT uq_payment_order  UNIQUE (Order_ID),
    CONSTRAINT fk_payment_order  FOREIGN KEY (Order_ID) REFERENCES ORDERS(Order_ID)
                                 ON DELETE CASCADE,
    CONSTRAINT chk_pay_method    CHECK (Method IN ('UPI','Card','COD')),
    CONSTRAINT chk_pay_status    CHECK (Pay_Status IN ('Pending','Paid','Failed'))
);

-- ---------------------------------------------------------------------
-- 13. DELIVERY  (FULFILLED_BY 1:1 with ORDER, HANDLES 1:N from AGENT)
-- DELIVERY (Delivery_ID, Delivery_Status, Order_ID, Agent_ID) - Order_ID UNIQUE
-- ---------------------------------------------------------------------
CREATE TABLE DELIVERY (
    Delivery_ID      INT          AUTO_INCREMENT,
    Delivery_Status  VARCHAR(15)  NOT NULL DEFAULT 'Assigned',
    Order_ID         INT          NOT NULL,
    Agent_ID         INT          NOT NULL,
    CONSTRAINT pk_delivery        PRIMARY KEY (Delivery_ID),
    CONSTRAINT uq_delivery_order  UNIQUE (Order_ID),
    CONSTRAINT fk_delivery_order  FOREIGN KEY (Order_ID) REFERENCES ORDERS(Order_ID)
                                  ON DELETE CASCADE,
    CONSTRAINT fk_delivery_agent  FOREIGN KEY (Agent_ID)
                                  REFERENCES DELIVERY_AGENT(Agent_ID),
    CONSTRAINT chk_delivery_status CHECK (Delivery_Status IN ('Assigned','Delivered'))
);

-- ---------------------------------------------------------------------
-- 14. REVIEW  (WRITES 1:N from CUSTOMER, ABOUT 1:1 with ORDER)
-- REVIEW (Review_ID, Rating, Customer_ID, Order_ID) - Order_ID UNIQUE
-- ---------------------------------------------------------------------
CREATE TABLE REVIEW (
    Review_ID    INT  AUTO_INCREMENT,
    Rating       INT  NOT NULL,
    Customer_ID  INT  NOT NULL,
    Order_ID     INT  NOT NULL,
    CONSTRAINT pk_review          PRIMARY KEY (Review_ID),
    CONSTRAINT uq_review_order    UNIQUE (Order_ID),
    CONSTRAINT fk_review_customer FOREIGN KEY (Customer_ID)
                                  REFERENCES CUSTOMER(Customer_ID),
    CONSTRAINT fk_review_order    FOREIGN KEY (Order_ID) REFERENCES ORDERS(Order_ID)
                                  ON DELETE CASCADE,
    CONSTRAINT chk_review_rating  CHECK (Rating BETWEEN 1 AND 5)
);

-- ---------------------------------------------------------------------
-- Verification (screenshot these for the Phase 2 PPT)
-- ---------------------------------------------------------------------
SHOW TABLES;
DESCRIBE ORDERS;
SELECT TABLE_NAME, CONSTRAINT_NAME, CONSTRAINT_TYPE
FROM   information_schema.TABLE_CONSTRAINTS
WHERE  TABLE_SCHEMA = 'food_delivery'
ORDER  BY TABLE_NAME, CONSTRAINT_TYPE;
