/*******************************************************************************
 * init script for cloudera slave
 *******************************************************************************/

 import org.cloudifysource.dsl.context.ServiceContextFactory
 
 def context = ServiceContextFactory.getServiceContext()
 def config = new ConfigSlurper().parse(new File("cdh4-slave-service.properties").toURL())
 builder = new AntBuilder()

 //saving some data in the instance attributes
 context.attributes.thisInstance.isReady = false
 
 println "cdh4-slave-init.groovy: Setting ipAddress to ${context.getPrivateAddress()}"
 context.attributes.thisInstance.ipAddress = context.getPrivateAddress()
 
 println "cdh4-master-init.groovy: Saving the publicIp ${context.getPublicAddress()}"
 context.attributes.thisInstance.publicAddress = context.getPublicAddress()
 
 //def hostAlias = InetAddress.getLocalHost().getHostName()
 def hostAlias = context.getServiceName()+"-"+context.getInstanceId()
 println "cdh4-slave-init.groovy: Setting hostname to ${hostAlias}"
 context.attributes.thisInstance.hostAlias = hostAlias
 
 if(config.mongoDPort != null) {
	 println "cdh4-slave-init.groovy: Setting mongod port to ${config.mongoDPort}"
	 context.attributes.thisInstance.mongoDPort = config.mongoDPort
 }
 
 //initializing the  hostMap
 context.attributes.thisInstance.hostsMap = [localhost:"127.0.0.1"]