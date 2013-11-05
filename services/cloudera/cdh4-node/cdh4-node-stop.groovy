/*******************************************************************************
 * stop script for cdh4 node host
 *******************************************************************************/

 import org.cloudifysource.dsl.context.ServiceContextFactory
 
 def context = ServiceContextFactory.getServiceContext()
 def builder = new AntBuilder()
 def stopScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-node-stop.sh"

 //tagging the instance as not running
 context.attributes.thisInstance.isReady = false
 
 builder.sequential {
	 echo(message:"cdh4-node-stop.groovy: Running ${stopScriptSh}...")
	 chmod(file: stopScriptSh, perm:"ugo+rx")
	 exec(executable: stopScriptSh,failonerror: "true")
 }
 