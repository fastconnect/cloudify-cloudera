/*******************************************************************************
 * init script for cloudera master
 *******************************************************************************/

 import org.cloudifysource.dsl.context.ServiceContextFactory
 
 def context = ServiceContextFactory.getServiceContext()
 def config = new ConfigSlurper().parse(new File("cdh4-master-service.properties").toURL())
 builder = new AntBuilder()

 //saving some data in the instance attributes
 context.attributes.thisInstance.isReady = false
 
 println "cdh4-master-init.groovy: Setting ipAddress to ${context.getPrivateAddress()}"
 context.attributes.thisInstance.ipAddress = context.getPrivateAddress()
 
 println "cdh4-master-init.groovy: Saving the publicIp ${context.getPublicAddress()}"
 context.attributes.thisInstance.publicAddress = context.getPublicAddress()
 
 //def hostAlias = InetAddress.getLocalHost().getHostName()
 def hostAlias = context.getServiceName()+"-"+context.getInstanceId()
 println "cdh4-master-init.groovy: Setting hostname to ${hostAlias}"
 context.attributes.thisInstance.hostAlias = hostAlias
 
 //mongoConfig and mongoS ports
 context.attributes.thisApplication.useMongo = config.useMongo
 
 if(config.mongoConfigPort != null) {
	 println "cdh4-master-init.groovy: Setting mongoConfig port to ${config.mongoConfigPort}"
	 context.attributes.thisInstance.mongoConfigPort = config.mongoConfigPort
 }
 if(config.mongoSPort != null) {
	 println "cdh4-master-init.groovy: Setting mongoS port to ${config.mongoSPort}"
	 context.attributes.thisInstance.mongoSPort = config.mongoSPort
 }
 
 //initializing the  hostMap
 context.attributes.thisInstance.hostsMap = [localhost:"127.0.0.1"]