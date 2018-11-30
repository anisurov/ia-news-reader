package com.x54.interactivenewsreader;

/**
 * Created by Anisur Rahman on 11/20/18.
 */
class NewsSiteDetails {

    public String name,home,intl,editorial,sports,entertainment;
    public  NewsSiteDetails(){}
    public NewsSiteDetails(String name,String home){
        this.name = name;
        this.home = home;
    }

    public NewsSiteDetails(String name,String home,String intl,String editorial,String sports,String entertainment){
        this.name = name;
        this.home = home;
        this.editorial = editorial;
        this.entertainment =entertainment;
        this.intl = intl;
        this.sports = sports;
    }
}
