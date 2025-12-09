import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
public class DBManager {
    private DBConfig config;
    private ArrayList<Relation> relations = new ArrayList<>();
    
    public DBManager(DBConfig config) {
        this.config = config;
    }
    public DBConfig getConfig() {
        return config;
    }
    public void addTable (Relation tab){
     if ( relations.contains(tab)){
        System.out.println("Table already exists.");
     } else {
        relations.add(tab);
     }
    }

    public Relation getTable (String nomTable){
        for (Relation r : relations){
            if (r.getName().equals (nomTable)){
                return r;
            }
        }
        return null;
    }
    public void removeTable (String nomTable){
        relations.removeIf(r -> r.getName().equals(nomTable));
    }

    public void removeAllTables (){
        relations.clear();
    }

    public void describeTable(String nomTable){
        Relation r = getTable(nomTable);
        if (r == null){
            System.out.println("Table " + nomTable + " does not exist.");
            return;
        }

        StringJoiner joiner = new StringJoiner(",", r.getName() + " (", ")");
        for (ColumnInfo c : r.getColumns()){
            String typeDesc = (c.type == ColumnType.VARCHAR || c.type == ColumnType.CHAR)
                    ? c.type + "(" + c.lengthT + ")"
                    : c.type.toString();
            joiner.add(c.name + ":" + typeDesc);
        }
        System.out.println(joiner.toString());
    }

    public void describeTables(){
        for (Relation r : relations){
            describeTable(r.getName());
        }
    }

    public void saveState(){
        Path savePath = Paths.get(config.getDbpath()).resolve("database.save");
        try {
            Files.createDirectories(savePath.getParent());
            try (BufferedWriter bw = Files.newBufferedWriter(savePath)) {
                // bw.write("#DB_SAVE_V1\n");
                bw.write("RELATIONS " + relations.size() + "\n");
                for (Relation r : relations) {
                    bw.write("TABLE " + r.getName() + "\n");

                    PageId header = r.getHeaderPageId();
                    int hFile = header != null ? header.getFileIdx() : -1;
                    int hPage = header != null ? header.getPageIdx() : -1;
                    bw.write("HEADER " + hFile + " " + hPage + "\n");

                    bw.write("NBSLOTS " + r.getNbSlotsPerDataPage() + "\n");
                    List<ColumnInfo> cols = r.getColumns();
                    bw.write("COLUMNS " + cols.size() + "\n");
                    for (ColumnInfo c : cols) {
                        bw.write("COL " + c.name + " " + c.type.name() + " " + c.lengthT + "\n");
                    }
                    bw.write("ENDTABLE\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to save state: " + e.getMessage());
        }
    }
    public void loadState(){
        Path savePath = Paths.get(config.getDbpath()).resolve("database.save");
        if (!Files.exists(savePath)) {
            return; // nothing to load
        }

        ArrayList<Relation> loaded = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(savePath)) {
            String line = br.readLine();
            // if (line == null || !line.startsWith("#DB_SAVE_V1")) {
            //     return;
            // }
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("TABLE ")) {
                    continue;
                }

                String tableName = line.substring("TABLE ".length());
                PageId header = null;
                int nbSlots = 0;
                List<ColumnInfo> cols = new ArrayList<>();

                while ((line = br.readLine()) != null && !line.equals("ENDTABLE")) {
                    if (line.startsWith("HEADER ")) {
                        String[] parts = line.split(" ");
                        if (parts.length >= 3) {
                            int f = Integer.parseInt(parts[1]);
                            int p = Integer.parseInt(parts[2]);
                            if (f >= 0 && p >= 0) {
                                header = new PageId(f, p);
                            }
                        }
                    } else if (line.startsWith("NBSLOTS ")) {
                        String[] parts = line.split(" ");
                        if (parts.length >= 2) {
                            nbSlots = Integer.parseInt(parts[1]);
                        }
                    } else if (line.startsWith("COL ")) {
                        String[] parts = line.split(" ", 4);
                        if (parts.length >= 4) {
                            String colName = parts[1];
                            ColumnType type = ColumnType.valueOf(parts[2]);
                            int len = Integer.parseInt(parts[3]);
                            cols.add(new ColumnInfo(colName, type, len));
                        }
                    }
                }

                if (!cols.isEmpty()) {
                    Relation rel = new Relation(tableName, cols, header, nbSlots, null, null);
                    loaded.add(rel);
                }
            }

            relations = loaded;
        } catch (IOException | RuntimeException e) {
            System.err.println("Failed to load state: " + e.getMessage());
        }
    }



}
