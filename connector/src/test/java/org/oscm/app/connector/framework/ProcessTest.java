package org.oscm.app.connector.framework;

import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({Process.class, Properties.class, IActivity.class})
public class ProcessTest {

  private Process process;
  private Properties props;
  private IActivity iActivity;

  private Map<String, String> transmitData;

  @Before
  public void setUp() {
    this.process = PowerMockito.spy(new Process());
    this.props = mock(Properties.class);
    this.iActivity = mock(Activity.class);
    this.transmitData = new HashMap<>();
  }

  @Test
  public void testSetConfiguration() {

    this.process.setConfiguration(this.props);

    assertEquals(this.props, this.process.getConfiguration());
  }

  @Test
  public void testSetActivity() throws ProcessException {

    this.process.setActivity(this.iActivity);

    assertEquals(this.iActivity, Whitebox.getInternalState(this.process, "activity"));
  }

  @Test
  public void testExecute() throws ProcessException {
    this.process.setActivity(this.iActivity);

    this.process.execute(this.transmitData);

    verify(this.iActivity, times(1)).transmitReceiveData(this.transmitData);
  }
}
