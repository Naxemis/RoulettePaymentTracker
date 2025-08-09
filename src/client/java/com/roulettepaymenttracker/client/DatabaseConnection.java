package com.roulettepaymenttracker.client;

import com.roulettepaymenttracker.ConfigLoader;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.roulettepaymenttracker.client.ActionBarNotification;

public class DatabaseConnection {
    private static final ActionBarNotification actionBarNotification = new ActionBarNotification();

    private static final String databaseURL = ConfigLoader.get_property("database_url");
    private static final String upsertSQL = "INSERT INTO payments (username, amount, session) VALUES (?, ?, ?) ON CONFLICT (username, session) DO UPDATE SET amount = payments.amount + EXCLUDED.amount;";

    private final ExecutorService databaseExecutor = Executors.newFixedThreadPool(2); // thread pool for database operations

    public CompletableFuture<Void> upsertPayment(String paymentUser, int paymentAmount, int rouletteSession) {

        return CompletableFuture.runAsync(() -> { // runs the operation on background thread
                try (Connection connection = DriverManager.getConnection(databaseURL)) { // connects with database
                    PreparedStatement upsertStatement = connection.prepareStatement(upsertSQL); // prepares SQL query for changes

                    upsertStatement.setString(1, paymentUser); // in the place of first "?" places paymentUser value
                    upsertStatement.setInt(2, paymentAmount); // in the place of second "?" places paymentAmount value
                    upsertStatement.setInt(3, rouletteSession); // in the place of third "?" place rouletteSession value

                    int rowsAffected = upsertStatement.executeUpdate(); // executes query

                    if (rowsAffected > 0) {
                        System.out.println("Successfully upserted payment in database for user: " + paymentUser);
                        actionBarNotification.sendMessage("Upserted payment in database.", "§a");
                    } else {
                        String message = "No rows affected by upserting payment in database.";
                        System.out.println(message);
                        actionBarNotification.sendMessage(message, "§6");
                    }
                }
                catch (SQLException exception) {
                    System.out.println("Something went wrong during upserting payment into database: " + exception.getMessage());
                    actionBarNotification.sendMessage("Something went wrong with database connection.", "§c");
                }
        }, databaseExecutor); // makes the method use dedicated thread pool for execution
    }

    public void async_process_shutdown() {
        databaseExecutor.shutdown(); // closes the database connection thread
        System.out.println("Closing database connection thread.");
    }

}
