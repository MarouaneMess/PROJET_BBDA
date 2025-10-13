import java.io.*;
import java.nio.file.*;

/**
 * Classe de tests pour DBConfig
 * Tests de construction en mémoire, chargement depuis fichier, et cas d'erreur
 */
public class DBConfigTest {
    
    private static int testsExecuted = 0;
    private static int testsPassed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== Tests DBConfig ===\n");
        
        // Tests de construction en mémoire
        testConstructorValid();
        testConstructorNull();
        
        // Tests de chargement depuis fichier
        testLoadFromValidFile();
        testLoadFromValidPropertiesFile();
        testLoadFromInvalidFile();
        testLoadFromNonExistentFile();
        testLoadFromEmptyFile();
        testLoadFromFileWithComments();
        
        // Tests des cas d'erreur
        testLoadWithMissingDbpath();
        testLoadWithEmptyDbpath();
        
        // Tests des méthodes utilitaires
        testToString();
        testEquals();
        
        // Tests TP3: nouveaux paramètres BufferManager
        testConstructorWithBM();
        testLoadFileWithBMParams();
        
        // Résumé des tests
        System.out.println("\n=== Résumé des tests ===");
        System.out.println("Tests exécutés : " + testsExecuted);
        System.out.println("Tests réussis : " + testsPassed);
        System.out.println("Tests échoués : " + (testsExecuted - testsPassed));
        
