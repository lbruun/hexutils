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
 */
package net.lbruun.hexutils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utilities for converting byte(s) into hexadecimal character/string
 * representation and vice versa. Methods are optimized for speed.
 * 
 * <p>
 * Hexadecimal output produced by this class will always use exactly 
 * two characters per byte, in other words hex values lower than or 
 * equal to {@code 'F'} will be left-padded with a {@code '0'}.
 * 
 * <p>
 * A delimiter can optionally be used to separate the byte values
 * in the hexadecimal representation. The characters in the delimiter
 * must be printable characters from the {@code US_ASCII} character set
 * and it must furthermore not contain any of the characters used to 
 * represent the hex values themselves (characters {@code 0-9}, {@code A-f} and 
 * {@code a-f}).
 * 
 * <p>
 * JDK 17 introduces {@code HexFormat} class which can often be used instead
 * of this class.
 */
public class Hex {

    /**
     * Characters allowed in hex representation (lower-case)
     */
    public static final char[] HEX_CHARS_LOWER = 
            new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /**
     * Characters allowed in hex representation (upper-case)
     */
    public static final char[] HEX_CHARS_UPPER = 
            new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    

    /**
     * Regular expression character set with the characters that are 
     * allowed in hex representation. (both upper and lower case characters)
     */ 
    public static final String HEX_REGEXP_CHARCLASS = "[0-9A-Fa-f]";
    
    
    /**
     * Case used for hex string.
     * 
     * <p>
     * Example (the word 'Hello');
     * <ul>
     *   <li>Upper case: {@code 48656C6C6F}</li>
     *   <li>Lower case: {@code 48656c6c6f}</li>
     * </ul>
     */
    public enum HexCase {
        LOWER,
        UPPER
    }

    private Hex() {
    }
    
    /**
     * Converts a byte into hexadecimal representation and put the result into a
     * target character array at a specified location. Exactly two chars
     * will be written to the target array.
     *
     * @param b input to convert to hex
     * @param target the char array where the result will be put
     * @param targetStartPos where to put the hex chars in {@code target}
     *          in the target array.
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @return the next position in the target array (after writing to it)
     */
    private static int putHexIntoCharArray(byte b, char[] target, int targetStartPos, HexCase caseType) {
        int octet = b & 0xFF;
        char[] hexArray = (caseType == HexCase.UPPER) ? HEX_CHARS_UPPER : HEX_CHARS_LOWER;
        target[targetStartPos] = hexArray[octet >>> 4];
        target[targetStartPos + 1] = hexArray[octet & 0x0F];
        return targetStartPos + 2;
    }

    private static void putHexIntoOutputStream(byte b, OutputStream os, HexCase caseType) throws IOException {
        int octet = b & 0xFF;
        char[] hexArray = (caseType == HexCase.UPPER) ? HEX_CHARS_UPPER : HEX_CHARS_LOWER;
        os.write((byte) hexArray[octet >>> 4]);
        os.write((byte) hexArray[octet & 0x0F]);
    }

    /**
     * Converts a single byte to its hexadecimal representation.
     * 
     * @param b input
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @return char array of length 2
     */
    public static char[] byteToHex(byte b, HexCase caseType) {
        char[] hexChars = new char[2];
        putHexIntoCharArray(b, hexChars, 0, caseType);
        return hexChars;
    }

  
    /**
     * Converts a byte array to its hexadecimal representation.
     * 
     * @param bytes input
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param delim optional delimiter between byte values in the encoded output, may be {@code null}
     * @return hexadecimal representation
     */
    public static char[] bytesToHex(byte[] bytes, HexCase caseType, String delim) {
        delimiterCheck(delim);
        if (bytes == null) {
            return null;
        }
        if (bytes.length == 0) {
            return new char[]{};
        }
        Objects.requireNonNull(caseType, "caseType cannot be null");
        
        int delimLength = (delim != null) ? delim.length() : 0;
        int noOfDelims = bytes.length - 1;
        char[] hexChars = new char[(bytes.length * 2) + (noOfDelims * delimLength)];

        int arrPos = 0;
        for (int i = 0; i < bytes.length; i++) {
            arrPos = putHexIntoCharArray(bytes[i], hexChars, arrPos, caseType);
            if (i < (bytes.length - 1)) { // as long as not at the end
                if (delim != null && (!delim.isEmpty())) {
                    for (int j = 0; j < delim.length(); j++) {
                        hexChars[arrPos + j] = delim.charAt(j);
                    }
                    arrPos = arrPos + delim.length();
                }
            }
        }
        return hexChars;
    }

    
    /**
     * Converts a byte array to its hexadecimal representation and returns
     * the result as a string.
     * 
     * @param bytes input
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param delim optional delimiter between byte values in the encoded output, may be {@code null}
     * @return hexadecimal representation
     */
    public static String bytesToHexStr(byte[] bytes, HexCase caseType, String delim) {
        char[] chars = bytesToHex(bytes, caseType, delim);
        if (chars == null) {
            return null;
        }
        return new String(chars);
    }

    
    /**
     * Converts a byte array to its hexadecimal representation and returns
     * the result as a string.
     * 
     * @param bytes input
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @return hexadecimal representation
     */
    public static String bytesToHexStr(byte[] bytes, HexCase caseType) {
        char[] chars = bytesToHex(bytes, caseType, null);
        if (chars == null) {
            return null;
        }
        return new String(chars);
    }
    
    
    
