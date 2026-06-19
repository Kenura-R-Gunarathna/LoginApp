package lk.krag.loginapp;

public class Task {
    public String id;
    public String title;
    public boolean completed;

    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Task.class)
    }

    public Task(String id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }
}