        if (testsExecuted == testsPassed) {
            System.out.println("✓ Tous les tests sont passés !");
        } else {
            System.out.println("✗ Certains tests ont échoué.");
        }
    }
    
    private static void testConstructorValid() {
        System.out.println("Test : Construction avec chemin valide");
        testsExecuted++;
        try {
            DBConfig config = new DBConfig("./data");
            if ("./data".equals(config.getDbpath())) {
                System.out.println("✓ PASSED");
                testsPassed++;
            } else {
                System.out.println("✗ FAILED : chemin incorrect");
            }
        } catch (Exception e) {
            System.out.println("✗ FAILED : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testConstructorNull() {
        System.out.println("Test : Construction avec chemin null");
        testsExecuted++;
        try {
            DBConfig config = new DBConfig(null);
            if (config.getDbpath() == null) {
                System.out.println("✓ PASSED");
                testsPassed++;
            } else {
                System.out.println("✗ FAILED : devrait accepter null");
            }
        } catch (Exception e) {
            System.out.println("✗ FAILED : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLoadFromValidFile() {
        System.out.println("Test : Chargement depuis fichier valide");
        testsExecuted++;
        try {
            // Créer un fichier de test temporaire
            String tempFile = createTempConfigFile("dbpath = ./test_db");
            
            DBConfig config = DBConfig.LoadDBConfigSimple(tempFile);
            if ("./test_db".equals(config.getDbpath())) {
                System.out.println("✓ PASSED");
                testsPassed++;
            } else {
                System.out.println("✗ FAILED : chemin incorrect : " + config.getDbpath());
            }
            
            // Nettoyer le fichier temporaire
            Files.deleteIfExists(Paths.get(tempFile));
        } catch (Exception e) {
            System.out.println("✗ FAILED : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLoadFromValidPropertiesFile() {
        System.out.println("Test : Chargement depuis fichier properties");
        testsExecuted++;
        try {
            // Créer un fichier properties de test temporaire
            String tempFile = createTempPropertiesFile("dbpath=./test_properties_db");
            
            DBConfig config = DBConfig.LoadDBConfig(tempFile);
            if ("./test_properties_db".equals(config.getDbpath())) {
                System.out.println("✓ PASSED");
                testsPassed++;
            } else {
                System.out.println("✗ FAILED : chemin incorrect : " + config.getDbpath());
            }
            
            // Nettoyer le fichier temporaire
            Files.deleteIfExists(Paths.get(tempFile));
        } catch (Exception e) {
            System.out.println("✗ FAILED : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLoadFromInvalidFile() {
        System.out.println("Test : Chargement depuis fichier avec format invalide");
        testsExecuted++;
        try {
            // Créer un fichier de test avec format invalide
            String tempFile = createTempConfigFile("format_invalide");
            
            DBConfig.LoadDBConfigSimple(tempFile);
            System.out.println("✗ FAILED : devrait lever une exception");
            
            // Nettoyer le fichier temporaire
            Files.deleteIfExists(Paths.get(tempFile));
        } catch (IllegalArgumentException e) {
            System.out.println("✓ PASSED : exception attendue levée");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("✗ FAILED : mauvais type d'exception : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLoadFromNonExistentFile() {
        System.out.println("Test : Chargement depuis fichier inexistant");
        testsExecuted++;
        try {
            DBConfig.LoadDBConfig("fichier_inexistant.txt");
            System.out.println("✗ FAILED : devrait lever une exception");
        } catch (IOException e) {
            System.out.println("✓ PASSED : exception attendue levée");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("✗ FAILED : mauvais type d'exception : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLoadFromEmptyFile() {
        System.out.println("Test : Chargement depuis fichier vide");
        testsExecuted++;
        try {
            String tempFile = createTempConfigFile("");
            
            DBConfig.LoadDBConfigSimple(tempFile);
            System.out.println("✗ FAILED : devrait lever une exception");
            
            Files.deleteIfExists(Paths.get(tempFile));
        } catch (IllegalArgumentException e) {
            System.out.println("✓ PASSED : exception attendue levée");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("✗ FAILED : mauvais type d'exception : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLoadFromFileWithComments() {
        System.out.println("Test : Chargement depuis fichier avec commentaires");
        testsExecuted++;
        try {
            String content = "# Ceci est un commentaire\n" +
                           "# dbpath = ./commentaire\n" +
                           "dbpath = ./real_path\n" +
                           "# Autre commentaire";
            String tempFile = createTempConfigFile(content);
            
            DBConfig config = DBConfig.LoadDBConfigSimple(tempFile);
            if ("./real_path".equals(config.getDbpath())) {
                System.out.println("✓ PASSED");
                testsPassed++;
            } else {
                System.out.println("✗ FAILED : chemin incorrect : " + config.getDbpath());
            }
            
            Files.deleteIfExists(Paths.get(tempFile));
        } catch (Exception e) {
            System.out.println("✗ FAILED : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLoadWithMissingDbpath() {
        System.out.println("Test : Chargement avec dbpath manquant");
        testsExecuted++;
        try {
            String tempFile = createTempConfigFile("autre_param = valeur");
            
            DBConfig.LoadDBConfigSimple(tempFile);
            System.out.println("✗ FAILED : devrait lever une exception");
            
            Files.deleteIfExists(Paths.get(tempFile));
        } catch (IllegalArgumentException e) {
            System.out.println("✓ PASSED : exception attendue levée");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("✗ FAILED : mauvais type d'exception : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLoadWithEmptyDbpath() {
        System.out.println("Test : Chargement avec dbpath vide");
        testsExecuted++;
        try {
            String tempFile = createTempPropertiesFile("dbpath=");
            
            DBConfig.LoadDBConfig(tempFile);
            System.out.println("✗ FAILED : devrait lever une exception");
            
            Files.deleteIfExists(Paths.get(tempFile));
        } catch (IllegalArgumentException e) {
            System.out.println("✓ PASSED : exception attendue levée");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("✗ FAILED : mauvais type d'exception : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testToString() {
        System.out.println("Test : Méthode toString");
        testsExecuted++;
        try {
            DBConfig config = new DBConfig("./test");
            String result = config.toString();
            if (result.contains("./test")) {
                System.out.println("✓ PASSED");
                testsPassed++;
            } else {
                System.out.println("✗ FAILED : toString ne contient pas le chemin");
            }
        } catch (Exception e) {
            System.out.println("✗ FAILED : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testEquals() {
        System.out.println("Test : Méthode equals");
        testsExecuted++;
        try {
            DBConfig config1 = new DBConfig("./test");
            DBConfig config2 = new DBConfig("./test");
            DBConfig config3 = new DBConfig("./other");
            
            if (config1.equals(config2) && !config1.equals(config3)) {
                System.out.println("✓ PASSED");
                testsPassed++;
            } else {
                System.out.println("✗ FAILED : equals ne fonctionne pas correctement");
            }
        } catch (Exception e) {
            System.out.println("✗ FAILED : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testConstructorWithBM() {
        System.out.println("Test : Constructeur avec bm_buffercount et bm_policy");
        testsExecuted++;
        try {
            DBConfig cfg = new DBConfig("./db", 1024, 8, 3, "MRU");
            if (cfg.getBm_buffercount() == 3 && "MRU".equals(cfg.getBm_policy())) {
                System.out.println("✓ PASSED");
                testsPassed++;
            } else {
                System.out.println("✗ FAILED : valeurs bm incorrectes");
            }
        } catch (Exception e) {
            System.out.println("✗ FAILED : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testLoadFileWithBMParams() {
        System.out.println("Test : Chargement fichier avec bm_* ");
        testsExecuted++;
        try {
            String content = "dbpath=./db\n" +
                    "pagesize=512\n" +
                    "dm_maxfilecount=5\n" +
                    "bm_buffercount=4\n" +
                    "bm_policy=MRU\n";
            String tempFile = createTempPropertiesFile(content);
            DBConfig cfg = DBConfig.LoadDBConfig(tempFile);
            Files.deleteIfExists(Paths.get(tempFile));
            boolean ok = cfg.getPagesize()==512 && cfg.getDm_maxfilecount()==5 && cfg.getBm_buffercount()==4 && "MRU".equals(cfg.getBm_policy());
            if (ok) { System.out.println("✓ PASSED"); testsPassed++; }
            else System.out.println("✗ FAILED : valeurs lues incorrectes");
        } catch (Exception e) {
            System.out.println("✗ FAILED : " + e.getMessage());
        }
        System.out.println();
    }
    
    private static String createTempConfigFile(String content) throws IOException {
        Path tempFile = Files.createTempFile("dbconfig_test", ".txt");
        Files.write(tempFile, content.getBytes());
        return tempFile.toString();
    }
    
    private static String createTempPropertiesFile(String content) throws IOException {
        Path tempFile = Files.createTempFile("dbconfig_test", ".properties");
        Files.write(tempFile, content.getBytes());
        return tempFile.toString();
    }
}
