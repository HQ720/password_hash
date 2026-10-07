package com.passwordhash;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class Main {

    private static final Path FILE =
            Path.of("passwords.vault");

    private static final int ITERATIONS =
            210_000;

    private static final SecureRandom random =
            new SecureRandom();

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Password Vault ===");

        String masterPassword;

        /*
         * First run:
         * create a new master password.
         */
        if (!Files.exists(FILE)) {

            System.out.println("No vault found.");
            System.out.println("Create your master password.");

            while (true) {

                System.out.print(
                        "Create master password: "
                );

                String password =
                        scanner.nextLine();

                System.out.print(
                        "Confirm master password: "
                );

                String confirm =
                        scanner.nextLine();

                if (password.equals(confirm)
                        && !password.isEmpty()) {

                    masterPassword = password;
                    break;

                } else {

                    System.out.println(
                            "Passwords do not match or are empty."
                    );
                }
            }

        } else {

            System.out.print(
                    "Master password: "
            );

            masterPassword =
                    scanner.nextLine();
        }

        byte[] salt;

        ArrayList<PasswordEntry> passwords =
                new ArrayList<>();

        /*
         * Load the existing vault.
         */
        if (Files.exists(FILE)) {

            String fileContent =
                    Files.readString(FILE).trim();

            try {

                String[] parts =
                        fileContent.split(":", 2);

                if (parts.length != 2) {
                    throw new Exception();
                }

                salt =
                        Base64.getDecoder()
                                .decode(parts[0]);

                String decrypted =
                        decrypt(
                                parts[1],
                                deriveKey(
                                        masterPassword,
                                        salt
                                )
                        );

                String[] lines =
                        decrypted.split("\n");

                for (String line : lines) {

                    if (!line.isBlank()) {

                        String[] entry =
                                line.split("=", 2);

                        if (entry.length == 2) {

                            String name =
                                    entry[0];

                            String password =
                                    new String(
                                            Base64.getDecoder()
                                                    .decode(
                                                            entry[1]
                                                    ),
                                            StandardCharsets.UTF_8
                                    );

                            passwords.add(
                                    new PasswordEntry(
                                            name,
                                            password
                                    )
                            );
                        }
                    }
                }

            } catch (Exception e) {

                System.out.println(
                        "Wrong master password."
                );

                return;
            }

        } else {

            /*
             * Generate a random salt for
             * the new vault.
             */
            salt = new byte[16];

            random.nextBytes(salt);

            saveVault(
                    passwords,
                    masterPassword,
                    salt
            );
        }

        /*
         * Main menu.
         */
        while (true) {

            System.out.println();
            System.out.println(
                    "1. Add password"
            );

            System.out.println(
                    "2. List passwords"
            );

            System.out.println(
                    "3. Get password"
            );

            System.out.println(
                    "4. Exit"
            );

            System.out.println(
                    "5. Change master password"
            );

            System.out.print("Choose: ");

            String choice =
                    scanner.nextLine();

            /*
             * Add password.
             */
            if (choice.equals("1")) {

                System.out.print("Name: ");

                String name =
                        scanner.nextLine();

                System.out.print("Password: ");

                String password =
                        scanner.nextLine();

                passwords.add(
                        new PasswordEntry(
                                name,
                                password
                        )
                );

                saveVault(
                        passwords,
                        masterPassword,
                        salt
                );

                System.out.println(
                        "Password saved."
                );

            /*
             * List passwords.
             */
            } else if (choice.equals("2")) {

                if (passwords.isEmpty()) {

                    System.out.println(
                            "No passwords saved."
                    );

                } else {

                    System.out.println(
                            "Saved passwords:"
                    );

                    for (PasswordEntry entry :
                            passwords) {

                        System.out.println(
                                "- "
                                        + entry.getName()
                        );
                    }
                }

            /*
             * Get password.
             */
            } else if (choice.equals("3")) {

                System.out.print("Name: ");

                String name =
                        scanner.nextLine();

                boolean found = false;

                for (PasswordEntry entry :
                        passwords) {

                    if (entry.getName()
                            .equals(name)) {

                        System.out.println(
                                "Password: "
                                        + entry.getPassword()
                        );

                        found = true;

                        break;
                    }
                }

                if (!found) {

                    System.out.println(
                            "Password not found."
                    );
                }

            /*
             * Exit.
             */
            } else if (choice.equals("4")) {

                saveVault(
                        passwords,
                        masterPassword,
                        salt
                );

                System.out.println(
                        "Vault locked."
                );

                break;

            /*
             * Change master password.
             */
            } else if (choice.equals("5")) {

                System.out.print(
                        "Current master password: "
                );

                String currentPassword =
                        scanner.nextLine();

                try {

                    /*
                     * Read the current vault.
                     */
                    String fileContent =
                            Files.readString(FILE).trim();

                    String[] parts =
                            fileContent.split(":", 2);

                    if (parts.length != 2) {
                        throw new Exception();
                    }

                    byte[] currentSalt =
                            Base64.getDecoder()
                                    .decode(parts[0]);

                    /*
                     * Try to decrypt the vault.
                     *
                     * If the password is wrong,
                     * AES-GCM authentication fails.
                     */
                    decrypt(
                            parts[1],
                            deriveKey(
                                    currentPassword,
                                    currentSalt
                            )
                    );

                    System.out.println(
                            "Current master password is correct."
                    );

                    System.out.print(
                            "New master password: "
                    );

                    String newPassword =
                            scanner.nextLine();

                    System.out.print(
                            "Confirm new master password: "
                    );

                    String confirm =
                            scanner.nextLine();

                    if (newPassword.equals(confirm)
                            && !newPassword.isEmpty()) {

                        /*
                         * Create a completely new salt.
                         */
                        byte[] newSalt =
                                new byte[16];

                        random.nextBytes(newSalt);

                        /*
                         * Re-encrypt all passwords
                         * with the new master password.
                         */
                        saveVault(
                                passwords,
                                newPassword,
                                newSalt
                        );

                        /*
                         * Update the password and salt
                         * currently being used by the program.
                         */
                        masterPassword =
                                newPassword;

                        salt =
                                newSalt;

                        System.out.println(
                                "Master password changed."
                        );

                    } else {

                        System.out.println(
                                "New passwords do not match."
                        );
                    }

                } catch (Exception e) {

                    System.out.println(
                            "Current master password is incorrect."
                    );
                }

            } else {

                System.out.println(
                        "Invalid choice."
                );
            }
        }
    }

    /*
     * Converts all password entries into text,
     * encrypts the text and saves it.
     */
    private static void saveVault(
            ArrayList<PasswordEntry> passwords,
            String masterPassword,
            byte[] salt
    ) throws Exception {

        String data = "";

        for (PasswordEntry entry :
                passwords) {

            String encodedPassword =
                    Base64.getEncoder()
                            .encodeToString(
                                    entry.getPassword()
                                            .getBytes(
                                                    StandardCharsets.UTF_8
                                            )
                            );

            data += entry.getName()
                    + "="
                    + encodedPassword
                    + "\n";
        }

        byte[] key =
                deriveKey(
                        masterPassword,
                        salt
                );

        String encrypted =
                encrypt(
                        data,
                        key
                );

        String fileContent =
                Base64.getEncoder()
                        .encodeToString(salt)
                        + ":"
                        + encrypted
                        + System.lineSeparator();

        Files.writeString(
                FILE,
                fileContent
        );
    }

    /*
     * Creates a 256-bit key from
     * the master password.
     */
    private static byte[] deriveKey(
            String password,
            byte[] salt
    ) throws Exception {

        PBEKeySpec spec =
                new PBEKeySpec(
                        password.toCharArray(),
                        salt,
                        ITERATIONS,
                        256
                );

        return SecretKeyFactory
                .getInstance(
                        "PBKDF2WithHmacSHA256"
                )
                .generateSecret(spec)
                .getEncoded();
    }

    /*
     * Encrypts the vault using AES-GCM.
     */
    private static String encrypt(
            String text,
            byte[] key
    ) throws Exception {

        byte[] iv =
                new byte[12];

        random.nextBytes(iv);

        Cipher cipher =
                Cipher.getInstance(
                        "AES/GCM/NoPadding"
                );

        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(
                        key,
                        "AES"
                ),
                new GCMParameterSpec(
                        128,
                        iv
                )
        );

        byte[] encrypted =
                cipher.doFinal(
                        text.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return Base64.getEncoder()
                .encodeToString(iv)
                + "."
                + Base64.getEncoder()
                .encodeToString(encrypted);
    }

    /*
     * Decrypts the vault using AES-GCM.
     */
    private static String decrypt(
            String text,
            byte[] key
    ) throws Exception {

        String[] parts =
                text.split(
                        "\\.",
                        2
                );

        if (parts.length != 2) {
            throw new Exception();
        }

        byte[] iv =
                Base64.getDecoder()
                        .decode(parts[0]);

        byte[] encrypted =
                Base64.getDecoder()
                        .decode(parts[1]);

        Cipher cipher =
                Cipher.getInstance(
                        "AES/GCM/NoPadding"
                );

        cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(
                        key,
                        "AES"
                ),
                new GCMParameterSpec(
                        128,
                        iv
                )
        );

        return new String(
                cipher.doFinal(encrypted),
                StandardCharsets.UTF_8
        );
    }

    /*
     * Represents one saved password.
     */
    static class PasswordEntry {

        private String name;

        private String password;

        public PasswordEntry(
                String name,
                String password
        ) {

            this.name = name;
            this.password = password;
        }

        public String getName() {

            return name;
        }

        public String getPassword() {

            return password;
        }
    }
}
