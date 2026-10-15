import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Grocery Management System - Java OOP conversion.
 *
 * Features:
 * - Product and Inventory management
 * - Person base class with Customer and Admin subclasses
 * - BillItem and Bill classes
 * - Checkout with stock validation and stock deduction
 * - Product search, restocking, removal and inventory valuation
 * - Sales history
 * - JSON persistence in grocery_data.json (no external libraries required)
 *
 * Compile: javac GroceryManagementSystem.java
 * Run:     java GroceryManagementSystem
 *
 * Note: The simple role selection is for a coursework/demo project, not secure
 * authentication. This is a standalone OOP conversion, so compare it with your
 * original Python file before replacing any original project-specific features.
 */
public class GroceryManagementSystem {
    private static final String DATA_FILE = "grocery_data.json";
    private final Scanner scanner = new Scanner(System.in);
    private final Inventory inventory = new Inventory();
    private final List<Bill> salesHistory = new ArrayList<>();
    private final JsonStore store = new JsonStore(DATA_FILE);

    public static void main(String[] args) {
        new GroceryManagementSystem().run();
    }

    private void run() {
        loadData();
        System.out.println("\n======================================");
        System.out.println("       GROCERY MANAGEMENT SYSTEM");
        System.out.println("======================================");
        boolean running = true;
        while (running) {
            System.out.println("\n1. Admin");
            System.out.println("2. Customer");
            System.out.println("3. Exit");
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> adminMenu(new Admin("Administrator"));
                case 2 -> customerMenu(new Customer(readNonEmpty("Enter customer name: ")));
                case 3 -> {
                    saveData();
                    running = false;
                    System.out.println("Data saved. Goodbye!");
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
    }

    private void adminMenu(Admin admin) {
        boolean back = false;
        while (!back) {
            admin.showWelcome();
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Add product");
            System.out.println("2. Restock product");
            System.out.println("3. Remove product");
            System.out.println("4. Search product");
            System.out.println("5. Display inventory");
            System.out.println("6. View sales history");
            System.out.println("7. View inventory value");
            System.out.println("8. Save data");
            System.out.println("9. Back to main menu");
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> addProduct();
                case 2 -> restockProduct();
                case 3 -> removeProduct();
                case 4 -> searchProducts();
                case 5 -> inventory.displayProducts();
                case 6 -> displaySalesHistory();
                case 7 -> System.out.printf(Locale.US, "Total inventory value: %.2f%n",
                        inventory.getTotalStockValue());
                case 8 -> saveData();
                case 9 -> back = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void customerMenu(Customer customer) {
        boolean back = false;
        while (!back) {
            customer.showWelcome();
            System.out.println("\n--- CUSTOMER MENU ---");
            System.out.println("1. View products");
            System.out.println("2. Search products");
            System.out.println("3. Buy products / Checkout");
            System.out.println("4. View my recent bill");
            System.out.println("5. Back to main menu");
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> inventory.displayProducts();
                case 2 -> searchProducts();
                case 3 -> checkout(customer);
                case 4 -> showLatestBillFor(customer.getName());
                case 5 -> back = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void addProduct() {
        System.out.println("\n--- ADD PRODUCT ---");
        String name = readNonEmpty("Product name: ");
        if (inventory.findByName(name) != null) {
            System.out.println("A product with that name already exists.");
            return;
        }
        double price = readPositiveDouble("Price per unit: ");
        int quantity = readNonNegativeInt("Initial quantity: ");
        String category = readNonEmpty("Category: ");
        Product product = new Product(inventory.nextId(), name, price, quantity, category);
        inventory.addProduct(product);
        saveData();
        System.out.println("Product added successfully. Product ID: " + product.getId());
    }

    private void restockProduct() {
        inventory.displayProducts();
        int id = readInt("Enter product ID to restock: ");
        Product product = inventory.findById(id);
        if (product == null) {
            System.out.println("Product not found.");
            return;
        }
        int quantity = readPositiveInt("Quantity to add: ");
        product.restock(quantity);
        saveData();
        System.out.println("Stock updated. New quantity: " + product.getQuantity());
    }

    private void removeProduct() {
        inventory.displayProducts();
        int id = readInt("Enter product ID to remove: ");
        Product removed = inventory.removeProduct(id);
        if (removed == null) {
            System.out.println("Product not found.");
        } else {
            saveData();
            System.out.println("Removed product: " + removed.getName());
        }
    }

    private void searchProducts() {
        String query = readNonEmpty("Enter product name or category to search: ");
        List<Product> results = inventory.search(query);
        if (results.isEmpty()) {
            System.out.println("No matching products found.");
        } else {
            System.out.println("\n--- SEARCH RESULTS ---");
            Inventory.printProducts(results);
        }
    }

    private void checkout(Customer customer) {
        if (inventory.getProducts().isEmpty()) {
            System.out.println("No products are currently available.");
            return;
        }
        Bill bill = new Bill(customer.getName());
        while (true) {
            inventory.displayProducts();
            int id = readInt("Enter product ID (0 to finish): ");
            if (id == 0) break;
            Product product = inventory.findById(id);
            if (product == null) {
                System.out.println("Product not found.");
                continue;
            }
            int alreadyInBill = bill.getQuantityForProduct(id);
            int available = product.getQuantity() - alreadyInBill;
            if (available <= 0) {
                System.out.println("No more units of this product are available for this bill.");
                continue;
            }
            System.out.println("Available to add: " + available);
            int quantity = readPositiveInt("Quantity: ");
            if (quantity > available) {
                System.out.println("Insufficient stock. Maximum you can add: " + available);
                continue;
            }
            bill.addItem(product, quantity);
            System.out.println("Added " + quantity + " x " + product.getName());
        }
        if (bill.getItems().isEmpty()) {
            System.out.println("No items selected. Checkout cancelled.");
            return;
        }
        if (!bill.canBeFulfilled(inventory)) {
            System.out.println("Stock changed or is insufficient. Nothing was charged.");
            return;
        }
        bill.completeCheckout(inventory);
        salesHistory.add(bill);
        bill.printReceipt();
        saveData();
    }

    private void displaySalesHistory() {
        if (salesHistory.isEmpty()) {
            System.out.println("No sales recorded yet.");
            return;
        }
        System.out.println("\n--- SALES HISTORY ---");
        for (Bill bill : salesHistory) {
            System.out.printf(Locale.US, "Bill #%d | %s | %s | Total: %.2f%n",
                    bill.getBillId(), bill.getCustomerName(), bill.getCreatedAt(), bill.getTotal());
        }
    }

    private void showLatestBillFor(String customerName) {
        for (int i = salesHistory.size() - 1; i >= 0; i--) {
            Bill bill = salesHistory.get(i);
            if (bill.getCustomerName().equalsIgnoreCase(customerName)) {
                bill.printReceipt();
                return;
            }
        }
        System.out.println("No completed bill found for " + customerName + ".");
    }

    private void loadData() {
        try {
            Map<String, Object> data = store.load();
            if (data == null) return;
            Object productsObject = data.get("products");
            if (productsObject instanceof List<?> products) {
                for (Object object : products) {
                    if (object instanceof Map<?, ?> map) {
                        Product product = Product.fromMap(castMap(map));
                        if (product != null) inventory.addProduct(product);
                    }
                }
            }
            Object salesObject = data.get("sales");
            if (salesObject instanceof List<?> sales) {
                for (Object object : sales) {
                    if (object instanceof Map<?, ?> map) {
                        Bill bill = Bill.fromMap(castMap(map));
                        if (bill != null) salesHistory.add(bill);
                    }
                }
            }
            System.out.println("Loaded " + inventory.getProducts().size() + " product(s) and "
                    + salesHistory.size() + " sale(s) from " + DATA_FILE + ".");
        } catch (Exception e) {
            System.out.println("Could not load saved data: " + e.getMessage());
            System.out.println("Starting with an empty inventory.");
        }
    }

    private void saveData() {
        Map<String, Object> data = new LinkedHashMap<>();
        List<Object> products = new ArrayList<>();
        for (Product product : inventory.getProducts()) products.add(product.toMap());
        List<Object> sales = new ArrayList<>();
        for (Bill bill : salesHistory) sales.add(bill.toMap());
        data.put("products", products);
        data.put("sales", sales);
        try {
            store.save(data);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    static Map<String, Object> castMap(Map<?, ?> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() instanceof String key) result.put(key, entry.getValue());
        }
        return result;
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) return value;
            System.out.println("Enter a number greater than zero.");
        }
    }

    private int readNonNegativeInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value >= 0) return value;
            System.out.println("Enter zero or a positive number.");
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (Double.isFinite(value) && value > 0) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a valid price greater than zero.");
        }
    }
}

abstract class Person {
    private final String name;

