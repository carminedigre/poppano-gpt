package records;

public class RecordDemo {

    // This is literally all you need for an immutable POJO,
    // which will have accessors, equals, hashCode, toString.
    record Customer(String firstName, String lastName, int customerId) {}

    public static void main(String[] args) {
        var customer = new Customer("Carmine", "Di Gregorio", 123456);

        IO.println(String.format("Hello, %s!",customer.firstName));
        IO.println(String.format("Full record: " + customer));

        IO.println(String.format("Equals + hashCode: %s %d",
                customer.equals(new Customer("Robin", "Williams", 678901)),
                customer.hashCode()));
    }
}
