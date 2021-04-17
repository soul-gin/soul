package com.soul.base.juc.level08;

import java.lang.reflect.Field;

/**
 * @author gin
 * @date 2021/3/17
 */
public enum SingleUser {
    /**
     * 单例对象
     */
    INSTANCE;
    private final int id;
    private final String name;

    SingleUser() {
        this.id = 1;
        this.name = "gin";
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public SingleUser getInstance() {
        return INSTANCE;
    }

    public String getProperties() {
        return "INSTANCE";
    }

    public static void main(String[] args) {
        SingleUser instance1 = SingleUser.INSTANCE.getInstance();
        SingleUser instance2 = instance1.getInstance();
        System.out.println(instance1.getName());
        System.out.println(instance2.getName());
        System.out.println(SingleUser.INSTANCE.getName());
        System.out.println(instance1.getId());

        try {
            Class<SingleUser> clazz = SingleUser.class;
            Field id = clazz.getDeclaredField("id");
            id.setAccessible(true);
            id.setInt(SingleUser.INSTANCE, 2);
            System.out.println(instance1.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //只有 INSTANCE 单例
        System.out.println(instance1 == instance2);
        System.out.println(instance1 == SingleUser.INSTANCE);
        System.out.println(instance2 == SingleUser.INSTANCE);
        System.out.println(SingleUser.INSTANCE.getProperties());
    }

}