    protected Person(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public abstract void showWelcome();
}

class Admin extends Person {
    public Admin(String name) { super(name); }

    @Override
    public void showWelcome() {
        System.out.println("\nWelcome, " + getName() + " (Admin).");
    }
}

class Customer extends Person {
    public Customer(String name) { super(name); }

    @Override
    public void showWelcome() {
        System.out.println("\nWelcome, " + getName() + "!");
    }
}

class Product {
    private final int id;
    private String name;
    private double price;
    private int quantity;
    private String category;

    public Product(int id, String name, double price, int quantity, String category) {
        if (id <= 0) throw new IllegalArgumentException("Product ID must be positive.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        if (!Double.isFinite(price) || price <= 0) throw new IllegalArgumentException("Price must be positive.");
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative.");
        this.id = id;
        this.name = name.trim();
        this.price = price;
        this.quantity = quantity;
        this.category = category == null ? "General" : category.trim();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getCategory() { return category; }
    public double getStockValue() { return price * quantity; }

    public void restock(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Restock quantity must be positive.");
        quantity += amount;
    }

    public void reduceStock(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        if (amount > quantity) throw new IllegalArgumentException("Insufficient stock for " + name + ".");
        quantity -= amount;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("price", price);
        map.put("quantity", quantity);
        map.put("category", category);
        return map;
    }

    public static Product fromMap(Map<String, Object> map) {
        try {
            return new Product(number(map.get("id")).intValue(), String.valueOf(map.get("name")),
                    number(map.get("price")).doubleValue(), number(map.get("quantity")).intValue(),
                    String.valueOf(map.getOrDefault("category", "General")));
        } catch (Exception e) {
            System.out.println("Skipping an invalid product record.");
            return null;
        }
    }

    private static Number number(Object value) {
        if (value instanceof Number n) return n;
        return new BigDecimal(String.valueOf(value));
    }
}

class Inventory {
    private final List<Product> products = new ArrayList<>();

    public List<Product> getProducts() { return products; }

    public int nextId() {
        return products.stream().mapToInt(Product::getId).max().orElse(0) + 1;
    }

    public void addProduct(Product product) {
        if (findById(product.getId()) != null) throw new IllegalArgumentException("Duplicate product ID.");
        if (findByName(product.getName()) != null) throw new IllegalArgumentException("Duplicate product name.");
        products.add(product);
    }

    public Product findById(int id) {
        for (Product product : products) if (product.getId() == id) return product;
        return null;
    }

    public Product findByName(String name) {
        for (Product product : products) {
            if (product.getName().equalsIgnoreCase(name.trim())) return product;
        }
        return null;
    }

    public Product removeProduct(int id) {
        Product product = findById(id);
        if (product != null) products.remove(product);
        return product;
    }

    public List<Product> search(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        List<Product> result = new ArrayList<>();
        for (Product product : products) {
            if (product.getName().toLowerCase(Locale.ROOT).contains(q)
                    || product.getCategory().toLowerCase(Locale.ROOT).contains(q)) result.add(product);
        }
        return result;
    }

    public double getTotalStockValue() {
        return products.stream().mapToDouble(Product::getStockValue).sum();
    }

    public void displayProducts() {
        System.out.println("\n--- CURRENT INVENTORY ---");
        if (products.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }
        printProducts(products);
    }

    public static void printProducts(List<Product> products) {
        System.out.printf("%-5s %-24s %-12s %-10s %-18s %-12s%n",
                "ID", "Product", "Price", "Quantity", "Category", "Stock Value");
        System.out.println("-".repeat(86));
        products.stream().sorted(Comparator.comparingInt(Product::getId)).forEach(p ->
                System.out.printf(Locale.US, "%-5d %-24s %-12.2f %-10d %-18s %-12.2f%n",
                        p.getId(), truncate(p.getName(), 23), p.getPrice(), p.getQuantity(),
                        truncate(p.getCategory(), 17), p.getStockValue()));
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}

class BillItem {
    private final int productId;
    private final String productName;
    private final double unitPrice;
    private final int quantity;

    public BillItem(Product product, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        this.productId = product.getId();
        this.productName = product.getName();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
    }

    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return unitPrice * quantity; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("productId", productId);
        map.put("productName", productName);
        map.put("unitPrice", unitPrice);
        map.put("quantity", quantity);
        return map;
    }

    public static BillItem fromMap(Map<String, Object> map) {
        try {
            int productId = ((Number) map.get("productId")).intValue();
            String name = String.valueOf(map.get("productName"));
            double price = ((Number) map.get("unitPrice")).doubleValue();
            int quantity = ((Number) map.get("quantity")).intValue();
            Product snapshot = new Product(Math.max(productId, 1), name, Math.max(price, 0.01),
                    0, "Historical");
            return new BillItem(snapshot, quantity);
        } catch (Exception e) {
            return null;
        }
    }

    public String getProductName() { return productName; }
    public double getUnitPrice() { return unitPrice; }
}

class Bill {
    private static int nextBillId = 1;
    private final int billId;
    private final String customerName;
    private final String createdAt;
    private final List<BillItem> items = new ArrayList<>();
    private boolean completed;

    public Bill(String customerName) {
        this(nextBillId++, customerName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                new ArrayList<>(), false);
    }

    private Bill(int id, String customerName, String createdAt, List<BillItem> items, boolean completed) {
        this.billId = id;
        this.customerName = customerName;
        this.createdAt = createdAt;
        this.items.addAll(items);
        this.completed = completed;
        nextBillId = Math.max(nextBillId, id + 1);
    }

    public int getBillId() { return billId; }
    public String getCustomerName() { return customerName; }
    public String getCreatedAt() { return createdAt; }
    public List<BillItem> getItems() { return items; }
    public double getTotal() { return items.stream().mapToDouble(BillItem::getSubtotal).sum(); }

    public int getQuantityForProduct(int productId) {
        return items.stream().filter(i -> i.getProductId() == productId)
                .mapToInt(BillItem::getQuantity).sum();
    }

    public void addItem(Product product, int quantity) {
        for (BillItem item : items) {
            if (item.getProductId() == product.getId()) {
                int combined = item.getQuantity() + quantity;
                items.remove(item);
                items.add(new BillItem(product, combined));
                return;
            }
        }
        items.add(new BillItem(product, quantity));
    }

    public boolean canBeFulfilled(Inventory inventory) {
        for (BillItem item : items) {
            Product product = inventory.findById(item.getProductId());
            if (product == null || product.getQuantity() < item.getQuantity()) return false;
        }
        return true;
    }

    public void completeCheckout(Inventory inventory) {
        if (completed) throw new IllegalStateException("This bill has already been completed.");
        if (!canBeFulfilled(inventory)) throw new IllegalStateException("Insufficient stock.");
        for (BillItem item : items) inventory.findById(item.getProductId()).reduceStock(item.getQuantity());
        completed = true;
    }

    public void printReceipt() {
        System.out.println("\n======================================");
        System.out.println("              RECEIPT");
        System.out.println("======================================");
        System.out.println("Bill ID: " + billId);
        System.out.println("Customer: " + customerName);
        System.out.println("Date: " + createdAt);
        System.out.println("--------------------------------------");
        System.out.printf("%-22s %5s %10s %10s%n", "Item", "Qty", "Price", "Subtotal");
        for (BillItem item : items) {
            System.out.printf(Locale.US, "%-22s %5d %10.2f %10.2f%n",
                    item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal());
        }
        System.out.println("--------------------------------------");
        System.out.printf(Locale.US, "TOTAL: %.2f%n", getTotal());
        System.out.println("======================================");
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("billId", billId);
        map.put("customerName", customerName);
        map.put("createdAt", createdAt);
        map.put("completed", completed);
        List<Object> itemMaps = new ArrayList<>();
        for (BillItem item : items) itemMaps.add(item.toMap());
        map.put("items", itemMaps);
        map.put("total", getTotal());
        return map;
    }

    public static Bill fromMap(Map<String, Object> map) {
        try {
            int id = ((Number) map.get("billId")).intValue();
            String customer = String.valueOf(map.get("customerName"));
            String created = String.valueOf(map.get("createdAt"));
            boolean completed = Boolean.TRUE.equals(map.get("completed"));
            List<BillItem> items = new ArrayList<>();
            Object rawItems = map.get("items");
            if (rawItems instanceof List<?> list) {
                for (Object object : list) {
                    if (object instanceof Map<?, ?> itemMap) {
                        BillItem item = BillItem.fromMap(GroceryManagementSystem.castMap(itemMap));
                        if (item != null) items.add(item);
                    }
                }
            }
            return new Bill(id, customer, created, items, completed);
        } catch (Exception e) {
            System.out.println("Skipping an invalid sale record.");
            return null;
        }
    }
}

/** Small JSON reader/writer for this project's predictable data format. */
class JsonStore {
    private final Path path;

    public JsonStore(String filename) { this.path = Path.of(filename); }

    @SuppressWarnings("unchecked")
    public Map<String, Object> load() throws IOException {
        if (!Files.exists(path)) return null;
        String json = Files.readString(path, StandardCharsets.UTF_8);
        Object parsed = new Parser(json).parse();
        if (!(parsed instanceof Map<?, ?> map)) throw new IOException("Root JSON value must be an object.");
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() instanceof String key) result.put(key, entry.getValue());
        }
        return result;
    }

