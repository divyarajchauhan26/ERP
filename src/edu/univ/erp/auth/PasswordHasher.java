package edu.univ.erp.auth;

// We need to import the jbcrypt library you added
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    /**
     * Hashes a plaintext password using bcrypt.
     *
     * @param plainTextPassword The password to hash.
     * @return A string containing the salt and the hash.
     */
    public static String hashPassword(String plainTextPassword) {
        // BCrypt.gensalt() creates a random salt
        // BCrypt.hashpw combines the password and salt and hashes them
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    /**
     * Checks if a plaintext password matches a stored bcrypt hash.
     *
     * @param plainTextPassword The password the user typed in.
     * @param storedHash        The hash we got from the database.
     * @return true if the password matches, false otherwise.
     */
    public static boolean checkPassword(String plainTextPassword, String storedHash) {
        return BCrypt.checkpw(plainTextPassword, storedHash);
    }

    // A simple main method to generate the hashes for our seed script
    public static void main(String[] args) {
        // Passwords for our sample users
        String adminPass = "adminpass";
        String instPass = "instpass";
        String stuPass1 = "stupass1";
        String stuPass2 = "stupass2";

        System.out.println("--- Hashed Passwords for Seed Script ---");
        System.out.println("admin1 (adminpass): " + hashPassword(adminPass));
        System.out.println("inst1 (instpass):   " + hashPassword(instPass));
        System.out.println("stu1 (stupass1):    " + hashPassword(stuPass1));
        System.out.println("stu2 (stupass2):    " + hashPassword(stuPass2));
    }
}