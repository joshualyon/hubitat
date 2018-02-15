/**
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Z-Wave Controller", namespace: "hubitat", author: "Anonymous") {
		capability "Actuator"
 		capability "Switch"
		capability "Refresh"
	}
}

def getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x56: 1,  // Crc16Encap
		0x70: 1,  // Configuration
	]
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, commandClassVersions)
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	return result
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    log.debug "BasicReport value was ${cmd.value}"
    def value = cmd.value ? "on" : "off"
	[name: "switch", value: value, type: "physical", descriptionText: "Switch controller ${value} reported", isStateChange: true]
}

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    log.debug "BasicSet value was ${cmd}"
	def value = cmd.value ? "on" : "off"
	[name: "switch", value: value, type: "physical", descriptionText: "Switch controller ${value} pressed", isStateChange: true]
}

def zwaveEvent(hubitat.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    log.debug "SwitchBinaryReport was ${cmd}"
    def value = cmd.value ? "on" : "off"
	[name: "switch", value: value, type: "digital", descriptionText: "SwitchBinaryReport: ${value}", isStateChange: true]
}


def zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(hubitat.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = commandClassVersions
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}


def zwaveEvent(hubitat.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}


def refresh() {
	delayBetween([
		zwave.switchBinaryV1.switchBinaryGet().format(),
		zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	])
}

def invertSwitch(invert=true) {
	if (invert) {
		zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
	}
	else {
		zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
	}
}