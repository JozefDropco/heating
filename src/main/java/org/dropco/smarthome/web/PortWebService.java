package org.dropco.smarthome.web;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.WiringPiGpioProviderBase;
import com.pi4j.wiringpi.GpioUtil;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.web.dto.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/ws/port")
public class PortWebService {
    SettingsDao dao = new SettingsDao();
    private static final Map<String,String> urlMap = ImmutableMap.<String,String>builder()
            .put("GPIO 0","https://pinout.xyz/pinout/pin11_gpio17")
            .put("GPIO 1","https://pinout.xyz/pinout/pin12_gpio18")
            .put("GPIO 2","https://pinout.xyz/pinout/pin13_gpio27")
            .put("GPIO 3","https://pinout.xyz/pinout/pin15_gpio22")
            .put("GPIO 4","https://pinout.xyz/pinout/pin16_gpio23")
            .put("GPIO 5","https://pinout.xyz/pinout/pin18_gpio24")
            .put("GPIO 6","https://pinout.xyz/pinout/pin22_gpio25")
            .put("GPIO 7","https://pinout.xyz/pinout/pin7_gpio4")
            .put("GPIO 8","https://pinout.xyz/pinout/pin3_gpio2")
            .put("GPIO 9","https://pinout.xyz/pinout/pin5_gpio3")
            .put("GPIO 10","https://pinout.xyz/pinout/pin24_gpio8")
            .put("GPIO 11","https://pinout.xyz/pinout/pin26_gpio7")
            .put("GPIO 12","https://pinout.xyz/pinout/pin19_gpio10")
            .put("GPIO 13","https://pinout.xyz/pinout/pin21_gpio9")
            .put("GPIO 14","https://pinout.xyz/pinout/pin23_gpio11")
            .put("GPIO 15","https://pinout.xyz/pinout/pin8_gpio14")
            .put("GPIO 16","https://pinout.xyz/pinout/pin10_gpio15")
            .put("GPIO 21","https://pinout.xyz/pinout/pin29_gpio5")
            .put("GPIO 22","https://pinout.xyz/pinout/pin31_gpio6")
            .put("GPIO 23","https://pinout.xyz/pinout/pin33_gpio13")
            .put("GPIO 24","https://pinout.xyz/pinout/pin35_gpio19")
            .put("GPIO 25","https://pinout.xyz/pinout/pin37_gpio26")
            .put("GPIO 26","https://pinout.xyz/pinout/pin32_gpio12")
            .put("GPIO 27","https://pinout.xyz/pinout/pin36_gpio16")
            .put("GPIO 28","https://pinout.xyz/pinout/pin38_gpio20")
            .put("GPIO 29","https://pinout.xyz/pinout/pin40_gpio21")
            .put("GPIO 30","https://pinout.xyz/pinout/pin27_gpio0")
            .put("GPIO 31","https://pinout.xyz/pinout/pin28_gpio1")
            .build();
    private static final Logger logger = Logger.getLogger(PortWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/inputs")
    public Response inputs() {
        Set<NamedPort> inputs = ServiceMode.getInputs();
        return Response.ok(new Gson().toJson(FluentIterable.from(inputs).transform(port -> {
            boolean isHigh = ServiceMode.getInputState(port.getRefCd());
           String url = Optional.ofNullable(dao.getString(port.getRefCd())).map(urlMap::get).orElse("#");
            return new Port(port.getRefCd(), url, port.getName(), Boolean.toString(isHigh));
        }).toList())).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/outputs")
    public Response outputs() {
        Set<NamedPort> outputs = ServiceMode.getOutputs();
        return Response.ok(new Gson().toJson(FluentIterable.from(outputs).transform(port -> {
            boolean isHigh = ServiceMode.getPort(port.getRefCd()).getState().isHigh();
            String url = Optional.ofNullable(dao.getString(port.getRefCd())).map(urlMap::get).orElse("#");
            return new Port(port.getRefCd(), url, port.getName(), Boolean.toString(isHigh));
        }).toList())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/output/{refCd}")
    public Response setValue(@PathParam("refCd") String refCd, String value) throws InterruptedException {
        //1.check if its output
        boolean match = FluentIterable.from(ServiceMode.getOutputs()).anyMatch(port -> port.getRefCd().equals(refCd));
        if (!match) return Response.status(Response.Status.BAD_REQUEST).build();
        //2. shutdown exclussions
        Collection<String> mutualExclussion = ServiceMode.getExclusions().get(refCd);
        mutualExclussion.forEach(exclussion -> {
            ServiceMode.getPort(exclussion).setState(false);
            logger.log(Level.INFO, "Port " + exclussion + " vypnuty");
        });
        if (!mutualExclussion.isEmpty()){
            Thread.sleep(1000);
        }
        //3. Now we can enable it
        GpioPinDigitalOutput port = ServiceMode.getPort(refCd);
        port.setState(new Gson().fromJson(value, Boolean.class));
        logger.log(Level.INFO, "Port " + refCd + (port.isHigh()?" zapnuty": " vypnuty"));
        return Response.ok(new Gson().toJson(mutualExclussion)).build();
    }


}
