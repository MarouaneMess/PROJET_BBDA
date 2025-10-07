import java.io.*;
import java.util.Scanner;

/**
 * Classe principale du MiniSGBDR
 * Application console pour la gestion de base de données relationnelles simplifiée
 */
public class MiniSGBDR {
    
    private DBConfig config;
    private Scanner scanner;
    private boolean running;
    
    /**
     * Constructeur du MiniSGBDR
     * @param config Configuration de la base de données
     */
    public MiniSGBDR(DBConfig config) {
        this.config = config;
        this.scanner = new Scanner(System.in);
        this.running = true;
    }
    
    /**
     * Point d'entrée de l'application
     * @param args Arguments de ligne de commande
     */
    public static void main(String[] args) {
        System.out.println("=== MiniSGBDR - Système de Gestion de Base de Données Relationnelles ===");
        System.out.println("Version 1.0 - TP1 BDDA");
        System.out.println();
        
        try {
            DBConfig config;
            
            // Charger la configuration
            if (args.length > 0) {
                // Configuration depuis fichier fourni en argument
                System.out.println("Chargement de la configuration depuis : " + args[0]);
                config = DBConfig.LoadDBConfig(args[0]);
            } else {
                // Configuration par défaut
                System.out.println("Utilisation de la configuration par défaut");
                config = new DBConfig("./DB");
            }
            
            System.out.println("Configuration chargée : " + config);
            System.out.println("Dossier de données : " + config.getDbpath());
            System.out.println();
            
            // Créer et lancer l'application
            MiniSGBDR sgbdr = new MiniSGBDR(config);
            sgbdr.run();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            System.err.println("Usage : java MiniSGBDR [fichier_config]");
            System.exit(1);
        }
    }
    
    /**
     * Boucle principale de l'application
     */
    public void run() {
        System.out.println("=== MiniSGBDR démarré ===");
        System.out.println("Tapez 'HELP' pour voir les commandes disponibles");
        System.out.println("Tapez 'EXIT' pour quitter l'application");
        System.out.println();
        
        while (running) {
            System.out.print("MiniSGBDR> ");
            String command = scanner.nextLine().trim();
            
            if (!command.isEmpty()) {
                processCommand(command);
            }
        }
        
        System.out.println("Au revoir !");
        scanner.close();
    }
    
    /**
     * Traite une commande utilisateur
     * @param command La commande à traiter
     */
    private void processCommand(String command) {
        String[] parts = command.split("\\s+");
        String mainCommand = parts[0].toUpperCase();
        
        switch (mainCommand) {
            case "EXIT":
                handleExit();
                break;
                
            case "HELP":
                handleHelp();
                break;
                
            case "STATUS":
                handleStatus();
                break;
                
            case "CONFIG":
                handleConfig();
                break;
                
            case "DEBUG":
                handleDebug(parts);
                break;
                
            default:
                System.out.println("Commande inconnue : " + command);
                System.out.println("Tapez 'HELP' pour voir les commandes disponibles");
                break;
        }
    }
    
    /**
     * Gère la commande EXIT
     */
    private void handleExit() {
        System.out.println("Arrêt du MiniSGBDR...");
        running = false;
    }
    
    /**
     * Gère la commande HELP
     */
    private void handleHelp() {
        System.out.println("=== Commandes disponibles ===");
        System.out.println("EXIT                    - Quitter l'application");
        System.out.println("HELP                    - Afficher cette aide");
        System.out.println("STATUS                  - Afficher le statut du système");
        System.out.println("CONFIG                  - Afficher la configuration actuelle");
        System.out.println("DEBUG INFO              - Afficher les informations de debug");
        System.out.println();
        System.out.println("=== Commandes à venir dans les prochains TPs ===");
        System.out.println("CREATE TABLE ...        - Créer une table");
        System.out.println("INSERT INTO ...         - Insérer des données");
        System.out.println("SELECT ...              - Sélectionner des données");
        System.out.println("DROP TABLE ...          - Supprimer une table");
        System.out.println();
    }
    
