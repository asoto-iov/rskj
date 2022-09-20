/*
 * This file is part of RskJ
 * Copyright (C) 2022 RSK Labs Ltd.
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

package co.rsk.db;

import com.google.common.annotations.VisibleForTesting;
import org.ethereum.datasource.TransientMap;
import org.ethereum.db.IndexedBlockStore;
import org.mapdb.DB;

import java.util.List;
import java.util.Map;

public class MapDBBlocksIndexReadonly extends MapDBBlocksIndex {

    public static MapDBBlocksIndexReadonly create(DB indexDB) {
        return new MapDBBlocksIndexReadonly(indexDB);
    }

    @VisibleForTesting
    static MapDBBlocksIndexReadonly createForTesting(DB indexDB, Map<Long, List<IndexedBlockStore.BlockInfo>> index, Map<String, byte[]> metadata) {
        return new MapDBBlocksIndexReadonly(indexDB, index, metadata);
    }

    private MapDBBlocksIndexReadonly(DB indexDB) {
        super(indexDB);
    }

    private MapDBBlocksIndexReadonly(DB indexDB, Map<Long, List<IndexedBlockStore.BlockInfo>> index, Map<String, byte[]> metadata) {
        super(indexDB, index, metadata);
    }

    @Override
    protected <K, V> Map<K, V> wrapIndex(Map<K, V> base) {
        return TransientMap.transientMap(base);
    }

    @Override
    public void flush() {
        // a read-only mapDB cannot be committed, even if there is nothing to commit
    }

}
