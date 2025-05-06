package com.yourorg.finance.service;

import com.yourorg.finance.dao.UserDao;
import com.yourorg.finance.model.User;
import com.yourorg.finance.util.HashUtil;

public class AuthService {
    private static AuthService instance;
    private final UserDao userDao = new UserDao();

    /** Who’s logged in right now */
    private User currentUser;

    public AuthService() { }

    /** Singleton accessor */
    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    /**
     * Registers a new user.
     * @return the newly created User (with ID populated).
     */
    public User register(String username, String plainPassword, String role) throws Exception {
        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already taken");
        }
        String hash = HashUtil.hash(plainPassword);
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(hash);
        u.setRole(role);
        userDao.create(u);
        return u;
    }

    /**
     * Attempts login.  If successful, remembers the user in `currentUser`.
     * @return the logged‑in User, or `null` on failure.
     */
    public User login(String username, String plainPassword) throws Exception {
        User u = userDao.findByUsername(username);
        if (u != null && HashUtil.check(plainPassword, u.getPasswordHash())) {
            this.currentUser = u;
            return u;
        }
        return null;
    }

    /** @return the user who most recently logged in, or null */
    public User getCurrentUser() {
        return currentUser;
    }

    /** Optional: log out the current user */
    public void logout() {
        currentUser = null;
    }
}
