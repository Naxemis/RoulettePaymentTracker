package com.roulettepaymenttracker;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RoulettePaymentDatabaseConnection {

    private static final String databaseURL = ConfigLoader.get_property("database_url");
    private static final String upsertSQL = "INSERT INTO payments (username, amount) VALUES ('?', ?) ON CONFLICT (username) DO UPDATE SET amount = payments.amount + EXCLUDED.amount;";

    public void upsertPayment(String paymentUser, int paymentAmount) {
        try (Connection connection = DriverManager.getConnection(databaseURL)) { // connects with database
            connection.setAutoCommit(false); // makes transaction wait for connection.commit() to be called manually

            try (PreparedStatement statement = connection.prepareStatement(upsertSQL)) {

                statement.setString(1, paymentUser); // in the place of first "?" places paymentUser value
                statement.setInt(2, paymentAmount); // in the place of second "?" places paymentAmount value

                int rowsAffected = statement.executeUpdate(); // executes query
                if (rowsAffected > 0) {
                    System.out.println("Successfully upserted payment in database for user: " + paymentUser);
                }
                else {
                    System.out.println("No rows affected by upserting payment in database.");
                }
            }

            connection.commit(); // commits transaction manually
        } catch (SQLException exception) {
            System.out.println("Something went wrong during upserting payment into database: " + exception.getMessage());
        }
    }
}