    /**
     * Converts a byte array to its hexadecimal representation and appends
     * the result to the provided {@code StringBuilder}.
     * 
     * @param bytes input 
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param delim optional delimiter between byte values in the encoded output, may be {@code null}
     * @param sb the StringBuilder to append to (not null)
     * @return the StringBuilder (same as the input 'sb')
     */
    public static StringBuilder bytesToHexStr(byte[] bytes, HexCase caseType, String delim, StringBuilder sb) {
        Objects.requireNonNull(sb, "Argument sb must not be null");
        char[] chars = bytesToHex(bytes, caseType, delim);
        if (chars == null) {
            return sb;
        }
        sb.append(chars);
        return sb;
    }
    
    /**
     * Converts a byte array to its hexadecimal representation and appends
     * the result to the provided {@code StringBuilder}.
     * 
     * @param bytes input 
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param sb the StringBuilder to append to (not null)
     * @return the StringBuilder (same as the input 'sb')
     */
    public static StringBuilder bytesToHexStr(byte[] bytes, HexCase caseType, StringBuilder sb) {
        return bytesToHexStr(bytes, caseType, null, sb);
    }

    private static void bytesToHexStreaming(InputStream is, OutputStream os, HexCase caseType, String delim, int maxCharsPerLine) throws IOException {
        delimiterCheck(delim);
        byte[] lineFeed = System.lineSeparator().getBytes(StandardCharsets.US_ASCII);
        byte[] delimBytes = null;
        int delimLen = 0;
        if (delim != null) {
            delimBytes = delim.getBytes(StandardCharsets.US_ASCII);
            delimLen = delimBytes.length;
        }
        int maxBytesPerLine = (maxCharsPerLine + delimLen) / (delimLen +2);       
        
        int b;
        boolean firstLineByteWritten = false;
        int bytesWritten = 0;
        while ((b = is.read()) != -1) {
            if (delimBytes != null) {
                if (firstLineByteWritten) {
                    os.write(delimBytes);
                } else {
                    firstLineByteWritten = true;
                }
            }
            putHexIntoOutputStream((byte) b, os, caseType);
            if (maxCharsPerLine != -1) {
                bytesWritten++;
                if (bytesWritten == maxBytesPerLine) {
                    os.write(lineFeed);
                    firstLineByteWritten = false;
                    bytesWritten = 0;
                }
            }
        }
    }

