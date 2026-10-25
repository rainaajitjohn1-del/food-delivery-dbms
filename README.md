# Food Delivery & Restaurant Order Management — Java + JDBC backend

BACSE202 Course Project · MySQL 8.4 · Java 21 · JDBC (MySQL Connector/J)

## Project structure

```
FoodDeliveryApp/
├── db.properties                  <- MySQL URL, user, password (edit the password!)
└── src/com/fooddelivery/
    ├── ConsoleDemo.java           <- text-menu demo of every JDBC operation (run this first)
    ├── db/
    │   ├── DBConnection.java      <- opens JDBC connections (reads db.properties)
    │   └── AppException.java      <- turns MySQL errors into readable messages
    ├── model/                     <- data classes: User, Restaurant, MenuItem, Address,
    │                                 Order, OrderLine, Delivery
    ├── dao/                       <- one Data Access Object per table group (all the SQL)
    │                                 UserDAO, AddressDAO, RestaurantDAO, MenuItemDAO,
    │                                 OrderDAO, DeliveryDAO, ReviewDAO, ReportDAO
    ├── service/
    │   └── FoodDeliveryService.java  <- THE ONLY CLASS THE UI CALLS
    └── ui/                        <- (front-end team) Swing / JavaFX screens go here
```

## Setup in IntelliJ IDEA (one time)

1. Make sure the database exists: run `01_create_tables.sql`, `02_insert_data.sql`,
   `03_business_logic.sql` in MySQL Workbench (plain ⚡ each).
2. IntelliJ → **File → Open** → select the `FoodDeliveryApp` folder.
3. **File → Project Structure → Project** → SDK = **21** (Temurin).
4. **File → Project Structure → Modules → Sources tab** → make sure `src` is marked
   as **Sources** (blue folder).
5. **File → Project Structure → Libraries → + → Java** → choose the
   `mysql-connector-j-x.x.x.jar` you downloaded → OK.
6. Open `db.properties` and replace `CHANGE_ME` with your MySQL root password.
7. **Run → Edit Configurations** → make sure **Working directory** is the
   `FoodDeliveryApp` folder (so `db.properties` is found).
8. Open `ConsoleDemo.java` → click the green ▶ next to `main` → the first line should say
   `Connected to MySQL database 'food_delivery'.`

## For the front-end team — how to call the backend

```java
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.db.AppException;
import com.fooddelivery.model.*;

FoodDeliveryService api = new FoodDeliveryService();   // create once, reuse everywhere

try {
    User user = api.login(emailField.getText());
    switch (user.role()) {
        case "CUSTOMER" -> new CustomerHome(user).setVisible(true);
        case "MANAGER"  -> new ManagerHome(user).setVisible(true);
        case "AGENT"    -> new AgentHome(user).setVisible(true);
    }
} catch (AppException ex) {
    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
}
```

Rules:
* **Every** call can throw `AppException` → catch it and show `ex.getMessage()` in a popup.
  The message is already user-friendly (e.g. "Rating must be between 1 and 5.").
* **No SQL in the UI.** Only call `FoodDeliveryService` methods.
* **No hardcoded data.** Restaurants, menus, orders etc. always come from these methods.
* Model objects have a readable `toString()`, so you can put them straight into a
  `JComboBox` / `JList` (e.g. `new JComboBox<>(api.getRestaurants().toArray(new Restaurant[0]))`).
* Report methods return `List<String[]>` where **row 0 = column headers**:
  ```java
  List<String[]> r = api.getSalesReport();
  JTable table = new JTable(r.subList(1, r.size()).toArray(new String[0][]), r.get(0));
  ```

### Method list

| Screen | Method | Returns |
|---|---|---|
| Login | `login(email)` | `User` — `role()` is CUSTOMER / MANAGER / AGENT |
| Register | `registerCustomer(first, last, email, phone)` | new User_ID |
| Profile | `addPhone(userId, phone)` | – |
| Restaurants | `getRestaurants()` | `List<Restaurant>` (with avg rating) |
| Menu | `getMenu(restaurantId)` | `List<MenuItem>` |
| Cart | `getAddresses(customerId)` / `addAddress(customerId, city)` | `List<Address>` / `Address` |
| Cart | `getTotal(itemQty, couponCode)` | total after discount (`double`) |
| Cart | `placeOrder(customerId, addressNo, restaurantId, itemQty, couponCode, payMethod)` | new Order_ID |
| My orders | `getOrderHistory(customerId)` | `List<Order>` |
| Order details | `getOrder(orderId)` / `getOrderItems(orderId)` | `Order` / `List<OrderLine>` |
| Review | `addReview(customerId, orderId, rating)` — enable only if `order.canBeReviewed()` | – |
| Manager menu | `getMyRestaurant(managerId)` | `Restaurant` |
| Manager menu | `addMenuItem(restId, name, price, category)` / `updateMenuItem(itemId, name, price, category)` / `deleteMenuItem(itemId)` | – |
| Manager orders | `getRestaurantOrders(restaurantId)` | `List<Order>` |
| Manager orders | `updateOrderStatus(orderId, order.nextStatus())` | – |
| Assign agent | `getAvailableAgents()` then `assignDelivery(orderId, agentId)` | `List<User>` / Delivery_ID |
| Agent | `getMyDeliveries(agentId)` | `List<Delivery>` (`collectCash()` = COD to collect) |
| Agent | `markDelivered(deliveryId)` | – |
| Reports | `getSalesReport()`, `getTopItems()`, `getRestaurantRatings()`, `getAgentPerformance()` | `List<String[]>` |

`itemQty` is a `Map<Integer, Integer>` of **Item_ID → quantity**, e.g.
```java
Map<Integer, Integer> cart = new LinkedHashMap<>();
cart.put(17, 2);   // 2 x Hakka Noodles
cart.put(18, 1);   // 1 x Chilli Chicken
double total = api.getTotal(cart, "FEST10");                       // 558.0
int orderId  = api.placeOrder(14, 1, 4, cart, "FEST10", "UPI");
```

Dropdown values: `FoodDeliveryService.CATEGORIES`, `FoodDeliveryService.PAYMENT_METHODS`.

### Order workflow (enforced by the database)
`Placed → Preparing → Out for Delivery → Delivered`
* Manager: Placed → Preparing with `updateOrderStatus`.
* Preparing → Out for Delivery happens **automatically** when `assignDelivery` is called.
* Out for Delivery → Delivered happens when the agent calls `markDelivered`
  (COD payment becomes Paid automatically).

### Test logins (sample data)
| Role | Email |
|---|---|
| Customer | `arjun.nair@gmail.com` (ID 11), `rohan.verma@gmail.com` (ID 14) |
| Manager | `rahul.menon@spicehub.in` (Spice Hub), `ananya.reddy@dragonwok.in` (Dragon Wok) |
| Agent | `imran.khan@fooddel.in`, `manoj.das@fooddel.in` (has a COD delivery to collect) |
