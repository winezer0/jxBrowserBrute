package com.nova.LoadDict;


import java.util.Objects;

public class UserPassPair {
    private String username;
    private String password;

    public UserPassPair(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // 需要重写 hashCode 和 equals 方法以便于 HashSet 正确比较 UserPassPair 对象
    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPassPair that = (UserPassPair) o;
        return username.equals(that.username) && password.equals(that.password);
    }

    @Override
    public String toString() {
        return String.format("%s:%s",username, password);
    }

    public String toString(String separator) {
        return String.format("%s%s%s",username,separator, password);
    }
}
