application {
	name = "cloudera"
	
	service {
		name = "cdh4-manager"
	}
	
	service {
		name = "cdh4-master"
		dependsOn = ["cdh4-manager"]
	}
	
	service {
		name = "cdh4-slave"
		dependsOn = ["cdh4-manager", "cdh4-master"]
	}
	
	service {
		name = "mysql"
	}
}
