import com.gmongo.GMongo
import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

context = ServiceContextFactory.getServiceContext()

def dbName = args[0]
def collectionName = args[1]
def shardKey = args[2]

println "cdh4-master-shardCollection.groovy: collection is ${dbName}.${collectionName}"
println "cdh4-master-shardCollection.groovy: shard Key is ${shardKey}. using hash pattern "

if(context.attributes.thisApplication.useMongo != true) {
	throw new IllegalStateException("mongodb service not used on the cluster");
}

mongoSPort = context.attributes.thisInstance["mongoSPort"] as int
mongo = new GMongo("127.0.0.1", mongoSPort)
configDB = mongo.getDB("config")
adminDB = mongo.getDB("admin")

shardDb = configDB.databases.findOne(_id:"${dbName}".toString())

if(shardDb ==null || !shardDb.partitioned) {
	println "cdh4-master-shardCollection.groovy: database <${dbName}> is not yet shard! shard it before!"
	return
}

/*if(shardDb ==null || !shardDb.partitioned) {
	println "cdh4-master-shardCollection.groovy: database <${dbName}> is not yet shard!"
	println "cdh4-master-shardDataBase.groovy: enabling sharding on database mongo ${dbName}"
	result = adminDB.command(["enablesharding":"${dbName}".toString()])
	println "cdh4-master-shardDataBase.groovy: enable sharding result: ${result}"
	return
}*/

println "cdh4-master-shardCollection.groovy: sharding the collection ${dbName}.${collectionName}"
result = adminDB.command(["shardCollection":"${dbName}.${collectionName}".toString(), key:[(shardKey):"hashed"]])
println "cdh4-master-shardCollection.groovy: sharding result result: ${result}"
