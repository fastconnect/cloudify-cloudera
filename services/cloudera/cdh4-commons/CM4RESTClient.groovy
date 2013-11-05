import org.apache.http.conn.HttpHostConnectException;

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
/**
 * 
 * Groovy Client for Cloudera Manager REST API
 * 
 * @author Igor
 *
 */
class CM4RESTClient {

	static final String CMD_HDFSFORMAT = "hdfsFormat"
	static final String CMD_HDFS_CREATE_TEMP_DIR = "hdfsCreateTmpDir"
	static final String CMD_START = "start"
	static final String CMD_RESTART = "restart"
	static final String CMD_STOP = "stop"
	static final String CMD_DEPLOY_CLIENT_CONF = "deployClientConfig"
	static final String CMD_ZOOKEEPER_INIT = "zooKeeperInit"
	static final String CMD_HIVE_CREATE_DB_AND_TABLES = "hiveCreateMetastoreDatabaseTables"
	static final String CMD_HIVE_CREATE_WAREHOUSE = "hiveCreateHiveWarehouse"
	static final String CMD_CREATE_OOZIE_DB = "createOozieDb"
	static final String CMD_INSTALL_OOZIE_SHARE_LIB = "installOozieShareLib"
	static final String CMD_HBASE_CREATE_ROOT = "hbaseCreateRoot"
	static final String CMD_CREATE_SQOOP_USER_DIR = "createSqoopUserDir"

	String apiUrl
	String apiVersion
	String serverHost
	String clustersPath = "clusters"
	String servicesPath = "services"
	String rolesPath = "roles"
	String hostsPath = "hosts"
	String roleCommandsPAth = "roleCommands"
	String commandsPath = "commands"
	String user
	String pass
	RESTClient restCli

	public CM4RESTClient(server, apiVers){
		this.serverHost = server
		this.apiVersion = apiVers
		this.user = "admin"
		this.pass = "admin"
		this.apiUrl = "http://${serverHost}:7180/api/${apiVersion}/"
		restCli = new RESTClient(apiUrl)
		restCli.auth.basic(user, pass)
		restCli.contentType = ContentType.JSON
	}


	/***********************************************************
	 *
	 * Cloudera Manager Global
	 *
	 **********************************************************/

	/**
	 * utilities
	 */
	//test if the api is available
	boolean testAvailable(){
		try{
			restCli.head(path: 'cm/version')
		}catch(e){
			if (e instanceof HttpHostConnectException ) {
				return false
			}
		}
		return true
	}

	//test if can run a command
	boolean canRunCommand(String checkingPath) {

		String path = checkingPath+"/commands"
		def response
		response = restCli.get(path:path)
		return (response.data.items.size()==0)
	}

	/**
	 * test if the rest api is available
	 * @param timeOutInSec : the timeout in second
	 * @return true or false
	 */
	boolean isApiAvailable(timeOutInSec){
		boolean response
		def leftTime = timeOutInSec * 1000
		println "CM4RESTClient.groovy: testing the availibility of the cm4 manager rest api"
		while(!(response=testAvailable()) && leftTime>0){
			sleep(1000)
			leftTime = leftTime - 1000
		}
		return response
	}

	
	/**
	 * Clusters
	 */

	/**
	 * create a cluster
	 * @param cluster : the cluster name
	 * @return true or false
	 * 
	 */
	boolean createCluster(String cluster, String cdhVersion){
		def doc
		def response
		doc = [items:[[name:cluster, version:cdhVersion]]]

		println "CM4RESTClient.groovy: creating a cluster:<${cluster}> ; version:<${cdhVersion}>"
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${clustersPath} ; doc: ${doc}"

		try{
			response = restCli.post( path: clustersPath, body: doc)
		}catch(e){
			if(e.response.status ==500 && e.response.data.message.contains("maximum number") && e.response.data.message.contains("already been reached")) {
				println "CM4RESTClient.groovy:WARN ${e.response.status}- a cluster already exist. This CM4 Edition only support 1 cluster...skipping creation."
				println "CM4RESTClient.groovy:WARN message is: [${e.response.data.message}]"
				return true
			}
			println "CM4RESTClient.groovy:FAIL ${e.response.status}-${e.message}...failed to create a cluster:<${cluster}> ; version:<${cdhVersion}>"
			//e.printStackTrace()
			return false
		}

		println "CM4RESTClient.groovy:SUCCEED created cluster:<${cluster}> ; version:<${cdhVersion}>"
		println response.data
		return true
	}
	
