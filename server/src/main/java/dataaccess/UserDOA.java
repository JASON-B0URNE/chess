package dataaccess;

import model.UserData;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

public class UserDOA {
    private static Collection<UserData> users = new HashSet<>();

    public static UserData get(String username) {
        AtomicReference<UserData> user = new AtomicReference<>();

        users.forEach( x -> {
            if (x.username().equals(username)) {
                user.set(x);
            }
        });

        return user.get();
    }

    public static void create(UserData user) {
        users.add(user);
    }

}
