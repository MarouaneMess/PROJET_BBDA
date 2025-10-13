import java.io.IOException;
import java.util.*;

/**
 * BufferManager: gère un pool de buffers (frames) et applique une politique de remplacement LRU/MRU.
 * API minimale demandée par le TP3.
 */
public class BufferManager {
    public enum Policy { LRU, MRU }

    public static class Frame {
        PageId pageId;        // page chargée ou null si frame libre
        byte[] data;          // contenu de la page
        int pinCount = 0;     // nombre de pins
        boolean dirty = false;// flag dirty
        long lastTouch = 0;   // timestamp pour LRU/MRU
    }

    private final DBConfig cfg;
    private final DiskManager dm;
    private Policy policy;
    private final Frame[] frames;
    private final Map<PageId, Integer> pageTable = new HashMap<>(); // PageId -> frame index

    public BufferManager(DBConfig cfg, DiskManager dm) {
        this.cfg = cfg;
        this.dm = dm; // do NOT copy
        this.policy = parsePolicy(cfg.getBm_policy());
        int n = Math.max(1, cfg.getBm_buffercount());
        this.frames = new Frame[n];
        for (int i = 0; i < n; i++) {
            frames[i] = new Frame();
            frames[i].data = new byte[cfg.getPagesize()];
        }
    }

    private Policy parsePolicy(String pol) {
        if (pol == null) return Policy.LRU;
        String p = pol.trim().toUpperCase();
        if (p.equals("MRU")) return Policy.MRU;
        return Policy.LRU;
    }

    public synchronized byte[] GetPage(PageId pageId) throws IOException {
        // Si déjà en cache
        Integer idx = pageTable.get(pageId);
        if (idx != null) {
            Frame f = frames[idx];
            f.pinCount++;
            f.lastTouch = System.nanoTime();
            return f.data;
        }

        // Sinon, chercher une frame libre
        int freeIdx = findFreeFrame();
        if (freeIdx == -1) {
            // appliquer la politique de remplacement
            freeIdx = selectVictim();
            if (freeIdx == -1) throw new IOException("Aucune frame éjectable (toutes pinCount>0)");
            evict(freeIdx);
        }

        // Charger la page
        Frame f = frames[freeIdx];
        Arrays.fill(f.data, (byte)0);
        dm.ReadPage(pageId, f.data);
        f.pageId = pageId;
        f.pinCount = 1;
        f.dirty = false;
        f.lastTouch = System.nanoTime();
        pageTable.put(pageId, freeIdx);
        return f.data;
    }

    public synchronized void FreePage(PageId pageId, boolean valdirty) {
        Integer idx = pageTable.get(pageId);
        if (idx == null) return; // page pas en cache
        Frame f = frames[idx];
        if (valdirty) f.dirty = true;
        if (f.pinCount > 0) f.pinCount--;
        f.lastTouch = System.nanoTime();
        // pas d'appel DiskManager ici
    }

    public synchronized void SetCurrentReplacementPolicy(String policy) {
        this.policy = parsePolicy(policy);
    }

    public synchronized void FlushBuffers() throws IOException {
        for (int i = 0; i < frames.length; i++) {
            Frame f = frames[i];
            if (f.pageId != null) {
                if (f.dirty) {
                    dm.WritePage(f.pageId, f.data);
                }
                // reset
                f.pageId = null;
                f.pinCount = 0;
                f.dirty = false;
                f.lastTouch = 0;
                Arrays.fill(f.data, (byte)0);
            }
        }
        pageTable.clear();
    }

    private int findFreeFrame() {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].pageId == null && frames[i].pinCount == 0) return i;
        }
        return -1;
    }

    private int selectVictim() {
        int victim = -1;
        long best = (policy == Policy.LRU) ? Long.MAX_VALUE : Long.MIN_VALUE;
        for (int i = 0; i < frames.length; i++) {
            Frame f = frames[i];
            if (f.pinCount > 0 || f.pageId == null) continue; // cannot evict pinned or empty
            if (policy == Policy.LRU) {
                if (f.lastTouch < best) { best = f.lastTouch; victim = i; }
            } else { // MRU
                if (f.lastTouch > best) { best = f.lastTouch; victim = i; }
            }
        }
        return victim;
    }

    private void evict(int idx) throws IOException {
        Frame f = frames[idx];
        if (f.dirty && f.pageId != null) {
            dm.WritePage(f.pageId, f.data);
        }
        if (f.pageId != null) pageTable.remove(f.pageId);
        f.pageId = null;
        f.pinCount = 0;
        f.dirty = false;
        f.lastTouch = 0;
        Arrays.fill(f.data, (byte)0);
    }

    // Helpers for tests/inspection
    public synchronized int getBufferCount() { return frames.length; }
    public synchronized Frame getFrame(int i) { return frames[i]; }
    public synchronized String getPolicy() { return policy.name(); }
}
