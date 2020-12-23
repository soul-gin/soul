package com.soul.base.io.level07;

public class MyCar implements Car {

    @Override
    public String getCarInfo(String info) {
        System.out.println("server get client args:" + info);
        return "server res: " + info;
    }

}
