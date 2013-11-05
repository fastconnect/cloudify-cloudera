/*******************************************************************************
 * deploy client config of alll services on all nodes of the cluster
 *******************************************************************************/

import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext()
def managerServiceName = "cdh4-manager"
def cluster = context.attributes[managerServiceName].cluster.name as String
def apiVersion = context.attributes[managerServiceName].apiVersion
def cmdSucceeded
CM4RESTClient restCli

def cm4Ip = context.attributes[managerServiceName].ipAddress

println "deployClientConfig.groovy: About to deploy the client config of cluster (${cluster}) on all nodes  .... "
restCli = new CM4RESTClient(cm4Ip, apiVersion)
println "deployClientConfig.groovy: api url is: ${restCli.apiUrl}..."

cmdSucceeded = restCli.clusterDeployClientConf(cluster)

if(!cmdSucceeded) {
	println "deployClientConfig.groovy: Failed to deploy the client config of cluster (${cluster}) on all nodes  .... "
	return
}
println "deployClientConfig.groovy: client conf of cluster (${cluster}) deployed on all nodes "
