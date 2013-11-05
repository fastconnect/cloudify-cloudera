import org.cloudifysource.dsl.context.ServiceContextFactory
import com.groovyclouds.aws.*

import java.text.DateFormat
import java.text.SimpleDateFormat


def context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("cdh4-master-service.properties").toURL())
def builder = new AntBuilder()

if(args.length <= 0) {
	println "cdh4-master-s3hdfsEmport.groovy: usage: invoke cdh4-master s3hdfsEmport <hdfsSourceDir> [s3DestFile]"
	return "usage: invoke cdh4-master s3hdfsEmport <hdfsSourceDir> [s3DestFile]"
}

//assert args.length<=0, "usage: invoke cdh4-master s3hdfsEmport <hdfsSourceDir> [s3DestFile]"

def hdfsSourceDir = args[0]
def s3DestFile
DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy~HH.mm.ss");
Date date = new Date();
tmpDestPath = "/tmp/custom-export-"+ dateFormat.format(date).toString()

if(args.length>1){
	s3DestFile = args[1]
}else{
	splited = hdfsSourceDir.split("/")
	s3DestFile = splited[splited.length-1].toString()
	println "cdh4-master-s3hdfsEmport.groovy: no s3 destination file name provided!! using ${s3DestFile}.tar.gz as destination filename"
}

// Check if there are S3 properties
if (config.aws) {

	assert config.aws.accessKey, ' s3 accessKey should not be empty'
	assert config.aws.secretAccessKey, 's3 secretAccessKey should not be empty'
	assert config.aws.location.bucket, 's3 bucket should not be empty'
	
	def tarFileName = "${s3DestFile}.tar.gz"
	//def tmpDestTar = "${tmpDestPath}/${tarFileName}"
	
	builder.sequential {
		mkdir(dir:"${tmpDestPath}")
		exec(executable: "hadoop", failonerror: "true"){
			arg(value:"fs")
			arg(line:"-get ${hdfsSourceDir} ${tmpDestPath}")
		}
	}
	
	builder.sequential {
		tar(destfile: "${tmpDestPath}/${tarFileName}",  basedir: "${tmpDestPath}/${s3DestFile}", includes: "**/*")
	}
	
	def path
	S3Driver s3 = S3Driver.setupInstance(config.aws.accessKey, config.aws.secretAccessKey, null, config.aws.s3DriverConf)
		
	if (!s3.bucketExists(config.aws.location.bucket)) {
		s3.createBucket(config.aws.location.bucket)
	}
	
	if (!config.aws.location.folderPath) {
		path = tarFileName
	}
	else {
		path = "${config.aws.location.folderPath}/${tarFileName}"
	}
	
	s3.addFileObject(config.aws.location.bucket, path, "${tmpDestPath}/${tarFileName}", "dump")
	
	ownerId = ""
	ownerDisplayName=""
	
	s3.setObjectACL(config.aws.location.bucket, path, ownerId, ownerDisplayName,
		[[type:S3Grant.GRANTEE_GROUP, Permission:S3Grant.PERMISSION_READ, groupType:S3Grant.GROUP_ALL_USERS]])
}


