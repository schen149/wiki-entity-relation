package edu.illinois.cs.cogcomp.wikirelation.importer.geoname;

import edu.illinois.cs.cogcomp.wikirelation.core.StringIDLinker;
import edu.illinois.cs.cogcomp.wikirelation.util.DataTypeUtil;

import java.util.HashSet;
import java.util.Set;

public class IDImporterWorker implements Runnable{
    
    private String line;
    private StringIDLinker idLinker;
    public IDImporterWorker(String line, StringIDLinker idLinker) {
        this.line = line;
        this.idLinker = idLinker;
    }

    @Override
    public void run() {
        String[] parts = line.split("\\t");
        if (parts.length != 19)
            return;
        int id = 0;
        try {
            id = Integer.parseInt(parts[0]);
        }
        catch (NumberFormatException e){
            return;
        }

        Set<String> forms = new HashSet<>();
        String form = DataTypeUtil.normalizeString(parts[1]);
        addToSet(forms, form);
        form = DataTypeUtil.normalizeString(parts[2]);
        addToSet(forms, form);
        if (!parts[3].isEmpty()) {
            for (String f : parts[3].split(",")) {
                form = DataTypeUtil.normalizeString(f);
                addToSet(forms, form);
            }
        }
        
        /* Update id linker one by one */
        for (String f: forms)
            idLinker.put(f, id);
    }

    private static void addToSet(Set<String> set, String str) {
        if (str != null)
            set.add(str);
    }
}
