import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a mapreduce slave
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

def ttRole = "TASKTRACKER"
def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/MAPREDUCE/mapreduce-config.properties").toURL())
def cmdSucceeded
def ttRoleName
CM4RESTClient restCli
def masterName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.MAPREDUCE as String



//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "mapreduce-slave-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "mapreduce-slave-config.groovy: REST API not available! exiting"
	System.exit(1)
}

//name
ttRoleName = service+"-"+ttRole.toLowerCase()+"-"+instanceId

//task tracker adding
println "mapreduce-slave-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${ttRole}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, ttRole, hostname, ttRoleName)) ){
	println "mapreduce-slave-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${ttRole}) .... exiting"
	System.exit(1)
}


// configuration of roles
//task tracker conf
def ttConfigMap = [items:[[name:'tasktracker_mapred_local_dir_list', value:'/mapred/local'],
		[name:'mapred_tasktracker_map_tasks_maximum', value:config.ttMaxMapTasks],
		[name:'mapred_tasktracker_reduce_tasks_maximum', value:config.ttMxReduceTasks]]]

println "mapreduce-slave-config.groovy: about to configure the jobtracker node: properties ares: ${ttConfigMap}..."
if (!(cmdSucceeded = restCli.configService(cluster, service, ttRoleName, ttConfigMap)) ){
	println "mapreduce-slave-config.groovy: Failed to configure the node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , roleName(${ttRoleName}) .... exiting "
	System.exit(1)
}
