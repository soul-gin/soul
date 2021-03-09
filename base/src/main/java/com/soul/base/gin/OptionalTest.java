package com.soul.base.gin;

import java.util.Optional;

/**
 * @author gin
 * @date 2021/3/9
 */
public class OptionalTest {

    public static void main(String[] args) {
        Double count = null;
        Optional<Double> optCount = Optional.ofNullable(count);
        System.out.println(optCount.orElse(0.0D).intValue());
    }

}
