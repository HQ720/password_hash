# Password Hash

A Java password vault I built as a home lab project while studying **Applied Cryptography** and learning **Data Structures and Algorithms in Java**.

I wanted to challenge myself to take some of the concepts I was learning and build something practical: a local password vault that securely encrypts stored passwords rather than keeping them in plain text.

## What it does

- Creates a personal master password on first use
- Stores password entries locally
- Lists saved accounts
- Retrieves stored passwords
- Allows the master password to be changed
- Encrypts the vault before writing it to disk
- Uses a unique salt and encryption IV
- Keeps the encrypted vault in a local `passwords.vault` file

## Cryptography

The project uses:

- **PBKDF2-HMAC-SHA256** for deriving an encryption key from the master password
- **210,000 PBKDF2 iterations**
- **16-byte random salt**
- **AES-256-GCM** for authenticated encryption
- **12-byte random IV**
- **128-bit GCM authentication tag**
- `SecureRandom` for cryptographically secure random values

The basic process is:

```text
Master Password
       ↓
PBKDF2-HMAC-SHA256
       ↓
256-bit Key
       ↓
AES-256-GCM
       ↓
Encrypted passwords.vault
```

The master password itself is never stored in the vault.

## Data Structures & Algorithms

I also used this project to apply some of the Data Structures and Algorithms concepts I have been learning in Java.

Password entries are stored using an `ArrayList<PasswordEntry>`.

Current operations include:

| Operation | Complexity |
|---|---|
| Add password | O(1) amortized |
| List passwords | O(n) |
| Search for password | O(n) |

The project deliberately uses a straightforward data structure so I can focus on understanding how the underlying operations work and how the data structure affects performance.

## Running the Project

The project uses Java 17 and Maven.

Clone the repository:

```bash
git clone https://github.com/HQ720/password_hash.git
cd password_hash
```

Build the project:

```bash
mvn clean package
```

Run it:

```bash
java -jar target/password-hash-1.0.0.jar
```

On the first run, you will create **your own master password**.

Your master password is not included in this repository.

## Vault File

The encrypted vault is stored locally as:

```text
passwords.vault
```

The file is excluded from Git using `.gitignore`, so personal vault data should not be uploaded to GitHub.

## Why I Built This

I wanted a project that combined two areas I was actively studying: **cryptography and algorithms**.

Building the vault gave me a way to
