package Connessione;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Connessione {

    protected String driver;
    protected String urlDb;
    protected String user;
    protected String password;

    public Connection connetti() throws IOException {
        driver = "com.mysql.jdbc.Driver";
        user = "twitter-event";
        password = "!tw1tt3r-3v3nt!";
        urlDb = "jdbc:mysql://localhost/twitter-event";

        Connection con;
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(urlDb, user, password);
            return con;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}