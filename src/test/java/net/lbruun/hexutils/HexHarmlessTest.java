/*
 * Copyright 2022 lbruun.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.lbruun.hexutils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HexHarmlessTest {

    @Test
    public void testConvertFromHarmless_char() {
        char c;
        char expResult;
        char result;
        
        c = 'g';
        expResult = '0';
        result = HexHarmless.convertFromHarmless(c);
        assertEquals(expResult, result);
        
        
        c = 'h';
        expResult = '1';
        result = HexHarmless.convertFromHarmless(c);
        assertEquals(expResult, result);
        
        c = 'k';
        expResult = '3';
        result = HexHarmless.convertFromHarmless(c);
        assertEquals(expResult, result);
        
        c = 'm';
        expResult = 'a';
        result = HexHarmless.convertFromHarmless(c);
        assertEquals(expResult, result);

        c = 't';
        expResult = 'e';
        result = HexHarmless.convertFromHarmless(c);
        assertEquals(expResult, result);
    }

    @Test
    public void testConvertToHarmless_char() {

        char c;
        char expResult;
        char result;
        
        c = '0';
        expResult = 'g';
        result = HexHarmless.convertToHarmless(c);
        assertEquals(expResult, result);
        
        
        c = '1';
        expResult = 'h';
        result = HexHarmless.convertToHarmless(c);
        assertEquals(expResult, result);
        
        c = '3';
        expResult = 'k';
        result = HexHarmless.convertToHarmless(c);
        assertEquals(expResult, result);
        
        c = 'a';
        expResult = 'm';
        result = HexHarmless.convertToHarmless(c);
        assertEquals(expResult, result);

        c = 'e';
        expResult = 't';
        result = HexHarmless.convertToHarmless(c);
        assertEquals(expResult, result);
    }

    @Test
    public void testConvertToHarmless_charArr() {
        char[] chars = new char[]{'a', 'b', '1'};
        char[] expResult = new char[]{'m', 'b', 'h'};
        char[] result = HexHarmless.convertToHarmless(chars);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testConvertFromHarmless_charArr() {
        char[] chars = new char[]{'m', 'b', 'h'};
        char[] expResult = new char[]{'a', 'b', '1'};
        char[] result = HexHarmless.convertFromHarmless(chars);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testByteToHexHarmless() {
        for (byte b : HexTestingUtils.ALL_BYTES) {
            char[] expResult = HexHarmless.convertToHarmless(
                    HexTestingUtils.jdkToHex(b, Hex.HexCase.LOWER).toCharArray());
            char[] result = HexHarmless.byteToHexHarmless(b);
            assertArrayEquals(expResult, result);
        }
    }


    @Test
    public void testHexCharHarmlessToByte() {
        for (int x = 0; x < HexHarmless.HARMLESS_HEX_CHARS.length; x++) {
            char c1 = HexHarmless.HARMLESS_HEX_CHARS[x];
            for (int y = 0; y < HexHarmless.HARMLESS_HEX_CHARS.length; y++) {
                char c2 = HexHarmless.HARMLESS_HEX_CHARS[y];
                byte bExpected = (byte) ((x * 16) + y);
                byte result = HexHarmless.hexCharHarmlessToByte(c1, c2);
                assertEquals(bExpected, result);
            }
        }
    }
}
