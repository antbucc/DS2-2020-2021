package ds2.protocol;

// Standard libraries
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// Support libraries
import org.eclipse.jdt.annotation.NonNull;

// Custom libraries
import ds2.application.Application;
import ds2.application.ApplicationEvent;
import ds2.network.NetworkMessage;
import ds2.nodes.Address;
import ds2.nodes.LocalEvent;
import ds2.nodes.Machine;
import ds2.simulator.SimulationEvent;
import ds2.utility.Event;

public abstract class Protocol<T extends Application<?>> {
	protected Machine runningOn = null;
	protected T application = null;
	
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
	
	public static <T extends Annotation> List<Method> getMethods(
			Class<? extends Protocol> protocolClass,
			Function<T, Class<?>> key,
			Class<T> annotationClass,
			Object obj) {
		return Arrays.stream(protocolClass.getDeclaredMethods())
			.filter((Method mth) -> mth.isAnnotationPresent(annotationClass))	  
			.filter((Method mth) -> {
				T h = mth.getAnnotation(annotationClass);
				Class<?> targetClass = key.apply(h);
				return targetClass == null || targetClass.isInstance(obj);
			})
			.collect(Collectors.toList());
	}

	public void handle(Event ev) throws Exception {
		if (ev instanceof ApplicationEvent) {
			this.application.handleApplicationEvent((ApplicationEvent)ev);
			return;
		} else if (ev instanceof NetworkMessage) {
			this.handleNetworkMessage((NetworkMessage) ev);
			return;
		} else if (ev instanceof LocalEvent) {
			this.handleLocalEvent((LocalEvent)ev);
			return;
		} else if (ev instanceof SimulationEvent) {
			this.handleSimulationEvent((SimulationEvent)ev);
			return;
		}
		
		throw new Exception("Event of unknown type");
	}
	
	protected void handleNetworkMessage(NetworkMessage ev) throws Exception {
		List<Method> methods = Protocol.getMethods(
				this.getClass(),
				NetworkMessageHandler::dataCls,
				NetworkMessageHandler.class,
				ev.getData());
		
		if ( methods.isEmpty() ) {
			throw new Exception("Unknown network event type received (" + ev.getData().getClass().getName() + ")");
		}
		
		Class<?> dataCls = methods.get(0).getAnnotation(NetworkMessageHandler.class).dataCls();
		Address destination = ev.getDestination();

		for (Method method: methods) {
			method.invoke(this, destination, dataCls.cast(ev.getData()));
		}
	}

	protected void handleSimulationEvent(SimulationEvent ev) throws Exception {
		List<Method> methods = Protocol.getMethods(
				this.getClass(),
				SimulationEventHandler::cls,
				SimulationEventHandler.class,
				ev);
		
		if ( methods.isEmpty() ) {
			throw new Exception("Unknown simulation event type received");
		}
		
		for (Method method: methods) {
			method.invoke(this, ev);
		}
	}
	
	protected void handleLocalEvent(LocalEvent ev) throws Exception {
		List<Method> methods = Protocol.getMethods(
				this.getClass(),
				LocalEventHandler::cls,
				LocalEventHandler.class,
				ev);
		
		if ( methods.isEmpty() ) {
			throw new Exception("Unknown local event type received");
		}
		
		for (Method method: methods) {
			method.invoke(this, ev);
		}
	}
	
	public void setRunningOn(@NonNull Machine runningOn) {
		this.runningOn = runningOn;
	}
	
	public void setApplication(T application) {
		this.application = application;
	}
	
	public @NonNull Address getAddress() {
		return this.runningOn.getAddress();
	}
	
	public Protocol(@NonNull Machine machine) {
		this.runningOn = machine;
	}
}
