/*
 * This file is part of RskJ
 * Copyright (C) 2018 RSK Labs Ltd.
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

package co.rsk.rpc;

import co.rsk.core.bc.BlockChainImpl;
import co.rsk.core.bc.BlockResult;
import co.rsk.core.bc.MiningMainchainView;
import co.rsk.mine.BlockToMineBuilder;
import co.rsk.mine.MinerServer;
import org.ethereum.TestUtils;
import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Blockchain;
import org.ethereum.rpc.exception.RskJsonRpcRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

class ExecutionFoundBlockRetrieverTest {

    private MiningMainchainView miningMainchainView;
    private Blockchain blockchain;
    private MinerServer minerServer;
    private BlockToMineBuilder builder;
    private ExecutionBlockRetriever retriever;
    private final static int INVALID_PARAM_ERROR_CODE = -32602;

    @BeforeEach
    void setUp() {
        blockchain = mock(BlockChainImpl.class);
        miningMainchainView = mock(MiningMainchainView.class);
        minerServer = mock(MinerServer.class);
        builder = mock(BlockToMineBuilder.class);
        retriever = new ExecutionBlockRetriever(miningMainchainView, blockchain, minerServer, builder);
    }

    @Test
    void getLatest() {
        Block latest = mock(Block.class);
        when(blockchain.getBestBlock())
                .thenReturn(latest);

        assertThat(retriever.retrieveExecutionBlock("latest").getBlock(), is(latest));
    }

    @Test
    void getLatestIsUpToDate() {
        Block latest1 = mock(Block.class);
        Block latest2 = mock(Block.class);
        when(blockchain.getBestBlock())
                .thenReturn(latest1)
                .thenReturn(latest2);

        assertThat(retriever.retrieveExecutionBlock("latest").getBlock(), is(latest1));
        assertThat(retriever.retrieveExecutionBlock("latest").getBlock(), is(latest2));
    }

    @Test
    void getPendingUsesMinerServerLatestBlock() {
        Block latest = mock(Block.class);
        when(minerServer.getLatestBlock())
                .thenReturn(Optional.of(latest));

        assertThat(retriever.retrieveExecutionBlock("pending").getBlock(), is(latest));
    }

    @Test
    void getPendingUsesMinerServerAndIsUpToDate() {
        Block latest1 = mock(Block.class);
        Block latest2 = mock(Block.class);
        when(minerServer.getLatestBlock())
                .thenReturn(Optional.of(latest1))
                .thenReturn(Optional.of(latest2));

        assertThat(retriever.retrieveExecutionBlock("pending").getBlock(), is(latest1));
        assertThat(retriever.retrieveExecutionBlock("pending").getBlock(), is(latest2));
    }

    @Test
    void getPendingBuildsPendingBlockIfMinerServerHasNoWork() {
        when(minerServer.getLatestBlock())
                .thenReturn(Optional.empty());

        BlockHeader bestHeader = mock(BlockHeader.class);
        Block bestBlock = mock(Block.class);
        when(bestBlock.getHeader()).thenReturn(bestHeader);
        when(blockchain.getBestBlock())
                .thenReturn(bestBlock);

        when(miningMainchainView.get())
                .thenReturn(new ArrayList<>(Collections.singleton(bestHeader)));

        Block builtBlock = mock(Block.class);
        BlockResult blockResult = mock(BlockResult.class);
        when(builder.build(new ArrayList<>(Collections.singleton(bestHeader)), null))
                .thenReturn(blockResult);
        when(blockResult.getBlock()).thenReturn(builtBlock);

        assertThat(retriever.retrieveExecutionBlock("pending").getBlock(), is(builtBlock));
    }

    @Test
    void getPendingReturnsCachedBlockIfMinerServerHasNoWork() {
        when(minerServer.getLatestBlock())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        BlockHeader bestHeader = mock(BlockHeader.class);
        Block bestBlock = mock(Block.class);
        when(bestBlock.getHeader()).thenReturn(bestHeader);
        when(blockchain.getBestBlock())
                .thenReturn(bestBlock)
                .thenReturn(bestBlock);

        List<BlockHeader> mainchainHeaders = new ArrayList<>();
        mainchainHeaders.add(bestBlock.getHeader());
        mainchainHeaders.add(bestBlock.getHeader());
        when(miningMainchainView.get())
                .thenReturn(mainchainHeaders);

        BlockResult blockResult = mock(BlockResult.class);
        Block builtBlock = mock(Block.class);
        when(bestBlock.isParentOf(builtBlock))
                .thenReturn(true);
        when(builder.build(mainchainHeaders, null))
                .thenReturn(blockResult);
        when(blockResult.getBlock()).thenReturn(builtBlock);

        assertThat(retriever.retrieveExecutionBlock("pending"), is(blockResult));
        assertThat(retriever.retrieveExecutionBlock("pending"), is(blockResult));
        // TODO(mc): the cache doesn't work properly in getExecutionBlock_workaround.
        //           this is a known bug in version 1.0.1, and should be fixed in master
        verify(builder, times(2)).build(mainchainHeaders, null);
    }

    @Test
    void getPendingDoesntUseCacheIfBestBlockHasChanged() {
        when(minerServer.getLatestBlock())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        BlockHeader bestHeader1 = mock(BlockHeader.class);
        Block bestBlock1 = mock(Block.class);
        when(bestBlock1.getHeader()).thenReturn(bestHeader1);

        BlockHeader bestHeader2 = mock(BlockHeader.class);
        Block bestBlock2 = mock(Block.class);
        when(bestBlock2.getHeader()).thenReturn(bestHeader2);

        when(blockchain.getBestBlock())
                .thenReturn(bestBlock1)
                .thenReturn(bestBlock2);

        when(miningMainchainView.get())
                .thenReturn(new ArrayList<>(Collections.singleton(bestHeader1)))
                .thenReturn(new ArrayList<>(Collections.singleton(bestHeader2)));

        Block builtBlock1 = mock(Block.class);
        when(bestBlock1.isParentOf(builtBlock1)).thenReturn(true);
        BlockResult blockResult1 = mock(BlockResult.class);
        when(blockResult1.getBlock()).thenReturn(builtBlock1);
        when(builder.build(new ArrayList<>(Collections.singleton(bestHeader1)), null)).thenReturn(blockResult1);

        Block builtBlock2 = mock(Block.class);
        when(bestBlock2.isParentOf(builtBlock2)).thenReturn(true);
        BlockResult blockResult2 = mock(BlockResult.class);
        when(blockResult2.getBlock()).thenReturn(builtBlock2);
        when(builder.build(new ArrayList<>(Collections.singleton(bestHeader2)), null)).thenReturn(blockResult2);

        assertThat(retriever.retrieveExecutionBlock("pending").getBlock(), is(builtBlock1));
        assertThat(retriever.retrieveExecutionBlock("pending").getBlock(), is(builtBlock2));
    }

    @Test
    void getByNumberBlockExistsHex() {
        Block myBlock = mock(Block.class);
        when(blockchain.getBlockByNumber(123))
                .thenReturn(myBlock);

        assertThat(retriever.retrieveExecutionBlock("0x7B").getBlock(), is(myBlock));
        assertThat(retriever.retrieveExecutionBlock("0x7b").getBlock(), is(myBlock));
    }

    @Test
    void getByNumberBlockExistsDec() {
        Block myBlock = mock(Block.class);
        when(blockchain.getBlockByNumber(123))
                .thenReturn(myBlock);

        assertThat(retriever.retrieveExecutionBlock("123").getBlock(), is(myBlock));
    }

    @Test
    void getByNumberInvalidBlockNumberHex() {
        when(blockchain.getBlockByNumber(123))
                .thenReturn(null);

        RskJsonRpcRequestException e = TestUtils
                .assertThrows(RskJsonRpcRequestException.class,
                        () -> retriever.retrieveExecutionBlock("0x7B"));
        Assertions.assertEquals(INVALID_PARAM_ERROR_CODE, (int) e.getCode());
    }

    @Test
    void getByNumberInvalidBlockNumberDec() {
        when(blockchain.getBlockByNumber(123))
                .thenReturn(null);
        RskJsonRpcRequestException e = TestUtils
                .assertThrows(RskJsonRpcRequestException.class,
                        () -> retriever.retrieveExecutionBlock("123"));
        Assertions.assertEquals(INVALID_PARAM_ERROR_CODE, (int) e.getCode());
    }

    @Test
    void getByNumberInvalidHex() {
        RskJsonRpcRequestException e = TestUtils
                .assertThrows(RskJsonRpcRequestException.class,
                        () -> retriever.retrieveExecutionBlock("0xzz"));
        Assertions.assertEquals(INVALID_PARAM_ERROR_CODE, (int) e.getCode());

        verify(blockchain, never()).getBlockByNumber(any(long.class));
    }

    @Test
    void getByNumberInvalidDec() {
        RskJsonRpcRequestException e = TestUtils
                .assertThrows(RskJsonRpcRequestException.class,
                        () -> retriever.retrieveExecutionBlock("zz"));
        Assertions.assertEquals(INVALID_PARAM_ERROR_CODE, (int) e.getCode());
        verify(blockchain, never()).getBlockByNumber(any(long.class));
    }

    @Test
    void getOtherThanPendingLatestOrNumberThrows() {
        RskJsonRpcRequestException e = TestUtils
                .assertThrows(RskJsonRpcRequestException.class,
                        () -> retriever.retrieveExecutionBlock("other"));
        Assertions.assertEquals(INVALID_PARAM_ERROR_CODE, (int) e.getCode());
    }
}
