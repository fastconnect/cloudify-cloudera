import com.gmongo.GMongo
import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

context = ServiceContextFactory.getServiceContext()

def hostname = args[0]
def ip = args[1]
def port = args[2]

println "cdh4-master-addMongoShard.groovy: addShard ${hostname}:${port} (${ip})"

if(context.attributes.thisApplication.useMongo != true) {
	throw new IllegalStateException("mongodb service not used on the cluster");
}


// add the shard in the sharding config if needed
mongoSPort = context.attributes.thisInstance["mongoSPort"] as int
mongo = new GMongo("127.0.0.1", mongoSPort)
configDB = mongo.getDB("config")
adminDB = mongo.getDB("admin")

shard = configDB.shards.findOne(host:"${hostname}:${port}".toString())
if(!shard){
	println "cdh4-master-addMongoShard.groovy: adding shard in mongo ${hostname}:${port}"
	result = adminDB.command(["addshard":"${hostname}:${port}".toString(), "name":hostname])
	println "cdh4-master-addMongoShard.groovy: add shard result: ${result}"
}else{
	println "cdh4-master-addMongoShard.groovy: ${hostname}:${port} is already a shard of the cluster... skipping"
}