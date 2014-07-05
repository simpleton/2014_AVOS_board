package com.sim.board.model;

import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVObject;

/**
 * Created by sim on 7/6/14.
 */
public class Venue extends AVObject{
    public String address;
    public Club club;
    public AVGeoPoint location;
    public String name;

    @Override
    public String toString() {
        return "address: " + address + "name:" + name + "location:" + location.getLatitude() + ":"
                + location.getLongitude() + "club:" + club.toString();
    }
}
