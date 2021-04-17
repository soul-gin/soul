package com.soul.base.anno.disruptor;

import java.util.Map;

/**
 * 行为事件
 *
 * @author gin
 * @date 2021/4/16
 */
public class BehaviorEvent {
    private Map<String, Object> value;

    public Map<String, Object> getValue() {
        return value;
    }

    public void setValue(Map<String, Object> value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BehaviorEvent{" +
                "value=" + value +
                '}';
    }

}