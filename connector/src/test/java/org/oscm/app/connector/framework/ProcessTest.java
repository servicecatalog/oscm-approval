/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.framework;

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
    process = PowerMockito.spy(new Process());
    props = mock(Properties.class);
    iActivity = mock(Activity.class);
    transmitData = new HashMap<>();
  }

  @Test
  public void testSetConfiguration() {

    process.setConfiguration(props);

    assertEquals(props, process.getConfiguration());
  }

  @Test
  public void testSetActivity() throws ProcessException {

    process.setActivity(iActivity);

    assertEquals(iActivity, Whitebox.getInternalState(process, "activity"));
  }

  @Test
  public void testExecute() throws ProcessException {
    process.setActivity(iActivity);

    process.execute(transmitData);

    verify(iActivity, times(1)).transmitReceiveData(transmitData);
  }
}
