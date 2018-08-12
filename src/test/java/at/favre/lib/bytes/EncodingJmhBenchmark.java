/*
 * Copyright 2018 Patrick Favre-Bulle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package at.favre.lib.bytes;

import org.openjdk.jmh.annotations.*;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
# JMH version: 1.21
# VM version: JDK 1.8.0_172, Java HotSpot(TM) 64-Bit Server VM, 25.172-b11
# i7 7700K / 24G

Benchmark                               (byteLength)   Mode  Cnt         Score        Error  Units
EncodingJmhBenchmark.encodeBase64Guava             1  thrpt    4  10361634,745 ± 152739,710  ops/s
EncodingJmhBenchmark.encodeBase64Guava            16  thrpt    4   4360485,804 ±  44729,417  ops/s
EncodingJmhBenchmark.encodeBase64Guava           128  thrpt    4    790407,010 ±   8095,476  ops/s
EncodingJmhBenchmark.encodeBase64Guava           512  thrpt    4    192448,674 ±   2196,035  ops/s
EncodingJmhBenchmark.encodeBase64Guava       1000000  thrpt    4       102,780 ±      2,949  ops/s
EncodingJmhBenchmark.encodeBase64Okio              1  thrpt    4  12658987,399 ± 361955,366  ops/s
EncodingJmhBenchmark.encodeBase64Okio             16  thrpt    4   7059404,777 ± 293665,348  ops/s
EncodingJmhBenchmark.encodeBase64Okio            128  thrpt    4   1749131,031 ±  85915,325  ops/s
EncodingJmhBenchmark.encodeBase64Okio            512  thrpt    4    239764,488 ±   6204,540  ops/s
EncodingJmhBenchmark.encodeBase64Okio        1000000  thrpt    4       107,868 ±      0,569  ops/s
 */

@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 4, time = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class EncodingJmhBenchmark {

    @Param({"1", "16", "128", "512", "1000000"})
    private int byteLength;
    private Map<Integer, Bytes[]> rndMap;

    private BinaryToTextEncoding.EncoderDecoder base64Okio;
    private BinaryToTextEncoding.EncoderDecoder base64Guava;
    private Random random;

    @Setup(Level.Trial)
    public void setup() {
        random = new Random();
        base64Okio = new BinaryToTextEncoding.Base64Encoding();
        base64Guava = new BaseEncoding(new BaseEncoding.Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()), BaseEncoding.BASE32_RFC4848_PADDING);
        rndMap = new HashMap<>();
        int[] lengths = new int[]{1, 16, 128, 512, 1000000};
        for (int length : lengths) {
            int count = 10;
            rndMap.put(length, new Bytes[count]);
            for (int i = 0; i < count; i++) {
                rndMap.get(length)[i] = Bytes.random(length);
            }
        }
    }

    @Benchmark
    public byte[] encodeBase64Okio() {
        return encodeDecode(base64Okio);
    }

    @Benchmark
    public byte[] encodeBase64Guava() {
        return encodeDecode(base64Guava);
    }

    private byte[] encodeDecode(BinaryToTextEncoding.EncoderDecoder encoder) {
        Bytes[] bytes = rndMap.get(byteLength);
        int rndNum = random.nextInt(bytes.length);

        String encoded = encoder.encode(bytes[rndNum].array(), ByteOrder.BIG_ENDIAN);
        return encoder.decode(encoded);
    }
}
