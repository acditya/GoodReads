public abstract class User {
    protected int id;
    protected String name;
    protected String role;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole(){
        return role;
    }
}
