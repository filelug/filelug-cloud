package org.clopuccino.servlet;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.clopuccino.Utility;
import org.clopuccino.domain.Bookmark;
import org.clopuccino.domain.HierarchicalModelType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <code>BookmarkResourceTest</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class BookmarkResourceTest extends AbstractResourceTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(BookmarkResourceTest.class.getSimpleName());

    // Do extends the super in this method
    @Before
    public void init() throws Exception {
        super.init();
    }

    /**
     * List created bookmarks:
     * <p/>
     * Documents   {"id":1385035260851,"path":"/Users/masonhsieh/Documents","label":"Documents","userId":"benius@gmail.com","type":"DIRECTORY"}
     * Books       {"id":1385035260960,"path":"/Users/masonhsieh/Google Drive/book","label":"Books","userId":"benius@gmail.com","type":"DIRECTORY"}
     * Movies      {"id":1385035261010,"path":"/Users/masonhsieh/Music/iTunes/iTunes Media/Home Videos","label":"Movies","userId":"benius@gmail.com","type":"DIRECTORY"}
     * Pictures    {"id":1385035261054,"path":"/Users/masonhsieh/Pictures/screensaver_babies","label":"Pictures","userId":"benius@gmail.com","type":"DIRECTORY"}
     * iPhoto 2012 {"id":1385035261095,"path":"/Users/masonhsieh/Pictures/iPhoto/iPhoto Library 2012","label":"iPhoto 2012","userId":"benius@gmail.com","type":"DIRECTORY"}
     * Music       {"id":1385035261133,"path":"/Users/masonhsieh/Music/iTunes/iTunes Media/Music","label":"Music","userId":"benius@gmail.com","type":"DIRECTORY"}
     * 鞦韆飛車     {"id":1385035261196,"path":"/Users/masonhsieh/Pictures/iPhoto/iPhoto Library 2013.photolibrary/Masters/2013/11/11/20131111-163018/IMG_2712.MOV","label":"鞦韆飛車","userId":"benius@gmail.com","type":"FILE"}
     *
     */
    @Test
    public void testSynchronizeBookmarks() throws Exception {
        List<Bookmark> bookmarks = new ArrayList<Bookmark>();

        Long id = 1385035260852L;
        String path = "/Users/masonhsieh/Documents";
        String label = "Documents";
        String userId = operatorId;

        bookmarks.add(new Bookmark(id, path, path, label, userId, HierarchicalModelType.DIRECTORY));

        id = 1385035260961L;
        path = "/Users/masonhsieh/Google Drive/book";
        label = "Books";

        bookmarks.add(new Bookmark(id, path, path, label, userId, HierarchicalModelType.DIRECTORY));

        id = 1385035261011L;
        path = "/Users/masonhsieh/Music/iTunes/iTunes Media/Home Videos";
        label = "Movies";

        bookmarks.add(new Bookmark(id, path, path, label, userId, HierarchicalModelType.DIRECTORY));

        id = 1385035261055L;
        path = "/Users/masonhsieh/Pictures/screensaver_babies";
        label = "Pictures";

        bookmarks.add(new Bookmark(id, path, path, label, userId, HierarchicalModelType.DIRECTORY));

        id = 1385035261096L;
        path = "/Users/masonhsieh/Pictures/iPhoto/iPhoto Library 2012";
        label = "iPhoto 2012";

        bookmarks.add(new Bookmark(id, path, path, label, userId, HierarchicalModelType.DIRECTORY));

        id = 1385035261134L;
        path = "/Users/masonhsieh/Music/iTunes/iTunes Media/Music";
        label = "Music";

        bookmarks.add(new Bookmark(id, path, path, label, userId, HierarchicalModelType.DIRECTORY));

        id = 1385035261197L;
        path = "/Users/masonhsieh/Pictures/iPhoto/iPhoto Library 2013.photolibrary/Masters/2013/11/11/20131111-163018/IMG_2712.MOV";
        label = "鞦韆飛車";

        bookmarks.add(new Bookmark(id, path, path, label, userId, HierarchicalModelType.DIRECTORY));

        synchronizeBookmarks(bookmarks);
    } // end testSynchronizeBookmarks()

    private void synchronizeBookmarks(List<Bookmark> bookmarks) throws Exception {
        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bookmarks);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String urlPath = "bookmarks/synchronize";

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Batch Created Bookmarks: " + responseString);

                break;
            case 401:
                LOGGER.info("Status 401(Unauthorized). Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    /**
     * List created bookmarks:
     * <p/>
     * Documents   {"id":1385035260851,"path":"/Users/masonhsieh/Documents","label":"Documents","userId":"benius@gmail.com","type":"DIRECTORY"}
     * Books       {"id":1385035260960,"path":"/Users/masonhsieh/Google Drive/book","label":"Books","userId":"benius@gmail.com","type":"DIRECTORY"}
     * Movies      {"id":1385035261010,"path":"/Users/masonhsieh/Music/iTunes/iTunes Media/Home Videos","label":"Movies","userId":"benius@gmail.com","type":"DIRECTORY"}
     * Pictures    {"id":1385035261054,"path":"/Users/masonhsieh/Pictures/screensaver_babies","label":"Pictures","userId":"benius@gmail.com","type":"DIRECTORY"}
     * iPhoto 2012 {"id":1385035261095,"path":"/Users/masonhsieh/Pictures/iPhoto/iPhoto Library 2012","label":"iPhoto 2012","userId":"benius@gmail.com","type":"DIRECTORY"}
     * Music       {"id":1385035261133,"path":"/Users/masonhsieh/Music/iTunes/iTunes Media/Music","label":"Music","userId":"benius@gmail.com","type":"DIRECTORY"}
     * 鞦韆飛車     {"id":1385035261196,"path":"/Users/masonhsieh/Pictures/iPhoto/iPhoto Library 2013.photolibrary/Masters/2013/11/11/20131111-163018/IMG_2712.MOV","label":"鞦韆飛車","userId":"benius@gmail.com","type":"FILE"}
     *
     */
    @Test
    public void testCreateBookmarks() throws Exception {
        String path = "/Users/masonhsieh/Documents";
        String label = "Documents";
        String userId = operatorId;

        createBookmark(path, label, userId);

        path = "/Users/masonhsieh/Google Drive/book";
        label = "Books";

        createBookmark(path, label, userId);

        path = "/Users/masonhsieh/Music/iTunes/iTunes Media/Home Videos";
        label = "Movies";

        createBookmark(path, label, userId);

        path = "/Users/masonhsieh/Pictures/screensaver_babies";
        label = "Pictures";

        createBookmark(path, label, userId);

        path = "/Users/masonhsieh/Pictures/iPhoto/iPhoto Library 2012";
        label = "iPhoto 2012";

        createBookmark(path, label, userId);

        path = "/Users/masonhsieh/Music/iTunes/iTunes Media/Music";
        label = "Music";

        createBookmark(path, label, userId);

        path = "/Users/masonhsieh/Pictures/iPhoto/iPhoto Library 2013.photolibrary/Masters/2013/11/11/20131111-163018/IMG_2712.MOV";
        label = "鞦韆飛車";

        createBookmark(path, label, userId);

        /* later for delete */
//        String path = "/Users/masonhsieh/Documents/Software";
//        String label = "Software";
//
//        createBookmark(path, label, userId);
    } // end testCreateBookmarks()

    private void createBookmark(String path, String label, String userId) throws Exception {
        final Bookmark bookmark = new Bookmark();
        bookmark.setPath(path);
        bookmark.setLabel(label);
        bookmark.setUserId(userId);

        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bookmark);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String urlPath = "bookmarks/new";

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Created Bookmark: " + responseString);

                break;
            case 400: // BAD_REQUEST, path not exists
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 401:
                LOGGER.info("Status 401(Unauthorized). Message: " + responseString);

                break;
            case 409: // CONFLICT, path or label duplicated
                LOGGER.info("Status 409. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    }

    @Test
    public void testListAllBookmarks() throws Exception {
        final String urlPath = "bookmarks";

        HttpResponse response = doGet(urlPath, sessionId);

        int status = response.getStatusLine().getStatusCode();

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        if (status == 200) {
            LOGGER.info("Found bookmark: " + responseString);
        } else {
            LOGGER.error("Error on finding all bookmarks. Status=" + status + "; Message=" + responseString);
        }
    }

    @Test
    public void testFindBookmarkById() throws Exception {
        final long id = 1385035261196L;
        final String urlPath = "bookmarks/find";

        ObjectMapper mapper = Utility.createObjectMapper();

        ObjectNode inputRootNode = mapper.createObjectNode();
        inputRootNode.put("id", id);

        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputRootNode);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        if (status == 200) {
            LOGGER.info("Found bookmark: " + responseString);
        } else {
            LOGGER.error("Error on finding bookmark by ID: " + id + "; Status=" + status + "; Message=" + responseString);

            assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    } // end testFindBookmarkById()

    @Test
    public void testUpdateBookmark() throws Exception {
        final Long id = 1384861259198L;
        final String path = "/Users/masonhsieh/Pictures/iPhoto/iPhoto Library 2013.photolibrary/Masters/2013/11/11/20131111-163018/IMG_2712.MOV";
        final String label = "盪鞦韆";

        final Bookmark bookmark = new Bookmark();
        bookmark.setId(id);
        bookmark.setPath(path);
        bookmark.setLabel(label);

        ObjectMapper mapper = Utility.createObjectMapper();
        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bookmark);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        String urlPath = "bookmarks/change";

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200: // OK
                LOGGER.info("Updated Bookmark: " + responseString);

                break;
            case 400: // BAD_REQUEST, path not exists
                LOGGER.info("Status 400. Message: " + responseString);

                break;
            case 401:
                LOGGER.info("Status 401(Unauthorized). Message: " + responseString);

                break;
            case 409: // CONFLICT, path or label duplicated
                LOGGER.info("Status 409. Message: " + responseString);

                break;
            default:
                LOGGER.error("Status not expected, " + status);

                assertTrue("Status=" + status + ", Info=" + responseString, status == 200);
        }
    } // end testUpdateBookmark()

    @Test
    public void testDeleteBookmarkById() throws Exception {
        /* {
         *      "id":1384862101812,
         *      "path":"/Users/masonhsieh/Documents/Software",
         *      "label":"Software",
         *      "userId":"benius@gmail.com",
         *      "type":"DIRECTORY"
         * }
         */
        final Long id = 1384862101812L;

        String urlPath = "bookmarks/delete";

        ObjectMapper mapper = Utility.createObjectMapper();

        ObjectNode inputRootNode = mapper.createObjectNode();
        inputRootNode.put("id", id);

        String inputJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputRootNode);

        /* DEBUG */
        LOGGER.info("Input JSON:\n" + inputJson);

        HttpResponse response = doPostJson(urlPath, inputJson, sessionId);

        int status = response.getStatusLine().getStatusCode();

        LOGGER.info("Status Message: " + response.getStatusLine().getReasonPhrase());

        String responseString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));

        switch (status) {
            case 200:
                LOGGER.info("Deleted Bookmark: " + responseString);

                Bookmark deletedBookmark = mapper.readValue(responseString, Bookmark.class);

                assertEquals("ID of the deleted bookmark not expected", id, deletedBookmark.getId());

                break;
            default:
                LOGGER.error("Error on deleting bookmark by ID: " + id + ". Status " + status + ". Message: " + responseString);
        }
    } // end testDeleteBookmarkById()
}
