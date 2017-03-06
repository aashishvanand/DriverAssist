package com.aashish.driverassist;

/**
 * Created by AASHI on 3/6/2017.
 */

public class Driver {
    private String name, rating;

    public Driver() {
    }

    public Driver(String name, String rating) {
        this.name = name;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name1) {
        this.name = name1;
    }

    public String getRating() {
        return rating;
    }
}