package org.oscm.app.connector.activity;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({
        HTTPClient.class,
        LogManager.class,
        Properties.class,
        HttpClientBuilder.class
})
public class HTTPClientTest {

    private static final Random RANDOM = new Random();

    private Logger logger;
    private Properties props;
    private HTTPClient httpClient;
    private HttpClientBuilder httpBuilder;

    private Map<String, String> transmitData;
    ResultSet resultSet;

    @BeforeClass
    public static void oneTimeSetup() {
        System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
        System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
    }

    @Before
    public void setUp() {
        this.httpClient = PowerMockito.spy(new HTTPClient());

        this.logger = mock(Logger.class);
        this.props = mock(Properties.class);

        this.transmitData = new HashMap<>();

        Whitebox.setInternalState(DatabaseWriter.class, "logger", logger);
    }

    @Test
    public void testDoConfigure() throws ProcessException {

        this.httpClient.doConfigure(this.props);

        verify(this.logger, times(1)).debug(contains("beanName: "));
    }

    @Test(expected = ProcessException.class)
    public void testTransmitReceiveDataProcessException() throws ProcessException {

        this.httpClient.transmitReceiveData(this.transmitData);

    }

    @Test
    public void testTransmitReceiveDataReturnTransmitData() throws Exception {
        PowerMockito.mockStatic(HttpClientBuilder.class);


        this.httpClient.setUrl("url&url");
        this.httpClient.setUsername("username");
        this.httpClient.setPassword("password");


        this.httpClient.transmitReceiveData(this.transmitData);

    }

//    @Test
//    public void testDoConfigure() throws ProcessException {
//
//        this.httpClient.doConfigure(this.props);
//
//        verify(this.logger, times(1)).debug(contains("beanName: "));
//    }
//
//    @Test
//    public void testDoConfigure() throws ProcessException {
//
//        this.httpClient.doConfigure(this.props);
//
//        verify(this.logger, times(1)).debug(contains("beanName: "));
//    }
}
