package co.rsk.peg.utils;

import co.rsk.bitcoinj.core.Address;
import co.rsk.bitcoinj.core.BtcECKey;
import co.rsk.bitcoinj.core.BtcTransaction;
import co.rsk.bitcoinj.core.Coin;
import co.rsk.config.BridgeConstants;
import co.rsk.core.RskAddress;
import co.rsk.crypto.Keccak256;
import co.rsk.peg.Bridge;
import co.rsk.peg.DeprecatedMethodCallException;
import co.rsk.peg.Federation;
import org.ethereum.config.blockchain.upgrades.ActivationConfig;
import org.ethereum.config.blockchain.upgrades.ConsensusRule;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.util.RLP;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.ethereum.vm.PrecompiledContracts;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Responsible for logging events triggered by BridgeContract in RLP format.
 *
 * @author kelvin.isievwore
 * @deprecated Methods included in this class are to be used only prior to RSKIP146 activation
 */
@Deprecated
public class BrigeEventLoggerLegacyImpl implements BridgeEventLogger {

    private static final byte[] BRIDGE_CONTRACT_ADDRESS = PrecompiledContracts.BRIDGE_ADDR.getBytes();

    private final BridgeConstants bridgeConstants;
    private final ActivationConfig.ForBlock activations;
    private List<LogInfo> logs;

    public BrigeEventLoggerLegacyImpl(BridgeConstants bridgeConstants, ActivationConfig.ForBlock activations, List<LogInfo> logs) {
        this.bridgeConstants = bridgeConstants;
        this.activations = activations;
        this.logs = logs;
    }

    @Override
    public void logUpdateCollections(Transaction rskTx) {
        if (activations.isActive(ConsensusRule.RSKIP146)) {
            throw new DeprecatedMethodCallException(
                "Calling BrigeEventLoggerLegacyImpl.logUpdateCollections method after RSKIP146 activation"
            );
        }
        this.logs.add(
            new LogInfo(BRIDGE_CONTRACT_ADDRESS,
                Collections.singletonList(Bridge.UPDATE_COLLECTIONS_TOPIC),
                RLP.encodeElement(rskTx.getSender().getBytes())
            )
        );
    }

    @Override
    public void logAddSignature(BtcECKey federatorPublicKey, BtcTransaction btcTx, byte[] rskTxHash) {
        if (activations.isActive(ConsensusRule.RSKIP146)) {
            throw new DeprecatedMethodCallException(
                "Calling BrigeEventLoggerLegacyImpl.logAddSignature method after RSKIP146 activation"
            );
        }
        List<DataWord> topics = Collections.singletonList(Bridge.ADD_SIGNATURE_TOPIC);
        byte[] data = RLP.encodeList(RLP.encodeString(btcTx.getHashAsString()),
            RLP.encodeElement(federatorPublicKey.getPubKeyHash()),
            RLP.encodeElement(rskTxHash));

        this.logs.add(new LogInfo(BRIDGE_CONTRACT_ADDRESS, topics, data));
    }

    @Override
    public void logReleaseBtc(BtcTransaction btcTx, byte[] rskTxHash) {
        if (activations.isActive(ConsensusRule.RSKIP146)) {
            throw new DeprecatedMethodCallException(
                "Calling BrigeEventLoggerLegacyImpl.logReleaseBtc method after RSKIP146 activation"
            );
        }
        List<DataWord> topics = Collections.singletonList(Bridge.RELEASE_BTC_TOPIC);
        byte[] data = RLP.encodeList(RLP.encodeString(btcTx.getHashAsString()), RLP.encodeElement(btcTx.bitcoinSerialize()));

        this.logs.add(new LogInfo(BRIDGE_CONTRACT_ADDRESS, topics, data));
    }

