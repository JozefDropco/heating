package org.dropco.smarthome.web;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.db.HeatingDao;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
                SettingsDao.LongConstant longConstant = new Gson().fromJson(payload, SettingsDao.LongConstant.class);
                dao.setLong(longConstant.refCd,longConstant.value);
                break;
            case "double":
                SettingsDao.DoubleConstant doubleConst = new Gson().fromJson(payload, SettingsDao.DoubleConstant.class);
                dao.setDouble(doubleConst.refCd,doubleConst.value);
                break;
            case "string":
                SettingsDao.StringConstant stringConstant = new Gson().fromJson(payload, SettingsDao.StringConstant.class);
                dao.setString(stringConstant.refCd,stringConstant.value);
                break;
        }
         return Response.ok().build();
    }

    @DELETE
    @Path("/measurePlace/{refCd}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMeasurePlace(@PathParam("refCd")String refCd) {
        new HeatingDao().deleteMeasurePlace(refCd);
        return Response.ok().build();
    }

    private String getRefCd(String refCode) {
        refCode = refCode.toUpperCase().replace(' ', '_').replace('-', '_');
        refCode = refCode.replaceAll("A", "");
        refCode = refCode.replaceAll("E", "");
        refCode = refCode.replaceAll("I", "");
        refCode = refCode.replaceAll("O", "");
        refCode = refCode.replaceAll("U", "");
        refCode = refCode.replaceAll("Y", "");
        refCode = refCode.replaceAll("_{2,}", "");
        if (refCode.endsWith("_")) return refCode.substring(0, refCode.length() - 1);
        return refCode;
    }

    private static class MeasurePlace{
        private String refCd;
        private String name;
        private String deviceId;
    }


    private static class  TempResult{
         Date lastDate;
        Collection<Series> series;
    }
    private static class Series{
        String name;
        String placeRefCd;
        List<Data> data= Lists.newArrayList();
    }

    private static class Data{
        Date x;
        double y;
    }


}
