import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.context.ServiceContextFactory

/*******************************************************************************
 * configure a hdfs master
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

def nnRole = "NAMENODE"
def snnRole = "SECONDARYNAMENODE"

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/services/HDFS/hdfs-config.properties").toURL())
def cmdSucceeded
def masterServiceName = "cdh4-master"
def managerServiceName = "cdh4-manager"
def cm4Ip = context.attributes[managerServiceName].ipAddress
def nnRoleName
def snnRoleName
CM4RESTClient restCli
def services = context.attributes[managerServiceName].services
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def service = services.HDFS as String

//rest client
restCli = new CM4RESTClient(cm4Ip, apiVersion)

println "hdfs-master-config.groovy: api url is: ${restCli.apiUrl}..."

//testing the availibility of the api
if(!restCli.isApiAvailable(60)){
	println "hdfs-master-config.groovy: REST API not available! exiting"
	System.exit(1)
}

//names
nnRoleName = service+"-"+nnRole.toLowerCase()+"-"+instanceId
snnRoleName = service+"-"+snnRole.toLowerCase()+"-"+instanceId

//name node adding
println "hdfs-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${nnRole}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, nnRole, hostname, nnRoleName)) ){
	println "hdfs-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${nnRole}) .... exiting"
	System.exit(1)
}

//sec name node adding
println "hdfs-master-config.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${snnRole}) .... "
if (!(cmdSucceeded = restCli.addRole(cluster, service, snnRole, hostname, snnRoleName)) ){
	println "hdfs-master-config.groovy: Failed to add a node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , role(${snnRole}) .... exiting"
	System.exit(1)
}


// configuration of roles
//name node conf
def nnConfigMap = [items:[[name:'dfs_name_dir_list', value:'/mnt/dfs/nn'], [name:'dfs_namenode_handler_count', value:config.nnHandlerCount]]]

println "hdfs-master-config.groovy: about to configure the hdfs namenode: properties ares: ${nnConfigMap}..."
if (!(cmdSucceeded = restCli.configService(cluster, service, nnRoleName, nnConfigMap)) ){
	println "hdfs-master-config.groovy: Failed to configure the node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , roleName(${nnRoleName}) .... exiting "
	System.exit(1)
}

//secondary name node conf
def snnConfigMap = [items:[[name:'fs_checkpoint_dir_list', value:'/mnt/dfs/snn']]]

println "hdfs-master-config.groovy: about to configure the hdfs secondary namenode: properties ares: ${snnConfigMap}..."
if (!(cmdSucceeded = restCli.configService(cluster, service, snnRoleName, snnConfigMap)) ){
	println "hdfs-master-config.groovy: Failed to configure the node hostname(${hostname}),  IP(${ip}), cluster(${cluster}), servie(${service}) , roleName(${snnRoleName}) .... exiting "
	System.exit(1)
}