	boolean addHostToCluster(String cluster, String hostName) {
		def doc
		def response
		def path = clustersPath+"/"+cluster+"/"+hostsPath
		doc = [items:[hostName]]

		println "CM4RESTClient.groovy: adding a host<${hostName}> to the cluster:<${cluster}> "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${clustersPath} ; doc: ${doc}"

		try{
			response = restCli.post( path: path, body: doc)
		}catch(e){
			if(e.response.status ==400 && e.response.data.message.contains("already belongs to")) {
				println "CM4RESTClient.groovy:WARN ${e.response.status}- the host:<${hostName}> already belongs to the cluster<${cluster}>...skipping adding."
				println "CM4RESTClient.groovy:WARN message is: [${e.response.data.message}]"
				return true
			}
			println "CM4RESTClient.groovy:FAIL ${e.response.status}-${e.message}...failed to add host<${hostName}> to the cluster:<${cluster}>"
			println "CM4RESTClient.groovy:FAIL message is: [${e.response.data.message}]"
			//e.printStackTrace()
			return false
		}

		println "CM4RESTClient.groovy:SUCCEED adding host<${hostName}> to the cluster:<${cluster}>"
		println response.data
		return true

	}

	
	/**
	 * Services
	 */

	/**
	 * add a service into a cluster
	 * @param cluster : the cluster name
	 * @param service : the service name
	 * @param type : the service type
	 * @return true or false
	 */
	boolean addService(String cluster, String service, String type){

		def doc
		def response
		def path

		type = type.toUpperCase()
		doc = [items:[[name:service, type:type]]]
		path = clustersPath+"/"+cluster+"/"+servicesPath

		println "CM4RESTClient.groovy: adding a service:<${service}> ; type:<${type}> ; cluster:<${cluster}> "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${path} ; doc: ${doc}"

		try{
			response = restCli.post( path: path, body: doc)
		}catch(e){
			if(e.response.status ==400 && e.response.data.message.contains("insert into SERVICES")) {
				println "CM4RESTClient.groovy:WARN ${e.response.status}- this service name already exist...skipping adding."
				println "CM4RESTClient.groovy:WARN message is: [${e.response.data.message}]; [${e.response.data.causes}]"
				return true
			}
			println "CM4RESTClient.groovy:FAIL ${e.response.status}-${e.message}...failed to add a service:<${service}> ; type:<${type}> ; cluster:<${cluster}>"
			println "CM4RESTClient.groovy:FAIL message is: [${e.response.data.message}]"

			//e.printStackTrace()
			return false
		}

		println "CM4RESTClient.groovy:SUCCEED added service:<${service}> ; type:<${type}> ; cluster:<${cluster}> "
		println response.data
		return true
	}


	/**
	 * add a role in a service
	 * 
	 *@param cluster : the cluster name
	 * @param service : the service name
	 * @param roleType : the role type to add
	 * @param hostId : the host representing the role
	 * @param roleName : the (displayed) role name in the service
	 * @return true or false
	 */
	boolean addRole(String cluster, String service, String roleType, String hostId, String roleName){

		def doc
		def response
		def path


		roleType = roleType.toUpperCase()
		doc = [items:[[name:roleName, type:roleType, hostRef:[hostId:hostId]]]]
		path = clustersPath+"/"+cluster+"/"+servicesPath+"/"+service+"/"+rolesPath

		println "CM4RESTClient.groovy: adding a roleType:<${roleType}> (hostId:${hostId}, roleName:${roleName}) to service:<${service}> ; cluster:<${cluster}> "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${path} ; doc: ${doc}"

		try{
			response = restCli.post( path: path, body: doc)
		}catch(e){
			if(e.response.status ==400 && 
				(   e.response.data.message.contains("Duplicate") 
			     || e.response.data.message.contains("duplicate")
			     || e.response.data.message.contains("maximum number"))) {
				println "CM4RESTClient.groovy:WARN ${e.response.status}- this role name already exist...skipping adding."
				println "CM4RESTClient.groovy:WARN message is: [${e.response.data.message}]"
				return true
			}
			println "CM4RESTClient.groovy:FAIL ${e.response.status}-${e.message}...failed to add a roleType:<${roleType}> (hostId:${hostId}, roleName:${roleName}) to service:<${service}> ; cluster:<${cluster}>"
			println "CM4RESTClient.groovy:FAIL message is: [${e.response.data.message}]"
			//println e.response.data
			//e.printStackTrace()
			return false
		}

		println "CM4RESTClient.groovy:SUCCEED added roleType:<${roleType}> (hostId:${hostId}, roleName:${roleName}) to service:<${service}> ; cluster:<${cluster}> "
		println response.data
		return true
	}



