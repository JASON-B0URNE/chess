package dataaccess;

import model.UserData;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

public class UserDOA implements InterfaceDOA<UserData> {
    private Collection<UserData> users = new HashSet<>();

    @Override
    public void create(UserData user) {
        this.users.add(user);
    }

    @Override
    public UserData get(String username) {
        AtomicReference<UserData> user = new AtomicReference<>();

        this.users.forEach( x -> {
            if (x.username().equals(username)) {
                user.set(x);
            }
        });

        return user.get();
    }

    @Override
    public void delete(UserData data) {

    }

    @Override
    public void clear() {
        this.users = new HashSet<>();
    }

    @Override
    public Collection<UserData> list() {
        return null;
    }

    @Override
    public void replace(UserData data) {

    }

}
