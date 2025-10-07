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

    /**
     * Constructeur par défaut (valeurs par défaut pour pagesize et dm_maxfilecount)
     */
    public DBConfig(String dbpath) {
        this(dbpath, 4096, 16);
    }

    public DBConfig(String dbpath, int pagesize, int dm_maxfilecount) {
        this.dbpath = dbpath;
        this.pagesize = pagesize;
        this.dm_maxfilecount = dm_maxfilecount;
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

    public void setDbpath(String dbpath) {
        this.dbpath = dbpath;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    public void setDm_maxfilecount(int dm_maxfilecount) {
        this.dm_maxfilecount = dm_maxfilecount;
    }

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
        String ps = props.getProperty("pagesize");
        String mf = props.getProperty("dm_maxfilecount");
        if (ps != null && !ps.trim().isEmpty()) {
            try { pagesize = Integer.parseInt(ps.trim()); } catch (NumberFormatException ignored) {}
        }
        if (mf != null && !mf.trim().isEmpty()) {
            try { maxfiles = Integer.parseInt(mf.trim()); } catch (NumberFormatException ignored) {}
        }

        return new DBConfig(dbpath, pagesize, maxfiles);
    }

    public static DBConfig LoadDBConfigSimple(String fichier_config) throws IOException, IllegalArgumentException {
        if (!Files.exists(Paths.get(fichier_config))) {
            throw new IOException("Le fichier de configuration n'existe pas : " + fichier_config);
        }

        String dbpathVal = null;
        int pagesizeVal = 4096;
        int maxfilesVal = 16;
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
                    default: break;
                }
            }
        }

        if (dbpathVal == null || dbpathVal.trim().isEmpty()) {
            throw new IllegalArgumentException("Le paramètre 'dbpath' n'a pas été trouvé dans le fichier de configuration");
        }

        return new DBConfig(dbpathVal, pagesizeVal, maxfilesVal);
    }

    @Override
    public String toString() {
        return "DBConfig{" +
                "dbpath='" + dbpath + '\'' +
                ", pagesize=" + pagesize +
                ", dm_maxfilecount=" + dm_maxfilecount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBConfig dbConfig = (DBConfig) o;
        if (dbpath != null ? !dbpath.equals(dbConfig.dbpath) : dbConfig.dbpath != null) return false;
        if (pagesize != dbConfig.pagesize) return false;
        return dm_maxfilecount == dbConfig.dm_maxfilecount;
    }

    @Override
    public int hashCode() {
        int result = dbpath != null ? dbpath.hashCode() : 0;
        result = 31 * result + pagesize;
        result = 31 * result + dm_maxfilecount;
        return result;
    }
}