	/**
	 * configure a service, either at role level or at service level
	 * 
	 * @param cluster : the cluster name
	 * @param service : the service name
	 * @param roleName : the role name. set to empty if configuration is at the service level and not at the role level
	 * @param configMap :
	 * @return true or false
	 */
	boolean configService(String cluster, String service, String roleName, configMap){

		def response
		def path
		def roleConfPath = roleName==null|roleName.isEmpty()?"":"roles/${roleName}/"

		path = clustersPath+"/"+cluster+"/"+servicesPath+"/"+service+"/"+roleConfPath+"config"

		println "CM4RESTClient.groovy: configuring the service:<${service}> ; role:<${roleName}> ; cluster:<${cluster}> "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${path} ; config: ${configMap}"

		try{
			response = restCli.put( path: path, body: configMap)
		}catch(e){
			println "CM4RESTClient.groovy:FAIL ${e.response.status}-${e.message}...failed to configure the service:<${service}> ; role:<${roleName}> ; cluster:<${cluster}>"
			println "CM4RESTClient.groovy:FAIL message is: [${e.response.data.message}]"
			//e.printStackTrace()
			return false
		}

		println "CM4RESTClient.groovy:SUCCEED Configured the service:<${service}> ; role:<${roleName}> ; cluster:<${cluster}> "
		println response.data
		return true
	}


	/**
	 * 
	 * execute a command on a service
	 * 
	 * @param cluster
	 * @param service
	 * @param command
	 * @param bodyDoc : a map if required by the command
	 * @return true or false
	 */
	boolean execServiceCommand(String cluster, String service, String command, bodyDoc ){

		//def doc
		def response
		def path
		def resultMsg
		def succeeded

		path = clustersPath+"/"+cluster+"/"+servicesPath+"/"+service+"/"+commandsPath+"/"+command

		println "CM4RESTClient.groovy: executing command <${command}> on service:<${service}> ;  cluster:<${cluster}> "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${path} "

		while(!canRunCommand( clustersPath+"/"+cluster+"/"+servicesPath+"/"+service )){
			println "waiting for others commands to finih"
			sleep(100)
		}

		try{

			switch(bodyDoc) {

				case ["",null]:
					response = restCli.post( path: path)
					break

				default:
					response = restCli.post( path: path,
					body: bodyDoc)
			}

			def cmd = response.data
			def cmdId = cmd.id
			def isActive = cmd.active as boolean

			//if active, waiting for his end
			if(isActive){
				def cmdResp
				def tempPath = "commands/${cmdId}"

				while (isActive) {
					sleep(1000)
					cmdResp = (restCli.get(path:tempPath)).data
					isActive = cmdResp.active as boolean
				}

				succeeded = cmdResp.success as boolean
				resultMsg = cmdResp.resultMessage
			}else {
				succeeded = false
				resultMsg = cmd.resultMessage
			}

		}catch(e){
			def errCode = e.response!=null?e.response.status+"-":""
			def msg = (e.response!=null && e.response.data!=null)?e.response.data.message:"...no specific message..."
			println "CM4RESTClient.groovy:FAIL ${errCode}${e.message}...failed to execute command <${command}> on service:<${service}> ;  cluster:<${cluster}> "
			println "CM4RESTClient.groovy:FAIL message is: [${msg}]"
			//e.printStackTrace()
			return false
		}

		def tag = succeeded?"SUCCESS":"FAIL"
		def boutDeMsg = succeeded?"executed":"failed to execute"
		println "CM4RESTClient.groovy:${tag} ${boutDeMsg} command <${command}> on service:<${service}> ;  cluster:<${cluster}> "
		println "CM4RESTClient.groovy:${tag} result message is: ${resultMsg}"
		return succeeded

	}


