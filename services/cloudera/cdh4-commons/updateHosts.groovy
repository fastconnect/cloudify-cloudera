import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import org.cloudifysource.dsl.context.ServiceContext;
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceInstance;

//context = ServiceContextFactory.getServiceContext()
def context = binding.variables.context as ServiceContext

//def hostname = args[0]
//def ip = args[1]


def managerServiceName = "cdh4-manager"
def masterServiceName = "cdh4-master"
def slaveServiceName = "cdh4-slave"
def mysqlServiceName = "mysql"
def ip = context.attributes.thisInstance.ipAddress
def hostAlias = context.attributes.thisInstance.hostAlias
def hostEntries = ""
def serviceNamesMap = [manager:(managerServiceName), master:(masterServiceName), slave:(slaveServiceName), mysql:(mysqlServiceName)]
def hostsMap = context.attributes.thisInstance.hostsMap

println "updateHosts.groovy: on ${context.getServiceName()}-${context.getInstanceId()}:${context.getPrivateAddress()}..."

hostsMap= hostsMap==null?[:]:hostsMap

try {
	// update /etc/hosts
	currOs=System.properties['os.name']
	def hostsPath = ""
	if ("${currOs}".toLowerCase().contains('windows')) {
		hostsPath = "\\system32\\drivers\\etc\\hosts"
	}
	else {
		hostsPath = "/etc/hosts"
	}

	def attrs
	def serviceInstances
	
	serviceNamesMap.each {
		if (context.waitForService(it.value, 1, TimeUnit.SECONDS) == null){return}	//service not present
		serviceInstances = context.waitForService(it.value, 20, TimeUnit.SECONDS).getInstances() //all the instances of the service
		serviceInstances.each {ins ->
			attrs = context.attributes[it.value].instances[ins.getInstanceId()]
			//println "this of ${attrs.hostAlias} & ${attrs.ipAddress} "
			if (attrs.ipAddress != null && attrs.hostAlias !=null){
				println "Got: ${attrs.hostAlias}: ${attrs.ipAddress}"
				hostsMap[attrs.hostAlias] = attrs.ipAddress
			}
		}
	}
	
	hostsMap.each {
		hostEntries += it.value + "\t" + it.key + "\n"
	}

	new File("/tmp/hosts").text = hostEntries

	println "updateHosts.groovy: updating hosts"
	builder = new AntBuilder()

	if ("${currOs}".toLowerCase().contains('windows')) {
		new File(hostsPath).text = hostEntries
	}
	else {
		builder.sequential {
			chmod(file:"${context.serviceDirectory}/sudoTee.sh", perm:'+x')
			exec(executable:"${context.serviceDirectory}/sudoTee.sh", osfamily:"unix", failonerror:"true"){
				arg(value:"/tmp/hosts")
				arg(value:"/etc/hosts")
			}
		}
	}

	println "updateHosts.groovy: new hostsMap is: "+hostsMap.toString()
	context.attributes.thisInstance.hostsMap = hostsMap
	
	return hostsMap
}
finally {
	/*println("updateHosts.groovy: releasing the lock...")
	currentInstance.invoke("releaseLock")
	println("updateHosts.groovy: lock released")
	//lockDirectory.delete()
*/}