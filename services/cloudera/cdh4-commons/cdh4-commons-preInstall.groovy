/*******************************************************************************
 * preInstall script for every nodes
 *******************************************************************************/

 import org.cloudifysource.dsl.context.ServiceContextFactory
import org.hyperic.sigar.OperatingSystem
 
 def context = ServiceContextFactory.getServiceContext()

 def config = new ConfigSlurper().parse(new File("${context.serviceName}-service.properties").toURL())

 def builder = new AntBuilder()
 
 
 def os = OperatingSystem.getInstance()
 def currVendor=os.getVendor()
 def isLinux
 switch (currVendor) {
	 case ["Ubuntu", "Debian", "Mint"]:
		 isLinux=true
		 break
	 case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:
		 isLinux=true
		 break
	 case ~/.*(?i)(Microsoft|Windows).*/:
		 isLinux=false
		 break
	 default: throw new Exception("Support for ${currVendor} is not implemented")
 }
 
 if ( isLinux ) {
	 
	 def preInstallScriptSh = "${context.serviceDirectory}/prepareHost.sh"
	 	 
	 println "${context.serviceName}-preinstall.groovy: Running ${preInstallScriptSh} os is ${currVendor}..."
	 
	 builder.sequential {
		 chmod(file:preInstallScriptSh, perm:"ugo+rx")
		 exec(executable: preInstallScriptSh, failonerror: "true") {
		 	arg(value: "precise")
		 	arg(value: "${config.clouderaManagerVersion}")
		 	arg(value: "${config.clouderaHadoopVersion}")
		 }
	 }
	 
	 println "${context.serviceName}-preinstall.groovy: Host ${context.getPrivateAddress()} ready for installations!"
 }else {
 	
 	throw new Exception("Support for ${currVendor} is not implemented") 
 }
