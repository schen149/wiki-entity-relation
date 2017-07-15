package edu.illinois.cs.cogcomp.wikirelation.gigaword;

import edu.illinois.cs.cogcomp.thrift.curator.Record;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

public class GigawordImporter {

    private String recordFilePath;
    public GigawordImporter(String recordFilePath){
        this.recordFilePath = recordFilePath;
    }

    public static Record deserializeRecordFromBytes(byte[] bytes) throws TException {
        TDeserializer deserializer = new TDeserializer(
                new TBinaryProtocol.Factory());
        Record r = new Record();
        deserializer.deserialize(r, bytes);
        return r;
    }
}
