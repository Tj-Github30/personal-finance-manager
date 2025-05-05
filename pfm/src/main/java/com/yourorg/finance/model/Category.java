// src/main/java/com/yourorg/finance/model/Category.java
package com.yourorg.finance.model;

public class Category {
    private final int id;
    private final int userId;
    private final String name;

    public Category(int id, int userId, String name) {
        this.id     = id;
        this.userId = userId;
        this.name   = name;
    }

    public int getId()       { return id; }
    public int getUserId()   { return userId; }
    public String getName()  { return name; }
    @Override public String toString() { return name; }  // lets ComboBox show name
}
