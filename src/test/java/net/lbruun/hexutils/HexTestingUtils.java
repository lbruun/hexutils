/*
 * Copyright 2022 lbruun.net
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;
import net.lbruun.hexutils.Hex.HexCase;


/**
 * Useful utilities for testing purpose.
 */
public class HexTestingUtils {
    
    // Array with every possible byte
    public static final byte[] ALL_BYTES = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            ALL_BYTES[i] = (byte) i;
        }
    }

    private HexTestingUtils() {
    }

    /**
     * Creates a temporary file filled with random bytes.
     */
    public static Path getTmpRandomBytesFile(String suffix, String prefix, int fileSize) throws IOException {
        Path tempFile = Files.createTempFile(suffix, prefix);
        int bufferSize = 8192;
        byte[] buffer = new byte[bufferSize];
        int chunks = fileSize / bufferSize;
        if ( (fileSize % bufferSize) > 0) {
            chunks++;
        }
        
        int bytesWritten = 0;
        try ( OutputStream os = Files.newOutputStream(tempFile)) {
            for (int i = 0; i < chunks; i++) {
                int bytesToBeWritten = fileSize - bytesWritten;
                if (bytesToBeWritten > bufferSize) {
                    bytesToBeWritten = bufferSize;
                }
                randomBytes(buffer, 0, bytesToBeWritten);
                os.write(buffer, 0, bytesToBeWritten);
            }
        }
        return tempFile;
    }
    
    
    public static void randomBytes(byte[] bArr, int startPos, int noOfBytes) {
        Random random = new Random();
        for (int i = startPos; i < noOfBytes; i++) {
            bArr[i] = (byte) random.nextInt(256);
        }
    }
    
    public static byte[] randomBytes(int noOfBytes) {
        Random random = new Random();
        byte[] bArr = new byte[noOfBytes];
        for (int i = 0; i < noOfBytes; i++) {
            bArr[i] = (byte) random.nextInt(256);
        }
        return bArr;
    }

    public static byte[] concatenateByteArrays(byte[]... bArrays) {
        int totalLen = 0;
        for (byte[] bArr : bArrays) {
            totalLen = totalLen + bArr.length;
        }
        byte[] out = new byte[totalLen];
        int pos = 0;
        for (byte[] bArr : bArrays) {
            System.arraycopy(bArr, 0, out, pos, bArr.length);
            pos = pos + bArr.length;
        }
        return out;
    }

    
    
    /**  Java8 method used for comparison.  */
    public static String jdkToHex(byte b, HexCase caseType) {
        String str = Integer.toString(Byte.toUnsignedInt(b), 16);
        if (caseType == HexCase.UPPER) {
            str = str.toUpperCase(Locale.ENGLISH);
        }
        if (str.length() == 1) {
            return "0" + str;
        }
        return str;
    }

    /**  Java8 method used for comparison.  */
    public static String jdkToHex(byte[] bArr, HexCase caseType, String delim) {
        int delimLen = (delim == null) ? 0 : delim.length();
        StringBuilder sb = new StringBuilder((bArr.length * 2) + ((bArr.length - 1) * delimLen));
        for(byte b : bArr) {
            if (sb.length() > 0 && delimLen > 0) {
                sb.append(delim);
            }
            String str = Integer.toString(Byte.toUnsignedInt(b), 16);
            if (caseType == HexCase.UPPER) {
                str = str.toUpperCase(Locale.ENGLISH);
            }
            if (str.length() == 1) {
                sb.append(' ');
            }
            sb.append(str);
        }
        return sb.toString();
    }

    /**  Java8 method used for comparison.  */
    public static String jdkToHex(byte[] bArr, HexCase caseType) {
        return jdkToHex(bArr, caseType, null);
    }
}
