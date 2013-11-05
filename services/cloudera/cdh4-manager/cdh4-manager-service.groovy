import java.util.concurrent.Semaphore

import org.cloudifysource.dsl.utils.ServiceUtils


service {
	extend "../cdh4-commons"
	
	name "cdh4-manager"

	numInstances 1
	
	elastic false
	
	compute {
		// http://fastconnect.org/pub/cloudify/boxes/precise64-cm4.box
		template "CDH4_MANAGER_LINUX"
	}
	
	lifecycle {
		
		init "cdh4-manager-init.groovy"
	
		install "cdh4-manager-install.groovy"
				
		preStart "cdh4-manager-preStart.groovy"
		
		start "cdh4-manager-start.groovy"
		
		postStart "cdh4-manager-postStart.groovy"
		
		stop "cdh4-manager-stop.groovy"
				
		startDetectionTimeoutSecs 480
		
		startDetection {		
			//if in postStart then return true
			if(context.attributes.thisService.containsKey("inPostStart") && context.attributes.thisService.inPostStart==true){
				println "Start Detection: service is in postStart cycle..."
				return true
			}
			println "IS PORT OCCUPIED ${port} --"+ ServiceUtils.isPortOccupied(port) +"____"+ServiceUtils.isPortOccupied("${context.privateAddress}", port) +"____"+ServiceUtils.isPortOccupied("${context.publicAddress}", port)
			return ServiceUtils.isPortOccupied(port)
		}
				
		
		details {
			def currPublicIP = context.isLocalCloud() ? InetAddress.localHost.hostAddress : context.publicAddress

			def managerUrl = "http://${currPublicIP}:${port}"
			println "cdh4-manager-service.groovy: cloudera manager URL is ${managerUrl}"
			
			return [
				"Coudera Manager URL":"<a href=\"${managerUrl}\" target=\"_blank\">${managerUrl}</a>"
			]
		}
		
		
		locator {			
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=postgres,Args.*.ct=cloudera-scm-server-db")
			
			myPids.addAll(ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.ct=com.cloudera.server.cmf.Main") )
			
			println ":cdh4-manager-service.groovy: current PIDs: ${myPids}"
			return myPids
		}
	}
	
	customCommands ([
		/*"addNode" : "cdh4-manager-addNode.groovy",
		"addService":"cdh4-manager-addService.groovy",
		"removeNode" : "cdh4-manager-removeNode.groovy",
		"startService" : "cdh4-manager-startService.groovy",
		"hdfsDeployConfig":"cdh4-manager-hdfsDeployConfig.groovy",
		"deployClientConfig":"cdh4-manager-deployClientConfig.groovy",*/
		/*"updateHosts" : {
			
			hostsFileUpdater.updateHosts()	
						
		},
			//"updateHosts.groovy" ,
*/		
		"displayHosts" : "displayHosts.groovy",
		
		/*"acquireLock": {
			updateHostsLock.acquire()
		},
	
		"releaseLock" : {
			updateHostsLock.release()
		}*/
	])

	
	
}