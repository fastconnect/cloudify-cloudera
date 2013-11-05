import java.text.DateFormat
import java.text.SimpleDateFormat

import org.cloudifysource.dsl.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()
def builder = new AntBuilder()

if(args.length <= 0) {
	println "cdh4-master-hdfsImport.groovy: usage: invoke cdh4-master hdfsImport <sourceUrl> [destPath]"
	return "usage: invoke cdh4-master hdfsImport <sourceUrl> [destinationPath]"
}

def sourceUrl = args[0].toString()
def destPath
def tmpDestPath
DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy~HH.mm.ss");
Date date = new Date();
tmpDestPath = "/tmp/custom-import-"+ dateFormat.format(date).toString()
split = sourceUrl.split("/")
fileName = split[split.length-1]

if(args.length > 1){
	destPath = args[1]
}else {
	destPath = tmpDestPath
	println "cdh4-master-hdfsImport.groovy: no destination path provided!! using ${destPath} as destination folder"
}

builder.sequential {
	exec(executable: "sudo", failonerror: "false"){
		arg(line:"-u hdfs")
		arg(line:"hadoop fs")
		arg(line:"-mkdir ${destPath}")
	}
	mkdir(dir:"${tmpDestPath}")
}

println "cdh4-master-hdfsImport.groovy: getting data from ${sourceUrl} to /tmp/imports"
builder.sequential {
	get(src: sourceUrl, dest: tmpDestPath, verbose:"false" )
}

println "cdh4-master-hdfsImport.groovy: copying data from local  ${destPath} to hdfs ${destPath}..."
builder.sequential {
	exec(executable: "sudo", failonerror: "true"){
		arg(line:"-u hdfs")
		arg(line:"hadoop fs")
		arg(line:"-put ${tmpDestPath}/${fileName} ${destPath}")
	}
}

println "cdh4-master-hdfsImport.groovy: import successfull from ${sourceUrl} to hdfs ${destPath}"
return "import successfull from ${sourceUrl} to hdfs ${destPath}"
