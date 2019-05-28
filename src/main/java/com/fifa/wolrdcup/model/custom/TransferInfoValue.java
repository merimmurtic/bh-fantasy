package com.fifa.wolrdcup.model.custom;

import com.fifa.wolrdcup.model.players.Player;

import java.util.ArrayList;
import java.util.List;

public class TransferInfoValue {

    private List<Player> transferIn = new ArrayList<>();
    private List<Player> transferOut = new ArrayList<>();


    public TransferInfoValue() {
    }

    public TransferInfoValue(List<Player> transferIn, List<Player> transferOut) {
        this.transferIn = transferIn;
        this.transferOut = transferOut;
    }

    public List<Player> getTransferIn() {
        return transferIn;
    }

    public void setTransferIn(List<Player> transferIn) {
        this.transferIn = transferIn;
    }

    public List<Player> getTransferOut() {
        return transferOut;
    }

    public void setTransferOut(List<Player> transferOut) {
        this.transferOut = transferOut;
    }


}
