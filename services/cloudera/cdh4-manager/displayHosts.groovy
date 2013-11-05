import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.context.ServiceContextFactory


/*******************************************************************************
 * display hosts script for custtom command
 * 
 * displays the list of couple IpAddress - hostName of all the nodes on the cluster
 * 
 * @param addressType: the type of address: either public or private one 
 *******************************************************************************/

def publicWord = "public"
def privateWord = "private"
def addressType = publicWord
def addrAttrName

def context = ServiceContextFactory.getServiceContext()
def masterServiceName = "cdh4-master"
def slaveServiceName = "cdh4-slave"
def managerServiceName = "cdh4-manager"
def mysqlServiceName = "mysql"
def serviceNamesMap = [manager:(managerServiceName), master:(masterServiceName), slave:(slaveServiceName), mysql:(mysqlServiceName)]

def hostsMap = [:]
def hostEntries=""

if(args.size()>0) {
	if(!(args[0] as String).equalsIgnoreCase(publicWord) && !(args[0] as String).equalsIgnoreCase(privateWord)) {
		println "displayHosts.groovy: illegal argument type!! argument should be either <private> or <public>. will display the default: <public>"
	}
	else{
		addressType = args[0].toString().toLowerCase()
	}
}

addrAttrName = addressType.equalsIgnoreCase(privateWord)?"ipAddress":"publicAddress"
def attrs
def allServiceInstances
serviceNamesMap.each {
	if (context.waitForService(it.value, 20, TimeUnit.SECONDS) == null){return}
	allServiceInstances = context.waitForService(it.value, 20, TimeUnit.SECONDS).getInstances() //saving all the instances of the service
	allServiceInstances.each {ins ->
		attrs = context.attributes[it.value].instances[ins.getInstanceId()]
		//println "this of ${attrs.hostAlias} & ${attrs[addrAttrName]} "
		if (attrs[addrAttrName] != null && attrs.hostAlias !=null){
			//println "Got: ${attrs.hostAlias}: ${attrs[addrAttrName]}"
			hostsMap[attrs.hostAlias] = attrs[addrAttrName]
		}
	}
}

hostsMap.each {
	hostEntries += it.value + "\t" + it.key + "\n"
}

println "displaysHosts.groovy: addres is of type ${addressType}. Host entries are:\n\n"
println "${hostEntries}"

return hostEntries


