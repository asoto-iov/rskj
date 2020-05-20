/*
 * This file is part of RskJ
 * Copyright (C) 2020 RSK Labs Ltd.
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

package co.rsk.net.light.message;

import co.rsk.net.light.LightClientMessageCodes;
import co.rsk.net.light.LightClientMessageVisitor;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;

import static org.ethereum.util.ByteUtil.toHexString;

public class GetBlockHeadersMessage extends LightClientMessage {

    private final long id;
    private final byte[] blockHash;
    private int count;

    public GetBlockHeadersMessage(long id, byte[] blockHash, int count) {
        this.id = id;
        this.blockHash = blockHash.clone();
        this.count = count;
        this.code = LightClientMessageCodes.GET_BLOCK_HEADER.asByte();
    }

    public GetBlockHeadersMessage(byte[] encoded) {
        RLPList paramsList = (RLPList) RLP.decode2(encoded).get(0);
        byte[] rlpId = paramsList.get(0).getRLPData();
        id = rlpId == null ? 0 : BigIntegers.fromUnsignedByteArray(rlpId).longValue();
        blockHash = paramsList.get(1).getRLPData();
        byte[] rlpCount = paramsList.get(2).getRLPData();
        count = rlpCount == null? 0 : BigIntegers.fromUnsignedByteArray(rlpCount).intValue();
        this.code = LightClientMessageCodes.GET_BLOCK_HEADER.asByte();
    }


    public long getId() {
        return this.id;
    }

    public byte[] getBlockHash() {
        return blockHash.clone();
    }


    @Override
    public byte[] getEncoded() {
        byte[] rlpId = RLP.encodeBigInteger(BigInteger.valueOf(getId()));
        byte[] rlpHash = RLP.encodeElement(getBlockHash());
        byte[] rlpCount = RLP.encodeBigInteger(BigInteger.valueOf(getCount()));
        return RLP.encodeList(rlpId, rlpHash, rlpCount);
    }

    @Override
    public Class<?> getAnswerMessage() {
        return BlockHeadersMessage.class;
    }


    @Override
    public String toString() {
        return "GetBlockHeaderMessage{" +
                "\nid= " + getId() +
                "\nblockHash= " + toHexString(getBlockHash()) +
                "\n}";
    }

    @Override
    public void accept(LightClientMessageVisitor v) {
        v.apply(this);
    }

    public int getCount() {
        return count;
    }
}