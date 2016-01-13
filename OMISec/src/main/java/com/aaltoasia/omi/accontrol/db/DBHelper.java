package com.aaltoasia.omi.accontrol.db;

import com.aaltoasia.omi.accontrol.ConfigHelper;
import com.aaltoasia.omi.accontrol.db.objects.OMIGroup;
import com.aaltoasia.omi.accontrol.db.objects.OMIRule;
import com.aaltoasia.omi.accontrol.db.objects.OMIUser;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by romanfilippov on 19/11/15.
 */
public class DBHelper {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private static final DBHelper instance = new DBHelper();
    private DBHelper() {
        logger.setLevel(Level.INFO);
        configureDB();
    }

    private int DEFAULT_GROUP_ID;
    private Connection connection;

    public static DBHelper getInstance() {

        return instance;
    }

    private void createTables() throws SQLException
    {
        Statement stmt = connection.createStatement();
        String sql = "CREATE TABLE USERS " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " USERNAME       VARCHAR(256)    NOT NULL,"+
                " EMAIL       VARCHAR(256)    UNIQUE NOT NULL)";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE RULES " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " HID            TEXT     NOT NULL," +
                " GROUP_ID          INT    NOT NULL," +
                " WRITE_PERMISSIONS   INT     NOT NULL)";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE GROUPS " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " GROUP_NAME          VARCHAR(256)    UNIQUE NOT NULL)";
        stmt.executeUpdate(sql);

        sql = "CREATE TABLE USERS_GROUPS_RELATION " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " USER_ID          INT    NOT NULL," +
                " GROUP_ID          INT    NOT NULL)";
        stmt.executeUpdate(sql);
        stmt.close();

