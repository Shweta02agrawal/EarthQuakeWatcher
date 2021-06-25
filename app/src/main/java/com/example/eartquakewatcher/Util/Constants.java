package com.example.eartquakewatcher.Util;

import java.util.Random;

public class Constants {
    public static final String URL="https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_week.geojson";
    public static final int LIMIT=30;

    //creating a random function for generating different marker colours

    public  static int randomInt(int  min,int max){
            return new Random().nextInt(max-min) + min;
    }
}
