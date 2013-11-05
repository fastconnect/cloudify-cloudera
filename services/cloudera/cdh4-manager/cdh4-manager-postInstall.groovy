/*******************************************************************************
 * postInstall script for cloudera manager host
 *******************************************************************************/

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceInstance

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("cdh4-manager-service.properties").toURL())
def builder = new AntBuilder()
def managerServiceName = "cdh4-manager"
def masterServiceName = "cdh4-master"
def slaveServiceName = "cdh4-slave"
def mysqlServiceName = "mysql"
def currentInstance
def cm4Instance
def ip = context.attributes.thisService.ipAddress
def hostAlias = context.attributes.thisService.hostAlias
def hostEntries = ""
def serviceNamesMap = [manager:(managerServiceName), master:(masterServiceName), slave:(slaveServiceName), mysql:(mysqlServiceName)]
def allServiceInstances = []
def hostsMap = context.attributes.thisInstance.hostsMap
//
//updating hosts file
//

println "cdh4-manager-postinstall.groovy: about updating hosts file.... "
def instancesAttr
hostsMap= hostsMap==null?[localhost:"127.0.0.1"]:hostsMap
def service
def lockTimeoutException
def maxAttempt = 10
def nbLeftAttempt

serviceNamesMap.each {
	if ((service = context.waitForService(it.value, 20, TimeUnit.SECONDS)) == null){return}
	println "cdh4-manager-postinstall.groovy: ========== SERVICE IS ${it.value} ============ "
	service.getInstances().each {
		lockTimeoutException = true
		nbLeftAttempt = maxAttempt
		println "cdh4-manager-postinstall.groovy: invoking updateHosts of: ${it.getHostName()}:${it.getHostAddress()}.... "
		while(lockTimeoutException && nbLeftAttempt>0) {
			try{
				(it as ServiceInstance).invoke("updateHosts")
				lockTimeoutException = false
			}catch(TimeoutException tex) {
				nbLeftAttempt--
				println "cdh4-manager-postinstall.groovy: Lock TimeOutException!! will retry after 1 second.  ${nbLeftAttempt} attempts left..."
				sleep 1000
			}
		}
		if(nbLeftAttempt==0) println "cdh4-manager-postinstall.groovy: unable to update hostsFile of ${it.getHostName()}:${it.getHostAddress()} after ${maxAttempt} attempts!"
	}
	
	//service.invoke("updateHosts")
}

println "cdh4-manager-postinstall.groovy: Setting hostname to ${hostAlias}"
builder.sequential {
	exec(executable: "sudo", failonerror: "true"){
		arg(line:"hostname ${hostAlias}")
	}
}
