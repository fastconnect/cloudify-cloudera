/*******************************************************************************
 * Install script for cloudera manager
 *******************************************************************************/

 import org.cloudifysource.dsl.context.ServiceContextFactory
 
 def context = ServiceContextFactory.getServiceContext()

 //def config = new ConfigSlurper().parse(new File("apache-service.properties").toURL())

 
 def builder = new AntBuilder()
 def installScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-manager-install.sh"
 
 builder.sequential {
	 echo(message:"cdh4-manager-install.groovy: Running ${installScriptSh}...")
	 chmod(file: installScriptSh, perm:"ugo+rx")
	 exec(executable: installScriptSh,failonerror: "true")
 }
 
 println "cdh4-manager-install.groovy: Finished to run ${context.getServiceDirectory()}/scripts-sh/cdh4-manager-install.sh"
