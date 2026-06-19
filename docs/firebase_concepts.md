# Firebase NoSQL Concepts

## 1. NoSQL Database (Realtime Database)
Firebase Realtime Database is a cloud-hosted **NoSQL** database. Unlike traditional SQL databases that use tables, rows, and columns, NoSQL databases store data in a flexible, JSON-like format.

### Key Characteristics:
*   **JSON Tree Structure:** Everything is stored as one large JSON object. There are no tables or schemas.
*   **Real-time Sync:** When data changes in the cloud, all connected clients (apps) receive the update instantly.
*   **Offline Support:** The SDK uses a local cache to serve and store data while the device is offline, syncing it once connectivity is restored.

## 2. CRUD Operations in this Project
In this app, we perform CRUD (Create, Read, Update, Delete) on tasks:

*   **Create:** Using `.push()` to generate a unique key for a new task and `.setValue()` to save the data.
*   **Read:** Using `addValueEventListener()` to listen for changes. This provides a `DataSnapshot` containing the current data at the specified path.
*   **Update:** Using `.child("completed").setValue(!task.completed)` to update a specific field without overwriting the entire object.
*   **Delete:** Using `.removeValue()` to delete a node from the JSON tree.

## 3. Data Modeling
Even though NoSQL is schemaless, we use a Java class (`Task.java`) to map the JSON data into objects. This makes the code cleaner and easier to maintain.

### Source Code: Task.java (Data Model)
```java
public class Task {
    public String id;
    public String title;
    public boolean completed;

    public Task() {} // Required for Firebase

    public Task(String id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }
}
```

### Source Code: CRUD Implementation (ProfileActivity.java)
```java
// CREATE: Adding a new task
private void addTask() {
    String title = binding.inputTask.getText().toString().trim();
    DatabaseReference ref = FirebaseDatabase.getInstance()
            .getReference("users").child(encodedEmail).child("tasks").push();
    Task task = new Task(ref.getKey(), title, false);
    ref.setValue(task);
}

// READ: Listening for data changes
private void loadTasks() {
    FirebaseDatabase.getInstance().getReference("users")
            .child(encodedEmail).child("tasks")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        Task task = taskSnapshot.getValue(Task.class);
                        // update UI
                    }
                }
            });
}

// UPDATE: Toggling completion status
private void toggleTask(Task task) {
    FirebaseDatabase.getInstance().getReference("users")
            .child(encodedEmail).child("tasks").child(task.id)
            .child("completed").setValue(!task.completed);
}

// DELETE: Removing a task
private void deleteTask(Task task) {
    FirebaseDatabase.getInstance().getReference("users")
            .child(encodedEmail).child("tasks").child(task.id).removeValue();
}
```
