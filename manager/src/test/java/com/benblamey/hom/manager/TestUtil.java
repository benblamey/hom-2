package com.benblamey.hom.manager;


import com.benblamey.hom.manager.Util;
import org.junit.Test;

public class TestUtil {

    @Test
    static void testRandomAlphaString() {
        String str = Util.randomAlphaString(10);
        assert str.length() == 10;
        System.out.println(str);
    }

    public static void main(String args[]) {
        testRandomAlphaString();
    }
}
