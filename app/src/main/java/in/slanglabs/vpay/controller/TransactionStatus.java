package in.slanglabs.vpay.controller;

public class TransactionStatus {
    public String mTxnId;
    public String mResponseCode;
    public String mStatus;
    public String mTxnRef;

    public TransactionStatus(String txnId, String responseCode, String status, String txnRef) {
        mTxnId = txnId;
        mResponseCode = responseCode;
        mStatus = status;
        mTxnRef = txnRef;
    }
}