    public void save(Map<String, Object> data) throws IOException {
        Files.writeString(path, stringify(data, 0) + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    private static String stringify(Object value, int depth) {
        if (value == null) return "null";
        if (value instanceof String s) return quote(s);
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map<?, ?> map) {
            if (map.isEmpty()) return "{}";
            StringBuilder out = new StringBuilder("{\n");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                out.append("  ".repeat(depth + 1)).append(quote(String.valueOf(entry.getKey())))
                   .append(": ").append(stringify(entry.getValue(), depth + 1));
                if (++i < map.size()) out.append(',');
                out.append('\n');
            }
            return out.append("  ".repeat(depth)).append('}').toString();
        }
        if (value instanceof Iterable<?> iterable) {
            List<String> parts = new ArrayList<>();
            for (Object item : iterable) parts.add("  ".repeat(depth + 1) + stringify(item, depth + 1));
            if (parts.isEmpty()) return "[]";
            return "[\n" + String.join(",\n", parts) + "\n" + "  ".repeat(depth) + "]";
        }
        return quote(String.valueOf(value));
    }

    private static String quote(String s) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }

    private static final class Parser {
        private final String text;
        private int index = 0;
        Parser(String text) { this.text = text; }

        Object parse() throws IOException {
            Object value = value();
            whitespace();
            if (index != text.length()) fail("Unexpected trailing content");
            return value;
        }

