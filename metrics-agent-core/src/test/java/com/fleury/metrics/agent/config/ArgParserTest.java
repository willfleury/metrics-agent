package com.fleury.metrics.agent.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.Test;

public class ArgParserTest {

    @Test
    public void testAllConfigPresent() throws Exception {
        String args = "agent-config:agent.yaml,log-config:log.properties";
        ArgParser parser = new ArgParser(args);

        assertThat(parser.getConfigFilename(), is("agent.yaml"));
        assertThat(parser.getLogConfigFilename(), is("log.properties"));
    }

    @Test
    public void testNoConfigPresent() throws Exception {
        ArgParser parser = new ArgParser("");

        assertThat(parser.getConfigFilename(), is(nullValue()));
        assertThat(parser.getLogConfigFilename(), is("/logging.properties"));
    }

    @Test
    public void testOneConfigPresent() throws Exception {
        ArgParser parser = new ArgParser("agent-config:agent.yaml");

        assertThat(parser.getConfigFilename(), is("agent.yaml"));
    }
}