    /**
     * Converts a stream of bytes (from an InputStream) to its hexadecimal
     * character representation and writes the result to the provided
     * OutputStream. This method provides a way to convert large amounts of
     * binary data without causing an out-of-memory error.
     * 
     * <p>
     * The method reads from the InputStream until end-of-stream or an error
     * occurs. Neither the InputStream nor the OutputStream is closed by
     * the method.
     * 
     * <p>
     * Note that representing large binary data structures in {@code base16} encoded
     * format (another word for hexadecimal character representation) is very
     * inefficient compared to for example {@code base64} encoding. Base64 
     * encoding is almost always preferable.
     * 
     * <p>
     * When dealing with files it is likely to be considerably faster if a 
     * buffered stream is used.
     *
     * @see #bytesToHexStreaming(Path, Path, HexCase, String, int, OpenOption...) 
     * @param is input 
     * @param os output
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param delim optional delimiter between byte values in the encoded output, may be {@code null}
     * @throws IOException if reading from the provided InputStream or writing
     *      to the provided OutputStream causes an exception.
     */
    public static void bytesToHexStreaming(InputStream is, OutputStream os, HexCase caseType, String delim) throws IOException {
        bytesToHexStreaming(is, os, caseType, delim, -1);
    }


    
    /**
     * Converts the content of a file to its hexadecimal character
     * representation and writes the result to another file.
     *
     * <p>
     * Assuming the delimiter is either {@code null} or some simple character(s)
     * like {@code '-'} then it can be guaranteed that the output file
     * will consist only of printable characters:  {@code 0-9}, {@code A-F}
     * or {@code a-f} as well as what is in the optional delimiter.
     * 
     * <p>
     * Using the {@code maxCharsPerLine} parameter the output file can optionally be
     * written with line breaks so that the file will look more like a text
     * file. The line break character(s) used will be {@link System#lineSeparator() 
     * according to platform}.
     * 
     * <p>
     * Note that representing large binary data structures in {@code base16} encoded
     * format (another word for hexadecimal character representation) is very
     * inefficient compared to for example {@code base64} encoding. Base64 
     * encoding is almost always preferable.
     * 
     * @param in input file
     * @param out output file
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param delim optional delimiter between byte values in the encoded output, may be {@code null}
     * @param maxCharsPerLine maximum number of characters per line in the output file
     *    or {@code -1} to not break the output into lines.
     * @param outOptions options specifying how the output file is opened.
     * @throws IOException if reading from the input file or writing
     *      to the output file causes an exception.
     */
    public static void bytesToHexStreaming(Path in, Path out, HexCase caseType, String delim, int maxCharsPerLine, OpenOption... outOptions) throws IOException {
        try ( BufferedInputStream is
                = new BufferedInputStream(Files.newInputStream(in));  
              BufferedOutputStream os
                = new BufferedOutputStream(Files.newOutputStream(out, outOptions));) {
            bytesToHexStreaming(is, os, caseType, delim, maxCharsPerLine);
        }
    }


    
    /**
     * Converts a hex character pair into a byte. For example the pair {@code '7'}
     * and {@code 'D'} returns the byte value {@code 125}.
       * 
     * <p>
     * The input values must be only the characters {@code 0-9}, {@code A-F} and
     * {@code a-f}.
     *    
     * @throws IllegalArgumentException if either c1 or c2 is an illegal character.
     * @param c1 first character of pair
     * @param c2 second character of pair
     * @return 
     */
    public static byte hexCharToByte(char c1, char c2) {
        int high = hexToDec(c1);
        int low = hexToDec(c2);
        if (high == -1 || low == -1) {
            char illegal = (high == -1) ? c1 : c2;
            throw new IllegalArgumentException("Character '" + illegal + "' is illegal. Only characters 0-9, A-F and a-f are allowed.");
        }
        return (byte) ((high * 16) + low);
    }
    
    
    

    /**
     * Converts a character sequence (a string) consisting of hexadecimal 
     * characters into a byte array.
     * 
     * <p>
     * The input must consist only of characters
     * {@code 0-9}, {@code A-F} and {@code a-f} as well as the characters 
     * from the optional {@code delimiter}.
     * 
     * @throws HexConversionException if input character array contains illegal chars
     * @param chars input
     * @param delim optional delimiter between byte values in the hex string input,
     *    may be {@code null}.
     *    If the hex string was created with a delimiter the then same 
     *    delimiter must be used here.
     * @return byte array or {@code null} if {@code chars} is {@code null}.
     */
    public static byte[] hexStrToBytes(char[] chars, String delim) {
        if (chars == null) {
            return null;
        }
        return hexStrToBytes(new String(chars), delim);
    }

    /**
     * Converts a character sequence (a string) consisting of hexadecimal 
     * characters into a byte array.
     * 
     * <p>
     * The input must consist only of characters {@code 0-9}, {@code A-F} and
     * {@code a-f}.
     * 
     * @param chars input
     * @return byte array or {@code null} if {@code chars} is {@code null}.
     */
    public static byte[] hexStrToBytes(char[] chars) {
        if (chars == null) {
            return null;
        }
        return hexStrToBytes(new String(chars), null);
    }

