package za.ac.belgiumcampus.controller;

import za.ac.belgiumcampus.dao.UserDAO;
import za.ac.belgiumcampus.model.User;
import za.ac.belgiumcampus.util.PasswordUtil;
import za.ac.belgiumcampus.util.SessionManager;
import za.ac.belgiumcampus.util.ValidationUtil;

public class AuthController {

    private final UserDAO userDAO;
    private User loggedInUser;

    public AuthController() {
        this.userDAO = new UserDAO();
    }

    public User register(String fullName, String username, String email,
                          String plainPassword, String role) {

        if (!ValidationUtil.isNotBlank(fullName)) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (!PasswordUtil.isStrongEnough(plainPassword)) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and include an uppercase letter, "
                            + "a lowercase letter, and a digit.");
        }
        if (userDAO.usernameExists(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("That email address is already registered.");
        }

        String normalisedRole = (role == null) ? "STOREKEEPER" : role.trim().toUpperCase();
        String hashedPassword = PasswordUtil.hash(plainPassword);
        User user = new User(fullName.trim(), username.trim(), email.trim(), hashedPassword, normalisedRole);

        boolean saved = userDAO.registerUser(user);
        if (!saved) {
            throw new IllegalStateException("Could not save the new account. Please try again.");
        }
        return user;
    }

    public User login(String username, String plainPassword) {
        if (!ValidationUtil.isNotBlank(username) || !ValidationUtil.isNotBlank(plainPassword)) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        User user = userDAO.getUserByUsername(username.trim());
        if (user == null || !PasswordUtil.verify(plainPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        

        SessionManager.login(user);
        this.loggedInUser = user;
        return user;
    }

    public void logout() {
        SessionManager.logout();
        this.loggedInUser = null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}