-- =====================================================================
-- BACSE202 Course Project - Food Delivery & Restaurant Order Management
-- Phase 2 | Stage 8: Business Logic Implementation
-- Run AFTER 01_create_tables.sql and 02_insert_data.sql
--
-- Contents
--   A. Functions  : order subtotal, order total (derived Total_Amount)
--   B. Triggers   : workflow rules enforced inside the database
--   C. Procedures : place order (transaction), assign delivery, mark delivered
--   D. Views      : order summary, Avg_Rating (derived), sales & performance reports
-- Every rule violation raises SQLSTATE 45000 with a readable message,
-- which the Java front end catches and shows to the user.
-- =====================================================================

USE food_delivery;

DROP FUNCTION  IF EXISTS fn_order_subtotal;
DROP FUNCTION  IF EXISTS fn_order_total;
DROP TRIGGER   IF EXISTS trg_orders_before_insert;
DROP TRIGGER   IF EXISTS trg_orders_status_flow;
DROP TRIGGER   IF EXISTS trg_orders_after_delivered;
DROP TRIGGER   IF EXISTS trg_order_item_before_insert;
DROP TRIGGER   IF EXISTS trg_order_item_before_update;
DROP TRIGGER   IF EXISTS trg_delivery_before_insert;
DROP TRIGGER   IF EXISTS trg_delivery_after_insert;
DROP TRIGGER   IF EXISTS trg_review_before_insert;
DROP PROCEDURE IF EXISTS sp_place_order;
DROP PROCEDURE IF EXISTS sp_assign_delivery;
DROP PROCEDURE IF EXISTS sp_mark_delivered;

DELIMITER $$

-- =====================================================================
-- A. FUNCTIONS
-- =====================================================================

-- Subtotal = SUM(Price x Quantity) over the order's items
CREATE FUNCTION fn_order_subtotal(p_order_id INT)
RETURNS DECIMAL(10,2)
READS SQL DATA
BEGIN
    DECLARE v_subtotal DECIMAL(10,2);
    SELECT COALESCE(SUM(m.Price * oi.Quantity), 0)
      INTO v_subtotal
      FROM ORDER_ITEM oi
      JOIN MENU_ITEM m ON m.Item_ID = oi.Item_ID
     WHERE oi.Order_ID = p_order_id;
    RETURN v_subtotal;
END$$

-- Total_Amount (derived attribute in the ER diagram)
-- = subtotal minus the coupon's percentage discount (if any)
CREATE FUNCTION fn_order_total(p_order_id INT)
RETURNS DECIMAL(10,2)
READS SQL DATA
BEGIN
    DECLARE v_discount DECIMAL(5,2) DEFAULT 0;
    SELECT COALESCE(c.Discount, 0)
      INTO v_discount
      FROM ORDERS o
      LEFT JOIN COUPON c ON c.Coupon_Code = o.Coupon_Code
     WHERE o.Order_ID = p_order_id;
    RETURN ROUND(fn_order_subtotal(p_order_id) * (1 - v_discount / 100), 2);
END$$

-- =====================================================================
-- B. TRIGGERS
-- =====================================================================

-- Rule 1: every new order starts in status 'Placed'
CREATE TRIGGER trg_orders_before_insert
BEFORE INSERT ON ORDERS
FOR EACH ROW
BEGIN
    IF NEW.Order_Status <> 'Placed' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A new order must start with status Placed.';
    END IF;
END$$

-- Rule 2: status can only move forward ONE step at a time
--         Placed -> Preparing -> Out for Delivery -> Delivered
-- Rule 3: an order can go Out for Delivery only after an agent is assigned
-- Rule 4: customer / restaurant / address of an order cannot be changed
CREATE TRIGGER trg_orders_status_flow
BEFORE UPDATE ON ORDERS
FOR EACH ROW
BEGIN
    DECLARE v_old_step INT;
    DECLARE v_new_step INT;

    IF NEW.Customer_ID <> OLD.Customer_ID
       OR NEW.Restaurant_ID <> OLD.Restaurant_ID
       OR NEW.Address_No <> OLD.Address_No THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Customer, restaurant and address of an order cannot be changed.';
    END IF;

    IF NEW.Order_Status <> OLD.Order_Status THEN
        SET v_old_step = FIELD(OLD.Order_Status, 'Placed','Preparing','Out for Delivery','Delivered');
        SET v_new_step = FIELD(NEW.Order_Status, 'Placed','Preparing','Out for Delivery','Delivered');

        IF v_new_step <> v_old_step + 1 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Invalid status change: order status must move forward one step at a time (Placed -> Preparing -> Out for Delivery -> Delivered).';
        END IF;

        IF NEW.Order_Status = 'Out for Delivery'
           AND NOT EXISTS (SELECT 1 FROM DELIVERY d WHERE d.Order_ID = NEW.Order_ID) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Assign a delivery agent before marking the order Out for Delivery.';
        END IF;
    END IF;