	/**
	 * execute a command on the cm4 level
	 * 
	 * @param command
	 * @param bodyDoc
	 * @return
	 */
	boolean execCmCommand( String command, bodyDoc ){

		//def doc
		def response
		def path
		def resultMsg
		def succeeded

		path = "cm/"+commandsPath+"/"+command

		println "CM4RESTClient.groovy: executing command <${command}> on cloudera manager "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${path} "

		while(!canRunCommand( "cm")){
			println "waiting for others commands to finih"
			sleep(100)
		}

		try{

			switch(bodyDoc) {

				case ["",null]:
					response = restCli.post( path: path)
					break

				default:
					response = restCli.post( path: path,
					body: bodyDoc)
			}

			def cmd = response.data
			def cmdId = cmd.id
			def isActive = cmd.active as boolean

			//if active, waiting for his end
			if(isActive){
				def cmdResp
				def tempPath = "commands/${cmdId}"

				while (isActive) {
					sleep(1000)
					cmdResp = (restCli.get(path:tempPath)).data
					isActive = cmdResp.active as boolean
				}

				succeeded = cmdResp.success as boolean
				resultMsg = cmdResp.resultMessage
			}else {
				succeeded = false
				resultMsg = cmd.resultMessage
			}

		}catch(e){
			def errCode = e.response!=null?e.response.status+"-":""
			def msg = (e.response!=null && e.response.data!=null)?e.response.data.message:"...no specific message..."
			println "CM4RESTClient.groovy:FAIL ${errCode}${e.message}...failed to execute command <${command}> on cloudera manager "
			println "CM4RESTClient.groovy:FAIL message is: [${msg}]"
			//e.printStackTrace()
			return false
		}

		def tag = succeeded?"SUCCESS":"FAIL"
		def boutDeMsg = succeeded?"executed":"failed to execute"
		println "CM4RESTClient.groovy:${tag} ${boutDeMsg} command <${command}> on cloudera manager "
		println "CM4RESTClient.groovy:${tag} result message is: ${resultMsg}"
		return succeeded

	}


	/**
	 * 
	 * execute a command on the role level
	 * 
	 * @param cluster
	 * @param service
	 * @param command
	 * @param bodyDoc
	 * @return
	 */
	boolean execRoleCommand(String cluster, String service, String command, bodyDoc ){

		//def doc
		def response
		def path
		def resultMsg
		def succeeded

		path = clustersPath+"/"+cluster+"/"+servicesPath+"/"+service+"/"+roleCommandsPAth+"/"+command

		println "CM4RESTClient.groovy: executing command <${command}> on roles level of service:<${service}> ;  cluster:<${cluster}> "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${path} doc:${bodyDoc}"

		while(!canRunCommand( clustersPath+"/"+cluster+"/"+servicesPath+"/"+service )){
			println "waiting for others commands to finih"
			sleep(100)
		}

		try{

			switch(bodyDoc) {

				case ["",null]:
					response = restCli.post( path: path)
					break

				default:
					response = restCli.post( path: path,
					body: bodyDoc)
			}

			def cmd = response.data.items[0]
			def cmdId = cmd.id
			def isActive = cmd.active as boolean

			//if active, waiting for his end
			if(isActive){
				def cmdResp
				def tempPath = "commands/${cmdId}"

				while (isActive) {
					sleep(1000)
					cmdResp = (restCli.get(path:tempPath)).data
					isActive = cmdResp.active as boolean
				}

				succeeded = cmdResp.success as boolean
				resultMsg = cmdResp.resultMessage
			}else {
				succeeded = false
				resultMsg = cmd.resultMessage
			}

		}catch(e){
			def errCode = e.response!=null?e.response.status+"-":""
			def msg = (e.response!=null && e.response.data!=null)?e.response.data.message:"...no specific message..."
			println "CM4RESTClient.groovy:FAIL ${errCode}${e.message}...failed to execute command <${command}> on roles level of service:<${service}> ;  cluster:<${cluster}> "
			println "CM4RESTClient.groovy:FAIL message is: [${msg}]"
			//e.printStackTrace()
			return false
		}

		def tag = succeeded?"SUCCESS":"FAIL"
		def boutDeMsg = succeeded?"executed":"failed to execute"

		println "CM4RESTClient.groovy:${tag} ${boutDeMsg} command <${command}> on roles level of service:<${service}> ;  cluster:<${cluster}> "
		println "CM4RESTClient.groovy:${tag} result message is: ${resultMsg}"
		return succeeded

	}
	
