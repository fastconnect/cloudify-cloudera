import org.cloudifysource.dsl.utils.ServiceUtils

service {
	extend "../cdh4-node"
	name "cdh4-slave"
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 10
	elastic true

	compute {
		// http://fastconnect.org/pub/cloudify/boxes/precise64-cdh4.box
		template "CDH4_NODE_LINUX"
	}

	lifecycle{

		start "cdh4-slave-start.groovy"

		startDetection {

			def test = ServiceUtils.isPortOccupied("${context.privateAddress}",agentListeningPort)
			println "IS PORT OCCUPIED (cm4-agent)${agentListeningPort} on ${context.privateAddress}--"+test
			if(!test) return false

			//case using mongo
			if (context.attributes.thisApplication.useMongo == true){
				def ports = []
				ports.add(context.attributes.thisInstance["mongoDPort"])
				test = ServiceUtils.arePortsOccupied(ports)
				println "IS PORT OCCUPIED (monogd)${ports} on localhost--"+test
			}
			
			return test
		}
	}

	/*customCommands ([
		"updateHosts" : "updateHosts.groovy"
	])
*/


}