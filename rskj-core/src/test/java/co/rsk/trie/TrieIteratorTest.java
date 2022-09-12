/*
 * This file is part of RskJ
 * Copyright (C) 2019 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.trie;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.*;
import java.util.stream.Stream;

import static org.bouncycastle.util.encoders.Hex.decode;
import static org.hamcrest.Matchers.*;

class TrieIteratorTest {

    @Test
    void testIterationElement() {
        byte[] expandedKey = new byte[]{0,1,1,0,1,1,0,0,0,1};

        IterationElement ie = new IterationElement(
                new TrieKeySlice(expandedKey,1,7),new Trie());
        String dump = ie.toString();
        Assertions.assertEquals("110110",dump);
    }

    // TODO:I check if this output is fine

    @ParameterizedTest(name = "{index}: {0}")
    @ArgumentsSource(TrieArgumentsProvider.class)
    void testIterator(Iterator<IterationElement> iterator, byte[] expectedValues) {
        int nodeCount = 0;
        while (iterator.hasNext()) {
            byte[] value = iterator.next().getNode().getValue();
            MatcherAssert.assertThat(value.length, is(1));
            MatcherAssert.assertThat("Unexpected value at [%d]\n", value, is(new byte[] { expectedValues[nodeCount] }));
            nodeCount++;
        }
        MatcherAssert.assertThat(nodeCount, is(expectedValues.length));
    }

    /**
     * @return the following tree
     *
     *       6
     *      / \
     *     /   \
     *    /     7
     *   2       \
     *  / \       \
     * 1   \       8
     *      4     /
     *     / \   9
     *    3   5
     */
    private static Trie buildTestTrie() {
        Trie trie = new Trie();
        trie = trie.put(decode("0a"), new byte[] { 0x06 });
        trie = trie.put(decode("0a00"), new byte[] { 0x02 });
        trie = trie.put(decode("0a80"), new byte[] { 0x07 });
        trie = trie.put(decode("0a0000"), new byte[] { 0x01 });
        trie = trie.put(decode("0a0080"), new byte[] { 0x04 });
        trie = trie.put(decode("0a008000"), new byte[] { 0x03 });
        trie = trie.put(decode("0a008080"), new byte[] { 0x05 });
        trie = trie.put(decode("0a8080"), new byte[] { 0x08 });
        trie = trie.put(decode("0a808000"), new byte[] { 0x09 });
        return trie;
    }

    private static class TrieArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of(buildTestTrie().getInOrderIterator(), new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x09, 0x08}),
                    Arguments.of(buildTestTrie().getPreOrderIterator(), new byte[] {0x06, 0x02, 0x01, 0x04, 0x03, 0x05, 0x07, 0x08, 0x09}),
                    Arguments.of(buildTestTrie().getPostOrderIterator(), new byte[] {0x01, 0x03, 0x05, 0x04, 0x02, 0x09, 0x08, 0x07, 0x06})
            );
        }
    }
}
