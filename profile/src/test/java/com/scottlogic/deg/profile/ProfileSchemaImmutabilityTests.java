/*
 * Copyright 2019 Scott Logic Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scottlogic.deg.profile;

import com.scottlogic.deg.profile.dto.SupportedVersionsGetter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfileSchemaImmutabilityTests {

    private static class VersionHash {
        private final String version;
        private final String hash;

        public VersionHash(String version, String hash) {
            this.version = version;
            this.hash = hash;
        }

        public String version() {
            return version;
        }
    }

    private static Set<VersionHash> versionToHash() {
        Set<VersionHash> versionToHash = new HashSet<>();
        // DO NOT MODIFY EXISTING HASHES! ONLY ADD!
        // The new checksum hash can be found by running the shell command sha256sum on the respective schema file
        // example: sha256sum profile/src/main/resources/profileschema/0.1/datahelix.schema.json
        // Ensure you run this on a unix based machine.
        // Alternatively, add the new version to the map, and the test will give you the hash it should be.
        versionToHash.add(new VersionHash(
            "0.1",
            "575c572e9d00d69b5775cf50f01fc79d8cf7babcb6eb2ac51b1a9572d490487c"));
        return versionToHash;
    }

    private static class VersionHashesProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return versionToHash().stream().map(Arguments::of);
        }
    }

    // If this test fails, you have added a new version of the schema or modified an existing one.
    // You need to include the hash of the new schema in the map in this file.
    // Do not modify existing hashes.
    @Test
    public void hashesMustBeAddedToThisFileForAnyNewSchemas() {
        Set<String> existingSchemas = new HashSet<>(new SupportedVersionsGetter().getSupportedSchemaVersions());
        assertEquals(
            existingSchemas,
            versionToHash().stream().map(VersionHash::version).collect(Collectors.toSet()),
            "At least one version is either missing or erroneously present in this test's list of checksums. " +
            "The new checksum must be added to the map of checksum hashes in this test file.");
    }

    @ParameterizedTest
    @ArgumentsSource(VersionHashesProvider.class)
    public void oldSchemasMustNotBeModified(VersionHash wrapper) throws NoSuchAlgorithmException {
        String location = "profileschema/" + wrapper.version + "/datahelix.schema.json";
        byte[] bytes = normaliseLineEndings(readClassResourceAsBytes(location));

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encoded = digest.digest(bytes);

        assertEquals(
            wrapper.hash,
            bytesToHex(encoded),
            "The expected hash for version %s does not match the hash supplied." +
            "If you weren't testing for a new hash, you probably modified an existing schema. Do not do this. " +
                "Create a new schema version instead.");
    }

    private static byte[] normaliseLineEndings(byte[] bytes) {
        final String encoding = "UTF-8";
        try {
            return new String(bytes, encoding).replaceAll("\\r\\n?", "\n").getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte each : bytes) {
            result.append(byteToHex(each));
        }
        return result.toString();
    }

    private static String byteToHex(byte in) {
        final char[] chars = "0123456789abcdef".toCharArray();
        StringBuilder output = new StringBuilder();
        int quotient = in < 0 ? 256 + in : in; // Account for twos complement

        for (int i = 0; i < 2; i++) {
            int remainder = quotient % 16;
            output.append(chars[remainder]);
            quotient = quotient / 16;
        }

        return output.reverse().toString();
    }

    private byte[] readClassResourceAsBytes(String location) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream schema = classLoader.getResourceAsStream(location);

        try {
            return IOUtils.toByteArray(schema);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}