package com.aaltoasia;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by romanfilippov on 19/11/15.
 */
public class DBHelper {

    private static final DBHelper instance = new DBHelper();
    private DBHelper() {
        configureDB();
    }

    private Connection connection;

    public static DBHelper getInstance() {

        return instance;
    }

    private void createTables() throws SQLException
    {
        Statement stmt = connection.createStatement();
        String sql = "CREATE TABLE USERS " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " USERNAME       VARCHAR(256)    NOT NULL, " +
                " GROUPS            TEXT     NOT NULL)";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE GROUPS " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " HID            TEXT     NOT NULL," +
                " GROUP_NAME          VARCHAR(256)    NOT NULL," +
                " WRITE_PERMISSIONS   INT     NOT NULL)";
        stmt.executeUpdate(sql);

        stmt.close();
    }

    public void updateOrCreateRule(String HID, String groupName, boolean writable)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE GROUPS SET WRITE_PERMISSIONS = ? WHERE HID = ? AND GROUP_NAME = ?;");
            stmt.setBoolean(1,writable);
            stmt.setString(2,HID);
            stmt.setString(3,groupName);
            int rows = stmt.executeUpdate();
            if (rows == 0)
            {
                System.out.println("Record for HID:"+HID+" not found. Creating new.");
                createRule(HID, groupName, writable);
            } else {
                System.out.println("Record for HID:"+HID+" was updated.");
            }

        } catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    public void createRule(String HID, String groupName, boolean writable)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("insert into GROUPS(HID,GROUP_NAME,WRITE_PERMISSIONS) values(?,?,?)");
            stmt.setString(1,HID);
            stmt.setString(2,groupName);
            stmt.setBoolean(3,writable);
            stmt.executeUpdate();

            System.out.println("Record for HID:"+HID+" successfully created.");

        } catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    public ArrayList<OMIUser> getUsers ()
    {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM USERS;" );
            ArrayList<OMIUser> resultsArray = new ArrayList<OMIUser>();
            while ( rs.next() ) {
                OMIUser nextUser = new OMIUser(OMIUser.OMIUserType.Unknown);
                nextUser.id = rs.getInt("ID");
                nextUser.username = rs.getString("USERNAME");
                nextUser.groups = rs.getString("GROUPS");

                resultsArray.add(nextUser);
            }
            rs.close();
            stmt.close();
            System.out.println("Users fetch request finished. Size:"+resultsArray.size());
            return resultsArray;

        } catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public void createUser(String username, String age)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("insert into USERS(USERNAME,GROUPS) values(?,?)");
            stmt.setString(1,username);
            stmt.setString(2,age);
            stmt.executeUpdate();

        } catch (SQLException ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    public boolean configureDB()
    {
        String dbName = ConfigHelper.dbName + ".db";
        String jdbcDriver = "jdbc:sqlite:"+ dbName;

        File file = new File(dbName);
        boolean dbExists = file.exists();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(jdbcDriver);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }
        System.out.println("Opened database successfully. Path:"+file.getAbsolutePath());

        if (!dbExists)
        {
            System.out.println("Creating new database");

            try {
                createTables();
            } catch (SQLException ex)
            {
                System.out.println("Error while creating tables: "+ex.getMessage());
                return false;
            }

            System.out.println("Created tables successfully.");
        }
        return true;
    }

}
