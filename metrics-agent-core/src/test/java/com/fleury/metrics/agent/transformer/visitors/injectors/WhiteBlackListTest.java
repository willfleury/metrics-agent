package com.fleury.metrics.agent.transformer.visitors.injectors;

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
        List<String> whiteList = asList(dotToSlash(CountedMethodClass.class.getName()));
        Configuration config = new Configuration(null, null, null, whiteList, null);

        Class<CountedMethodClass> clazz = execute(CountedMethodClass.class, config);

        Object obj = clazz.newInstance();

        obj.getClass().getMethod("counted").invoke(obj);

        assertEquals(1, metrics.getCount("counted"));
    }

    @Test
    public void ignoreBlackListed() throws Exception {
        List<String> blackList = asList(dotToSlash(CountedMethodClass.class.getName()));
        Configuration config = new Configuration(null, null, null, null, blackList);

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
