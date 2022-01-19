package org.dropco.smarthome.heating.solar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dropco.smarthome.RuntimeTypeAdapterFactory;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.ParkPosition;
import org.dropco.smarthome.heating.solar.dto.Position;

public class SolarSerializer {
   private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(RuntimeTypeAdapterFactory.of(Position.class, "type", false)
            .registerSubtype(DeltaPosition.class, "delta")
            .registerSubtype(AbsolutePosition.class, "absolute")
            .registerSubtype(ParkPosition.class, "parking")).create();

    /***
     * Gets the gson
     * @return
     */
    public static Gson getGson() {
        return gson;
    }
}
