
service {
	name "cdh4-commons"
	numInstances 1
	
	def hostsFileUpdater = new HostsFileUpdater()
	
	lifecycle {
				
		preInstall "cdh4-commons-preInstall.groovy"
		
		postInstall "cdh4-commons-postInstall.groovy"
		
	}

	
	
	customCommands ([
		
		"updateHosts" : {
			
			return hostsFileUpdater.updateHosts(context)
						
		}
	])
}