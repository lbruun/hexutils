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

import java.util.regex.Pattern;

/**
 * Utilities for converting to/from harmless hex characters.
 * 
 * <p>
 * Why harmless hex characters? <br>
 * With the English language it is possible out of the 
 * hex characters ({@code 0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f}) to form words that might
 * be bad/funny/peculiar/offensive. The idea with harmless hex characters
 * is to replace vowels and vowel-like hex characters with consonants, the theory
 * being that in the English language it is near-impossible to form words 
 * solely out of consonants.
 * 
 * <p>
 * The following replacements are done:
 * <pre>
 * | ---- | ----------- |
 * | char | replaced by |
 * | ---- | ----------- |
 * |  0   |    g        |
 * |  1   |    h        |
 * |  3   |    k        |
 * |  a   |    m        |
 * |  e   |    t        |
 * | ---- | ----------- |
 * </pre>
 * thus as an example, the classic hex encoded string of {@code 0ffaeb} would become
 * {@code gffmtb} in harmless hex.
 * 
 * <p>
 * This docucters {@code [2,4-9,b,c,d,f,g,h,k,m,t]} as <i>harmless hex chars</i>.
 * Harmless hex exists only in the lower-case variant.
 * 
 * <p>
 * The concept of harmless hex characters exists in Google's Kubernetes
 * from where the character mapping in this class originates. 
 * (<a href="https://github.com/kubernetes/kubernetes/blob/master/staging/src/k8s.io/kubectl/pkg/util/hash/hash.go" target="_blank">source code link</a>,
 * function {@code encodeHash})
 * 
 */
public class HexHarmless {
    
    /**
     * Regular expression character set with the characters that are 
     * allowed in harmless hex representation.
     */ 
    public static final String HARMLESS_HEX_REGEXP_CHARCLASS = "[24-9bcdfghkmt]";
    
    /**
     * Characters allowed in harmless hex representation.
     */
    public static final char[] HARMLESS_HEX_CHARS = 
            new char[]{'g','h','2','k','4','5','6','7','8','9','m','b','c','d','t','f'};
    
    private HexHarmless() {
    }
     
    /**
     * Returns classic hex char equivalent of the input harmless hex char. The input is expected
     * to be a lower-case harmless hex char.
     * 
     * @throws IllegalArgumentException if input is not a lower-case harmless hex char
     * @see #convertToHarmless(char) 
     * @param c hex char
     * @return harmless equivalent
     */
    public static char convertFromHarmless(char c) {
        Pattern x;
        switch(c) {
            case 'g':
                return '0';
	    case 'h':
		return '1';
            case 'k':
                return '3';
            case 'm':
                return 'a';
            case 't':
                return 'e';
            case '2':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'b':
            case 'c':
            case 'd':
            case 'f':
                return c;
            default:
                throw new IllegalArgumentException("Character '" + c + "' is illegal. Only characters [2,4-9,b,c,d,f,g,h,k.m,t] are allowed.");
        }
    } 
    
    /**
     * Returns harmless equivalent of the input hex char. The input is expected
     * to be a lower-case classic hex char.
     * 
     * @throws IllegalArgumentException if input is not a lower-case classic hex char
     * @see #convertFromHarmless(char) 
     * @param c harmless hex char
     * @return hex char equivalent
     */
    public static char convertToHarmless(char c) {
        switch(c) {
            case '0':
                return 'g';
	    case '1':
		return 'h';
	    case '2':
		return c;
            case '3':
                return 'k';
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return c;
            case 'a':
                return 'm';
            case 'b':
            case 'c':
            case 'd':
                return c;
            case 'e':
                return 't';
            case 'f':
                return c;
            default:
                throw new IllegalArgumentException("Character '" + c + "' is illegal. Only characters [0-9] and [a-f] are allowed.");
        }
    }
    
