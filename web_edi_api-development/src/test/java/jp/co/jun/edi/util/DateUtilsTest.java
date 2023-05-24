// CHECKSTYLE:OFF

package jp.co.jun.edi.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * テスト.
 */
public class DateUtilsTest {

    @Test
    public void test_stringYYYYWWToDate_201852() throws Exception {
        assertEquals("2018/12/30", DateUtils.formatYMD(DateUtils.stringYYYYWWToDate("201852")));
    }

    @Test
    public void test_stringYYYYWWToDate_201901() throws Exception {
        assertEquals("2019/01/06", DateUtils.formatYMD(DateUtils.stringYYYYWWToDate("201901")));
    }

    @Test
    public void test_stringYMDToYYYYWW_20181121() throws Exception {
        assertEquals("201847", DateUtils.stringYMDToYYYYWW("2018/11/21"));
    }

    @Test
    public void test_stringYMDToYYYYWW_20181230() throws Exception {
        assertEquals("201852", DateUtils.stringYMDToYYYYWW("2018/12/30"));
    }

    @Test
    public void test_stringYMDToYYYYWW_20181231() throws Exception {
        assertEquals("201901", DateUtils.stringYMDToYYYYWW("2018/12/31"));
    }

    @Test
    public void test_stringYMDToYYYYWW_20190101() throws Exception {
        assertEquals("201901", DateUtils.stringYMDToYYYYWW("2019/01/01"));
    }
}
//CHECKSTYLE:ON
