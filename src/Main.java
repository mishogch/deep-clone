// Main.java
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Create an instance of Man
        List<String> books = new ArrayList<>();
        books.add("Book1");
        books.add("Book2");

        Man man = new Man("John", 30, books);

        // Perform deep copy
        Man copiedMan = (Man) CopyUtils.deepCopy(man);

        // Display original and copied objects
        System.out.println("Original man: " + man);
        System.out.println("Copied man: " + copiedMan);

        // Modify original object
        System.out.println("\nChanging original man's name to Bob");
        man.setName("Bob");

        System.out.println("Original man's name: " + man.getName());
        System.out.println("Copied man's name: " + copiedMan.getName());

        // Modify original object's favorite books
        System.out.println("\nAdding 'Book3' to original man's favorite books");
        man.getFavoriteBooks().add("Book3");

        System.out.println("Original man's favorite books: " + man.getFavoriteBooks());
        System.out.println("Copied man's favorite books: " + copiedMan.getFavoriteBooks());
    }
}
