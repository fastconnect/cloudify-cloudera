/*******************************************************************************
 * stop script for cloudera manager
 *******************************************************************************/

 import org.cloudifysource.dsl.context.ServiceContextFactory
 
 def context = ServiceContextFactory.getServiceContext() 
 def builder = new AntBuilder()
 def stopScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-manager-stop.sh"

 builder.sequential {
	 echo(message:"cdh4-manager-stop.groovy: Running ${stopScriptSh}...")
	 chmod(file: stopScriptSh, perm:"ugo+rx")
	 exec(executable: stopScriptSh,failonerror: "true")
 }
 
 println "cdh4-manager-stop.groovy: cdh4-manager is down now."
 
/* println "cdh4-manager-stop.groovy: stopping cloudera manager services..."
 builder.sequential {
	 exec(executable: "sudo", osfamily:"unix", failonerror:"false") {
			 arg(line:"service cloudera-scm-server stop")
	 }
	 exec(executable: "sudo", osfamily:"unix", failonerror:"false") {
			 arg(line:"service cloudera-scm-server-db stop")
	 }
 }
  */


 
 