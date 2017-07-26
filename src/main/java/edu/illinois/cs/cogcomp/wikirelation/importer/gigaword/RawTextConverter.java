package edu.illinois.cs.cogcomp.wikirelation.importer.gigaword;

import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.wikirelation.util.CommonUtil;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class RawTextConverter {
    public static void main(String[] args){
        if (args.length != 2) {
            System.err.println("Usage: java RawTextConverter [gigaword-rec-path] [out-dir]");
            System.exit(1);
        }

        File recDir = new File(args[0]);
        String outDir = args[1];
        if (!recDir.isDirectory()) 
            return;
        
        File[] list = recDir.listFiles();
        
        if (list == null) 
            return;
        
        int count = 0;
        for (File f: list) {
            if (!f.isFile())
                continue;

            try {
                FileInputStream in = new FileInputStream(f);
                Record rec = CommonUtil.deserializeRecordFromBytes(IOUtils.toByteArray(in));
                IOUtils.write(rec.getRawText(), new FileOutputStream(outDir + File.separator + f.getName()));
            }
            catch (Exception e) {
                continue;
            }
            
            count++;
            if (count % 2000 == 0) {
                System.out.println("Processed :\t" + count);
            }
        }
    }
}
