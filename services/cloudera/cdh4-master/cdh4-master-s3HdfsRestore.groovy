import org.cloudifysource.dsl.context.ServiceContextFactory

import java.text.DateFormat
import java.text.SimpleDateFormat

context = ServiceContextFactory.getServiceContext()
def builder = new AntBuilder()
config = new ConfigSlurper().parse(new File("cdh4-master-service.properties").toURL())

if(args.length <= 0) {
	println "cdh4-master-s3HdfsRestore.groovy: usage: invoke cdh4-master s3HdfsRestore <sourcePath>"
	return
}


def accessKey = URLEncoder.encode(config.aws.accessKey, "utf-8")
def secretAccessKey = URLEncoder.encode(config.aws.secretAccessKey, "utf-8")

def sourcePath = args[0].toString()
//def srcUrl="s3://${accessKey}:${secretAccessKey}@cloudify-eu/${sourcePath}/"
def srcUrl="s3://${config.aws.location.bucket}/${sourcePath}"

DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd~HH.mm.ss");
Date date = new Date();
defaultRestoreName = "hdfs-restore-"+ dateFormat.format(date).toString()

def hdfsDestPath 

if(args.length <= 1) {
	println "cdh4-master-s3HdfsBackup.groovy: no hdfs path provided! will restore into /tmp/${defaultRestoreName}"
	hdfsDestPath="/tmp/${defaultRestoreName}"
}else {
	hdfsDestPath = "${args[1].toString()}"
}


println "cdh4-master-s3HdfsRestore.groovy: getting data via distcp: from ${srcUrl} to hdfs ${hdfsDestPath}"
builder.sequential {
	exec(executable: "sudo", failonerror: "false"){
		arg(line:"-u hdfs")
		arg(line:"hadoop distcp")
		arg(value:"-update")
		arg(value:srcUrl)
		arg(value:hdfsDestPath)
	}
}

println "cdh4-master-s3HdfsRestore.groovy: restore successfull from ${srcUrl} to hdfs '${hdfsDestPath}'"
return