    /**
     * Gère la commande STATUS
     */
    private void handleStatus() {
        System.out.println("=== Statut du MiniSGBDR ===");
        System.out.println("État : En fonctionnement");
        System.out.println("Configuration : " + config);
        
        // Vérifier si le dossier de données existe
        File dbDir = new File(config.getDbpath());
        if (dbDir.exists() && dbDir.isDirectory()) {
            System.out.println("Dossier de données : Accessible");
            File[] files = dbDir.listFiles();
            int fileCount = (files != null) ? files.length : 0;
            System.out.println("Nombre de fichiers : " + fileCount);
        } else {
            System.out.println("Dossier de données : Non accessible ou inexistant");
        }
        
        System.out.println("Mémoire JVM utilisée : " + 
                         (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MB");
        System.out.println();
    }
    
    /**
     * Gère la commande CONFIG
     */
    private void handleConfig() {
        System.out.println("=== Configuration actuelle ===");
        System.out.println("Chemin des données (dbpath) : " + config.getDbpath());
        
        // Vérifications supplémentaires
        File dbDir = new File(config.getDbpath());
        System.out.println("Chemin absolu : " + dbDir.getAbsolutePath());
        System.out.println("Existe : " + dbDir.exists());
        System.out.println("Est un dossier : " + dbDir.isDirectory());
        System.out.println("Accessible en lecture : " + dbDir.canRead());
        System.out.println("Accessible en écriture : " + dbDir.canWrite());
        System.out.println();
    }
    
    /**
     * Gère les commandes DEBUG
     * @param parts Les parties de la commande
     */
    private void handleDebug(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage : DEBUG <sous-commande>");
            System.out.println("Sous-commandes disponibles : INFO");
            return;
        }
        
        String subCommand = parts[1].toUpperCase();
        
        switch (subCommand) {
            case "INFO":
                handleDebugInfo();
                break;
                
            default:
                System.out.println("Sous-commande DEBUG inconnue : " + subCommand);
                System.out.println("Sous-commandes disponibles : INFO");
                break;
        }
    }
    
    /**
     * Gère la commande DEBUG INFO
     */
    private void handleDebugInfo() {
        System.out.println("=== Informations de Debug ===");
        
        // Informations sur la JVM
        Runtime runtime = Runtime.getRuntime();
        System.out.println("--- Informations JVM ---");
        System.out.println("Version Java : " + System.getProperty("java.version"));
        System.out.println("Répertoire de travail : " + System.getProperty("user.dir"));
        System.out.println("Mémoire max : " + runtime.maxMemory() / 1024 / 1024 + " MB");
        System.out.println("Mémoire totale : " + runtime.totalMemory() / 1024 / 1024 + " MB");
        System.out.println("Mémoire libre : " + runtime.freeMemory() / 1024 / 1024 + " MB");
        System.out.println("Processeurs disponibles : " + runtime.availableProcessors());
        
        // Informations sur le système
        System.out.println("--- Informations Système ---");
        System.out.println("OS : " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("Architecture : " + System.getProperty("os.arch"));
        System.out.println("Utilisateur : " + System.getProperty("user.name"));
        
        // Informations sur la configuration
        System.out.println("--- Configuration SGBDR ---");
        System.out.println("DBPath : " + config.getDbpath());
        
        File dbDir = new File(config.getDbpath());
        System.out.println("DBPath existe : " + dbDir.exists());
        if (dbDir.exists()) {
            System.out.println("DBPath est un dossier : " + dbDir.isDirectory());
            System.out.println("DBPath permissions : R=" + dbDir.canRead() + 
                             " W=" + dbDir.canWrite() + " X=" + dbDir.canExecute());
            
            if (dbDir.isDirectory()) {
                File[] files = dbDir.listFiles();
                System.out.println("Nombre de fichiers dans DBPath : " + 
                                 ((files != null) ? files.length : "Erreur lecture"));
            }
        }
        
        System.out.println();
    }
}