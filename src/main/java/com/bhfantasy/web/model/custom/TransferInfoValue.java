package com.bhfantasy.web.model.custom;

import com.bhfantasy.web.model.players.Player;

import java.util.ArrayList;
import java.util.List;

public class TransferInfoValue {

    private List<Player> transferIn = new ArrayList<>();
    private List<Player> transferOut = new ArrayList<>();

    public TransferInfoValue() {
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
