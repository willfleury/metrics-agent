package com.fleury.metrics.agent.config;

import static com.fleury.metrics.agent.model.MetricType.Counted;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fleury.metrics.agent.model.Metric;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.junit.Test;

/**
 *
 * @author Will Fleury
 */
public class ConfigurationTest {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationTest.class.getName());

    @Test
    public void testParseMetricsConfig() {
        InputStream is = this.getClass().getResourceAsStream("/config/sample.yaml");
        Configuration config = Configuration.createConfig(is);

        assertTrue(!config.getMetrics().isEmpty());

        List<Metric> metrics = config.findMetrics("com/fleury/sample/Engine", "sampleMethod(I)J");
        assertEquals(2, metrics.size());

        Metric metric = metrics.get(0);
        assertEquals(Counted, metric.getType());
        assertEquals("count", metric.getName());
        assertEquals("trying to count", metric.getDoc());
        assertEquals(Arrays.asList("name1:value1", "name2:value2"), metric.getLabels());
    }

    @Test
    public void testParseMetricSystemConfig() {
        InputStream is = this.getClass().getResourceAsStream("/config/sample.yaml");
        Configuration config = Configuration.createConfig(is);

        assertTrue(!config.getMetricSystemConfiguration().isEmpty());
    }

}
