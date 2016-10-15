/**
 *  MyQ Garage Door
 *
 *  Copyright 2014 Adam Heinmiller
 *
 *  Updated per License by Barry A. Burke
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
 
 
/*  
Installation:
Create the Device Type:
	1.  Create a new device type, the name can be anything
	2.  Paste this code into the new device and save
	3.  Publish the device type, "For Me"
Add a new Device:
	1.  Create a new device, name it something appropriate
	2.  Give it a unique Device Network ID
	3.  Select "MyQ Garage Door" as the Type (should be at the bottom)
	4.  Make sure "Published" is selected as the Version
	5.  Select the Location, Hub, etc.
	6.  Click Create
	
Setup your Garage Door:
	1.  Get your Username, Password and Door Name used in the MyQ mobile app
	2.  Edit your new device's Preferences and enter the information above
	
If everything worked correctly, the door should retrieve the current status.  If you see "Unknown" there is probably an issue with your username and password; use the logs to capture error information.  If you see "Door not Found" your garage door name is not correct.
*/ 

   
preferences 
{
    input("username", "text", title: "Username", description: "MyQ username (email address)")
    input("password", "password", title: "Password", description: "MyQ password")
    input("door_name", "text", title: "Door Name", description: "MyQ Garage Door name or Device ID")
    input("is_craftsman", "enum", title: "Is your opener a Craftsman Assurelink?", metadata:[values:["Yes","No"]])
}

