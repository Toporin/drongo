package com.sparrowwallet.drongo.wallet;

import com.sparrowwallet.drongo.KeyDerivation;
import com.sparrowwallet.drongo.KeyPurpose;
import com.sparrowwallet.drongo.crypto.ChildNumber;

import java.util.*;
import java.util.stream.Collectors;

public class WalletNode implements Comparable<WalletNode> {
    private final String derivationPath;
    private String label;
    private Set<WalletNode> children = new TreeSet<>();
    private Set<BlockchainTransactionHashIndex> transactionOutputs = new TreeSet<>();

    private transient KeyPurpose keyPurpose;
    private transient int index = -1;
    private transient List<ChildNumber> derivation;

    public WalletNode(String derivationPath) {
        this.derivationPath = derivationPath;
        parseDerivation();
    }

    public WalletNode(KeyPurpose keyPurpose) {
        this.derivation = List.of(keyPurpose.getPathIndex());
        this.derivationPath = KeyDerivation.writePath(derivation);
        this.keyPurpose = keyPurpose;
        this.index = keyPurpose.getPathIndex().num();
    }

    public WalletNode(KeyPurpose keyPurpose, int index) {
        this.derivation = List.of(keyPurpose.getPathIndex(), new ChildNumber(index));
        this.derivationPath = KeyDerivation.writePath(derivation);
        this.keyPurpose = keyPurpose;
        this.index = index;
    }

    public String getDerivationPath() {
        return derivationPath;
    }

    private void parseDerivation() {
        this.derivation = KeyDerivation.parsePath(derivationPath);
        this.keyPurpose = KeyPurpose.fromChildNumber(derivation.get(0));
        this.index = derivation.get(derivation.size() - 1).num();
    }

    public int getIndex() {
        if(index < 0) {
            parseDerivation();
        }

        return index;
    }

    public KeyPurpose getKeyPurpose() {
        if(keyPurpose == null) {
            parseDerivation();
        }

        return keyPurpose;
    }

    public List<ChildNumber> getDerivation() {
        if(derivation == null) {
            parseDerivation();
        }

        return derivation;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getValue() {
        if(transactionOutputs == null) {
            return null;
        }

        return getUnspentTransactionOutputs().stream().mapToLong(BlockchainTransactionHashIndex::getValue).sum();
    }

    public Set<WalletNode> getChildren() {
        return children;
    }

    public void setChildren(Set<WalletNode> children) {
        this.children = children;
    }

    public Set<BlockchainTransactionHashIndex> getTransactionOutputs() {
        return transactionOutputs;
    }

    public void setTransactionOutputs(Set<BlockchainTransactionHashIndex> transactionOutputs) {
        this.transactionOutputs = transactionOutputs;
    }

    public Set<BlockchainTransactionHashIndex> getUnspentTransactionOutputs() {
        Set<BlockchainTransactionHashIndex> unspentTXOs = new TreeSet<>(transactionOutputs);
        return unspentTXOs.stream().filter(txo -> !txo.isSpent()).collect(Collectors.toCollection(HashSet::new));
    }

    public void fillToIndex(int index) {
        for(int i = 0; i <= index; i++) {
            WalletNode node = new WalletNode(getKeyPurpose(), i);
            getChildren().add(node);
        }
    }

    public Integer getHighestUsedIndex() {
        WalletNode highestNode = null;
        for(WalletNode childNode : getChildren()) {
            if(!childNode.getTransactionOutputs().isEmpty()) {
                highestNode = childNode;
            }
        }

        return highestNode == null ? null : highestNode.index;
    }

    @Override
    public String toString() {
        return derivationPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalletNode node = (WalletNode) o;
        return derivationPath.equals(node.derivationPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(derivationPath);
    }

    @Override
    public int compareTo(WalletNode node) {
        return getIndex() - node.getIndex();
    }

    public void clearHistory() {
        transactionOutputs.clear();
        for(WalletNode childNode : getChildren()) {
            childNode.clearHistory();
        }
    }

    public WalletNode copy() {
        WalletNode copy = new WalletNode(derivationPath);
        copy.setLabel(label);

        for(WalletNode child : getChildren()) {
            copy.getChildren().add(child.copy());
        }

        for(BlockchainTransactionHashIndex txo : getTransactionOutputs()) {
            copy.getTransactionOutputs().add(txo.copy());
        }

        return copy;
    }
}
