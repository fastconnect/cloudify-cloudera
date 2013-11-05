import org.apache.http.client.utils.URLEncodedUtils;
import org.cloudifysource.dsl.context.ServiceContextFactory
import com.groovyclouds.aws.*

import java.text.DateFormat
import java.text.SimpleDateFormat


def context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("cdh4-master-service.properties").toURL())
def builder = new AntBuilder()

def accessKey = URLEncoder.encode(config.aws.accessKey, "utf-8")
def secretAccessKey = URLEncoder.encode(config.aws.secretAccessKey, "utf-8")

//def s3DestPath = "s3://${accessKey}:${secretAccessKey}@cloudify-eu"
def s3DestPath = "s3://${config.aws.location.bucket}"
def hdfsSourcePath = "/"
DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd~HH.mm.ss");
Date date = new Date();
backupName = "hdfs-backup-"+ dateFormat.format(date).toString()
//defaultS3DestPath = "hdfs-backup-"+ dateFormat.format(date).toString()

if(args.length <= 0) {
	println "cdh4-master-s3HdfsBackup.groovy: no hdfs path provided! will backup the '/' directory "
}else {
	hdfsSourcePath = args[0].toString()
}

if(args.length <= 1) {
	println "cdh4-master-s3HdfsBackup.groovy: no s3 path provided! will backup into ${s3DestPath}/${backupName}"
	s3DestPath="${s3DestPath}/${backupName}"
}else {
	s3DestPath = "${s3DestPath}/${args[1].toString()}"
}

/*s3DestPath="${s3DestPath}/${backupName}"
s3DestPath = args.length >1 ? s3DestPath+"/"+args[1].toString() : s3DestPath
*/
println "cdh4-master-s3HdfsBackup.groovy: putting data to s3 via distcp: from hdfs ${hdfsSourcePath} to ${s3DestPath}"
builder.sequential {
	exec(executable: "sudo", failonerror: "false"){
		arg(line:"-u hdfs")
		arg(line:"hadoop distcp")
		arg(value:"-update")
		arg(value:hdfsSourcePath)
		arg(value:s3DestPath)
	}
}

println "cdh4-master-s3HdfsBackup.groovy: backup successfull from hdfs ${hdfsSourcePath} to ${s3DestPath}"
return
