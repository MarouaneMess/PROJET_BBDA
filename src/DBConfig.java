import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Classe de configuration pour le MiniSGBDR
 * Contient les paramètres de configuration du système
 */
public class DBConfig {
    private String dbpath;
    private int pagesize;
    private int dm_maxfilecount;
    // TP3: BufferManager configuration
    private int bm_buffercount;   // number of frames/buffers
    private String bm_policy;     // replacement policy: LRU or MRU

    /**
     * Constructeur par défaut (valeurs par défaut pour pagesize et dm_maxfilecount)
     */
    public DBConfig(String dbpath) {
        this(dbpath, 4096, 16, 2, "LRU");
    }

    public DBConfig(String dbpath, int pagesize, int dm_maxfilecount) {
        this(dbpath, pagesize, dm_maxfilecount, 2, "LRU");
    }

    // TP3: full constructor including BufferManager params
    public DBConfig(String dbpath, int pagesize, int dm_maxfilecount, int bm_buffercount, String bm_policy) {
        this.dbpath = dbpath;
        this.pagesize = pagesize;
        this.dm_maxfilecount = dm_maxfilecount;
        this.bm_buffercount = bm_buffercount;
        this.bm_policy = (bm_policy == null || bm_policy.isEmpty()) ? "LRU" : bm_policy.toUpperCase();
    }

    public String getDbpath() {
        return dbpath;
    }

    public int getPagesize() {
        return pagesize;
    }

    public int getDm_maxfilecount() {
        return dm_maxfilecount;
    }

    // TP3 getters
    public int getBm_buffercount() { return bm_buffercount; }
    public String getBm_policy() { return bm_policy; }

    public void setDbpath(String dbpath) {
        this.dbpath = dbpath;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    public void setDm_maxfilecount(int dm_maxfilecount) {
        this.dm_maxfilecount = dm_maxfilecount;
    }

    // TP3 setters
    public void setBm_buffercount(int bm_buffercount) { this.bm_buffercount = bm_buffercount; }
    public void setBm_policy(String bm_policy) { this.bm_policy = (bm_policy == null || bm_policy.isEmpty()) ? "LRU" : bm_policy.toUpperCase(); }

    public static DBConfig LoadDBConfig(String fichier_config) throws IOException, IllegalArgumentException {
        if (!Files.exists(Paths.get(fichier_config))) {
            throw new IOException("Le fichier de configuration n'existe pas : " + fichier_config);
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(fichier_config)) {
            props.load(fis);
        }

        String dbpath = props.getProperty("dbpath");
        if (dbpath == null || dbpath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le paramètre 'dbpath' est manquant ou vide dans le fichier de configuration");
        }
        dbpath = dbpath.trim();
        if (dbpath.startsWith("'") && dbpath.endsWith("'")) dbpath = dbpath.substring(1, dbpath.length() - 1);
        if (dbpath.startsWith("\"") && dbpath.endsWith("\"")) dbpath = dbpath.substring(1, dbpath.length() - 1);

        int pagesize = 4096;
        int maxfiles = 16;
        int bmCount = 2;
        String bmPol = "LRU";
        String ps = props.getProperty("pagesize");
        String mf = props.getProperty("dm_maxfilecount");
        String bc = props.getProperty("bm_buffercount");
        String bp = props.getProperty("bm_policy");
        if (ps != null && !ps.trim().isEmpty()) {
            try { pagesize = Integer.parseInt(ps.trim()); } catch (NumberFormatException ignored) {}
        }
        if (mf != null && !mf.trim().isEmpty()) {
            try { maxfiles = Integer.parseInt(mf.trim()); } catch (NumberFormatException ignored) {}
        }
        if (bc != null && !bc.trim().isEmpty()) {
            try { bmCount = Integer.parseInt(bc.trim()); } catch (NumberFormatException ignored) {}
        }
        if (bp != null && !bp.trim().isEmpty()) {
            bmPol = bp.trim();
            if (bmPol.startsWith("'") && bmPol.endsWith("'")) bmPol = bmPol.substring(1, bmPol.length() - 1);
            if (bmPol.startsWith("\"") && bmPol.endsWith("\"")) bmPol = bmPol.substring(1, bmPol.length() - 1);
        }

        return new DBConfig(dbpath, pagesize, maxfiles, bmCount, bmPol);
    }

    public static DBConfig LoadDBConfigSimple(String fichier_config) throws IOException, IllegalArgumentException {
        if (!Files.exists(Paths.get(fichier_config))) {
            throw new IOException("Le fichier de configuration n'existe pas : " + fichier_config);
        }

        String dbpathVal = null;
        int pagesizeVal = 4096;
        int maxfilesVal = 16;
        int bmCountVal = 2;
        String bmPolVal = "LRU";
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fichier_config))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                String value = parts[1].trim();
                if (value.startsWith("'") && value.endsWith("'")) value = value.substring(1, value.length() - 1);
                if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);
                switch (key) {
                    case "dbpath": dbpathVal = value; break;
                    case "pagesize": try { pagesizeVal = Integer.parseInt(value); } catch (NumberFormatException ignored) {} break;
                    case "dm_maxfilecount": try { maxfilesVal = Integer.parseInt(value); } catch (NumberFormatException ignored) {} break;
                    case "bm_buffercount": try { bmCountVal = Integer.parseInt(value); } catch (NumberFormatException ignored) {} break;
                    case "bm_policy": bmPolVal = value; break;
                    default: break;
                }
            }
        }

        if (dbpathVal == null || dbpathVal.trim().isEmpty()) {
            throw new IllegalArgumentException("Le paramètre 'dbpath' n'a pas été trouvé dans le fichier de configuration");
        }
        return new DBConfig(dbpathVal, pagesizeVal, maxfilesVal, bmCountVal, bmPolVal);
    }

    @Override
    public String toString() {
        return "DBConfig{" +
                "dbpath='" + dbpath + '\'' +
                ", pagesize=" + pagesize +
                ", dm_maxfilecount=" + dm_maxfilecount +
                ", bm_buffercount=" + bm_buffercount +
                ", bm_policy=" + bm_policy +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBConfig dbConfig = (DBConfig) o;
        if (dbpath != null ? !dbpath.equals(dbConfig.dbpath) : dbConfig.dbpath != null) return false;
        if (pagesize != dbConfig.pagesize) return false;
        if (dm_maxfilecount != dbConfig.dm_maxfilecount) return false;
        if (bm_buffercount != dbConfig.bm_buffercount) return false;
        return bm_policy != null ? bm_policy.equals(dbConfig.bm_policy) : dbConfig.bm_policy == null;
    }

    @Override
    public int hashCode() {
        int result = dbpath != null ? dbpath.hashCode() : 0;
        result = 31 * result + pagesize;
        result = 31 * result + dm_maxfilecount;
        result = 31 * result + bm_buffercount;
        result = 31 * result + (bm_policy != null ? bm_policy.hashCode() : 0);
        return result;
    }
}