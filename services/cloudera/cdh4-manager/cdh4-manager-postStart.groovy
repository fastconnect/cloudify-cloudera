import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * postStart script for cloudera manager
 *******************************************************************************/

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/cdh4-manager-service.properties").toURL())
def cm4Ip = context.attributes.thisService.ipAddress
def jsonDoc
def cmdSucceeded
Binding execContext
CM4RESTClient restCli

restCli = new CM4RESTClient(cm4Ip, config.apiVersion)

//lock for startDetection
context.attributes.thisService.inPostStart = true
//apiVersion
context.attributes.thisService['apiVersion'] = config.apiVersion

println "cdh4-manager-postStart.groovy: api url is: ${restCli.apiUrl}..."

// //testing the availibility of the api
if(!restCli.isApiAvailable(120)){
	println "cdh4-manager-addNode.groovy: REST API not available! exiting"
	context.attributes.thisService['inPostStart'] = false
	System.exit(1)
}

//create a cluster
def cluster = config.cluster.name
println "cdh4-manager-postStart.groovy: about to create a new cluster. cluster name is:  ${cluster}"
if(!(cmdSucceeded = restCli.createCluster(cluster, config.cluster.version))) {
	println "cdh4-manager-postStart.groovy: failed to create a new cluster <${cluster}>... exiting the postart routine"
	//release the lock after exiting
	context.attributes.thisService['inPostStart'] = false
	System.exit(1)
}
//saving cluster name into to context
context.attributes.thisService["cluster"] = config.cluster

//add availlable services
if( !(config.containsKey("services")) || config.services == null || (config.services.size()==0 )) {
	println "cdh4-manager-postStart.groovy: no services definition found! skipping adding services..."
	context.attributes.thisService["services"] =[:]
	//release the lock after exiting
	context.attributes.thisService['inPostStart'] = false
	System.exit(0)
}

println "cdh4-manager-postStart.groovy: about to add services to the cluster <${cluster}>..."
println "cdh4-manager-postStart.groovy: services are ${config.services}"

config.services.each {
	def args = ["${it.value}",
				"${it.key.toUpperCase()}"] as String[]
	execContext = new Binding(args)
	new GroovyShell(execContext).evaluate(new File("cdh4-manager-addService.groovy"))
}

//release the lock after exiting
context.attributes.thisService.inPostStart = false

println "cdh4-manager-postStart.groovy: End of PostStart"
