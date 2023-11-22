package edu.unh.cs.cs619.bulletzone.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;


import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.repository.GameRepository;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;
import edu.unh.cs.cs619.bulletzone.util.ArrayListWrapper;

@RestController
@RequestMapping(value = "/games")
class GamesController {

    private static final Logger log = LoggerFactory.getLogger(GamesController.class);

    private final GameRepository gameRepository;

    @Autowired
    public GamesController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @RequestMapping(method = RequestMethod.POST, value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity<LongWrapper> join(HttpServletRequest request) {
        Tank tank;
        try {
            tank = gameRepository.join(request.getRemoteAddr());
            log.info("Player joined: tankId={} IP={}", tank.getId(), request.getRemoteAddr());

            return new ResponseEntity<LongWrapper>(
                    new LongWrapper(tank.getId()),
                    HttpStatus.CREATED
            );
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    ResponseEntity<GridWrapper> grid() {
        return new ResponseEntity<GridWrapper>(new GridWrapper(gameRepository.getGrid()), HttpStatus.OK);
    }

//    @RequestMapping(method = RequestMethod.GET, value = "", produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.OK)
//    public
//    @ResponseBody
//    ResponseEntity<GridEventListWrapper> event(Timestamp timestamp) {
//        return new ResponseEntity<GridEventListWrapper>(new GridEventListWrapper(gameRepository.getHistory(timestamp)), HttpStatus.OK);
//    }


    @RequestMapping(method = RequestMethod.GET, value = "{tankId}/getPowerups/{isTank}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    ResponseEntity<ArrayListWrapper> getPowerups(@PathVariable long tankId, @PathVariable boolean isTank) {

        if(isTank){
            return new ResponseEntity<ArrayListWrapper>(new ArrayListWrapper(gameRepository.getTankPowerups(tankId)), HttpStatus.OK);
        } else {
            return new ResponseEntity<ArrayListWrapper>(new ArrayListWrapper(gameRepository.getSoldierPowerups(tankId)), HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{tankId}/turn/{direction}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> turn(@PathVariable long tankId, @PathVariable byte direction)
            throws TankDoesNotExistException, LimitExceededException, IllegalTransitionException {
        return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.turn(tankId, Direction.fromByte(direction))),
                HttpStatus.OK
        );
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{tankId}/move/{direction}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> move(@PathVariable long tankId, @PathVariable byte direction)
            throws TankDoesNotExistException, LimitExceededException, IllegalTransitionException {
        return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.move(tankId, Direction.fromByte(direction))),
                HttpStatus.OK
        );
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{tankId}/fire", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> fire(@PathVariable long tankId)
            throws TankDoesNotExistException, LimitExceededException {
        try {
            return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.fire(tankId, 1)),
                HttpStatus.OK
            );
        } catch (IllegalTransitionException e) {
            return new ResponseEntity<BooleanWrapper>(new BooleanWrapper(false), HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{tankId}/fire/{bulletType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> fire(@PathVariable long tankId, @PathVariable int bulletType)
            throws TankDoesNotExistException, LimitExceededException {
        try {
            return new ResponseEntity<BooleanWrapper>(
                    new BooleanWrapper(gameRepository.fire(tankId, bulletType)),
                    HttpStatus.OK
            );
        } catch (IllegalTransitionException e) {
            return new ResponseEntity<BooleanWrapper>(new BooleanWrapper(false), HttpStatus.OK);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "{tankId}/leave", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    HttpStatus leave(@PathVariable long tankId)
            throws TankDoesNotExistException {
        //System.out.println("Games Controller leave() called, tank ID: "+tankId);
        gameRepository.leave(tankId);
        return HttpStatus.OK;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleBadRequests(Exception e) {
        return e.getMessage();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/soldier/{tankId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity<LongWrapper> deploySoldier(@PathVariable long tankId) {
        LongWrapper soldierId = gameRepository.deploySoldier(tankId);
        return new ResponseEntity<LongWrapper>(
                new LongWrapper(soldierId.getResult()),
                HttpStatus.CREATED
        );
    }
    @PostMapping(value = "/setTankPowerup/{tankId}/{powerupValue}/{isTank}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    void setTankPowerup(@PathVariable long tankId, @PathVariable int powerupValue, @PathVariable boolean isTank) {
        try {
            log.debug("setTankPowerup called with tankId: {} and powerupType: {}", tankId, powerupValue);

            // Call the method to set the tank's powerup
            if(isTank) {
                gameRepository.setTankPowerup(tankId, powerupValue);
            } else{
                gameRepository.setSoldierPowerup(tankId,powerupValue);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequestMapping(method = RequestMethod.POST, value = "{tankId}/updateLife", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void updateLife(@PathVariable long tankId, int newLife) throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        // Find the tank with tankId and update its life
       gameRepository.updateLife(tankId, newLife);
    }

    @RequestMapping(method = RequestMethod.GET, value = "{tankId}/getHealth", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Long> getHealth(@PathVariable long tankId) {
        try {
            int health = gameRepository.getHealth(tankId);
            return new ResponseEntity<>((long) health, HttpStatus.OK);
        } catch (Exception e) {
            // Handle exceptions if necessary
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "{tankId}/getSoldierHealth", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<Long> getSoldierHealth(@PathVariable long tankId) {
        try {
            // Add logging statement to print the incoming tankId
            System.out.println("Received request for tankId: " + tankId);

            int health = gameRepository.getSoldierHealth(tankId);

            // Add logging statement to print the retrieved health
            System.out.println("Retrieved health for tankId " + tankId + ": " + health);

            return new ResponseEntity<>((long) health, HttpStatus.OK);
        } catch (Exception e) {
            // Add logging statement to print exceptions
            e.printStackTrace();

            // Handle exceptions if necessary
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
