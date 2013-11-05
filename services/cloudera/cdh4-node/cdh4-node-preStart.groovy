/*******************************************************************************
 * postInstall script for cdh4 node host
 *******************************************************************************/

import java.util.concurrent.TimeUnit

import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceInstance

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("cdh4-node-service.properties").toURL())
def builder = new AntBuilder()
def myIp = context.attributes.thisInstance.ipAddress
def myHostAlias = context.attributes.thisInstance.hostAlias
def managerServiceName = "cdh4-manager"

//tagging the instance as not running
context.attributes.thisInstance.isReady = false

println "cdh4-node-preStart.groovy: Stopping hadoop-httpfs service.... "
builder.sequential {
	exec(executable: "sudo", osfamily:"unix", failonerror:"false") {
		arg(line:"service hadoop-httpfs stop")
	}
}

println "cdh4-node-preStart.groovy: Stopping sqoop2-server service.... "
builder.sequential {
	exec(executable: "sudo", osfamily:"unix", failonerror:"false") {
		arg(line:"service sqoop2-server stop")
	}
}

println "cdh4-node-preStart.groovy: Stopping oozie service.... "
builder.sequential {
	exec(executable: "sudo", osfamily:"unix", failonerror:"false") {
		arg(line:"service oozie stop")
	}
}

def stopScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-node-stop.sh"
builder.sequential {
	echo(message:"cdh4-node-preStart.groovy: Running ${stopScriptSh}...")
	chmod(file: stopScriptSh, perm:"ugo+rx")
	exec(executable: stopScriptSh,failonerror: "true")
}

cm4Service = context.waitForService("cdh4-manager", 60, TimeUnit.SECONDS)
def cm4Ip = context.attributes[managerServiceName].ipAddress

println "cdh4-node-preStart.groovy: got cloudera manager service!!Ip is ${cm4Ip}"
println "cdh4-node-preStart.groovy: customising the file /etc/cloudera-scm-agent/config.ini with: server_host:${cm4Ip} ; server_port:${config.agentServerPort} "

def agentConfFilePath = "/etc/cloudera-scm-agent/config.ini"
def agentConfFileTemp = System.properties["user.home"]+"/agent-config.ini"
def agentConfFile = new File(agentConfFilePath)
def agentConfFileText = agentConfFile.text
agentConfFileText = agentConfFileText.replaceFirst(/server_host=.*/, "server_host=${cm4Ip}")
		.replaceFirst(/server_port=.*/, "server_port=${config.agentServerPort}")
		.replaceFirst(/# listening_port=.*/, "listening_port=${config.agentListeningPort}")
		.replaceFirst(/# listening_ip=.*/, "listening_ip=${myIp}")		//bind on a specific IP (private one on EC2)
		.replaceFirst(/# listening_hostname=.*/, "listening_hostname=${myHostAlias}")  //reports a specific hostName
		

agentConfFile = new File(agentConfFileTemp)
agentConfFile.write(agentConfFileText)

builder.sequential {
	chmod(file:"${context.serviceDirectory}/sudoTee.sh", perm:'ugo+x')
	exec(executable: "${context.serviceDirectory}/sudoTee.sh", osfamily:"unix", failonerror:"true") {
		arg(value:"${agentConfFileTemp}")
		arg(value:"${agentConfFilePath}")
	}
}
