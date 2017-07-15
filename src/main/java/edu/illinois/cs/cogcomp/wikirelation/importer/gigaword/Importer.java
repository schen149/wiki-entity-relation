package edu.illinois.cs.cogcomp.wikirelation.importer.gigaword;

import edu.illinois.cs.cogcomp.thrift.curator.Record;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

public class Importer {

    private String recordFilePath;
    public Importer(String recordFilePath){
        this.recordFilePath = recordFilePath;
    }

    public void populateDB() {

    }

    public static Record deserializeRecordFromBytes(byte[] bytes) throws TException {
        Record rec = new Record();
        TDeserializer td = new TDeserializer();
        td.deserialize(rec, bytes);
        return rec;
    }
}
