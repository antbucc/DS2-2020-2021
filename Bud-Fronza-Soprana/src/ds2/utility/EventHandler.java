package ds2.utility;

// Standard libraries
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// Custom libraries
import ds2.application.ApplicationEvent;
import ds2.network.NetworkMessage;
import ds2.nodes.LocalEvent;
import ds2.simulator.SimulationEvent;

/**
 * This class can be extended to automatically provide a handle function which will use annotated functions to handle the different types of events
 */
public abstract class EventHandler {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public abstract @interface SimulationEventHandler {
		public Class<? extends SimulationEvent> cls();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public abstract @interface NetworkMessageHandler {
		public Class<?> dataCls();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public abstract @interface LocalEventHandler {
		public Class<? extends LocalEvent> cls();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public abstract @interface ApplicationEventHandler {
		public Class<? extends ApplicationEvent> cls();
	}
	
	/**
	 * Utility function to look for annotated methods
	 * @param <T> The type of the annotation to look for
	 * @param eventHandlerClass The class to inspect (the one in which you want to look for annotations)
	 * @param key Function To get from the annotation the class to compare to the class of the event
	 * @param annotationClass The class of the annotation (T)
	 * @param obj The event
	 * @return A list of the annotated methods which correspond
	 */
	public static <T extends Annotation> List<Method> getMethods(
			Class<? extends EventHandler> eventHandlerClass,
			Function<T, Class<?>> key,
			Class<T> annotationClass,
			Object obj) {
		return Arrays.stream(eventHandlerClass.getDeclaredMethods())
			.filter((Method mth) -> mth.isAnnotationPresent(annotationClass))	  
			.filter((Method mth) -> {
				T h = mth.getAnnotation(annotationClass);
				Class<?> targetClass = key.apply(h);
				return targetClass == null || targetClass.isInstance(obj);
			})
			.collect(Collectors.toList());
	}

	/**
	 * Handler function for any event. It delegates to the more specific functions
	 * @param ev The event to handle
	 */
	public void handle(Event ev) {
		if (ev instanceof ApplicationEvent) {
			this.handleApplicationEvent((ApplicationEvent)ev);
			return;
		} else if (ev instanceof NetworkMessage) {
			this.handleNetworkMessage((NetworkMessage<?>) ev);
			return;
		} else if (ev instanceof LocalEvent) {
			this.handleLocalEvent((LocalEvent)ev);
			return;
		} else if (ev instanceof SimulationEvent) {
			this.handleSimulationEvent((SimulationEvent)ev);
			return;
		}
		
		throw new RuntimeException("Event of unknown type");
	}
	
	/**
	 * Handler function for NeworkMessage
	 * @param ev The NetworkMessage to handle
	 */
	public void handleNetworkMessage(NetworkMessage<?> ev) {
		List<Method> methods = getMethods(
				this.getClass(),
				NetworkMessageHandler::dataCls,
				NetworkMessageHandler.class,
				ev.getData());
		
		if ( methods.isEmpty() ) {
			throw new RuntimeException("Unknown network event type received (" + ev.getData().getClass().getName() + ")");
		}

		for (Method method: methods) {
			try {
				method.invoke(this, ev);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RuntimeException("The function called is of the wrong type", e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Handler failed", e);
			}
		}
	}

	/**
	 * Handler function for SimulationEvent
	 * @param ev The SimulationEvent to handle
	 */
	public void handleSimulationEvent(SimulationEvent ev) {
		List<Method> methods = getMethods(
				this.getClass(),
				SimulationEventHandler::cls,
				SimulationEventHandler.class,
				ev);
		
		if ( methods.isEmpty() ) {
			throw new RuntimeException("Unknown simulation event type received");
		}
		
		for (Method method: methods) {
			try {
				method.invoke(this, ev);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RuntimeException("The function called is of the wrong type", e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Handler failed", e);
			}
		}
	}
	
	/**
	 * Handler function for LocalEvent
	 * @param ev The LocalEvent to handle
	 */
	public void handleLocalEvent(LocalEvent ev) {
		List<Method> methods = getMethods(
				this.getClass(),
				LocalEventHandler::cls,
				LocalEventHandler.class,
				ev);
		
		if ( methods.isEmpty() ) {
			throw new RuntimeException("Unknown local event type received");
		}
		
		for (Method method: methods) {
			try {
				method.invoke(this, ev);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RuntimeException("The function called is of the wrong type", e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Handler failed", e);
			}
		}
	}
	
	/**
	 * Handler function for ApplicationEvent
	 * @param ev The ApplicationEvent to handle
	 */
	public void handleApplicationEvent(ApplicationEvent ev) {
		List<Method> methods = getMethods(
				this.getClass(),
				ApplicationEventHandler::cls,
				ApplicationEventHandler.class,
				ev);
		
		if ( methods.isEmpty() ) {
			throw new RuntimeException("Unknown application event type received");
		}
		
		for (Method method: methods) {
			try {
				method.invoke(this, ev);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RuntimeException("The function called is of the wrong type", e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Handler failed", e);
			}
		}
	}
		
	public EventHandler() {}
}
