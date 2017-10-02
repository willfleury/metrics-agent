package com.fleury.metrics.agent.config;

import static com.fleury.metrics.agent.model.MetricType.Counted;
import static com.fleury.metrics.agent.model.MetricType.Timed;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.fleury.metrics.agent.model.Metric;
import com.fleury.metrics.agent.model.MetricType;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Will Fleury
 */
public class ConfigurationTest {

    @Test
    public void testParseAndExpandMetricImports() {
        InputStream is = this.getClass().getResourceAsStream("/config/sample.yaml");
        Configuration config = Configuration.createConfig(is);

        assertFalse(config.findMetrics("com/fleury/sample/Engine").isEmpty());

        List<Metric> metrics = config.findMetrics("com/fleury/sample/Engine", "sampleMethod", "(I)J");
        assertEquals(2, metrics.size());

        assertMetricDetails(metrics.get(0), Counted, "count", "trying to count", Arrays.asList("name1:value1", "name2:value2"));
        assertMetricDetails(metrics.get(1), Timed, "timer", "trying to time", Arrays.asList("name1:value1", "name2:value2"));

        metrics = config.findMetrics("com/test/Special", "sampleMethod", "(Ljava/lang/String;)J");
        assertEquals(1, metrics.size());

        assertMetricDetails(metrics.get(0), Counted, "count", "trying to count", null);
    }

    private void assertMetricDetails(Metric metric, MetricType type, String name, String doc, List<String> labels) {
        assertEquals(type, metric.getType());
        assertEquals(name, metric.getName());
        assertEquals(doc, metric.getDoc());
        assertEquals(labels, metric.getLabels());
    }

    @Test
    public void testParseMetricSystemConfig() {
        InputStream is = this.getClass().getResourceAsStream("/config/sample.yaml");
        Configuration config = Configuration.createConfig(is);

        assertFalse(config.getSystem().isEmpty());
    }

}
