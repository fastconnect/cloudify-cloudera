
/*******************************************************************************
 * start script for cloudera manager
 *******************************************************************************/
import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/cdh4-manager-service.properties").toURL())
def cmUrl = ""

def builder = new AntBuilder()
def startScriptSh = "${context.serviceDirectory}/scripts-sh/cdh4-manager-start.sh"
println "cdh4-manager-start.groovy: Running ${startScriptSh}..."
builder.sequential {
	echo(message:"cdh4-manager-start.groovy: Running ${startScriptSh}...")
	chmod(file: startScriptSh, perm:"ugo+rx")
	exec(executable: startScriptSh,failonerror: "true")
}

//waiting for the rest api to be available
//sleep(30000)
cmUrl = "http://${context.publicAddress}:${config.port}"

println "cdh4-manager-start.groovy: cdh4-manager is fully up and available at  ${cmUrl}"

return

