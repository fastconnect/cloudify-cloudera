import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.dsl.context.ServiceContextFactory

def ant = new AntBuilder()
def context = ServiceContextFactory.serviceContext
def userHome = System.properties["user.home"]
def installDir = "${userHome}/.cloudify/${context.serviceName}-${context.instanceId}"

ant.mkdir(dir:"${installDir}/weblogic")

def scriptName = args[0]

println "mysql_query_file.groovy: download script file "
ant.get(src:"http://25.0.0.1/vagrant_shared_files/${scriptName}", dest:"${installDir}/${scriptName}", skipexisting:false)

def scriptFilePath = "${installDir}/${scriptName}"

println "mysql_query_file.groovy: execute script file "
args = new Object[4]
args[0] = "demo"
args[1] = "p1234"
args[2] = "demo"
args[3] = "source ${scriptFilePath}"

evaluate(new File("${context.serviceDirectory}/mysql_query.groovy"))