    @Override
    public void logCommitFederation(Block executionBlock, Federation oldFederation, Federation newFederation) {
        if (activations.isActive(ConsensusRule.RSKIP146)) {
            throw new DeprecatedMethodCallException(
                "Calling BrigeEventLoggerLegacyImpl.logCommitFederation method after RSKIP146 activation"
            );
        }
        List<DataWord> topics = Collections.singletonList(Bridge.COMMIT_FEDERATION_TOPIC);

        byte[] oldFedFlatPubKeys = flatKeysAsRlpCollection(oldFederation.getBtcPublicKeys());
        byte[] oldFedData = RLP.encodeList(RLP.encodeElement(oldFederation.getAddress().getHash160()), RLP.encodeList(oldFedFlatPubKeys));

        byte[] newFedFlatPubKeys = flatKeysAsRlpCollection(newFederation.getBtcPublicKeys());
        byte[] newFedData = RLP.encodeList(RLP.encodeElement(newFederation.getAddress().getHash160()), RLP.encodeList(newFedFlatPubKeys));

        long newFedActivationBlockNumber = executionBlock.getNumber() + this.bridgeConstants.getFederationActivationAge();

        byte[] data = RLP.encodeList(oldFedData, newFedData, RLP.encodeString(Long.toString(newFedActivationBlockNumber)));

        this.logs.add(new LogInfo(BRIDGE_CONTRACT_ADDRESS, topics, data));
    }

    @Override
    public void logLockBtc(RskAddress rskReceiver, BtcTransaction btcTx, Address senderBtcAddress, Coin amount) {
        throw new DeprecatedMethodCallException("Calling BrigeEventLoggerLegacyImpl.logLockBtc method after RSKIP146 activation");
    }

    @Override
    public void logPeginBtc(RskAddress rskReceiver, BtcTransaction btcTx, Coin amount, int protocolVersion) {
        throw new DeprecatedMethodCallException("Calling BrigeEventLoggerLegacyImpl.logPeginBtc method after RSKIP146 activation");
    }

    @Override
    public void logReleaseBtcRequested(byte[] rskTxHash, BtcTransaction btcTx, Coin amount) {
        throw new DeprecatedMethodCallException("Calling BrigeEventLoggerLegacyImpl.logReleaseBtcRequested method after RSKIP146 activation");
    }

    @Override
    public void logRejectedPegin(BtcTransaction btcTx, RejectedPeginReason reason) {
        throw new DeprecatedMethodCallException("Calling BrigeEventLoggerLegacyImpl.logRejectedPegin method after RSKIP146 activation");
    }

    @Override
    public void logUnrefundablePegin(BtcTransaction btcTx, UnrefundablePeginReason reason) {
        throw new DeprecatedMethodCallException("Calling BrigeEventLoggerLegacyImpl.logUnrefundablePegin method after RSKIP146 activation");
    }

    @Override
    public void logReleaseBtcRequestReceived(String sender, byte[] btcDestinationAddress, Coin amount) {
        throw new DeprecatedMethodCallException("Calling BrigeEventLoggerLegacyImpl.logReleaseBtcRequestReceived method after RSKIP146 activation");
    }

    @Override
    public void logReleaseBtcRequestRejected(String sender, Coin amount, RejectedPegoutReason reason) {
        throw new DeprecatedMethodCallException("Calling BrigeEventLoggerLegacyImpl.logReleaseBtcRequestRejected method after RSKIP146 activation");
    }

    @Override
    public void logBatchPegoutCreated(BtcTransaction btcTx, List<Keccak256> rskTxHashes) {
        throw new DeprecatedMethodCallException("Calling BrigeEventLoggerLegacyImpl.logBatchPegoutCreated method after RSKIP146 activation");
    }

    private byte[] flatKeysAsRlpCollection(List<BtcECKey> keys) {
        return flatKeys(keys, (k -> RLP.encodeElement(k.getPubKey())));
    }

    private byte[] flatKeys(List<BtcECKey> keys, Function<BtcECKey, byte[]> parser) {
        List<byte[]> pubKeys = keys.stream()
            .map(parser)
            .collect(Collectors.toList());
        int pubKeysLength = pubKeys.stream().mapToInt(key -> key.length).sum();

        byte[] flatPubKeys = new byte[pubKeysLength];
        int copyPos = 0;
        for (byte[] key : pubKeys) {
            System.arraycopy(key, 0, flatPubKeys, copyPos, key.length);
            copyPos += key.length;
        }

        return flatPubKeys;
    }
}