        private Object value() throws IOException {
            whitespace();
            if (index >= text.length()) return fail("Unexpected end of input");
            char c = text.charAt(index);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return string();
            if (c == 't') { literal("true"); return true; }
            if (c == 'f') { literal("false"); return false; }
            if (c == 'n') { literal("null"); return null; }
            return number();
        }

        private Map<String, Object> object() throws IOException {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            whitespace();
            if (take('}')) return map;
            while (true) {
                whitespace();
                if (index >= text.length() || text.charAt(index) != '"') return fail("Expected object key");
                String key = string();
                whitespace(); expect(':');
                map.put(key, value());
                whitespace();
                if (take('}')) return map;
                expect(',');
            }
        }

        private List<Object> array() throws IOException {
            expect('[');
            List<Object> list = new ArrayList<>();
            whitespace();
            if (take(']')) return list;
            while (true) {
                list.add(value());
                whitespace();
                if (take(']')) return list;
                expect(',');
            }
        }

        private String string() throws IOException {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (c == '"') return out.toString();
                if (c == '\\') {
                    if (index >= text.length()) return fail("Incomplete escape");
                    char e = text.charAt(index++);
                    switch (e) {
                        case '"', '\\', '/' -> out.append(e);
                        case 'b' -> out.append('\b');
                        case 'f' -> out.append('\f');
                        case 'n' -> out.append('\n');
                        case 'r' -> out.append('\r');
                        case 't' -> out.append('\t');
                        case 'u' -> {
                            if (index + 4 > text.length()) return fail("Invalid Unicode escape");
                            try {
                                out.append((char) Integer.parseInt(text.substring(index, index + 4), 16));
                            } catch (NumberFormatException ex) { return fail("Invalid Unicode escape"); }
                            index += 4;
                        }
                        default -> { return fail("Invalid escape sequence"); }
                    }
                } else {
                    if (c < 0x20) return fail("Control character in string");
                    out.append(c);
                }
            }
            return fail("Unterminated string");
        }

        private Number number() throws IOException {
            int start = index;
            if (take('-')) { /* sign */ }
            while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            if (take('.')) while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            if (take('e') || take('E')) {
                if (!take('+')) take('-');
                while (index < text.length() && Character.isDigit(text.charAt(index))) index++;
            }
            if (start == index) return fail("Expected a value");
            try {
                BigDecimal n = new BigDecimal(text.substring(start, index));
                try { return n.longValueExact(); } catch (ArithmeticException ignored) { return n; }
            } catch (NumberFormatException e) { return fail("Invalid number"); }
        }

        private void literal(String literal) throws IOException {
            if (!text.startsWith(literal, index)) fail("Invalid literal");
            index += literal.length();
        }

        private void whitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
        }

        private void expect(char c) throws IOException {
            whitespace();
            if (!take(c)) fail("Expected '" + c + "'");
        }

        private boolean take(char c) {
            if (index < text.length() && text.charAt(index) == c) { index++; return true; }
            return false;
        }

        private <T> T fail(String message) throws IOException {
            throw new IOException(message + " at position " + index);
        }
    }
}
