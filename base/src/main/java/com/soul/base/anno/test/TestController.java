package com.soul.base.anno.test;

import com.soul.base.anno.BehaviorReport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gin
 * @date 2021/4/15
 */
@RestController
public class TestController {

    @BehaviorReport
    @GetMapping("/testAsp")
    public String testMethod(String parm){
        return "test: " + parm;
    }

}
