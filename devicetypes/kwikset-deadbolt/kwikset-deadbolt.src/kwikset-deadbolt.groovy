/**
 *  Kwikset Deadbolt
 *
 *  Copyright 2014 Korban Hadley
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
	// Automatically generated. Make future change here.
	definition (name: "Kwikset Deadbolt", namespace: "Kwikset Deadbolt", author: "Korban Hadley") {
		capability "Configuration"
		capability "Lock Codes"
		capability "Polling"
		capability "Battery"
		capability "Lock"

		attribute "user1", "string"
		attribute "code1", "string"

		command "usercodechange"
	}

	definition (name: "Kwikset Deadbolt", namespace: "Kwikset Deadbolt", author: "Korban Hadley") {
		capability "Battery"
		capability "Polling"
		capability "Configuration"
		capability "Lock"
		capability "Lock Codes"

		attribute "user1", "string"
		attribute "code1", "string"

		command "usercodechange"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'battery' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'user1' attribute
	// TODO: handle 'code1' attribute

}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
}

def configure() {
	log.debug "Executing 'configure'"
	// TODO: handle 'configure' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
}

def updateCodes() {
	log.debug "Executing 'updateCodes'"
	// TODO: handle 'updateCodes' command
}

def setCode() {
	log.debug "Executing 'setCode'"
	// TODO: handle 'setCode' command
}

def deleteCode() {
	log.debug "Executing 'deleteCode'"
	// TODO: handle 'deleteCode' command
}

def requestCode() {
	log.debug "Executing 'requestCode'"
	// TODO: handle 'requestCode' command
}

def reloadAllCodes() {
	log.debug "Executing 'reloadAllCodes'"
	// TODO: handle 'reloadAllCodes' command
}

def usercodechange() {
	log.debug "Executing 'usercodechange'"
	// TODO: handle 'usercodechange' command
}


