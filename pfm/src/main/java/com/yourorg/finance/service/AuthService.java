package com.yourorg.finance.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.yourorg.finance.dao.UserDao;
import com.yourorg.finance.model.User;

public class AuthService {
    private final UserDao userDao = new UserDao();

    /** Register a new user */
    public User register(String username, String rawPassword, String role) throws Exception {
        // 1) hash the password
        String hash = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
        User user = new User(username, hash, role);
        // 2) save to DB
        userDao.create(user);
        return user;
    }

    /** Authenticate user credentials */
    public User login(String username, String rawPassword) throws Exception {
        User user = userDao.findByUsername(username);
        if (user == null) return null;
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), user.getPasswordHash());
        return result.verified ? user : null;
    }
}
