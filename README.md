# Password Hash

A small Java password vault that I built as a home lab project.

I was studying Applied Cryptography and also learning Data Structures and Algorithms in Java, so I wanted to challenge myself to actually build something using some of the concepts I was learning.

The idea was to make a local password vault where passwords aren't just sitting in a file as plain text.

## What it does

- Creates a master password when you first run it
- Lets you add and store passwords
- Lets you list saved accounts
- Lets you retrieve a saved password
- Lets you change your master password
- Encrypts the vault before saving it
- Stores everything locally in `passwords.vault`

## Cryptography

The main cryptography used in the project is:

- PBKDF2-HMAC-SHA256
- 210,000 PBKDF2 iterations
- 16-byte random salt
- AES-256-GCM
- 12-byte random IV
- 128-bit GCM authentication tag
- SecureRandom

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

The master password itself is not stored.

PBKDF2 is used to derive the encryption key from the master password, and AES-GCM is then used to encrypt the actual vault data.

## Data structures and algorithms

I also wanted to use this project to practise some of the data structures and algorithms I have been learning in Java.

The passwords are stored using an `ArrayList<PasswordEntry>`.

At the moment:

- Adding an entry is O(1) amortized
- Listing entries is O(n)
- Searching for an entry is O(n)

I kept the data structure fairly simple so I could focus on understanding how it works rather than trying to build something overly complicated.

## Running it

You need Java 17 and Maven.

Clone the repository:

```bash
git clone https://github.com/HQ720/password_hash.git
cd password_hash
```

Build it:

```bash
mvn clean package
```

Run it:

```bash
java -jar target/password-hash-1.0.0.jar
```

On the first run, you will be asked to create your own master password.

The master password is not included in the project.

## Vault file

The encrypted vault is stored locally in:

```text
passwords.vault
```

This file is included in `.gitignore`, so it won't be pushed to GitHub.

## Why I built it

I wanted to take what I was learning in Applied Cryptography and Data Structures and Algorithms and turn it into something practical.

It was a good way for me to learn more about key derivation, encryption, salts, IVs, file handling, `ArrayList`, searching and algorithmic complexity while building something I could actually use.
