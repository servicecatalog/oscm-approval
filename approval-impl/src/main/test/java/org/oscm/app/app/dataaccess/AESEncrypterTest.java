/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 10 Aug 2020
 *
 * <p>*****************************************************************************
 */
package java.org.oscm.app.app.dataaccess;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import org.junit.Test;
import org.oscm.app.dataaccess.AESEncrypter;

/** @author worf */
public class AESEncrypterTest {

  @Test
  public void testEncrypt() throws GeneralSecurityException {
    // given
    String expected = "Ajgbts55l+DNxlgBjxxjFg==";
    // when
    String result = AESEncrypter.encrypt("test");
    // then
    assertEquals(expected, result);
  }

  @Test
  public void testEncryptByte() throws GeneralSecurityException {
    // given
    String expected = "Ajgbts55l+DNxlgBjxxjFg==";
    // when
    byte[] bytes = AESEncrypter.encrypt("test".getBytes());
    // then
    assertEquals(expected, new String(bytes, StandardCharsets.UTF_8));
  }

  @Test
  public void testDecrypt() throws GeneralSecurityException {
    // given
    String expected = "test";
    // when
    String result = AESEncrypter.decrypt("Ajgbts55l+DNxlgBjxxjFg==");
    // then
    assertEquals(expected, result);
  }

  @Test
  public void testDecryptByte() throws GeneralSecurityException {
    // given
    String expected = "test";
    // when
    byte[] result = AESEncrypter.decrypt("Ajgbts55l+DNxlgBjxxjFg==".getBytes());
    // then
    assertEquals(expected, new String(result, StandardCharsets.UTF_8));
  }
}
