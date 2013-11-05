import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 *  setup all the dependencies of hbase before (re)starting the service
 *
 *******************************************************************************/

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HBASE/hbase-config.properties").toURL())
def builder = new AntBuilder()
def cmdSucceeded
CM4RESTClient restCli
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HBASE as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hbase-setup-start.groovy: api url is: ${restCli.apiUrl}..."
//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hbase-setup-start.groovy: REST API not available! exiting"
	System.exit(1)
}


//create the hbase root dir
println "hbase-setup-start.groovy: about to create the hbase root dir..."
def isRootDirCreated = false
if (context.attributes[managerServiceName].containsKey("${service}.isRootDirCreated") ) {
	isRootDirCreated = context.attributes[managerServiceName]["${service}.isRootDirCreated"] as boolean
}
if( isRootDirCreated ) {
	println "hbase-setup-start.groovy: hbase database already created!!!..."
}else if (!(cmdSucceeded = restCli.execServiceCommand(cluster, service, CM4RESTClient.CMD_HBASE_CREATE_ROOT, null)) ){
	println "hbase-setup-start.groovy: Failed to create the hbase root dir... exiting "
	System.exit(1)
}else{
	context.attributes[managerServiceName]["${service}.isRootDirCreated"] = true
}


//(re)starting the service
def startingScript = "${context.serviceDirectory}/startService.groovy"
def args = [ "${cluster}",
			 "${service}" ] as String[]
execContext = new Binding(args)
new GroovyShell(execContext).evaluate(new File("${startingScript}"))
