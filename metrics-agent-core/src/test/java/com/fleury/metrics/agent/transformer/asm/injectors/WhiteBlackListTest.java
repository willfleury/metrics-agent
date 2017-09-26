package com.fleury.metrics.agent.transformer.asm.injectors;

import static com.fleury.metrics.agent.config.Configuration.dotToSlash;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.fleury.metrics.agent.annotation.Counted;
import com.fleury.metrics.agent.config.Configuration;
import java.util.List;
import org.junit.Test;

public class WhiteBlackListTest extends BaseMetricTest {

    @Test
    public void allowWhiteListed() throws Exception {
        Configuration config = new Configuration();
        List<String> whiteList = asList(dotToSlash(CountedMethodClass.class.getName()));
        config.setWhiteList(whiteList);

        Class<CountedMethodClass> clazz = execute(CountedMethodClass.class, config);

        Object obj = clazz.newInstance();

        obj.getClass().getMethod("counted").invoke(obj);

        assertEquals(1, metrics.getCount("counted"));
    }

    @Test
    public void ignoreBlackListed() throws Exception {
        Configuration config = new Configuration();
        List<String> blackList = asList(dotToSlash(CountedMethodClass.class.getName()));
        config.setBlackList(blackList);

        Class<CountedMethodClass> clazz = execute(CountedMethodClass.class, config);

        Object obj = clazz.newInstance();

        obj.getClass().getMethod("counted").invoke(obj);

        assertEquals(0, metrics.getCount("counted"));
    }

    public static class CountedMethodClass {

        @Counted(name = "counted")
        public void counted() {
            BaseMetricTest.performBasicTask();
        }
    }
}
