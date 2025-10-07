/**
 * Identifiant d'une page : index du fichier (FileIdx) et index de page dans le fichier (PageIdx)
 */
public class PageId {
    private final int fileIdx;
    private final int pageIdx;

    public PageId(int fileIdx, int pageIdx) {
        this.fileIdx = fileIdx;
        this.pageIdx = pageIdx;
    }

    public int getFileIdx() {
        return fileIdx;
    }

    public int getPageIdx() {
        return pageIdx;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageId pageId = (PageId) o;
        return fileIdx == pageId.fileIdx && pageIdx == pageId.pageIdx;
    }

    @Override
    public int hashCode() {
        int result = fileIdx;
        result = 31 * result + pageIdx;
        return result;
    }

    @Override
    public String toString() {
        return "PageId{" + "fileIdx=" + fileIdx + ", pageIdx=" + pageIdx + '}';
    }
}
