
/*******************************************************************************
 * start script for cloudera manager
 *******************************************************************************/
import org.cloudifysource.dsl.context.ServiceContextFactory
  
 def context = ServiceContextFactory.getServiceContext()
 def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/cdh4-manager-service.properties").toURL())
def cm4Ip = context.attributes.thisService.ipAddress
 def cmUrl = ""
 
 def builder = new AntBuilder()
 def stopScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-manager-stop.sh"
 
 println "cdh4-manager-prestart.groovy: stopping cdh4-manager services..."
 

 builder.sequential {
	 exec(executable: "sudo", osfamily:"unix", failonerror:"false") {
			 arg(line:"service cloudera-scm-server stop")
	 }
	 exec(executable: "sudo", osfamily:"unix", failonerror:"false") {
			 arg(line:"service cloudera-scm-server-db stop")
	 }
 }
 
/*
 builder.sequential {
	 echo(message:"cdh4-manager-start.groovy: Running ${stopScriptSh}...")
	 chmod(file: stopScriptSh, perm:"ugo+rx")
	 exec(executable: stopScriptSh,failonerror: "true")
 }*/
   
 return

 