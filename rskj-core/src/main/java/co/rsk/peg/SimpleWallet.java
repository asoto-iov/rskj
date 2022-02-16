package co.rsk.peg;

import co.rsk.bitcoinj.core.Context;
import co.rsk.bitcoinj.wallet.RedeemData;
import co.rsk.bitcoinj.wallet.Wallet;

import javax.annotation.Nullable;

/**
 * Simple wallet class implementation use to watch address
 */
public class SimpleWallet extends Wallet {
    public SimpleWallet(Context context) {
        super(context);
    }

    /**
     * @param payToScriptHash
     * @return null because base implementation method throws UnsupportedOperationException
     * and this cause an error when getting the amount sent to an address.
     */
    @Nullable
    @Override
    public RedeemData findRedeemDataFromScriptHash(byte[] payToScriptHash) {
        return null;
    }
}