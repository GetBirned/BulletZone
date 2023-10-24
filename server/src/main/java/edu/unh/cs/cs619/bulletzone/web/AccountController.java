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

import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;
import edu.unh.cs.cs619.bulletzone.repository.DataRepository;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;

@RestController
@RequestMapping(value = "/games/account")
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final DataRepository data;

    @Autowired
    public AccountController(DataRepository repo) {
        this.data = repo;
    }

    /**
     * Handles a PUT request to register a new user account
     *
     * @param name The username
     * @param password The password
     * @return a response w/ success boolean
     */
    @RequestMapping(method = RequestMethod.PUT, value = "register/{name}/{password}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<BooleanWrapper> register(@PathVariable String name, @PathVariable String password)
    {
        // Log the request
        log.debug("Register '" + name + "' with password '" + password + "'");
        // Return the response (true if account created)
        boolean res = false;
        if((data.validateUser(name, password, true)) == null) {
            res = true;
        }
        return new ResponseEntity<BooleanWrapper>(new BooleanWrapper(
                //TODO: something that invokes users.createUser(name, password) and does
                     // other setup in the DataRepository (actually calls data.validateUser(...))
                 res
                ),
                HttpStatus.CREATED);
    }

    /**
     * Handles a PUT request to login a user
     *
     * @param name The username
     * @param password The password
     * @return a response w/ the user ID (or -1 if invalid)
     */
    @RequestMapping(method = RequestMethod.PUT, value = "login/{name}/{password}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<LongWrapper> login(@PathVariable String name, @PathVariable String password)
    {
        // Log the request
        log.debug("Login '" + name + "' with password '" + password + "'");

        GameUser user = data.validateUser(name, password, true);


        if (user != null) {
            // Return the response with the user's ID if login is valid
            return new ResponseEntity<>(new LongWrapper(user.getId()), HttpStatus.OK);
        } else {
            // Return a response indicating that the login is not valid (e.g., -1 or another error code)
            return null;
        }

    }

}
