import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * setup all the dependencies of hdfs before (re)starting the service
 *******************************************************************************/
def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HDFS/hdfs-config.properties").toURL())
def builder = new AntBuilder()
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
Binding execContext
CM4RESTClient restCli
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HDFS as String


//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hdfs-setup-start.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hdfs-setup-start.groovy: REST API not available! exiting"
	System.exit(1)
}


//formating the namenode
println "hdfs-setup-start.groovy: about to format the hdfs..."
def isFormated = false
if (context.attributes[masterServiceName].containsKey("${service}.isFormated") ) {
	isFormated = context.attributes[masterServiceName]["${service}.isFormated"] as boolean
}
if( isFormated ) {
	println "hdfs-setup-start.groovy: one Namenode already formated!!!..."
}else{
	def jsonDoc = [items:[service+'-namenode-1']]
	if (!(cmdSucceeded = restCli.execRoleCommand(cluster, service, CM4RESTClient.CMD_HDFSFORMAT, jsonDoc)) ){
		println "hdfs-setup-start.groovy: Failed to format the hdfs... exiting "
		System.exit(1)
	}
	context.attributes[masterServiceName]["${service}.isFormated"] = true
}


//(re)starting the service
def startingScript = "${context.serviceDirectory}/startService.groovy"
def args = [ "${cluster}",
			 "${service}" ] as String[]
execContext = new Binding(args)
new GroovyShell(execContext).evaluate(new File("${startingScript}"))


//creating the hdfs temp dir
println "hdfs-setup-start.groovy: about to create the hdfs tmp dir..."
def isTmpDirCreated = false
if (context.attributes[masterServiceName].containsKey("${services.HDFS}.isTmpDirCreated") ) {
	isTmpDirCreated = context.attributes[masterServiceName]["${services.HDFS}.isTmpDirCreated"] as boolean
}
if( isTmpDirCreated ) {
	println "hdfs-setup-start.groovy: hdfs tmp dir already created!!!..."
}else if (!(cmdSucceeded = restCli.execServiceCommand(cluster, services.HDFS, CM4RESTClient.CMD_HDFS_CREATE_TEMP_DIR, null)) ){
	println "hdfs-setup-start.groovy: Failed to to create the hdfs temp dir... exiting "
	System.exit(1)
}else{
	context.attributes[masterServiceName]["${services.HDFS}.isTmpDirCreated"] = true
}

