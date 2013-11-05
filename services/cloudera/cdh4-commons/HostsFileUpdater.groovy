import groovy.transform.*

import org.cloudifysource.dsl.context.ServiceContext
import org.cloudifysource.dsl.context.ServiceContextFactory

class HostsFileUpdater {

	private final updateHostsLock = new Object()
	//private ServiceContext context = ServiceContextFactory.getServiceContext()

	@Synchronized("updateHostsLock")
	Object updateHosts(ServiceContext context) {
		
		def map = [context:(context)]
		Binding execContext = new Binding(map)
		return new GroovyShell(execContext).evaluate(new File("${context.serviceDirectory}/updateHosts.groovy"))
	}
}