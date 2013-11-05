import org.cloudifysource.dsl.utils.ServiceUtils

service {
	extend "../cdh4-commons"
	name "cdh4-node"
	numInstances 1
	
	compute {
		//template "CDH4_LINUX"
		template "MEDIUM_UBUNTU"
	}
		
	lifecycle {
		
		init "${context.serviceName}-init.groovy"
			
		install "cdh4-node-install.groovy"
		
		preStart "cdh4-node-preStart.groovy"
		
		start "cdh4-node-start.groovy"
		
		postStart "${context.serviceName}-postStart.groovy"
		
		stop "cdh4-node-stop.groovy"
		
		startDetectionTimeoutSecs 240
		
		startDetection {
			println "IS PORT OCCUPIED ${agentListeningPort} on ${context.privateAddress}--"+ServiceUtils.isPortOccupied("${context.privateAddress}",agentListeningPort)
			return ServiceUtils.isPortOccupied("${context.privateAddress}",agentListeningPort)
			//return true
		}
						
		
		locator {
			def myPids= ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.ct=python,Args.*.ct=/cmf/agent/src/cmf/")
			println ":${context.serviceName}-service.groovy: current PIDs : ${myPids}"
			return myPids
		}
	}
	
	customCommands ([
		"updateCoreSiteXml" : "updateCoreSiteXml.groovy",
	])

	
	
}