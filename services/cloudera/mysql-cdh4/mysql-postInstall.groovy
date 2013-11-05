/*******************************************************************************
 * postInstall script for cloudera manager host
 *******************************************************************************/

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceInstance

def context = ServiceContextFactory.getServiceContext()
//def config = new ConfigSlurper().parse(new File("mysql-service.properties").toURL())
def builder = new AntBuilder()
def managerServiceName = "cdh4-manager"
def masterServiceName = "cdh4-master"
def slaveServiceName = "cdh4-slave"
def mysqlServiceName = "mysql"
def hostAlias = context.attributes.thisInstance.hostAlias
def serviceNamesMap = [manager:(managerServiceName), master:(masterServiceName), slave:(slaveServiceName), mysql:(mysqlServiceName)]
def hostsMap = context.attributes.thisInstance.hostsMap

//
//updating hosts file
//

println "mysql-postinstall.groovy: about updating hosts file.... "
hostsMap= hostsMap==null?[localhost:"127.0.0.1"]:hostsMap
def service
def lockTimeoutException
def maxAttempt = 60
def nbLeftAttempt
def insHostName

serviceNamesMap.each {
	if ((service = context.waitForService(it.value, 5, TimeUnit.SECONDS)) == null){return}
	println "mysql-postinstall.groovy: ==========SERVICE IS ${it.value}============ "
	service.getInstances().each {ins->
		insHostName = context.attributes[it.value].instances[ins.instanceId].hostAlias
		lockTimeoutException = true
		nbLeftAttempt = maxAttempt
		println "mysql-postinstall.groovy: invoking updateHosts of: ${insHostName}:${ins.getHostAddress()}.... "
		while(lockTimeoutException && nbLeftAttempt>0) {
			try{
				def res = (ins as ServiceInstance).invoke("updateHosts")
				println "newHostsMap of  ${insHostName}:${ins.getHostAddress()} " + res.toString()
				lockTimeoutException = false
			}catch(TimeoutException tex) {
				nbLeftAttempt--
				println "mysql-postinstall.groovy: Lock TimeOutException!! will retry after 1 second.  ${nbLeftAttempt} attempts left..."
				sleep 1000
			}
		}
		if(nbLeftAttempt==0) println "mysql-postinstall.groovy: unable to update hostsFile of ${insHostName}:${ins.getHostAddress()} after ${maxAttempt} attempts!"
	}
	
	//service.invoke("updateHosts")
}

println "mysql-postinstall.groovy: Setting hostname to ${hostAlias}"
builder.sequential {
	exec(executable: "sudo", failonerror: "true"){
		arg(line:"hostname ${hostAlias}")
	}
}
