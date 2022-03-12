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

import java.io.PrintStream;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Utilities for pretty printing a byte array, also known as "hex dumps". Often
 * such dumps are printed in the debug mode of an application, for example when
 * representing wire data.
 *
 * <p>
 * Output look similar to the following example:
 * <pre>
 * 0000 : BF A4 3B 16 1C 88 5C 56 11 78 41 62 72 61 63 61  |..;...\V.xAbraca|
 * 0016 : 64 61 62 72 61 1A 06 94 80 CA 1A 38 42 ED 58 25  |dabra......8B.X%|
 * 0032 : 92 03 AD BF 7F 60 F4 73 AD 55 B0 A9 5C C4 86 B4  |.....`.s.U..\...|
 * 0048 : 5B 11 62 55 B3 BD C4 EA C3 EA 4D 66 2A 02 1C 6C  |[.bU......Mf*..l|
 * 0064 : 0E 84 B1 F8 D8 05 91 51 CE A3 9E 46              |.......Q...F    |
 * </pre>
 * The output can optionally be prefixed with space indention.
 * <p>
 * There are 3 sections in the output:
 * <ol>
 *   <li>Byte counter. This section allows the human eye to quickly establish
 *       where in the byte array a value is located.
 *       The value is the position in the input byte array of the byte printed 
 *       immediately after the colon character on the same line.</li>
 *   <li>Hex representation separated by spaces.</li>
 *   <li>Printable characters enclosed in '|'. This is a textual
 *       representation of the bytes where characters which are not 
 *       not part of the ASCII-127 character set or a non-printable
 *       are represented by the dot character.</li>
 * </ol>
 * 
 * <p>
 * Parameters explanation:
* <ul>
 *   <li>{@code bytesPerLine}. The number of bytes represented on each line 
 *       of output. Typical values are {@code 10} (produces output 50 characters wide
 *       less indention) and {@code 16} (produces output 74 characters wide
 *       less indention). The example above use {@code 16}.</li>
 *   <li>{@code indent}. Number of space characters to prefix each line with.
 *       Indention is typically preferable in logging scenario because it allows a log file consumer
 *       to realize that the lines belong together a single log event.</li>
 * </ul>
 * 
 */
public class HexDump {
    
    private HexDump() {
    }

    /**
     * Dump hex output and output into a PrintStream. This is for example useful for
     * printing the hex dump to {@link java.lang.System#out}.
     *
     * @param input input bytes to be pretty printed
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param bytesPerLine number of bytes to dump on each line of output (typically 10 or 16)
     * @param indent number of space characters to prefix each line of output with
     * @param ps where the output goes to
     */
    public static void prettyPrint(byte[] input, Hex.HexCase caseType, int bytesPerLine, int indent, PrintStream ps) {       
        prettyPrint0(input, caseType, bytesPerLine, indent, (Integer lineNo, String lineStr) -> {
            ps.println(lineStr);
        });
    }


    /**
     * Dump hex output and output into a PrintStream. This is for example useful for
     * printing the hex dump to {@link java.lang.System#out}.
     * 
     * <p>
     * This method use recommended defaults:
     * <ul>
     *    <li> {@code caseType = UPPER}</li>
     *    <li> {@code bytesPerLine = 16}</li>
     *    <li> {@code indent = 4}</li>
     * </ul>
     *
     * @see #prettyPrint(byte[], net.lbruun.hexutils.Hex.HexCase, int, int, java.io.PrintStream) 
     * @param input input bytes to be pretty printed
     * @param ps where the output goes to
     */
    public static void prettyPrint(byte[] input, PrintStream ps) {
        prettyPrint(input, Hex.HexCase.UPPER, 16, 4, ps);
    }


    /**
     * Dump hex output and return result as a String array where each string represents
     * a line of output.
     *
     * @param input input bytes to be pretty printed
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param bytesPerLine number of bytes to dump on each line of output (typically 10 or 16)
     * @param indent number of space characters to prefix each line of output with
     * @return lines of output
     */
    public static String[] prettyPrint(byte[] input, Hex.HexCase caseType, int bytesPerLine, int indent) {
        String[] lines = new String[getNoOfLines(input.length, bytesPerLine)];
        prettyPrint0(input, caseType, bytesPerLine, indent, (Integer lineNo, String lineStr) -> {
            lines[lineNo] = lineStr;
        });
        return lines;
    }

    /**
     * Dump hex output and return result as a String array where each string represents
     * a line of output.
     *
     * <p>
     * This method use recommended defaults:
     * <ul>
     *    <li> {@code caseType = UPPER}</li>
     *    <li> {@code bytesPerLine = 16}</li>
     *    <li> {@code indent = 4}</li>
     * </ul>
     *
     * @param input input bytes to be pretty printed
     * @return lines of output
     */
    public static String[] prettyPrint(byte[] input) {
        return prettyPrint(input, Hex.HexCase.UPPER, 16, 4);
    }

    /**
     * Dump hex output and return result as a String.
     *
     * @see #prettyPrintStr(byte[], net.lbruun.hexutils.Hex.HexCase, int, int) 
     * @param input input bytes to be pretty printed
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param bytesPerLine number of bytes to dump on each line of output (typically 10 or 16)
     * @param indent number of space characters to prefix each line of output with
     * @param lineSeparator the string to use to separate lines
     * @return lines of output
     */
    public static String prettyPrintStr(byte[] input, Hex.HexCase caseType, int bytesPerLine, int indent, String lineSeparator) {
        final StringBuilder sb = new StringBuilder();
        prettyPrint0(input, caseType, bytesPerLine, indent, (Integer lineNo, String lineStr) -> {
            sb.append(lineStr);
            sb.append(lineSeparator);
        });
        return sb.toString();
    }

 
    
    /**
     * Dump hex output and return result as a String. {@link java.lang.System#lineSeparator()} 
     * is used to as line separator.
     *
     * @see #prettyPrintStr(byte[], net.lbruun.hexutils.Hex.HexCase, int, int) 
     * @param input input bytes to be pretty printed
     * @param caseType if the hexadecimal representation should be in upper or lower case
     * @param bytesPerLine number of bytes to dump on each line of output (typically 10 or 16)
     * @param indent number of space characters to prefix each line of output with
     * @return lines of output
     */
    public static String prettyPrintStr(byte[] input, Hex.HexCase caseType, int bytesPerLine, int indent) {
        return prettyPrintStr(input, caseType, bytesPerLine, indent, System.lineSeparator());
    }


    /**
     * Dump hex output and return result as a String. {@link java.lang.System#lineSeparator()} 
     * is used as line separator.
     *
     * <p>
     * This method use recommended defaults:
     * <ul>
     *    <li> {@code caseType = UPPER}</li>
     *    <li> {@code bytesPerLine = 16}</li>
     *    <li> {@code indent = 4}</li>
     *    <li> {@code lineSeparator = }{@link java.lang.System#lineSeparator()}</li>
     * </ul>
     * @see #prettyPrintStr(byte[], net.lbruun.hexutils.Hex.HexCase, int, int) 
     * @param input input bytes to be pretty printed
     * @return lines of output
     */
    public static String prettyPrintStr(byte[] input) {
        return prettyPrintStr(input, Hex.HexCase.UPPER, 16, 4, System.lineSeparator());
    }

    
    /**
     * <pre>
     * 0000 : BF A4 3B 16 1C 88 5C 56 11 78 41 62 72 61 63 61  |..;...\V.xAbraca|
     * 0016 : 64 61 62 72 61 1A 06 94 80 CA 1A 38 42 ED 58 25  |dabra......8B.X%|
     * 0032 : 92 03 AD BF 7F 60 F4 73 AD 55 B0 A9 5C C4 86 B4  |.....`.s.U..\...|
     * 0048 : 5B 11 62 55 B3 BD C4 EA C3 EA 4D 66 2A 02 1C 6C  |[.bU......Mf*..l|
     * 0064 : 0E 84 B1 F8 D8 05 91 51 CE A3 9E 46              |.......Q...F    |
     * </pre>
     * @param input
     * @param caseType
     * @param bytesPerLine
     * @param ps 
     */
    private static void prettyPrint0(byte[] input, Hex.HexCase caseType, int bytesPerLine, int indent, BiConsumer<Integer, String> stringConsumer) {
        Objects.requireNonNull(input, "input byte array must not be null");
        if (bytesPerLine < 1) {
            throw new IllegalArgumentException("bytesPerLine must be > 0");
        }
        if (indent < 0) {
            throw new IllegalArgumentException("indent must be >= 0");
        }
        

        // No of digits in the left-hand counter area
        int counterDigits =  getCounterDigits(input.length);
        int noOfLines = getNoOfLines(input.length, bytesPerLine);
        
        // charsPerLine is solely used to right-size StringBuilder's internal buffer
        int charsPerLine =
                + indent
                + counterDigits 
                + 3
                + (bytesPerLine *2) + (bytesPerLine -1)
                + 3
                + bytesPerLine
                + 1;
        String counterFormatStr = "%0" + counterDigits + "d : ";
        
        for (int lineNo = 0; lineNo < noOfLines; lineNo++) {
            String hexDumpLine = hexDumpLine(input, caseType, bytesPerLine, indent, charsPerLine, lineNo, counterFormatStr);
            stringConsumer.accept(lineNo, hexDumpLine);;
        }
    }
    
    

    
    
    private static String hexDumpLine(byte[] input, Hex.HexCase caseType, int bytesPerLine, int indent, int charsPerLine, int lineNo, String counterFormatStr) {
        StringBuilder sb = new StringBuilder(charsPerLine);
        for (int x = 0; x < indent; x++) {
            sb.append(' ');
        }
        sb.append(String.format(counterFormatStr, (lineNo * bytesPerLine)));
        StringBuilder realChars = new StringBuilder(5 + bytesPerLine);
        realChars.append("  |");
        for (int i = 0; i < bytesPerLine; i++) {
            // arrayPos is the byte number in the 'input' array which
            // is currently being processed
            int arrayPos = (lineNo * bytesPerLine) + i;
            char[] charPair;
            if (arrayPos >= input.length) {
                charPair = new char[]{' ', ' '};
                realChars.append(' ');
            } else {
                charPair = Hex.byteToHex(input[arrayPos], caseType);
                realChars.append(printable(input[arrayPos]));
            }
            sb.append(charPair[0]);
            sb.append(charPair[1]);
            if (i < (bytesPerLine - 1)) {
                sb.append(' ');
            }
        }
        realChars.append('|');
        sb.append(realChars.toString());
        return sb.toString();
    }



    // Calculates the number of digits in the "counter area".
    // This is based on the total number of bytes in the input.
    // For example if total number of bytes is 4216723125 then we
    // need a counter area with 10 digits to represent this.
    private static int getCounterDigits(int inputLen) {
        return Math.max(countDigit(inputLen), 4);
    }

    // Calculates number of lines needed to represent the hex dump
    private static int getNoOfLines(int inputLen, int bytesPerLine) {
        return (inputLen % bytesPerLine == 0)
                ? (inputLen / bytesPerLine)
                : (inputLen / bytesPerLine) + 1;
    }

    
    // Gets printable ASCII-127 char or '.' char
    private static char printable(byte b) {
        char c = (char) b;
        if (c >= ' ' && c <= '~') {
            return c;
        }
        return '.';
    }
    
    // No of digits in input x
    private static int countDigit(int x) {
        return (int) Math.floor(Math.log10(x) + 1);
    }

}
