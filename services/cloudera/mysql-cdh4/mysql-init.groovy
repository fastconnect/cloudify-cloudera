/*******************************************************************************
 * init script for cloudera manager
 *******************************************************************************/

import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext()
//def config = new ConfigSlurper().parse(new File("mysql-service.properties").toURL())
builder = new AntBuilder()

//saving some data in the service attributes
println "cdh4-master-init.groovy: Setting ipAddress to ${context.getPrivateAddress()}"
context.attributes.thisInstance.ipAddress = context.getPrivateAddress()


println "cdh4-master-init.groovy: Saving the publicIp ${context.getPublicAddress()}"
context.attributes.thisInstance.publicAddress = context.getPublicAddress()

//def hostAlias = InetAddress.getLocalHost().getHostName()
def hostAlias = context.getServiceName()+"-"+context.getInstanceId()
context.attributes.thisInstance.hostAlias = hostAlias

//initializing the  hostMap
context.attributes.thisInstance.hostsMap = [localhost:"127.0.0.1"]