END$$

-- Rule 5: when an order is Delivered -> close its delivery and
--         mark a pending Cash-on-Delivery payment as Paid
CREATE TRIGGER trg_orders_after_delivered
AFTER UPDATE ON ORDERS
FOR EACH ROW
BEGIN
    IF NEW.Order_Status = 'Delivered' AND OLD.Order_Status <> 'Delivered' THEN
        UPDATE DELIVERY
           SET Delivery_Status = 'Delivered'
         WHERE Order_ID = NEW.Order_ID;

        UPDATE PAYMENT
           SET Pay_Status = 'Paid'
         WHERE Order_ID = NEW.Order_ID
           AND Method = 'COD'
           AND Pay_Status = 'Pending';
    END IF;
END$$

-- Rule 6: an order can only contain items from ITS OWN restaurant
-- Rule 7: items can only be added while the order is still 'Placed'
CREATE TRIGGER trg_order_item_before_insert
BEFORE INSERT ON ORDER_ITEM
FOR EACH ROW
BEGIN
    DECLARE v_order_rest  INT;
    DECLARE v_item_rest   INT;
    DECLARE v_status      VARCHAR(20);

    SELECT Restaurant_ID, Order_Status INTO v_order_rest, v_status
      FROM ORDERS WHERE Order_ID = NEW.Order_ID;

    SELECT Restaurant_ID INTO v_item_rest
      FROM MENU_ITEM WHERE Item_ID = NEW.Item_ID;

    IF v_item_rest IS NOT NULL AND v_item_rest <> v_order_rest THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'This item belongs to a different restaurant. An order can only contain items from one restaurant.';
    END IF;

    IF v_status IS NOT NULL AND v_status <> 'Placed' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Items can only be added while the order is still Placed.';
    END IF;
END$$

-- Same restaurant rule when an order line is edited
CREATE TRIGGER trg_order_item_before_update
BEFORE UPDATE ON ORDER_ITEM
FOR EACH ROW
BEGIN
    DECLARE v_status VARCHAR(20);
    SELECT Order_Status INTO v_status FROM ORDERS WHERE Order_ID = NEW.Order_ID;

    IF v_status <> 'Placed' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Order items cannot be changed once the restaurant starts preparing the order.';
    END IF;

    IF NEW.Item_ID <> OLD.Item_ID
       AND (SELECT Restaurant_ID FROM MENU_ITEM WHERE Item_ID = NEW.Item_ID)
        <> (SELECT Restaurant_ID FROM ORDERS WHERE Order_ID = NEW.Order_ID) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'This item belongs to a different restaurant. An order can only contain items from one restaurant.';
    END IF;
END$$

-- Rule 8: an agent can be assigned only to an order that is 'Preparing'
-- Rule 9: an agent cannot hold two active deliveries at the same time
CREATE TRIGGER trg_delivery_before_insert
BEFORE INSERT ON DELIVERY
FOR EACH ROW
BEGIN
    DECLARE v_status VARCHAR(20);
    SELECT Order_Status INTO v_status FROM ORDERS WHERE Order_ID = NEW.Order_ID;

    IF v_status <> 'Preparing' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'A delivery agent can only be assigned to an order that is Preparing.';
    END IF;

    IF EXISTS (SELECT 1 FROM DELIVERY
                WHERE Agent_ID = NEW.Agent_ID
                  AND Delivery_Status = 'Assigned') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'This delivery agent is already on an active delivery.';
    END IF;

    SET NEW.Delivery_Status = 'Assigned';
END$$

