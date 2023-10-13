package edu.unh.cs.cs619.bulletzone.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.unh.cs.cs619.bulletzone.repository.DataRepository;
import edu.unh.cs.cs619.bulletzone.util.StringArrayWrapper;

@RestController
@RequestMapping(value = "/games/catalog")
public class CatalogController {
    private static final Logger log = LoggerFactory.getLogger(CatalogController.class);

    private final DataRepository data;

    @Autowired
    public CatalogController(DataRepository repo) {
        this.data = repo;
    }

    /**
     * Handles a GET request to return all category
     * @return a response includes String array
     */
    @RequestMapping(method = RequestMethod.GET, value = "getCategories", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<StringArrayWrapper> getCategories(){
        // Log the request
        log.debug("getCategories");
        // Return the response (list of strings)
        /*
        return new ResponseEntity<StringArrayWrapper>(new StringArrayWrapper(
                TODO: something that invokes types.getCategories() in the DataRepository
                ),
                HttpStatus.OK);
         */
        return null;
    }

    /**
     * Handles a GET request to return different component types in a given category
     * @param category The category
     * @return a response includes String array
     */
    @RequestMapping(method = RequestMethod.GET, value = "getTypes/{category}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<StringArrayWrapper> getTypes(@PathVariable String category){
        // Log the request
        log.debug("getTypes '" + category + "'");
        // Return the response (list of strings)
        /*
        return new ResponseEntity<StringArrayWrapper>(new StringArrayWrapper(
                TODO: something that invokes types.get*() in the DataRepository
                ),
                HttpStatus.OK);
         */
        return null;
    }
}
