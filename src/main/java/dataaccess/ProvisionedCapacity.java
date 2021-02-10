package dataaccess;

/**
 *  Convience class for holding the units of table provisioning
 */
public class ProvisionedCapacity {
    private long readUnits;
    private long writeUnits;
    private long gbStorage;

    public ProvisionedCapacity(long reads, long writes) {
        this.readUnits = reads;
        this.writeUnits = writes;
    }

    public long getReadUnits() {

        return readUnits;
    }

    public void setReadUnits(long readUnits) {

        this.readUnits = readUnits;
    }

    public long getWriteUnits() {

        return writeUnits;
    }

    public void setWriteUnits(long writeUnits) {

        this.writeUnits = writeUnits;
    }

    public long getGbStorage() {

        return gbStorage;
    }

    public void setGbStorage(long gbStorage) {

        this.gbStorage = gbStorage;
    }
}
