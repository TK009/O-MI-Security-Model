package com.aaltoasia;

/**
 * Created by romanfilippov on 15/12/15.
 */
public class LoginService {

    private static final LoginService instance = new LoginService();
    private LoginService() {

    }

    public static LoginService getInstance() {
        return instance;
    }

//    public void logOrRegisterUser()
}
