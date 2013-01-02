package edu.sc.seis.seisFile.sac;

import junit.framework.TestCase;

/**
 * SacTimeSeriesTest.java
 * 
 * @author Created by Philip Oliver-Paull
 */
public class SacTimeSeriesTest extends TestCase {

    public void testSwapBytesShort() {
        short s1 = Short.MIN_VALUE;
        short s2 = Short.MAX_VALUE;
        short s3 = 0;
        short s4 = SacHeader.swapBytes(SacHeader.swapBytes(s1));
        short s5 = SacHeader.swapBytes(SacHeader.swapBytes(s2));
        short s6 = SacHeader.swapBytes(SacHeader.swapBytes(s3));
        assertEquals(s1, s4);
        assertEquals(s2, s5);
        assertEquals(s3, s6);
    }

    public void testSwapBytesInt() {
        int i1 = Integer.MIN_VALUE + 1;
        int i2 = Integer.MAX_VALUE - 1;
        int i3 = 0;
        int i4 = 255 + (252 << 8) + ( 26 << 16) + (74 << 24);
        int i1s = SacHeader.swapBytes(SacHeader.swapBytes(i1));
        int i2s = SacHeader.swapBytes(SacHeader.swapBytes(i2));
        int i3s = SacHeader.swapBytes(SacHeader.swapBytes(i3));
        int i4s = SacHeader.swapBytes(SacHeader.swapBytes(i4));
        assertEquals(i1, i1s);
        assertEquals(i2, i2s);
        assertEquals(i3, i3s);
        assertEquals(i4, i4s);
        System.out.println(i4+"  "+i4s);
    }

    public void testSwapBytesLong() {
        long l1 = Long.MIN_VALUE;
        long l2 = Long.MAX_VALUE;
        long l3 = 0l;
        long l4 = SacHeader.swapBytes(SacHeader.swapBytes(l1));
        long l5 = SacHeader.swapBytes(SacHeader.swapBytes(l2));
        long l6 = SacHeader.swapBytes(SacHeader.swapBytes(l3));
        assertEquals(l1, l4);
        assertEquals(l2, l5);
        assertEquals(l3, l6);
    }
}
