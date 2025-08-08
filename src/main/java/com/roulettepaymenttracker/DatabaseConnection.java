package com.roulettepaymenttracker;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseConnection {

    private static final String databaseURL = ConfigLoader.get_property("database_url");
    private static final String upsertSQL = "INSERT INTO payments (username, amount) VALUES (?, ?) ON CONFLICT (username) DO UPDATE SET amount = payments.amount + EXCLUDED.amount;";

    private final ExecutorService databaseExecutor = Executors.newFixedThreadPool(2); // thread pool for database operations

    public CompletableFuture<Void> upsertPayment(String paymentUser, int paymentAmount) {

        return CompletableFuture.runAsync(() -> { // runs the operation on background thread
                try (Connection connection = DriverManager.getConnection(databaseURL)) { // connects with database
                    PreparedStatement upsertStatement = connection.prepareStatement(upsertSQL); // prepares SQL query for changes

                    upsertStatement.setString(1, paymentUser); // in the place of first "?" places paymentUser value
                    upsertStatement.setInt(2, paymentAmount); // in the place of second "?" places paymentAmount value

                    int rowsAffected = upsertStatement.executeUpdate(); // executes query

                    if (rowsAffected > 0) {
                        System.out.println("Successfully upserted payment in database for user: " + paymentUser);
                    } else {
                        System.out.println("No rows affected by upserting payment in database.");
                    }
                }
                catch (SQLException exception) {
                    System.out.println("Something went wrong during upserting payment into database: " + exception.getMessage());
                }
        }, databaseExecutor); // makes the method use dedicated thread pool for execution
    }

    public void async_process_shutdown() {
        databaseExecutor.shutdown(); // closes the database connection thread
        System.out.println("Closing database connection thread.");
    }

}
