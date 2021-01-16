package org.dropco.smarthome.web;

import com.google.gson.Gson;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.DoubleConstant;
import org.dropco.smarthome.dto.LongConstant;
import org.dropco.smarthome.dto.StringConstant;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.logging.Logger;

@Path("/ws/const")
public class ConstWebService {
    private static final Logger logger = Logger.getLogger(ConstWebService.class.getName());
    public static SettingsDao SETTINGS_DAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws ParseException {
        return Response.ok(new Gson().toJson(SETTINGS_DAO.readAll())).build();
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@QueryParam("type") String type, String payload) {
        switch (type) {
            case "long":
                LongConstant longConstant = new Gson().fromJson(payload, LongConstant.class);
                SETTINGS_DAO.updateLongConstant(longConstant);
                break;
            case "double":
                DoubleConstant doubleConst = new Gson().fromJson(payload, DoubleConstant.class);
                SETTINGS_DAO.updateDoubleConstant(doubleConst);
                break;
            case "string":
                StringConstant stringConstant = new Gson().fromJson(payload, StringConstant.class);
                SETTINGS_DAO.updateStringConstant(stringConstant);
                break;
        }
         return Response.ok().build();
    }


}
