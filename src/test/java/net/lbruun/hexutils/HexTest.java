/*
 * Copyright 2021 lbruun.net.
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
 */package net.lbruun.hexutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.lbruun.hexutils.Hex.HexCase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HexTest {
    

    public void testByteToHex() {
        for(byte b : HexTestingUtils.ALL_BYTES) {
            String actual = new String(Hex.byteToHex(b, HexCase.LOWER));
            String expected = HexTestingUtils.jdkToHex(b, HexCase.LOWER);
            assertEquals(expected, actual);
        }
    }
    
    @Test
    public void testBytesToHex() {
        byte[] bytes;
        String hexStr;
        String expected;
        
        
        bytes = "æøåÆØÅ".getBytes(StandardCharsets.UTF_8);
        hexStr = Hex.bytesToHexStr(bytes, HexCase.LOWER);
        expected = HexTestingUtils.jdkToHex(bytes, HexCase.LOWER);
        assertEquals(expected, hexStr);

        bytes = "abcPHqmn".getBytes(StandardCharsets.UTF_8);
        hexStr = Hex.bytesToHexStr(bytes, HexCase.UPPER);
        expected = HexTestingUtils.jdkToHex(bytes, HexCase.UPPER);
        assertEquals(expected, hexStr);

        bytes = "withDelim".getBytes(StandardCharsets.UTF_8);
        hexStr = Hex.bytesToHexStr(bytes, HexCase.UPPER, ":");
        expected = HexTestingUtils.jdkToHex(bytes, HexCase.UPPER, ":");
        assertEquals(expected, hexStr);

        bytes = "withDelim2".getBytes(StandardCharsets.UTF_8);
        hexStr = Hex.bytesToHexStr(bytes, HexCase.UPPER, ":-");
        expected = HexTestingUtils.jdkToHex(bytes, HexCase.UPPER, ":-");
        assertEquals(expected, hexStr);
    }

    @Test
    public void testHexToByte() {
        byte actual;
        byte expected;
        
        actual = Hex.hexCharToByte('0', 'a');
        expected = "\n".getBytes(StandardCharsets.US_ASCII)[0];
        assertEquals(expected, actual);
        
        actual = Hex.hexCharToByte('f', '6');
        expected = (byte) 246;
        assertEquals(expected, actual);
    }
    
    
    @Test
    public void testHexesToByte() {
        
        String hexStr;
        byte[] bytes;
        byte[] bytesExpected;

        hexStr = "6a617661";
        bytes = Hex.hexStrToBytes(hexStr);
        bytesExpected = "java".getBytes(StandardCharsets.UTF_8);        
        assertArrayEquals(bytesExpected, bytes);
        
        
        hexStr = "6a-61-76-61";
        bytes = Hex.hexStrToBytes(hexStr, "-");
        bytesExpected = "java".getBytes(StandardCharsets.UTF_8);        
        assertArrayEquals(bytesExpected, bytes);
        
    }

    @Test
    public void testBytesToHexStr() {
        byte[] bytes;
        String delim = ":";
        String actual;
        String expResult;

        bytes = "abcPHqmn".getBytes(StandardCharsets.UTF_8);
        expResult = HexTestingUtils.jdkToHex(bytes, HexCase.LOWER, delim);
        actual = Hex.bytesToHexStr(bytes, HexCase.LOWER, delim);
        assertEquals(expResult, actual);
    }
    
    
    @Test
    public void testBytesToHexAndBack() throws IOException {
        
        bytesToHexAndBack0(1000, "-", true);
        bytesToHexAndBack0(1000, null, true);
        
        bytesToHexAndBack0(1000, "::", true);
        assertThrows(IllegalArgumentException.class, () -> {
            bytesToHexAndBack0(1000, "-\n", true);
        });
    }

    private void bytesToHexAndBack0(int fileSize, String delim, boolean showOnStdout) throws IOException {
        
        Path tmpBinFile1 = HexTestingUtils.getTmpRandomBytesFile("test-", ".bin", fileSize);
        Path tmpBinFile2 = Files.createTempFile("test-", ".bin");
        
        
        Path tmpHexFile = Files.createTempFile("test-", ".hex");

        // Convert bytes --> hex
        Hex.bytesToHexStreaming(tmpBinFile1, tmpHexFile, HexCase.LOWER, delim, 78);
        
        
        // Write the file to stdout (for visual inspection)
        if (showOnStdout) {
            try ( BufferedReader bfr = Files.newBufferedReader(tmpHexFile, StandardCharsets.US_ASCII)) {
                bfr.lines().forEach((line) -> {
                    System.out.println(line);
                });
            }
        }
        
        // Convert hex --> bytes
        Hex.hexToBytesStreaming(tmpHexFile, tmpBinFile2, delim);

        assertArrayEquals(Files.readAllBytes(tmpBinFile1), Files.readAllBytes(tmpBinFile2));
        
        tmpBinFile1.toFile().delete();
        tmpBinFile2.toFile().delete();
        tmpHexFile.toFile().delete();
    }
}
