// CHECKSTYLE:OFF

package jp.co.jun.edi.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * テスト.
 */
public class LogStringUtilTest {

    @Test
    public void test() throws Exception {
        assertEquals(
                "{\"log-name\":{\"message\":\"log-message\",\"java.lang.NullPointerException\":{\"message\":\"log-exception-message\"},\"key1\":\"value1\",\"key2\":{\"key2-1\":\"value2-1\",\"key2-2\":\"value2-2\"},\"key3\":\"value3\",\"key4\":\"value4\"}}",
                LogStringUtil.of("log-name")
                        .message("log-message")
                        .exception(new NullPointerException("log-exception-message"))
                        .value("key1", "value1")
                        .value("key2", LogStringUtil.ofMap().put("key2-1", "value2-1").put("key2-2", "value2-2").toMap())
                        .value("key3", "value3")
                        .value("key4", "value4")
                        .build());
    }

}
//CHECKSTYLE:ON
