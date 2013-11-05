/*******************************************************************************
 * Install script for cdh4-node
 *******************************************************************************/

import org.cloudifysource.dsl.context.ServiceContextFactory
 
 def context = ServiceContextFactory.getServiceContext()
 def builder = new AntBuilder()
 def installScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-node-install.sh"
  
 builder.sequential {
	 echo(message:"cdh4-node-install.groovy: Running ${installScriptSh}...")
	 chmod(file: installScriptSh, perm:"ugo+rx")
	 exec(executable: installScriptSh, failonerror: "true")
 }
