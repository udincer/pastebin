package pastebin;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


/**
 * This is the meat of the app. It defines all the endpoints used by the frontend, including text content submission and
 * retrieval, also endpoints for file upload and download.
 *
 * Tevfik. August 28, 2015
 */

@Controller
@RequestMapping("/")
public class BinController {

    @Autowired
    ContentRepository repository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    /**
     * Serves the home page, containing minimal instructions.
     *
     * @return home.html
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getIndex() {
        return "home";
    }


    /**
     * Serves page content, based on token provided.
     *
     * @param model model for mustache
     * @param token string uniquely identifying resource
     * @return textviewer.htm;
     */
    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    public String getContent(Model model, @PathVariable String token) {
        TextContent content = repository.findByToken(token);
        List<IndexTuple> tuples = getFilesForToken(token);
        model.addAttribute("files", tuples);
        if (content == null) {
            model.addAttribute("content", "");
        } else {
            model.addAttribute("content", content.getContent());
        }

        return "textviewer";

    }

    /**
     * Handling POSTs for text content.
     *
     * @param token   string uniquely identifying resource
     * @param content TextContent object (Spring deserializes this)
     * @return Echo the content
     */
    @RequestMapping(value = "/{token}", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseBody
    public TextContent postContent(@PathVariable String token, @RequestBody TextContent content) {
        repository.save(content);
        return content;
    }

    /**
     * Handling POSTs for binary file content.
     *
     * @param token string uniquely identifying resource
     * @param file  MultipartFile form upload
     * @return Whether or not the file upload was successful
     */
    @RequestMapping(value = "/{token}/file", method = RequestMethod.POST)
    @ResponseBody
    public Object postFile(@PathVariable String token, @RequestParam("file") MultipartFile file) {
        String name = file.getOriginalFilename();
        try {
            DBObject metadata = new BasicDBObject();
            metadata.put("token", token);
            gridFsTemplate.store(file.getInputStream(), name, file.getContentType(), metadata).save();
        } catch (IOException e) {
            e.printStackTrace();
            return new Object() {
                public String status = "FAILURE";
            };
        }
        return new Object() {
            public String status = "SUCCESS";
        };
    }

    /**
     * Retrieve and download files for given index
     *
     * @param token string uniquely identifying resource
     * @param index index by upload order
     * @return File to be downloaded, or error status
     */
    @RequestMapping(value = "/{token}/file/{index}", method = RequestMethod.GET)
    public HttpEntity<byte[]> getFile(@PathVariable("token") String token, @PathVariable Integer index) {
        try {
            List<GridFSDBFile> list = getFiles(token);
            GridFSDBFile file = list.get(index);
            if (file != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                file.writeTo(os);
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, file.getContentType());
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFilename());
                return new HttpEntity<>(os.toByteArray(), headers);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.IM_USED);
        }
    }

    /**
     * Token comes in, list of filenames and indices come out
     *
     * @param token string uniquely identifying resource
     * @return List of tuples containing filename and index.
     */
    private List<IndexTuple> getFilesForToken(String token) {
        List<IndexTuple> tuples = new ArrayList<>();
        List<GridFSDBFile> files = gridFsTemplate.find(getTokenQuery(token));
        int i = 0;
        for (GridFSDBFile file : files) {
            tuples.add(new IndexTuple(file.getFilename(), i));
            i++;
        }
        return tuples;
    }

    /**
     * Token comes in, list of files come out
     *
     * @param token string uniquely identifying resource
     * @return List of files, in order
     */
    private List<GridFSDBFile> getFiles(String token) {
        return gridFsTemplate.find(getTokenQuery(token));
    }

    /**
     * Generates a Query for searching the GridFS metadata for token
     *
     * @param token string uniquely identifying resource
     * @return Query for searching the GridFS metadata for token
     */
    private static Query getTokenQuery(String token) {
        return Query.query(GridFsCriteria.whereMetaData("token").is(token));
    }

    /**
     * Model class containing filename and index tuples
     */
    public class IndexTuple {
        String filename;
        Integer ind;

        public IndexTuple(String filename, Integer ind) {
            this.filename = filename;
            this.ind = ind;
        }
    }

}
