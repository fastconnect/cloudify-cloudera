import org.cloudifysource.dsl.context.ServiceContextFactory
import groovy.util.XmlParser
import groovy.xml.XmlUtil

context = ServiceContextFactory.getServiceContext()
def builder = new AntBuilder()
config = new ConfigSlurper().parse(new File("cdh4-master-service.properties").toURL())

def filePath = "/etc/hadoop/conf/core-site.xml"
def tmpFilePath = "/tmp/core-site.xml" 


builder.sequential{
	copy(file:filePath, tofile:tmpFilePath, overwrite:"true")
	//copy(file:tmpFilePath, todir:"/home/vagrant", overwrite:"true")
}

// configure Hadoop core
def core_site = new XmlSlurper().parse(tmpFilePath)
core_site.appendNode{
		property {
			name("fs.s3.awsAccessKeyId")
			value("${config.aws.accessKey}")
		}
}
core_site.appendNode{
	property {
		name("fs.s3.awsSecretAccessKey")
		value("${config.aws.secretAccessKey}")
	}
}

def writer = new FileWriter(tmpFilePath)
def xmlString = XmlUtil.serialize( core_site )
writer.write(xmlString)
writer.close()

builder.sequential{
	exec(executable: "sudo", failonerror:"false"){
		arg(value:"cp")
		arg(value:tmpFilePath)
		arg(value:filePath)
	}
}