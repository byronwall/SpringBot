package BotBrains;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by byronandanne on 12/10/2014.
 */
public class DatabaseMaster {

    //use the singleton pattern here -> one db per AI

    private static DatabaseMaster _instance = null;
    Connection conn = null;
    /*TODO work remaining:
    simple method to create new tables for data
    fast ability to store images in a table also
    need to store data into those created tables
    some reporting features might be nice
    get rid of the sample code
    replace all references to SpringBot.write to use this class instead
     */
    //use this to store tables
    private List<String> tables = new ArrayList<>();
    private String path;

    private DatabaseMaster() {
    }

    public static DatabaseMaster get() {
        if (_instance == null) {
            _instance = new DatabaseMaster();
        }

        return _instance;
    }

    public synchronized void addDataToTable(String table, String data, int frame) {
        //need to log the timestamp and value

        if (!tables.contains(table)) {
            //create the table first
            tables.add(table);
            createTheTableFirst(table);

        }

        //should have a table... add the value in
        try {

            PreparedStatement insert = conn.prepareStatement("INSERT INTO " + table + "('frame', 'value') VALUES (?,?)");
            insert.setInt(1, frame);
            insert.setString(2, data);
            insert.addBatch();
            insert.executeBatch();

        } catch (SQLException e) {
            SpringBot.write("SQL ERROR, add data to table: " + e.getMessage());
        }
    }

    public synchronized void commitData() {
        try {
            conn.commit();
        } catch (SQLException e) {
            SpringBot.write("SQL ERROR, commit to table: " + e.getMessage());
        }
    }

    private synchronized void createTheTableFirst(String table) {

        try {

            Statement create = conn.createStatement();
            create.executeUpdate("CREATE TABLE " + table + "(frame integer, value string)");

        } catch (SQLException e) {
            SpringBot.write("SQL ERROR, create table: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        //this is required in order to load the drivers... apparently
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            SpringBot.write("CLASS ERROR: " + e.getMessage());
        }


        return DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public void setup(String id) throws SQLException {


        // set up the path for now;
        path = "C:/Users/byronandanne/Documents/My Games/Spring/AI/Skirmish/SpringBot/0.1/db/" + id + ".db";

        conn = getConnection();
        conn.setAutoCommit(false);


    }

}
