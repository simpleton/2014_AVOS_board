package com.sim.board.model;

import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;

import java.io.File;

/**
 * Created by sim on 7/6/14.
 */
public class Club extends AVObject{
    public String color;
    public AVFile logo;
    public String name;

    @Override
    public String toString() {
        return "color: " + color + "name: " + name + "file:" + logo.getName();
    }
}
