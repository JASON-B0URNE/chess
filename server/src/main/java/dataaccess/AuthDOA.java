package dataaccess;

import model.AuthData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class AuthDOA implements InterfaceDOA<AuthData> {
    private Collection<AuthData> authTokens = new HashSet<>();

    @Override
    public void create(AuthData auth) {
        this.authTokens.add(auth);
    }

    @Override
    public AuthData get(String authToken) {
        AtomicReference<AuthData> session = new AtomicReference<>();

        this.authTokens.forEach( x -> {
            if (Objects.equals(x.authToken(), authToken)) {
                System.out.println("Got Here: ");
                session.set(x);
            }
        });

        return session.get();
    }

    @Override
    public void clear() {
        this.authTokens = new HashSet<>();
    }

    @Override
    public Collection<AuthData> list() {
        return this.authTokens;
    }

    @Override
    public void replace(AuthData data) {

    }

    @Override
    public void delete(AuthData auth) {
        this.authTokens.remove(auth);
    }
}
