import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hbase slave
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

def role = "REGIONSERVER"
def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HBASE/hbase-config.properties").toURL())
def cmdSucceeded
def roleName
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

println "hbase-slave-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hbase-slave-config.groovy: REST API not available! exiting"
	System.exit(1)
}

//name
roleName = service+"-"+role.toLowerCase()+"-"+instanceId

//region server adding
println "hbase-slave-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${role}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, role, hostname, roleName)) ){
	println "hbase-slave-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${role}) .... exiting"
	System.exit(1)
}


/*// configuration of roles
//task tracker conf
def ttConfigMap = [items:[[name:'tasktracker_mapred_local_dir_list', value:'/mapred/local'],
		[name:'mapred_tasktracker_map_tasks_maximum', value:config.ttMaxMapTasks],
		[name:'mapred_tasktracker_reduce_tasks_maximum', value:config.ttMxReduceTasks]]]

println "hbase-slave-config.groovy: about to configure the jobtracker node: properties ares: ${ttConfigMap}..."
if (!(cmdSucceeded = restCli.configService(cluster, service, roleName, ttConfigMap)) ){
	println "hbase-slave-config.groovy: Failed to configure the node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , roleName(${roleName}) .... exiting "
	System.exit(1)
}*/
