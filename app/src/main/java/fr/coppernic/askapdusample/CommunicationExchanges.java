package fr.coppernic.askapdusample;

import fr.coppernic.sdk.utils.core.CpcBytes;

public class CommunicationExchanges {
    private byte[] mDataSent;
    private byte[] mDataReceived;
    private byte[] mStatus;

    public CommunicationExchanges(byte[] dataSent, byte[] dataReceived, byte[] status) {
        if (dataSent != null) {
            mDataSent = new byte[dataSent.length];
            System.arraycopy(dataSent, 0, mDataSent, 0, dataSent.length);
        }

        if (dataReceived != null) {
            mDataReceived = new byte[dataReceived.length];
            System.arraycopy(dataReceived, 0, mDataReceived, 0, dataReceived.length);
        }

        if (status != null) {
            mStatus = new byte[status.length];
            System.arraycopy(status, 0, mStatus, 0, status.length);
        }

    }

    public String getDataSent() {
        if (mDataSent == null) return "null";
        return CpcBytes.byteArrayToString(mDataSent);
    }

    public String getDataReceived() {
        if(mDataReceived == null) return "null";
        return CpcBytes.byteArrayToString(mDataReceived);
    }

    public String getStatus() {
        if (mStatus == null) return "null";
        return CpcBytes.byteArrayToString(mStatus);
    }
}
