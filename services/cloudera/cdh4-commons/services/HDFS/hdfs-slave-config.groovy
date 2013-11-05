import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hdfs slave
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

def dnRole = "DATANODE"

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HDFS/hdfs-config.properties").toURL())
def builder = new AntBuilder()
def cmdSucceeded
Binding execContext
def dnRoleName
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
CM4RESTClient restCli
def cm4Ip = context.attributes[managerServiceName].ipAddress
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HDFS as String


//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hdfs-slave-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hdfs-slave-config.groovy: REST API not available! exiting"
	System.exit(1)
}

//name
dnRoleName = service+"-"+dnRole.toLowerCase()+"-"+instanceId

//data node adding
println "hdfs-slave-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${dnRole}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, dnRole, hostname, dnRoleName)) ){
	println "hdfs-slave-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${dnRole}) .... exiting"
	System.exit(1)
}

//data node conf
def dnConfigMap = [items:[[name:'dfs_data_dir_list', value:'/mnt/dfs/dn1,/mnt/dfs/dn2,/mnt/dfs/dn3'], [name:'dfs_datanode_failed_volumes_tolerated', value:config.dnFailedVolumeTolerated]]]

println "hdfs-slave-config.groovy: about to configure the hdfs namenode: properties ares: ${dnConfigMap}..."
if (!(cmdSucceeded = restCli.configService(cluster, service, dnRoleName, dnConfigMap)) ){
	println "hdfs-slave-config.groovy: Failed to configure the node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , roleName(${dnRoleName}) .... exiting "
	System.exit(1)
}
