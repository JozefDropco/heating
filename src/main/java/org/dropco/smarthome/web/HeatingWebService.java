package org.dropco.smarthome.web;

import com.google.gson.Gson;
import org.dropco.smarthome.heating.solar.BoilerBlocker;
import org.dropco.smarthome.heating.solar.SolarCircularPump;
import org.dropco.smarthome.heating.solar.ThreeWayValve;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.logging.Logger;

@Path("/ws/heating")
public class HeatingWebService {
    private static final Logger logger = Logger.getLogger(HeatingWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response stats(@QueryParam("fromDate") String fromString, @QueryParam("toDate") String toString) throws ParseException {
        FullStats fullStats = new FullStats();
        fullStats.solarCircularPump = SolarCircularPump.getState();
        fullStats.heatingBoilerBlock = BoilerBlocker.getState();
        fullStats.threeWayBypass = ThreeWayValve.getState()==false;
        fullStats.threeWayOpened = ThreeWayValve.getState();
        return Response.ok(new Gson().toJson(fullStats)).build();
    }

    private class FullStats {
        boolean solarCircularPump;
        boolean heatingBoilerBlock;
        boolean threeWayBypass;
        boolean threeWayOpened;
    }

}
