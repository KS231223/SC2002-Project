package ims;

public abstract class User {
    private final String id;
    private String name;
    private String password;
    private final Role role;

    protected User(String id, String name, String password, Role role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public boolean verifyPassword(String candidate) {
        return password != null && password.equals(candidate);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