metadata 
{
	definition (name: "MyQ Garage Door", author: "Adam Heinmiller") 
    {
		capability "Polling"
        capability "Switch"
        capability "Switch Level"
        capability "Refresh"
        capability "Contact Sensor"
        capability "Sensor"
        capability "Momentary"
        capability "Actuator"
        
        attribute "doorStatus", "string"  // No longer used - we are exp[ected to report door status in "device.status"
//        attribute "vacationStatus", "string"
        attribute "lastDoorAction", "string"
        
        command "open"
        command "close"
        command "login"
        command "getDevice"
        command "getDoorStatus"
        command "openDoor"
        command "closeDoor"
        command "refresh"
        command "poll"
	}

	simulator 
    {
		// TODO: define status and reply messages here
	}

	tiles
    {    

		standardTile("sDoorToggle", "device.status", width: 1, height: 1, canChangeIcon: true) 
		{
			state "unknown", label: 'Unknown', icon: "st.unknown.unknown.unknown", action: "refresh.refresh", backgroundColor: "#afafaf"
			state "door_not_found", label:'Not Found', backgroundColor: "#CC1821" 
			state "stopped", label: 'Stopped', icon: "st.contact.contact.open", action: "close", backgroundColor: "#cc0000"
			state "closed", label: 'Closed', icon:"st.doors.garage.garage-closed", action: "open", backgroundColor: "#79b821"
			state "closing", label: 'Closing', icon:"st.doors.garage.garage-closing", backgroundColor: "#ffe71e"
			state "open", label: 'Open', icon:"st.doors.garage.garage-open", action: "close", backgroundColor: "#ffa81e"
			state "opening", label: 'Opening', icon:"st.doors.garage.garage-opening", backgroundColor: "#ffe71e"
			state "pending", label: 'Pending', icon:"st.doors.garage.garage-open", backgroundColor: "#ffe71e"
            state "moving", label: 'Moving', icon: "st.motion.motion.active", action: "refresh.refresh", backgroundColor: "#ffe71e"
		}

        standardTile("refresh", "device.status", inactiveLabel: false, decoration: "flat") 
        {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        standardTile("sContact", "device.contact")
        {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
        }

        valueTile("vLastDoorAction", "device.lastDoorAction", width: 2, height: 1, decoration: "flat")
        {
        	state "default", label: '${currentValue}'
        }
        
/*
		standardTile("sLogin", "device.switch", inactiveLabel: false, decoration: "flat") 
        {
			state "default", label:'Login', action:"login"
		}
		standardTile("sGetDeviceInfo", "device.switch", inactiveLabel: false, decoration: "flat") 
        {
			state "default", label:'GetDevices', action:"getDevice"
		}
		standardTile("sGetDoorStatus", "device.switch", inactiveLabel: false, decoration: "flat") 
        {
			state "default", label:'GetStatus', action:"getDoorStatus"
		}
		standardTile("sOpenDoor", "device.switch", inactiveLabel: false, decoration: "flat") 
        {
			state "default", label:'OpenDoor', action:"open"
		}
        
		standardTile("sCloseDoor", "device.switch", inactiveLabel: false, decoration: "flat") 
        {
			state "default", label:'CloseDoor', action:"close"
		}
*/
		
        def debugDetailTiles = [] // + ["sContact", "sLogin", "sGetDeviceInfo", "sGetDoorStatus", "sOpenDoor", "sCloseDoor"]
        		
        main(["sDoorToggle"])
        details(["sDoorToggle", "vLastDoorAction", "sContact", "refresh"] + debugDetailTiles)
    }

}

// parse events into attributes
def parse(String description) 
{}


def installed() {
	log.debug "Installing MyQ Garage Door"

	state.Login = [ BrandID: "Chamberlain", Expiration: 0 ]
    state.DeviceID = 0
    initialize()
    
    //disable checking of login when first installing the app
    //checkLogin()
    //refresh()
}


def updated() {
	log.debug "Updating MyQ Garage Door"
    
	state.Login.Expiration = 0
    state.DeviceID = 0
	initialize()
    
    checkLogin()
    refresh()
}

def initialize() {
     state.lastDoorStatus = "unknown"
     state.lastContactStatus = "unknown"
     state.lastActionLabel = "unknown"
}

// handle commands
def poll() 
{
    refresh()   
}

def push() {
	def cStatus
    
    checkLogin()
    
	getDoorStatus() { dStatus -> cStatus = dStatus }
    log.debug "Push: doorStatus is $cStatus"
	
	if ( cStatus == "open") { 
		close() 
		return
	}
	if ( cStatus == "closed") {
		open()
        return
	}
}

def on()
{
	open()
}

def off()
{
	close()
}

def refresh()
{    
	checkLogin()
    
    getDoorStatus() { dStatus ->
        log.debug "In refresh(), door status is $dStatus"
   		
        setDoorState(dStatus)
       	setContactSensorState(dStatus)
   	}
}

def open()
{
	log.debug "Opening Door"

    def dInitStatus
    def dCurrentStatus = "opening"
    def dPendStatus = "foo"
    def cmd = []
	cmd << "delay 1000"
    
    checkLogin()
    getDoorStatus() { dStatus -> dInitStatus = dStatus }
    log.debug "InitStatus = ${dInitStatus}"
    
	if (dInitStatus == "opening" || dInitStatus == "open" || dInitStatus == "moving") { return }


	setDoorState("opening", true)

    openDoor()
    
    while (dPendStatus != "opening") { 		// Wait until the door actually reports "opening"
    	cmd
        getDoorStatus() { dStatus -> dPendStatus = dStatus }
    }

	while (dCurrentStatus != "open")		// Now wait until the door tells us it is actually open
    {
		sleepForDuration(1000) {
        	getDoorStatus(dInitStatus) { dStatus -> dCurrentStatus = dStatus }
        }
    }

    
	log.debug "Final Door Status: $dCurrentStatus"

	setDoorState(dCurrentStatus, true)
	setContactSensorState(dCurrentStatus, true)
	
//   	cmd = []
//    cmd << "delay 2500"
//	cmd << refresh()
//	cmd
}

def close()
{
	log.debug "Closing Door"
    
	def dInitStatus
    def dPendStatus = "pending"
    def dTotalSleep = 0
    def dMaxSleep = 15000 // enough for an 8-foot doo
    def cmd = []
	cmd << "delay 1000"
    
   	checkLogin()
    getDoorStatus() { dStatus -> dInitStatus = dStatus }
    refresh()
                   
	if (dInitStatus == "closing" || dInitStatus == "closed" || dInitStatus == "moving") { return  }

	setDoorState("pending", true)
    closeDoor()
    
    while ((dPendStatus != "closing") && (dPendStatus != "closed")) { // Wait until the door reports "closing"
    	cmd
        getDoorStatus() { dStatus -> dPendStatus = dStatus }
    }

    setDoorState(dPendStatus, true)
    def dCurrentStatus = dPendStatus
    
	while ((dCurrentStatus != "closed") && (dTotalSleep <= dMaxSleep))
    {
		sleepForDuration(1000) {
            dTotalSleep += it
        	getDoorStatus(dInitStatus) { dStatus -> dCurrentStatus = dStatus }
        }
    }
    
    if (dTotalSleep > dMaxSleep) {
    	log.debug "Exceeded Door Close time: $dTotalSleep"
        log.debug "Ending status = $dCurrentStatus"
    }

	log.debug "Final Door Status: $dCurrentStatus"

	setDoorState(dCurrentStatus, true)
	setContactSensorState(dCurrentStatus, true)
	
//	cmd = []
//    cmd << "delay 2500"
//	cmd << refresh()
//	cmd
}

def checkLogin()
{
	//log.debug "Checking Login Credentials"

	if (state.Login.Expiration <= new Date().getTime())
    {
    	login()        
    }
    
    if (state.DeviceID == 0)
    {    	
    	getDevice()
    }
}

def login()
{
	log.debug "Logging In to Webservice"

	def loginQParams = [
		username: settings.username,
        password: settings.password,
        culture: "en"
    ]

    callApiGet("api/user/validatewithculture", [], loginQParams) { response ->
        state.Login = [
            BrandID: response.data.BrandName,
            UserID: response.data.UserId,
            SecToken: response.data.SecurityToken,
            Expiration: (new Date()).getTime() + 300000
        ]
		log.debug "Sec Token: $state.Login.SecToken"
    }
}

def getDevice()
{
	log.debug "Getting MyQ Devices"
    
    // If we set a door name that looks like a device id, use it as a device id
    if ((settings.door_name ?: "blank").isLong() == true) {
    	log.debug "Door Name:  Assuming Door Name is a Device ID, $settings.door_name"
        state.DeviceID = settings.door_name
        return
    }
       
    def loginQParams = [
		securityToken: state.Login.SecToken
    ]
	
    callApiGet("api/userdevicedetails/get", [], loginQParams) { response ->
        
        def garageDevices = response.getData().Devices.findAll{ it.TypeId == 47 || it.TypeID == 259 }
		def allDevices = response.getData().Devices
        
        // Find all devices on MyQ Account
        allDevices.each { pDevice ->
        	def dDeviceName = pDevice.Attributes.find{ it.Name == "desc" }?.Value ?: "Home"
            def dTypeID = pDevice?.TypeId
            def dDeviceID = pDevice?.DeviceId            
        
        	log.debug "Device Discovered:  Type ID: $dTypeID, Device Name: $dDeviceName, Device ID: $dDeviceID"        
        }
        
		if (garageDevices.isEmpty() == true) {
			log.debug "Device Discovery found no supported door devices"
    		setDoorState("door_not_found")
			return
        }

        state.DeviceID = 0

		garageDevices.each{ pDevice ->
        	def doorAttrib = pDevice.Attributes.find{ it.Name == "desc" }
        	if (doorAttrib.Value.toLowerCase() == settings.door_name.toLowerCase()) {
            	log.debug "Door ID: $pDevice.DeviceId"
				state.DeviceID = pDevice.DeviceId
            }
        }
        
        if (state.DeviceID == 0) {
        	log.debug "Supported door devices were found but none matched name '$settings.door_name'"
        }
    }
}

def getDoorStatus(initialStatus = null, callback)
{
    def loginQParams = [
		securityToken: state.Login.SecToken,
        devId: state.DeviceID,
        name: "doorstate"
    ]

	callApiGet("api/deviceattribute/getdeviceattribute", [], loginQParams) { response ->
        
    	def doorState = translateDoorStatus( response.data.AttributeValue, initialStatus )
		calcLastActivityTime( response.data.UpdatedTime.toLong() ) 	// this is apparently the time the door started moving
        															// not the time we sent the last command. For "close", 
                                                                    // (at least some) doors will BEEP for a few seconds before
                                                                    // actually moving the door.                                                     
        callback(doorState)        
    }
}


def calcLastActivityTime(lastActivity)
{
	def currentTime = new Date().getTime()
	def diffTotal = currentTime - lastActivity
                
	def lastActLabel = ""
        
	//diffTotal = (86400000 * 12) + (3600000 * 2) + (60000 * 1)
        
	def diffDays = (diffTotal / 86400000) as long
	def diffHours = (diffTotal % 86400000 / 3600000) as long
    def diffMinutes = (diffTotal % 86400000 % 3600000 / 60000) as long
    def diffSeconds = (diffTotal % 86400000 % 3600000 % 60000 / 1000) as long
        
        
	if (diffDays == 1) lastActLabel += "${diffDays} Day"
	else if (diffDays > 1) lastActLabel += "${diffDays} Days"
        
	if (diffDays > 0 && diffHours > 0) lastActLabel += ", "
        
	if (diffHours == 1) lastActLabel += "${diffHours} Hour"
	else if (diffHours > 1) lastActLabel += "${diffHours} Hours"

	if (diffDays == 0 && diffHours > 0 && diffMinutes > 0) lastActLabel += ", "

	if (diffDays == 0 && diffMinutes == 1) lastActLabel += "${diffMinutes} Minute"
	if (diffDays == 0 && diffMinutes > 1) lastActLabel += "${diffMinutes} Minutes"

	if (diffTotal < 60000) lastActLabel = "${diffSeconds} Seconds"
    
    if (lastActLabel != state.lastActionLabel) {
    	state.lastActionLabel = lastActLabel
	    sendEvent(name: "lastDoorAction", value: lastActLabel, descriptionText: "$lastActLabel", display: true, isStateChange: true)
    }
}


def openDoor()
{ 	
    def loginQParams = [
		
        AttributeValue: "1",
        AttributeName: "desireddoorstate"
    ]

	callApiPut("api/deviceattribute/putdeviceattribute", [], loginQParams) { response ->
        // if error, do something?
	}
}


def closeDoor()
{ 	
    def loginQParams = [
        AttributeValue: "0",
        AttributeName: "desireddoorstate"
    ]

	callApiPut("api/deviceattribute/putdeviceattribute", [], loginQParams) { response ->
        // if error, do something?
	}
}


def setContactSensorState(newStatus, isStateChangeX = false)
{
    def chg = false
   	if (newStatus != state.lastContactStatus) {
   		chg = true
   		state.lastContactStatus = newStatus
   	}



    if (newStatus == "closed") {
    	log.debug "Setting contact/switch status to $newStatus, state is $chg"
    	sendEvent(name: "contact", value: "closed", display: true, isStateChange: chg, descriptionText: "Contact is closed")
        sendEvent(name: "switch", value: "off", display: true, isStateChange: chg, descriptionText: "Switch is off")
    }
    else if (newStatus == "open") {
    	log.debug "Setting contact/switch status to $newStatus, state is $chg"
		sendEvent(name: "contact", value: "open", display: true, isStateChange: chg, descriptionText: "Contact is open")
        sendEvent(name: "switch", value: "on", display: true, isStateChange: chg, descriptionText: "Switch is on")
    }
}


def setDoorState(newStatus, isStateChangeX = false)
{ 
    if (newStatus != state.lastDoorStatus) {
		log.debug "Setting door status to $newStatus, state is true"
		state.lastDoorStatus = newStatus
        sendEvent(name: "status", value: "${newStatus}", isStateChange: true, display: true, descriptionText: "Door is $newStatus")
    }
    else {
		log.debug "Setting door status to $newStatus, state is false"
		sendEvent(name: "status", value: "${newStatus}", display: true, descriptionText: "Door is $newStatus")
    }
}


def translateDoorStatus(iStatus, initStatus = null)
{
	def dReturn = "unknown"
    
	if (iStatus == "2") dReturn = "closed"
	else if (iStatus == "1" || iStatus == "9") {
    	if (initStatus == "pending") {
        	dReturn = "pending"
        }
        else { dReturn = "open" }
    }
	else if (iStatus == "4" || (iStatus == "8" && initStatus == "closed")) dReturn = "opening"
	else if (iStatus == "5" || (iStatus == "8" && initStatus == "open")) dReturn = "closing"
    else if (iStatus == "5" || (iStatus == "8" && initStatus == "pending")) dReturn = "closing"
    else if (iStatus == "3") dReturn = "stopped"
    else if (iStatus == "8" && initStatus == null) dReturn = "moving"
    
    if (dReturn == "unknown") { log.error "Unknown Door Status ID: $iStatus" }

	return dReturn
}

def sleepForDuration(duration, callback = {})
{
	def dTotalSleep = 0
	def dStart = new Date().getTime()
    def cmds = []
	cmds << "delay 1000"
    cmds << refresh()
    
    while (dTotalSleep <= duration)
    {            
		cmds
        dTotalSleep = (new Date().getTime() - dStart)
    }

    log.debug "Slept ${dTotalSleep}ms"

	callback(dTotalSleep)
}


def callApiPut(apipath, headers = [], queryParams = [], callback = {})
{
	def baseURL = "https://myqexternal.myqdevice.com/"
    
	def finalHeaders = [
    	"User-Agent": "${state.Login.BrandID}/1332 (iPhone; iOS 7.1.1; Scale/2.00)"
    ] + headers


    def finalQParams = [
    
    	//ApplicationId: "NWknvuBd7LoFHfXmKNMBcgajXtZEgKUh4V7WNzMidrpUUluDpVYVZx+xT4PCM5Kx",
    	ApplicationId : getVarParams().AppID,
        DeviceId: state.DeviceID,
    	securityToken: state.Login.SecToken
        
    ] + queryParams
   
    def finalParams = [ 
    
    	//uri: baseURL,
    	uri: getVarParams().BaseURL, 
        path: apipath, 
        headers: finalHeaders,
        contentType: "application/json; charset=utf-8",
        body: finalQParams
	]
    
	//log.debug finalParams
    
    try
    {
    	httpPut(finalParams) { response ->
        
        	if (response.data.ErrorMessage) {
            	log.debug "API Error: $response.data"
            }
            callback(response)
        }
    }
    catch (Error e)
    {
		log.debug "APIput error: $e"
//		setDoorState("unknown", true)
    }
    finally
    {
    }
}

//Switch base on the setting flag controll MyQ vs. Craftsmand
def getVarParams() {
	def baseURL = "https://myqexternal.myqdevice.com/"
	def dParams = [
		BaseURL: "https://myqexternal.myqdevice.com/",
        AppID: "NWknvuBd7LoFHfXmKNMBcgajXtZEgKUh4V7WNzMidrpUUluDpVYVZx+xT4PCM5Kx"
	]
    if ((settings.is_craftsman ?: "No") == "Yes") {    
		dParams.BaseURL = "https://craftexternal.myqdevice.com/"
        dParams.AppID = "eU97d99kMG4t3STJZO/Mu2wt69yTQwM0WXZA5oZ74/ascQ2xQrLD/yjeVhEQccBZ"
    }
	return dParams
}

def callApiGet(apipath, headers = [], queryParams = [], callback = {})
{
	def baseURL = "https://myqexternal.myqdevice.com/"
    
    def finalHeaders = [
    	"User-Agent": "${state.Login.BrandID}/1332 (iPhone; iOS 7.1.1; Scale/2.00)"
    ] + headers
    
    def finalQParams = [
	//appId : "NWknvuBd7LoFHfXmKNMBcgajXtZEgKUh4V7WNzMidrpUUluDpVYVZx+xT4PCM5Kx",
    	appId: getVarParams().AppID,
        filterOn: "true"
    
    ] + queryParams
    
    def finalParams = [ 
    
    	//uri: baseURL,
    	uri: getVarParams().BaseURL, 
        path: apipath, 
        headers: finalHeaders,
        query: finalQParams
	]
    
    //log.debug finalParams

    try
    {
    	httpGet(finalParams) { response ->
        
        	if (response.data.ErrorMessage) {
            
            	log.debug "API Error: $response.data"
            }
            
            callback(response)
        }
    }
    catch (Error e)
    {
    	log.debug "APIget error: $e"
//		setDoorState("unknown", true)
    }
    finally
    {
    }
}