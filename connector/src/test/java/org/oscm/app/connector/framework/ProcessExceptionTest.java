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
    this.exceptionMsg = "\nDescription of what caused the error";
    this.message = "Test message for TestException";
    this.errorCode = 100;
    this.cause = new TestException(this.exceptionMsg);
  }

  @Test
  public void testProcessException() {
    this.process = new ProcessException(this.message, this.errorCode, this.cause);
    this.process = PowerMockito.spy(this.process);

    assertEquals(this.message, Whitebox.getInternalState(this.process, "message"));
    assertEquals(
        Optional.ofNullable(this.errorCode),
        Optional.ofNullable(Whitebox.getInternalState(this.process, "errorCode")));
    assertEquals(this.cause, Whitebox.getInternalState(this.process, "cause"));
  }

  @Test
  public void testProcessExceptionWithoutException() {
    this.process = new ProcessException(this.message, this.errorCode);
    this.process = PowerMockito.spy(this.process);

    assertEquals(this.message, Whitebox.getInternalState(this.process, "message"));
    assertEquals(
        Optional.ofNullable(this.errorCode),
        Optional.ofNullable(Whitebox.getInternalState(this.process, "errorCode")));
  }

  @Test
  public void testGetErrorCode() {
    this.process = new ProcessException(this.message, this.errorCode, this.cause);
    this.process = PowerMockito.spy(this.process);

    final int result = this.process.getErrorcode();

    assertEquals(this.errorCode, result);
  }

  @Test
  public void testGetCause() {
    this.process = new ProcessException(this.message, this.errorCode, this.cause);
    this.process = PowerMockito.spy(this.process);

    final Exception result = this.process.getCause();

    assertEquals(this.cause, result);
  }

  @Test
  public void testToString() {
    String expectedResult =
        "errorCode=100\nTest message for TestException\nDescription of what caused the error\n";
    this.process = new ProcessException(this.message, this.errorCode, this.cause);
    this.process = PowerMockito.spy(this.process);

    final String result = this.process.toString();

    assertEquals(expectedResult, result);
  }

  private static class TestException extends Exception {
    private TestException(String errorMessage) {
      super(errorMessage);
    }
  }
}
