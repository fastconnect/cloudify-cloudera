/*******************************************************************************
 * postStart script for cdh4 master host
 *******************************************************************************/

 import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceInstance
 
 def context = ServiceContextFactory.getServiceContext()
 def config = new ConfigSlurper().parse(new File("cdh4-master-service.properties").toURL())
 def builder = new AntBuilder()
 Binding execContext
 def managerServiceName = "cdh4-manager"
 def state = "MASTER"
 

  //tagging the instance as running
 context.attributes.thisInstance.isReady = true	
  
  println "cdh4-master-postStart.groovy: trying to contact cloudera manager service.... "
 
 def cm4Service = context.waitForService("cdh4-manager", 20, TimeUnit.SECONDS)
 
 if(cm4Service==null) {
	println "cdh4-master-postStart.groovy: no cloudera manager service found!! exiting the postStart routine.... "
	  System.exit(0)
 }
 //def cm4Instance = cm4Service.waitForInstances(1, 40, TimeUnit.SECONDS)[0]
 
 def cm4Ip = context.attributes[managerServiceName].ipAddress
 def cm4HostName =  context.attributes[managerServiceName].hostAlias
 
 println "cdh4-master-postStart.groovy: got cloudera manager service!! Hostname is ${cm4HostName} and Ip is ${cm4Ip}"
 println "cdh4-master-postStart.groovy: configuring the node for services. state is <${state}>  . "
 
//configuring the node on the manager
def args = [ state ] as String[]
execContext = new Binding(args)
new GroovyShell(execContext).evaluate(new File("cdh4-node-configNode.groovy"))


//for the usage of pig shell in the hue ui
builder.sequential {
	exec(executable: "sudo", osfamily:"unix", failonerror:"true") {
			arg(line:"chown root:hue /usr/share/hue/apps/shell/src/shell/build/setuid")
	}
	exec(executable: "sudo", osfamily:"unix", failonerror:"true") {
		arg(line:"chmod 4750 /usr/share/hue/apps/shell/src/shell/build/setuid")
	}
}
 
//def currentInstance = (context.waitForService(context.getServiceName(), 20, TimeUnit.SECONDS).getInstances().find{ it.instanceId == context.instanceId }) as ServiceInstance
 //cm4Instance.invoke("addNode", currentInstance.getHostName() as String, currentInstance.getHostAddress() as String, currentInstance.getInstanceId() as String, "MASTER" )
// cm4Instance.invoke("addNode", currentInstance.getHostName() as String, currentInstance.getHostAddress() as String, currentInstance.getInstanceId() as String, "cluster1", "hdfs1", "SECONDARYNAMENODE" )
// cm4Instance.invoke("addNode", currentInstance.getHostName() as String, currentInstance.getHostAddress() as String, currentInstance.getInstanceId() as String, "cluster1", "mapReduce1", "JOBTRACKER" )
 
 