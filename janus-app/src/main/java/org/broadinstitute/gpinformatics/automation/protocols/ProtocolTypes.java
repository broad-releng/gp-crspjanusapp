package org.broadinstitute.gpinformatics.automation.protocols;

public enum ProtocolTypes {
    FingerprintNormalization("Fingerprint Aliquot", "FingerprintingAliquot"),
    ShearingNormalization("Shearing Aliquot", "ShearingAliquot"),
    Normalization("Norm", "InitialNormalization"),
    RaiseVolume("Volume Addition", "VolumeAddition"),
    TruSeqAliquot("TruSeq Aliquot", "PolyATSAliquot"),
    TruSeqSpike("TruSeq Norm/Spike", "PolyATSAliquotSpike"),
    RNACaliperQC("RNA Caliper QC", "RNACaliperSetup"),
    VolumeTransfer("Volume Transfer", "EmergeVolumeTransfer"),
    CrspSpike("Crsp Norms and Spikes", "SpikeIn");

    private String value;
    private String eventType;

    ProtocolTypes(String value, String eventName) {
        this.value = value;
        this.eventType = eventName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getEventType() {
        return eventType;
    }
}
