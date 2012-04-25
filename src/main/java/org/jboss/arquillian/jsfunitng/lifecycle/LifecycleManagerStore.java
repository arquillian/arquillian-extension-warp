package org.jboss.arquillian.jsfunitng.lifecycle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Validate;

public class LifecycleManagerStore {

    private static ConcurrentHashMap<Class<?>, Binding> STORE = new ConcurrentHashMap<Class<?>, Binding>();
    private static ConcurrentHashMap<LifecycleManager, Tracking> TRACK = new ConcurrentHashMap<LifecycleManager, Tracking>();

    @SuppressWarnings("serial")
    private static class Binding extends HashMap<Object, LifecycleManager> {
    }

    @SuppressWarnings("serial")
    private static class Tracking extends HashSet<Class<?>> {
    }
    
    @Inject
    Instance<LifecycleManager> manager;

    public static <T> LifecycleManager get(Class<T> clazz, T boundObject) throws ObjectNotAssociatedException {
        Validate.notNull(boundObject, "boundObject must not be null");
        Binding binding = STORE.get(clazz);
        if (binding == null) {
            throw new ObjectNotAssociatedException();
        }
        LifecycleManager manager = binding.get(boundObject);
        if (manager == null) {
            throw new ObjectNotAssociatedException();
        }
        return manager;
    }

    public <T> void bind(Class<T> clazz, T object) throws ObjectAlreadyAssociatedException {
        final LifecycleManager manager = getManager();
        
        // update binding

        Binding binding = STORE.get(clazz);
        if (binding == null) {
            STORE.putIfAbsent(clazz, new Binding());
            binding = STORE.get(clazz);
        }

        if (binding.put(object, manager) != null) {
            throw new ObjectAlreadyAssociatedException();
        }

        // update tracking

        Tracking tracking = TRACK.get(manager);
        if (tracking == null) {
            TRACK.putIfAbsent(manager, new Tracking());
            tracking = TRACK.get(manager);
        }

        tracking.add(clazz);
    }

    public <T> void unbind(Class<T> clazz, T object) throws ObjectNotAssociatedException {
        final LifecycleManager manager = getManager();

        // cancel binding

        Binding binding = STORE.get(clazz);
        if (binding == null) {
            throw new ObjectNotAssociatedException();
        }
        if (!binding.containsKey(object)) {
            throw new ObjectNotAssociatedException();
        }
        binding.remove(object);

        // cancel tracking

        Tracking tracking = TRACK.get(manager);
        tracking.remove(clazz);
    }

    public <T> void verifyManagerUnbound(LifecycleManager manager) throws StoreHasAssociatedObjectsException {
        Tracking tracking = TRACK.get(manager);
        if (tracking != null) {
            if (!tracking.isEmpty()) {
                throw new StoreHasAssociatedObjectsException();
            }
            TRACK.remove(manager);
        }
    }
    
    private LifecycleManager getManager() {
        return manager.get();
    }

    @SuppressWarnings("serial")
    public static class ObjectAlreadyAssociatedException extends Exception {
    }

    @SuppressWarnings("serial")
    public static class ObjectNotAssociatedException extends Exception {
    }

    @SuppressWarnings("serial")
    public static class StoreHasAssociatedObjectsException extends Exception {
    }
}
