/*******************************************************************************
 * start script for cdh4 node host
 *******************************************************************************/

 import org.cloudifysource.dsl.context.ServiceContextFactory
 
 def context = ServiceContextFactory.getServiceContext()
 //def config = new ConfigSlurper().parse(new File("cdh4-node-service.properties").toURL())
 def builder = new AntBuilder()
 def startScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-node-start.sh"
 
 builder.sequential {
	 echo(message:"cdh4-node-start.groovy: Running ${startScriptSh}...")
	 chmod(file: startScriptSh, perm:"ugo+rx")
	 exec(executable: startScriptSh,failonerror: "true")
 }
 
/* println "cdh4-node-start.groovy: Starting cloudera manager agents "
 builder.sequential {
	 exec(executable: "sudo", osfamily:"unix", failonerror:"true") {
			 arg(line:"service cloudera-scm-agent start")
	 }
 }*/