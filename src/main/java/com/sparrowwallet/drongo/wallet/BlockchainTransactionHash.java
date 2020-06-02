package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.protocol.Sha256Hash;

import java.util.Objects;

public abstract class BlockchainTransactionHash {
    private final Sha256Hash hash;
    private final int height;
    private final Long fee;

    private String label;

    public BlockchainTransactionHash(Sha256Hash hash) {
        this(hash, 0, 0L);
    }

    public BlockchainTransactionHash(Sha256Hash hash, int height) {
        this(hash, height, 0L);
    }

    public BlockchainTransactionHash(Sha256Hash hash, int height, Long fee) {
        this.hash = hash;
        this.height = height;
        this.fee = fee;
    }

    public Sha256Hash getHash() {
        return hash;
    }

    public String getHashAsString() {
        return hash.toString();
    }

    public int getHeight() {
        return height;
    }

    public Long getFee() {
        return fee;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return hash.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockchainTransactionHash that = (BlockchainTransactionHash) o;
        return hash.equals(that.hash) && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, height);
    }

    public int compareTo(BlockchainTransactionHash reference) {
        int heightDiff = height - reference.height;
        if(heightDiff != 0) {
            return heightDiff;
        }

        return hash.compareTo(reference.hash);
    }
}
