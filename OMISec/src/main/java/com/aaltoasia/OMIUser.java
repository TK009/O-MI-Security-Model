package com.aaltoasia;

/**
 * Created by romanfilippov on 18/11/15.
 */
public class OMIUser {

    /** type of a user */
    public enum OMIUserType {
        OAuth,
        Shibboleth,
        Unknown;

    }

    public boolean isUserAuthorized;
    public OMIUserType userType;

    public int id;
    public String username;
    public String groups;

    public OMIUser(OMIUserType userType)
    {
        this.userType = userType;
    }


}
