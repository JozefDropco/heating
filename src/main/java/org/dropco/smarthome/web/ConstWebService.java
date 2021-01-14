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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws ParseException {
        SettingsDao dao = new SettingsDao();
        return Response.ok(new Gson().toJson(dao.readAll())).build();
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@QueryParam("type") String type, String payload) {
        SettingsDao dao = new SettingsDao();
        switch (type) {
            case "long":
                LongConstant longConstant = new Gson().fromJson(payload, LongConstant.class);
                dao.updateLongConstant(longConstant);
                break;
            case "double":
                DoubleConstant doubleConst = new Gson().fromJson(payload, DoubleConstant.class);
                dao.updateDoubleConstant(doubleConst);
                break;
            case "string":
                StringConstant stringConstant = new Gson().fromJson(payload, StringConstant.class);
                dao.updateStringConstant(stringConstant);
                break;
        }
         return Response.ok().build();
    }


}
