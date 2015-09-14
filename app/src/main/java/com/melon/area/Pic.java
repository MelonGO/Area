package com.melon.area;

import java.io.Serializable;
import java.util.HashMap;

public class Pic implements Serializable{
    private static final long serialVersionUID = 1L;

    HashMap<Integer, Integer> picStatus;

    public Pic(){
        this.picStatus = new HashMap<Integer, Integer>();
    }

    public HashMap<Integer, Integer> getPicStatus(){
        return picStatus;
    }
    public void setPicStatus(HashMap<Integer, Integer> pic){
        picStatus = pic;
    }

}
