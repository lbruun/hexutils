# Hex Utils
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.lbruun/hexutils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.lbruun/hexutils)
[![javadoc](https://javadoc.io/badge2/net.lbruun/hexutils/javadoc.svg)](https://javadoc.io/doc/net.lbruun/hexutils) 

Minimal no-dep library dedicated to convert bytes to their hex representation and vice-versa.



### Features

* Fast
* Lossless and consistent conversion: the hex string produced can be used to convert back to the original byte array.
* Left-padded conversion: each byte is always represented by exactly 2 hex chars (for example, the Linux newline character is `0a`, not `a`).
* Supports delimiters. Typically used when representing certificate thumbprints or MAC addresses.
* Support for both lower-case and upper-case.
* Streaming support. Convert very large amount of data to/from hex without memory exhaustion.
* Support for harmless hex: A hex presentation which minimizes the chance of accidentially forming bad words out of the classic hex chars.
* Hex dumps. Like you would use them for wire dumps, etc.
* Can be used with Java 8 onwards.

### Alternatives

- JDK 17 finally has [HexFormat class](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/HexFormat.html). It solves many - but not all - of the issues which this library also solves. Preferable if you are using JDK 17 or later. Hex Dumps is probably the only thing it cannot do.
- String [format](https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html). You can use `String.format("%02x", b)` in order to convert a single byte into left-padded lower-case hex. The method is unfortunately insanely slow. 
- `BigInteger`. You can use `(new BigInteger(bytes)).toString(16)` to convert a byte array into hex. Does left-padding correctly so that `A\nBC` would become `410a4243`. I suspect that creating a BigInteger object for the sole purpose of converting to hex has a little bit of overhead but most likely this overhead is ignorable.
- `Integer.toHexString()` / `Integer.toString(x, 16)`. It doesn't do left-padding and will not work on a byte-array.
- `DatatypeConverter` in JAXB. See methods `parseHexBinary()` and `printHexBinary()`. The methods parse and produce left-padded hex strings. Unfortunately, JAXB is no longer part of the JDK. 
- General purpose libraries like Apache Commons and Google's Guava has some of the features of this library.


### Usage

Library is available on Maven Central.

```xml
<dependency>
    <groupId>net.lbruun</groupId>
    <artifactId>hexutils</artifactId>
    <version>  --LATEST--  </version>
</dependency>
```



### Documentation

For in-depth documentation see [JavaDoc](https://javadoc.io/doc/net.lbruun/hexutils).


#### General

From byte array to hex:
```java
byte[] macAddress = getMacAddress();
String macAddressHex = Hex.bytesToHexStr(macAddress, HexCase.UPPER, "-");
// For example:  58-E0-85-A9-BD-0D
```

From hex to byte array:
```java
byte[] macAddress = Hex.hexStrToBytes(macAddressHex, "-");
```


#### Hex Dumps

When representing wire data it is often desirable to represent sent or received data in a format similar to this:

```text
 0000 : BF A4 3B 16 1C 88 5C 56 11 78 41 62 72 61 63 61  |..;...\V.xAbraca|
 0016 : 64 61 62 72 61 1A 06 94 80 CA 1A 38 42 ED 58 25  |dabra......8B.X%|
 0032 : 92 03 AD BF 7F 60 F4 73 AD 55 B0 A9 5C C4 86 B4  |.....`.s.U..\...|
 0048 : 5B 11 62 55 B3 BD C4 EA C3 EA 4D 66 2A 02 1C 6C  |[.bU......Mf*..l|
 0064 : 0E 84 B1 F8 D8 05 91 51 CE A3 9E 46              |.......Q...F    |
```
This type of output can be produced with the `HexDump` class.

#### Harmless Hex

With the classic hex characters it is possible to create words that may be inappropriate.
Harmless Hex is used in Google's Kubernetes and possibly elsewhere too.
The idea with harmless hex characters is to replace vowels and vowel-like digits with consonants, the theory being that in the English language it is near-impossible to form words solely out of consonants.

Harmless hex can be encoded and decoded with the `HexHarmless` class.
The following replacements are done:

```text
 | ---- | ----------- |
 | char | replaced by |
 | ---- | ----------- |
 |  0   |    g        |
 |  1   |    h        |
 |  3   |    k        |
 |  a   |    m        |
 |  e   |    t        |
 | ---- | ----------- |
 ```

As an example, the classic hex encoded string of `0ffaeb` would become `gffmtb` in harmless hex. 

Harmless hex mainly has its place for permanent identifiers.
