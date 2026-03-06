package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public interface InterfaceDOA<T> {
    void create(T data) throws SQLException;
    public T get(String str) throws SQLException;
    public void delete(T data) throws SQLException;
    public void clear() throws SQLException;
    public Collection<T> list() throws SQLException;
    void replace(T data) throws SQLException;
}
