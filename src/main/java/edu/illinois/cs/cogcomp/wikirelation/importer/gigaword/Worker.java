package edu.illinois.cs.cogcomp.wikirelation.importer.gigaword;

import edu.illinois.cs.cogcomp.thrift.curator.Record;

public abstract class Worker implements Runnable{

    Record record;

    public Worker(Record record) {
        this.record = record;
    }

    @Override
    public void run() {
        processRecord(this.record);
    }

    public abstract void processRecord(Record r);

}
