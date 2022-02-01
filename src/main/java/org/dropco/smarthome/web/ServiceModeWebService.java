package org.dropco.smarthome.web;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.watering.ServiceMode;
import org.dropco.smarthome.web.dto.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public abstract class ServiceModeWebService {
    private static final Map<String, String> urlMap = ImmutableMap.<String, String>builder()
            .put("GPIO 0", "https://pinout.xyz/pinout/pin11_gpio17")
            .put("GPIO 1", "https://pinout.xyz/pinout/pin12_gpio18")
            .put("GPIO 2", "https://pinout.xyz/pinout/pin13_gpio27")
            .put("GPIO 3", "https://pinout.xyz/pinout/pin15_gpio22")
            .put("GPIO 4", "https://pinout.xyz/pinout/pin16_gpio23")
            .put("GPIO 5", "https://pinout.xyz/pinout/pin18_gpio24")
            .put("GPIO 6", "https://pinout.xyz/pinout/pin22_gpio25")
            .put("GPIO 7", "https://pinout.xyz/pinout/pin7_gpio4")
            .put("GPIO 8", "https://pinout.xyz/pinout/pin3_gpio2")
            .put("GPIO 9", "https://pinout.xyz/pinout/pin5_gpio3")
            .put("GPIO 10", "https://pinout.xyz/pinout/pin24_gpio8")
            .put("GPIO 11", "https://pinout.xyz/pinout/pin26_gpio7")
            .put("GPIO 12", "https://pinout.xyz/pinout/pin19_gpio10")
            .put("GPIO 13", "https://pinout.xyz/pinout/pin21_gpio9")
            .put("GPIO 14", "https://pinout.xyz/pinout/pin23_gpio11")
            .put("GPIO 15", "https://pinout.xyz/pinout/pin8_gpio14")
            .put("GPIO 16", "https://pinout.xyz/pinout/pin10_gpio15")
            .put("GPIO 21", "https://pinout.xyz/pinout/pin29_gpio5")
            .put("GPIO 22", "https://pinout.xyz/pinout/pin31_gpio6")
            .put("GPIO 23", "https://pinout.xyz/pinout/pin33_gpio13")
            .put("GPIO 24", "https://pinout.xyz/pinout/pin35_gpio19")
            .put("GPIO 25", "https://pinout.xyz/pinout/pin37_gpio26")
            .put("GPIO 26", "https://pinout.xyz/pinout/pin32_gpio12")
            .put("GPIO 27", "https://pinout.xyz/pinout/pin36_gpio16")
            .put("GPIO 28", "https://pinout.xyz/pinout/pin38_gpio20")
            .put("GPIO 29", "https://pinout.xyz/pinout/pin40_gpio21")
            .put("GPIO 30", "https://pinout.xyz/pinout/pin27_gpio0")
            .put("GPIO 31", "https://pinout.xyz/pinout/pin28_gpio1")
            .build();

    @GET
    @Path("/serviceMode")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceModeState() {
        return Response.ok(getServiceModeJson(getServiceMode())).build();
    }


    @POST
    @Path("/serviceMode")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setServiceModeState(@QueryParam("state") boolean state) {
       setServiceMode(state);
        return Response.ok(getServiceModeJson(getServiceMode())).build();
    }
    String getServiceModeJson(boolean state) {
        return new Gson().toJson(new org.dropco.smarthome.web.dto.ServiceMode(state));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/port/inputs")
    public Response inputs() {
        Set<NamedPort> inputs = getInputs();
        return Db.applyDao(new SettingsDao(), dao -> Response.ok(new Gson().toJson(FluentIterable.from(inputs).transform(port -> {
            boolean isHigh = getInputState(port.getRefCd());
            String url = Optional.ofNullable(dao.getString(port.getRefCd())).map(urlMap::get).orElse("#");
            return new Port(port.getRefCd(), url, port.getName(), Boolean.toString(isHigh));
        }).toList())).build());
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/port/outputs")
    public Response outputs() {
        Set<NamedPort> outputs = getOutputs();
        return Db.applyDao(new SettingsDao(), dao -> Response.ok(new Gson().toJson(FluentIterable.from(outputs).transform(port -> {
            boolean state = getOutputState(port.getRefCd());
            String url = Optional.ofNullable(dao.getString(port.getRefCd())).map(urlMap::get).orElse("#");
            return new Port(port.getRefCd(), url, port.getName(), Boolean.toString(state));
        }).toList())).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("port/output/{refCd}")
    public Response setValue(@PathParam("refCd") String refCd, String value) throws InterruptedException {
        //1.check if its output
        boolean match = FluentIterable.from(getOutputs()).anyMatch(port -> port.getRefCd().equals(refCd));
        if (!match) return Response.status(Response.Status.BAD_REQUEST).build();
        //3. Now we can enable it
        Set<String> mutualExclussion = setOutputState(refCd, new Gson().fromJson(value, Boolean.class));
        return Response.ok(new Gson().toJson(mutualExclussion)).build();
    }

    protected abstract boolean getServiceMode();

    protected abstract void setServiceMode(boolean state);

    protected abstract Set<NamedPort> getInputs();

    protected abstract boolean getInputState(String portRefCd);

    protected abstract Set<NamedPort> getOutputs();

    protected abstract boolean getOutputState(String portRefCd);

    protected abstract Set<String> setOutputState(String portRefCd, boolean state);

}
