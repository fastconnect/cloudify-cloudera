import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hbase master
 *
 * @param hostname
 * @param ip
 * @param instanceId
 *
 *******************************************************************************/
/*params*/
def hostname = args[0] as String
def ip = args[1] as String
def instanceId = args[2] as String

def role = "MASTER"
def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HBASE/hbase-config.properties").toURL())
def cmdSucceeded
def roleName
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HBASE as String


//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)
println "hbase-master-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hbase-master-config.groovy: REST API not available! exiting"
	System.exit(1)
}

//name
roleName = service+"-"+role.toLowerCase()+"-"+instanceId

//master adding
println "hbase-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${role}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, role, hostname, roleName)) ){
	println "hbase-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${role}) .... exiting"
	System.exit(1)
}

/*//job tracker conf
def configMap = [items:[[name:'jobtracker_mapred_local_dir_list', value:'/mapred/jt'], 
						  [name:'mapred_job_tracker_handler_count', value:config.mrJtHandlerCount]]]

println "hbase-master-config.groovy: about to configure the jobtracker node: properties ares: ${configMap}..."
if (!(cmdSucceeded = restCli.configService(cluster, service, roleName, configMap)) ){
	println "hbase-master-config.groovy: Failed to configure the node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , roleName(${roleName}) .... exiting "
	System.exit(1)
}*/
