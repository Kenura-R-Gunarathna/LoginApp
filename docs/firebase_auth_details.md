# Firebase Authentication Details

## 1. Authentication vs. Database
In our previous version, we manually saved passwords in the Realtime Database. This was insecure. **Firebase Auth** is a dedicated service for managing user identities.

### Why use Firebase Auth?
*   **Security:** Passwords are never stored in plain text. Firebase handles hashing and salting automatically.
*   **Scalability:** It can handle millions of users and supports many providers (Google, Facebook, Phone, etc.).
*   **Session Management:** It keeps the user logged in even if the app is closed.

## 2. Core Methods Used
In this project, we utilize the following standard `FirebaseAuth` methods:

*   **`createUserWithEmailAndPassword(email, password)`:** 
    *   Registers a new user.
    *   Automatically validates email format and password strength (minimum 6 characters).
*   **`signInWithEmailAndPassword(email, password)`:** 
    *   Verifies credentials.
    *   Returns a `FirebaseUser` object if successful.
*   **`signOut()`:** 
    *   Clears the local user session.
*   **`user.delete()`:** 
    *   Permanently removes the user's account from the Firebase Authentication servers.

## 3. Best Practices Implemented
*   **Email Encoding:** Since Firebase Realtime Database keys cannot contain dots (`.`), we replaced them with commas (`,`) when using the email as a path for profile data (e.g., `user@example,com`).
*   **Context Passing:** The email is passed between Activities using `Intent.putExtra()`, allowing the Profile screen to load data specific to the logged-in user.

## 4. Source Code: Authentication Implementation

### Sign Up Implementation (SignUpActivity.java)
```java
// Using Firebase Auth to register a new user
mAuth.createUserWithEmailAndPassword(email, password)
    .addOnSuccessListener(authResult -> {
        // Handle successful registration
        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
    })
    .addOnFailureListener(e -> {
        // Handle failure (e.g. email already in use)
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    });
```

### Sign In Implementation (SignInActivity.java)
```java
// Using Firebase Auth to sign in an existing user
mAuth.signInWithEmailAndPassword(email, password)
    .addOnSuccessListener(authResult -> {
        // Navigate to Profile on success
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    })
    .addOnFailureListener(e -> {
        // Handle failure (e.g. wrong password)
        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
    });
```

### Sign Out & Account Deletion (ProfileActivity.java)
```java
// Sign Out
private void logout() {
    FirebaseAuth.getInstance().signOut();
    finish();
}

// Account Deletion
private void deleteAccount() {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user != null) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Account removed from Auth server
            }
        });
    }
}
```
