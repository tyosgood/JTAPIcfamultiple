/*
Install this script on the Shelly relay devices.  It creates urls that the jtapi program calls to actually
flash the lights.
*/


let timer_handle;
let count = 1;

//function for ringing (alerting)
function alerting(request, response){
  response.code = 200;
  response.send();

  function callback(userdata) {
    Shelly.call("Switch.Toggle",{ "id":0 },null,null);
  }
  Timer.clear(timer_handle);
  timer_handle = Timer.set(500,true,callback,null); 
}

//function for on-hold
function held(request, response){
  response.code = 200;
  response.send();

  function callback(userdata) {
    Shelly.call("Switch.Toggle",{ "id":0 },null,null);
  }
  Timer.clear(timer_handle);
  timer_handle = Timer.set(1500,true,callback,null); 
}

//function for idle 
function idle(request, response){
  response.code = 200;
  response.send();
  Timer.clear(timer_handle);
  Shelly.call("Switch.set", {"id": 0, "on": false}, null);
}

//function for active call
function active(request, response){
  response.code = 200;
  response.send();
  Timer.clear(timer_handle);
  
 // active_callback(null);
  //comment out the line above and un-comment the below to change the active behavior to steady on (for demos)
      Shelly.call("Switch.set", {"id": 0, "on": true}, null);
}

//function to implement the two short one long cadence for active calls
function active_callback(userdata) {
   count++;
  // two short flashes, one long    
  if (count % 6 === 0) {
    // one long flash
    Shelly.call("Switch.set", {"id": 0, "on": true}, null);
    timer_handle = Timer.set(1250,false,active_callback,null);
    count = 0; 
  } else {
    // two short flashes (on - off - on - off)
    Shelly.call("Switch.Toggle",{ "id":0 },null,null);
    timer_handle = Timer.set(250,false,active_callback,null); 
  }
}


//activate the URLs to call the above functions
HTTPServer.registerEndpoint("alerting", alerting);
HTTPServer.registerEndpoint("held", held);
HTTPServer.registerEndpoint("idle", idle);
HTTPServer.registerEndpoint("active", active);