	boolean clusterDeployClientConf(String cluster) {
		//def doc
		def response
		def path
		def resultMsg
		def succeeded

		path = clustersPath+"/"+cluster+"/"+commandsPath+"/"+CMD_DEPLOY_CLIENT_CONF

		println "CM4RESTClient.groovy: executing command <${CMD_DEPLOY_CLIENT_CONF}> on cluster <${cluster}> "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${path} "

		while(!canRunCommand(clustersPath+"/"+cluster)){
			println "waiting for others commands to finih"
			sleep(100)
		}

		try{
			response = restCli.post( path: path)
			def cmd = response.data
			def cmdId = cmd.id
			def isActive = cmd.active as boolean

			//if active, waiting for his end
			if(isActive){
				def cmdResp
				def tempPath = "commands/${cmdId}"

				while (isActive) {
					sleep(1000)
					cmdResp = (restCli.get(path:tempPath)).data
					isActive = cmdResp.active as boolean
				}

				succeeded = cmdResp.success as boolean
				resultMsg = cmdResp.resultMessage
			}else {
				succeeded = false
				resultMsg = cmd.resultMessage
			}

		}catch(e){
			def errCode = e.response!=null?e.response.status+"-":""
			def msg = (e.response!=null && e.response.data!=null)?e.response.data.message:"...no specific message..."
			println "CM4RESTClient.groovy:FAIL ${errCode}${e.message}...failed to execute command <${CMD_DEPLOY_CLIENT_CONF}> on cluster <${cluster}> "
			println "CM4RESTClient.groovy:FAIL message is: [${msg}]"
			//e.printStackTrace()
			return false
		}

		def tag = succeeded?"SUCCESS":"FAIL"
		def boutDeMsg = succeeded?"executed":"failed to execute"
		println "CM4RESTClient.groovy:${tag} ${boutDeMsg} command <${CMD_DEPLOY_CLIENT_CONF}> on cluster <${cluster}> "
		println "CM4RESTClient.groovy:${tag} result message is: ${resultMsg}"
		return succeeded

	}
	
	
	boolean deleteHost(String hostName ) {
		def response
		def path

		path = hostsPath+"/"+hostName

		println "CM4RESTClient.groovy: deleting the host<${hostName}>... "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${path} "

		try{
			response = restCli.delete( path: path)
		}catch(e){
			println "CM4RESTClient.groovy:FAIL ${e.response.status}-${e.message}...failed to delete the host<${hostName}>"
			println "CM4RESTClient.groovy:FAIL message is: [${e.response.data.message}]"
			//e.printStackTrace()
			return false
		}

		println "CM4RESTClient.groovy:SUCCEED deleting the host<${hostName}> "
		println response.data
		return true
	}




	/*************************************
	 *
	 * SERVICE HDFS
	 *
	 *************************************/


	/**
	 * format the hdfs filesys by issuing the command hdfsFormat
	 * 
	 * @param cluster
	 * @param service
	 * @param nameNodeRoleName : the name of the namenode to format
	 * @return true or false
	 * 
	 */
	boolean hdfsFormat(String cluster, String service, String nameNodeRoleName) {

		def doc
		def response
		def path
		def resultMsg
		def succeeded

		path = clustersPath+"/"+cluster+"/"+servicesPath+"/"+service+"/"+roleCommandsPAth+"/hdfsFormat"
		doc = [items:[nameNodeRoleName]]
		println "CM4RESTClient.groovy: formating the namenode:<${nameNodeRoleName}> ; service:<${service}> ;  cluster:<${cluster}> "
		println "CM4RESTClient.groovy: fullPath: ${apiUrl}${path} "

		while(!canRunCommand( clustersPath+"/"+cluster+"/"+servicesPath+"/"+service )){
			println "waiting for others commands to finih"
			sleep(100)
		}

		try{
			response = restCli.post( path: path,
			body: doc)

			def cmd = response.data.items[0]
			def cmdId = cmd.id
			def isActive = cmd.active as boolean

			//if active, waiting for his end
			if(isActive){
				def cmdResp
				def tempPath = "commands/${cmdId}"

				while (isActive) {
					sleep(1000)
					cmdResp = (restCli.get(path:tempPath)).data
					isActive = cmdResp.active as boolean
				}

				succeeded = cmdResp.success as boolean
				resultMsg = cmdResp.resultMessage
			}else {
				succeeded = false
				resultMsg = cmd.resultMessage
			}

		}catch(e){
			println "CM4RESTClient.groovy:FAIL ${e.response.status}-${e.message}...failed to format the namenode:<${nameNodeRoleName}> ; service:<${service}> ;  cluster:<${cluster}> "
			println "CM4RESTClient.groovy:FAIL message is: [${e.response.data.message}]"
			//e.printStackTrace()
			return false
		}

		def tag = succeeded?"SUCCESS":"FAIL"
		def boutDeMsg = succeeded?"formated":"failed to format"

		println "CM4RESTClient.groovy:${tag} ${boutDeMsg} namenode:<${nameNodeRoleName}> ; service:<${service}> ;  cluster:<${cluster}> "
		println "CM4RESTClient.groovy:${tag} result message is: ${resultMsg}"
		return succeeded
	}


	




}