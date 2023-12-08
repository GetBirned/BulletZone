package edu.unh.cs.cs619.bulletzone.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import edu.unh.cs.cs619.bulletzone.datalayer.BulletZoneData;
import edu.unh.cs.cs619.bulletzone.datalayer.account.BankAccount;
import edu.unh.cs.cs619.bulletzone.datalayer.user.GameUser;
import edu.unh.cs.cs619.bulletzone.repository.DataRepository;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.IntegerWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;

@RestController
@RequestMapping(value = "/games/account")
public class AccountController {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final DataRepository data;

    public BankAccount getAccountByUsername(String username) {
        Collection<GameUser> users = data.bzdata.users.getUsers();
        for (GameUser user : users) {
            if (username.equals(user.getUsername())) {
                Collection<BankAccount> accounts = user.getOwnedAccounts();
                // Assuming a user can have only one associated BankAccount, you can return the first one
                // Adjust this logic based on your actual requirements
                return accounts.isEmpty() ? null : accounts.iterator().next();
            }
        }
        return null; // Return null if no user is found for the given username
    }


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
        boolean res = true;
        if((data.validateUser(name, password, true)) == null) {
            log.debug("MADE IT TO IF STATEMENT");

            res = false;

        }
        log.debug("I AM RETURNING THIS " + res);


        GameUser user = data.validateUser(name, password, true);

        BankAccount account = data.bzdata.accounts.create();
        data.bzdata.permissions.setOwner(account, user);
        data.bzdata.accounts.modifyBalance(account, 1000);
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

        // Create a new user and get the associated BankAccount

        //TODO check if the user has a bank account using the bakAccRepo getAccount

        if(getAccountByUsername(name) == null) {
            BankAccount account = data.bzdata.accounts.create();
            data.bzdata.permissions.setOwner(account, user);
            data.bzdata.accounts.modifyBalance(account, 1000);
        }
        //BankAccount account = data.bzdata.accounts.create();
        //data.bzdata.permissions.setOwner(account, user);


        if (user != null) {
            // Return the response with the user's ID if login is valid
            return new ResponseEntity<>(new LongWrapper(user.getId()), HttpStatus.OK);
        } else {
            // Return a response indicating that the login is not valid (e.g., -1 or another error code)
            return null;
        }

    }

    @PostMapping(value = "/updateBankAccount/{accountId}/{powerupValue}/{numCoins}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    void updateBankAccount (@PathVariable long accountId, @PathVariable int numCoins) {
        try {
            //log.debug("setTankPowerup called with tankId: {} and powerupType: {}", tankId, powerupValue);

            // Call the method to set the tank's powerup



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "/updateBalance/{username}/{amount}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<BooleanWrapper> updateBalance(@PathVariable String username, @PathVariable double amount) {
        // Find the user's BankAccount
        BankAccount account = getAccountByUsername(username);

        if (account != null) {
            // Update the balance
            boolean success = data.bzdata.accounts.modifyBalance(account, amount);

            if (success) {log.debug("Updated balance!!!!!!!!!!!!!!!!!!!!!!!!!!1");
                return new ResponseEntity<>(new BooleanWrapper(true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new BooleanWrapper(false), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(new BooleanWrapper(false), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get the balance of a user's BankAccount.
     *
     * @param username The username
     * @return a response containing the balance if successful, or an error response
     */
    @GetMapping(value = "/getBalance/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<IntegerWrapper> getBalance(@PathVariable String username) {
        // Find the user's BankAccount
        BankAccount account = getAccountByUsername(username);

        if (account != null) {
            // Get the balance
            double balance = account.getBalance();
            return new ResponseEntity<>(new IntegerWrapper((int)balance), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new IntegerWrapper(-1), HttpStatus.NOT_FOUND);
        }
    }


}
