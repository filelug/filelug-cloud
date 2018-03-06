package org.clopuccino.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * <code></code>
 *
 * @author masonhsieh
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseListBookmarksAndRootsModel extends ResponseModel {

    @JsonProperty("bookmarks")
    private List<Bookmark> bookmarks;

    @JsonProperty("roots")
    private List<String> roots;

    public ResponseListBookmarksAndRootsModel() {
        super();
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public void addBookmark(Bookmark bookmark) {
        if (bookmarks == null) {
            bookmarks = new ArrayList<>();
        }

        bookmarks.add(bookmark);
    }

    public void removeBookmark(Bookmark bookmark) {
        if (bookmarks != null && bookmarks.size() > 0) {
            bookmarks.remove(bookmark);
        }
    }

    public List<String> getRoots() {
        return roots;
    }

    public void setRoots(List<String> roots) {
        this.roots = roots;
    }

    public void addRoot(String root) {
        if (roots == null) {
            roots = new ArrayList<>();
        }

        roots.add(root);
    }

    public void removeRoot(String root) {
        if (roots != null && roots.size() > 0) {
            roots.remove(root);
        }
    }
}
