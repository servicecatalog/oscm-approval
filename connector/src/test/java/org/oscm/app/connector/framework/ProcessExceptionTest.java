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

import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({ProcessException.class, Properties.class, IActivity.class})
public class ProcessExceptionTest {

  private ProcessException process;
  private String exceptionMsg;
  private String message;
  private int errorCode;
  private Exception cause;

  @Before
  public void setUp() {
    exceptionMsg = "\nDescription of what caused the error";
    message = "Test message for TestException";
    errorCode = 100;
    cause = new TestException(exceptionMsg);
  }

  @Test
  public void testProcessException() {
    process = new ProcessException(message, errorCode, cause);
    process = PowerMockito.spy(process);

    assertEquals(message, Whitebox.getInternalState(process, "message"));
    assertEquals(
        Optional.ofNullable(errorCode),
        Optional.ofNullable(Whitebox.getInternalState(process, "errorCode")));
    assertEquals(cause, Whitebox.getInternalState(process, "cause"));
  }

  @Test
  public void testProcessExceptionWithoutCause() {
    process = new ProcessException(message, errorCode);
    process = PowerMockito.spy(process);

    assertEquals(message, Whitebox.getInternalState(process, "message"));
    assertEquals(
        Optional.ofNullable(errorCode),
        Optional.ofNullable(Whitebox.getInternalState(process, "errorCode")));
  }

  @Test
  public void testGetErrorCode() {
    process = new ProcessException(message, errorCode, cause);
    process = PowerMockito.spy(process);

    final int result = process.getErrorcode();

    assertEquals(errorCode, result);
  }

  @Test
  public void testGetCause() {
    process = new ProcessException(message, errorCode, cause);
    process = PowerMockito.spy(process);

    final Exception result = process.getCause();

    assertEquals(cause, result);
  }

  @Test
  public void testToString() {
    String expectedResult =
        "errorCode=100\nTest message for TestException\nDescription of what caused the error\n";
    process = new ProcessException(message, errorCode, cause);
    process = PowerMockito.spy(process);

    final String result = process.toString();

    assertEquals(expectedResult, result);
  }

  private static class TestException extends Exception {
    private TestException(String errorMessage) {
      super(errorMessage);
    }
  }
}