    /**
     * Converts a character sequence (a string) consisting of hexadecimal 
     * characters into a byte array.
     * 
     * <p>
     * The input must consist only of characters
     * {@code 0-9}, {@code A-F} and {@code a-f} as well as the characters 
     * from the optional {@code delimiter}.
     * 
     * @throws HexConversionException if input CharSequence contains illegal chars
     * @param s input
     * @param delim optional delimiter between byte values in the hex string input,
     *    may be {@code null}.
     *    If the hex string was created with a delimiter the then same 
     *    delimiter must be used here.
     * @return byte array or {@code null} if input {@code s} is {@code null}.
     */
    public static byte[] hexStrToBytes(CharSequence s, String delim) {
        delimiterCheck(delim);
        if (s == null) {
            return null;
        }
        final int len = s.length();
        if (len == 0) {
            return new byte[0];
        }
        
        int delimLen = (delim == null) ? 0 : delim.length();
        
        int numerator = len + delimLen;
        int divisor = 2 + delimLen;
        if (numerator % divisor != 0) {
            throw new IllegalArgumentException("Hex string \"" + s+  "\" is of unexpected length");
        }

        byte[] out = new byte[numerator / divisor];

        int byteCount = 0;
        for (int i = 0; i < len;) {
            char c = s.charAt(i);
            if (delimLen > 0 && (delim.indexOf(c) >= 0)) {
                // Skip delimiter char
                i++;
                continue;
            }
            int high = hexToDec(c);
            int low = hexToDec(s.charAt(i + 1));
            if (high == -1 || low == -1) {
                int pos = (high == -1) ? i : i+1;
                throw new HexConversionException("Hex string \"" + s + "\" contains illegal character at position " + (pos+1) + ". Only characters 0-9, A-F and a-f are allowed.");
            }
            out[byteCount] = (byte) ((high * 16) + low);
            i += 2;
            byteCount++;
        }

        return out;
    }


    /**
     * Converts a character sequence (a string) consisting of hexadecimal 
     * characters into a byte array.
     * 
     * <p>
     * The input must consist only of characters {@code 0-9}, {@code A-F} and
     * {@code a-f}.
     * 
     * @throws HexConversionException if input CharSequence contains illegal chars
     * @param s input
     * @return byte array or {@code null} if input {@code s} is {@code null}.
     */
    public static byte[] hexStrToBytes(CharSequence s) {
        return hexStrToBytes(s, null);
    }


    
    /**
     * Converts an input stream of hexadecimal characters characters into bytes
     * and writes the resulting bytes to output stream.
     * 
     * <p>
     * The input stream must contain only bytes which complies with one of the
     * following:
     * <ul>
     *   <li>Is a hex character as per {@link isHexChar(char)}.</li>
     *   <li>Is an EOL character (decimal 10 and 13).</li>
     *   <li>Is a character from the delimiter string.</li>
     * </ul>
     * As such the method can read output produced by {@code bytesToHexStreaming()}
     * methods of this class.
     * 
     * <p>
     * The input stream is read until end-of-stream or an error occurs. Neither
     * the input stream nor the output stream is closed by this method. You must
     * do this yourself.
     * 
     * @throws HexConversionException if input stream contains invalid characters
     * @throws IOException if reading from the input stream or writing
     *      to the output stream causes an exception.
     * @param is input
     * @param os output 
     * @param delim
     */
    public static void hexToBytesStreaming(InputStream is, OutputStream os, String delim) throws IOException {

        delimiterCheck(delim);

        byte[] delimBytes = null;
        int delimLen = -1;
        if (delim != null && (!delim.isEmpty())) {
            delimBytes = delim.getBytes(StandardCharsets.US_ASCII);
            delimLen = delimBytes.length;
        }
        
        char c1 = 0;
        char c2 = 0;
        int b;   // byte read from inputstream
        int bytePos = -1;   // current position in inputstream
        int delimPos = -1;  // current position in delimiter which is expected
        while ((b = is.read()) != -1) {
            bytePos++;
            if (b > 127) {
                throw new HexConversionException("Character not belonging to US-ASCII character set was found at byte position " + bytePos);
            }

            if (delimBytes != null && delimPos == -1) {
                if (b == delimBytes[0]) {
                    if (delimLen > 1) {
                        delimPos = 1;
                    }
                    continue;
                }
            }
            if (delimPos != -1) {
                if (b == delimBytes[delimPos]) {
                    if (delimPos < (delimLen - 1)) {
                        delimPos++;
                    } else {
                        delimPos = -1;
                    }
                    continue;
                }
                throw new HexConversionException("Illegal character at position " + bytePos + ". Expected '" + delim.charAt(delimPos) + "'");
            }
            if (b == 10 || b == 13) {  // ignore EOL chars
                continue;
            }
            
            
            // The byte read must now be a hex char
            
            char c = (char)b;

            if (!isHexChar(c)) {
                throw new HexConversionException("Illegal character at position " + bytePos);
            }
            
            if (c1 == 0) {
                c1 = c;
            } else {
                c2 = c;
                byte convertedByte = hexCharToByte(c1, c2);
                os.write(convertedByte);
                c1 = 0;  // reset
                c2 = 0;  // reset
            }
        }
    }

