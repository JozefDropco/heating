package org.dropco.smarthome.heating.solar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import org.dropco.smarthome.dto.NamedPort;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ServiceMode {
    private static final AtomicBoolean serviceMode =new AtomicBoolean(false);
    private static final Set<NamedPort> outputs = new LinkedHashSet<>();
    private static final Set<NamedPort> inputs = new LinkedHashSet<>();
    private static final Map<String,NamedPort> namedPortMap = new HashMap<>();
    private static final Map<String, Supplier<Boolean>> inputStateGetterMap = new HashMap<>();
    private static final Map<String, Supplier<Boolean>> outputStateGetterMap = new HashMap<>();
    private static final Map<String, Function<Boolean, Set<String>>> outputStateSetterMap = new HashMap<>();
    private static final List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());
    public static void startServiceMode(){
        serviceMode.set(true);
        subscribers.forEach(subscriber-> subscriber.accept(true));
    }


    public static void stopServiceMode(){
        serviceMode.set(false);
        subscribers.forEach(subscriber-> subscriber.accept(false));
    }

    public static boolean isServiceMode(){
        return serviceMode.get();
    }

    public static Set<NamedPort> getOutputs() {
        return Collections.unmodifiableSet(outputs);
    }

    public static  void addOutput(NamedPort port, Supplier<Boolean> stateGetter, Function<Boolean, Set<String>> stateSetter) {
        outputs.add(port);
        namedPortMap.put(port.getRefCd(),port);
        outputStateGetterMap.put(port.getRefCd(),stateGetter);
        outputStateSetterMap.put(port.getRefCd(),stateSetter);
    }

    public static  void addInput(NamedPort port, Supplier<Boolean> stateGetter) {
        inputs.add(port);
        namedPortMap.put(port.getRefCd(),port);
        inputStateGetterMap.put(port.getRefCd(),stateGetter);
    }

    public static Set<NamedPort> getInputs() {
        return Collections.unmodifiableSet(inputs);
    }


    public static Map<String, NamedPort> getNamedPortMap() {
        return Collections.unmodifiableMap(namedPortMap);
    }

    public static void addSubsriber(Consumer<Boolean> consumer){
        subscribers.add(consumer);
    }

    /***
     * Gets the digitalOutput
     * @return
     */
    public static boolean getOutputState(String refCd) {
        return outputStateGetterMap.getOrDefault(refCd,()->false).get();
    }
    /***
     * Gets the digitalInput
     * @return
     */
    public static boolean getInputState(String refCd) {
        return inputStateGetterMap.getOrDefault(refCd,()->false).get();
    }

    public static Set<String> setState(String portRefCd, boolean state) {
        return outputStateSetterMap.getOrDefault(portRefCd,(s)->Collections.emptySet()).apply(state);
    }
}