        DEFAULT_GROUP_ID = createGroup("Default");
    }

    public int createGroup(String groupName)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO GROUPS(GROUP_NAME) VALUES(?)");
            stmt.setString(1,groupName);
            stmt.executeUpdate();

            int res = stmt.getGeneratedKeys().getInt(1);
            stmt.close();
            logger.info("Group with name:"+groupName+" successfully created. ID="+res);
            return res;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return -1;
        }
    }

    public boolean updateGroup(int groupID, String groupName)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE GROUPS SET GROUP_NAME = ? WHERE ID = ?;");
            stmt.setString(1,groupName);
            stmt.setInt(2,groupID);
            stmt.executeUpdate();
            stmt.close();
            logger.info("Group with ID:"+groupID+" successfully updated.");
            return true;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
        }
    }

    public boolean deleteGroup(int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM GROUPS WHERE ID=?;");
            stmt.setInt(1,groupID);
            stmt.executeUpdate();

            stmt = connection.prepareStatement("DELETE FROM RULES WHERE GROUP_ID=?;");
            stmt.setInt(1,groupID);
            stmt.executeUpdate();

            stmt = connection.prepareStatement("DELETE FROM USERS_GROUPS_RELATION WHERE GROUP_ID=?;");
            stmt.setInt(1,groupID);
            stmt.executeUpdate();

            logger.info("Group with ID="+groupID+" deleted successfully. Related rules were removed and users removed from the group");
            stmt.close();
            return true;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
        }
    }

    public ArrayList<OMIGroup> getGroups ()
    {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM GROUPS" );
            ArrayList<OMIGroup> resultsArray = new ArrayList<OMIGroup>();
            while ( rs.next() ) {
                OMIGroup nextGroup = new OMIGroup();
                nextGroup.id = rs.getInt("ID");
                nextGroup.name = rs.getString("GROUP_NAME");


                //TODO: requires optimization!
                PreparedStatement prst = connection.prepareStatement("SELECT USER_ID FROM USERS_GROUPS_RELATION WHERE GROUP_ID=?;");
                prst.setInt(1, nextGroup.id);
                ResultSet rs2 = prst.executeQuery();

                ArrayList<Integer> userIDs = new ArrayList<Integer>();
                while ( rs2.next() ) {
                    userIDs.add(rs2.getInt("USER_ID"));
                }

                nextGroup.userIDs = userIDs;
                rs2.close();
                prst.close();

                resultsArray.add(nextGroup);
            }
            rs.close();
            stmt.close();
            logger.info("Groups fetch request finished. Size:"+resultsArray.size());
            return resultsArray;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return null;
        }
    }

    public ArrayList<OMIGroup> getGroups (int[] groupIDs)
    {
        try {
            if (groupIDs.length < 1)
            {
                return new ArrayList<OMIGroup>();
            }

            String query = "SELECT * FROM GROUPS WHERE ID IN(";

            for (int i = 0; i < groupIDs.length-1; i++) {
                query += "?,";
            }

            query += "?);";
            PreparedStatement stmt = connection.prepareStatement(query);

            for (int i = 0; i < groupIDs.length; i++) {
                stmt.setInt(i+1, groupIDs[i]);
            }

            ResultSet rs = stmt.executeQuery(query);
            ArrayList<OMIGroup> resultsArray = new ArrayList<OMIGroup>();
            while ( rs.next() ) {
                OMIGroup nextGroup = new OMIGroup();
                nextGroup.id = rs.getInt("ID");
                nextGroup.name = rs.getString("GROUP_NAME");

                resultsArray.add(nextGroup);
            }
            rs.close();
            stmt.close();
            logger.info("Groups fetch request finished. Size:"+resultsArray.size());
            return resultsArray;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return null;
        }
    }

    public int getGroupID(String groupName) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT ID FROM GROUPS WHERE GROUP_NAME=?");
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                int res = rs.getInt("ID");
                stmt.close();
                return res;
            }
            stmt.close();
            return -1;
        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return -1;
        }
    }

    public boolean addUserToGroup (int userID, int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO USERS_GROUPS_RELATION(USER_ID,GROUP_ID) VALUES(?,?)");
            stmt.setInt(1,userID);
            stmt.setInt(2,groupID);
            stmt.executeUpdate();
            stmt.close();

            logger.info("User with ID="+userID+" successfully added to the group with ID="+groupID);
            return true;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
        }
    }

    public boolean addUsersToGroup (int[] userIDs, int groupID)
    {
        try {
            String query = "INSERT INTO USERS_GROUPS_RELATION(USER_ID,GROUP_ID) VALUES ";

            for (int i = 0; i < userIDs.length-1; i++) {
                query += "(?,?),";
            }

            query += "(?,?);";

            PreparedStatement stmt = connection.prepareStatement(query);
            for (int i = 1; i < userIDs.length+1; i++) {
                stmt.setInt(2*i-1,userIDs[i-1]);
                stmt.setInt(2*i,groupID);
            }
            stmt.executeUpdate();
            stmt.close();

            logger.info("Users list successfully added to the group with ID="+groupID);
            return true;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
        }
    }

    public boolean removeUserFromGroup (int userID, int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM USERS_GROUPS_RELATION WHERE USER_ID=? AND GROUP_ID=?");
            stmt.setInt(1,userID);
            stmt.setInt(2,groupID);
            stmt.executeUpdate();
            stmt.close();

            logger.info("User with ID="+userID+" successfully removed from the group with ID="+groupID);
            return true;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
        }
    }

    public boolean removeUsersFromGroup (int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM USERS_GROUPS_RELATION WHERE GROUP_ID=?;");
            stmt.setInt(1, groupID);
            stmt.executeUpdate();
            stmt.close();

            logger.info("All users successfully removed from the group with ID="+groupID);
            return true;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
        }
    }

    public boolean updateUsersForGroup (int[] userIDs, int groupID)
    {
//        try {

            // TODO: query optimization
//            String query = "DELETE FROM USERS_GROUPS_RELATION WHERE USER_ID NOT IN (";
//            for (int i = 0; i < userIDs.length-1; i++) {
//                query += "?,";
//            }
//
//            query += "?) AND GROUP_ID=?;";
//            PreparedStatement stmt = connection.prepareStatement(query);
//
//            for (int i = 0; i < userIDs.length; i++) {
//                stmt.setInt(i+1, userIDs[i]);
//            }
//
//            stmt.setInt(userIDs.length+1,groupID);
//            stmt.executeUpdate();


            // Delete all users from current group (may be optimized)
            removeUsersFromGroup(groupID);

            // Add users from response
            addUsersToGroup(userIDs, groupID);

            logger.info("User list successfully updated for the group with ID="+groupID);
            return true;

//        } catch (SQLException ex)
//        {
//            logger.severe(ex.getMessage());
//            return false;
//        }
    }

    public boolean updateOrCreateRule(String HID, int groupID, boolean writable)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE RULES SET WRITE_PERMISSIONS = ? WHERE HID = ? AND GROUP_ID = ?;");
            stmt.setBoolean(1,writable);
            stmt.setString(2,HID);
            stmt.setInt(3,groupID);
            int rows = stmt.executeUpdate();
            if (rows == 0)
            {
                logger.info("Record for HID:"+HID+" not found. Creating new.");
                createRule(HID, groupID, writable);
            } else {
                logger.info("Record for HID:"+HID+" was updated.");
            }
            stmt.close();
            return true;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
        }
    }

    public boolean createRule(String HID, int groupID, boolean writable)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO RULES(HID,GROUP_ID,WRITE_PERMISSIONS) VALUES(?,?,?)");
            stmt.setString(1,HID);
            stmt.setInt(2,groupID);
            stmt.setBoolean(3,writable);
            stmt.executeUpdate();
            stmt.close();

            logger.info("Record for HID:"+HID+" successfully created.");
            return true;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
        }
    }

    public ArrayList<OMIRule> getRules(int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM RULES WHERE GROUP_ID=?;");
            stmt.setInt(1, groupID);
            ResultSet rs = stmt.executeQuery();
            ArrayList<OMIRule> resultsArray = new ArrayList<OMIRule>();
            while ( rs.next() ) {
                OMIRule nextRule = new OMIRule();
                nextRule.id = rs.getInt("ID");
                nextRule.hid = rs.getString("HID");
                nextRule.groupID = rs.getInt("GROUP_ID");
                nextRule.writePermissions = rs.getInt("WRITE_PERMISSIONS");

                resultsArray.add(nextRule);
            }
            rs.close();
            stmt.close();

            logger.info("Rules fetch request finished for group:"+groupID+". Size:"+resultsArray.size());
            return resultsArray;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return null;
        }
    }

    public boolean deleteRule(String HID, int groupID)
    {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM RULES WHERE HID=? AND GROUP_ID=?");
            stmt.setString(1,HID);
            stmt.setInt(2,groupID);
            stmt.executeUpdate();
            stmt.close();

            logger.info("Record for HID:"+HID+" successfully created.");
            return true;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
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
                nextUser.email = rs.getString("EMAIL");

                resultsArray.add(nextUser);
            }
            rs.close();
            stmt.close();

            logger.info("Users fetch request finished. Size:"+resultsArray.size());
            return resultsArray;

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return null;
        }
    }

    public boolean createUserIfNotExists(OMIUser user)
    {
        try {

            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM USERS WHERE USERNAME=? AND EMAIL=?");
            stmt.setString(1,user.username);
            stmt.setString(2,user.email);
            ResultSet rs = stmt.executeQuery();

            // No such users, insert one
            if (!rs.isBeforeFirst()) {
                stmt = connection.prepareStatement("INSERT INTO USERS(USERNAME,EMAIL) VALUES(?,?)");
                stmt.setString(1,user.username);
                stmt.setString(2,user.email);
                boolean res = (stmt.executeUpdate() != 0);

                if (!res)
                    return false;


                // Add new user to Default group (everybody belongs it)
                int userID = stmt.getGeneratedKeys().getInt(1);
                stmt = connection.prepareStatement("INSERT INTO USERS_GROUPS_RELATION(USER_ID,GROUP_ID) VALUES(?,?)");
                stmt.setInt(1, userID);
                stmt.setInt(2, DEFAULT_GROUP_ID);

                res = (stmt.executeUpdate() != 0);
                stmt.close();
                logger.info("New user created successfully. ID="+userID);
                return res;
            } else {
                return true;
            }

        } catch (SQLException ex)
        {
            logger.severe(ex.getMessage());
            return false;
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
            logger.severe( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }

        if (!dbExists)
        {
            logger.info("Creating new database");

            try {
                createTables();
            } catch (SQLException ex)
            {
                logger.info("Error while creating tables: "+ex.getMessage());
                return false;
            }

            logger.info("Created tables successfully.");
        } else {
            logger.info("Opened database successfully. Path:"+file.getAbsolutePath());

            DEFAULT_GROUP_ID = getGroupID("Default");
            if (DEFAULT_GROUP_ID == -1)
                DEFAULT_GROUP_ID = createGroup("Default");
        }
        return true;
    }

}