    /**
     * Converts the content of a hex file file to its byte representation and
     * writes the result to another file.
     *
     * <p>
     * The input file must contain only bytes which complies with one of the
     * following:
     * <ul>
     *   <li>Is a hex character as per {@link isHexChar(char)}.</li>
     *   <li>Is an EOL character (decimal 10 and 13).</li>
     *   <li>Is a character from the delimiter string.</li>
     * </ul>
     * As such the method can read output produced by {@code bytesToHexStreaming()}
     * methods of this class.
     * 
     * 
     * @param in input file
     * @param out output file
     * @param delim optional delimiter between byte values in the encoded output, may be {@code null}.
     *    If the input file uses a delimiter between hex pairs then the same delimiter must be 
     *    specified here.
     * @param outOptions options specifying how the output file is opened.
     * @throws IOException if reading from the input file or writing
     *      to the output file causes an exception.
     * @throws HexConversionException if input file contains invalid characters
     */
    public static void hexToBytesStreaming(Path in, Path out, String delim, OpenOption... outOptions) throws IOException {
        try ( BufferedInputStream is
                = new BufferedInputStream(Files.newInputStream(in));  BufferedOutputStream os
                = new BufferedOutputStream(Files.newOutputStream(out, outOptions));) {
            hexToBytesStreaming(is, os, delim);
        }
    }


    /**
     * Tests if a string only contains hex characters.
     * Testing is done as per {@link isHexChar(char)} method.
     *
     * @param str input
     * @return true if all characters in {@code str} are hex characters, false otherwise
     *    or if {@code str} is {@code null}.
     */
    public static boolean isHexStr(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!isHexChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    /**
     * Tests if a character is a hex character.
     * This means any of the following characters:
     * <ul>
     *   <li>{@code 0-9}</li>
     *   <li>{@code A-F}</li>
     *   <li>{@code a-f}</li>
     * </ul>
     * @param c the character to test
     * @return true if the character is a hex character, otherwise false
     */
    public static boolean isHexChar(char c) {
        return ( c >= '0' && c <= '9') ||
                ( c >= 'A' && c <= 'F') ||
                ( c >= 'a' && c <= 'f');
    }

    
    /* ******************************************************
       Helpers
       ****************************************************** */
    
    
    private static void delimiterCheck(String delim) {
        if (delim == null || delim.isEmpty()) {
            return;
        }
        for (int i = 0; i < delim.length(); i++) {
            char c = delim.charAt(i);
            if (isHexChar(c)) {
                throw new IllegalArgumentException("delim must not contain any of the characters 0-9, A-F or a-f");
            }
            if (!isAsciiPrintableChar(c)) {
                throw new IllegalArgumentException("delim must only contain printable ASCII-127 characters");
            }
        }
    }
    
    private static boolean isAsciiPrintableChar(char c) {
        return (c >= 32 && c < 127);
    }
    
    
    private static int hexToDec(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        if ('A' <= c && c <= 'F') {
            return c - 'A' + 10;
        }
        if ('a' <= c && c <= 'f') {
            return c - 'a' + 10;
        }
        return -1; // represents illegal char 
    }

}
