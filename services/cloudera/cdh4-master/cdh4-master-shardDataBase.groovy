import com.gmongo.GMongo
import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

context = ServiceContextFactory.getServiceContext()

def dbName = args[0]

println "cdh4-master-shardDataBase.groovy: database is ${dbName}"

if(context.attributes.thisApplication.useMongo != true) {
	throw new IllegalStateException("mongodb service not used on the cluster");
}

mongoSPort = context.attributes.thisInstance["mongoSPort"] as int
mongo = new GMongo("127.0.0.1", mongoSPort)
configDB = mongo.getDB("config")
shardDb = configDB.databases.findOne(_id:"${dbName}".toString())
adminDB = mongo.getDB("admin")

if(shardDb ==null || !shardDb.partitioned) {
	println "cdh4-master-shardDataBase.groovy: enabling sharding on database mongo ${dbName}"
	result = adminDB.command(["enablesharding":"${dbName}".toString()])
	println "cdh4-master-shardDataBase.groovy: enable sharding result: ${result}"
}else {
	println "cdh4-master-shardDataBase.groovy: database <${dbName}> is already sharded!"
}