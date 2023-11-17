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

import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;

/** "http://stman1.cs.unh.edu:6191/games"
 * "http://10.0.0.145:6191/games"
 * http://10.0.2.2:8080/
 * Created by simon on 10/1/14.
 */

//@Rest(rootUrl = "http://10.21.159.62:8080/games",
@Rest(rootUrl = "http://10.21.128.60:6197/games",
//@Rest(rootUrl = "http://stman1.cs.unh.edu:61907/games",
    //"http://10.21.128.183:6197/games"
//@Rest(rootUrl = "http://stman1.cs.unh.edu:61907/games",
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

    @Post("/{tankId}/setPowerup/{powerupValue}")
    void setTankPowerup(@Path long tankId, @Path int powerupValue);

    @Delete("/{tankId}/leave")
    BooleanWrapper leave(@Path long tankId);

    @Post("/soldier/{tankId}")
    LongWrapper deploySoldier(@Path long tankId);

    @Put("/{tankId}/updateLife/{newLife}")
    BooleanWrapper updateLife(@Path long tankId, @Path int newLife);

    @Get("/{tankId}/getHealth")
    LongWrapper getHealth(@Path long tankId);
}
