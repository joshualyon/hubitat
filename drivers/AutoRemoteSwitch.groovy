// Developer: josh@sharptools.io
metadata {
	definition (name: "AutoRemote Wifi Device", namespace: "joshualyon/testing", author: "josh") {
		capability "Sensor"
		capability "Switch"
		command "statusOn"
		command "statusOff"
		command "sendCommand", ["string"]
		command "sendCommandMessage", ["string", "string"]
        
		preferences {
			input(name: "deviceIP", type: "string", title:"AutoRemote Device IP Address", description: "", required: true, displayDuringSetup: true)
			input(name: "customCommand", type: "string", title:"Default Command to Send", description: "ex: {command}=:=on", displayDuringSetup: true)
	    	}
	}
}

def installed() {
	log.trace "Executing 'installed'"
	initialize()
	off()
}

def updated() {
	log.trace "Executing 'updated'"
	initialize()
}

private initialize() {
	log.trace "Executing 'initialize'"
}

def statusOn(){
	log.debug "Updating the status to 'on' without sending network command"
	on(false)
}

def statusOff(){
	log.debug "Updating the status to 'off' without sending network command"
	off(false)
}

def on(isRemoteCommand=true) {
	log.trace "Executing 'on'"
	sendEvent(name: "switch", value: "on", isStateChange: true)
	if(isRemoteCommand)
		sendCustomCommand("on")
}

def off(isRemoteCommand=true) {
	log.trace "Executing 'off'"
	sendEvent(name: "switch", value: "off", isStateChange: true)
	if(isRemoteCommand)
		sendCustomCommand("off")
}

def sendCommand(command){
	log.debug "Sending command: ${command}"
	sendARCommand(command)
}

def sendCommandMessage(command, message){
	log.debug "Sending command with message: ${command}=:=${message}"
	sendARCommand("${command}=:=${message}")
}

def sendCustomCommand(command){
	log.debug "Sending default custom command: ${customCommand}=:=${command}"
	sendARCommand("${customCommand}=:=${command}")
}

def sendARCommand(command){
	def path = varCommand 
	def body = "" 
	def headers = [:] 
	headers.put("HOST", "${deviceIP}:1817")

	try {
		def hubAction = new hubitat.device.HubAction(
			method: "GET",
			path: "/?message=${command}",
			body: body,
			headers: headers
        	)
		log.debug hubAction
		return hubAction
	}
	catch (Exception err) {
        	log.debug "sendARCommand error ${err} when sending HubAction ${hubAction}"
	}  
}

def parse(String description) {
	log.debug(description)
}
