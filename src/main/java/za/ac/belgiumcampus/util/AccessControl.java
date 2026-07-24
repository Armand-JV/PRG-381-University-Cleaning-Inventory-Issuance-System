package za.ac.belgiumcampus.util;

public final class AccessControl {

    private AccessControl() {}

    public static boolean canDelete() {
        return SessionManager.isSupervisor();
    }

    public static boolean canManageUsers() {
        return SessionManager.isSupervisor();
    }
}