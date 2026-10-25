-- =====================================================================
-- BACSE202 Course Project - Food Delivery & Restaurant Order Management
-- Phase 2 | Stage 8 demo: business rules in action
-- Run AFTER scripts 01, 02 and 03.
--
-- HOW TO USE IN WORKBENCH: do NOT run this whole file at once.
-- Put the cursor on ONE statement and press the lightning bolt with the
-- cursor (Execute current statement, Cmd+Enter). Screenshot each result.
-- Statements marked "EXPECTED ERROR" should fail with the message shown.
-- To reset the data afterwards, re-run scripts 01, 02 and 03.
-- =====================================================================

USE food_delivery;

-- ---------------------------------------------------------------------
-- DEMO 1: Place an order (transaction) and see its derived total
-- Customer 14 (Rohan), address 1, Dragon Wok (4), coupon FEST10, UPI
-- 2 x Hakka Noodles (190) + 1 x Chilli Chicken (240) = 620 - 10% = 558
-- ---------------------------------------------------------------------
CALL sp_place_order(14, 1, 4, 'FEST10', 'UPI',
                    '[{"item":17,"qty":2},{"item":18,"qty":1}]', @new_order);
SELECT @new_order AS New_Order_ID;
SELECT * FROM v_order_summary WHERE Order_ID = @new_order;

-- ---------------------------------------------------------------------
-- DEMO 2 (EXPECTED ERROR): item from a different restaurant
-- Item 1 (Chicken Biryani) belongs to Spice Hub, not Dragon Wok.
-- The whole order is rolled back - nothing is saved.
-- ---------------------------------------------------------------------
CALL sp_place_order(14, 1, 4, NULL, 'COD',
                    '[{"item":17,"qty":1},{"item":1,"qty":1}]', @bad_order);
-- Error: This item belongs to a different restaurant...
SELECT COUNT(*) AS Orders_In_Table FROM ORDERS;   -- unchanged, proves rollback

-- ---------------------------------------------------------------------
-- DEMO 3 (EXPECTED ERROR): skipping a status step
-- ---------------------------------------------------------------------
UPDATE ORDERS SET Order_Status = 'Delivered' WHERE Order_ID = @new_order;
-- Error: Invalid status change...

-- Correct flow: Placed -> Preparing
UPDATE ORDERS SET Order_Status = 'Preparing' WHERE Order_ID = @new_order;

-- ---------------------------------------------------------------------
-- DEMO 4 (EXPECTED ERROR): agent already on an active delivery
-- Agent 6 (Suresh) is currently delivering order 17.
-- ---------------------------------------------------------------------
CALL sp_assign_delivery(@new_order, 6, @d);
-- Error: This delivery agent is already on an active delivery.

-- Agent 9 (Imran) is free -> order automatically becomes Out for Delivery
CALL sp_assign_delivery(@new_order, 9, @d);
SELECT Order_ID, Order_Status FROM ORDERS WHERE Order_ID = @new_order;

-- ---------------------------------------------------------------------
-- DEMO 5 (EXPECTED ERROR): reviewing an order that is not delivered yet
-- ---------------------------------------------------------------------
INSERT INTO REVIEW (Rating, Customer_ID, Order_ID) VALUES (5, 14, @new_order);
-- Error: You can review an order only after it has been delivered.

-- Agent completes the delivery -> order Delivered, delivery closed
CALL sp_mark_delivered(@d);
SELECT * FROM v_order_summary WHERE Order_ID = @new_order;

-- ---------------------------------------------------------------------
-- DEMO 6 (EXPECTED ERROR): someone else reviewing Rohan's order
-- ---------------------------------------------------------------------
INSERT INTO REVIEW (Rating, Customer_ID, Order_ID) VALUES (4, 11, @new_order);
-- Error: Only the customer who placed this order can review it.

-- Rohan reviews his own delivered order -> allowed
INSERT INTO REVIEW (Rating, Customer_ID, Order_ID) VALUES (5, 14, @new_order);

-- ---------------------------------------------------------------------
-- DEMO 7: COD payment is settled automatically on delivery
-- Order 18 is Out for Delivery with COD Pending (delivery 18, agent 7)
-- ---------------------------------------------------------------------
SELECT Order_ID, Method, Pay_Status FROM PAYMENT WHERE Order_ID = 18;  -- Pending
CALL sp_mark_delivered(18);
SELECT Order_ID, Method, Pay_Status FROM PAYMENT WHERE Order_ID = 18;  -- Paid

-- ---------------------------------------------------------------------
-- DEMO 8 (EXPECTED ERRORS): constraint violations from the DDL
-- ---------------------------------------------------------------------
INSERT INTO REVIEW (Rating, Customer_ID, Order_ID) VALUES (6, 17, 8);
-- Error: Check constraint 'chk_review_rating' is violated.

INSERT INTO USERS (First_Name, Last_Name, Email)
VALUES ('Test', 'User', 'arjun.nair@gmail.com');
-- Error: Duplicate entry ... for key 'USERS.uq_users_email'

INSERT INTO MENU_ITEM (Item_Name, Price, Category, Restaurant_ID)
VALUES ('Free Biryani', -50, 'Main Course', 1);
-- Error: Check constraint 'chk_item_price' is violated.

CALL sp_place_order(11, 1, 1, 'FAKE50', 'UPI', '[{"item":1,"qty":1}]', @x);
-- Error: foreign key constraint fails (fk_orders_coupon) - coupon does not exist
