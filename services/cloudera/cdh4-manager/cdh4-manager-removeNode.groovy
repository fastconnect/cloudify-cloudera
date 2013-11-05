/*******************************************************************************
 * remodeNode script for cdh4-master
 *
 * @param hostname
 * 
 *******************************************************************************/

import org.cloudifysource.dsl.context.ServiceContextFactory

/*params*/
def hostname = args[0]
 def context = ServiceContextFactory.getServiceContext()
 def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/cdh4-manager-service.properties").toURL())
 def builder = new AntBuilder()
 def jsonDoc
 def requestLine
 def resourceUrl
 def cmdSucceeded
 CM4RESTClient restCli

 
def cm4Ip = context.attributes.thisService.ipAddress
 
 restCli = new CM4RESTClient(cm4Ip, config.apiVersion)
 
 def cmApiUrl = "http://${cm4Ip}:${config.port}/api/v3"
 
 println "cdh4-manager-removeNode.groovy: api url is: ${restCli.apiUrl}..."
 
 println "cdh4-manager-removeNode.groovy: About to remove a node from the cluster. propertiess are: hostname(${hostname}).... "
 jsonDoc = [items:[hostname]]
 
 println "cdh4-manager-removeNode.groovy: trying to decommission the host ${hostname}..."
 if (!(cmdSucceeded = restCli.execCmCommand("hostsDecommission", jsonDoc))) {
	 println "cdh4-manager-removeNode.groovy: failled to decommision node... exiting"
	 System.exit(0)
 }
  
 println "cdh4-manager-removeNode.groovy: node decommisionned"
  
 println "cdh4-manager-removeNode.groovy: trying to delete the node ${hostname}..."
 if (!(cmdSucceeded = restCli.deleteHost(hostname))) {
	 println "cdh4-manager-removeNode.groovy: failled to delete the node ${hostname}... exiting"
	 System.exit(1)
 }
 
 println "cdh4-manager-removeNode.groovy: node ${hostname} removed"
 
 //println "cdh4-manager-removeNode.groovy: About to add a node to the cluster. propertiess are: hostname(${hostname}),  IP(${ip}), servie(${service}) , role(${role}) .... "
 