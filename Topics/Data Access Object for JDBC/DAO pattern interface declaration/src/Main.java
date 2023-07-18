interface UserDao {
    void add(User user);
    User get(int id);
    void update(User user);
    void delete(int id);
}

/* Do not change code below */
class UserDaoImpl implements UserDao {
    private final List<User> users;

    public UserDaoImpl() {
        users = new ArrayList<>();
    }

    @Override
    public void add(User user) {
        users.add(user);
        System.out.println(user + ", added");
    }

    @Override
    public User get(int id) {
        User found = findById(id);
        if (found == null) {
            System.out.println("User : id " + id + ", not found");
            return null;
        }
        System.out.println(found + ", found");
        return new User(found.getId(), found.getName());

    }

    @Override
    public void update(User user) {
        User found = findById(user.getId());
        if (found != null) {
            found.setName(user.getName());
            System.out.println(found + ", updated");
        } else {
            System.out.println("User " + user.getId() + ", not found");
        }

    }

    @Override
    public void delete(int id) {
        User found = findById(id);
        if (found != null) {
            users.remove(found);
            System.out.println(found + ", deleted");
        } else {
            System.out.println("User " + id + ", not found");
        }

    }

    private User findById(int id) {
        for (User user : users) {
            if (id == user.getId()) {
                return user;
            }
        }
        return null;
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); 
        UserDao userDao = new UserDaoImpl();

        int id = scanner.nextInt(); 
        String name = scanner.next();
        userDao.add(new User(id, name));
        User firstUser = userDao.get(id);

        id = scanner.nextInt(); 
        name = scanner.next();
        userDao.add(new User(id, name));
        User secondUser = userDao.get(id);

        User noUser = userDao.get(10);

        id = scanner.nextInt(); 
        String newName = scanner.next();
        User updateUser = userDao.get(id);
        updateUser.setName(newName);
        userDao.update(updateUser);

        System.out.println(userDao.get(id));
        System.out.println(secondUser);

        userDao.delete(firstUser.id);
    }
}