    /**
     * Replaces classic hex characters in the input array with their harmless
     * equivalents. Note that the input array is altered by this method.
     * 
     * @throws IllegalArgumentException if input array contains characters 
     *    which are not lower-case classic hex char.
     * @param chars
     * @return 
     */
    public static char[] convertToHarmless(char[] chars) {
        if (chars == null) {
            return chars;
        }
        for(int i = 0; i < chars.length; i++) {
            chars[i] = HexHarmless.convertToHarmless(chars[i]);
        }
        return chars;
    }
    
    /**
     * Replaces harmless hex characters in the input array with their classic
     * hex char equivalents. Note that the input array is altered by this method.
     * 
     * @throws IllegalArgumentException if input array contains characters 
     *    which are not lower-case harmless hex char.
     * @param chars
     * @return 
     */
    public static char[] convertFromHarmless(char[] chars) {
        if (chars == null) {
            return chars;
        }
        for(int i = 0; i < chars.length; i++) {
            chars[i] = HexHarmless.convertFromHarmless(chars[i]);
        }
        return chars;
    }    
    
    
    
    
  /**
     * Converts a single byte to its harmless hexadecimal representation.
     * 
     * <p>
     * See {@link HexHarmless}.
     * 
     * @param b input
     * @return char array of length 2
     */
    public static char[] byteToHexHarmless(byte b) {
        return convertToHarmless(Hex.byteToHex(b, Hex.HexCase.LOWER));
    }
    
    /**
     * Converts a byte array to its harmless hexadecimal representation.
     * 
     * @param bytes input
     * @return harmless hexadecimal representation
     */
    public static char[] bytesToHexHarmless(byte[] bytes) {
        return convertToHarmless(Hex.bytesToHex(bytes, Hex.HexCase.LOWER, null));
    }


    /**
     * Converts a byte array to its harmless hexadecimal representation.
     * 
     * @param bytes input
     * @return harmless hexadecimal representation
     */
    public static String bytesToHexStrHarmless(byte[] bytes) {
        return new String(bytesToHexHarmless(bytes));
    }

    /**
     * Converts a harmless hex character pair into a byte. For example the pair {@code 't'}
     * and {@code 'k'} returns the byte value {@code 227}.
       * 
     * <p>
     * The input values must be only the characters {@code [4-9,b,c,d,f,m,t]}.
     *    
     * @throws IllegalArgumentException if either c1 or c2 is an illegal character.
     * @param c1 first character of pair
     * @param c2 second character of pair
     * @return 
     */
    public static byte hexCharHarmlessToByte(char c1, char c2) {
        return Hex.hexCharToByte(HexHarmless.convertFromHarmless(c1), HexHarmless.convertFromHarmless(c2));
    }

    /**
     * Converts a character array consisting of harmless hexadecimal 
     * characters into a byte array.
     * 
     * <p>
     * The input must be of equal-length and consist only of lower-case harmless
     * hex chars.
     * 
     * @throws IllegalArgumentException if input array contains characters 
     *    which are not lower-case harmless hex char.
     * @param chars input
     * @return byte array or {@code null} if input is {@code null}.
     */
    public static byte[] hexStrHarmlessToBytes(char[] chars) {
        if (chars == null) {
            return null;
        }
        return Hex.hexStrToBytes(convertFromHarmless(chars));
    }

    /**
     * Converts a character sequence (a string) consisting of harmless hexadecimal 
     * characters into a byte array.
     * 
     * <p>
     * The input must be of equal-length and consist only of lower-case harmless
     * hex chars.
     * 
     * @throws IllegalArgumentException if input sequence contains characters 
     *    which are not lower-case harmless hex char.
     * @param s input
     * @return byte array or {@code null} if input is {@code null}.
     */
    public static byte[] hexStrHarmlessToBytes(CharSequence s) {
        if (s == null) {
            return null;
        }
        char[] chars = new char[s.length()];
        for(int i = 0; i < s.length(); i++) {
            chars[i] = HexHarmless.convertFromHarmless(s.charAt(i));
        }
        return Hex.hexStrToBytes(chars);
    }
}
