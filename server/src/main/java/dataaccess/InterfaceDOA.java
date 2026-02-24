package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashSet;

public interface InterfaceDOA<T> {
    void create(T data);
    public T get(String str);
    public void delete(T data);
    public void clear();
    public Collection<T> list();
    void replace(T data);
}
