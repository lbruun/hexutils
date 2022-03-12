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

import java.nio.charset.StandardCharsets;
import net.lbruun.hexutils.Hex.HexCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HexDumpTest {
    
    /**
     * Outputs to stdout for visual inspection. Doesn't actually test anything.
     */
    @Test
    public void testPrettyPrint() {
        byte[] input;
        
        System.out.println("prettyPrint - with 4 indents");
        input = "Abcracadabra. There once was a fox which met a frog. And they became friends.".getBytes(StandardCharsets.UTF_8);
        HexDump.prettyPrint(input, HexCase.UPPER, 16, 4, System.out);
        
        System.out.println("prettyPrint - every possibly byte, with 2 indents");
        HexDump.prettyPrint(HexTestingUtils.ALL_BYTES, HexCase.UPPER, 16, 2, System.out);

        System.out.println("prettyPrint - almost random bytes, with 0 indents");
        input = HexTestingUtils.concatenateByteArrays(HexTestingUtils.randomBytes(10), "Abracadabra".getBytes(StandardCharsets.UTF_8), HexTestingUtils.randomBytes(55));
        HexDump.prettyPrint(input, HexCase.UPPER, 16, 0, System.out);
    }
    

    public void testPrettyPrint_more_than_16_and_uneven() {
        byte[] input;       
        
        // 19 bytes
        input = "ABCDEFGHIJKLMNOPQRS".getBytes(StandardCharsets.US_ASCII);
        String[] output = HexDump.prettyPrint(input, HexCase.UPPER, 16, 0);
        
        assertEquals(2, output.length);
        
        String expectedLine0 = "0000 : 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50  |ABCDEFGHIJKLMNOP|";
        String expectedLine1 = "0016 : 51 52 53                                         |QRS             |";
 
        assertEquals(expectedLine0, output[0]);
        assertEquals(expectedLine1, output[1]);
    }


    public void testPrettyPrint_less_than_16() {
        byte[] input;
        
        
        // 4 bytes
        input = "ABCD".getBytes(StandardCharsets.US_ASCII);
        String[] output = HexDump.prettyPrint(input, HexCase.UPPER, 16, 0);
        
        assertEquals(1, output.length);
        
        String line0 = "0000 : 41 42 43 44                                      |ABCD            |";
 
        assertEquals(line0, output[0]);
    }

    
    public void testPrettyPrint_exactly_16() {
        byte[] input;
               
        // 16 bytes
        input = "ABCDEFGHIJKLMNOP".getBytes(StandardCharsets.US_ASCII);
        String[] output = HexDump.prettyPrint(input, HexCase.UPPER, 16, 0);
        
        assertEquals(1, output.length);
        
        String line0 = "0000 : 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50  |ABCDEFGHIJKLMNOP|";
 
        assertEquals(line0, output[0]);
    }
}
