public class RecordId {
    private PageId pageId; // Identifiant de la page où se trouve le record
    private int slotIdx;   // Indice du slot dans la page

    public RecordId(PageId pageId, int slotIdx) {
        this.pageId = pageId;
        this.slotIdx = slotIdx;
    }

    public PageId getPageId() {
        return pageId;
    }

    public int getSlotIdx() {
        return slotIdx;
    }

    public void setPageId(PageId pageId) {
        this.pageId = pageId;
    }

    public void setSlotIdx(int slotIdx) {
        this.slotIdx = slotIdx;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RecordId other = (RecordId) obj;
        return slotIdx == other.slotIdx &&
            (pageId != null && pageId.equals(other.pageId));
    }

    @Override
    public int hashCode() {
        int result = (pageId != null) ? pageId.hashCode() : 0;
        result = 31 * result + slotIdx;
        return result;
    }

    @Override
    public String toString() {
        return "RecordId{pageId=" + pageId + ", slotIdx=" + slotIdx + '}';
    }
}
