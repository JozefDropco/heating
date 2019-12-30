package org.dropco.smarthome;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.solar.move.SolarPanelThreadManager;
import org.dropco.smarthome.watering.WateringThreadManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceMode {
    private static final AtomicBoolean serviceMode =new AtomicBoolean(false);
    private static final Set<NamedPort> outputs = new LinkedHashSet<>();
    private static final Set<NamedPort> inputs = new LinkedHashSet<>();
    private static final Map<String,NamedPort> namedPortMap = new HashMap<>();
    private static final Multimap<String,String> exclusions = Multimaps.newListMultimap(Maps.newHashMap(), ArrayList::new);

    public static void startServiceMode(){
        serviceMode.set(true);
        SolarPanelThreadManager.stop();
        WateringThreadManager.stop();
    }


    public static void stopServiceMode(){
        serviceMode.set(false);
    }

    public static boolean isServiceMode(){
        return serviceMode.get();
    }

    public static Set<NamedPort> getOutputs() {
        return Collections.unmodifiableSet(outputs);
    }

    public static  void addOutput(NamedPort port) {
        outputs.add(port);
        namedPortMap.put(port.getRefCd(),port);
    }

    public static  void addInput(NamedPort port) {
        inputs.add(port);
        namedPortMap.put(port.getRefCd(),port);
    }

    public static Set<NamedPort> getInputs() {
        return Collections.unmodifiableSet(inputs);
    }

    public static Multimap<String, String> getExclusions() {
        return exclusions;
    }

    public static Map<String, NamedPort> getNamedPortMap() {
        return Collections.unmodifiableMap(namedPortMap);
    }
}