-- Rule 10: assigning an agent moves the order to 'Out for Delivery'
CREATE TRIGGER trg_delivery_after_insert
AFTER INSERT ON DELIVERY
FOR EACH ROW
BEGIN
    UPDATE ORDERS
       SET Order_Status = 'Out for Delivery'
     WHERE Order_ID = NEW.Order_ID;
END$$

-- Rule 11: a review is allowed only for a Delivered order,
--          and only by the customer who placed that order
CREATE TRIGGER trg_review_before_insert
BEFORE INSERT ON REVIEW
FOR EACH ROW
BEGIN
    DECLARE v_status   VARCHAR(20);
    DECLARE v_customer INT;

    SELECT Order_Status, Customer_ID INTO v_status, v_customer
      FROM ORDERS WHERE Order_ID = NEW.Order_ID;

    IF v_status <> 'Delivered' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'You can review an order only after it has been delivered.';
    END IF;

    IF v_customer <> NEW.Customer_ID THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only the customer who placed this order can review it.';
    END IF;
END$$

-- =====================================================================
-- C. STORED PROCEDURES
-- =====================================================================

-- Place an order as ONE transaction: ORDERS + ORDER_ITEM + PAYMENT.
-- If any step fails (bad item, wrong restaurant, bad coupon...), everything
-- is rolled back and the error is passed back to the caller.
-- p_items is JSON, e.g. '[{"item":1,"qty":2},{"item":2,"qty":1}]'
CREATE PROCEDURE sp_place_order(
    IN  p_customer_id   INT,
    IN  p_address_no    INT,
    IN  p_restaurant_id INT,
    IN  p_coupon_code   VARCHAR(20),
    IN  p_method        VARCHAR(10),
    IN  p_items         JSON,
    OUT p_order_id      INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_items IS NULL OR JSON_LENGTH(p_items) = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'An order must contain at least one item.';
    END IF;

    IF p_coupon_code = '' THEN
        SET p_coupon_code = NULL;
    END IF;

    START TRANSACTION;

    INSERT INTO ORDERS (Order_Status, Customer_ID, Address_No, Restaurant_ID, Coupon_Code)
    VALUES ('Placed', p_customer_id, p_address_no, p_restaurant_id, p_coupon_code);

    SET p_order_id = LAST_INSERT_ID();

    INSERT INTO ORDER_ITEM (Order_ID, Item_ID, Quantity)
    SELECT p_order_id, j.item, j.qty
      FROM JSON_TABLE(p_items, '$[*]'
             COLUMNS (item INT PATH '$.item',
                      qty  INT PATH '$.qty')) AS j;

    -- UPI / Card are paid online at order time; COD is collected on delivery
    INSERT INTO PAYMENT (Method, Pay_Status, Order_ID)
    VALUES (p_method,
            CASE WHEN p_method = 'COD' THEN 'Pending' ELSE 'Paid' END,
            p_order_id);

    COMMIT;
END$$

-- Assign an agent to an order (triggers check the rules and
-- move the order to Out for Delivery)
CREATE PROCEDURE sp_assign_delivery(
    IN  p_order_id    INT,
    IN  p_agent_id    INT,
    OUT p_delivery_id INT
)
BEGIN
    INSERT INTO DELIVERY (Order_ID, Agent_ID) VALUES (p_order_id, p_agent_id);
    SET p_delivery_id = LAST_INSERT_ID();
END$$

-- Agent marks a delivery as completed: the order becomes Delivered,
-- and the trigger closes the delivery and settles a COD payment.
CREATE PROCEDURE sp_mark_delivered(IN p_delivery_id INT)
BEGIN
    DECLARE v_order_id INT;

    SELECT Order_ID INTO v_order_id
      FROM DELIVERY
     WHERE Delivery_ID = p_delivery_id AND Delivery_Status = 'Assigned';

    IF v_order_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'No active delivery found with this ID.';
    END IF;

    UPDATE ORDERS SET Order_Status = 'Delivered' WHERE Order_ID = v_order_id;
END$$

DELIMITER ;

-- =====================================================================
-- D. VIEWS (derived attributes + reports)
-- =====================================================================

-- Full order summary with derived Total_Amount
CREATE OR REPLACE VIEW v_order_summary AS
SELECT o.Order_ID,
       o.Customer_ID,
       CONCAT(u.First_Name, ' ', u.Last_Name) AS Customer_Name,
       a.City                                 AS Delivery_City,
       r.Restaurant_ID,
       r.Rest_Name,
       o.Order_Status,
       fn_order_subtotal(o.Order_ID)          AS Subtotal,
       o.Coupon_Code,
       COALESCE(c.Discount, 0)                AS Discount_Percent,
       fn_order_total(o.Order_ID)             AS Total_Amount,
       p.Method                               AS Payment_Method,
       p.Pay_Status
FROM   ORDERS o
JOIN   USERS u      ON u.User_ID = o.Customer_ID
JOIN   ADDRESS a    ON a.Customer_ID = o.Customer_ID AND a.Address_No = o.Address_No
JOIN   RESTAURANT r ON r.Restaurant_ID = o.Restaurant_ID
LEFT JOIN COUPON c  ON c.Coupon_Code = o.Coupon_Code
LEFT JOIN PAYMENT p ON p.Order_ID = o.Order_ID;

-- Avg_Rating (derived attribute of RESTAURANT in the ER diagram)
CREATE OR REPLACE VIEW v_restaurant_rating AS
SELECT r.Restaurant_ID,
       r.Rest_Name,
       ROUND(AVG(rv.Rating), 2) AS Avg_Rating,
       COUNT(rv.Review_ID)      AS Total_Reviews
FROM   RESTAURANT r
LEFT JOIN ORDERS o  ON o.Restaurant_ID = r.Restaurant_ID
LEFT JOIN REVIEW rv ON rv.Order_ID = o.Order_ID
GROUP  BY r.Restaurant_ID, r.Rest_Name;

-- Sales report: revenue from delivered orders, per restaurant
CREATE OR REPLACE VIEW v_restaurant_sales AS
SELECT r.Restaurant_ID,
       r.Rest_Name,
       COUNT(o.Order_ID)                        AS Delivered_Orders,
       COALESCE(SUM(fn_order_total(o.Order_ID)), 0) AS Total_Revenue
FROM   RESTAURANT r
LEFT JOIN ORDERS o ON o.Restaurant_ID = r.Restaurant_ID
                  AND o.Order_Status = 'Delivered'
GROUP  BY r.Restaurant_ID, r.Rest_Name;

-- Top-selling items (all non-failed orders)
CREATE OR REPLACE VIEW v_top_items AS
SELECT m.Item_ID,
       m.Item_Name,
       r.Rest_Name,
       SUM(oi.Quantity)           AS Units_Sold,
       SUM(oi.Quantity * m.Price) AS Gross_Sales
FROM   ORDER_ITEM oi
JOIN   MENU_ITEM m  ON m.Item_ID = oi.Item_ID
JOIN   RESTAURANT r ON r.Restaurant_ID = m.Restaurant_ID
JOIN   PAYMENT p    ON p.Order_ID = oi.Order_ID
WHERE  p.Pay_Status <> 'Failed'
GROUP  BY m.Item_ID, m.Item_Name, r.Rest_Name;

-- Delivery agent performance
CREATE OR REPLACE VIEW v_agent_performance AS
SELECT da.Agent_ID,
       CONCAT(u.First_Name, ' ', u.Last_Name)                 AS Agent_Name,
       da.Vehicle_No,
       SUM(CASE WHEN d.Delivery_Status = 'Delivered' THEN 1 ELSE 0 END) AS Completed,
       SUM(CASE WHEN d.Delivery_Status = 'Assigned'  THEN 1 ELSE 0 END) AS Active
FROM   DELIVERY_AGENT da
JOIN   USERS u       ON u.User_ID = da.Agent_ID
LEFT JOIN DELIVERY d ON d.Agent_ID = da.Agent_ID
GROUP  BY da.Agent_ID, u.First_Name, u.Last_Name, da.Vehicle_No;

-- =====================================================================
-- Verification (screenshot these for the Phase 2 PPT)
-- =====================================================================
SELECT * FROM v_order_summary ORDER BY Order_ID;
SELECT * FROM v_restaurant_rating ORDER BY Avg_Rating DESC;
SELECT * FROM v_restaurant_sales ORDER BY Total_Revenue DESC;
SELECT * FROM v_top_items ORDER BY Units_Sold DESC LIMIT 10;
SELECT * FROM v_agent_performance;
