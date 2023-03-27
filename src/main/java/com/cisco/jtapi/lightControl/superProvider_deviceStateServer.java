package com.cisco.jtapi.lightControl;

// Copyright (c) 2023 Cisco and/or its affiliates.
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

// Builds off Jtapi Examples to create a system to monitor Cisco phones via jtapi
// to flash an indicator light in different patterns depending on the state of the
// phone
// Enter the CUCM server IP and jtapi username and password in the .env file
// Load the names of the Cisco phones and the URL of the light to be associated
// in the monitorList.csv file


import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import javax.telephony.*;
import java.util.*;
import java.io.*;
import com.cisco.jtapi.extensions.*;
import io.github.cdimascio.dotenv.Dotenv;

public class superProvider_deviceStateServer {

 
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SS");

    private static void log(String msg) {
        System.out.println(dtf.format(LocalDateTime.now()) + " " + msg);
    }
    
    // Create a hash map to get friendly names for the device states
    public static Map <Integer, String> stateName = new HashMap<Integer, String>();

     //get list of devices to monitor
    public static Map<String, String> deviceList = HashMapFromTextFile();
  

    public static void main(String[] args) throws

    JtapiPeerUnavailableException, ResourceUnavailableException, MethodNotSupportedException, InvalidArgumentException,
            PrivilegeViolationException, InvalidPartyException, InvalidStateException, InterruptedException {

        stateName.put(CiscoTerminal.DEVICESTATE_IDLE, "IDLE");
        stateName.put(CiscoTerminal.DEVICESTATE_ACTIVE, "ACTIVE");
        stateName.put(CiscoTerminal.DEVICESTATE_ALERTING, "ALERTING");
        stateName.put(CiscoTerminal.DEVICESTATE_HELD, "HELD");
        stateName.put(CiscoTerminal.DEVICESTATE_WHISPER, "WHISPER");
        stateName.put(CiscoTerminal.DEVICESTATE_UNKNOWN, "UNKNOWN");
            
        // Retrieve environment variables from .env, if present
        Dotenv dotenv=Dotenv.load();

        

        // iterate over device list HashMap entries
        for (Map.Entry<String, String> entry :
             deviceList.entrySet()) {
            System.out.println(entry.getKey() + " : "
                               + entry.getValue());
        }

 
        // The Handler class provides observers for provider/address/terminal/call events
        Handler handler = new Handler();

        // Create the JtapiPeer object, representing the JTAPI library
        log("Initializing Jtapi");
        CiscoJtapiPeer peer = (CiscoJtapiPeer) JtapiPeerFactory.getJtapiPeer(null);

        // Create and open the Provider, representing a JTAPI connection to CUCM CTI
        // Manager
        String providerString = String.format(
            "%s;login=%s;passwd=%s",
            dotenv.get("CUCM_ADDRESS"),
            dotenv.get("JTAPI_USERNAME"),
            dotenv.get("JTAPI_PASSWORD"));
        log("Connecting Provider: " + providerString);
        CiscoProvider provider=(CiscoProvider) peer.getProvider(providerString);
        log("Awaiting ProvInServiceEv...");
        provider.addObserver(handler);
        handler.providerInService.waitTrue();

       
        

        //Create Array list to hold the terminal objects
        List<CiscoTerminal> terminalList = new ArrayList<CiscoTerminal>();

        
        //create a Terminal for each device in the deviceList (from CSV)
        for (Map.Entry<String, String> entry : deviceList.entrySet()) {
                log("Creating phoneTerminal using device name: " + entry.getKey());
                terminalList.add((CiscoTerminal) provider.createTerminal((entry.getKey()))); 
        }
        
        //add observer and set filters for each terminal
        for (int i = 0; i < terminalList.size(); i++){
           //add observer for each terminal
           terminalList.get(i).addObserver(handler);

           //add filters for each terminal
           CiscoTermEvFilter termFilter = terminalList.get(i).getFilter();

            termFilter.setDeviceStateIdleEvFilter(true);
            termFilter.setDeviceStateActiveEvFilter(true);
            termFilter.setDeviceStateAlertingEvFilter(true);
            termFilter.setDeviceStateHeldEvFilter(true);
            termFilter.setDeviceStateWhisperEvFilter(false); 

            terminalList.get(i).setFilter(termFilter);

            //log terminal montior
            log("Monitoring state changes for: "+terminalList.get(i).getName()+"...");

        }
            
    }

    public static Map<String, String> HashMapFromTextFile()
    {

        Map<String, String> map
            = new HashMap<String, String>();
        BufferedReader br = null;

        try {

            // create file object
            File file = new File("src/main/java/com/cisco/jtapi/superProvider_deviceStateServer/monitorList.csv");

            // create BufferedReader object from the File
            br = new BufferedReader(new FileReader(file));

            String line = null;

            // read file line by line
            while ((line = br.readLine()) != null) {

                // split the line by :
                String[] parts = line.split(",");

                // first part is device, second is url
                String device = parts[0].trim();
                String url = parts[1].trim();

                // put device, url in HashMap if they are
                // not empty
                if (!device.equals("") && !url.equals(""))
                    map.put(device, url);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {

            // Always close the BufferedReader
            if (br != null) {
                try {
                    br.close();
                }
                catch (Exception e) {
                };
            }
        }

        return map;
    }
}
