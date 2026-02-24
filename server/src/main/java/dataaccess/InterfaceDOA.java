package dataaccess;

import model.AuthData;
import model.UserData;

public interface InterfaceDOA<T> {
    void create(T data);
    public T get(String str);
    public void delete(T data);
    public void clear();
}
