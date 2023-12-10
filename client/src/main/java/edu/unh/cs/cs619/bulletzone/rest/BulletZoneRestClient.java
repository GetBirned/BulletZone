package edu.unh.cs.cs619.bulletzone.rest;

import org.androidannotations.rest.spring.annotations.Delete;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Post;
import org.androidannotations.rest.spring.annotations.Put;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.api.RestClientErrorHandling;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;

import edu.unh.cs.cs619.bulletzone.util.ArrayListWrapper;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.IntegerWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;

/** "http://stman1.cs.unh.edu:6191/games"
 * "http://10.0.0.145:6191/games"
 * http://10.0.2.2:8080/
 * Created by simon on 10/1/14.
 */
//
@Rest(rootUrl = "http://10.21.113.247:6011/games",
//@Rest(rootUrl = "http://10.21.113.247:6099/games",


        converters = {StringHttpMessageConverter.class, MappingJackson2HttpMessageConverter.class}
        // TODO: disable intercepting and logging
        // , interceptors = { HttpLoggerInterceptor.class }
)
public interface BulletZoneRestClient extends RestClientErrorHandling {
    void setRootUrl(String rootUrl);

    @Post("")
    LongWrapper join() throws RestClientException;

    @Get("")
    GridWrapper grid();

    @Put("/account/register/{username}/{password}")
    BooleanWrapper register(@Path String username, @Path String password);

    @Put("/account/login/{username}/{password}")
    LongWrapper login(@Path String username, @Path String password);

    @Put("/{tankId}/move/{direction}")
    BooleanWrapper move(@Path long tankId, @Path byte direction);

    @Put("/{tankId}/turn/{direction}")
    BooleanWrapper turn(@Path long tankId, @Path byte direction);

    @Put("/{tankId}/fire/1")
    BooleanWrapper fire(@Path long tankId);

    @Post("/setTankPowerup/{tankId}/{powerupValue}/{isTank}")
    void setTankPowerup(@Path long tankId, @Path int powerupValue, @Path boolean isTank);

    @Delete("/{tankId}/leave")
    BooleanWrapper leave(@Path long tankId);

    @Post("/soldier/{tankId}")
    LongWrapper deploySoldier(@Path long tankId);

    @Put("/{accountId}/updateBankAccount/{numCoins}")
    BooleanWrapper updateBankAccount(@Path long accountId, @Path int numCoins);

    @Get("/{tankId}/getHealth")
    LongWrapper getHealth(@Path long tankId);

    @Get("/{tankId}/getPowerups/{isTank}")
    ArrayListWrapper getPowerups(@Path long tankId, @Path boolean isTank);

    @Get("/{tankId}/getSoldierHealth")
    LongWrapper getSoldierHealth(@Path long tankId);

    @Get("/{tankId}/getBuilderHealth")
    LongWrapper getBuilderHealth(@Path long tankId);

    @Post("/builder/{tankId}")
    LongWrapper controlBuilder(@Path long tankId);

    @Post("/tank/{tankId}")
    LongWrapper controlTank(@Path long tankId);

    @Post("/dismantleImprovement/{tankId}")
    LongWrapper dismantleImprovement(@Path long tankId);

    @Post("/buildImprovement/{choice}/{tankId}")
    LongWrapper buildImprovement(@Path int choice,@Path long tankId);

    @Post("/buildTrap/{choice}/{tankId}")
    LongWrapper buildTrap(@Path int choice,@Path long tankId);

    @Get("/{tankId}/ejectPowerup/{isTank}")
    LongWrapper ejectPowerup(@Path long tankId, @Path boolean isTank);

    @Post("/account/updateBalance/{username}/{amount}")
    BooleanWrapper updateBalance(@Path String username, @Path double amount);

    @Get("/account/getBalance/{username}")
    IntegerWrapper getBalance(@Path String username);

    @Post("/buildTime/{tankId}")
    LongWrapper getBuildTime(@Path long tankId);

    @Post("/dismantleTime/{tankId}")
    LongWrapper getDismantleTime(@Path long tankId);
}
